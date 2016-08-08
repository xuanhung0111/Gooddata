package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAccountPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.json.JSONException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.ChangePasswordDialog;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.account.RegionalNumberFormattingDialog;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;

public class UserAccountSettingTest extends AbstractUITest {

    private static final String PROJECT_NAME = "GoodSales";

    private static final String NEW_PASSWORD = "Gooddata12345";
    private static final String SHORT_PASSWORD = "aaaaa";
    private static final String WRONG_PASSWORD = "abcde12345";

    private static final String OLD_NUMBER_FORMAT = "1,234.12";
    private static final String NEW_NUMBER_FORMAT = "1.234,12";

    private static final String SUCCESS_MESSAGE = "Your %s%s was successfully changed.";
    private static final String NUMBER_FORMAT_SUCCESS_MESSAGE = "Your regional number formatting "
            + "settings were saved successfully.";

    private static final String SHORT_PASSWORD_ERROR_MESSAGE = "Password too short. "
            + "Minimum length is 7 characters.";

    private static final String FIELD_REQUIRED_ERROR_MESSAGE = "Field is required."
            + "\nPassword too short. Minimum length is 7 characters.";

    private static final String COMMONLY_PASSWORD_ERROR_MESSAGE = "You selected a commonly used password. "
            + "Choose something unique.\nSequential and repeated characters "
            + "are not allowed in passwords.";

    private static final String SEQUENTIAL_PASSWORD_ERROR_MESSAGE = "Sequential and repeated characters are "
            + "not allowed in passwords.";

    private static final String WRONG_PASSWORD_ERROR_MESSAGE = "You typed in wrong password.";

    private static final String PASSWORD_NOT_RELATED_ERROR_MESSAGE = "does not match related field";

    private static final String SHOULD_NOT_EMPTY = "Should not be empty";
    private static final String INVALID_PHONE_NUMBER = "This is not a valid phone number";

    private static final String NEW_INFO_STRING = String.valueOf(System.currentTimeMillis());

    private String oldPassword;
    private String user;

    private PersonalInfo personalInfo;
    private PersonalInfo personalInfoOrigin;

    @Test
    public void initLoginAndData() throws JSONException {
        signIn(false, UserRoles.ADMIN);

        user = testParams.getUser();
        oldPassword = testParams.getPassword();

        personalInfo = new PersonalInfo()
                .withEmail(testParams.getUser())
                .withFirstName("Firstname " + NEW_INFO_STRING)
                .withLastName("Lastname " + NEW_INFO_STRING)
                .withCompany("Company " + NEW_INFO_STRING)
                .withPhoneNumber(NEW_INFO_STRING);
    }

    @Test(dependsOnMethods = {"initLoginAndData"})
    public void openOneProject() {
        initProjectsPage();

        List<String> projectIds = waitForFragmentVisible(projectsPage).getProjectsIds(PROJECT_NAME);
        assertFalse(projectIds.isEmpty(), "Project Ids are empty");

        projectsPage.goToProject(projectIds.get(0));
        waitForDashboardPageLoaded(browser);

        testParams.setProjectId(projectIds.get(0));
    }

    @Test(dependsOnMethods = {"openOneProject"})
    public void getUserInformation() {
        initAccountPage();

        PersonalInfoDialog personalInfoDialog = accountPage.openPersonalInfoDialog();
        personalInfoOrigin = personalInfoDialog.getUserInfo();
    }

    @Test(dependsOnMethods = { "getUserInformation" })
    public void editUserInformation() {
        try {
            initAccountPage();

            PersonalInfoDialog personalInfoDialog = accountPage.openPersonalInfoDialog();
            assertFalse(personalInfoDialog.isEmailInputFieldEditable());

            personalInfoDialog.fillInfoFrom(personalInfo);
            assertEquals(personalInfoDialog.getUserInfo(), personalInfo);

            personalInfoDialog.discardChange();
            accountPage.openPersonalInfoDialog();
            assertEquals(personalInfoDialog.getUserInfo(), personalInfoOrigin);

            personalInfoDialog.fillInfoFrom(personalInfo)
                    .saveChange();
            checkGreenBar(browser, format(SUCCESS_MESSAGE, "account", " information"));

            refreshAccountPage();
            accountPage.openPersonalInfoDialog();
            assertEquals(personalInfoDialog.getUserInfo(), personalInfo);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to edit user information", this.getClass());
            throw e;

        } finally {
            initAccountPage();

            PersonalInfoDialog personalInformationDialog = accountPage.openPersonalInfoDialog();
            personalInformationDialog.fillInfoFrom(personalInfoOrigin)
                    .saveChange();
            checkGreenBar(browser, format(SUCCESS_MESSAGE, "account", " information"));
        }
    }

