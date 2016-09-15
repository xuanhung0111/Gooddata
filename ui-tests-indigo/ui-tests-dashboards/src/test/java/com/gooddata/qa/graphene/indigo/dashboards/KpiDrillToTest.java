package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteDashboardsUsingCascase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillToSelect;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.base.Predicate;

public class KpiDrillToTest extends GoodSalesAbstractDashboardTest {

    private static final KpiConfiguration kpiWithDrillTo = new KpiConfiguration.Builder()
        .metric(METRIC_LOST)
        .dataSet(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DASH_TAB_OUTLOOK)
        .build();

    private static final KpiConfiguration kpiWithoutDrillTo = new KpiConfiguration.Builder()
        .metric(METRIC_AMOUNT)
        .dataSet(DATE_CREATED)
        .build();

    private boolean isMobileRunning;

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasNoDrillTo() throws JSONException, IOException {
        startEditMode().addKpi(kpiWithoutDrillTo).saveEditModeWithWidgets();

        try {
            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .selectLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkNewlyAddedKpiHasNoDrillTo-beforeDashboardSaved", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
            assertEquals(waitForFragmentVisible(indigoDashboardsPage)
                    .getConfigurationPanel()
                    .getDrillToValue(), DrillToSelect.PLACEHOLDER);

            indigoDashboardsPage.cancelEditModeWithoutChange();

            takeScreenshot(browser, "checkNewlyAddedKpiHasNoDrillTo-afterDashboardSaved", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);

            initIndigoDashboardsPageWithWidgets()
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiDrillToInViewMode() throws JSONException, IOException {
        startEditMode().addKpi(kpiWithDrillTo).saveEditModeWithWidgets();

        try {
            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInViewMode-beforeKpiValueClick", getClass());

            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "checkKpiDrillToInViewMode-afterKpiValueClick", getClass());

            assertNotEquals(browser.getCurrentUrl(), currentUrl);
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiDrillToInEditMode() throws JSONException, IOException {
        startEditMode().addKpi(kpiWithDrillTo).saveEditModeWithWidgets();

        try {
            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .selectLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardNotSaved-beforeKpiValueClick", getClass());

            // switch to last tab, wait for dashboard page load and close that tab
            BrowserUtils.switchToLastTab(browser);
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardNotSaved-afterKpiValueClick", getClass());
            browser.close();

            BrowserUtils.switchToFirstTab(browser);

            waitForFragmentVisible(indigoDashboardsPage)
                .cancelEditModeWithoutChange()
                .switchToEditMode()
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-unselectedKpiValueClick", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);

            indigoDashboardsPage
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-beforeSelectedKpiValueClick", getClass());

            // switch to last tab, wait for dashboard page load and close that tab
            BrowserUtils.switchToLastTab(browser);
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "checkKpiDrillToInEditMode-dashboardSaved-afterSelectedKpiValueClick", getClass());
            browser.close();

            BrowserUtils.switchToFirstTab(browser);
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkDeleteKpiDrillTo() throws JSONException, IOException {
        startEditMode().addKpi(kpiWithDrillTo).saveEditModeWithWidgets();

        try {
            waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .selectLastWidget(Kpi.class);

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
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkUpdateKpiDrillTo() throws JSONException, IOException {
        startEditMode().addKpi(kpiWithDrillTo).saveEditModeWithWidgets();

        try {
            waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .selectLastWidget(Kpi.class);

            ConfigurationPanel cp = indigoDashboardsPage.getConfigurationPanel();
            assertEquals(cp.getDrillToValue(), DASH_TAB_OUTLOOK);

            cp.selectDrillToByName(DASH_TAB_WHATS_CHANGED);

            indigoDashboardsPage.saveEditModeWithWidgets()
                .getLastWidget(Kpi.class)
                .clickKpiValue();
            waitForDashboardPageLoaded(browser);

            assertFalse(dashboardsPage.getTabs().isTabSelected(0));
            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiDrillToToPersonalDashboardTab() throws JSONException, IOException {
        String personalDashboard = "Personal dashboard";
        String personalTab = "Personal tab";

        addNewTabInNewDashboard(personalDashboard, personalTab);

        try {
            startEditMode()
                    .addKpi(new KpiConfiguration.Builder().metric(METRIC_WON).dataSet(DATE_CREATED)
                            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).drillTo(personalTab).build())
                    .saveEditModeWithWidgets();

            try {
                logout();
                signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

                initDashboardsPage();
                assertThat(dashboardsPage.getDashboardsNames(), not(contains(personalDashboard)));

                initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

                assertEquals(indigoDashboardsPage.getConfigurationPanel().getDrillToValue(), "Unlisted dashboard");

                indigoDashboardsPage.cancelEditModeWithoutChange()
                    .getLastWidget(Kpi.class)
                    .clickKpiValue();
                waitForDashboardPageLoaded(browser);

                checkRedBar(browser);
                DashboardTabs tabs = dashboardsPage.getTabs();
                assertEquals(tabs.getTabLabel(tabs.getSelectedTabIndex()), personalTab);
            } finally {
                logout();
                signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

                deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
            }
        } finally {
            initDashboardsPage();
            dashboardsPage.selectDashboard(personalDashboard);
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void deleteDashboardAndTabAfterSettingKpiDrillTo() throws IOException, JSONException {
        String newDashboard = "New dashboard";
        String newTab = "New tab";

        addNewTabInNewDashboard(newDashboard, newTab);

        try {
            startEditMode()
                    .addKpi(new KpiConfiguration.Builder().metric(METRIC_AVG_AMOUNT).dataSet(DATE_CREATED)
                            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).drillTo(newTab).build())
                    .saveEditModeWithWidgets();

            try {
                initDashboardsPage();
                dashboardsPage.selectDashboard(newDashboard);
                dashboardsPage.editDashboard();
                dashboardsPage.getDashboardEditBar().tryToDeleteDashboard();

                Predicate<WebDriver> redBarAppear = browser -> browser.findElements(BY_RED_BAR).size() != 0;
                Graphene.waitGui().until(redBarAppear);

                assertEquals(browser.findElement(BY_RED_BAR).getText(), "Dashboard cannot be deleted because"
                        + " it is linked from a scheduled email or KPI dashboard. Remove all links and retry.");
                waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
                dashboardsPage.getDashboardEditBar().saveDashboard();

                DashboardsRestUtils.deleteDashboardTab(getRestApiClient(), getObjectUriFromUrl(browser.getCurrentUrl()),
                        newTab);

                initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

                assertEquals(indigoDashboardsPage.getConfigurationPanel().getDrillToValue(), "Unlisted dashboard");

                indigoDashboardsPage.cancelEditModeWithoutChange()
                    .getLastWidget(Kpi.class)
                    .clickKpiValue();
                waitForDashboardPageLoaded(browser);

                DashboardTabs tabs = dashboardsPage.getTabs();
                assertThat(tabs.getAllTabNames(), not(contains(newTab)));
            } finally {
                deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
            }
        } finally {
            initDashboardsPage();
            dashboardsPage.selectDashboard(newDashboard);
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"mobile"})
    public void checkKpiWithDrillToRedirects() throws JSONException, IOException {
        final Metric lostMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_LOST));
        final String kpiUri =
                createKpiUsingRest(new KpiMDConfiguration.Builder().title(lostMetric.getTitle())
                        .metric(lostMetric.getUri()).dateDataSet(getDateDatasetUri(DATE_CREATED))
                        .comparisonType(ComparisonType.NO_COMPARISON).comparisonDirection(ComparisonDirection.NONE)
                        .drillToDashboard(String.format("/gdc/md/%s/obj/916", testParams.getProjectId()))
                        .drillToDashboardTab("adzD7xEmdhTx") // Outlook tab
                        .build());
        addWidgetToWorkingDashboard(kpiUri);
        try {
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "checkKpiWithDrillToRedirects-beforeKpiValueClick", getClass());

            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            waitForDashboardPageLoaded(browser);

            takeScreenshot(browser, "checkKpiWithDrillToRedirects-afterKpiValueClick", getClass());

            assertNotEquals(browser.getCurrentUrl(), currentUrl);
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"mobile"})
    public void checkKpiWithoutDrillToDoesNotRedirect() throws JSONException, IOException {
        addWidgetToWorkingDashboard(createNumOfActivitiesKpi());

        try {
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "checkKpiWithoutDrillToDoesNotRedirect-beforeKpiValueClick", getClass());

            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .clickKpiValue();

            takeScreenshot(browser, "checkKpiWithoutDrillToDoesNotRedirect-afterKpiValueClick", getClass());

            checkNoNewBrowserTabOrWindowNorRedirected(currentUrl);
        } finally {
            deleteDashboardsUsingCascase(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        if (!isMobileRunning) createAndAddUserToProject(UserRoles.EDITOR);
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
        initDashboardsPage()
                .addNewDashboard(newDashboard)
                .addNewTab(newTab);
        checkRedBar(browser);
        dashboardsPage.saveDashboard();
    }
}
