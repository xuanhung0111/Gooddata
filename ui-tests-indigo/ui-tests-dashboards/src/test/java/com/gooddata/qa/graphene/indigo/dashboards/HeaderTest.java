package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;
import static org.testng.Assert.assertFalse;

public class HeaderTest extends DashboardsTest {

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkKpiLinkMissing() throws JSONException {
        try {
            disableFeatureFlagInProject(testParams.getProjectId(), ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS);
            initDashboardsPage();

            assertFalse(ApplicationHeaderBar.isKpisLinkVisible(browser));

        } finally {
            setupFeatureFlagInProject(testParams.getProjectId(), ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS);
        }
    }

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
