package com.gooddata.qa.graphene.dashboards;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.PublishType;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddGranteesDialog;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog.ALERT_INFOBOX_CSS_SELECTOR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DashboardPermissionsTest extends GoodSalesAbstractTest {

    private String viewerLogin;
    private String editorLogin;

    @BeforeClass
    public void before() throws InterruptedException {
        addUsersWithOtherRoles = true;
        viewerLogin = testParams.getViewerUser();
        editorLogin = testParams.getEditorUser();
    }

    /**
     * lock dashboard - only admins can edit
     * @throws Exception 
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldLockDashboard() throws Exception {
        createDashboard("Locked dashboard");
        lockDashboard(true);
        assertEquals(dashboardsPage.isLocked(), true);
    }
    
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldUnlockDashboard() throws Exception {
        createDashboard("Unlocked dashboard");
        lockDashboard(false);
        assertEquals(dashboardsPage.isLocked(), false);
    }

    /**
     * publish - make dashboard visible to every1 ( don't touch locking )
     * @throws InterruptedException 
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldPublishDashboard() throws InterruptedException {
        createDashboard("Published dashboard");
        publishDashboard(true);
        assertEquals(dashboardsPage.isUnlisted(), false);
    }

    /**
     * unpublish - make dashboard visible to owner only ( don't touch locking )
     * @throws InterruptedException 
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldUnpublishDashboard() throws InterruptedException {
        createDashboard("Unpublished dashboard");
        publishDashboard(false);
        assertEquals(dashboardsPage.isUnlisted(), true);
    }

    /**
     * change visibility to specific user can access, unlock and hit cancel button to forget changes
     * @throws InterruptedException 
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"admin-tests"})
    public void shouldNotChangePermissionsWhenCancelled() throws InterruptedException {
        createDashboard("Unchanged dashboard");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        permissionsDialog.publish(PublishType.SPECIFIC_USERS_CAN_ACCESS);
        permissionsDialog.lock();
        permissionsDialog.cancel();
        
        waitForElementVisible(dashboardsPage.getRoot());
        assertFalse(dashboardsPage.isLocked());
        assertTrue(dashboardsPage.isUnlisted());
    }

    /**
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible for all
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
     * click to "eye" icon instead of settings and check that dashboard is locked and not visible for all
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
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnGroups = {"admin-tests"}, groups = {"viewer-tests"})
    public void prepareEditorAndViewerTests() throws JSONException, InterruptedException {
        initDashboardsPage();
        
        logout();
        signIn(false, UserRoles.ADMIN);
        
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
    @Test(dependsOnMethods = {"prepareEditorAndViewerTests"}, groups = {"viewer-tests"})
    public void prepareViewerTests() throws JSONException, InterruptedException {
        initDashboardsPage();
        
        logout();
        signIn(false, UserRoles.VIEWER);
    }

    /**
     * check dashboard names visible to viewer - should see both - both are visible to every1
     */
    @Test(dependsOnMethods = {"prepareViewerTests"}, groups = {"viewer-tests"})
    public void shouldShowLockedAndUnlockedDashboardsToViewer() throws JSONException, InterruptedException {
        initDashboardsPage();
        List<String> dashboards = dashboardsPage.getDashboardsNames();
        
        assertTrue(dashboards.contains("Unlocked and published for viewer"));
        assertTrue(dashboards.contains("Locked and published for viewer"));
        assertFalse(dashboards.contains("Unlocked and unpublished for viewer"));
        assertFalse(dashboards.contains("Locked and unpublished for viewer"));
    }

    /**
     *  open dashboards and check icons
     */
    @Test(dependsOnMethods = {"prepareViewerTests"}, groups = {"viewer-tests"})
    public void shouldNotShowLockIconToViewer() throws InterruptedException {
        selectDashboard("Locked and published for viewer");
        assertFalse(dashboardsPage.isLocked());
        
        selectDashboard("Unlocked and published for viewer");
        assertFalse(dashboardsPage.isLocked());
    }
    
    @Test(dependsOnGroups = {"viewer-tests"}, groups = {"editor-tests"})
    public void prepareEditorTests() throws JSONException, InterruptedException {
        initDashboardsPage();
        
        logout();
        signIn(false, UserRoles.EDITOR);
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldShowLockedAndUnlockedDashboardsToEditor() throws JSONException, InterruptedException {
        initDashboardsPage();
        List<String> dashboards = dashboardsPage.getDashboardsNames();
        
        assertTrue(dashboards.contains("Unlocked and published for viewer"));
        assertTrue(dashboards.contains("Locked and published for viewer"));
        assertFalse(dashboards.contains("Unlocked and unpublished for viewer"));
        assertFalse(dashboards.contains("Locked and unpublished for viewer"));
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldShowLockIconToEditor() throws InterruptedException {
        selectDashboard("Locked and published for viewer");
        assertTrue(dashboardsPage.isLocked());
        
        selectDashboard("Unlocked and published for viewer");
        assertFalse(dashboardsPage.isLocked());
    }

    @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
    public void shouldNotAllowEditorToEditLockedDashboard() throws InterruptedException {
        selectDashboard("Locked and published for viewer");
        waitForDashboardPageLoaded(browser);
        assertFalse(dashboardsPage.isEditButtonPresent());
    }
    
      /**
      * CL-6018 test case - editor can switch visibility of project but cant see locking
      */
     @Test(dependsOnMethods = {"prepareEditorTests"}, groups = {"editor-tests"})
     public void shouldAllowEditorChangeVisibilityLockedDashboard() throws JSONException, InterruptedException {
         selectDashboard("Locked and published for editor to share");
         
         publishDashboard(false);
         assertEquals(dashboardsPage.isUnlisted(), true);
    
         final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
         
         // The click that should open grantee candidates dialog does not work here
         // I am suspicious it might be because there is a message informing the user
         // that he is hiding this dashboard from himself.
         Thread.sleep(1000);
         
         final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
         
         List<WebElement> addedGrantees = permissionsDialog.getAddedGrantees();
         assertEquals(addedGrantees.size(), 1);
         
         addGranteesDialog.selectItem(viewerLogin);
         addGranteesDialog.share();
         Thread.sleep(1000);
         
         addedGrantees = permissionsDialog.getAddedGrantees();
         assertEquals(addedGrantees.size(), 2);
     }
    
    @Test(dependsOnGroups = {"editor-tests"}, groups = {"acl-tests"})
    public void prepareACLTests() throws Exception {
        initDashboardsPage();
        
        logout();
        signIn(false, UserRoles.ADMIN);
        
        String projectId = testParams.getProjectId();
        
        RestUtils.addUserGroup(getRestApiClient(), projectId, "Alcoholics anonymous");
        RestUtils.addUserGroup(getRestApiClient(), projectId, "Xenofobes & xylophones");
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldHaveGranteeCandidatesAvailable() throws JSONException, InterruptedException {
        selectDashboard("Unchanged dashboard");

        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        assertEquals(elements.size(), 1);

        List<WebElement> candidates = Lists.newArrayList(waitForCollectionIsNotEmpty(addGranteesDialog.getGrantees()));
        assertEquals(candidates.size(), 4);
        
        By nameSelector = By.cssSelector(".grantee-name");
        By loginSelector = By.cssSelector(".grantee-email");
        
        assertEquals(candidates.get(0).findElement(nameSelector).getText().trim(), "Alcoholics anonymous");
        assertEquals(candidates.get(1).findElement(nameSelector).getText().trim(), "Xenofobes & xylophones");
        assertEquals(candidates.get(2).findElement(loginSelector).getText().trim(), editorLogin);
        assertEquals(candidates.get(2).findElement(loginSelector).getText().trim(), viewerLogin);
    }
    
    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldNotShareDashboardWhenDialogWasDismissed() throws JSONException, InterruptedException {
        selectDashboard("Unchanged dashboard");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        
        addGranteesDialog.selectItem(viewerLogin);
        addGranteesDialog.selectItem(editorLogin);
        addGranteesDialog.cancel();
        Thread.sleep(1000);

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        assertEquals(elements.size(), 1);
    }
    
    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShareDashboardToUsers() throws JSONException, InterruptedException {
        createDashboard("Dashboard shared to users");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        
        addGranteesDialog.selectItem(viewerLogin);
        addGranteesDialog.selectItem(editorLogin);
        addGranteesDialog.share();
        Thread.sleep(1000);

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        assertEquals(elements.size(), 3);
    }
    
    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShareDashboardToGroups() throws JSONException, InterruptedException {
        createDashboard("Dashboard shared to groups");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        
        addGranteesDialog.selectItem("Alcoholics anonymous");
        addGranteesDialog.selectItem("Xenofobes & xylophones");
        addGranteesDialog.share();
        Thread.sleep(1000);

        List<WebElement> elements = permissionsDialog.getAddedGrantees();
        assertEquals(elements.size(), 3);
    }

    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShowNoUserIfNoneMatchesSearchQuery() throws JSONException, InterruptedException {
        selectDashboard("Unchanged dashboard");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        final AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();

        assertEquals(addGranteesDialog.getGranteesCount("dsdhjak", false), 0);
    }
    
    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldNotShowGranteesInCandidatesDialog() throws JSONException, InterruptedException {
        createDashboard("No duplicate grantees dashboard");
        
        PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        AddGranteesDialog addGranteesDialog = permissionsDialog.openAddGranteePanel();
        
        assertEquals(addGranteesDialog.getGranteesCount(editorLogin, true), 1);
        addGranteesDialog.selectItem(editorLogin);
        addGranteesDialog.share();
        Thread.sleep(500);
        
        permissionsDialog = dashboardsPage.openPermissionsDialog();
        addGranteesDialog = permissionsDialog.openAddGranteePanel();
        
        assertEquals(addGranteesDialog.getGranteesCount(editorLogin, false), 0);
    }

    /**
     * CL-6045 test case - user (nor owner or grantee) can see warn message before kick himself from grantees
     */
    @Test(dependsOnMethods = {"prepareACLTests"}, groups = {"acl-tests"})
    public void shouldShowHidingFromYourselfNotificationToEditor() throws InterruptedException, JSONException {
        createDashboard("Ordinary dashboard");
        
        final PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
        waitForElementPresent(permissionsDialog.getRoot().findElement(ALERT_INFOBOX_CSS_SELECTOR));
        permissionsDialog.cancel();
    }
}
