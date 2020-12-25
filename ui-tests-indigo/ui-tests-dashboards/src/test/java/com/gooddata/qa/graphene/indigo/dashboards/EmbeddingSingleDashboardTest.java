package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.Sleeper;

public class EmbeddingSingleDashboardTest extends AbstractDashboardTest {

    private static final String DASH_PIPELINE_ANALYSIS_URI = "/gdc/md/%s/obj/916";
    private static final String TAB_OUTLOOK_IDENTIFIER = "adzD7xEmdhTx";
    private static final String EMBEDDED_ERROR_MESSAGE = "Sorry, you do not have access to this page."
            + "\nAsk your administrator to grant you permissions.";

    private String dashboardOnlyUser;
    private IndigoRestRequest indigoRestRequest;
    private AttributeFiltersPanel filterPanel;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createAmountMetric();
        getMetricCreator().createLostMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    @DataProvider(name = "editPermissionProvider")
    public Object[][] getEditPermissionProvider() {
        return new Object[][] {
            {UserRoles.ADMIN, EmbeddedType.IFRAME},
            {UserRoles.ADMIN, EmbeddedType.URL},
            {UserRoles.EDITOR, EmbeddedType.IFRAME},
            {UserRoles.EDITOR, EmbeddedType.URL}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "editPermissionProvider")
    public void loginEmbeddedDashboardWithEditPermission(UserRoles role, EmbeddedType type)
            throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        if (role != UserRoles.ADMIN) {
            logoutAndLoginAs(role);
        }

        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            assertFalse(indigoDashboardsPage.isOnEditMode(), role + " should be directed to view mode but not");
            assertTrue(indigoDashboardsPage.isEditButtonVisible(), role + " cannot edit dashboard");

            indigoDashboardsPage
                    .switchToEditMode()
                    .addKpi(new KpiConfiguration.Builder()
                            .metric(METRIC_LOST)
                            .dataSet(DATE_DATASET_CREATED)
                            .build())
                    .saveEditModeWithWidgets();

            takeScreenshot(browser, role + "-can-edit-embedded-dashboard-by" + type, getClass());
            assertEquals(indigoDashboardsPage.getKpisCount(), 2);
            assertEquals(indigoDashboardsPage.getLastWidget(Kpi.class).getHeadline(), METRIC_LOST);

        } finally {
            getMdService().removeObjByUri(dashboardUri);

            if (role != UserRoles.ADMIN) {
                logoutAndLoginAs(UserRoles.ADMIN);
            }
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "editPermissionProvider")
    public void loginEmbeddedWithEmptyDashboardByEditPermission(UserRoles role, EmbeddedType type) throws JSONException {
        if (role != UserRoles.ADMIN) {
            logoutAndLoginAs(role);
        }

        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad();

            takeScreenshot(browser, role + "-is-directed-to-empty-dashboard-in-edit-mode-with-" + type, getClass());
            assertTrue(indigoDashboardsPage.isOnEditMode(), role + " cannot edit dashboard");

        } finally {
            if (role != UserRoles.ADMIN) {
                logoutAndLoginAs(UserRoles.ADMIN);
            }
        }
    }

    @DataProvider(name = "viewPermissionProvider")
    public Object[][] getViewPermissionProvider() {
        return new Object[][] {
            {UserRoles.VIEWER, EmbeddedType.IFRAME},
            {UserRoles.VIEWER, EmbeddedType.URL},
            {UserRoles.DASHBOARD_ONLY, EmbeddedType.IFRAME},
            {UserRoles.DASHBOARD_ONLY, EmbeddedType.URL}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProvider")
    public void loginEmbeddedDashboardWithViewPermission(UserRoles role, EmbeddedType type)
            throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
        logoutAndLoginAs(role);

        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            takeScreenshot(browser, role + "-cannot-edit-embedded-dashboard-with-" + type, getClass());
            assertFalse(indigoDashboardsPage.isOnEditMode(), role + "can edit embedded dashboard");
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), role + "can edit embedded dashboard");

        } finally {
            getMdService().removeObjByUri(dashboardUri);
            logoutAndLoginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProvider")
    public void loginEmbeddedWithEmptyDashboardByViewPermission(UserRoles role, EmbeddedType type)
            throws JSONException {
        logoutAndLoginAs(role);
        try {
            initEmbeddedIndigoDashboardPageByType(type);
            takeScreenshot(browser, "Error-shows-when-" + role + "-login-embedded-with-empty-dashboard-by-" + type, getClass());
            assertEquals(getEmbeddedErrorMessage(), EMBEDDED_ERROR_MESSAGE);
        } finally {
            logoutAndLoginAs(UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "embeddedTypeProvider")
    public Object[][] getEmbeddedTypeProvider() {
        return new Object[][] {
            {EmbeddedType.IFRAME},
            {EmbeddedType.URL}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "embeddedTypeProvider")
    public void loginEmbeddedDashboardWithUnauthorizedUser(EmbeddedType type)
            throws ParseException, JSONException, IOException {
        String unauthorizedUser = createDynamicUserFrom(testParams.getUser());
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        logout();
        signInAtGreyPages(unauthorizedUser, testParams.getPassword());

        try {
            initEmbeddedIndigoDashboardPageByType(type);

            takeScreenshot(browser, "Error-shows-when-login-with-un-authentication-user-by-" + type, getClass());
            assertEquals(getEmbeddedErrorMessage(), EMBEDDED_ERROR_MESSAGE);

        } finally {
            getMdService().removeObjByUri(dashboardUri);
            logoutAndLoginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "embeddedTypeProvider")
    public void checkHeaderNotDisplay(EmbeddedType type) {
        initEmbeddedIndigoDashboardPageByType(type).waitForDashboardLoad();

        takeScreenshot(browser, "Header-bar-not-display-in-embedded-dashboard-by" + type, getClass());
        assertFalse(Header.isVisible(browser), "Header is visible on embedded dashboard");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "embeddedTypeProvider")
    public void checkDrillToDashboard(EmbeddedType type) throws JSONException, IOException {
        final Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));
        final String drillKpiUri = createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(amountMetric.getTitle())
                .metric(amountMetric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .drillToDashboard(format(DASH_PIPELINE_ANALYSIS_URI, testParams.getProjectId()))
                .drillToDashboardTab(TAB_OUTLOOK_IDENTIFIER)
                .build());

        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(drillKpiUri));

        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            takeScreenshot(browser, "Can-drill-kpi-in-embedded-dashboard-with-" + type, getClass());
            assertTrue(indigoDashboardsPage.getLastWidget(Kpi.class).isDrillable(),
                    "Kpi is not drillable in embedded dashboard");

            indigoDashboardsPage.switchToEditMode().selectLastWidget(Kpi.class);
            takeScreenshot(browser, "Drill-To-select-visible-in-embedded-dashboard-with-" + type, getClass());
            assertTrue(indigoDashboardsPage
                    .getConfigurationPanel()
                    .isDrillToSelectVisible(),
                    "Drill To Select is not visible in embedded dashboard");
            takeScreenshot(browser, "Can-just-see-drill-kpi-in-embedded-dashboard-with-" + type, getClass());
            assertTrue(indigoDashboardsPage.getLastWidget(Kpi.class).isDrillableButNotClickable(),
                    "Kpi drill is not not visible or is clickable in embedded dashboard, edit mode");

        } finally {
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "embeddedTypeProvider")
    public void setAlertInEmbeddedDashboard(EmbeddedType type) throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        try {
            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "500");
            takeScreenshot(browser, "Alert-is-set-in-embedded-dashboard-with-" + type, getClass());
            assertTrue(indigoDashboardsPage.getLastWidget(Kpi.class).hasSetAlert(), "Kpi alert is not set");

            deleteAlertForLastKpi();
            takeScreenshot(browser, "Alert-is-deleted-in-embedded-dashboard-with-" + type, getClass());
            assertFalse(indigoDashboardsPage.getLastWidget(Kpi.class).hasSetAlert(), "Kpi alert is not deleted");

        } finally {
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    private void addMultipleFilters(Collection<String> filters) {
        filters.forEach(att -> indigoDashboardsPage.addAttributeFilter(att)
                .getAttributeFiltersPanel().getAttributeFilter(att).ensureDropdownClosed());
    }

    @Test(dependsOnMethods = {"setAlertInEmbeddedDashboard"}, dependsOnGroups = {"createProject"})
    public void createKpisDashboard() {
        Collection<String> filters = asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT);
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL);
        addMultipleFilters(filters);
    }

    @Test(dependsOnMethods = {"createKpisDashboard"}, dependsOnGroups = {"createProject"})
    public void changePositionExistingAttributeFilter() {
        filterPanel = indigoDashboardsPage.getAttributeFiltersPanel();
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_ACTIVITY));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getLastIndexWebElementAttributeFilter(),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_DEPARTMENT, ATTR_ACTIVITY, ATTR_ACCOUNT));
    }

    @Test(dependsOnMethods = {"changePositionExistingAttributeFilter"}, dependsOnGroups = {"createProject"})
    public void changePositionNewAttributeFilter() {
        indigoDashboardsPage.addAttributeFilter(ATTR_REGION, "East Coast");
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_DEPARTMENT, ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_REGION));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_REGION, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(2),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_REGION, ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getIndexWebElementAttributeFilter(2));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_REGION, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_REGION));
    }

    @Test(dependsOnMethods = {"changePositionNewAttributeFilter"}, dependsOnGroups = {"createProject"})
    public void changePositionOfDateFilter() {
        indigoDashboardsPage.dragDateAttributeToFilterPlaceholder();
        assertEquals(indigoDashboardsPage.getFirstAttributeFilter(), "Date range");
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT,ATTR_REGION));
    }

    @Test(dependsOnMethods = {"changePositionOfDateFilter"}, dependsOnGroups = {"createProject"}, expectedExceptions =
            {TimeoutException.class})
    public void changePositionAttributeOnTwoRows() {
        indigoDashboardsPage.addAttributeFilter(ATTR_STAGE_HISTORY).addAttributeFilter(ATTR_STATUS)
                .addAttributeFilter(ATTR_IS_CLOSED).addAttributeFilter(ATTR_OPP_SNAPSHOT)
                .addAttributeFilter(ATTR_IS_ACTIVE).addAttributeFilter(ATTR_IS_TASK)
                .addAttributeFilter(ATTR_FORECAST_CATEGORY).addAttributeFilter(ATTR_OPPORTUNITY)
                .addAttributeFilter(ATTR_PRIORITY).addAttributeFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting")
                .addAttributeFilter(ATTR_STAGE_NAME, "Risk Assessment")
                .clickFilterShowAllOnFilterBar()
                .addAttributeFilter(ATTR_SALES_REP, "Alejandro Vabiano")
                .addAttributeFilter(ATTR_IS_WON, "false")
                .addAttributeFilter(ATTR_PRODUCT, "Grammar Plus")
                .clickFilterShowAllOnFilterBar();

        List<String> expectedAttFilter = indigoDashboardsPage.getListCurrentAttributeFilter();
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
        List<String> currentAttFilter = indigoDashboardsPage.getListCurrentAttributeFilter();
        assertNotEquals(expectedAttFilter, currentAttFilter);
        indigoDashboardsPage.clickFilterShowLessOnFilterBar();
        sleepTightInSeconds(1);
        dragAndDropWithCustomBackend(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
    }

    @DataProvider(name = "embeddedUrlProvider")
    public Object[][] getEmbeddedUrlProvider() {
        return new Object[][] {
            {UserRoles.ADMIN},
            {UserRoles.EDITOR}
        };
    }

    @Test(dependsOnGroups = { "createProject" }, dataProvider = "embeddedUrlProvider")
    public void embeddedDashboardWithoutLoggedInByUrl(UserRoles userRole) throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
        IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL)
            .waitForDashboardLoad().waitForWidgetsLoading();
        String currentUrl = browser.getCurrentUrl();
        try {
            logout();
            LoginFragment.waitForPageLoaded(browser);
            openUrl(currentUrl);
            LoginFragment.waitForPageLoaded(browser);
            takeScreenshot(browser, "Show-login-page-when-embedded-dashboard-without-logged-in-by-url", getClass());
            signIn(false, userRole);
            indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
            takeScreenshot(browser, "Open-embedded-dashboard-after-logged-in-by-url", getClass());
            assertEquals(indigoDashboardsPage.getKpisCount(), 1);
            assertEquals(indigoDashboardsPage.getLastWidget(Kpi.class).getHeadline(), METRIC_AMOUNT);
        } 
        finally {
            if (userRole != UserRoles.ADMIN) {
                logoutAndLoginAs(UserRoles.ADMIN);
            }
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void embeddedDashboardWithoutLoggedInByIframe() throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
        initEmbeddedIndigoDashboardPageByIframe().waitForDashboardLoad().waitForWidgetsLoading();
        logout();
        try {
            LoginFragment.waitForPageLoaded(browser);
            initEmbeddedIndigoDashboardPageByIframe(false);
            takeScreenshot(browser, "Show-error-message-when-embedded-dashboard-without-logged-in-by-iframe", getClass());
            assertThat(LoginFragment.getInstance(browser).getContainerLoginForm(), containsString("Login to GoodData"));
        } 
        finally {
            signIn(true, UserRoles.ADMIN);
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
        dashboardOnlyUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
    }

    private void logoutAndLoginAs(UserRoles role) throws JSONException {
        if (role == UserRoles.DASHBOARD_ONLY) {
            logout();
            signInAtGreyPages(dashboardOnlyUser, testParams.getPassword());
        } else {
            logoutAndLoginAs(true, role);
        }
    }

    private String getEmbeddedErrorMessage() {
        waitForElementVisible(By.className("app-dashboards"), browser);
        return waitForElementPresent(By.className("embedded-error"), browser).getText();
    }
}
