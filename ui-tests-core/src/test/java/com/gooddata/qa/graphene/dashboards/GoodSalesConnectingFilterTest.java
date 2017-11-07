package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.asserts.AssertUtils.assertHeadersEqual;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createFilterVariable;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;

public class GoodSalesConnectingFilterTest extends GoodSalesAbstractTest {

    private static final String REPORT_1 = "Report1";
    private static final String REPORT_2 = "Report2";

    private static final String V_STAGE = "VStage";

    private static final String TEST_DASHBOARD = "TestDashboard";
    private static final String TMP_DASHBOARD = "TmpDashboard";

    private static final String DB1 = "DB1";
    private static final String DB2 = "DB2";

    private static final int YEAR_OF_DATA = 2011;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-connecting-filter";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();

        createFilterVariable(getRestApiClient(), testParams.getProjectId(),
                V_STAGE, getAttributeByTitle(ATTR_STAGE_NAME).getUri());

        createReport(GridReportDefinitionContent.create(REPORT_1,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(REPORT_2,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
        initReportsPage().openReport(REPORT_2);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter(V_STAGE, "Interest", "Discovery",
                "Short List", "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost"));
        reportPage.saveReport();

        initDashboardsPage();

        dashboardsPage.addNewDashboard(TEST_DASHBOARD);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        addReportsAndFilters(dashboardEditBar, String.format("%s ago", 
                Calendar.getInstance().get(Calendar.YEAR) - YEAR_OF_DATA));
        dashboardsPage.addNewTab("tab 2");
        addReportsAndFilters(dashboardEditBar, String.format("%s ago", 
                Calendar.getInstance().get(Calendar.YEAR) - YEAR_OF_DATA));
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void linkDashboardFilterBetweenTabs() {
        makeCopyFromDashboard(TEST_DASHBOARD);

        try {
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME))
                .changeAttributeFilterValues("Negotiation");

            Sleeper.sleepTightInSeconds(3);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Negotiation");
            assertHeadersEqual(asList("Negotiation","2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertHeadersEqual(asList("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements());

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Negotiation");
            Sleeper.sleepTightInSeconds(3);
            assertHeadersEqual(asList("Negotiation", "2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertHeadersEqual(asList("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements());

            dashboardsPage.editDashboard();
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).changeSelectionToOneValue();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).changeSelectionToOneValue();
            dashboardsPage.saveDashboard();
            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Interest");
            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Interest");

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValues("Conviction");

            Sleeper.sleepTightInSeconds(3);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Conviction");

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Conviction");

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget("filter-time")
                .changeTimeFilterValueByClickInTimeLine("2015");
            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget("filter-time").getCurrentValue(), "2015");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disconnectBetweenSingleOptionAndMultipleFilter() {
        makeCopyFromDashboard(TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME))
                .changeAttributeFilterValues("Discovery");
            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME))
                    .getCurrentValue(), "Discovery");

            dashboardsPage.editDashboard();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValues("Short List");
            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                    .getCurrentValue(), "Short List");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterInDuplicateTab() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TMP_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            addReportsAndFilters(dashboardEditBar, String.format("%s ago", 
                    Calendar.getInstance().get(Calendar.YEAR) - YEAR_OF_DATA));
            dashboardEditBar.saveDashboard();

            dashboardsPage.duplicateDashboardTab(0);
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME))
                .changeAttributeFilterValues("Negotiation");

            Sleeper.sleepTightInSeconds(3);
            assertHeadersEqual(asList("Negotiation", "2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertHeadersEqual(asList("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements());

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Negotiation");
            Sleeper.sleepTightInSeconds(3);
            assertHeadersEqual(asList("Negotiation", "2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertHeadersEqual(asList("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements());

            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValues("Conviction");
            Sleeper.sleepTightInSeconds(3);
            assertHeadersEqual(asList("Negotiation", "2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Conviction");
            Sleeper.sleepTightInSeconds(3);
            assertHeadersEqual(asList("Negotiation", "2011"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements());
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getContent().getFilterWidget("filter-time")
                .changeTimeFilterValueByClickInTimeLine("2013");
            Sleeper.sleepTightInSeconds(2);
            assertTrue(dashboardsPage.getContent().getReport("Report1", TableReport.class).isNoData());
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget("filter-time").getCurrentValue(), "2013");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterInCopyTab() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(DB1);
        dashboardsPage.addNewDashboard(DB2);

        try {
            dashboardsPage.selectDashboard(DB1);
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            addReportsAndFilters(dashboardEditBar);
            dashboardEditBar.saveDashboard();

            dashboardsPage.selectDashboard(DB2);
            dashboardsPage.editDashboard();
            dashboardEditBar = dashboardsPage.getDashboardEditBar();
            addReportsAndFilters(dashboardEditBar);
            dashboardEditBar.saveDashboard();

            dashboardsPage.selectDashboard(DB1);
            dashboardsPage.copyDashboardTab(0, DB2);

            dashboardsPage.selectDashboard(DB2);
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME))
                .changeAttributeFilterValues("Negotiation");
            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getCurrentValue(),
                    "Negotiation");

            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValues("Conviction");
            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Conviction");
        } finally {
            dashboardsPage.selectDashboard(DB1);
            dashboardsPage.deleteDashboard();

            dashboardsPage.selectDashboard(DB2);
            dashboardsPage.deleteDashboard();
        }
    }

    private void makeCopyFromDashboard(String dashboard) {
        initDashboardsPage();

        dashboardsPage.selectDashboard(dashboard);
        dashboardsPage.saveAsDashboard(TMP_DASHBOARD, PermissionType.USE_EXISTING_PERMISSIONS);
        dashboardsPage.selectDashboard(TMP_DASHBOARD);
    }

    private void addReportsAndFilters(DashboardEditBar dashboardEditBar, String timeRange)
            {
        addReportsAndFilters(dashboardEditBar);

        dashboardEditBar.addTimeFilterToDashboard(DATE_DIMENSION_SNAPSHOT, DateGranularity.YEAR, timeRange);
        WebElement filter = dashboardsPage.getContent().getFilterWidget("filter-time").getRoot();
        filter.click();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(filter);
    }

    private void addReportsAndFilters(DashboardEditBar dashboardEditBar) {
        dashboardEditBar.addReportToDashboard(REPORT_1);
        WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

        dashboardEditBar.addReportToDashboard(REPORT_2);
        report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(report);

        dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getRoot();
        filter.click();
        DashboardWidgetDirection.UP.moveElementToRightPlace(filter);

        dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, V_STAGE);
        filter = dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getRoot();
        filter.click();
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(filter);
    }
}
