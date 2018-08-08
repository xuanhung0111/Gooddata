package com.gooddata.qa.graphene.manage;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BulkPermissionMetricSetting extends AbstractEmbeddedModeTest {

    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());

        Metrics metrics = getMetricCreator();
        metrics.createNumberOfLostOppsMetric();
        metrics.createWonMetric();

        metrics.createLostMetric();
        metrics.createNumberOfActivitiesMetric();

        dashboardRequest.setPrivateMetric(METRIC_LOST, true);
        dashboardRequest.setLockedMetric(METRIC_NUMBER_OF_ACTIVITIES, true);

        dashboardRequest.setPrivateMetric(METRIC_WON, true);
        dashboardRequest.setLockedMetric(METRIC_WON, true);
    }

    @DataProvider(name = "privateMetric")
    public Object[][] getPrivateMetric() {
        return new Object[][] {
                {METRIC_LOST},
                {METRIC_WON}
        };
    }
    @Test(dependsOnGroups = "createProject", dataProvider = "privateMetric")
    public void mixedVisibilitySetting(String privateMetric) throws IOException {
        MetricPage metricPage = initMetricPage();
        PermissionSettingDialog permissionSettingDialog = metricPage
                .selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, privateMetric);
        assertEquals(permissionSettingDialog.getVisibilityDescription(), "Mixed selection (change)");
        assertEquals(permissionSettingDialog.getVisibilityLinkText(), "change");

        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(metricPage.waitForListMetricLoading().isPrivateMetric(privateMetric), "Metric shouldn't change");
            assertFalse(metricPage.isPrivateMetric(METRIC_NUMBER_OF_LOST_OPPS), "Metric shouldn't change");

            metricPage.selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, privateMetric)
                    .clickVisibilityLink().save();
            waitForFragmentNotVisible(permissionSettingDialog);
            metricPage.waitForListMetricLoading();
            Screenshots.takeScreenshot(browser, "mixed visibility Setting with " + privateMetric, getClass());
            assertFalse(metricPage.isPrivateMetric(privateMetric), "Metric should be public");
            assertFalse(metricPage.isPrivateMetric(METRIC_NUMBER_OF_LOST_OPPS), "Metric should be public");

            dashboardRequest.setPrivateMetric(privateMetric, true);
            initMetricPage().selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, privateMetric)
                    .clickVisibilityLink().setVisibility(false).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(metricPage.waitForListMetricLoading().isPrivateMetric(privateMetric), "Metric should be private");
            assertTrue(metricPage.isPrivateMetric(METRIC_NUMBER_OF_LOST_OPPS), "Metric should be private");
        } finally {
            dashboardRequest.setPrivateMetric(privateMetric, true);
            dashboardRequest.setPrivateMetric(METRIC_NUMBER_OF_LOST_OPPS, false);
        }
    }

    @DataProvider(name = "lockedMetric")
    public Object[][] getLockedMetric() {
        return new Object[][] {
                {METRIC_NUMBER_OF_ACTIVITIES},
                {METRIC_WON}
        };
    }
    @Test(dependsOnGroups = "createProject", dataProvider = "lockedMetric")
    public void mixedLockSetting(String lockedMetric) throws IOException {
        MetricPage metricPage = initMetricPage();
        PermissionSettingDialog permissionSettingDialog = metricPage
                .selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, lockedMetric);
        assertTrue(permissionSettingDialog.getEditDescriptions().contains("No change (mixed selection)"),
                "Mixed selection should display");
        assertEquals(permissionSettingDialog.getSelectedEditSection(), PermissionType.MIXED);
        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(metricPage.waitForListMetricLoading().isMetricLocked(lockedMetric), "Metric shouldn't change");
            assertFalse(metricPage.isMetricLocked(METRIC_NUMBER_OF_LOST_OPPS), "Metric shouldn't change");

            metricPage.selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, lockedMetric)
                    .setEditingPermission(PermissionType.ALL).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            metricPage.waitForListMetricLoading();
            Screenshots.takeScreenshot(browser, "mixed lock setting with " + lockedMetric, getClass());
            assertFalse(metricPage.isMetricLocked(lockedMetric), "Metric should be not locked");
            assertFalse(metricPage.isMetricLocked(METRIC_NUMBER_OF_LOST_OPPS), "Metric should be not locked");

            dashboardRequest.setLockedMetric(lockedMetric, true);
            initMetricPage().selectMetricsAndOpenPermissionDialog(METRIC_NUMBER_OF_LOST_OPPS, lockedMetric)
                    .setEditingPermission(PermissionType.ADMIN).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(metricPage.waitForListMetricLoading().isMetricLocked(lockedMetric), "Metric should be locked");
            assertTrue(metricPage.isMetricLocked(METRIC_NUMBER_OF_LOST_OPPS), "Metric should be locked");
        } finally {
            dashboardRequest.setLockedMetric(lockedMetric, true);
            dashboardRequest.setLockedMetric(METRIC_NUMBER_OF_LOST_OPPS, false);
        }
    }
}
