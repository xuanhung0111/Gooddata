package com.gooddata.qa.graphene.indigo.analyze.postMessage;

import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageAnalysisPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.TABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForMainPageLoading;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.openqa.selenium.By.tagName;

public class ControlFilterEmbeddedADProgramTest extends AbstractEventingTest {

    private static final String FRAME_AD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_AD_post_message.html";
    private static final String FINANCIAL_VALUE = "101 Financial";
    private static final String WEST_VALUE = "14 West";
    private static final String WATS_TO_EXPLORER = "Western Wats > Explorer";
    private static final String WEST_TO_COMPUSCI = "14 West > CompuSci";
    private static final String ERROR_CODE = "error:invalidArgument";
    private static final String ERROR_CODE_RUNTIME = "error:runtime";
    private static final String ERROR_NO_FILTER = "There is no filter item on the filter bar to remove";
    private static final String INVALID_FILTER_FORMAT = "The command has invalid filter format";
    private static final String ERROR_DISPLAY_FORM = "The command has invalid attribute display form value";
    private static final String ERROR_NON_DISPLAY_FORM = "The command has non-default attribute display form.";
    private static final String FIRST_COMPUTED_ATTRIBUTE = "Computed Attribute 1";
    private static final String SECOND_COMPUTED_ATTRIBUTE = "Computed Attribute 2";
    private static final String ERROR_OVER_FILTER = "The app cannot be applied more than 20 items on filter bar";
    private static final String ERROR_ADD_DATE_EXIST = "There is different date in bucket where you are using date filter";
    private static final String INVALID_DATE_FORMAT = "The command has invalid filter format";
    private static final String INVALID_DATE_RANGE = "The command has date filter with invalid date range";
    private static final String INVALID_GRANULARITY = "The command has invalid date filter granularity";

    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS = "receive event after send commands";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI;
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_IDENTIFIER;
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI = "receive event after action ui";

    private String financialValueOfAccountAttribute;
    private String westValueOfAccountAttribute;
    private String explorerAttributeUri;
    private String accountIdentifier;
    private String opportunityIdentifier;
    private JSONObject content;
    private String computedAttrUri;
    private String computedAttrIdentifier;
    private String valueOfAccountAttributeUris;
    private EmbeddedAnalysisPage embeddedAnalysisPage;
    private PostMessageAnalysisPage postMessageApiPage;

    @BeforeClass(alwaysRun = true)
    @Override
    public void enableDynamicUser() {
        // always turn on dynamic user on this test or we have to fix the test logic to adapt when turn off that feature
        useDynamicUser = false;
    }

    protected void addUsersWithOtherRolesToProject(UserRoles userRoles) throws ParseException, JSONException, IOException {
        createAndAddUserToProject(userRoles);
    }

    protected void addEditorUsersToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
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

        financialValueOfAccountAttribute = getMdService().getAttributeElements(getAttributeByTitle(ATTR_ACCOUNT))
                .stream().filter(element -> FINANCIAL_VALUE.equals(element.getTitle())).findFirst().get().getUri();

        westValueOfAccountAttribute = getMdService().getAttributeElements(getAttributeByTitle(ATTR_ACCOUNT))
                .stream().filter(element -> WEST_VALUE.equals(element.getTitle())).findFirst().get().getUri();

        explorerAttributeUri = getMdService().getAttributeElements(getAttributeByTitle(ATTR_OPPORTUNITY))
                .stream().filter(element -> WATS_TO_EXPLORER.equals(element.getTitle())).findFirst().get().getUri();

        valueOfAccountAttributeUris = "[" + "\"" + financialValueOfAccountAttribute + "\"" + "," + " \""
                + westValueOfAccountAttribute + "\"" + "]";

