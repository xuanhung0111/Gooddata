package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SplashScreen;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.AMOUNT;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_CREATED;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DRILL_TO_OUTLOOK;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class SplashScreenTest extends DashboardsTest {

    private static final String SPLASH_SCREEN_MOBILE_MESSAGE = "To set up a KPI dashboard, head to your desktop and make your browser window wider.";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DRILL_TO_OUTLOOK)
        .build();

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkNewProjectWithoutKpisFallsToSplashCreen() {
        initIndigoDashboardsPage()
                .getSplashScreen();

        takeScreenshot(browser, "checkNewProjectWithoutKpisFallsToSplashCreen", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboard() {
        setupKpiFromSplashScreen(kpi);

        takeScreenshot(browser, "checkCreateNewKpiDashboard", getClass());

        teardownKpiWithDashboardDelete();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkEnterCreateNewKpiDashboardAndCancel() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        indigoDashboardsPage
                .addWidget(kpi)
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        takeScreenshot(browser, "checkEnterCreateNewKpiDashboardAndCancel", getClass());

        indigoDashboardsPage
                .getSplashScreen()
                .startEditingWidgets();

        assertEquals(indigoDashboardsPage.getKpisCount(), 0);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboardRemoveAndCreateAgain() {
        setupKpiFromSplashScreen(kpi);
        teardownKpiWithDashboardDelete();

        // do not use setupKpi here - it refreshes the page
        // this is a test case without page refresh
        indigoDashboardsPage
                .getSplashScreen()
                .startEditingWidgets();
        indigoDashboardsPage
                .addWidget(kpi)
                .saveEditModeWithKpis();

        takeScreenshot(browser, "checkCreateNewKpiDashboardRemoveAndCreateAgain", getClass());

        // do not use teardownKpi here - it refreshes the page
        // this is a test case without page refresh
        indigoDashboardsPage
                .switchToEditMode()
                .clickLastKpiDeleteButton()
                .waitForDialog()
                .submitClick();
        indigoDashboardsPage
                .saveEditModeWithoutKpis()
                .getSplashScreen();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"mobile"})
    public void checkCreateNewKpiDashboardNotAvailableOnMobile() {
        SplashScreen splashScreen = initIndigoDashboardsPage().getSplashScreen();
        String mobileMessage = splashScreen.getMobileMessage();

        assertEquals(mobileMessage, SPLASH_SCREEN_MOBILE_MESSAGE);
        splashScreen.waitForCreateKpiDashboardButtonMissing();

        takeScreenshot(browser, "checkCreateNewKpiDashboardNotAvailableOnMobile", getClass());
    }
}
