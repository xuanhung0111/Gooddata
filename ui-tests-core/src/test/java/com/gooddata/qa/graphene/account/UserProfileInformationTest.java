package com.gooddata.qa.graphene.account;

import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

public class UserProfileInformationTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Report";

    private PersonalInfo userInfo;
    private List<String> allVariables;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "User-profile-information-test";
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
        initReportsPage();

        PersonalInfo personalInfo = reportsPage.getReportOwnerInfoFrom(REPORT_NAME);
        assertEquals(userInfo.getFullName(), personalInfo.getFullName());
        assertEquals(userInfo.getEmail(), personalInfo.getEmail());
        assertEquals(userInfo.getPhoneNumber(), personalInfo.getPhoneNumber());

        UserProfilePage userProfilePage = reportsPage.openReportOwnerProfilePageFrom(REPORT_NAME);
        assertEquals(userProfilePage.getUserInfo(), userInfo);

        assertTrue(userProfilePage.isItemDisplayedInRecentActivity(REPORT_NAME),
                "Report: " + REPORT_NAME + " is not displayed in Recent Activity");

        assertTrue(userProfilePage.getRecentActivityItems() <= 10,
                "Number of items in Recent Activity has exceeded over 10");

        assertEquals(allVariables, userProfilePage.getAllUserVariables());
    }
}
