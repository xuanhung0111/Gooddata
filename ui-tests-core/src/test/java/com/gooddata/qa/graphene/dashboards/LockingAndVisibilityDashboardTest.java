package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.ProjectDashboard;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.CreatedReportDialog;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;

import static com.gooddata.qa.graphene.AbstractTest.Profile.EDITOR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_SALES_REPS_BY_WON_AND_LOST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LockingAndVisibilityDashboardTest extends AbstractEmbeddedModeTest {

    private static final String PRIVATE_DASHBOARD = "Private Dashboard";
    private static final String PUBLIC_DASHBOARD = "Public Dashboard";
    private static final String API_DASHBOARD = "API Dashboard";
    private static final String EDITOR_PRIVATE_DASHBOARD = "Editor Private Dashboard";
    private DashboardRestRequest dashboardRequest;
    private ReportRestRequest reportRequest;
    private RestClient restEditorClient;
    private String editorDashboardUri;
    private String privateDashboardUri;
    private String publicDashboardUri;
    private String apiDashboardUri;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        reportRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        
        getReportCreator().createTopSalesRepsByWonAndLostReport();
        getReportCreator().createAmountByStageNameReport();

        privateDashboardUri = dashboardRequest.createDashboard(initDashboard(PRIVATE_DASHBOARD).getMdObject());
        publicDashboardUri = dashboardRequest.createDashboard(initDashboard(PUBLIC_DASHBOARD).getMdObject());
        apiDashboardUri = dashboardRequest.createDashboard(initDashboard(API_DASHBOARD).getMdObject());
        dashboardRequest.setPrivateDashboard(PRIVATE_DASHBOARD, true);
        dashboardRequest.setPrivateDashboard(PUBLIC_DASHBOARD, false);

        //Create dashboard by Editor
        restEditorClient = new RestClient(getProfile(EDITOR));
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(restEditorClient, testParams.getProjectId());
        editorDashboardUri = dashboardRestRequest.createDashboard(initDashboard(EDITOR_PRIVATE_DASHBOARD).getMdObject());
        dashboardRestRequest.setPrivateDashboard(EDITOR_PRIVATE_DASHBOARD, true);
    }

    @Test(dependsOnGroups = "createProject")
    public void affectLockedDashboardToReportsAndMetric() throws IOException {
        String dashboardTitle = "Dashboard Test";
        initDashboardsPage().addNewDashboard(dashboardTitle)
                .addReportToDashboard(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST).saveDashboard().lockDashboard(true);
        try {
            ReportsPage reportsPage = initReportsPage();
            assertTrue(reportsPage.isReportLocked(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST), "Report should be locked");
            CreatedReportDialog createdReportDialog =
                    reportsPage.openReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST).clickLockIcon();
            assertEquals(createdReportDialog.getEnabledEditPermission(), singletonList(PermissionType.ADMIN));
            assertEquals(createdReportDialog.getLockedAncestors(), singletonList(dashboardTitle));
            assertEquals(createdReportDialog.getRowInfoEditPermission(),
                    "because it belongs to these objects with edit restrictions.");
            MetricPage metricPage = initMetricPage();
            assertTrue(metricPage.isMetricLocked(METRIC_WON), "Metric should be locked");
            assertTrue(metricPage.isMetricLocked(METRIC_LOST), "Metric should be locked");
        } finally {
            initDashboardsPage().selectDashboard(dashboardTitle).deleteDashboard();
            reportRequest.setLockedReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST, false);
            dashboardRequest.setLockedMetric(METRIC_WON, false);
            dashboardRequest.setLockedMetric(METRIC_LOST, false);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void affectLockedReportToMetric() throws IOException {
        initReportsPage().selectReportsAndOpenPermissionDialog(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST)
                .setEditingPermission(PermissionType.ADMIN).save();
        try {
            MetricPage metricPage = initMetricPage();
            assertTrue(metricPage.isMetricLocked(METRIC_WON), "Metric should be locked");
            assertTrue(metricPage.isMetricLocked(METRIC_LOST), "Metric should be locked");
            PermissionSettingDialog permissionSettingDialog = metricPage.openPermissionSettingDialogFor(METRIC_WON);
            assertEquals(permissionSettingDialog.getRowInfoEditPermission(),
                    "because it belongs to these objects with edit restrictions.");
            assertEquals(permissionSettingDialog.getLockedAncestors(),
                    singletonList(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
        } finally {
            reportRequest.setLockedReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST, false);
            dashboardRequest.setLockedMetric(METRIC_WON, false);
            dashboardRequest.setLockedMetric(METRIC_LOST, false);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void editPersonalObjectsInDashboard() throws IOException {
        final String dashboardTest = "Dashboard Test";
        reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, true);
        try {
            DashboardEditBar dashboardEditBar = initDashboardsPage()
                    .addNewDashboard(dashboardTest).editDashboard();
            DropDown addReportDropDown = dashboardEditBar.expandReportMenu();
            assertEquals(addReportDropDown.listTitlesOfItems(),
                    asList(REPORT_AMOUNT_BY_STAGE_NAME, REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
            assertTrue(addReportDropDown.isPrivateItem(REPORT_AMOUNT_BY_STAGE_NAME),
                    "Eye icon should display beside private report");
            addReportDropDown.searchAndSelectItem(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST);
            SimpleMenu copyToSubMenu = dashboardsPage.getTabs().expandCopyToDropDown(0);
            assertEquals(copyToSubMenu.listTitlesOfItems(), asList(PRIVATE_DASHBOARD, API_DASHBOARD, PUBLIC_DASHBOARD));
            assertTrue(copyToSubMenu.isPrivateItem(PRIVATE_DASHBOARD),
                    "Eye icon should display beside private dashboard");
            logoutAndLoginAs(true, UserRoles.EDITOR);
            dashboardEditBar = initDashboardsPage().addNewDashboard(dashboardTest).editDashboard();
            addReportDropDown = dashboardEditBar.expandReportMenu();
            assertEquals(addReportDropDown.listTitlesOfItems(), singletonList(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
            addReportDropDown.searchAndSelectItem(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST);
            assertEquals(dashboardsPage.getTabs().expandCopyToDropDown(0).listTitlesOfItems(),
                    asList(EDITOR_PRIVATE_DASHBOARD, API_DASHBOARD, PUBLIC_DASHBOARD));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void editPersonalObjectsInEmbeddedDashboard() throws IOException {
        final String dashboardTest = "Embedded Dashboard Test";
        reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, true);
        try {
            embeddedUri = initDashboardsPage().addNewDashboard(dashboardTest).openEmbedDashboardDialog().getPreviewURI();
            DashboardEditBar dashboardEditBar = initEmbeddedDashboard().editDashboard();
            DropDown dropDownAddReport = dashboardEditBar.expandReportMenu();
            assertEquals(dropDownAddReport.listTitlesOfItems(),
                    asList(REPORT_AMOUNT_BY_STAGE_NAME, REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
            assertTrue(dropDownAddReport.isPrivateItem(REPORT_AMOUNT_BY_STAGE_NAME),
                    "Eye icon should display beside private report");
            logoutAndLoginAs(true, UserRoles.EDITOR);
            embeddedUri = initDashboardsPage().addNewDashboard(dashboardTest).openEmbedDashboardDialog().getPreviewURI();
            dashboardEditBar = initEmbeddedDashboard().editDashboard();
            dropDownAddReport = dashboardEditBar.expandReportMenu();
            assertEquals(dropDownAddReport.listTitlesOfItems(), singletonList(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void hiddenDashboardByDefaultOnEditMode() {
        initDashboardsPage().openEditExportEmbedMenu().select("Add Dashboard");
        assertEquals(dashboardsPage.getTooltipFromEyeIcon(),
                "You can change permissions only after creating the dashboard first.");
        dashboardsPage.saveDashboard();
        assertFalse(dashboardsPage.isBlueBubbleTooltipDisplayed(), "After closing bubble will dismiss forever");
        assertFalse(dashboardsPage.selectDashboard(API_DASHBOARD).isBlueBubbleTooltipDisplayed(),
                "Bubble won't display with dashboard is created by API");
    }

    @Test(dependsOnGroups = "createProject")
    public void listVisibilityDashboardsOnAPI() {
        Collection<String> listDashboardUrisOfEditor = restEditorClient.getMetadataService()
                .findUris(getProject(), ProjectDashboard.class);
        assertThat(listDashboardUrisOfEditor, hasItems(publicDashboardUri, apiDashboardUri, editorDashboardUri));
        assertFalse(listDashboardUrisOfEditor.contains(privateDashboardUri),
                "Private dashboard of admin user shouldn't be shown");
        Collection<String> listDashboardUrisOfAdmin = getMdService().findUris(getProject(), ProjectDashboard.class);
        assertThat(listDashboardUrisOfAdmin, hasItems(privateDashboardUri, publicDashboardUri, apiDashboardUri));
    }

    private Dashboard initDashboard(String dashboardName) {
        Tab firstTab = Builder.of(Tab::new).with(tab -> tab.setTitle("First Tab")).build();
        return  Builder.of(Dashboard::new).with((Dashboard dash) -> {
            dash.setName(dashboardName);
            dash.addTab(firstTab);
        }).build();
    }
}
