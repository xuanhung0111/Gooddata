package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;

public class EmptyStateTest extends AbstractCsvUploaderTest {

    @Test(dependsOnGroups = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage().waitForHeaderVisible();
        DatasetsListPage.getInstance(browser).waitForAddDataButtonVisible();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage().waitForEmptyStateLoaded();
        takeScreenshot(browser, "empty-state", getClass());
        log.info("Empty state message: " + DatasetsListPage.getInstance(browser).getEmptyStateMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    private void logoutTest() throws JSONException {
        initDataUploadPage();
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).click();
        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);
        LoginFragment.waitForPageLoaded(browser);
        signIn(false, UserRoles.ADMIN);
    }
}
