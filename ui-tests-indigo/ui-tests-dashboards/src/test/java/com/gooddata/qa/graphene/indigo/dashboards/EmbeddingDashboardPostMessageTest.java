package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class EmbeddingDashboardPostMessageTest extends AbstractDashboardTest {
    private static final By FANCYBOX_OVERLAY_LOADED = By.className("fancybox-overlay-fixed");
    private static final By FANCYBOX_LOADED = By.className("fancybox-opened");

    private String dashboardOnlyUser;
    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
        dashboardOnlyUser = createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPostMessageForDashboardLoadEvent() throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        try {
            initEmbeddedIndigoDashboardPageByIframe()
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            takeScreenshot(browser, "Post-mesage-for-dashboard-load-event", getClass());
            assertTrue(getPostMessageEvent(PostMessageEvent.LOADING_STARTED).isPresent(), "Post message event not found");
            assertTrue(getPostMessageEvent(PostMessageEvent.LOADED).isPresent(), "Post message event not found");
            assertTrue(getPostMessageEvent(PostMessageEvent.RESIZED).isPresent(), "Post message event not found");

        } finally {
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPostMessageForCreateAndDeleteDashboard() throws JSONException, IOException {
        try {
            initEmbeddedIndigoDashboardPageByIframe()
                    .waitForDashboardLoad()
                    .addKpi(new KpiConfiguration.Builder()
                            .metric(METRIC_AMOUNT)
                            .dataSet(DATE_DATASET_CREATED)
                            .build())
                    .saveEditModeWithWidgets();

            Optional<JSONObject> dashboardCreated = getPostMessageEvent(PostMessageEvent.DASHBOARD_CREATED);

            takeScreenshot(browser, "Post-message-event-for-create-dashboard", getClass());
            assertTrue(dashboardCreated.isPresent(), "Post message event not found");
            assertEquals(dashboardCreated.get()
                    .getJSONObject("data")
                    .getString("project"),
                    testParams.getProjectId());

        } finally {
            getEmbeddedDashboard().switchToEditMode().deleteDashboard(true);

            Optional<JSONObject> dashboardDeleted = getPostMessageEvent(PostMessageEvent.DASHBOARD_DELETED);

            takeScreenshot(browser, "Post-message-event-for-delete-dashboard", getClass());
            assertTrue(dashboardDeleted.isPresent(), "Post message event not found");
            assertEquals(dashboardDeleted.get()
                    .getJSONObject("data")
                    .getString("project"),
                    testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPostMessageWithUserNotBelongToProject() throws JSONException, IOException {
        String user = createDynamicUserFrom(testParams.getUser());
        String password= testParams.getPassword();
        logout();
        signInAtUI(user,password);

        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        try {
            tryToInitEmbeddedIndigoDashboardPage();
            waitForElementVisible(FANCYBOX_OVERLAY_LOADED, browser);
            waitForElementVisible(FANCYBOX_LOADED, browser);

            Optional<JSONObject> noPermissions = getPostMessageEvent(PostMessageEvent.NO_PERMISSIONS);

            takeScreenshot(browser, "Post-message-with-user-not-belong-to-project", getClass());
            assertTrue(noPermissions.isPresent(), "Post message event not found");
            assertEquals(noPermissions.get()
                    .getJSONObject("data")
                    .getString("reason"),
                    "viewDenied");
        } finally {
            getMdService().removeObjByUri(dashboardUri);
            logoutAndLoginAs(UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPostMessageForUpdateDashboard() throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        try {
            initEmbeddedIndigoDashboardPageByIframe()
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading()
                    .switchToEditMode()
                    .addKpi(new KpiConfiguration.Builder()
                            .metric(METRIC_AMOUNT)
                            .dataSet(DATE_DATASET_CREATED)
                            .build())
                    .saveEditModeWithWidgets();

            Optional<JSONObject> dashboardUpdated = getPostMessageEvent(PostMessageEvent.DASHBOARD_UPDATED);

            takeScreenshot(browser, "Post-message-event-for-update-dashboard", getClass());
            assertTrue(dashboardUpdated.isPresent(), "Post message event not found");
            assertEquals(dashboardUpdated.get()
                    .getJSONObject("data")
                    .getString("project"),
                    testParams.getProjectId());

        } finally {
            getMdService().removeObjByUri(dashboardUri);
        }
    }

    @DataProvider(name = "editDeniedUserProvider")
    public Object[][] getEditDeniedUserProvider() {
        return new Object[][] {
            {UserRoles.VIEWER},
            {UserRoles.DASHBOARD_ONLY}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "editDeniedUserProvider")
    public void testPostMessageForEditDeniedUser(UserRoles role) throws JSONException, IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        logoutAndLoginAs(role);

        try {
            initEmbeddedIndigoDashboardPageByIframe()
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            Optional<JSONObject> noPermissions = getPostMessageEvent(PostMessageEvent.NO_PERMISSIONS);

            takeScreenshot(browser, "Post-message-event-for-edit-denied-user-" + role, getClass());
            assertTrue(noPermissions.isPresent(), "Post message event not found");
            assertEquals(noPermissions.get()
                    .getJSONObject("data")
                    .getString("reason"),
                    "editDenied");

        } finally {
            getMdService().removeObjByUri(dashboardUri);
            logoutAndLoginAs(UserRoles.ADMIN);
        }
    }

    private List<JSONObject> getPostMessageEvents() throws JSONException {
        browser.switchTo().defaultContent();
        String postMessageEventString = waitForElementVisible(By.id("demo"), browser).getText();

        return Stream.of(postMessageEventString.split(" PostMessageEvent : "))
                .filter(e -> e.startsWith("{"))
                .map(e -> {
                    try {
                        return new JSONObject(e);
                    } catch (JSONException ex) {
                        throw new RuntimeException("There has an error on creating JSON object");
                    }
                })
                .collect(toList());
    }

    private Optional<JSONObject> getPostMessageEvent(PostMessageEvent event) throws JSONException {
        return getPostMessageEvents()
                .stream()
                .filter(e -> {
                    try {
                        return e.getString("name").equals(event.getName());
                    } catch (JSONException ex) {
                        throw new RuntimeException("There has an error when get Json key");
                    }
                })
                .findFirst();
    }

    private void logoutAndLoginAs(UserRoles role) throws JSONException {
        if (role == UserRoles.DASHBOARD_ONLY) {
            logout();
            signInAtGreyPages(dashboardOnlyUser, testParams.getPassword());
        } else {
            logoutAndLoginAs(true, role);
        }
    }

    private IndigoDashboardsPage getEmbeddedDashboard() {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return IndigoDashboardsPage.getInstance(browser);
    }

    private enum PostMessageEvent {
        LOADING_STARTED("loadingStarted"),
        LOADED("loaded"),
        NO_PERMISSIONS("noPermissions"),
        RESIZED("resized"),
        DASHBOARD_CREATED("dashboardCreated"),
        DASHBOARD_UPDATED("dashboardUpdated"),
        DASHBOARD_DELETED("dashboardDeleted");

        private String name;

        PostMessageEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
