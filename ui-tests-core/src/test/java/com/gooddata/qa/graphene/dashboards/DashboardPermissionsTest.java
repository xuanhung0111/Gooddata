package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog.ALERT_INFOBOX_CSS_SELECTOR;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.PublishType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.google.common.collect.Lists;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {

    private static final String XENOFOBES_XYLOPHONES = "Xenofobes & xylophones";
    private static final String ALCOHOLICS_ANONYMOUS = "Alcoholics anonymous";
    private static final String UNCHANGED_DASHBOARD = "Unchanged dashboard";
    private String userGroup1Id;
    private String userGroup2Id;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"}, priority = 0)
    public void checkBackToTheOnlyOneVisibileDashboard() throws IOException, JSONException {
        String dashboardName = "Admin Unpublished Dashboard";
        try {
            String dashboardUri = createTestDashboard(dashboardName);
            publishDashboard(false);

            logout();
            signIn(true, UserRoles.EDITOR);

            // Editor loads the dashboard url of Admin
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardUri);

            waitForDashboardPageLoaded(browser);
            assertEquals(dashboardsPage.getDashboardName(), dashboardName);
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
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldLockDashboard() throws Exception {
        createTestDashboard("Locked dashboard");
        lockDashboard(true);
        assertEquals(dashboardsPage.isLocked(), true);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldUnlockDashboard() throws Exception {
        createTestDashboard("Unlocked dashboard");
        lockDashboard(false);
        assertEquals(dashboardsPage.isLocked(), false);
    }

    /**
     * publish - make dashboard visible to every1 ( don't touch locking )
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldPublishDashboard() throws JSONException, IOException {
        createTestDashboard("Published dashboard");
        publishDashboard(true);
        assertFalse(dashboardsPage.isUnlisted());
    }

    /**
     * unpublish - make dashboard visible to owner only ( don't touch locking )
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void shouldUnpublishDashboard() throws JSONException, IOException {
        createTestDashboard("Unpublished dashboard");
        publishDashboard(false);
        assertTrue(dashboardsPage.isUnlisted());
    }

    /**
     * when a dashboard is created, its default settings are "visibility:specific user" + editing
     * unlocked change visibility to everyone can access, editing locked and hit cancel button to
     * forget changes
     * @throws IOException
     * @throws JSONException
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
    public void shouldNotChangePermissionsWhenCancelled() throws JSONException, IOException {
        createTestDashboard(UNCHANGED_DASHBOARD);

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
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
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
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
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
     * @throws IOException 
     * @throws JSONException 
     */
    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests", "sanity"})
    public void checkPermissionDialogInDashboardEditMode() throws JSONException, IOException {
        createTestDashboard("Check Permission in Edit Mode");

        PermissionsDialog permissionsDialog = dashboardsPage.unlistedIconClick();
        permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
        permissionsDialog.submit();
        assertFalse(dashboardsPage.isUnlisted());
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     * @throws IOException
     */
    @Test(dependsOnGroups = {"admin-tests"}, groups = {"viewer-tests", "sanity"}, alwaysRun = true)
    public void prepareEditorAndViewerTests() throws JSONException, IOException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);
        initDashboardsPage();

        createTestDashboard("Unlocked and published for viewer");
        publishDashboard(true);

        createTestDashboard("Locked and published for viewer");
        publishDashboard(true);
        lockDashboard(true);

        createTestDashboard("Unlocked and unpublished for viewer");
        publishDashboard(false);

        createTestDashboard("Locked and unpublished for viewer");
        publishDashboard(false);
        lockDashboard(true);

        createTestDashboard("Locked and published for editor to share");
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

        selectCandidatesAndShare(addGranteesDialog, testParams.getViewerUser());

        assertEquals(permissionsDialog.getAddedGrantees().size(), 2);
    }

    @Test(dependsOnGroups = {"editor-tests"}, groups = {"acl-tests", "sanity"}, alwaysRun = true)
    public void prepareACLTests() throws Exception {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);

        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String userGroup1Uri = userManagementRestRequest.addUserGroup(ALCOHOLICS_ANONYMOUS);
        String userGroup2Uri = userManagementRestRequest.addUserGroup(XENOFOBES_XYLOPHONES);
        userGroup1Id = getUserGroupID(userGroup1Uri);
        userGroup2Id = getUserGroupID(userGroup2Uri);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldHaveGranteeCandidatesAvailable() throws JSONException, IOException {
        createTestDashboard("ACL test dashboard");
        publishDashboard(false);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(permissionsDialog.getAddedGrantees().size(), 1);

        List<WebElement> candidates =
                Lists.newArrayList(waitForCollectionIsNotEmpty(addGranteesDialog.getGrantees()));

        assertEquals(candidates.size(), testParams.getDomainUser() != null ? 5 : 4);
        By nameSelector = By.cssSelector(".grantee-name");
        By loginSelector = By.cssSelector(".grantee-email");
        List<String> expectedGrantees =
                new ArrayList<>(asList(ALCOHOLICS_ANONYMOUS, XENOFOBES_XYLOPHONES,
                        testParams.getEditorUser(), testParams.getViewerUser()));
        List<String> actualGrantees =
                new ArrayList<>(asList(candidates.get(0).findElement(nameSelector).getText().trim(), candidates.get(1)
                        .findElement(nameSelector).getText().trim(), candidates.get(2).findElement(loginSelector)
                        .getText().trim(), candidates.get(3).findElement(loginSelector).getText().trim()));

        if (testParams.getDomainUser() != null) {
            expectedGrantees.add(testParams.getDomainUser());
            actualGrantees.add(candidates.get(4).findElement(loginSelector).getText().trim());
        }
        assertTrue(CollectionUtils.isEqualCollection(expectedGrantees, actualGrantees),
                "Report isn't applied filter correctly");
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldNotShareDashboardWhenDialogWasDismissed() throws JSONException {
        selectDashboard(UNCHANGED_DASHBOARD);

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndCancel(addGranteesDialog, testParams.getViewerUser(), testParams.getEditorUser(), ALCOHOLICS_ANONYMOUS,
                XENOFOBES_XYLOPHONES);
        assertEquals(permissionsDialog.getAddedGrantees().size(), 1);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShareDashboardToUsers() throws JSONException, IOException {
        closeDialogIfVisible();
        createTestDashboard("Dashboard shared to users");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, testParams.getViewerUser(), testParams.getEditorUser());

        assertEquals(permissionsDialog.getAddedGrantees().size(), 3);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShareDashboardToGroups() throws JSONException, IOException {
        closeDialogIfVisible();
        createTestDashboard("Dashboard shared to groups");

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
    public void shouldNotShowGranteesInCandidatesDialog() throws JSONException, IOException {
        createTestDashboard("No duplicate grantees dashboard");

        PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount(testParams.getEditorUser(), true), 1);

        selectCandidatesAndShare(addGranteesDialog, testParams.getEditorUser());

        addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount(testParams.getEditorUser(), false), 0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests", "sanity"})
    public void shouldShowDashboardSharedWithAllUser() throws JSONException, IOException {
        closeDialogIfVisible();
        createTestDashboard("Dashboard shared to all users and groups");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        List<String> candidates = new ArrayList<>(asList(testParams.getViewerUser(), testParams.getEditorUser(),
                ALCOHOLICS_ANONYMOUS, XENOFOBES_XYLOPHONES));
        if (testParams.getDomainUser() != null) {
            candidates.add(testParams.getDomainUser());
        }

        selectCandidatesAndShare(addGranteesDialog, candidates.toArray(new String[0]));

        if (testParams.getDomainUser() != null) {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 6);
        } else {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
        }
        assertEquals(permissionsDialog.openAddGranteePanel().getGranteesCount("", false),0);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldCacheSpecificUsersWhenSwitchFromEveryoneToSpecificUsers() throws JSONException, IOException {
        createTestDashboard("Dashboard shared to some specific users");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, testParams.getViewerUser(), ALCOHOLICS_ANONYMOUS);
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

            if (testParams.getDomainUser() != null) {
                assertEquals(permissionsDialog.getAddedGrantees().size(), 6);
            } else {
                assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
            }

            permissionsDialog.removeUser(testParams.getViewerUser());
            permissionsDialog.removeGroup(ALCOHOLICS_ANONYMOUS);
            permissionsDialog.removeGroup(XENOFOBES_XYLOPHONES);
            permissionsDialog.removeUser(testParams.getEditorUser());
            if (testParams.getDomainUser() != null) {
                permissionsDialog.removeUser(testParams.getDomainUser());
            }

            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            assertTrue(permissionsDialog.checkCannotRemoveOwner(),
                    "There is the delete icon of dashboard owner grantee");

            permissionsDialog.undoRemoveUser(testParams.getEditorUser());

            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.undoRemoveGroup(ALCOHOLICS_ANONYMOUS);
            permissionsDialog.submit();

            dashboardsPage.openPermissionsDialog();
            assertEquals(permissionsDialog.getAddedGrantees().size(), 3);

            final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

            if (testParams.getDomainUser() != null) {
                selectCandidatesAndShare(addGranteesDialog, testParams.getDomainUser(), testParams.getViewerUser(),
                        XENOFOBES_XYLOPHONES);
                assertEquals(permissionsDialog.getAddedGrantees().size(), 6);
            } else {
                selectCandidatesAndShare(addGranteesDialog, testParams.getViewerUser(), XENOFOBES_XYLOPHONES);
                assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
            }

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

        permissionsDialog.removeUser(testParams.getEditorUser());
        assertEquals(permissionsDialog.getTitleOfSubmitButton(), "Save Changes");

        permissionsDialog.undoRemoveUser(testParams.getEditorUser());
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

        if (testParams.getDomainUser() != null) {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 6);
        } else {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
        }
    }

    @Test(dependsOnGroups = {"acl-tests"}, groups = {"acl-tests-usergroups", "sanity"}, alwaysRun = true)
    public void prepareUsergroupTests() throws IOException, JSONException {
        initDashboardsPage();

        logout();
        signIn(false, UserRoles.ADMIN);

        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String editorProfileUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());
        String viewerProfileUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(), testParams.getViewerUser());

        userManagementRestRequest.addUsersToUserGroup(userGroup1Id, editorProfileUri);
        userManagementRestRequest.addUsersToUserGroup(userGroup2Id, viewerProfileUri);
    }

    @Test(dependsOnMethods = {"prepareUsergroupTests"}, groups = {"acl-tests-usergroups", "sanity"})
    public void shouldVisibleToUserInGroup() throws JSONException, IOException {
        createTestDashboard("Dashboard shared to user group");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        selectCandidatesAndShare(addGranteesDialog, testParams.getViewerUser(), ALCOHOLICS_ANONYMOUS);
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
        selectCandidatesAndShare(addGranteesDialog, testParams.getEditorUser());
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
        if (testParams.getDomainUser() != null) {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 6);
        } else {
            assertEquals(permissionsDialog.getAddedGrantees().size(), 5);
        }
    }

    /**
     * CL-6045 test case - user (nor owner or grantee) can see warn message before kick himself from
     * grantees
     * @throws IOException
     */
    @Test(dependsOnMethods = {"prepareUsergroupTests"}, groups = {"acl-tests-usergroups"})
    public void shouldShowHidingFromYourselfNotificationToEditor() throws JSONException, IOException {
        try {
            createTestDashboard("Hide yourself test dashboard");
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
            selectCandidatesAndShare(addGranteesDialog, testParams.getEditorUser());
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeUser(testParams.getEditorUser());
            waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));

            permissionsDialog.undoRemoveUser(testParams.getEditorUser());
            assertEquals(permissionsDialog.getRoot().findElements(ALERT_INFOBOX_CSS_SELECTOR).size(), 0);

            permissionsDialog.removeUser(testParams.getEditorUser());
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

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
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
            log.info("Select user/usergroup: " + candidate);
            addGranteesDialog.searchAndSelectItem(candidate);
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

    private String createTestDashboard(String name) throws JSONException, IOException {
        JSONObject dashboardObj = new JSONObject() {{
            put("projectDashboard", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("rememberFilters", 0);
                    put("tabs", new JSONArray() {{
                        put(new JSONObject() {{
                            put("title", "First Tab");
                            put("items", new JSONArray());
                        }});
                    }});
                    put("filters", new JSONArray());
                }});
                put("meta", new JSONObject() {{
                    put("title", name);
                    put("locked", 0);
                    put("unlisted", 1); // need this value to display unlisted/eye icon
                }});
            }});
        }};

        String dashboardURI = dashboardRequest.createDashboard(dashboardObj);

        //refresh page to update the dashboards has just been created
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + DASHBOARD_PAGE_SUFFIX + "|" + dashboardURI);
        WaitUtils.waitForDashboardPageLoaded(browser);
        return dashboardURI;
    }
}
