package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsGeneralTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;

public class EditModePermissionsTest extends DashboardsGeneralTest {

    @BeforeClass(alwaysRun = true)
    public void before() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkViewerCannotEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.VIEWER);

            initIndigoDashboardsPage()
                    .getSplashScreen()
                    .waitForCreateKpiDashboardButtonMissing();

        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkEditorCanEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initIndigoDashboardsPage()
                    .getSplashScreen()
                    .waitForCreateKpiDashboardButtonVisible();

        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            openUrl(getIndigoDashboardsPageUri());
            Graphene.waitGui().until((WebDriver driver) -> driver.getCurrentUrl().contains(ACCOUNT_PAGE));
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }
}
