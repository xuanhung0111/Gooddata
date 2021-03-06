package com.gooddata.qa.graphene.filters;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.SelectionConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.List;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DashboardFiscalDateFilterTest extends AbstractDashboardWidgetTest {

    private static final String FILTER_NAME = "DATE (SALARY)";
    private static final String INPUT_YEAR = "FY2007";
    private static final String DASHBOARD_NAME = "Fiscal filter test";
    private static final String THIS = "this";
    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;

    @Override
    public void initProperties() {
        // use empty project
        projectTitle = "Fiscal-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        URL maqlResource = getClass().getResource("/fiscal-date/maql.txt");
        postMAQL(IOUtils.toString(maqlResource), STATUS_POLLING_CHECK_ITERATIONS);

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);
        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);

        initDashboardsPage().addNewDashboard(DASHBOARD_NAME)
                .editDashboard()
                .addTimeFilterToDashboard(TimeFilterPanel.DateGranularity.YEAR, "this")
                .saveDashboard();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldShowCustomLabels() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "Display fiscal filter label incorrectly.");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "Display fiscal filter label on timeline incorrectly.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldShowCustomLabelsInViewMode() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "Display fiscal filter label incorrectly.");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "Display fiscal filter label on timeline incorrectly.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldShowCustomLabelsForOtherTypes() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);

        // Quarter
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.QUARTER, THIS);
        int quarter = Integer.parseInt(timeFilter.getCurrentValue().split("/")[0], 10);
        assertTrue((quarter >= 1) && (quarter <= 4), "Display fiscal filter label for quarter incorrectly.");

        // Month
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.MONTH, THIS);
        assertTrue(timeFilter.getCurrentValue().matches("\\D{3}\\s\\d{4}"), "Display fiscal filter label for month incorrectly.");

        // Week
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.WEEK, THIS);
        assertTrue(timeFilter.getCurrentValue().startsWith("W"), "Display fiscal filter label for week incorrectly.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldLoadCorrectFromToFiscalDate() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        System.out.println("date description " + timeFilter.getCurrentValue());
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "Display fiscal filter label incorrectly.");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel().selectTimeLine(INPUT_YEAR);
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "Display fiscal filter label on timeline incorrectly.");
        assertEquals(timeFilterPanel.getFromValue(), "01/01/2007");
        assertEquals(timeFilterPanel.getToValue(), "12/30/2007");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldAppliedFiscalFilterForReport() {
        String reportName = "Sum Of Payments";
        String metricName = "Sum Of Payments";

        createMetric(metricName,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))),
                "#,##0.00");

        createReport(new UiReportDefinition()
                        .withName(reportName)
                        .withWhats(metricName)
                        .withHows("Department", "Year (Salary)"),
                reportName);

        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        dashboardsPage.addReportToDashboard(reportName);

        TableReport report = getReport(reportName);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report.getRoot());
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(report.getRoot());

        FilterWidget timeFilter = getFilter(FILTER_NAME);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(timeFilter.getRoot());

        timeFilter.openPanel()
                .getTimeFilterPanel()
                .selectTimeLine(INPUT_YEAR)
                .submit();

        assertTrue(report.getAttributeValues().toString().contains(INPUT_YEAR), INPUT_YEAR + " should be displayed in the report.");
        assertFalse(report.getAttributeValues().toString().contains("FY2006"), "FY2006 shouldn't displayed in the report.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableDatePicker() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        configureSelectionDateFilter(timeFilter.getRoot(), false, true);

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        assertFalse(timeFilterPanel.isDatePickerIconPresent(), "Calendar icon shouldn't be displayed.");

        timeFilterPanel.clickOnFromInput();
        assertFalse(timeFilterPanel.isDatePickerPresent(), "Date picker shouldn't be displayed.");

        timeFilterPanel.clickOnToInput();
        assertFalse(timeFilterPanel.isDatePickerPresent(), "Date picker shouldn't be displayed.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enableDatePicker() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        configureSelectionDateFilter(timeFilter.getRoot(), false, false);

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        assertTrue(timeFilterPanel.isDatePickerIconPresent(), "Calendar icon isn't displayed.");

        timeFilterPanel.clickOnFromInput();
        assertTrue(timeFilterPanel.isDatePickerPresent(), "Date picker isn't displayed.");

        timeFilterPanel.clickOnToInput();
        assertTrue(timeFilterPanel.isDatePickerPresent(), "Date picker isn't displayed.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideFromTo() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);

        configureSelectionDateFilter(timeFilter.getRoot(), true, true);
        assertTrue(timeFilter.openPanel().getTimeFilterPanel().isFromToNotVisible(), "From/To inputs still be displayed.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDateConfigurationOnSelectionTab() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel.openConfigurationPanelFor(getFilter(FILTER_NAME).getRoot(), browser);

        SelectionConfigPanel selectionConfigPanel = widgetConfigPanel.getTab(Tab.SELECTION, SelectionConfigPanel.class);
        assertTrue(selectionConfigPanel.isHideFromToOptionEnabled(), "Wrong default configuration");
        assertTrue(selectionConfigPanel.isHideCalendarOptionEnabled(), "Wrong default configuration");

        selectionConfigPanel.setHideFromToOption(true);
        assertFalse(selectionConfigPanel.isHideCalendarOptionEnabled(), "Hidden date picker should be disabled");

        selectionConfigPanel.setHideFromToOption(false);
        selectionConfigPanel.setHideCalendarOption(true);
        assertTrue(selectionConfigPanel.isHideFromToOptionEnabled(), "Hidden date range should be enabled");

        selectionConfigPanel.setHideFromToOption(true);
        assertFalse(selectionConfigPanel.isHideCalendarOptionEnabled(), "Hidden date picker should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void defaultDateConfigurationOnSelectionTab() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel.openConfigurationPanelFor(getFilter(FILTER_NAME).getRoot(), browser);

        SelectionConfigPanel selectionConfigPanel = widgetConfigPanel.getTab(Tab.SELECTION, SelectionConfigPanel.class);
        assertFalse(selectionConfigPanel.isHideFromToOptionSelected(), "Should be uncheck");
        assertFalse(selectionConfigPanel.isHideCalendarOptionSelected(), "Should be uncheck");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void defaultSettingOnTimeFilterPanel() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();

        TimeFilterPanel timeFilterPanel = getFilter(FILTER_NAME).openPanel().getTimeFilterPanel();
        assertTrue(timeFilterPanel.isDatePickerIconPresent(), "Date picker should be displayed");
        assertTrue(timeFilterPanel.clickOnFromInput().isDatePickerPresent(), "Calender should be displayed");
        assertTrue(timeFilterPanel.clickOnToInput().isDatePickerPresent(), "Calender should be displayed");
    }

    private void configureSelectionDateFilter(WebElement timeFilter, boolean isHideDateRange, boolean isHideDatePicker) {
        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel.openConfigurationPanelFor(timeFilter, browser);
        widgetConfigPanel.getTab(Tab.SELECTION, SelectionConfigPanel.class)
                .setHideFromToOption(isHideDateRange)
                .setHideCalendarOption(isHideDatePicker);
        widgetConfigPanel.saveConfiguration();
    }

    private boolean isCustomYearLabels(List<String> labels) {
        return labels.stream().allMatch(timelineItem -> timelineItem.startsWith("FY"));
    }
}
