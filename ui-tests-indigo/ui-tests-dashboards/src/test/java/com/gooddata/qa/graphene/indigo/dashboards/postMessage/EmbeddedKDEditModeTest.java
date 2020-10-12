package com.gooddata.qa.graphene.indigo.dashboards.postMessage;

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
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EmbeddedKDEditModeTest extends AbstractDashboardEventingTest {

    private static final String FRAME_KD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_KD_post_message.html";
    private static final String INCORRECT_DATE_FORMAT = "The command has invalid attribute or date data format";
    private static final String INVALID_DATE_RANGE = "The command has date filter with invalid date range";
    private static final String INVALID_GRANULARITY = "The command has date filter granularity which is invalid or disabled";
    private static final String FIRST_COMPUTED_ATTRIBUTE = "Computed Attribute 1";
    private static final String SECOND_COMPUTED_ATTRIBUTE = "Computed Attribute 2";
    private static final String THIRD_COMPUTED_ATTRIBUTE = "Computed Attribute 3";

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
    public void verifyValidAttributeFilterOnKD() throws IOException {
        log.info("Attribute filter (positive filter) with some values");
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(positiveFilterHtmlFile);
        assertThat(indigoDashboardsPage.waitForDashboardLoad().getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT).getSelection(), containsString("101 Financial, 14 West"));
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        indigoDashboardsPage.deleteAttributeFilter(ATTR_ACCOUNT);

        log.info("Attribute items out of 1000 values");
        JSONArray exploreAttrUris = new JSONArray() {{
            put(explorerAttributeUri);
        }};
        final String outOfValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                exploreAttrUris.toString(), opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(outOfValueHtmlFile);
        assertThat(indigoDashboardsPage.waitForDashboardLoad().getAttributeFiltersPanel().waitForAttributeFiltersLoaded()
                .getAttributeFilter(ATTR_OPPORTUNITY).getSelection(), containsString("Western Wats > Explorer"));
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        indigoDashboardsPage.deleteAttributeFilter(ATTR_OPPORTUNITY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyInvalidAttributeFilterOnKD() throws IOException {
        log.info("Send post message has wrong element url");
        final String incorrectSyntaxHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                "\"[gdc/md]\"", opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(incorrectSyntaxHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_INVALID_FILTER);

        log.info("Send post message has wrong text value");
        JSONArray wrongValueUris = new JSONArray() {{
            put(explorerAttributeUri + "999");
        }};
        final String wrongValueHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                wrongValueUris.toString(), opportunityIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(wrongValueHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_DISPLAY_FORM);

        log.info("Send post message has different displayForm");
        final String diffFormHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, "label.account.id", FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(diffFormHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, ERROR_NON_DISPLAY_FORM);

        log.info("Attribute filter with All value");
        final String allFilterHtmlFile = createTemplateHtmlFileWithNegativeFilter(getObjectIdFromUri(dashboardUri),
                "[]", accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(allFilterHtmlFile);
        assertFalse(getLatestPostMessage("setFilterContextFinished").isNull("data"),
                "Message should have object included data");
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("All"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyComputedAttributeFilter() throws IOException {
        String computedAttrUri = getMdService().getAttributeElements(getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE))
                .stream().filter(element -> "Large".equals(element.getTitle())).findFirst().get().getUri();
        String computedAttrIdentifier = getAttributeByTitle(FIRST_COMPUTED_ATTRIBUTE).getDefaultDisplayForm()
                .getIdentifier();
        final String computedAttributeHTmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                "[\"" + computedAttrUri + "\"]", computedAttrIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(computedAttributeHTmlFile);
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                .getSelection(),"Large");
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
                                 String expectedValue) throws IOException {
        final String allTimeHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE, startTime, endTime, granularity, dateFilterType);
        sendPostMessageForDateFilterInEditMode(allTimeHtmlFile);
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), expectedValue);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void combineAllFiltersAttribute() throws IOException {
        final JSONArray uris = new JSONArray() {{
            put(computedAttrUri);
        }};
        final String combineFilterHtmlFile = createTemplateHtmlFileCombineFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE, "0", "0", "ALL_TIME_GRANULARITY",
                valueOfAccountAttributeUris, accountIdentifier, uris.toString(), computedAttrIdentifier);
        initIndigoDashboardWithEditMode(combineFilterHtmlFile);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setCombineFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), "All time");
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("101 Financial, 14 West"));
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(FIRST_COMPUTED_ATTRIBUTE)
                .getSelection(), "Large");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void overrideFilter() throws IOException {
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String filterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        indigoDashboardsPage = openEmbeddedPage(filterHtmlFile).switchToEditMode()
                .addAttributeFilter(ATTR_ACCOUNT, "2 Wheel Bikes");
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("2 Wheel Bikes"));
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("101 Financial, 14 West"));
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
        sendPostMessageForDateFilterInEditMode(dateAbsoluteHtmlFile);
        verifyErrorCommandPostMessage(ERROR_CODE, error);
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), "All time");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyDuplicatedGranularity() throws IOException {
        log.info("Duplicated granularity param");
        final String duplicatedGranularityHtmlFile = createTemplateHtmlFileWithDateFilter(getObjectIdFromUri(dashboardUri),
                FRAME_KD_POST_MESSAGE_PATH_FILE,"-1", "-1", "abc",
                "relativeDateFilter");
        initIndigoDashboardWithEditMode(duplicatedGranularityHtmlFile);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setDuplicatedDateFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
        verifyErrorCommandPostMessage(ERROR_CODE, INVALID_GRANULARITY);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void doSomeActionsAfterSendingPostMessage() throws IOException {
        String dashboardTitle = generateAnalyticalDashboardName();
        final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
        final String positiveFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
        sendPostMessageForAttributeFilter(positiveFilterHtmlFile);
        assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                containsString("101 Financial, 14 West"));
        indigoDashboardsPage.resizeMaximumWidget().saveEditMode();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(dashboardTitle).getAttribute("class"),
                containsString("s-fluid-layout-column-width-" + ResizeBullet.TWELVE.getNumber()));
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
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            indigoDashboardsPage.deleteAttributeFilter(ATTR_ACCOUNT);
            addEditorUsersToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
            assertThat(indigoDashboardsPage.getDashboardBodyText(),
                    containsString("YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" + "Contact your administrator."));
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
            sendPostMessageForAttributeFilter(positiveFilterHtmlFile);
            assertThat(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).getSelection(),
                    containsString("101 Financial, 14 West"));
        } finally {
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
            sendPostMessageForAttributeFilter(attributeFilterHtmlFile);
            indigoDashboardsPage.saveEditModeWithWidgets();
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
            sendPostMessageForAttributeFilter(attributeFilterHtmlFile);
            indigoDashboardsPage.saveEditModeWithWidgets();
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
        try {
            String dashboardTitle = generateAnalyticalDashboardName();
            final String dashboardUri = createAnalyticalDashboard(dashboardTitle,
                    INSIGHT_TEST_RECEIVE_EVENT_AFTER_SEND_COMMANDS_URI);
            final String attributeFilterHtmlFile = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri),
                    valueOfAccountAttributeUris, accountIdentifier, FRAME_KD_POST_MESSAGE_PATH_FILE);
            sendPostMessageForAttributeFilter(attributeFilterHtmlFile);
            indigoDashboardsPage.saveEditModeWithWidgets();
            indigoDashboardsPage.exportDashboardToPDF();
            List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));
            log.info("PDF: " + contents);
            assertThat(contents, hasItems(dashboardTitle));
        } finally {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT).saveEditMode();
        }
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }

    public void sendPostMessageForDateFilterInEditMode(String fileName) {
        initIndigoDashboardWithEditMode(fileName);
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

    public void initIndigoDashboardWithEditMode(String htmlFile) {
        indigoDashboardsPage = openEmbeddedPage(htmlFile).switchToEditMode();
    }

    public void sendPostMessageForAttributeFilter(String fileName) {
        initIndigoDashboardWithEditMode(fileName);
        cleanUpLogger();
        postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
        postMessageApiPage.setFilter();
        indigoDashboardsPage.waitForWidgetsLoading();
    }
}
