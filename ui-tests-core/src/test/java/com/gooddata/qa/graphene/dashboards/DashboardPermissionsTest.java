package com.gooddata.qa.graphene.dashboards;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog.ALERT_INFOBOX_CSS_SELECTOR;
import static com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog.GRANTEES_PANEL;
import static java.util.Arrays.asList;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {

    private static final String LOCKED_DASHBOARD_NAME = "Locked and Visible";
    private static final String UNLOCKED_DASHBOARD_NAME = "Unlocked and Visible";
    private static final String ACL_DASHBOARD_NAME = "dashboardVisibleToViewerAndEditor";

    private String originalDashboardName;

    private String viewerLogin;
    private String editorLogin;

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
        viewerLogin = testParams.getViewerUser();
        editorLogin = testParams.getEditorUser();
    }

    /**
     * lock dashboard - only admins can edit
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
    public void lockDashboard() {
        switchLockingDashboard(false);
    }

    /**
     * publish - make dashboard visible to every1 ( don't touch locking )
     */
    @Test(dependsOnMethods = {"lockDashboard"}, groups = {"admin-tests"})
    public void publishDashboard() {
        switchVisibilityDashboard(false);
    }

    /**
     * unpublish - make dashboard visible to owner only ( don't touch locking )
     */
    @Test(dependsOnMethods = {"publishDashboard"}, groups = {"admin-tests"})
    public void unPublishDashboard() {
        switchVisibilityDashboard(true);
    }

    /**
     * change visibility to specific user can access, unlock and hit cancel button to forget changes
     */
    @Test(dependsOnMethods = {"unPublishDashboard"}, groups = {"admin-tests"})
    public void cancelPermissionsDialog() {
        initDashboardsPage();
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        permissionsDialog.unlock();
        permissionsDialog.cancel();
        Screenshots.takeScreenshot(browser, "is-locked-unlisted-2", this.getClass());
        waitForElementVisible(dashboardsPage.getRoot());
        Assert.assertTrue(dashboardsPage.isLocked());
        Assert.assertTrue(dashboardsPage.isUnlisted());
    }

    /**
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible for all
     */
    @Test(dependsOnMethods = {"cancelPermissionsDialog"}, groups = {"admin-tests"})
    public void clickOnLockIcon() {
        initDashboardsPage();
        final PermissionsDialog permissionsDialog = dashboardsPage.lockIconClick();
        Assert.assertTrue(permissionsDialog.getLockAdminRadio().getAttribute("checked") != null);
        waitForElementPresent(permissionsDialog.getRoot().findElement(GRANTEES_PANEL));
    }

    /**
     * select and unlock dashboard "Pipeline Analysis" ( don't touch the visibility settings )
     */
    @Test(dependsOnMethods = {"clickOnLockIcon"}, groups = {"admin-tests"})
    public void unlockDashboard() throws JSONException, InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard("Pipeline Analysis");
        switchLockingDashboard(true);
    }

    /**
     * add dashboard '{@value #LOCKED_DASHBOARD_NAME}' - lock it and show to all +
     * add dashboard '{@value #UNLOCKED_DASHBOARD_NAME}' - show to all
     */
    @Test(dependsOnMethods = {"unlockDashboard"}, groups = {"admin-tests"})
    public void initializeEditorAndViewerTests() throws InterruptedException {
        initDashboardsPage();
        originalDashboardName = dashboardsPage.getDashboardName();
        dashboardsPage.addNewDashboard(LOCKED_DASHBOARD_NAME);
        switchLockingDashboard(false);
        switchVisibilityDashboard(false);
        dashboardsPage.addNewDashboard(UNLOCKED_DASHBOARD_NAME);
        switchVisibilityDashboard(false);
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnGroups = {"admin-tests"}, groups = {"non-admin-tests"})
    public void viewerCheckDashboardsList() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.VIEWER);
        checkDashboardsNames(asList(LOCKED_DASHBOARD_NAME, UNLOCKED_DASHBOARD_NAME));
    }

    /**
     *  open dashboards and check icons
     */
    @Test(dependsOnMethods = {"viewerCheckDashboardsList"}, groups = {"non-admin-tests"})
    public void viewerCheckLockIconIsNotVisible() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(LOCKED_DASHBOARD_NAME);
        Assert.assertFalse(dashboardsPage.isLocked());
        dashboardsPage.selectDashboard(UNLOCKED_DASHBOARD_NAME);
        Assert.assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"viewerCheckLockIconIsNotVisible"}, groups = {"non-admin-tests"})
    public void editorCheckDashboardsList() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.EDITOR);
        checkDashboardsNames(asList(LOCKED_DASHBOARD_NAME, UNLOCKED_DASHBOARD_NAME));
    }

    @Test(dependsOnMethods = {"editorCheckDashboardsList"}, groups = {"non-admin-tests"})
    public void editorCheckLockIconIsVisible() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(LOCKED_DASHBOARD_NAME);
        Assert.assertTrue(dashboardsPage.isLocked());
        dashboardsPage.selectDashboard(UNLOCKED_DASHBOARD_NAME);
        Assert.assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"editorCheckLockIconIsVisible"}, groups = {"non-admin-tests"})
    public void editorTryEditingLockedDashboard() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(LOCKED_DASHBOARD_NAME);
        waitForDashboardPageLoaded(browser);
        Assert.assertFalse(dashboardsPage.isEditButtonPresent());
    }

    @Test(dependsOnMethods = {"editorTryEditingLockedDashboard"})
    public void adminCheckVisibleDashboards() throws JSONException, InterruptedException {
        logout();
        signIn(false, UserRoles.ADMIN);
        checkDashboardsNames(asList(originalDashboardName, LOCKED_DASHBOARD_NAME, UNLOCKED_DASHBOARD_NAME));
    }

    @Test(dependsOnMethods = {"adminCheckVisibleDashboards"}, groups = {"acl-tests"})
    public void searchForUsersToShareDashboardWithThem() throws JSONException, InterruptedException {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(ACL_DASHBOARD_NAME);
        initDashboardsPage();

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        Assert.assertEquals(elements.size(), 1);

        Assert.assertEquals(addGranteesDialog.getNumberOfGrantees(), 2);
        Assert.assertEquals(addGranteesDialog.getGranteesCount(viewerLogin, true), 1);
        addGranteesDialog.selectItem(viewerLogin);
        Assert.assertEquals(addGranteesDialog.getGranteesCount(editorLogin, true), 1);
        addGranteesDialog.selectItem(editorLogin);
        addGranteesDialog.share();

        elements = permissionsDialog.getAddedGrantees();
        Assert.assertEquals(elements.size(), 3);
    }

    @Test(dependsOnMethods = {"searchForUsersToShareDashboardWithThem"}, groups = {"acl-tests"})
    public void searchNonExistingUser() throws JSONException, InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(ACL_DASHBOARD_NAME);
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        Assert.assertEquals(addGranteesDialog.getGranteesCount("dsdhjak", false), 0);
        addGranteesDialog.cancel();

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        Assert.assertEquals(elements.size(), 3);
    }

    /**
     * CL-6018 test case - editor can switch visibility of project but cant see locking
     */
    @Test(dependsOnMethods = {"searchNonExistingUser"}, groups = {"acl-tests"})
    public void editorCanChangeVisibilityOfLockedDashboard() throws JSONException, InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(ACL_DASHBOARD_NAME);
        switchLockingDashboard(false);
        logout();

        signIn(false, UserRoles.EDITOR);
        initDashboardsPage();
        dashboardsPage.selectDashboard(ACL_DASHBOARD_NAME);

        final PermissionsDialog permissionsDialog = unpublishUser(viewerLogin, true);

        dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        List<WebElement> addedGrantees = permissionsDialog.getAddedGrantees();
        Assert.assertEquals(addedGrantees.size(), 2);
        Assert.assertEquals(addGranteesDialog.getNumberOfGrantees(), 1);
        Assert.assertEquals(addGranteesDialog.getGranteesCount(viewerLogin, true), 1);
        addGranteesDialog.selectItem(viewerLogin);
        addGranteesDialog.share();

        addedGrantees = permissionsDialog.getAddedGrantees();
        Assert.assertEquals(addedGrantees.size(), 3);

        waitForElementNotPresent(permissionsDialog.getLockAdminRadio());
    }

    /**
     * CL-6045 test case - user (nor owner or grantee) can see warn message before kick himself from grantees
     */
    @Test(dependsOnMethods = {"editorCanChangeVisibilityOfLockedDashboard"}, groups = {"acl-tests"})
    public void editorCanSeeHidingFromHimselfWarnNotification() throws InterruptedException, JSONException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(ACL_DASHBOARD_NAME);
        final PermissionsDialog permissionsDialog = unpublishUser(editorLogin, false);
        waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));
        permissionsDialog.cancel();

        dashboardsPage.selectDashboard(LOCKED_DASHBOARD_NAME);
        dashboardsPage.openPermissionsDialog();
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));
        permissionsDialog.cancel();
    }

    private PermissionsDialog unpublishUser(final String login, final boolean submitDialog) {
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.removeUser(login);
        if (submitDialog) {
            permissionsDialog.submit();
        }
        return permissionsDialog;
    }

    /**
     * @param isAlreadyLocked is currently locked
     */
    private void switchLockingDashboard(boolean isAlreadyLocked) {
        initDashboardsPage();
        dashboardsPage.lockDashboard(isAlreadyLocked);
        waitForElementVisible(dashboardsPage.getRoot());
        Screenshots.takeScreenshot(browser, "dashboard was " + getLockString(isAlreadyLocked) + " and now is " + getLockString(!isAlreadyLocked), this.getClass());
        Assert.assertEquals(!dashboardsPage.isLocked(), isAlreadyLocked);
    }

    private String getLockString(boolean isAlreadyLocked) {
        return (isAlreadyLocked ? "locked" : "unlocked");
    }

    /**
     * @param isAlreadyPublishedToAll  - is currently unlisted
     */
    private void switchVisibilityDashboard(boolean isAlreadyPublishedToAll) {
        initDashboardsPage();
        dashboardsPage.publishDashboard(isAlreadyPublishedToAll);
        waitForElementVisible(dashboardsPage.getRoot());
        Screenshots.takeScreenshot(browser, "dashboard was " + getListString(isAlreadyPublishedToAll) + " and now is " + getListString(!isAlreadyPublishedToAll), this.getClass());
        Assert.assertEquals(dashboardsPage.isUnlisted(), isAlreadyPublishedToAll);
    }

    private String getListString(boolean isAlreadyPublishedToAll) {
        return (isAlreadyPublishedToAll ? "listed" : "unlisted");
    }

    private void checkDashboardsNames(List<String> expectedDashboardsNames) throws InterruptedException {
        initDashboardsPage();
        Assert.assertEquals(dashboardsPage.getDashboardsNames(), expectedDashboardsNames);
    }
}