    @Test(dependsOnMethods = { "getUserInformation" })
    public void editUserInformationWithEmptyData() {
        initAccountPage();

        PersonalInfoDialog personalInfoDialog = accountPage.openPersonalInfoDialog();
        assertFalse(personalInfoDialog.isEmailInputFieldEditable());

        personalInfoDialog.fillInfoFrom(new PersonalInfo())
                .saveChange();
        assertThat(personalInfoDialog.getFirstNameErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getLastNameErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getCompanyErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getPhoneNumberErrorMessage(), equalTo(INVALID_PHONE_NUMBER));
    }

    @Test(dependsOnMethods = { "openOneProject" })
    public void editUserPassword() throws JSONException {
        try {
            initAccountPage();

            ChangePasswordDialog changePasswordDialog = accountPage.openChangePasswordDialog();
            changePasswordDialog.enterOldPassword(oldPassword).enterNewPassword(NEW_PASSWORD)
                    .enterConfirmPassword(NEW_PASSWORD);
            assertTrue(changePasswordDialog.areAllInputsFilled());

            changePasswordDialog.discardChange();
            logout()
                .login(user, NEW_PASSWORD, false);
            LoginFragment.getInstance(browser).checkInvalidLogin();

            LoginFragment.getInstance(browser).login(user, oldPassword, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

            changePassword(oldPassword, NEW_PASSWORD);

            logout()
                .login(user, oldPassword, false);
            LoginFragment.getInstance(browser).checkInvalidLogin();

            LoginFragment.getInstance(browser).login(user, NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to edit user password", this.getClass());
            throw e;

        } finally {
            changePassword(NEW_PASSWORD, oldPassword);
        }
    }

    @Test(dependsOnMethods = { "openOneProject" })
    public void editUserPasswordWithInvalidValue() {
        final SoftAssert softAssert = new SoftAssert();

        initAccountPage();
        ChangePasswordDialog changePasswordDialog = accountPage.openChangePasswordDialog();
        changePasswordDialog.enterOldPassword(SHORT_PASSWORD)
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.enterOldPassword(oldPassword)
                .enterNewPassword(SHORT_PASSWORD)
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword("", "");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), FIELD_REQUIRED_ERROR_MESSAGE);

        changePasswordDialog.changePassword(oldPassword, "12345678");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), COMMONLY_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword(oldPassword, "aaaaaaaa");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword(WRONG_PASSWORD, NEW_PASSWORD);
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), WRONG_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.enterOldPassword(oldPassword)
                .enterNewPassword(NEW_PASSWORD)
                .enterConfirmPassword("")
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), PASSWORD_NOT_RELATED_ERROR_MESSAGE);
        softAssert.assertAll();
    }

    @Test(dependsOnMethods = { "openOneProject" })
    public void editRegionalNumberFormat() {
        try {
            initAccountPage();

            RegionalNumberFormattingDialog numberFormattingDialog = accountPage
                    .openRegionalNumberFormattingDialog();
            numberFormattingDialog.selectNumberFormat(NEW_NUMBER_FORMAT);
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), NEW_NUMBER_FORMAT);

            numberFormattingDialog.discardChange();
            accountPage.openRegionalNumberFormattingDialog();
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), OLD_NUMBER_FORMAT);

            numberFormattingDialog.selectNumberFormat(NEW_NUMBER_FORMAT).saveChange();
            checkGreenBar(browser, NUMBER_FORMAT_SUCCESS_MESSAGE);

            refreshAccountPage();
            accountPage.openRegionalNumberFormattingDialog();
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), NEW_NUMBER_FORMAT);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to edit regional number format", this.getClass());
            throw e;

        } finally {
            initAccountPage();
            RegionalNumberFormattingDialog numberFormattingDialog = accountPage
                    .openRegionalNumberFormattingDialog();
            numberFormattingDialog.selectNumberFormat(OLD_NUMBER_FORMAT).saveChange();
            checkGreenBar(browser, NUMBER_FORMAT_SUCCESS_MESSAGE);
        }
    }

    @Test(dependsOnMethods = { "openOneProject" })
    public void goToActiveProjects() {
        initAccountPage();

        accountPage.openActiveProjectsPage();
        projectsPage.goToProject(testParams.getProjectId());
        waitForDashboardPageLoaded(browser);
        assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));
    }

    private void refreshAccountPage() {
        browser.navigate().refresh();
        waitForAccountPageLoaded(browser);
    }

    private void changePassword(String oldPassword, String newPassword) {
        initAccountPage();

        ChangePasswordDialog changePasswordDialog = accountPage.openChangePasswordDialog();
        changePasswordDialog.changePassword(oldPassword, newPassword);
        checkGreenBar(browser, format(SUCCESS_MESSAGE, "password", ""));
    }
}
