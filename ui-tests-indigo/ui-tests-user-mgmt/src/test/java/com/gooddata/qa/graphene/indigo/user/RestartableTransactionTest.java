package com.gooddata.qa.graphene.indigo.user;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertFalse;

import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.user.UserStates;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class RestartableTransactionTest extends AbstractUITest {

    @Test(groups = {PROJECT_INIT_GROUP, "precondition"})
    public void userLogin() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"userLogin"}, groups = {"precondition"})
    public void turnOnUserManagementFlag() {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT, true);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testRestartableTransaction() {
        initDashboardsPage();
        initUserManagementPage();

        for (int i = 0; i < 30; i++) {
            // Need sleep here to increase way the error can happen

            userManagementPage.filterUserState(UserStates.ACTIVE);
            sleepTightInSeconds(2);
            userManagementPage.selectAllUserEmails();
            sleepTightInSeconds(2);
            userManagementPage.deactivateUsers();
            checkError();

            sleepTightInSeconds(2);
            userManagementPage.filterUserState(UserStates.DEACTIVATED);
            sleepTightInSeconds(2);
            userManagementPage.selectAllUserEmails();
            sleepTightInSeconds(2);
            userManagementPage.activateUsers();
            checkError();
        }
    }

    private void checkError() {
        final WebElement e = waitForElementVisible(className("gd-message"), browser);
        assertFalse(e.getAttribute("class").contains("error"));
    }
}
