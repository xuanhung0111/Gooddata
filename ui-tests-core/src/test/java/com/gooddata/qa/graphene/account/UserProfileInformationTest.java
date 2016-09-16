package com.gooddata.qa.graphene.account;

import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getCurrentUserProfile;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

public class UserProfileInformationTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Report";

    private PersonalInfo userInfo;
    private List<String> allVariables;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-profile-information-test";
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
    public void createReportTest() {
        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats("Amount")
                .withHows("Stage Name"),
                "Simple report");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void initGetUserInformation() {
        PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
        takeScreenshot(browser, "User info", getClass());
        userInfo = personalInfoDialog.getUserInfo();

        initVariablePage();
        allVariables = ObjectsTable.getInstance(id(ObjectTypes.VARIABLE.getObjectsTableID()), browser).getAllItems();
    }

    @Test(dependsOnMethods = { "initGetUserInformation" })
    public void checkUserProfileInformation() {
        PersonalInfo personalInfo = initReportsPage().getReportOwnerInfoFrom(REPORT_NAME);
        assertEquals(userInfo.getFullName(), personalInfo.getFullName());
        assertEquals(userInfo.getEmail(), personalInfo.getEmail());
        assertEquals(userInfo.getPhoneNumber(), personalInfo.getPhoneNumber());

        UserProfilePage userProfilePage = ReportsPage.getInstance(browser).openReportOwnerProfilePageFrom(REPORT_NAME);
        assertEquals(userProfilePage.getUserInfo(), userInfo);

        assertTrue(userProfilePage.isItemDisplayedInRecentActivity(REPORT_NAME),
                "Report: " + REPORT_NAME + " is not displayed in Recent Activity");

        assertTrue(userProfilePage.getRecentActivityItems() <= 10,
                "Number of items in Recent Activity has exceeded over 10");

        assertEquals(allVariables, userProfilePage.getAllUserVariables());
    }
}
