package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAccountPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.createBlankProject;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.account.AccountPage;
import com.gooddata.qa.graphene.fragments.account.ChangePasswordDialog;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.account.RegionalNumberFormattingDialog;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;

public class UserAccountSettingTest extends AbstractUITest {

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

    private PersonalInfo personalInfo;

    private String accountSettingUser;

    @Test
    public void prepareDataForTest() throws ParseException, JSONException, IOException {
        accountSettingUser = createDynamicUserFrom(testParams.getUser());

        GoodData goodDataClient = getGoodDataClient(accountSettingUser, testParams.getPassword());
        testParams.setProjectId(createBlankProject(goodDataClient, "Account-setting-test",
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment()));

        signInAtGreyPages(accountSettingUser, testParams.getPassword());

        String newInfoString = String.valueOf(System.currentTimeMillis());
        personalInfo = new PersonalInfo()
                .withEmail(accountSettingUser)
                .withFirstName("Firstname " + newInfoString)
                .withLastName("Lastname " + newInfoString)
                .withCompany("Company " + newInfoString)
                .withPhoneNumber(newInfoString);
    }

    @Test(dependsOnMethods = { "prepareDataForTest" })
    public void editUserInformation() {
        PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
        assertFalse(personalInfoDialog.isEmailInputFieldEditable());

        PersonalInfo personalInfoOrigin = personalInfoDialog.getUserInfo();
        personalInfoDialog.fillInfoFrom(personalInfo);
        assertEquals(personalInfoDialog.getUserInfo(), personalInfo);

        personalInfoDialog.discardChange();
        AccountPage.getInstance(browser).openPersonalInfoDialog();
        assertEquals(personalInfoDialog.getUserInfo(), personalInfoOrigin);

        personalInfoDialog.fillInfoFrom(personalInfo)
                .saveChange();
        checkGreenBar(browser, format(SUCCESS_MESSAGE, "account", " information"));

        refreshAccountPage()
            .openPersonalInfoDialog();
        assertEquals(personalInfoDialog.getUserInfo(), personalInfo);
    }

    @Test(dependsOnMethods = { "prepareDataForTest" })
    public void editUserInformationWithEmptyData() {
        PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
        assertFalse(personalInfoDialog.isEmailInputFieldEditable());

        personalInfoDialog.fillInfoFrom(new PersonalInfo())
                .saveChange();
        assertThat(personalInfoDialog.getFirstNameErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getLastNameErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getCompanyErrorMessage(), equalTo(SHOULD_NOT_EMPTY));
        assertThat(personalInfoDialog.getPhoneNumberErrorMessage(), equalTo(INVALID_PHONE_NUMBER));
    }

    @Test(dependsOnMethods = {"prepareDataForTest"})
    public void editUserPassword() throws JSONException {
        try {
            ChangePasswordDialog changePasswordDialog = initAccountPage().openChangePasswordDialog();
            changePasswordDialog.enterOldPassword(testParams.getPassword()).enterNewPassword(NEW_PASSWORD)
                    .enterConfirmPassword(NEW_PASSWORD);
            assertTrue(changePasswordDialog.areAllInputsFilled());

            changePasswordDialog.discardChange();
            logout()
                .login(accountSettingUser, NEW_PASSWORD, false);
            LoginFragment.getInstance(browser).checkInvalidLogin();

            LoginFragment.getInstance(browser).login(accountSettingUser, testParams.getPassword(), true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

            changePassword(testParams.getPassword(), NEW_PASSWORD);

            logout()
                .login(accountSettingUser, testParams.getPassword(), false);
            LoginFragment.getInstance(browser).checkInvalidLogin();

            LoginFragment.getInstance(browser).login(accountSettingUser, NEW_PASSWORD, true);
            waitForElementVisible(BY_LOGGED_USER_BUTTON, browser);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to edit user password", this.getClass());
            throw e;

        } finally {
            changePassword(NEW_PASSWORD, testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"prepareDataForTest"})
    public void editUserPasswordWithInvalidValue() {
        final SoftAssert softAssert = new SoftAssert();

        ChangePasswordDialog changePasswordDialog = initAccountPage().openChangePasswordDialog();
        changePasswordDialog.enterOldPassword(SHORT_PASSWORD)
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.enterOldPassword(testParams.getPassword())
                .enterNewPassword(SHORT_PASSWORD)
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SHORT_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword("", "");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), FIELD_REQUIRED_ERROR_MESSAGE);

        changePasswordDialog.changePassword(testParams.getPassword(), "12345678");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), COMMONLY_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword(testParams.getPassword(), "aaaaaaaa");
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), SEQUENTIAL_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.changePassword(WRONG_PASSWORD, NEW_PASSWORD);
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), WRONG_PASSWORD_ERROR_MESSAGE);

        changePasswordDialog.enterOldPassword(testParams.getPassword())
                .enterNewPassword(NEW_PASSWORD)
                .enterConfirmPassword("")
                .saveChange();
        softAssert.assertEquals(changePasswordDialog.getErrorMessage(), PASSWORD_NOT_RELATED_ERROR_MESSAGE);
        softAssert.assertAll();
    }

    @Test(dependsOnMethods = {"prepareDataForTest"})
    public void editRegionalNumberFormat() {
        try {
            RegionalNumberFormattingDialog numberFormattingDialog = initAccountPage()
                    .openRegionalNumberFormattingDialog();
            numberFormattingDialog.selectNumberFormat(NEW_NUMBER_FORMAT);
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), NEW_NUMBER_FORMAT);

            numberFormattingDialog.discardChange();
            AccountPage.getInstance(browser).openRegionalNumberFormattingDialog();
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), OLD_NUMBER_FORMAT);

            numberFormattingDialog.selectNumberFormat(NEW_NUMBER_FORMAT).saveChange();
            checkGreenBar(browser, NUMBER_FORMAT_SUCCESS_MESSAGE);

            refreshAccountPage()
                .openRegionalNumberFormattingDialog();
            assertEquals(numberFormattingDialog.getSelectedNumberFormat(), NEW_NUMBER_FORMAT);

        } catch(Exception e) {
            takeScreenshot(browser, "Fail to edit regional number format", this.getClass());
            throw e;

        } finally {
            RegionalNumberFormattingDialog numberFormattingDialog = initAccountPage()
                    .openRegionalNumberFormattingDialog();
            numberFormattingDialog.selectNumberFormat(OLD_NUMBER_FORMAT).saveChange();
            checkGreenBar(browser, NUMBER_FORMAT_SUCCESS_MESSAGE);
        }
    }

    @Test(dependsOnMethods = {"prepareDataForTest"})
    public void goToActiveProjects() {
        initAccountPage()
            .openActiveProjectsPage()
            .goToProject(testParams.getProjectId());
        waitForDashboardPageLoaded(browser);
        assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));
    }

    private AccountPage refreshAccountPage() {
        browser.navigate().refresh();
        waitForAccountPageLoaded(browser);
        return AccountPage.getInstance(browser);
    }

    private void changePassword(String oldPassword, String newPassword) {
        ChangePasswordDialog changePasswordDialog = initAccountPage().openChangePasswordDialog();
        changePasswordDialog.changePassword(oldPassword, newPassword);
        checkGreenBar(browser, format(SUCCESS_MESSAGE, "password", ""));
    }
}
