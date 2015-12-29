package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog.ALERT_INFOBOX_CSS_SELECTOR;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.PublishType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {

    private static final String XENOFOBES_XYLOPHONES = "Xenofobes & xylophones";
    private static final String ALCOHOLICS_ANONYMOUS = "Alcoholics anonymous";
    private static final String UNCHANGED_DASHBOARD = "Unchanged dashboard";
    private String viewerLogin;
    private String editorLogin;
    private String userGroup1Id;
    private String userGroup2Id;
    private String editorProfileUri;
    private String viewerProfileUri;

    @BeforeClass(alwaysRun = true)
    public void before() {
        addUsersWithOtherRoles = true;
        viewerLogin = testParams.getViewerUser();
        editorLogin = testParams.getEditorUser();
        editorProfileUri = testParams.loadProperty("editorProfileUri");
        viewerProfileUri = testParams.loadProperty("viewerProfileUri");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"}, priority = 0)
    public void checkBackToTheOnlyOneVisibileDashboard() throws JSONException {
        try {
            selectDashboard("Pipeline Analysis");
            publishDashboard(false);
            String dashboardUrl = browser.getCurrentUrl();

            logout();
            signIn(false, UserRoles.EDITOR);
            createDashboard("Only one dashboard of Editor");
            sleepTightInSeconds(1);

            // Editor loads the dashboard url of Admin
            System.out.println("Loading page ... " + dashboardUrl);
            browser.get(dashboardUrl);
            waitForDashboardPageLoaded(browser);
            waitForElementVisible(dashboardsPage.getRoot());
            sleepTightInSeconds(2);
            String dashboardName = dashboardsPage.getDashboardName();
            // the dashboard name in this case will contains a redundant character in the end
            // check the code of getDashboardName for more details.
            assertEquals(dashboardName.substring(0, dashboardName.length() - 1), "Pipeline Analysis");

            selectDashboard("Only one dashboard of Editor");
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    /**
     * lock dashboard - only admins can edit
     * 
     * @throws Exception
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldLockDashboard() throws Exception {
        createDashboard("Locked dashboard");
        lockDashboard(true);
        assertEquals(dashboardsPage.isLocked(), true);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldUnlockDashboard() throws Exception {
        createDashboard("Unlocked dashboard");
        lockDashboard(false);
        assertEquals(dashboardsPage.isLocked(), false);
    }

    /**
     * publish - make dashboard visible to every1 ( don't touch locking )
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldPublishDashboard() {
        createDashboard("Published dashboard");
        publishDashboard(true);
        assertFalse(dashboardsPage.isUnlisted());
    }

    /**
     * unpublish - make dashboard visible to owner only ( don't touch locking )
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldUnpublishDashboard() {
        createDashboard("Unpublished dashboard");
        publishDashboard(false);
        assertTrue(dashboardsPage.isUnlisted());
    }

    /**
     * when a dashboard is created, its default settings are "visibility:specific user" + editing
     * unlocked change visibility to everyone can access, editing locked and hit cancel button to
     * forget changes
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldNotChangePermissionsWhenCancelled() {
        createDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
        permissionsDialog.lock();
        permissionsDialog.cancel();

        waitForElementVisible(dashboardsPage.getRoot());
        assertFalse(dashboardsPage.isLocked());
        assertTrue(dashboardsPage.isUnlisted());
    }

    /**
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible
     * for all
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldOpenPermissionsDialogWhenClickingOnLockIcon() {
        initDashboardsPage();

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.lock();
        permissionsDialog.submit();

        waitForElementVisible(dashboardsPage.lockIconClick().getRoot());
    }

    /**
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible
     * for all
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldOpenPermissionsDialogWhenClickingOnEyeIcon() {
        initDashboardsPage();

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        permissionsDialog.submit();

        waitForElementVisible(dashboardsPage.unlistedIconClick().getRoot());
    }

    /**
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible
     * for all
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void checkPermissionDialogInDashboardEditMode() {
        createDashboard("Check Permission in Edit Mode");

        PermissionsDialog permissionsDialog = dashboardsPage.unlistedIconClick();
        permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
        permissionsDialog.submit();
        assertFalse(dashboardsPage.isUnlisted());
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnGroups = {"admin-tests"}, groups = {"viewer-tests", "sanity"})
    public void prepareEditorAndViewerTests() throws JSONException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);
        sleepTightInSeconds(1);
        createDashboard("Unlocked and published for viewer");
        publishDashboard(true);

        createDashboard("Locked and published for viewer");
        publishDashboard(true);
        lockDashboard(true);

        createDashboard("Unlocked and unpublished for viewer");
        publishDashboard(false);

        createDashboard("Locked and unpublished for viewer");
        publishDashboard(false);
        lockDashboard(true);

        createDashboard("Locked and published for editor to share");
        publishDashboard(true);
        lockDashboard(true);
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnMethods = {"prepareEditorAndViewerTests"}, groups = {"viewer-tests", "sanity"})
    public void prepareViewerTests() throws JSONException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.VIEWER);
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnMethods = {"prepareViewerTests"}, groups = {"viewer-tests", "sanity"})
    public void shouldShowLockedAndUnlockedDashboardsToViewer() throws JSONException {
        initDashboardsPage();
        List<String> dashboards = dashboardsPage.getDashboardsNames();

        assertTrue(dashboards.contains("Unlocked and published for viewer"));
        assertTrue(dashboards.contains("Locked and published for viewer"));
        assertFalse(dashboards.contains("Unlocked and unpublished for viewer"));
        assertFalse(dashboards.contains("Locked and unpublished for viewer"));
    }

    /**
     * open dashboards and check icons
     */
    @Test(dependsOnMethods = {"prepareViewerTests"}, groups = {"viewer-tests"})
    public void shouldNotShowLockIconToViewer() {
        selectDashboard("Locked and published for viewer");
        assertFalse(dashboardsPage.isLocked());

        selectDashboard("Unlocked and published for viewer");
        assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnGroups = {"viewer-tests"}, groups = {"editor-tests", "sanity"}, alwaysRun = true)
    public void prepareEditorTests() throws JSONException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.EDITOR);
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests", "sanity"})
    public void shouldShowLockedAndUnlockedDashboardsToEditor() throws JSONException {
        initDashboardsPage();
        List<String> dashboards = dashboardsPage.getDashboardsNames();

        assertTrue(dashboards.contains("Unlocked and published for viewer"));
        assertTrue(dashboards.contains("Locked and published for viewer"));
        assertFalse(dashboards.contains("Unlocked and unpublished for viewer"));
        assertFalse(dashboards.contains("Locked and unpublished for viewer"));
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldShowLockIconToEditor() {
        selectDashboard("Locked and published for viewer");
        assertTrue(dashboardsPage.isLocked());

        selectDashboard("Unlocked and published for viewer");
        assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldNotAllowEditorToEditLockedDashboard() {
        selectDashboard("Locked and published for viewer");
        waitForDashboardPageLoaded(browser);
        assertFalse(dashboardsPage.isEditButtonPresent());
    }

    /**
     * CL-6018 test case - editor can switch visibility of project but cant see locking
     */
    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldAllowEditorChangeVisibilityLockedDashboard() throws JSONException {
        selectDashboard("Locked and published for editor to share");

        publishDashboard(false);
        assertEquals(dashboardsPage.isUnlisted(), true);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

        assertFalse(permissionsDialog.isLockOptionDisplayed());

        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(permissionsDialog.getAddedGrantees().size(), 1);

        selectCandidatesAndShare(addGranteesDialog, viewerLogin);

        assertEquals(permissionsDialog.getAddedGrantees().size(), 2);
    }

    @Test(dependsOnGroups = {"editor-tests"}, groups = {"acl-tests", "sanity"}, alwaysRun = true)
    public void prepareACLTests() throws Exception {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);

        String userGroup1Uri =
                RestUtils.addUserGroup(getRestApiClient(), testParams.getProjectId(), ALCOHOLICS_ANONYMOUS);
        String userGroup2Uri =
                RestUtils.addUserGroup(getRestApiClient(), testParams.getProjectId(), XENOFOBES_XYLOPHONES);
        userGroup1Id = getUserGroupID(userGroup1Uri);
        userGroup2Id = getUserGroupID(userGroup2Uri);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldHaveGranteeCandidatesAvailable() throws JSONException {
        createDashboard("ACL test dashboard");
        publishDashboard(false);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(permissionsDialog.getAddedGrantees().size(), 1);

        List<WebElement> candidates =
                Lists.newArrayList(waitForCollectionIsNotEmpty(addGranteesDialog.getGrantees()));
        assertEquals(candidates.size(), 4);
        By nameSelector = By.cssSelector(".grantee-name");
        By loginSelector = By.cssSelector(".grantee-email");
        List<String> expectedGrantees =
                Arrays.asList(ALCOHOLICS_ANONYMOUS, XENOFOBES_XYLOPHONES, editorLogin, viewerLogin);
        List<String> actualGrantees =
                Arrays.asList(candidates.get(0).findElement(nameSelector).getText().trim(), candidates.get(1)
                        .findElement(nameSelector).getText().trim(), candidates.get(2).findElement(loginSelector)
                        .getText().trim(), candidates.get(3).findElement(loginSelector).getText().trim());
        assertTrue(CollectionUtils.isEqualCollection(expectedGrantees, actualGrantees),
                "Report isn't applied filter correctly");
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldNotShareDashboardWhenDialogWasDismissed() throws JSONException {
        selectDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndCancel(addGranteesDialog, viewerLogin, editorLogin, ALCOHOLICS_ANONYMOUS,
                XENOFOBES_XYLOPHONES);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 1);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShareDashboardToUsers() throws JSONException {
        closeDialogIfVisible();
        createDashboard("Dashboard shared to users");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, viewerLogin, editorLogin);

        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShareDashboardToGroups() throws JSONException {
        closeDialogIfVisible();
        createDashboard("Dashboard shared to groups");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, ALCOHOLICS_ANONYMOUS, XENOFOBES_XYLOPHONES);

        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShowNoUserIfNoneMatchesSearchQuery() throws JSONException {
        selectDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount("dsdhjak", false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShowCorrectResultIfSearchQueryContainsSpecialCharacters() throws JSONException {
        selectDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount("?!#&", false), 0);
        assertEquals(addGranteesDialog.getGranteesCount("null", false), 0);
        assertEquals(addGranteesDialog.getGranteesCount("<button>abc</button>", false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShowNoResultIfSearchGroup() throws JSONException {
        selectDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount(ALCOHOLICS_ANONYMOUS, false), 0);
        assertEquals(addGranteesDialog.getGranteesCount(XENOFOBES_XYLOPHONES, false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldNotShowGranteesInCandidatesDialog() throws JSONException {
        createDashboard("No duplicate grantees dashboard");

        PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount(editorLogin, true), 1);

        selectCandidatesAndShare(addGranteesDialog, editorLogin);

        addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount(editorLogin, false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShowDashboardSharedWithAllUser() throws JSONException {
        closeDialogIfVisible();
        createDashboard("Dashboard shared to all users and groups");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, viewerLogin, editorLogin, ALCOHOLICS_ANONYMOUS,
                XENOFOBES_XYLOPHONES);

        assertEquals(permissionsDialog.getAddedGrantees().size(), 5);

        permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount("", false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldCacheSpecificUsersWhenSwitchFromEveryoneToSpecificUsers() throws JSONException {
        createDashboard("Dashboard shared to some specific users");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, viewerLogin, ALCOHOLICS_ANONYMOUS);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);

        permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
        sleepTightInSeconds(1);
        assertEquals(permissionsDialog.getRoot().findElements(PermissionsDialog.GRANTEES_PANEL).size(), 0);

        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        sleepTightInSeconds(1);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);
    }

    @Test(dependsOnMethods = {"shouldShowDashboardSharedWithAllUser"}, groups = {"acl-tests"})
    public void shouldEditorEditGrantees() throws JSONException {
        try {
            logout();
            signIn(false, UserRoles.EDITOR);

            selectDashboard("Dashboard shared to all users and groups");
            final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

            assertEquals(permissionsDialog.getAddedGrantees().size(), 5);

            permissionsDialog.removeUser(viewerLogin);
            permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
            permissionsDialog.removeGroup(XENOFOBES_XYLOPHONES);
            permissionsDialog.removeUser(editorLogin);
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            assertTrue(permissionsDialog.checkCannotRemoveOwner(), "There is the delete icon of DB Owner grantee");

            permissionsDialog.undoRemoveUser(editorLogin);

            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.undoRemoveGroup(ALCOHOLICS_ANONYMOUS);
            permissionsDialog.submit();

            dashboardsPage.openPermissionsDialog();

            assertEquals(permissionsDialog.getAddedGrantees().size(), 3);

            final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
            selectCandidatesAndShare(addGranteesDialog, viewerLogin, XENOFOBES_XYLOPHONES);
            assertEquals(permissionsDialog.getAddedGrantees().size(), 5);

            permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
            permissionsDialog.submit();

            assertFalse(dashboardsPage.isUnlisted());
            waitForElementVisible(By.cssSelector(".s-btn-ok__got_it"), browser).click();
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
            selectDashboard("Dashboard shared to all users and groups");
            final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
            permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
            permissionsDialog.submit();
        }
    }

    @Test(dependsOnMethods = {"shouldShowDashboardSharedWithAllUser"}, groups = {"acl-tests"})
    public void shouldRevertStatusOfSubmitButton() throws JSONException {
        selectDashboard("Dashboard shared to all users and groups");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Done");

        permissionsDialog.removeUser(editorLogin);
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Save Changes");

        permissionsDialog.undoRemoveUser(editorLogin);
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Done");

        permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Save Changes");

        permissionsDialog.undoRemoveGroup(ALCOHOLICS_ANONYMOUS);
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Done");
    }

    @Test(dependsOnMethods = {"shouldShowDashboardSharedWithAllUser"}, groups = {"acl-tests"})
    public void shouldUseExistingPermissionsInSaveAs() throws JSONException {
        selectDashboard("Dashboard shared to all users and groups");

        dashboardsPage.saveAsDashboard("Check Permission in Dashboad Save As",
                PermissionType.USE_EXISTING_PERMISSIONS);
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

        assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
    }

    @Test(dependsOnGroups = {"acl-tests"}, groups = {"acl-tests-usergroups", "sanity"}, alwaysRun = true)
    public void prepareUsergroupTests() throws IOException, JSONException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);

        RestUtils.addUsersToUserGroup(getRestApiClient(), userGroup1Id, editorProfileUri);
        RestUtils.addUsersToUserGroup(getRestApiClient(), userGroup2Id, viewerProfileUri);
    }

    @Test(dependsOnMethods = {"prepareUsergroupTests"}, groups = {"acl-tests-usergroups", "sanity"})
    public void shouldVisibleToUserInGroup() throws JSONException {
        createDashboard("Dashboard shared to user group");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, viewerLogin, ALCOHOLICS_ANONYMOUS);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);

        permissionsDialog.submit();
        assertTrue(checkDashboardVisible("Dashboard shared to user group"));
    }

    @Test(dependsOnMethods = {"shouldVisibleToUserInGroup"}, groups = {"acl-tests-usergroups"})
    public void shouldInvisibleToUserIfRelatedGroupIsRemoved() throws JSONException {
        selectDashboard("Dashboard shared to user group");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

        permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
        permissionsDialog.submit();

        assertFalse(checkDashboardVisible("Dashboard shared to user group"));
    }

    @Test(dependsOnMethods = {"shouldInvisibleToUserIfRelatedGroupIsRemoved"}, groups = {"acl-tests-usergroups"})
    public void shouldVisibleToUserIfRelatedGroupIsRemovedButUserIsKept() throws JSONException {
        selectDashboard("Dashboard shared to user group");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        assertEquals(permissionsDialog.getAddedGrantees().size(), 2);

        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        selectCandidatesAndShare(addGranteesDialog, editorLogin);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);

        permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);

        permissionsDialog.submit();
        assertTrue(checkDashboardVisible("Dashboard shared to user group"));
    }

    @Test(dependsOnMethods = {"prepareUsergroupTests"}, groups = {"acl-tests-usergroups"})
    public void shouldNotRemoveUserGroupWhenDialogDismissed() throws JSONException {
        selectDashboard("Dashboard shared to all users and groups");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();

        permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
        permissionsDialog.cancel();

        dashboardsPage.openPermissionsDialog();
        assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
    }

    /**
     * CL-6045 test case - user (nor owner or grantee) can see warn message before kick himself from
     * grantees
     */
    @Test(dependsOnMethods = {"prepareUsergroupTests"}, groups = {"acl-tests-usergroups"})
    public void shouldShowHidingFromYourselfNotificationToEditor() throws JSONException {
        try {
            createDashboard("Hide yourself test dashboard");
            publishDashboard(true);

            logout();
            signIn(false, UserRoles.EDITOR);
            selectDashboard("Hide yourself test dashboard");
            final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
            permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
            selectCandidatesAndShare(addGranteesDialog, ALCOHOLICS_ANONYMOUS);
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            permissionsDialog.undoRemoveGroup(ALCOHOLICS_ANONYMOUS);
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            addGranteesDialog = permissionsDialog.openAddGranteePanel();
            selectCandidatesAndShare(addGranteesDialog, editorLogin);
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeUser(editorLogin);
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            permissionsDialog.undoRemoveUser(editorLogin);
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeUser(editorLogin);
            permissionsDialog.submit();

            selectDashboard("Published dashboard");
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            List<String> dashboards = dashboardsPage.getDashboardsNames();
            assertFalse(dashboards.contains("Hide yourself test dashboard"));
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    private boolean checkDashboardVisible(String dashboardName) throws JSONException {
        try {
            logout();
            signIn(false, UserRoles.EDITOR);

            initDashboardsPage();
            List<String> dashboards = dashboardsPage.getDashboardsNames();

            return dashboards.contains(dashboardName);
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    private void selectCandidatesAndShare(AddGranteesDialog addGranteesDialog, String... candidates) {
        selectCandidates(addGranteesDialog, candidates);
        addGranteesDialog.share();
        sleepTightInSeconds(1);
    }

    private void selectCandidatesAndCancel(AddGranteesDialog addGranteesDialog, String... candidates) {
        selectCandidates(addGranteesDialog, candidates);
        addGranteesDialog.cancel();
        sleepTightInSeconds(1);
    }

    private void selectCandidates(AddGranteesDialog addGranteesDialog, String... candidates) {
        for (String candidate : candidates) {
            addGranteesDialog.selectItem(candidate);
        }
    }

    private String getUserGroupID(String userGroupUri) {
        String[] parts = userGroupUri.split("/");
        return parts[parts.length - 1];
    }

    private void closeDialogIfVisible() {
        initDashboardsPage();
        System.out.println("Try to close permission dialog...");
        if (!dashboardsPage.isPermissionDialogVisible())
            return;

        PermissionsDialog permissionsDialog = dashboardsPage.getPermissionsDialog();
        if (permissionsDialog.isDoneButtonVisible()) {
            permissionsDialog.done();
        } else {
            permissionsDialog.cancel();
        }
    }
}
