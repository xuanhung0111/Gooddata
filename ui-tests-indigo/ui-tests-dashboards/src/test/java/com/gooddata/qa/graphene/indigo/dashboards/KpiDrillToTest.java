package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillToSelect;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class KpiDrillToTest extends DashboardWithWidgetsTest {

    private static final KpiConfiguration kpiWithDrillTo = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DRILL_TO_OUTLOOK)
        .build();

    private static final KpiConfiguration kpiWithoutDrillTo = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .build();

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasNoDrillTo() {
        setupKpi(kpiWithoutDrillTo);

        try {
            String currentUrl = browser.getCurrentUrl();

            indigoDashboardsPage
                .switchToEditMode()
                .selectLastKpi()
                .clickKpiValue();

            takeScreenshot(browser, "checkNewlyAddedKpiHasNoDrillTo-beforeDashboardSaved", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
            assertEquals(indigoDashboardsPage.getConfigurationPanel().getDrillToValue(), DrillToSelect.PLACEHOLDER);

            indigoDashboardsPage
                .cancelEditMode();

            takeScreenshot(browser, "checkNewlyAddedKpiHasNoDrillTo-afterDashboardSaved", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);

            initIndigoDashboardsPageWithWidgets()
                .getLastKpi()
                .clickKpiValue();

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiDrillToInViewMode() {
        setupKpi(kpiWithDrillTo);

        try {
            String currentUrl = browser.getCurrentUrl();

            indigoDashboardsPage
                .getLastKpi()
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInViewMode-beforeKpiValueClick", getClass());

            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "checkKpiDrillToInViewMode-afterKpiValueClick", getClass());

            assertNotEquals(browser.getCurrentUrl(), currentUrl);
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiDrillToInEditMode() {
        setupKpi(kpiWithDrillTo);

        try {
            String currentUrl = browser.getCurrentUrl();

            indigoDashboardsPage
                .switchToEditMode()
                .selectLastKpi()
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardNotSaved-beforeKpiValueClick", getClass());

            // switch to last tab, wait for dashboard page load and close that tab
            BrowserUtils.switchToLastTab(browser);
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardNotSaved-afterKpiValueClick", getClass());
            browser.close();

            BrowserUtils.switchToFirstTab(browser);

            indigoDashboardsPage
                .cancelEditMode()
                .switchToEditMode()
                .getLastKpi()
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-unselectedKpiValueClick", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);

            indigoDashboardsPage
                .getLastKpi()
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-beforeSelectedKpiValueClick", getClass());

            // switch to last tab, wait for dashboard page load and close that tab
            BrowserUtils.switchToLastTab(browser);
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-afterSelectedKpiValueClick", getClass());
            browser.close();

            BrowserUtils.switchToFirstTab(browser);
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDeleteKpiDrillTo() {
        setupKpi(kpiWithDrillTo);

        try {
            indigoDashboardsPage
                .switchToEditMode()
                .selectLastKpi();

            ConfigurationPanel cp = indigoDashboardsPage.getConfigurationPanel();

            takeScreenshot(browser, "checkDeleteKpiDrillTo-drillToSet", getClass());

            String drillToValue = cp.getDrillToValue();

            String drillToPlaceholder = cp
                .clickRemoveDrillToButton()
                .getDrillToValue();

            takeScreenshot(browser, "checkDeleteKpiDrillTo-drillToRemoved", getClass());

            assertNotEquals(drillToValue, drillToPlaceholder);
            assertEquals(drillToPlaceholder, DrillToSelect.PLACEHOLDER);
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkKpiWithDrillToRedirects() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkKpiWithDrillToRedirects-beforeKpiValueClick", getClass());

        String currentUrl = browser.getCurrentUrl();

        indigoDashboardsPage
            .getLastKpi()
            .clickKpiValue();

        waitForDashboardPageLoaded(browser);

        takeScreenshot(browser, "checkKpiWithDrillToRedirects-afterKpiValueClick", getClass());

        assertNotEquals(browser.getCurrentUrl(), currentUrl);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkKpiWithoutDrillToDoesNotRedirect() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkKpiWithoutDrillToDoesNotRedirect-beforeKpiValueClick", getClass());

        String currentUrl = browser.getCurrentUrl();

        indigoDashboardsPage
            .getFirstKpi()
            .clickKpiValue();

        takeScreenshot(browser, "checkKpiWithoutDrillToDoesNotRedirect-afterKpiValueClick", getClass());

        checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
    }

    private void checkNoNewBrowserTabOrWindowNorRedirected(String currentUrl) {
        assertTrue(browser.getWindowHandles().size() == 1);
        assertEquals(browser.getCurrentUrl(), currentUrl);
    }
}
