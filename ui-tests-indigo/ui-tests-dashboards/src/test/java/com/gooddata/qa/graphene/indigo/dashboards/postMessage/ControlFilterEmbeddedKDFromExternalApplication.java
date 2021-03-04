package com.gooddata.qa.graphene.indigo.dashboards.postMessage;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageAnalysisPage;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageKPIDashboardPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ControlFilterEmbeddedKDFromExternalApplication extends AbstractDashboardEventingTest {

    private static final String FRAME_KD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_KD_post_message.html";

    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS = "receive event after send commands";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI = "receive event after action ui";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI;
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI_URI;

    private String activityUri;
    private String activityIdentifier;
    private ProjectRestRequest projectRestRequest;

    @BeforeClass(alwaysRun = true)
    @Override
    public void enableDynamicUser() {
        // always turn on dynamic user on this test or we have to fix the test logic to adapt when turn off that feature
        useDynamicUser = false;
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI =
            createInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS, COLUMN_CHART,
                asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());
        INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI_URI =
            createInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI, COLUMN_CHART,
                asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());

        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)),
            testParams.getProjectId());
        activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        activityIdentifier = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getIdentifier();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void sendCommandsEditDashboardToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.editDashboard();
        indigoDashboardsPage.waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("switchedToEdit");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(dashboardUri));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitle);
    }

    @Test(dependsOnMethods = {"sendCommandsEditDashboardToEmbeddedGDC"})
    public void sendCommandsExportToPDFToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        openEmbeddedPage(file);
        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.exportToPDF();

        verifyCommandCallbackAfterExportingDashboard(dashboardTitle, "154,271");
    }

    @Test(dependsOnMethods = {"sendCommandsExportToPDFToEmbeddedGDC"})
    public void sendCommandsAddFilterToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode();

        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.addFilter();
        indigoDashboardsPage.waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("filterAdded");
        assertFalse(Objects.isNull(content), "Do not receive GDC event filterAdded");
    }

    @Test(dependsOnMethods = {"sendCommandsAddFilterToEmbeddedGDC"})
    public void sendCommandsAddInsightToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode();

        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.addInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        indigoDashboardsPage.waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("widgetAdded");
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("widgetCategory"), "visualization");
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("uri"),
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("title"),
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS);

        indigoDashboardsPage.waitForSaveButtonEnabled().saveEditMode();
    }

    @Test(dependsOnMethods = {"sendCommandsAddFilterToEmbeddedGDC"})
    public void sendCommandsAddKpiToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode();

        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.addKpi();
        indigoDashboardsPage.waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("widgetAdded");
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("widgetCategory"), "kpi");
    }

    @Test(dependsOnMethods = {"sendCommandsAddKpiToEmbeddedGDC"})
    public void sendCommandsCancelDashboardToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode();

        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.cancelDashboard();
        indigoDashboardsPage.waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("switchedToView");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(dashboardUri));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitle);
    }

    @Test(dependsOnMethods = {"sendCommandsAddKpiToEmbeddedGDC"})
    public void sendCommandsSaveDashboardToEmbeddedGDC() throws IOException {
        logoutAndLoginAs(true, UserRoles.ADMIN);
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode().addInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS);

        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        String dashboardTitleSaved = generateAnalyticalDashboardName();
        postMessageApiPage.saveDashboard(dashboardTitleSaved);
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("dashboardSaved");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(indigoRestRequest.getAnalyticalDashboardUri(dashboardTitleSaved)));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitleSaved));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitleSaved);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEventAfterSwitchingToEditDashboard() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        cleanUpLogger();
        browser.switchTo().frame("iframe");
        indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading();

        JSONObject content = getLatestPostMessage("switchedToEdit");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(dashboardUri));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitle);
    }

    @Test(dependsOnMethods = {"receiveEventAfterSwitchingToEditDashboard"})
    public void receiveEventAfterExportingToPDF() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        cleanUpLogger();
        browser.switchTo().frame("iframe");
        indigoDashboardsPage.exportDashboardToPDF();

        verifyCommandCallbackAfterExportingDashboard(dashboardTitle, "154,271");
    }

    @Test(dependsOnMethods = {"receiveEventAfterExportingToPDF"})
    public void receiveEventAfterSavingDashboard() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        indigoDashboardsPage.switchToEditMode().addInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS);
        cleanUpLogger();
        browser.switchTo().frame("iframe");

        String dashboardTitleSaved = generateAnalyticalDashboardName();
        indigoDashboardsPage.changeDashboardTitle(dashboardTitleSaved).saveEditModeWithWidgets();

        JSONObject content = getLatestPostMessage("dashboardSaved");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(indigoRestRequest.getAnalyticalDashboardUri(dashboardTitleSaved)));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitleSaved));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitleSaved);
    }

    @Test(dependsOnMethods = {"receiveEventAfterSavingDashboard"})
    public void receiveEventAfterCancelingDashboard() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).switchToEditMode();
        cleanUpLogger();
        browser.switchTo().frame("iframe");
        indigoDashboardsPage.cancelEditModeWithoutChange();

        JSONObject content = getLatestPostMessage("switchedToView");
        assertEquals(content.getJSONObject("data").getString("project"), testParams.getProjectId());
        assertEquals(content.getJSONObject("data").getString("dashboardId"),
            getObjectIdFromUri(dashboardUri));
        assertEquals(content.getJSONObject("data").getString("dashboard"),
            indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle));
        assertEquals(content.getJSONObject("data").getString("title"), dashboardTitle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideTopBarInEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        String dashboardTitle = generateAnalyticalDashboardName();

        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI_URI);

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
            uris.toString(), activityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
        cleanUpLogger();

        PostMessageKPIDashboardPage postMessageKPIDashboardPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageKPIDashboardPage.getEmbeddedPage(
            format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[topBar,widgetsCatalogue]",
                testParams.getHost(), testParams.getProjectId(),
                indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertFalse(indigoDashboardsPage.isDashboardTitleVisible(), "The top bar of embedded KD should be hidden");
        assertFalse(IndigoInsightSelectionPanel.isPresent(browser),
            "The widget catalogue of embedded KD should be hidden");

        postMessageKPIDashboardPage.getEmbeddedPage(
            format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[topBar]",
                testParams.getHost(), testParams.getProjectId(),
                indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertFalse(indigoDashboardsPage.isDashboardTitleVisible(), "The top bar of embedded KD should be hidden");

        postMessageKPIDashboardPage.getEmbeddedPage(
            format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[widgetsCatalogue]",
                testParams.getHost(), testParams.getProjectId(),
                indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        indigoDashboardsPage.switchToEditMode();
        assertFalse(IndigoInsightSelectionPanel.isPresent(browser),
            "The widget catalogue of embedded KD should be hidden");

        postMessageKPIDashboardPage.getEmbeddedPage(
            format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s",
                testParams.getHost(), testParams.getProjectId(),
                indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        indigoDashboardsPage.switchToEditMode();
        assertTrue(indigoDashboardsPage.isDashboardTitleVisible(), "The top bar of embedded KD should be visible");
        assertTrue(IndigoInsightSelectionPanel.isPresent(browser),
            "The widget catalogue of embedded KD should be visible");

        postMessageKPIDashboardPage.getEmbeddedPage(
            format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[abc]",
                testParams.getHost(), testParams.getProjectId(),
                indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        indigoDashboardsPage.switchToEditMode();
        assertTrue(indigoDashboardsPage.isDashboardTitleVisible(), "The top bar of embedded KD should be visible");
        assertTrue(IndigoInsightSelectionPanel.isPresent(browser),
            "The widget catalogue of embedded KD should be visible");
    }

    private void verifyCommandCallbackAfterExportingDashboard(String dashboardTitle, String expectedResults) {
        JSONObject content = getLatestPostMessage("exportedToPdf");
        assertThat(content.getJSONObject("data")
            .getString("link"), containsString("/gdc/exporter/result/" + testParams.getProjectId()));

        List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));
        takeScreenshot(browser, dashboardTitle, getClass());
        log.info(dashboardTitle + contents.toString());

        assertThat(contents, hasItems(expectedResults));
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }
}
