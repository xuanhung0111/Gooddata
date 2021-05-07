package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.EDITOR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.sdk.model.md.report.Report;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.common.StatusBar;
import com.gooddata.qa.graphene.fragments.common.StatusBar.Status;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

public class UserProfileInformationTest extends GoodSalesAbstractTest {

    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private static final String ERROR_MESSAGE = "You are not allowed to see this user profile.";

    private PersonalInfo userInfo;
    private List<String> allVariables;
    private Report adminReport;
    private Report editorReport;
    private Metric avgAmountMetric;
    
    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "User-profile-information-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        avgAmountMetric = createMetric(new RestClient(getProfile(EDITOR)), "Average of Amount",
                format("SELECT AVG([%s])", getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT))),
                DEFAULT_METRIC_FORMAT);
        adminReport = createReportViaRest(new RestClient(getProfile(ADMIN)), GridReportDefinitionContent.create(
                "Report" + generateHashString(),
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm()
                        .getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(avgAmountMetric))));
        editorReport = createReportViaRest(new RestClient(getProfile(EDITOR)), GridReportDefinitionContent.create(
                "Report" + generateHashString(),
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm()
                        .getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(avgAmountMetric))));

        getVariableCreator().createStatusVariable();
        getVariableCreator().createQuoteVariable();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeUserLanguage() throws JSONException, IOException {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, true);
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        try {
            initAccountPage().changeLanguage("Français");
            assertEquals(userManagementRestRequest.getCurrentUserProfile().getString("language"), "fr-FR");
        } finally {
            initAccountPage().changeLanguage("English US");
            assertEquals(userManagementRestRequest.getCurrentUserProfile().getString("language"), "en-US");
            projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, false);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initGetUserInformation() {
        PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
        takeScreenshot(browser, "User info", getClass());
        userInfo = personalInfoDialog.getUserInfo();

        initVariablePage();
        allVariables = ObjectsTable.getInstance(id(ObjectTypes.VARIABLE.getObjectsTableID()), browser).getAllItems();
    }

    @Test(dependsOnMethods = {"initGetUserInformation"})
    public void checkUserProfileInformation() {
        PersonalInfo personalInfo = initReportsPage().getReportOwnerInfoFrom(adminReport.getTitle());
        assertEquals(userInfo.getFullName(), personalInfo.getFullName());
        assertEquals(userInfo.getEmail(), personalInfo.getEmail());
        assertEquals(userInfo.getPhoneNumber(), personalInfo.getPhoneNumber());

        UserProfilePage userProfilePage = ReportsPage.getInstance(browser)
                .openReportOwnerProfilePageFrom(adminReport.getTitle());
        assertEquals(userProfilePage.getUserInfo(), userInfo);

        assertTrue(userProfilePage.isItemDisplayedInRecentActivity(adminReport.getTitle()),
                "Report: " + adminReport.getTitle() + " is not displayed in Recent Activity");

        assertTrue(userProfilePage.getRecentActivityItems() <= 10,
                "Number of items in Recent Activity has exceeded over 10");

        assertEquals(allVariables, userProfilePage.getAllUserVariables());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotAccessProfilePageOfDisabledUser() throws JSONException, ParseException, IOException {
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId());
        userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initProjectsAndUsersPage().openDeactivatedUserTab().clickUserProfileLinkFrom(testParams.getEditorUser());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void accessProfilePageOfDisabledUserFromMetric() throws ParseException, JSONException, IOException {
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId());
        userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initMetricPage().clickMetricOwner(avgAmountMetric.getTitle());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void accessProfilePageOfDisabledUserFromReport() throws ParseException, JSONException, IOException {
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId());
        userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initReportsPage().clickReportOwner(editorReport.getTitle());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            userManagementRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void accessProfileOfDisabledUserButEnabledInOtherProject() throws ParseException, JSONException, IOException {
        String sameProject = createNewEmptyProject(getProfile(DOMAIN), "Copy of " + projectTitle);
        UserManagementRestRequest userRestRequestSameProject = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), sameProject);
        userRestRequestSameProject.addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        userRestRequestSameProject.addUserToProject(testParams.getUser(), UserRoles.ADMIN);

        UserManagementRestRequest userRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId());
        userRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            assertEquals(initReportsPage().openReportOwnerProfilePageFrom(editorReport.getTitle())
                    .getUserInfo().getEmail(), testParams.getEditorUser());
            assertEquals(initMetricPage().openMetricOwnerProfilePage(avgAmountMetric.getTitle())
                    .getUserInfo().getEmail(), testParams.getEditorUser());
        } finally {
            userRestRequest.updateUserStatusInProject(testParams.getEditorUser(), UserStatus.ENABLED);
            deleteProject(getProfile(DOMAIN), sameProject);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideUserInfoWhenDeletingAccount() throws IOException{
        try{
            String userEditor = createAndAddUserToProject(UserRoles.EDITOR);

            logout();
            signInAtGreyPages(userEditor, testParams.getPassword());

            String metricName = "Hide User Amount";
            MetricPage metricPage = initMetricPage()
                .createAggregationMetric(MetricTypes.AVG, new CustomMetricUI()
                    .withName(metricName)
                    .withFacts(FACT_AMOUNT));

            metricPage.setVisibility(true, metricName);
            assertThat(metricPage.getDataPageRowsByTitle(metricName), hasItem("FirstName LastName"));

            logoutAndLoginAs(true, UserRoles.ADMIN);
            assertEquals(initMetricPage().openMetricOwnerProfilePage(metricName)
                .getUserInfo().getEmail(), userEditor);

            logout();
            signInAtUI(userEditor, testParams.getPassword());
            PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();

            if (personalInfoDialog.getEmail().equals(userEditor) &&
                !personalInfoDialog.getEmail().equals(testParams.getDomainUser())) {

                log.info("Deleted User Editor " + userEditor);
                initAccountPage().deleteAccount();
            }

            LoginFragment loginPage = LoginFragment.getInstance(browser);
            loginPage.login(testParams.getDomainUser(), testParams.getPassword(), true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

            List<String> metrics = initMetricPage().getDataPageRowsByTitle(metricName);
            assertEquals(metrics, asList(metricName, StringUtils.EMPTY));
        }finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
