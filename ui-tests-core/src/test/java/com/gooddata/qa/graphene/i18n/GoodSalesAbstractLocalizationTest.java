package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.io.IOException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public abstract class GoodSalesAbstractLocalizationTest extends GoodSalesAbstractTest {

    private String userUri;

    protected String embeddedUri;

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void createAndUsingTestUser() throws ParseException, JSONException, IOException {
        final String testUser = generateEmail(testParams.getUser());
        userUri = UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), testUser,
                testParams.getPassword());

        addUserToProject(testUser, UserRoles.ADMIN);

        logout();
        signInAtGreyPages(testUser, testParams.getPassword());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws JSONException {
        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        UserManagementRestUtils.deleteUserByUri(getRestApiClient(), userUri);
    }

    public EmbeddedDashboard initEmbeddedDashboard() {
        browser.get(embeddedUri);
        EmbeddedDashboard page = Graphene.createPageFragment(EmbeddedDashboard.class,
                waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        return page;
    }
}
