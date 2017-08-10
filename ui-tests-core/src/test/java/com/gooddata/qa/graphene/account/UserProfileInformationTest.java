package com.gooddata.qa.graphene.account;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getCurrentUserProfile;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
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
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.UserStatus;

public class UserProfileInformationTest extends GoodSalesAbstractTest {

    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private static final String ERROR_MESSAGE = "You are not allowed to see this user profile.";

    private PersonalInfo userInfo;
    private List<String> allVariables;
    private GoodData editorGoodData;
    private Report adminReport;
    private Report editorReport;
    private Metric avgAmountMetric;
    
    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-profile-information-test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"initialize"})
    public void initialize() {
        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        avgAmountMetric = createMetric(getEditorGoodData(), "Average of Amount", 
                format("SELECT AVG([%s])", getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT))),
                DEFAULT_METRIC_FORMAT);
        adminReport = createReportViaRest(getGoodDataClient(), GridReportDefinitionContent.create(
                "Report" + generateHashString(),
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm()
                        .getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(avgAmountMetric))));
        editorReport = createReportViaRest(getEditorGoodData(), GridReportDefinitionContent.create(
                "Report" + generateHashString(),
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm()
                        .getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(avgAmountMetric))));
    }

    @Test(dependsOnGroups = { "createProject" })
    public void changeUserLanguage() throws JSONException, IOException {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, true);

        try {
            initAccountPage().changeLanguage("Français");
            assertEquals(getCurrentUserProfile(getRestApiClient()).getString("language"), "fr-FR");
        } finally {
            initAccountPage().changeLanguage("English US");
            assertEquals(getCurrentUserProfile(getRestApiClient()).getString("language"), "en-US");
            setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, false);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void initGetUserInformation() {
        PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
        takeScreenshot(browser, "User info", getClass());
        userInfo = personalInfoDialog.getUserInfo();

        initVariablePage();
        allVariables = ObjectsTable.getInstance(id(ObjectTypes.VARIABLE.getObjectsTableID()), browser).getAllItems();
    }

    @Test(dependsOnMethods = { "initGetUserInformation", "initialize" })
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
        UserManagementRestUtils.updateUserStatusInProject(
                getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initProjectsAndUsersPage().openDeactivatedUserTab().clickUserProfileLinkFrom(testParams.getEditorUser());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            UserManagementRestUtils.updateUserStatusInProject(
                    getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnMethods = {"initialize"}) 
    public void accessProfilePageOfDisabledUserFromMetric() throws ParseException, JSONException, IOException {
        UserManagementRestUtils.updateUserStatusInProject(
                getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initMetricPage().clickMetricOwner(avgAmountMetric.getTitle());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            UserManagementRestUtils.updateUserStatusInProject(
                    getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnMethods = {"initialize"}) 
    public void accessProfilePageOfDisabledUserFromReport() throws ParseException, JSONException, IOException {
        UserManagementRestUtils.updateUserStatusInProject(
                getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            initReportsPage().clickReportOwner(editorReport.getTitle());
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getStatus(), Status.ERROR);
            assertEquals(statusBar.getMessage(), ERROR_MESSAGE);
        } finally {
            UserManagementRestUtils.updateUserStatusInProject(
                    getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getEditorUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnMethods = {"initialize"}) 
    public void accessProfileOfDisabledUserButEnabledInOtherProject() throws ParseException, JSONException, IOException {
        GoodData domainGoodData = getGoodDataClient(
                testParams.getDomainUser() == null ? testParams.getUser() : testParams.getDomainUser(),
                        testParams.getPassword());
        String sameProject = ProjectRestUtils.createProject(domainGoodData, "Copy of " + projectTitle,
                projectTemplate,testParams.getAuthorizationToken(), testParams.getProjectDriver(), 
                testParams.getProjectEnvironment());
        UserManagementRestUtils.addUserToProject(
                getDomainUserRestApiClient(), sameProject, testParams.getEditorUser(), UserRoles.EDITOR);
        UserManagementRestUtils.addUserToProject(
                getDomainUserRestApiClient(), sameProject, testParams.getUser(), UserRoles.ADMIN);

        UserManagementRestUtils.updateUserStatusInProject(
                getDomainUserRestApiClient(), testParams.getProjectId(),
                testParams.getEditorUser(), UserStatus.DISABLED);
        try {
            assertEquals(initReportsPage().openReportOwnerProfilePageFrom(editorReport.getTitle())
                    .getUserInfo().getEmail(), testParams.getEditorUser());
            assertEquals(initMetricPage().openMetricOwnerProfilePage(avgAmountMetric.getTitle())
                    .getUserInfo().getEmail(), testParams.getEditorUser());
        } finally {
            UserManagementRestUtils.updateUserStatusInProject(
                    getDomainUserRestApiClient(), testParams.getProjectId(),
                    testParams.getEditorUser(), UserStatus.ENABLED);
            ProjectRestUtils.deleteProject(domainGoodData, sameProject);
        }
    }

    private GoodData getEditorGoodData() {
        if (editorGoodData == null) {
            editorGoodData = getGoodDataClient(testParams.getEditorUser(), testParams.getPassword());
        }
        return editorGoodData;
    }
}
