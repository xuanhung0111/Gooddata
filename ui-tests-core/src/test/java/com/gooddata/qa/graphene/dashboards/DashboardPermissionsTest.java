package com.gooddata.qa.graphene.dashboards;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {

    private String lockedDashboardName;
    private String unlockedDashboardName;
    private String originalDashboardName;

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() {
        lockedDashboardName = "Locked and Visible";
        unlockedDashboardName = "Unlocked and Visible";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"admin-tests"})
    public void lockDashboard() {
        changePermissions(true, null);
    }

    @Test(dependsOnMethods = {"lockDashboard"}, groups = {"admin-tests"})
    public void unpublishDashboard() {
        changePermissions(null, false);
    }

    @Test(dependsOnMethods = {"unpublishDashboard"}, groups = {"admin-tests"})
    public void cancelPermissionsDialog() {
        initDashboardsPage();
        dashboardsPage.openPermissionsDialog();
        dashboardsPage.getPermissionsDialog().publish(true);
        dashboardsPage.getPermissionsDialog().unlock();
        dashboardsPage.getPermissionsDialog().cancel();
        Screenshots.takeScreenshot(browser, "is-locked-unlisted-2", this.getClass());
        waitForElementVisible(dashboardsPage.getRoot());
        Assert.assertTrue(dashboardsPage.isLocked());
        Assert.assertTrue(dashboardsPage.isUnlisted());
    }

    @Test(dependsOnMethods = {"cancelPermissionsDialog"}, groups = {"admin-tests"})
    public void clickOnLockIcon() {
        initDashboardsPage();
        dashboardsPage.lockIconClick();
        waitForElementVisible(dashboardsPage.getPermissionsDialog().getRoot());
        Assert.assertTrue(dashboardsPage.getPermissionsDialog().getLockAdminRadio().getAttribute("checked") != null);
        Assert.assertFalse(dashboardsPage.getPermissionsDialog().getVisibilityCheckbox().getAttribute("checked") != null);
    }

    @Test(dependsOnMethods = {"clickOnLockIcon"}, groups = {"admin-tests"})
    public void unlockDashboard() throws JSONException, InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard("Pipeline Analysis");
        changePermissions(false, null);
    }

    @Test(dependsOnMethods = {"unlockDashboard"}, groups = {"admin-tests"})
    public void initializeEditorAndViewerTests() throws InterruptedException {
        initDashboardsPage();
        originalDashboardName = dashboardsPage.getDashboardName();
        dashboardsPage.addNewDashboard(lockedDashboardName);
        changePermissions(null, true);
        changePermissions(true, null);
        dashboardsPage.addNewDashboard(unlockedDashboardName);
        changePermissions(null, true);
    }

    @Test(dependsOnGroups = {"admin-tests"}, groups = {"non-admin-tests"})
    public void viewerCheckDashboardsList() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.VIEWER);
        checkDashboardsNames(Arrays.asList(lockedDashboardName, unlockedDashboardName));
    }

    @Test(dependsOnMethods = {"initializeEditorAndViewerTests"}, groups = {"non-admin-tests"})
    public void viewerCheckLockIconIsNotVisible() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(lockedDashboardName);
        Assert.assertFalse(dashboardsPage.isLocked());
        dashboardsPage.selectDashboard(unlockedDashboardName);
        Assert.assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"viewerCheckLockIconIsNotVisible"}, groups = {"non-admin-tests"})
    public void editorCheckDashboardsList() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.EDITOR);
        checkDashboardsNames(Arrays.asList(lockedDashboardName, unlockedDashboardName));
    }

    @Test(dependsOnMethods = {"viewerCheckLockIconIsNotVisible"}, groups = {"non-admin-tests"})
    public void editorCheckLockIconIsVisible() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(lockedDashboardName);
        Assert.assertTrue(dashboardsPage.isLocked());
        dashboardsPage.selectDashboard(unlockedDashboardName);
        Assert.assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"editorCheckLockIconIsVisible"}, groups = {"non-admin-tests"})
    public void editorTryEditingLockedDashboard() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(lockedDashboardName);
        waitForDashboardPageLoaded(browser);
        Assert.assertFalse(dashboardsPage.isEditButtonPresent());
    }

    @Test(dependsOnMethods = {"editorTryEditingLockedDashboard"})
    public void adminCheckVisibleDashboards() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.ADMIN);
        checkDashboardsNames(Arrays.asList(originalDashboardName, lockedDashboardName, unlockedDashboardName));
    }

    private void changePermissions(Boolean locked, Boolean listed) {
        initDashboardsPage();
        if (locked != null) {
            dashboardsPage.lockDashboard(locked);
        }
        if (listed != null) {
            dashboardsPage.publishDashboard(listed);
        }
        waitForElementVisible(dashboardsPage.getRoot());
        Screenshots.takeScreenshot(browser, "dashboard-" + (locked != null ? "locked" : "unlocked") + "-" +
                (listed != null ? "listed" : "unlisted"), this.getClass());
        if (listed != null) {
            Assert.assertEquals(!dashboardsPage.isUnlisted(), (boolean) listed);
        }
        if (locked != null) {
            Assert.assertEquals(dashboardsPage.isLocked(), (boolean) locked);
        }
    }

    private void checkDashboardsNames(List<String> expectedDashboardsNames) throws InterruptedException {
        initDashboardsPage();
        Assert.assertEquals(dashboardsPage.getDashboardsNames(), expectedDashboardsNames);
    }
}
