package com.gooddata.qa.graphene.dashboards;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {
    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void lockDashboard() {
        changePermissions(true, null);
    }

    @Test(dependsOnMethods = {"lockDashboard"}, groups = {"tests"})
    public void unpublishDashboard() {
        changePermissions(null, false);
    }

    @Test(dependsOnMethods = {"unpublishDashboard"}, groups = {"tests"})
    public void cancelPermissionsDialog() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        waitForElementVisible(dashboardsPage.getRoot());
        dashboardsPage.openPermissionsDialog();
        dashboardsPage.getPermissionsDialog().publish(true);
        dashboardsPage.getPermissionsDialog().unlock();
        dashboardsPage.getPermissionsDialog().cancel();
        Screenshots.takeScreenshot(browser, "is-locked-unlisted-2", this.getClass());
        waitForElementVisible(dashboardsPage.getRoot());
        Assert.assertTrue(dashboardsPage.isLocked());
        Assert.assertTrue(dashboardsPage.isUnlisted());
    }

    @Test(dependsOnMethods = {"cancelPermissionsDialog"}, groups = {"tests"})
    public void clickOnLockIcon() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        waitForElementVisible(dashboardsPage.getRoot());
        dashboardsPage.lockIconClick();
        waitForElementVisible(dashboardsPage.getPermissionsDialog().getRoot());
        Assert.assertTrue(dashboardsPage.getPermissionsDialog().getLockAdminRadio().getAttribute("checked") != null);
        Assert.assertFalse(dashboardsPage.getPermissionsDialog().getVisibilityCheckbox().getAttribute("checked") != null);
    }

    @Test(dependsOnMethods = {"clickOnLockIcon"}, groups = {"tests"})
    public void unlockDashboard() {
        changePermissions(false, null);
    }

    @Test(dependsOnMethods = {"unlockDashboard"}, groups = {"tests"})
    public void publishDashboard() {
        changePermissions(null, true);
    }

    private void changePermissions(Boolean locked, Boolean listed) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|projectDashboardPage");
        waitForElementVisible(dashboardsPage.getRoot());
        if (locked != null) {
            dashboardsPage.lockDashboard(locked);
        }
        if (listed != null) {
            dashboardsPage.publishDashboard(listed);
        }
        waitForElementVisible(dashboardsPage.getRoot());

        if (listed != null) {
            Assert.assertEquals((boolean) listed, !dashboardsPage.isUnlisted());
        }
        if (locked != null) {
            Assert.assertEquals((boolean) locked, dashboardsPage.isLocked());
        }

    }
}
