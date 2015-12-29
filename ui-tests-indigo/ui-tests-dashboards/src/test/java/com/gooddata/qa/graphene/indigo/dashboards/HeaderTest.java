package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;

public class HeaderTest extends DashboardsTest {

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkLogout() throws JSONException {
        try {
            initIndigoDashboardsPage()
                    .logout();

            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }
}