        accountIdentifier = getAttributeByTitle(ATTR_ACCOUNT).getDefaultDisplayForm().getIdentifier();
        opportunityIdentifier = getAttributeByTitle(ATTR_OPPORTUNITY).getDefaultDisplayForm().getIdentifier();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        createComputedAttribute(ATTR_SALES_REP, METRIC_AMOUNT, FIRST_COMPUTED_ATTRIBUTE);
        createComputedAttribute(ATTR_SALES_REP, METRIC_AMOUNT, SECOND_COMPUTED_ATTRIBUTE);
        computedAttrUri = getMdService().getAttributeElements(getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE))
                .stream().filter(element -> "Large".equals(element.getTitle())).findFirst().get().getUri();
        computedAttrIdentifier = getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE).getDefaultDisplayForm().getIdentifier();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterWithSomeValidAttributes() throws IOException {
        log.info("Attribute filter (negative filter) with some values");
        final String negativeFilterHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris, accountIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(negativeFilterHtmlFile);
        verifyValidCommandPostMessage();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("All except 101 Financial, 14 West"));

        log.info("Attribute filter with All value");
        final String allFilterHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                "[]", accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(allFilterHtmlFile);
        verifyValidCommandPostMessage();
        assertEquals(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT)),
                asList(ATTR_ACCOUNT, "All"));

        log.info("Attribute filter (positive filter) with some values");
        final String positiveFilterHTmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(positiveFilterHTmlFile);
        verifyValidCommandPostMessage();
        assertEquals(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT)),
                asList(ATTR_ACCOUNT, "101 Financial, 14 West"));

        log.info("Attribute items out of 1000 values");
        JSONArray exploreAttrUris = new JSONArray() {{
            put(explorerAttributeUri);
        }};
        final String outOfValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), exploreAttrUris.toString(), opportunityIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(outOfValueHtmlFile);
        verifyValidCommandPostMessage();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_OPPORTUNITY),
                containsString("Western Wats > Explorer"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterWithSomeInvalidAttributes() throws IOException {
        log.info("Send post message has wrong element url");
        final String incorrectSyntaxHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                "\"[gdc/md]\"", opportunityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(incorrectSyntaxHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, INVALID_FILTER_FORMAT);

        log.info("Send post message has wrong text value");
        JSONArray wrongValueUris = new JSONArray() {{
            put(explorerAttributeUri + "999");
        }};
        final String wrongValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                wrongValueUris.toString(), opportunityIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(wrongValueHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_DISPLAY_FORM);

        log.info("Send post message has different displayForm");
        final String diffFormHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                valueOfAccountAttributeUris, "label.account.id", FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(diffFormHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_NON_DISPLAY_FORM);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyLimitFilters() throws IOException {
        final String positiveFilterHTmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        embeddedAnalysisPage = openEmbeddedPage(positiveFilterHTmlFile);
        embeddedAnalysisPage.waitForReportComputing();
        embeddedAnalysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES);
        embeddedAnalysisPage.addDateFilter().addFilter(ATTR_ACTIVITY).addFilter(ATTR_ACTIVITY_TYPE)
                .addFilter(ATTR_DEPARTMENT).addFilter(ATTR_FORECAST_CATEGORY).addFilter(ATTR_IS_ACTIVE)
                .addFilter(ATTR_IS_CLOSED).addFilter(ATTR_IS_TASK).addFilter(ATTR_IS_WON).addFilter(ATTR_OPP_SNAPSHOT)
                .addFilter(ATTR_OPPORTUNITY).addFilter(ATTR_PRIORITY).addFilter(ATTR_PRODUCT).addFilter(ATTR_REGION)
                .addFilter(ATTR_SALES_REP).addFilter(ATTR_STAGE_HISTORY).addFilter(ATTR_STAGE_NAME)
                .addFilter(ATTR_STATUS).addFilter(FIRST_COMPUTED_ATTRIBUTE).addFilter(SECOND_COMPUTED_ATTRIBUTE);
        cleanUpLogger();
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_OVER_FILTER);

        embeddedAnalysisPage.removeFilter(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyValidCommandPostMessage();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyComputedAttributeFilter() throws IOException {
        final String positiveFilterHTmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                "[\"" + computedAttrUri + "\"]", computedAttrIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        embeddedAnalysisPage = openEmbeddedPage(positiveFilterHTmlFile);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyValidCommandPostMessage();
        assertEquals(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getFilterText(FIRST_COMPUTED_ATTRIBUTE)),
                asList(FIRST_COMPUTED_ATTRIBUTE, "Large"));
    }

    @DataProvider(name = "verifyDateFilter")
    public Object[][] getDateFilter() {
        return new Object[][] {
                {"0", "0", "ALL_TIME_GRANULARITY", "relativeDateFilter", "All time"},
                {"-6", "0", "GDC.time.year", "relativeDateFilter", "Jan 1, 2014 - Dec 31, 2020"},
                {"\"2017-01-01\"", "\"2017-12-31\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", "Jan 1, 2017 - Dec 31, 2017"},
                {"\"2017-01-01\"", "\"2017-01-01\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", "Jan 1, 2017"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "verifyDateFilter")
    public void verifyDateFilter(String startTime, String endTime, String granularity, String dateFilterType, String expectedValue) throws IOException {
        final String allTimeHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), FRAME_AD_POST_MESSAGE_PATH_FILE,
                startTime, endTime, granularity, dateFilterType);
        sendPostMessageForDateFilter(allTimeHtmlFile);
        verifyValidCommandPostMessage();
        assertThat(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getDateFilterText()), hasItem(expectedValue));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyDuplicateDateFilter() throws IOException {
        log.info("Add more one date value");
        final String twoDateFilterHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), FRAME_AD_POST_MESSAGE_PATH_FILE,
                "0", "0", "ALL_TIME_GRANULARITY", "relativeDateFilter");
        embeddedAnalysisPage = openEmbeddedPage(twoDateFilterHtmlFile);
        embeddedAnalysisPage.waitForReportComputing().changeReportType(TABLE).addDateToColumnsAttribute().waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setDateFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_ADD_DATE_EXIST);
    }

    @DataProvider(name = "verifyDateDimensionFilter")
    public Object[][] getDateDimensionFilter() {
        return new Object[][] {
                {"\"2017/05/25\"", "\"2017/12/25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INVALID_DATE_FORMAT},
                {"\"2012-01-25\"", "\"2010-05-25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INVALID_DATE_RANGE},
                {"\"2012-01-25\"", "\"100000-05-25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INVALID_FILTER_FORMAT},
                {"-1", "-1", "abc", "relativeDateFilter", INVALID_GRANULARITY}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "verifyDateDimensionFilter")
    public void dateDimensionWithManyDateFormats(String startTime, String endTime, String granularity, String dateFilterType, String error) throws IOException {
        final String dateAbsoluteHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), FRAME_AD_POST_MESSAGE_PATH_FILE,
                startTime, endTime,  granularity, dateFilterType);
        sendPostMessageForDateFilter(dateAbsoluteHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, error);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void sendPostMessageWillAllItemsAttribute() throws IOException {
        final String allValueHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), "[]", opportunityIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        embeddedAnalysisPage = openEmbeddedPage(allValueHtmlFile);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyValidCommandPostMessage();
        FiltersBucket filtersBucket = embeddedAnalysisPage.getFilterBuckets();
        assertEquals(parseFilterText(filtersBucket.getFilterText(ATTR_OPPORTUNITY)), asList(ATTR_OPPORTUNITY, "All"));

        filtersBucket.configAttributeFilter(ATTR_OPPORTUNITY, WATS_TO_EXPLORER);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyValidCommandPostMessage();
        assertEquals(parseFilterText(filtersBucket.getFilterText(ATTR_OPPORTUNITY)), asList(ATTR_OPPORTUNITY, "All"));

        filtersBucket.configAttributeFilter(ATTR_OPPORTUNITY, WATS_TO_EXPLORER, WEST_TO_COMPUSCI);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        verifyValidCommandPostMessage();
        assertEquals(parseFilterText(filtersBucket.getFilterText(ATTR_OPPORTUNITY)), asList(ATTR_OPPORTUNITY, "All"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineAllFiltersAttribute() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(computedAttrUri);
        }};
        final String combineFilterHtmlFile = createTemplateHtmlFileCombineFilter(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), FRAME_AD_POST_MESSAGE_PATH_FILE,
                "0", "6", "absoluteDateFilter", valueOfAccountAttributeUris,
                accountIdentifier, uris.toString(), computedAttrIdentifier);
        loadingHtmlFile(combineFilterHtmlFile);
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setCombineFilter();
        embeddedAnalysisPage.waitForReportComputing();

        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));
        assertEquals(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getFilterText(FIRST_COMPUTED_ATTRIBUTE)),
                asList(FIRST_COMPUTED_ATTRIBUTE, "Large"));
        assertThat(parseFilterText(embeddedAnalysisPage.getFilterBuckets().getDateFilterText()),
                hasItem("Jan 1, 1970 - Jan 1, 1970"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void overrideFilter() throws IOException {
        final String filterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris, accountIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        loadingHtmlFile(filterHtmlFile);
        browser.switchTo().frame("iframe");
        embeddedAnalysisPage.addAttribute(ATTR_ACCOUNT);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("All"));
        cleanUpLogger();
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeFilter() throws IOException {
        final String removeFilterHtml = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris, accountIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        embeddedAnalysisPage = openEmbeddedPage(removeFilterHtml);
        embeddedAnalysisPage.waitForReportComputing().addFilter(ATTR_ACCOUNT).addDateFilter();
        cleanUpLogger();
        PostMessageAnalysisPage postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.removeFilter();
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getFilterBuckets().getFiltersCount(), 0);
        verifyValidCommandPostMessage();

        cleanUpLogger();
        postMessageApiPage.removeFilter();
        content = getLatestPostMessage("insightRendered");
        verifyErrorCommandPostMessage(ERROR_CODE_RUNTIME, ERROR_NO_FILTER);
    }

    @Test(dependsOnMethods = {"verifyLimitFilters"})
    public void filterAppliedExecuteWillBeKeptAsDefaultValue() throws IOException {
        final String positiveFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),valueOfAccountAttributeUris, accountIdentifier,
                FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(positiveFilterHTml);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        browser.navigate().refresh();
        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        waitForMainPageLoading(browser);
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getFilterBuckets().getFiltersCount(), 0);

        cleanUpLogger();
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
        postMessageApiPage.saveInsight();
        browser.navigate().refresh();
        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        waitForMainPageLoading(browser);
        embeddedAnalysisPage.waitForReportComputing();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));
        embeddedAnalysisPage.removeFilter(ATTR_ACCOUNT).setInsightTitle(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS)
                .saveInsight();
    }

    @Test(dependsOnMethods = {"filterAppliedExecuteWillBeKeptAsDefaultValue"})
    public void doSomeActionsOnEmbeddedAD() throws IOException {
        final String positiveFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(positiveFilterHTml);
        embeddedAnalysisPage.addAttribute(ATTR_ACCOUNT).addStack(ATTR_DEPARTMENT).waitForReportComputing();

        assertEquals(embeddedAnalysisPage.getMeasureAsColumnBucketBucket().getItemNames(),
                asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), ATTR_ACCOUNT);
        assertEquals(embeddedAnalysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        embeddedAnalysisPage.removeAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), "");
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        embeddedAnalysisPage.addAttribute(ATTR_PRIORITY).waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), ATTR_PRIORITY);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        embeddedAnalysisPage.changeReportType(ReportType.LINE_CHART);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        embeddedAnalysisPage.swapAttributeToStack(ATTR_PRIORITY, ATTR_DEPARTMENT).waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), ATTR_DEPARTMENT);
        assertEquals(embeddedAnalysisPage.getStacksBucket().getAttributeName(), ATTR_PRIORITY);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        browser.switchTo().defaultContent();
        postMessageApiPage.undo();
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), ATTR_PRIORITY);
        assertEquals(embeddedAnalysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        browser.switchTo().defaultContent();
        postMessageApiPage.redo();
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(embeddedAnalysisPage.getAttributesBucket().getAttributeName(), ATTR_DEPARTMENT);
        assertEquals(embeddedAnalysisPage.getStacksBucket().getAttributeName(), ATTR_PRIORITY);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        MetricConfiguration metricConfig = embeddedAnalysisPage.getMetricsBucket().
                getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_ACTIVITY_TYPE, "Email");
        embeddedAnalysisPage.waitForReportComputing();
        assertEquals(metricConfig.getFilterText(), "Activity Type: Email");

        metricConfig.removeFilter();
        assertEquals(metricConfig.getNumberOfAttrFilter(), 0);
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        embeddedAnalysisPage.getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        embeddedAnalysisPage.waitForReportComputing();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT),
                containsString("Direct Sales"));

        cleanUpLogger();
        postMessageApiPage.saveInsight();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));

        browser.switchTo().defaultContent();
        postMessageApiPage.saveAsInsight();
        assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                containsString("101 Financial, 14 West"));
        embeddedAnalysisPage.setInsightTitle(INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS).saveInsight();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void embeddedAppliedProtectedAttribute() throws IOException {
        AttributeRestRequest attributeRestRequest = new AttributeRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String attributeUri = attributeRestRequest.getAttributeByTitle(ATTR_ACCOUNT).getUri();

        try {
            attributeRestRequest.setAttributeProtected(attributeUri);
            final String positiveFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI),valueOfAccountAttributeUris, accountIdentifier,
                    FRAME_AD_POST_MESSAGE_PATH_FILE);
            sendPostMessage(positiveFilterHTml);
            assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                    containsString("101 Financial, 14 West"));
            addEditorUsersToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);
            sendPostMessage(positiveFilterHTml);
            assertThat(embeddedAnalysisPage.getMetricsBucket().getMetricName(), containsString(METRIC_NUMBER_OF_ACTIVITIES));
            assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                    containsString("101 Financial, 14 West"));
            assertEquals(embeddedAnalysisPage.getMainEditor().getCanvasMessage(), "YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" +
                    "Contact your administrator.");
            Screenshots.takeScreenshot(browser, "embeddedAppliedProtectedAttribute", getClass());
        } finally {
            attributeRestRequest.unsetAttributeProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "userProvider")
    public Object[][] getUserProvider() {
        return new Object[][] {
                {UserRoles.EDITOR},
                {UserRoles.EDITOR_AND_INVITATIONS},
                {UserRoles.EXPLORER},
                {UserRoles.EXPLORER_EMBEDDED}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "userProvider")
    public void postMessageWithAllUserRoles(UserRoles userRoles) throws IOException {
            try {
                final String positiveFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                        INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris,
                        accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
                addUsersWithOtherRolesToProject(userRoles);
                logoutAndLoginAs(true, userRoles);
                sendPostMessage(positiveFilterHTml);
                assertThat(embeddedAnalysisPage.getFilterBuckets().getFilterText(ATTR_ACCOUNT),
                        containsString("101 Financial, 14 West"));
            } finally {
                logoutAndLoginAs(true, UserRoles.ADMIN);
            }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportXlsxFileOnEmbeddedAd() throws IOException {
        final String attributeFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris,
                accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(attributeFilterHTml);
        embeddedAnalysisPage.exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
        exportXLSXDialog.uncheckOption(ExportXLSXDialog.OptionalExport.CELL_MERGED).uncheckOption(
                ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();
        final File xlsxFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS + "." + ExportFormat.EXCEL_XLSX.getName());
        waitForExporting(xlsxFile);
        log.info("Data XLSX after export: " + XlsxUtils.excelFileToRead(xlsxFile.getPath(), 0));
        assertThat(XlsxUtils.excelFileToRead(xlsxFile.getPath(), 0).toString(),
                containsString("# of Activities, 85.0"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportCsvFileOnEmbeddedAd() throws IOException {
        final String attributeFilterHTml = createTemplateHtmlFile(getObjectIdFromUri(
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI), valueOfAccountAttributeUris,
                accountIdentifier, FRAME_AD_POST_MESSAGE_PATH_FILE);
        sendPostMessage(attributeFilterHTml);
        embeddedAnalysisPage.exportTo(OptionalExportMenu.File.CSV);
        final File csvFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                + INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS + "." + ExportFormat.CSV.getName());
        waitForExporting(csvFile);
        log.info("Data CSV after export: " + CSVUtils.readCsvFile(csvFile));
        assertThat(CSVUtils.readCsvFile(csvFile).toString(), containsString("# of Activities, 85"));
    }

    public void loadingHtmlFile(String fileName) {
        embeddedAnalysisPage = openEmbeddedPage(fileName);
        embeddedAnalysisPage.waitForReportComputing();
        cleanUpLogger();
    }

    public void sendPostMessage(String fileName) {
        loadingHtmlFile(fileName);
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setFilter();
        embeddedAnalysisPage.waitForReportComputing();
    }

    public void sendPostMessageForDateFilter(String fileName) {
        loadingHtmlFile(fileName);
        postMessageApiPage = PostMessageAnalysisPage.getInstance(browser);
        postMessageApiPage.setDateFilter();
        embeddedAnalysisPage.waitForReportComputing();
    }

    public void verifyValidCommandPostMessage() {
        content = getLatestPostMessage("insightRendered");
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("title"),
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS);
        assertEquals(content.getJSONObject("data").getJSONObject("insight").getString("uri"),
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
    }

    public void verifyErrorCommandPostMessage(String errorCode, String errorMessage) {
        content = getLatestPostMessage("appCommandFailed");
        assertEquals(content.getJSONObject("data").getString("errorCode"), errorCode);
        assertEquals(content.getJSONObject("data").getString("errorMessage"), errorMessage);
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }

    private void createComputedAttribute(String attribute, String metric, String name) {
        initAttributePage().moveToCreateAttributePage().createComputedAttribute(
                new ComputedAttributeDefinition()
                        .withAttribute(attribute)
                        .withMetric(metric)
                        .withName(name));
        initAttributePage();
        String titleSelector = ".s-title-" + CssUtils.simplifyText(name);
        By computedAttributeItem = By.cssSelector(titleSelector + " a");
        waitForElementVisible(computedAttributeItem, browser).click();
    }
}
