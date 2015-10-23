package com.gooddata.qa.graphene.indigo.dashboards;

import java.io.IOException;
import java.util.stream.Stream;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillToSelect;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.base.Predicate;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
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

    @BeforeClass(alwaysRun = true)
    public void before() {
        addUsersWithOtherRoles = true;
    }

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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkUpdateKpiDrillTo() {
        setupKpi(kpiWithDrillTo);

        try {
            indigoDashboardsPage
                .switchToEditMode()
                .selectLastKpi();

            ConfigurationPanel cp = indigoDashboardsPage.getConfigurationPanel();
            assertEquals(cp.getDrillToValue(), DRILL_TO_OUTLOOK);

            cp.selectDrillToByName(DRILL_TO_WHATS_CHANGED);
            indigoDashboardsPage.saveEditModeWithKpis()
                .getLastKpi()
                .clickKpiValue();
            waitForDashboardPageLoaded(browser);
            assertFalse(dashboardsPage.getTabs().isTabSelected(0));
            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiDrillToToPersonalDashboardTab() throws JSONException {
        String personalDashboard = "Personal dashboard";
        String personalTab = "Personal tab";

        addNewTabInNewDashboard(personalDashboard, personalTab);

        try {
            setupKpi(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .drillTo(personalTab)
                .build());

            try {
                logout();
                signIn(false, UserRoles.EDITOR);

                initDashboardsPage();
                assertThat(dashboardsPage.getDashboardsNames(), not(contains(personalDashboard)));

                initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

                assertEquals(indigoDashboardsPage.getConfigurationPanel().getDrillToValue(), "Unlisted dashboard");

                indigoDashboardsPage.cancelEditMode()
                    .getLastKpi()
                    .clickKpiValue();
                waitForDashboardPageLoaded(browser);

                checkRedBar(browser);
                DashboardTabs tabs = dashboardsPage.getTabs();
                assertEquals(tabs.getTabLabel(tabs.getSelectedTabIndex()), personalTab);
            } finally {
                logout();
                signIn(false, UserRoles.ADMIN);

                teardownKpi();
            }
        } finally {
            initDashboardsPage();
            dashboardsPage.selectDashboard(personalDashboard);
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void deleteDashboardAndTabAfterSettingKpiDrillTo() throws IOException, JSONException {
        String newDashboard = "New dashboard";
        String newTab = "New tab";

        addNewTabInNewDashboard(newDashboard, newTab);

        try {
            setupKpi(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .drillTo(newTab)
                .build());

            try {
                initDashboardsPage();
                assertTrue(dashboardsPage.selectDashboard(newDashboard));
                dashboardsPage.editDashboard();
                dashboardsPage.getDashboardEditBar().tryToDeleteDashboard();

                Predicate<WebDriver> redBarAppear = browser -> browser.findElements(BY_RED_BAR).size() != 0;
                Graphene.waitGui().until(redBarAppear);

                assertEquals(browser.findElement(BY_RED_BAR).getText(), "Dashboard cannot be deleted because"
                        + " it is linked from a scheduled email or KPI dashboard. Remove all links and retry.");
                waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
                dashboardsPage.getDashboardEditBar().saveDashboard();

                RestUtils.deleteDashboardTab(getRestApiClient(), getObjectUriFromUrl(browser.getCurrentUrl()),
                        newTab);

                initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

                assertEquals(indigoDashboardsPage.getConfigurationPanel().getDrillToValue(), "Unlisted dashboard");

                indigoDashboardsPage.cancelEditMode()
                    .getLastKpi()
                    .clickKpiValue();
                Graphene.waitGui().until(redBarAppear);

                assertEquals(browser.findElement(BY_RED_BAR).getText(),
                        "The dashboard tab you are looking for no longer exists.");
                waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
            } finally {
                teardownKpi();
            }
        } finally {
            initDashboardsPage();
            dashboardsPage.selectDashboard(newDashboard);
            dashboardsPage.deleteDashboard();
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

    private String getObjectUriFromUrl(String url) {
        return Stream.of(url.split("\\|"))
                .filter(part -> part.startsWith("/gdc/md"))
                .findFirst()
                .get();
    }

    private void addNewTabInNewDashboard(String newDashboard, String newTab) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(newDashboard);
        dashboardsPage.editDashboard();
        waitForDashboardPageLoaded(browser);
        dashboardsPage.addNewTab(newTab);
        checkRedBar(browser);
        dashboardsPage.getDashboardEditBar().saveDashboard();
    }
}
