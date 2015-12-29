package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;

public class GoodSalesConnectingFilterTest extends GoodSalesAbstractTest {

    private static final String REPORT_1 = "Report1";
    private static final String REPORT_2 = "Report2";

    private static final String STAGE_NAME = "Stage Name";
    private static final String V_STAGE = "VStage";
    private static final String AMOUNT = "Amount";
    private static final String YEAR_SNAPSHOT = "Year (Snapshot)";

    private static final String TEST_DASHBOARD = "TestDashboard";
    private static final String TMP_DASHBOARD = "TmpDashboard";

    private static final String DB1 = "DB1";
    private static final String DB2 = "DB2";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-connecting-filter";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init"})
    public void createVariable() {
        initVariablePage();

        variablePage.createVariable(new AttributeVariable(V_STAGE).withAttribute(STAGE_NAME));
    }

    @Test(dependsOnMethods = {"createVariable"}, groups = {"init"})
    public void createReports() {
        // *** create report 1 ***
        initReportsPage();
        UiReportDefinition rd =
                new UiReportDefinition().withName(REPORT_1).withWhats(AMOUNT).withHows(STAGE_NAME)
                        .withHows(new HowItem(YEAR_SNAPSHOT, Position.TOP));
        createReport(rd, REPORT_1);

        // *** create report 2 ***
        initReportsPage();
        rd = new UiReportDefinition().withName(REPORT_2).withWhats(AMOUNT).withHows(STAGE_NAME);
        createReport(rd, REPORT_2);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter(V_STAGE, "Interest", "Discovery",
                "Short List", "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost"));
        reportPage.saveReport();
    }

    @Test(dependsOnMethods = {"createReports"}, groups = {"init"})
    public void prepareDashboard() {
        initDashboardsPage();

        dashboardsPage.addNewDashboard(TEST_DASHBOARD);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        addReportsAndFilters(dashboardEditBar, "4 ago");
        dashboardsPage.addNewTab("tab 2");
        addReportsAndFilters(dashboardEditBar, "4 ago");
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnGroups = {"init"})
    public void linkDashboardFilterBetweenTabs() {
        makeCopyFromDashboard(TEST_DASHBOARD);

        try {
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME))
                .changeAttributeFilterValue("Negotiation");

            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(isEqualCollection(singleton("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements()));

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getCurrentValue(),
                    "Negotiation");
            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(isEqualCollection(singleton("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements()));

            dashboardsPage.editDashboard();
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).changeSelectionToOneValue();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();
            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getCurrentValue(),
                    "Interest");
            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getCurrentValue(),
                    "Interest");

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValue("Conviction");
            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Conviction");

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget("filter-time")
                .changeTimeFilterValueByClickInTimeLine("2015");
            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget("filter-time").getCurrentValue(), "2015");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void disconnectBetweenSingleOptionAndMultipleFilter() {
        makeCopyFromDashboard(TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME))
                .changeAttributeFilterValue("Discovery");
            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME))
                    .getCurrentValue(), "Discovery");

            dashboardsPage.editDashboard();
            dashboardsPage.getTabs().openTab(1);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValue("Short List");
            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                    .getCurrentValue(), "Short List");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilterInDuplicateTab() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TMP_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            addReportsAndFilters(dashboardEditBar, "4 ago");
            dashboardEditBar.saveDashboard();

            dashboardsPage.duplicateDashboardTab(0);
            dashboardsPage.getTabs().openTab(0);
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME))
                .changeAttributeFilterValue("Negotiation");

            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(isEqualCollection(singleton("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements()));

            dashboardsPage.getTabs().openTab(1);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getCurrentValue(),
                    "Negotiation");
            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(isEqualCollection(singleton("Negotiation"),
                    dashboardsPage.getContent().getReport("Report2", TableReport.class).getAttributeElements()));

            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValue("Conviction");
            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getCurrentValue(),
                    "Conviction");
            Sleeper.sleepTightInSeconds(3);
            assertTrue(isEqualCollection(asList("2011", "Negotiation"),
                    dashboardsPage.getContent().getReport("Report1", TableReport.class).getAttributeElements()));
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getContent().getFilterWidget("filter-time")
                .changeTimeFilterValueByClickInTimeLine("2013");
            Sleeper.sleepTightInSeconds(2);
            assertTrue(dashboardsPage.getContent().getReport("Report1", TableReport.class).isNoData());
            assertTrue(dashboardsPage.getContent().getReport("Report2", TableReport.class).isNoData());

            dashboardsPage.getTabs().openTab(1);
            assertNotEquals(dashboardsPage.getContent().getFilterWidget("filter-time").getCurrentValue(), "2013");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
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
            dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME))
                .changeAttributeFilterValue("Negotiation");
            dashboardsPage.getTabs().openTab(0);
            assertEquals(dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getCurrentValue(),
                    "Negotiation");

            dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE))
                .changeAttributeFilterValue("Conviction");
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

        dashboardEditBar.addTimeFilterToDashboard(3, timeRange);
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

        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, STAGE_NAME);
        WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(STAGE_NAME)).getRoot();
        filter.click();
        DashboardWidgetDirection.UP.moveElementToRightPlace(filter);

        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, V_STAGE);
        filter = dashboardsPage.getContent().getFilterWidget(simplifyText(V_STAGE)).getRoot();
        filter.click();
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(filter);
    }
}
