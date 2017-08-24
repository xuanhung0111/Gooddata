package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.postEtlPullIntegration;
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

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Fiscal-test";
        projectTemplate = "";
        expectedGoodSalesDashboardsAndTabs = null;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"fiscalDateInit"})
    public void loadProject() throws JSONException, URISyntaxException, IOException {
        URL maqlResource = getClass().getResource("/fiscal-date/maql.txt");
        postMAQL(IOUtils.toString(maqlResource), STATUS_POLLING_CHECK_ITERATIONS);

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);

        postEtlPullIntegration(getRestApiClient(), testParams.getProjectId(),
                webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
    }

    @Test(dependsOnMethods = {"loadProject"}, groups = {"fiscalDateInit"})
    public void prepareFiscalDateFilterTest() throws Throwable {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);
        prepareDashboardWithDateFilter();
    }

    @Test(dependsOnGroups = {"fiscalDateInit"})
    public void shouldShowCustomLabels() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "display fiscal filter label incorrectly");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "display fiscal filter label on timeline incorrectly");
    }

    @Test(dependsOnGroups = {"fiscalDateInit"})
    public void shouldShowCustomLabelsInViewMode() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "display fiscal filter label incorrectly");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel();
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "display fiscal filter label on timeline incorrectly");
    }

    @Test(dependsOnGroups = {"fiscalDateInit"})
    public void shouldShowCustomLabelsForOtherTypes() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME).editDashboard();
        FilterWidget timeFilter = getFilter(FILTER_NAME);

        // Quarter
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.QUARTER, THIS);
        int quarter = Integer.parseInt(timeFilter.getCurrentValue().split("/")[0].toString(), 10);
        assertTrue((quarter >= 1) && (quarter <= 4), "display fiscal filter label for quarter incorrectly.");

        // Month
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.MONTH, THIS);
        assertTrue(timeFilter.getCurrentValue().matches("\\D{3}\\s\\d{4}"), "display fiscal filter label for month incorrectly.");

        // Week
        timeFilter.editDefaultTimeFilterValue(TimeFilterPanel.DateGranularity.WEEK, THIS);
        assertTrue(timeFilter.getCurrentValue().startsWith("W"));
    }

    @Test(dependsOnGroups = {"fiscalDateInit"})
    public void shouldLoadCorrectFromToFiscalDate() {
        initDashboardsPage().selectDashboard(DASHBOARD_NAME);
        FilterWidget timeFilter = getFilter(FILTER_NAME);
        System.out.println("date description " + timeFilter.getCurrentValue());
        assertTrue(timeFilter.getCurrentValue().startsWith("FY"), "display fiscal filter label incorrectly");

        TimeFilterPanel timeFilterPanel = timeFilter.openPanel().getTimeFilterPanel().selectTimeLine(INPUT_YEAR);
        List<String> selectedTimelineItemNames = timeFilterPanel.getSelectedTimelineItemNames();

        assertTrue(isCustomYearLabels(selectedTimelineItemNames), "display fiscal filter label on timeline incorrectly");
        assertEquals(timeFilterPanel.getFromValue(), "01/01/2007");
        assertEquals(timeFilterPanel.getToValue(), "12/30/2007");
    }

    @Test(dependsOnGroups = {"fiscalDateInit"})
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
        report.waitForReportLoading();

        assertTrue(report.getAttributeElements().toString().contains(INPUT_YEAR));
        assertFalse(report.getAttributeElements().toString().contains("FY2006"));
    }

    private boolean isCustomYearLabels(List<String> labels) {
        return labels.stream().allMatch(timelineItem -> timelineItem.startsWith("FY"));
    }

    private void prepareDashboardWithDateFilter() throws Throwable {
        initDashboardsPage().addNewDashboard(DASHBOARD_NAME)
                .editDashboard()
                .addTimeFilterToDashboard(TimeFilterPanel.DateGranularity.YEAR, "this")
                .saveDashboard();
    }
}
