package com.gooddata.qa.graphene.lcm.dashboards;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.UrlParserUtils;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.ATT_LCM_DATA_PRODUCT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class EmbeddedDashboardUsingClientIdTest extends AbstractProjectTest {

    protected boolean useK8sExecutor = false;

    private static final String EMBEDDED_DASHBOARD_NAME = "Embedded Dashboard";
    private static final String FIRST_TAB = "First Tab";
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "Client project " + generateHashString();

    private static String DRILL_REPORT;

    private String devProjectId;
    private String clientProjectId;
    private String identifier;
    private String dashboardId;

    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void initProperties() {
        appliedFixture = GOODSALES;
        validateAfterClass = false;
        projectTitle = "KPI Dashboards with LCM";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();

        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String reportUri = new Reports(
            new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId()).createAmountByProductReport();

        Tab sourceTab = Builder.of(Tab::new).with(tab -> {
            tab.setTitle(FIRST_TAB);
            tab.addItem(Builder.of(ReportItem::new)
                .with(report -> report.setObjUri(reportUri))
                .build());
        }).build();

        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(EMBEDDED_DASHBOARD_NAME);
            dash.addTab(sourceTab);
        }).build();

        dashboardRequest.createDashboard(dashboard.getMdObject());

        devProjectId = testParams.getProjectId();
        clientProjectId = createProjectUsingFixture(CLIENT_PROJECT_TITLE, ResourceTemplate.GOODSALES,
            testParams.getDomainUser());

        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        lcmBrickFlowBuilder.setSegmentId(SEGMENT_ID).setClientId(CLIENT_ID)
            .setDevelopProject(devProjectId).setClientProjects(clientProjectId).buildLcmProjectParameters();

        System.out.println("*******************************************************");
        System.out.println(format("* dev project id: %s    *", devProjectId));
        System.out.println(format("* client project id: %s *", clientProjectId));
        System.out.println("*******************************************************");
        lcmBrickFlowBuilder.runLcmFlow();

        testParams.setProjectId(clientProjectId);
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);

        dashboardId = UrlParserUtils.getObjId(dashboardRequest.getDashboardUri(EMBEDDED_DASHBOARD_NAME));
        identifier = dashboardRequest.getDashboardId(EMBEDDED_DASHBOARD_NAME);
    }

    @DataProvider
    public Object[][] getEmbeddedFormat() {
        return new Object[][]{
            {format("/dashboard.html#project=/gdc/projects/client:%s:%s&dashboard=/gdc/md/client:%s:%s/obj/" +
                "identifier:%s", ATT_LCM_DATA_PRODUCT, CLIENT_ID, ATT_LCM_DATA_PRODUCT, CLIENT_ID, identifier)},
            {format("/dashboard.html#project=/gdc/projects/client:%s:%s&dashboard=/gdc/md/client:%s:%s/obj/%s",
                ATT_LCM_DATA_PRODUCT, CLIENT_ID, ATT_LCM_DATA_PRODUCT, CLIENT_ID, dashboardId)},
            {format("/dashboard.html#project=/gdc/projects/%s&dashboard=/gdc/md/%s/obj/identifier:%s",
                clientProjectId, clientProjectId, identifier)},
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "getEmbeddedFormat")
    public void doSomeActionsWithEmbeddedDashboard(String formatUrl) {
        openUrl(formatUrl);
        EmbeddedDashboard embeddedDashboard = EmbeddedDashboard.getInstance(browser);

        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
        takeScreenshot(browser, "edit-Saved-View-With-Embedded-Dashboard", this.getClass());

        assertEquals(tableReport.getReportTitle(), REPORT_AMOUNT_BY_PRODUCT);
        assertEquals(tableReport.getRawMetricValues(), Arrays.asList("$27,222,899.64", "$22,946,895.47", "$38,596,194.86",
            "$8,042,031.92", "$9,525,857.91", "$10,291,576.74"));

        embeddedDashboard.editDashboard();

        DRILL_REPORT = "Drill Report" + generateHashString();
        log.info("New Drill Report: " + DRILL_REPORT);

        embeddedDashboard = EmbeddedDashboard.getInstance(browser);
        EmbeddedReportPage reportPage = embeddedDashboard.openEmbeddedReportPage();
        reportPage.setReportName(DRILL_REPORT).openWhatPanel()
            .openMetricDetail(METRIC_AMOUNT)
            .addDrillStep(ATTR_STAGE_NAME)
            .done();
        reportPage.finishCreateReport();

        tableReport = embeddedDashboard.getContent().getReport(DRILL_REPORT, TableReport.class);

        assertEquals(tableReport.getReportTitle(), DRILL_REPORT);
        assertEquals(tableReport.getRawMetricValues(), Arrays.asList("$116,625,456.54"));

        filterDashboardWithEmbeddedDashboard();

        drillReportWithEmbeddedDashboard();

        sortReportWithEmbeddedDashboard();
    }

    private void filterDashboardWithEmbeddedDashboard() {
        EmbeddedDashboard embeddedDashboard = EmbeddedDashboard.getInstance(browser);
        try {
            embeddedDashboard.addAttributeFilterToDashboard(
                AddDashboardFilterPanel.DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT, DashboardWidgetDirection.RIGHT);
            embeddedDashboard.getFilterWidgetByName(ATTR_DEPARTMENT).changeAttributeFilterValues("Inside Sales");

            TableReport tableReport = embeddedDashboard.getContent().getReport(DRILL_REPORT, TableReport.class);
            takeScreenshot(browser, "filterDashboardWithEmbeddedDashboard", this.getClass());

            assertEquals(tableReport.getRawMetricValues(), Arrays.asList("$36,219,131.58"));
        } finally {
            DashboardEditWidgetToolbarPanel.removeWidget(
                embeddedDashboard.getFilterWidgetByName(ATTR_DEPARTMENT).getRoot(), browser);
        }
        embeddedDashboard.saveDashboard();
    }

    private void drillReportWithEmbeddedDashboard() {
        EmbeddedDashboard embeddedDashboard = EmbeddedDashboard.getInstance(browser);
        TableReport tableReport = embeddedDashboard.getContent().getReport(DRILL_REPORT, TableReport.class);

        DashboardDrillDialog drillDialog = tableReport.openDrillDialogFrom(
            "$116,625,456.54", TableReport.CellType.METRIC_VALUE);

        tableReport = drillDialog.getReport(TableReport.class);
        takeScreenshot(browser, "drillReportWithEmbeddedDashboard", this.getClass());

        assertEquals(tableReport.getAttributeValues(), Arrays.asList("Interest", "Discovery", "Short List",
            "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost"));
        drillDialog.closeDialog();
        tableReport.waitForLoaded();
    }

    private void sortReportWithEmbeddedDashboard() {
        EmbeddedDashboard embeddedDashboard = EmbeddedDashboard.getInstance(browser);
        TableReport tableReport = embeddedDashboard.getContent().getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);

        tableReport.sortBy(METRIC_AMOUNT, TableReport.CellType.METRIC_HEADER, TableReport.Sort.DESC).waitForLoaded();
        takeScreenshot(browser, "sortReportWithEmbeddedDashboard", this.getClass());

        assertEquals(tableReport.getRawMetricValues(), asList("$38,596,194.86", "$27,222,899.64", "$22,946,895.47",
            "$10,291,576.74", "$9,525,857.91", "$8,042,031.92"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }

    private Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }
}
