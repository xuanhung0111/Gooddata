package com.gooddata.qa.graphene.indigo.analyze.postMessage;

import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageAnalysisPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.List;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.BAR_CHART;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ControlFilterEmbeddedADFromExternalApplication extends AbstractEventingTest {

    private static final String FRAME_AD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_AD_post_message.html";

    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS = "receive event after send commands";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI;
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_IDENTIFIER;

    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI = "receive event after action ui";

    private static String INSIGHT_SAVED = "test save";
    private static String INSIGHT_SAVED_AS = "test save as";

    private String activityUri;
    private String activityIdentifier;

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

        INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_IDENTIFIER = getObjIdentifiers(singletonList(
            indigoRestRequest.getInsightUri(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS))).get(0);

        createInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI, COLUMN_CHART,
            asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());

        activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        activityIdentifier = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getIdentifier();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void sendCommandsOpenInsightToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
            uris.toString(), activityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();

        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.openInsight();
        embeddedAnalysisPage.waitForReportComputing();

        JSONObject content = getLatestPostMessage("insightOpened");
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("title"),
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS);
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("uri"),
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("identifier"),
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_IDENTIFIER);

        assertEquals(embeddedAnalysisPage.getChartReport().getDataLabels(), singletonList("154,271"));
    }

    @Test(dependsOnMethods = {"sendCommandsOpenInsightToEmbeddedGDC"})
    public void sendCommandsSaveAndSaveAsToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), uris.toString(), activityIdentifier,
            FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.saveInsight();

        verifyCommandSaveInsight(INSIGHT_SAVED);

        cleanUpLogger();
        postMessageApiPage.saveAsInsight    ();
        verifyCommandSaveInsight(INSIGHT_SAVED_AS);
    }

    @Test(dependsOnMethods = {"sendCommandsSaveAndSaveAsToEmbeddedGDC"})
    public void sendCommandsExportToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(indigoRestRequest.getInsightUri(INSIGHT_SAVED)),
            uris.toString(), activityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.exportInsight();

        embeddedAnalysisPage.waitForReportComputing();

        verifyCommandCallbackAfterExportingInsight("test export",
            asList(asList("", METRIC_NUMBER_OF_ACTIVITIES), asList(METRIC_NUMBER_OF_ACTIVITIES, "154271.0")));
    }

    @Test(dependsOnMethods = {"sendCommandsExportToEmbeddedGDC"})
    public void sendCommands_Undo_Redo_Clear_ToEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(indigoRestRequest.getInsightUri(INSIGHT_SAVED)),
            uris.toString(), activityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.addMetric(METRIC_AMOUNT).waitForReportComputing();
        cleanUpLogger();

        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.undo();
        MetricsBucket metricsBucket = embeddedAnalysisPage.waitForReportComputing().getMetricsBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_NUMBER_OF_ACTIVITIES));

        JSONObject content = getLatestPostMessage("undoFinished");
        assertFalse(Objects.isNull(content), "Do not receive GDC event undoFinished");

        cleanUpLogger();
        postMessageApiPage.redo();
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT));

        content = getLatestPostMessage("redoFinished");
        assertFalse(Objects.isNull(content), "Do not receive GDC event redoFinished");

        cleanUpLogger();
        postMessageApiPage.clear();
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(metricsBucket.getItemNames(), ListUtils.EMPTY_LIST);

        content = getLatestPostMessage("clearFinished");
        assertFalse(Objects.isNull(content), "Do not receive GDC event clearFinished");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEventAfterSavingAndSavingAsInsight() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(indigoRestRequest.getInsightUri(
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI)), uris.toString(), activityIdentifier,
            FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        cleanUpLogger();
        browser.switchTo().frame("iframe");
        embeddedAnalysisPage.changeReportType(BAR_CHART).saveInsight().waitForReportComputing();

        verifyCommandSaveInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI);
    }

    @Test(dependsOnMethods = {"receiveEventAfterSavingAndSavingAsInsight"})
    public void receiveEventAfterExportingInsight() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(
            indigoRestRequest.getInsightUri(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI)),
            uris.toString(), activityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        cleanUpLogger();
        browser.switchTo().frame("iframe");
        embeddedAnalysisPage.waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog.getInstance(browser).checkOption(ExportXLSXDialog.OptionalExport.CELL_MERGED)
            .checkOption(ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();
        verifyCommandCallbackAfterExportingInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI,
            asList(asList("", METRIC_NUMBER_OF_ACTIVITIES), asList(METRIC_NUMBER_OF_ACTIVITIES, "154271.0")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hideTopBarInEmbeddedGDC() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(indigoRestRequest.getInsightUri(
            INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI)), uris.toString(), activityIdentifier,
            FRAME_AD_POST_MESSAGE_PATH_FILE);

        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        cleanUpLogger();

        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.getEmbeddedPage(
            format("https://%s/analyze/embedded/#/%s/reportId/edit?hideControl=[topBar]", testParams.getHost(), testParams.getProjectId()));

        assertFalse(embeddedAnalysisPage.isPageHeaderVisible(), "The top bar of embedded AD should be hidden");

        postMessageApiPage.getEmbeddedPage(
            format("https://%s/analyze/embedded/#/%s/reportId/edit?hideControl=[abc]", testParams.getHost(), testParams.getProjectId()));
        assertTrue(embeddedAnalysisPage.isPageHeaderVisible(), "The top bar of embedded AD should be visible");
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }

    private void verifyCommandSaveInsight(String insightTitle) {
        JSONObject content = getLatestPostMessage("visualizationSaved");
        String INSIGHT_URI = indigoRestRequest.getInsightUri(insightTitle);
        String INSIGHT_IDENTIFIER = getObjIdentifiers(singletonList(indigoRestRequest.getInsightUri(insightTitle))).get(0);

        assertEquals(content.getJSONObject("data").getJSONObject("visualizationObject").getJSONObject("meta")
            .getString("title"), insightTitle);
        assertEquals(content.getJSONObject("data").getJSONObject("visualizationObject").getJSONObject("meta")
            .getString("uri"), INSIGHT_URI);
        assertEquals(content.getJSONObject("data").getJSONObject("visualizationObject").getJSONObject("meta")
            .getString("identifier"), INSIGHT_IDENTIFIER);
    }

    private void verifyCommandCallbackAfterExportingInsight(
        String insightTitle, List<List<String>> expectedResults) throws IOException {
        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + insightTitle + "." + ExportFormat.EXCEL_XLSX.getName());
        waitForExporting(exportFile);

        JSONObject content = getLatestPostMessage("exportInsightFinished");
        assertThat(content.getJSONObject("data")
            .getString("link"), containsString("/gdc/exporter/result/" + testParams.getProjectId()));

        log.info(insightTitle + ":" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), expectedResults);
    }

    private JSONObject getLatestPostMessage(String name) {
        Function<WebDriver, Boolean> isLoggerDisplayed = browser -> getLoggerContent() != StringUtils.EMPTY;

        Graphene.waitGui()
            .pollingEvery(1, TimeUnit.SECONDS)
            .withTimeout(3, TimeUnit.MINUTES)
            .until(isLoggerDisplayed);

        String contentStr = getLoggerContent();
        log.info(contentStr);
        return Stream.of(contentStr.split("\n"))
            .map(JSONObject::new)
            .filter(jsonObject -> jsonObject.getString("name").equals(name))
            .findFirst()
            .orElse(null);
    }
}
