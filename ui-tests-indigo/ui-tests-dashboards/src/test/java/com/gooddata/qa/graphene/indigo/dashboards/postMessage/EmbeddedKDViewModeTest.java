package com.gooddata.qa.graphene.indigo.dashboards.postMessage;

import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageKPIDashboardPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
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
import java.util.List;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class EmbeddedKDViewModeTest extends AbstractDashboardEventingTest {

    private static final String FRAME_KD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_KD_post_message.html";
    private static final String ERROR_OVER_FILTER = "The app cannot be applied more than 20 attribute filters";
    private static final String INCORRECT_DATE_FORMAT = "The command has invalid attribute or date data format";
    private static final String INVALID_DATE_RANGE = "The command has date filter with invalid date range";
    private static final String INVALID_GRANULARITY = "The command has date filter granularity which is invalid or disabled";
    private static final String FIRST_COMPUTED_ATTRIBUTE = "Computed Attribute 1";
    private static final String SECOND_COMPUTED_ATTRIBUTE = "Computed Attribute 2";
    private static final String THIRD_COMPUTED_ATTRIBUTE = "Computed Attribute 3";
    private static final String ERROR_NOT_SUPPORTED = "Add new filter in view mode is not supported";

    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS = "receive event after send commands";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI = "receive event after action ui";
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI;
    private static String INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI_URI;
    private static String ERROR_DISPLAY_FORM = "The command has invalid attribute display form value";
    private static String FINANCIAL_VALUE = "101 Financial";
    private static String WEST_VALUE = "14 West";
    private static String WHEEL_BIKES_VALUE = "2 Wheel Bikes";
    private static String WATS_EXPLORER_VALUE = "Western Wats > Explorer";
    private static String ERROR_CODE = "error:invalidArgument";
    private static String ERROR_INVALID_FILTER = "The command has invalid attribute or date data format";
    private static String ERROR_NON_DISPLAY_FORM = "The command has non-default attribute display form.";

    private String accountIdentifier;
    private String financialValueOfAccountAttribute;
    private String westValueOfAccountAttribute;
    private String valueOfAccountAttributeUris;
    private String explorerAttributeUri;
    private JSONObject content;
    private String opportunityIdentifier;
    private String dashboardUri;
    private String computedAttrUri;
    private String computedAttrIdentifier;
    private ProjectRestRequest projectRestRequest;
    private String dashboardTitleTemplate;
    private PostMessageKPIDashboardPage postMessageApiPage;
    private DashboardRestRequest dashboardRequest;

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
        INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI_URI =
            createInsight(INSIGHT_TEST_RECEIVE_EVENT_AFTER_ACTION_UI, COLUMN_CHART,
                asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
            testParams.getProjectId());
        accountIdentifier = getAttributeByTitle(ATTR_ACCOUNT).getDefaultDisplayForm().getIdentifier();
        financialValueOfAccountAttribute = getMdService().getAttributeElements(getAttributeByTitle(ATTR_ACCOUNT))
                .stream().filter(element -> FINANCIAL_VALUE.equals(element.getTitle())).findFirst().get().getUri();
        westValueOfAccountAttribute = getMdService().getAttributeElements(getAttributeByTitle(ATTR_ACCOUNT))
                .stream().filter(element -> WEST_VALUE.equals(element.getTitle())).findFirst().get().getUri();
        valueOfAccountAttributeUris = "[" + "\"" + financialValueOfAccountAttribute + "\"" + "," + " \""
                + westValueOfAccountAttribute + "\"" + "]";

        opportunityIdentifier = getAttributeByTitle(ATTR_OPPORTUNITY).getDefaultDisplayForm().getIdentifier();
        explorerAttributeUri = getMdService().getAttributeElements(getAttributeByTitle(ATTR_OPPORTUNITY))
                .stream().filter(element -> WATS_EXPLORER_VALUE.equals(element.getTitle())).findFirst().get().getUri();

        dashboardTitleTemplate = generateAnalyticalDashboardName();
        dashboardUri = createAnalyticalDashboard(dashboardTitleTemplate,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        createComputedAttribute(ATTR_SALES_REP, METRIC_AMOUNT, FIRST_COMPUTED_ATTRIBUTE);
        createComputedAttribute(ATTR_SALES_REP, METRIC_AMOUNT, SECOND_COMPUTED_ATTRIBUTE);
        createComputedAttribute(ATTR_SALES_REP, METRIC_AMOUNT, THIRD_COMPUTED_ATTRIBUTE);

        computedAttrUri = getMdService().getAttributeElements(getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE))
                .stream().filter(element -> "Large".equals(element.getTitle())).findFirst().get().getUri();
        computedAttrIdentifier = getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE).getDefaultDisplayForm()
                .getIdentifier();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams
                .getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD,
                false);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXPLORE_INSIGHTS_FROM_KD,
                false);

        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition()
                .setDashboardName(" ").setTabName(" ").setProjectName(" ").setReportName(" ");
        dashboardRequest.exportDashboardSetting(exportDashboardDefinition);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hiddenHeadersInEmbeddedGDC() throws IOException {
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String filterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        indigoDashboardsPage = openEmbeddedPage(filterHtmlFile).waitForWidgetsLoading();
        cleanUpLogger();
        PostMessageKPIDashboardPage postMessageKPIDashboardPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageKPIDashboardPage.getEmbeddedPage(
                format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[filterBar]",
                        testParams.getHost(), testParams.getProjectId(),
                        indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertFalse(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardFilterVisible(),
                "The filter bar of embedded KD should be hidden");

        postMessageKPIDashboardPage.getEmbeddedPage(
                format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[filterBar,topBar,widgetsCatalogue]",
                        testParams.getHost(), testParams.getProjectId(),
                        indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertFalse(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardFilterVisible(),
                "The filter bar of embedded KD should be hidden");
        assertFalse(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardTitleVisible(),
                "The dashboard title of embedded KD should be hidden");
        cleanUpLogger();
        postMessageKPIDashboardPage.setFilter();
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_NOT_SUPPORTED);

        postMessageKPIDashboardPage.getEmbeddedPage(
                format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s??hideControl=[filterBar]&redirect-drill-url-to-message=all",
                        testParams.getHost(), testParams.getProjectId(),
                        indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertFalse(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardFilterVisible(),
                "The filter bar of embedded KD should be hidden");
        assertTrue(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardTitleVisible(),
                "The dashboard title of embedded KD should be shown");

        postMessageKPIDashboardPage.getEmbeddedPage(
                format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s",
                        testParams.getHost(), testParams.getProjectId(),
                        indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertTrue(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardFilterVisible(),
                "The filter bar of embedded KD should be visible");
        assertTrue(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardTitleVisible(),
                "The dashboard title of embedded KD should be shown");

        postMessageKPIDashboardPage.getEmbeddedPage(
                format("https://%s/dashboards/embedded/#/project/%s/dashboard/%s?hideControl=[abc]",
                        testParams.getHost(), testParams.getProjectId(),
                        indigoRestRequest.getAnalyticalDashboardIdentifier(dashboardTitle)));
        assertTrue(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardFilterVisible(),
                "The filter bar of embedded KD should be visible");
        assertTrue(indigoDashboardsPage.waitForDrillModalDialogLoading().isDashboardTitleVisible(),
                "The top bar of embedded KD should be visible");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyValidAttributeFilterOnKD() throws IOException {
        log.info("Attribute filter (positive filter) with some values");
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(positiveFilterHtmlFile, ATTR_ACCOUNT);
        assertThat(indigoDashboardsPage.waitForDashboardLoad().getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT).getSelection(), containsString("101 Financial, 14 West"));
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();

        log.info("Attribute items out of 1000 values");
        JSONArray exploreAttrUris = new JSONArray() {{
            put(explorerAttributeUri);
        }};
        final String outOfValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                exploreAttrUris.toString(), opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(outOfValueHtmlFile, ATTR_OPPORTUNITY);
        assertThat(indigoDashboardsPage.waitForDashboardLoad().getAttributeFiltersPanel().waitForAttributeFiltersLoaded()
                .getAttributeFilter(ATTR_OPPORTUNITY).getSelection(), containsString("Western Wats > Explorer"));
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_OPPORTUNITY).saveEditMode();

        log.info("Attribute filter with All value");
        final String allFilterHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(dashboardUri),
                "[]", accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        indigoDashboardsPage = openEmbeddedPage(allFilterHtmlFile).switchToEditMode()
                .addAttributeFilter(ATTR_ACCOUNT, WHEEL_BIKES_VALUE).saveEditMode();
        cleanUpLogger();
        postMessageApiPage.setFilter();
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("All"));
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyInvalidAttributeFilterOnKD() throws IOException {
        log.info("Send post message has wrong element url");
        final String incorrectSyntaxHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                "\"[gdc/md]\"", opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(incorrectSyntaxHtmlFile, ATTR_OPPORTUNITY);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_INVALID_FILTER);
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_OPPORTUNITY).saveEditMode();

        log.info("Send post message has wrong text value");
        JSONArray wrongValueUris = new JSONArray() {{
            put(explorerAttributeUri + "999");
        }};
        final String wrongValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                wrongValueUris.toString(), opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(wrongValueHtmlFile, ATTR_OPPORTUNITY);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_DISPLAY_FORM);
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_OPPORTUNITY).saveEditMode();

        log.info("Send post message has different displayForm");
        final String diffFormHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, "label.account.id", FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(diffFormHtmlFile, ATTR_ACCOUNT);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_NON_DISPLAY_FORM);
        indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
    }

    @Test(dependsOnMethods = {"verifyDuplicatedGranularity"})
    public void verifyLimitFilters() throws IOException {
        final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        indigoDashboardsPage = openEmbeddedPage(positiveFilterHtmlFile).waitForWidgetsLoading().switchToEditMode()
                .addAttributeFilter(ATTR_ACTIVITY).addAttributeFilter(ATTR_ACTIVITY_TYPE)
                .addAttributeFilter(ATTR_DEPARTMENT).addAttributeFilter(ATTR_FORECAST_CATEGORY)
                .addAttributeFilter(ATTR_IS_ACTIVE).addAttributeFilter(ATTR_IS_CLOSED).addAttributeFilter(ATTR_IS_TASK)
                .addAttributeFilter(ATTR_IS_WON).addAttributeFilter(ATTR_OPP_SNAPSHOT)
                .addAttributeFilter(ATTR_OPPORTUNITY).addAttributeFilter(ATTR_PRIORITY)
                .addAttributeFilter(ATTR_PRODUCT)
                .addAttributeFilter(ATTR_REGION).addAttributeFilter(ATTR_SALES_REP)
                .addAttributeFilter(ATTR_STAGE_HISTORY)
                .addAttributeFilter(ATTR_STAGE_NAME).addAttributeFilter(ATTR_STATUS)
                .addAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                .addAttributeFilter(SECOND_COMPUTED_ATTRIBUTE)
                .addAttributeFilter(THIRD_COMPUTED_ATTRIBUTE, "Large");
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_OVER_FILTER);
        indigoDashboardsPage.clickFilterShowAllOnFilterBar().waitForWidgetsLoading();
        indigoDashboardsPage.deleteAttributeFilter(THIRD_COMPUTED_ATTRIBUTE).waitForWidgetsLoading();
        cleanUpLogger();
        postMessageApiPage.setFilter();
        indigoDashboardsPage.clickFilterShowAllOnFilterBar().waitForWidgetsLoading();
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("101 Financial, 14 West"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyComputedAttributeFilter() throws IOException {
        try {
            String computedAttrUri = getMdService().getAttributeElements(getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE))
                    .stream().filter(element -> "Large".equals(element.getTitle())).findFirst().get().getUri();
            String computedAttrIdentifier = getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE).getDefaultDisplayForm()
                    .getIdentifier();
            final String computedAttributeHTmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    "[\"" + computedAttrUri + "\"]", computedAttrIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(computedAttributeHTmlFile, FIRST_COMPUTED_ATTRIBUTE);
            assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                    .getSelection(),"Large");
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(FIRST_COMPUTED_ATTRIBUTE).saveEditMode();
        }
    }

    @DataProvider(name = "verifyDateFilter")
    public Object[][] getDateFilter() {
        return new Object[][] {
                {"0", "0", "ALL_TIME_GRANULARITY", "relativeDateFilter", "All time"},
                {"-6", "0", "GDC.time.year", "relativeDateFilter", "Last 7 years"},
                {"\"2017-01-01\"", "\"2017-12-31\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", "01/01/2017–12/31/2017"},
                {"\"2017-01-01\"", "\"2017-01-01\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", "01/01/2017"},
                {"-3", "0", "GDC.time.date", "relativeDateFilter", "Last 4 days"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "verifyDateFilter")
    public void verifyDateFilter(String startTime, String endTime, String granularity, String dateFilterType,
                                 String expectedValue)
            throws IOException {
        final String allTimeHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE, startTime, endTime, granularity, dateFilterType);
        sendPostMessageForDateFilterInViewMode(allTimeHtmlFile);
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), expectedValue);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineAllFiltersAttribute() throws IOException {
        try {
            final JSONArray uris = new JSONArray() {{
                put(computedAttrUri);
            }};
            final String combineFilterHtmlFile = createTemplateHtmlFileCombineFilter(getObjectIdFromUri(dashboardUri),
                    FRAME_KD_POST_MESSAGE_PATH_FILE, "0", "0", "ALL_TIME_GRANULARITY",
                    valueOfAccountAttributeUris, accountIdentifier, uris.toString(), computedAttrIdentifier);
            indigoDashboardsPage = openEmbeddedPage(combineFilterHtmlFile).switchToEditMode()
                    .addAttributeFilter(ATTR_ACCOUNT).addAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                    .waitForWidgetsLoading().saveEditMode();
            cleanUpLogger();
            postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
            postMessageApiPage.setCombineFilter();
            indigoDashboardsPage.waitForWidgetsLoading();
            assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), "All time");
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                            .getSelection(), "Large");
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT)
                    .deleteAttributeFilter(FIRST_COMPUTED_ATTRIBUTE).saveEditMode();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void overrideFilter() throws IOException {
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String filterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            indigoDashboardsPage = openEmbeddedPage(filterHtmlFile).switchToEditMode()
                    .addAttributeFilter(ATTR_ACCOUNT, "2 Wheel Bikes").saveEditMode();
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("2 Wheel Bikes"));
            cleanUpLogger();
            postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
            postMessageApiPage.setFilter();
            indigoDashboardsPage.waitForWidgetsLoading();
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @DataProvider(name = "verifyDateDimensionFilter")
    public Object[][] getDateDimensionFilter() {
        return new Object[][] {
                {"\"2017/05/25\"", "\"2017/12/25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INCORRECT_DATE_FORMAT},
                {"\"2012-01-25\"", "\"2010-05-25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INVALID_DATE_RANGE},
                {"\"2012-01-25\"", "\"100000-05-25\"", "ALL_TIME_GRANULARITY", "absoluteDateFilter", INCORRECT_DATE_FORMAT},
                {"-1", "-1", "abc", "relativeDateFilter", INVALID_GRANULARITY}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "verifyDateDimensionFilter")
    public void dateDimensionWithManyDateFormats(String startTime, String endTime, String granularity,
                                                 String dateFilterType, String error) throws IOException {
        final String dateAbsoluteHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE,startTime, endTime,
                granularity, dateFilterType);
        sendPostMessageForDateFilterInViewMode(dateAbsoluteHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, error);
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), "All time");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyDuplicatedGranularity() throws IOException {
        log.info("Duplicated granularity param");
        final String duplicatedGranularityHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE,"-1", "-1", "abc",
                "relativeDateFilter");
        indigoDashboardsPage = openEmbeddedPage(duplicatedGranularityHtmlFile);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setDuplicatedDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        verifyErrorCommandPostMessage(ERROR_CODE, INVALID_GRANULARITY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void resetFilterWhenUserOutOfViewMode() throws IOException {
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            addFilterForDashboard(positiveFilterHtmlFile, ATTR_ACCOUNT);
            cleanUpLogger();
            postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
            postMessageApiPage.setFilter();
            sleepTightInSeconds(2);
            indigoDashboardsPage.getAttributeFiltersPanel().waitForAttributeFiltersLoaded();
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            indigoDashboardsPage.switchToEditMode();
            indigoDashboardsPage.getAttributeFiltersPanel().waitForAttributeFiltersLoaded();
            assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    "All");
        } finally {
            indigoDashboardsPage.deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void doSomeActionsAfterSendingPostMessage() throws IOException {
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile, ATTR_ACCOUNT);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            indigoDashboardsPage.switchToEditMode().resizeMaximumWidget().saveEditMode();
            assertThat(indigoDashboardsPage.getWidgetFluidLayout(dashboardTitle).getAttribute("class"),
                    containsString("s-fluid-layout-column-width-" + ResizeBullet.TWELVE.getNumber()));
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("All"));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void embeddedAppliedProtectedAttribute() throws IOException {
        AttributeRestRequest attributeRestRequest = new AttributeRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String attributeUri = attributeRestRequest.getAttributeByTitle(ATTR_ACCOUNT).getUri();

        try {
            attributeRestRequest.setAttributeProtected(attributeUri);
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile, ATTR_ACCOUNT);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
            addEditorUsersToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile, ATTR_ACCOUNT);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            assertThat(indigoDashboardsPage.getDashboardBodyText(), containsString("YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" +
                    "CONTACT YOUR ADMINISTRATOR."));
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
                {UserRoles.EXPLORER_EMBEDDED},
                {UserRoles.EXPLORER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "userProvider")
    public void checkPostMessageWorkingWithAllUsers(UserRoles userRoles) throws IOException {
        try {
            addUsersWithOtherRolesToProject(userRoles);
            logoutAndLoginAs(true, userRoles);
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile, ATTR_ACCOUNT);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportXlsxOnEmbeddedKd() throws IOException {
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String attributeFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(attributeFilterHtmlFile, ATTR_ACCOUNT);
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog exportXLSXDialog = ExportXLSXDialog.getInstance(browser);
            exportXLSXDialog.uncheckOption(ExportXLSXDialog.OptionalExport.CELL_MERGED).uncheckOption(
                    ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();
            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + dashboardTitle + "." + ExportFormat.EXCEL_XLSX.getName());
            waitForExporting(exportFile);
            log.info("Data XLSX after export: " + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
            assertThat(XlsxUtils.excelFileToRead(exportFile.getPath(), 0).toString(),
                    containsString("# of Activities, 85.0"));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportCsvOnEmbeddedKd() throws IOException {
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String attributeFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(attributeFilterHtmlFile, ATTR_ACCOUNT);
            indigoDashboardsPage.selectFirstWidget(Insight.class).exportTo(OptionalExportMenu.File.CSV);
            final File csvFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + dashboardTitle + "." + ExportFormat.CSV.getName());
            waitForExporting(csvFile);
            log.info("Data CSV after export: " + CSVUtils.readCsvFile(csvFile));
            assertThat(CSVUtils.readCsvFile(csvFile).toString(), containsString("# of Activities, 85"));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportPdfOnEmbeddedKd() throws IOException {
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String attributeFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(attributeFilterHtmlFile, ATTR_ACCOUNT);
        indigoDashboardsPage.exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));
        log.info("PDF: " + contents);
        assertThat(contents, hasItems(dashboardTitle));
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }

    public void sendPostMessageForDateFilterInViewMode(String fileName) {
        indigoDashboardsPage = openEmbeddedPage(fileName);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
    }

    public void verifyErrorCommandPostMessage(String errorCode, String errorMessage) {
        content = getLatestPostMessage("appCommandFailed");
        assertEquals(content.getJSONObject("data").getString("errorCode"), errorCode);
        assertEquals(content.getJSONObject("data").getString("errorMessage"), errorMessage);
    }

    public void addFilterForDashboard(String htmlFile, String attributeName) {
        indigoDashboardsPage = openEmbeddedPage(htmlFile).switchToEditMode().addAttributeFilter(attributeName)
                .saveEditMode();
    }

    public void sendPostMessageForAttributeFilter(String fileName, String attributeName) {
        addFilterForDashboard(fileName, attributeName);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
    }
}
