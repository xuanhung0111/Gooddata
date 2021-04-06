package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static java.util.Arrays.asList;
import static java.lang.String.format;

public class ACLsE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private String firstUser;
    private String secondUser;
    private String thirdUser;
    private String forthUser;
    private String owner;
    private String dataSourceId;
    private DataloadScheduleDetail scheduleDetail;
    private ConnectionDetail snowflakeDetail;
    private ContentDatasourceContainer container;
    private final String invalidUser = "invalidString";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_WAREHOUSE = "ATT_WAREHOUSE";
    private final String DATASOURCE_DATABASE = "ATT_DATASOURCE_TEST";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "PUBLIC";
    private final String PROCESS_NAME = "PROCESS_TEST";

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        DATASOURCE_URL = testParams.getSnowflakeJdbcUrl();
        DATASOURCE_USERNAME = testParams.getSnowflakeUserName();
        DATASOURCE_PASSWORD = testParams.getSnowflakePassword();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareDataTest() throws IOException {
        // Create datasource
        createNewDatasource();
        // Create users
        firstUser = createAndAddUserToProject(UserRoles.ADMIN);
        secondUser = createAndAddUserToProject(UserRoles.ADMIN);
        thirdUser = createAndAddUserToProject(UserRoles.ADMIN);
        forthUser = createAndAddUserToProject(UserRoles.ADMIN);
        owner = testParams.getUser();
    }

    //    As end user A , I created Datasource D1  share it with user B, C valid role Use , add user C again in the dialog,
    //    add invalid user D, user E already exist role Use . Make sure that:
    //    share success to user B, C .
    //      - Just display user C once time
    //      - User D show error message “The username was not recognized and will be skipped” .
    //      - User E show warning message “The Datasource is already shared with this user name. This setting will be overriden”
    //    Share user C, F  with role Manage. Make sure that:
    //      - User C change role to Manage
    //      - User F is shared successfully with role Manage
    //    Verify on Shared with section:
    //      - Display Owner (you) for user A
    //      - Diplay Username, Use, Manage correctly
    //      - Show correctly number at Shared with
    @Test(dependsOnMethods = "prepareDataTest")
    public void shareDSToUsers() {
        initDatasourceManagementPage();
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        snowflakeDetail = container.getConnectionDetail();
        // add user to check existing user
        AddUserDialog addUserDialog = snowflakeDetail.getUserHeading().openAddUserDialog();
        addUserDialog.inputUser(thirdUser);
        addUserDialog.clickShareButton();

        // re-open Add UserDialog, add valid users with Use role
        snowflakeDetail.getUserHeading().openAddUserDialog();
        addUserDialog.inputUser(firstUser);
        addUserDialog.inputUser(secondUser);
        // add again user second, make sure can add only once time
        addUserDialog.inputUser(secondUser);
        // add invalid user
        addUserDialog.inputUser(invalidUser);
        //add user already existing
        addUserDialog.inputUser(thirdUser);
        // add owner of datasource
        addUserDialog.inputUser(owner);

        //make sure doesn't add second User twice
        assertEquals(addUserDialog.getNumberOfTag(), 5);
        //verify popup messages
        assertEquals(addUserDialog.getErrorMessageOnUserTag(invalidUser), "The username was not recognized " +
                "and will be skipped");
        assertEquals(addUserDialog.getWarningMessageOnUserTag(thirdUser), "The Data Source is already " +
                "shared with this username. Its settings will be overriden");
        assertEquals(addUserDialog.getErrorMessageOnUserTag(owner), "The Datasource owner's access right" +
                " can not be modified");
        //Share user, make sure share success to user B,C with role Use, keep existing user E
        addUserDialog.clickShareButton();
        WarningAddUserPopUp warningPopup = WarningAddUserPopUp.getInstance(browser);
        assertEquals(warningPopup.getHeaderText(), "Data Source shared with only a subset of users.");
        assertEquals(warningPopup.getWarningText(), format("Data Source could not be shared with the following usernames: %s.", owner));
        sleepTightInSeconds(5);

        snowflakeDetail.getUserHeading().openAddUserDialog();
        //change role user , and add user to role Manage
        addUserDialog.inputUser(secondUser);
        addUserDialog.inputUser(forthUser);

        addUserDialog.setManagePermission();
        addUserDialog.clickShareButton();

        //add only user has incorrect format
        snowflakeDetail.getUserHeading().openAddUserDialog();
        addUserDialog.inputUser(owner);
        addUserDialog.clickShareButton();
        assertEquals(OverlayWrapper.getInstance(browser).getTextErrorShareUser(), "Data Source could not be shared" +
                " with any of the provided usernames.");
        OverlayWrapper.getInstance(browser).actionOnErrorShareUserDialog();

        //verify list shared user
        List<String> sharedUser = asList(owner, firstUser, secondUser, thirdUser, forthUser);
        UserField userField = snowflakeDetail.getUserField();
        log.info("Share user list:" + sharedUser);
        log.info("list shared user:" + userField.getListSharedUser());
        assertThat(userField.getListSharedUser(), containsInAnyOrder(sharedUser.toArray()));

        //verify Shared with message
        snowflakeDetail.getUserHeading().isCorrectNumberSharedWith(5);

        assertEquals(userField.getRoleOfChosenUser(owner), "Owner");
        assertEquals(userField.getRoleOfChosenUser(firstUser), "Use");
        assertEquals(userField.getRoleOfChosenUser(secondUser), "Use, Manage");
        assertEquals(userField.getRoleOfChosenUser(thirdUser), "Use");
        assertEquals(userField.getRoleOfChosenUser(forthUser), "Use, Manage");

        assertEquals(userField.getNameOfChosenUser(firstUser), "FirstName LastName");
        assertEquals(userField.getNameOfChosenUser(secondUser), "FirstName LastName");
        assertTrue(userField.isCurrentUser(owner));
    }

    //  Login user C, F make sure that C, F can edit datasource
    //  Login user B make sure that user B  just can view datasource
    @DataProvider (name = "permissionCheckProvider")
    public Object[][] permissionCheckProvider() {
        return new Object[][] {
                {firstUser, "Display Edit button, it should hidden !!", "Display Delete button, it should hidden !!",
                        "Add button should disable" },
                {secondUser, "Not display Edit button, it should display !!", "Not display Delete button, it should display !!",
                        "Add button should enable"},
                {forthUser, "Not display More button, it should display !!", "Not display Delete button, it should display !!",
                        "Add button should enable" },
        };
    }
    @Test(dependsOnMethods = "shareDSToUsers", dataProvider = "permissionCheckProvider")
    public void permissionUserTest(String user, String messageEdit, String messageDelete, String messageAddbutton) {
        logout();
        signInAtGreyPages(user, testParams.getPassword());
        selectDatasource();
        UserField userField = snowflakeDetail.getUserField();
        if (userField.getRoleOfChosenUser(user).equals("Use")) {
            getHeading().clickMoreButton();
            assertFalse(canEditDatasourceOnUI(), messageEdit);
            assertFalse(canDeleteDatasourceOnUI(), messageDelete);
            assertTrue(snowflakeDetail.getUserHeading().isAddButtonDisable(), messageAddbutton);
        } else {
            getHeading().clickMoreButton();
            assertTrue(canEditDatasourceOnUI(), messageEdit);
            assertTrue(canDeleteDatasourceOnUI(), messageDelete);
            assertFalse(snowflakeDetail.getUserHeading().isAddButtonDisable(), messageAddbutton);
        }
    }

    //    User A can:
    //      - change role user F to Use by click Pencil button
    //      - change role user B to Manage by click Pencil button
    //    Login user F again, make sure F just can View datasource
    //    Remove user F by click “X" button on Share with datasource
    @Test(dependsOnMethods = "permissionUserTest")
    public void changePermissionUserTest() {
        logout();
        signInAtGreyPages(owner, testParams.getPassword());
        initDatasourceManagementPage();
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        UserField userField = snowflakeDetail.getUserField();

        EditUserDialog editUserDialog = userField.openEditUseDialog(forthUser);
        editUserDialog.clickCancel();
        assertEquals(userField.getRoleOfChosenUser(forthUser), "Use, Manage");

        EditUserDialog editUserDialog2 = userField.openEditUseDialog(forthUser);
        editUserDialog2.setUsePermission();
        editUserDialog2.clickSave();
        assertEquals(userField.getRoleOfChosenUser(forthUser), "Use");

        EditUserDialog editUserDialog3 = userField.openEditUseDialog(firstUser);
        editUserDialog3.setManagePermission();
        editUserDialog3.clickSave();
        assertEquals(userField.getRoleOfChosenUser(firstUser), "Use, Manage");

        logout();
        signInAtGreyPages(forthUser, testParams.getPassword());
        selectDatasource();
        getHeading().clickMoreButton();
        assertFalse(canEditDatasourceOnUI(), "Display Edit button, it should hidden !!");
        assertTrue(snowflakeDetail.getUserHeading().isAddButtonDisable(), "Add button should disable");

        logout();
        signInAtGreyPages(firstUser, testParams.getPassword());
        selectDatasource();
        getHeading().clickMoreButton();
        assertTrue(canEditDatasourceOnUI(), "Not display Edit button, it should display !!");

        DeleteUserDialog deleteUserDialog = userField.openDeleteUseDialog(forthUser);
        deleteUserDialog.clickCancel();
        snowflakeDetail.getUserHeading().isCorrectNumberSharedWith(5);

        DeleteUserDialog deleteUserDialog2 = userField.openDeleteUseDialog(forthUser);
        deleteUserDialog2.clickDeleteButton();
        snowflakeDetail.getUserHeading().isCorrectNumberSharedWith(4);
        //verify list shared user
        List<String> sharedUser = asList(owner, firstUser, secondUser, thirdUser);
        assertThat(userField.getListSharedUser(), containsInAnyOrder(sharedUser.toArray()));
    }

    //    Login user domain admin , check that :
    //      - User domain admin can edit, delete Datasource
    //      - Doesn't display domain admin on “Shared with" (already cover above)
    @Test(dependsOnMethods = "changePermissionUserTest")
    public void domainAdminPermissionTest() {
        logout();
        signInAtGreyPages(testParams.getDomainUser(), testParams.getPassword());
        openUrl("/admin/connect/#/datasource/" + dataSourceId);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        heading.clickMoreButton();
        assertTrue(MoreContentDialog.getInstance(browser).isEditButtonEnable(), "Domain admin should have permission on this Datasource");
        assertTrue(canDeleteDatasourceOnUI(), "Not display Delete button, it should display !!");
    }

    //    Use ACLs on LCM process make sure user C can run with basic setting.
    //    Check validate error on user F when access schedule because he don't have permission on DS
    @Test(dependsOnMethods = "domainAdminPermissionTest")
    public void verifyOnLCMProcess() {
        try {
            logout();
            signInAtGreyPages(firstUser, testParams.getPassword());
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            sleepTightInSeconds(2);
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            DeploySDDProcessDialog deployLCMProcessDialog = deployForm
                    .scrollToSelectProcessType(DeployProcessForm.ProcessType.LCM_RELEASE, 10000)
                    .enterProcessName(PROCESS_NAME)
                    .clickSwitchToDataSourceLink()
                    .clickAddDatasource();
            Assert.assertEquals(deployLCMProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME);

            deployForm.enterProcessName(PROCESS_NAME).submit();
            assertTrue(projectDetailPage.hasProcess(PROCESS_NAME), "Process is not deployed");


            logout();
            signInAtGreyPages(forthUser, testParams.getPassword());
            initDiscProjectDetailPage();
            projectDetailPage.getProcess(PROCESS_NAME).clickRedeployButton();
            assertEquals(deployLCMProcessDialog.getTextErrorDatasource(), format("Not authorized to use Data Source '%s' (ID=%s)."
                    , DATASOURCE_NAME, dataSourceId));
        } finally {
            logout();
            signInAtGreyPages(firstUser, testParams.getPassword());
            initDiscProjectDetailPage().deleteProcess(PROCESS_NAME);
        }
    }

    //Try to delete datasource by user C, check that : delete datasource successfully
    @Test(dependsOnMethods = "verifyOnLCMProcess")
    public void deleteDatasourceTest() {
        logout();
        signInAtGreyPages(firstUser, testParams.getPassword());
        openUrl("/admin/connect/#/datasource/" + dataSourceId);
        DatasourceHeading heading = container.getDatasourceHeading();
        heading.clickMoreButton();
        MoreContentDialog dialogOption = MoreContentDialog.getInstance(browser);
        dialogOption.clickDeleteButton().clickDelete();
        contentWrapper.waitLoadingManagePage();
        assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws IOException {
        logout();
        signInAtGreyPages(owner, testParams.getPassword());
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
        }
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), firstUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), secondUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), thirdUser);
        userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), forthUser);
    }

    private void createNewDatasource() {
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        dataSourceId = container.getDataSourceId();
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME), "list data sources doesn't have created Datasource");
    }

    private void selectDatasource() {
        initDatasourceManagementPage();
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
    }

    private DatasourceHeading getHeading() {
        return container.getDatasourceHeading();
    }

    private boolean canEditDatasourceOnUI() {
        return MoreContentDialog.getInstance(browser).isEditButtonEnable();
    }

    private boolean canDeleteDatasourceOnUI() {
        return MoreContentDialog.getInstance(browser).isDeleteButtonEnable();
    }

    private void deleteDatasource(String datasourceName) {
        dsMenu.selectDataSource(datasourceName);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        initDatasourceManagementPage();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(datasourceName);
    }
}
