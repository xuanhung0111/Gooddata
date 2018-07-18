package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.CreatedReportDialog;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_SALES_REPS_BY_WON_AND_LOST;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LockingDashboardTest extends AbstractEmbeddedModeTest {

    private DashboardRestRequest dashboardRequest;
    private ReportRestRequest reportRequest;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        reportRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        
        getReportCreator().createTopSalesRepsByWonAndLostReport();
    }

    @Test(dependsOnGroups = "createProject")
    public void affectLockedDashboardToReportsAndMetric() throws IOException {
        String dashboardTitle = "Dashboard Test";
        initDashboardsPage().addNewDashboard(dashboardTitle)
                .addReportToDashboard(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST).saveDashboard().lockDashboard(true);
        try {
            ReportsPage reportsPage = initReportsPage();
            assertTrue(reportsPage.isReportLocked(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST), "report should be locked");
            CreatedReportDialog createdReportDialog = reportsPage.openReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST).clickLockIcon();
            assertEquals(createdReportDialog.getEnabledEditPermission(), singletonList(PermissionType.ADMIN));
            assertEquals(createdReportDialog.getLockedAncestors(), singletonList(dashboardTitle));
            assertEquals(createdReportDialog.getRowInfoEditPermission(),
                    "because it belongs to these objects with edit restrictions.");
            MetricPage metricPage = initMetricPage();
            assertTrue(metricPage.isMetricLocked(METRIC_WON), "metric should be locked");
            assertTrue(metricPage.isMetricLocked(METRIC_LOST), "metric should be locked");
        } finally {
            dashboardRequest.deleteAllDashboards();
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
            assertTrue(metricPage.isMetricLocked(METRIC_WON), "metric should be locked");
            assertTrue(metricPage.isMetricLocked(METRIC_LOST), "metric should be locked");
            PermissionSettingDialog permissionSettingDialog = metricPage.openEditingPermission(METRIC_WON);
            assertEquals(permissionSettingDialog.getRowInfoEditPermission(),
                    "because it belongs to these objects with edit restrictions.");
            assertEquals(permissionSettingDialog.getLockedAncestors(), singletonList(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST));
        } finally {
            reportRequest.setLockedReport(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST, false);
            dashboardRequest.setLockedMetric(METRIC_WON, false);
            dashboardRequest.setLockedMetric(METRIC_LOST, false);
        }
    }
}
