package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.annotations.Test;

public class EditModeTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkEditButtonPresent() {
        initIndigoDashboardsPage();

        takeScreenshot(browser, "checkEditButtonPresent", getClass());

        assertTrue(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkEditButtonMissing() {
        initIndigoDashboardsPage();

        takeScreenshot(browser, "checkEditButtonMissing", getClass());

        assertFalse(indigoDashboardsPage.isEditButtonVisible());
    }

}
