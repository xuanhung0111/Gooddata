package com.gooddata.qa.graphene.rolap;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.http.rolap.RolapRestUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class GoodSalesMetadataDeletedTest extends GoodSalesAbstractTest {

    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;
    private static final String WIN_RATE_METRIC = "Win Rate";
    private static final String DASHBOARD_NAME = "Test metadata deleted";

    private static final String OPPORTUNITY_SNAPSHOT_DATASET = "OpportunitySnapshot";

    private static final String NEW_PIPELINE_DRILL_IN_REPORT = "New Pipeline [Drill-In]";
    private static final String MOVED_IN_PIPELINE_DRILL_IN_REPORT = "Moved In Pipeline [Drill - In]";
    private static final String STARTING_PIPELINE_REPORT = "Starting Pipeline [hl]";
    private static final String ACTIVITIES_BY_TYPE_REPORT = "Activities by Type";

    private static final String COMMENT = "comment";

    private static final String OBJECT_ID_NOT_FOUND = "Object ID %s not found";
    private static final String REQUESTED_DASHBOARD_NOT_EXIST = "The dashboard you have requested does not exist.";
    private static final String DROP_OBJECT_ERROR_MESSAGE = "Can't delete object (%s) while referenced by "
            + "dependent objects (%s).";

    private static final AttributeInfo IS_WON_ATTRIBUTE = new AttributeInfo()
            .withId("1089")
            .withName("Is Won?")
            .<AttributeInfo>withIdentifier("attr.stage.iswon")
            .withDataset("Stage")
            .withLabelIds("1090")
            .withElements(new AttributeElementInfo("false", "955128"),
                    new AttributeElementInfo("true", "955127"));

    private static final AttributeInfo ACCOUNT_ATTRIBUTE = new AttributeInfo().withId("969").withName("Account");

    private static final FactInfo VELOCITY_FACT = new FactInfo().withName("Velocity").withId("1176")
            .<FactInfo>withIdentifier("fact.stagehistory.velocity").withDataset("stagehistory");

    private static final MetricInfo NUMBER_OF_OPPORTUNITIES_METRIC = new MetricInfo().withId("2825")
            .withName("# of Opportunities").<MetricInfo>withIdentifier("afdV48ABh8CN")
            .withAffectedMetric("# of Won Opps.")
            .withAffectedReports("Top 5 Lost (by $)", "Total Lost [hl]", "Quarterly Win Rate");

    private static final ReportInfo NEW_WON_DRILL_IN_REPORT = new ReportInfo().withId("64072")
            .withName("New Won [Drill-In]").withIdentifier("afPveYFCcevy");

    private static final ReportInfo NEW_LOST_DRILL_IN_REPORT = new ReportInfo()
            .<ReportInfo>withName("New Lost [Drill-In]").withReportDef("64178");

    private static final DatasetInfo PRODUCT_DATASET = new DatasetInfo().withId("947").withName("Product")
            .<DatasetInfo>withIdentifier("dataset.product").withAttributes("Product");

    private static final DatasetInfo STAGE_DATASET = new DatasetInfo().withId("1083").withName("Stage")
            .<DatasetInfo>withIdentifier("dataset.stage")
            .withAttributes("Is Active?", "Is Won?", "Stage Name");

    private static final DatasetInfo ACCOUNT_DATASET = new DatasetInfo().withId("967").withName("Account")
            .<DatasetInfo>withIdentifier("dataset.account").withAttributes("Account");

    private static final DatasetInfo DATE_ACTIVITY_DATASET = new DatasetInfo().withId("708")
            .withName("Date (Activity)").<DatasetInfo>withIdentifier("activity.dataset.dt");

    private static final DatasetInfo DATE_CREATED_DATASET = new DatasetInfo().withId("164")
            .withName("Date (Created)").<DatasetInfo>withIdentifier("created.dataset.dt");

    private static final VariableInfo QUOTA_VARIABLE = new VariableInfo().withId("1306").withName("Quota")
            .<VariableInfo>withIdentifier("acCEy9XbdbeO").withAffectedMetric("Quota")
            .withAffectedReport("Actual Performance vs. Goal");

    private static final VariableInfo STATUS_VARIABLE = new VariableInfo().withId("16168").withName("Status")
            .withIdentifier("ae1zQEAYe8gT");

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-metadata-deteled";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteScheduleEmail() throws IOException, JSONException {
        try {
            String dashboardSchedule = "dashboard";
            String reportSchedule = "report";

            initDashboardsPage();
            addReportToNewDashboard(ACTIVITIES_BY_TYPE_REPORT, DASHBOARD_NAME);

            initEmailSchedulesPage();
            emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), dashboardSchedule, "test",
                    DASHBOARD_NAME);
            emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), reportSchedule, "test",
                    ACTIVITIES_BY_TYPE_REPORT, ExportFormat.ALL);

            initEmailSchedulesPage();
            emailSchedulesPage.deleteSchedule(dashboardSchedule);
            emailSchedulesPage.deleteSchedule(reportSchedule);

            assertFalse(isObjectDeleted(ACTIVITIES_BY_TYPE_REPORT, Places.REPORT));
            assertFalse(isObjectDeleted(DASHBOARD_NAME, Places.DASHBOARD));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteComment() throws IOException, JSONException {
        String[] objectLinks = {
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT,
                    NEW_LOST_DRILL_IN_REPORT.reportDef),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, ACCOUNT_ATTRIBUTE.id),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT,
                    NUMBER_OF_OPPORTUNITIES_METRIC.id),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, ACCOUNT_DATASET.id),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, QUOTA_VARIABLE.id),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, VELOCITY_FACT.id)
        };

        dropAllComments(objectLinks, DropStrategy.CASCADE);

        assertFalse(isObjectDeleted(NEW_LOST_DRILL_IN_REPORT.name, Places.REPORT));
        assertFalse(isObjectDeleted(ACCOUNT_DATASET.name, Places.DATASET));
        assertFalse(isObjectDeleted(ACCOUNT_ATTRIBUTE.name, Places.ATTRIBUTE));
        assertFalse(isObjectDeleted(NUMBER_OF_OPPORTUNITIES_METRIC.name, Places.METRIC));
        assertFalse(isObjectDeleted(VELOCITY_FACT.name, Places.FACT));
        assertFalse(isObjectDeleted(QUOTA_VARIABLE.name, Places.VARIABLE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteAttribute() throws JSONException, IOException {
        String userFilterUri = createUserFilterFrom(IS_WON_ATTRIBUTE);
        String computedAttributeName = createComputedAttributeUsing(IS_WON_ATTRIBUTE);
        String filterVariableName = createFilterVariableUsingAttribute(IS_WON_ATTRIBUTE).getRight();
        String metricName = createMetricUsing(IS_WON_ATTRIBUTE);
        String reportName =
                createReportUsing(new UiReportDefinition().withName("Report " + System.currentTimeMillis())
                        .withHows(IS_WON_ATTRIBUTE.name));
        try {
            createDashboardWithAttributeFilter(DashFilterTypes.ATTRIBUTE, IS_WON_ATTRIBUTE.name);
            String savedViewName = createSavedView("true");

            dropObject(IS_WON_ATTRIBUTE.identifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(userFilterUri, Places.GREY_PAGE));
            assertFalse(isObjectDeleted(IS_WON_ATTRIBUTE.dataset, Places.DATASET));
            assertFalse(isObjectDeleted(computedAttributeName, Places.ATTRIBUTE));
            assertTrue(isObjectDeleted(filterVariableName, Places.VARIABLE));
            assertTrue(isObjectDeleted(metricName, Places.METRIC));
            assertTrue(isObjectDeleted(reportName, Places.REPORT));
            assertTrue(isObjectDeleted(IS_WON_ATTRIBUTE.name, Places.DASHBOARD_FILTER));
            assertFalse(isObjectDeleted(savedViewName, Places.SAVED_VIEW));
            for (String labelUri : IS_WON_ATTRIBUTE.getLabelUriFormats()) {
                assertTrue(isObjectDeleted(String.format(labelUri, testParams.getProjectId()), Places.GREY_PAGE));
            }
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteFact() throws JSONException, IOException {
        String metricName = createMetricUsing(VELOCITY_FACT, SimpleMetricTypes.SUM);

        dropObject(VELOCITY_FACT.identifier, DropStrategy.CASCADE);
        assertFalse(isObjectDeleted(VELOCITY_FACT.dataset, Places.DATASET));
        assertTrue(isObjectDeleted(metricName, Places.METRIC));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deletePrompt() throws JSONException, IOException {
        dropObject(QUOTA_VARIABLE.identifier, DropStrategy.CASCADE);
        assertTrue(isObjectDeleted(QUOTA_VARIABLE.affectedMetric, Places.METRIC));
        assertTrue(isObjectDeleted(QUOTA_VARIABLE.affectedReport, Places.REPORT));

        try {
            createDashboardWithAttributeFilter(DashFilterTypes.PROMPT, STATUS_VARIABLE.name);

            dropObject(STATUS_VARIABLE.identifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(STATUS_VARIABLE.name, Places.DASHBOARD_FILTER));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteMetric() throws JSONException, IOException {
        try {
            createDashboardWithGeoChart(NUMBER_OF_OPPORTUNITIES_METRIC.name);

            dropObject(NUMBER_OF_OPPORTUNITIES_METRIC.identifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(NUMBER_OF_OPPORTUNITIES_METRIC.affectedMetric, Places.METRIC));
            for (String report : NUMBER_OF_OPPORTUNITIES_METRIC.affectedReports) {
                assertTrue(isObjectDeleted(report, Places.REPORT));
            }
            assertTrue(isObjectDeleted("geo chart", Places.DASHBOARD_GEO));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteReport() throws JSONException, IOException {
        String reportSchedule = null;
        try {
            addReportToNewDashboard(NEW_WON_DRILL_IN_REPORT.name, DASHBOARD_NAME);
            reportSchedule = createReportSchedule(NEW_WON_DRILL_IN_REPORT.name);

            dropObject(NEW_WON_DRILL_IN_REPORT.identifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(NEW_WON_DRILL_IN_REPORT.name, Places.DASHBOARD_REPORT));
            assertFalse(isObjectDeleted(reportSchedule, Places.SCHEDULE_EMAIL));
        } finally {
            tryDeleteDashboard();
            deleteSchedule(reportSchedule);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"group1"})
    public void deleteDashboard() throws IOException, JSONException {
        addReportToNewDashboard(NEW_LOST_DRILL_IN_REPORT.name, DASHBOARD_NAME);

        String url = browser.getCurrentUrl();
        System.out.println(url);
        String dashboardId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("|"));
        String dashboardIdentifier = getIdentifierFromObjId(dashboardId, ObjectType.PROJECT_DASHBOARD);

        String previewUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();
        String dashboardSchedule = createDashboardSchedule(DASHBOARD_NAME);

        dropObject(dashboardIdentifier, DropStrategy.CASCADE);
        assertTrue(isObjectDeleted(previewUri, Places.DASHBOARD_EMBED));
        assertTrue(isObjectDeleted(dashboardSchedule, Places.SCHEDULE_EMAIL));
    }

    @Test(dependsOnGroups = {"group1"}, alwaysRun = true)
    public void resetLDM() throws JSONException, IOException {
        deleteProject(testParams.getProjectId());
        createProject();
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithNoReportUsageUsingCascadeStrategy() throws IOException, JSONException {
        deleteDomainHelper(DropStrategy.CASCADE, MOVED_IN_PIPELINE_DRILL_IN_REPORT, NEW_PIPELINE_DRILL_IN_REPORT);
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithNoReportUsageUsingAllInStrategy() throws IOException, JSONException {
        String report1 = createReportUsing(new UiReportDefinition().withName("Report1").withHows("Stage History"));
        String report2 = createReportUsing(new UiReportDefinition().withName("Report2").withHows("Stage History"));

        deleteDomainHelper(DropStrategy.ALL_IN, report1, report2);
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithReportUsageUsingCascadeStrategy() throws IOException, JSONException {
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();

        moveReportsToFolder(folderName, STARTING_PIPELINE_REPORT);
        String folderIdentifier = getIdentifierFromObjId(folderId, ObjectType.DOMAIN);

        try {
            addReportToNewDashboard(STARTING_PIPELINE_REPORT, DASHBOARD_NAME);
            dropObject(folderIdentifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(STARTING_PIPELINE_REPORT, Places.DASHBOARD_REPORT));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"deleteDomainWithReportUsageUsingCascadeStrategy"}, groups = {"group2"})
    public void deleteDomainWithReportUsageUsingAllInStrategy() throws IOException, JSONException {
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();

        moveReportsToFolder(folderName, ACTIVITIES_BY_TYPE_REPORT);
        String folderIdentifier = getIdentifierFromObjId(folderId, ObjectType.DOMAIN);

        try {
            addReportToNewDashboard(ACTIVITIES_BY_TYPE_REPORT, DASHBOARD_NAME);
            tryDropObject(folderIdentifier, DropStrategy.ALL_IN);
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithNoAttributeUsageUsingCascadeStrategy() throws IOException, JSONException {
        dropObject(PRODUCT_DATASET.identifier, DropStrategy.CASCADE);
        for (String attribute : PRODUCT_DATASET.attributes) {
            assertTrue(isObjectDeleted(attribute, Places.ATTRIBUTE));
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithAttributeUsageUsingCascadeStrategy() throws IOException, JSONException {
        try {
            createDashboardWithAttributeFilter(DashFilterTypes.ATTRIBUTE, STAGE_DATASET.attributes[0]);

            dropObject(STAGE_DATASET.identifier, DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(STAGE_DATASET.attributes[0], Places.DASHBOARD_FILTER));
            for (String attribute : STAGE_DATASET.attributes) {
                assertTrue(isObjectDeleted(attribute, Places.ATTRIBUTE));
            }
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithAttributeUsageUsingAllInStrategy() throws JSONException,
            ParseException, IOException {
        try {
            createDashboardWithAttributeFilter(DashFilterTypes.ATTRIBUTE, STAGE_DATASET.attributes[0]);

            tryDropObject(STAGE_DATASET.identifier, DropStrategy.ALL_IN);
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"deleteDatasetWithAttributeUsageUsingAllInStrategy"}, groups = {"group2"})
    public void deleteDatasetWithNoAttributeUsageUsingAllInStrategy() throws JSONException, IOException {
        dropObject(ACCOUNT_DATASET.identifier, DropStrategy.CASCADE);
        for (String attribute : ACCOUNT_DATASET.attributes) {
            assertTrue(isObjectDeleted(attribute, Places.ATTRIBUTE));
        }
    }

    @Test(dependsOnMethods = {"deleteDatasetWithNoAttributeUsageUsingAllInStrategy"}, groups = {"group2"})
    public void deleteConnectedDataset() throws IOException, JSONException {
        assertFalse(isObjectDeleted(OPPORTUNITY_SNAPSHOT_DATASET, Places.DATASET));
    }

    @Test(dependsOnGroups = {"group2"})
    public void deleteDatasetContinually() throws JSONException {
        dropObject(DATE_ACTIVITY_DATASET.identifier, DropStrategy.CASCADE);
        dropObject(DATE_CREATED_DATASET.identifier, DropStrategy.CASCADE);
    }

    private void tryDeleteDashboard() {
        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            dashboardsPage.deleteDashboard();
        } catch (AssertionError e) {
            // sometime we get RED BAR: Dashboard no longer exists
            // in this case, we also want to delete this dashboard so ignore this issue
        }
    }

    private void deleteSchedule(String scheduleName) {
        if (scheduleName == null)
            return;

        initEmailSchedulesPage();
        emailSchedulesPage.deleteSchedule(scheduleName);
    }

    private void deleteDomainHelper(DropStrategy strategy, String... reports) throws IOException, JSONException {
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();

        moveReportsToFolder(folderName, reports);
        String folderIdentifier = getIdentifierFromObjId(folderId, ObjectType.DOMAIN);

        dropObject(folderIdentifier, strategy);
        assertTrue(isObjectDeleted(folderName, Places.REPORT_FOLDER));
        for (String report : reports) {
            assertTrue(isObjectDeleted(report, Places.REPORT));
        }
    }

    private void moveReportsToFolder(String folder, String... reports) {
        initReportsPage();
        reportsPage.getDefaultFolders().openFolder("All");
        reportsPage.moveReportsToFolder(folder, reports);
    }

    private Pair<String, String> createNewReportFolder() {
        String folderName = "New Folder";
        initReportsPage();
        reportsPage.addNewFolder(folderName);

        reportsPage.getCustomFolders().openFolder(folderName);
        String url = browser.getCurrentUrl();
        String folderId = url.substring(url.lastIndexOf("/") + 1);

        return Pair.of(folderId, folderName);
    }

    private String createDashboardSchedule(String dashboard) {
        String subject = "Dashboard Schedule " + System.currentTimeMillis();
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), subject, "body", dashboard);

        return subject;
    }

    private String createReportSchedule(String report) {
        String subject = "Report Schedule " + System.currentTimeMillis();
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), subject, "body", report, ExportFormat.ALL);

        return subject;
    }

    private String getIdentifierFromObjId(String id, ObjectType type) throws IOException, JSONException {
        return getIdentifierFromObjLink(String.format("/gdc/md/%s/obj/%s", testParams.getProjectId(), id), type);
    }

    private String getIdentifierFromObjLink(String link, ObjectType type) throws IOException, JSONException {
        JSONObject json = RestUtils.getJsonObject(getRestApiClient(), link);
        return json.getJSONObject(type.toString()).getJSONObject("meta").getString("identifier");
    }

    private String createMetricUsing(FactInfo fact, SimpleMetricTypes sum) {
        initFactPage();
        factsTable.selectObject(fact.name);
        return factDetailPage.createSimpleMetric(SimpleMetricTypes.SUM, fact.name);
    }

    private boolean isObjectDeleted(String object, Places place) throws IOException, JSONException {
        switch (place) {
            case GREY_PAGE:
                JSONObject json = RestUtils.getJsonObject(getRestApiClient(), object,
                        HttpStatus.NOT_FOUND);
                if (!json.has("error"))
                    return false;
                if (!object
                        .endsWith("/obj/" + json.getJSONObject("error").getJSONArray("parameters").getString(0)))
                    return false;
                if (!OBJECT_ID_NOT_FOUND.equals(json.getJSONObject("error").getString("message")))
                    return false;
                return true;
            case METRIC:
                initMetricPage();
                return !metricPage.isMetricVisible(object);
            case ATTRIBUTE:
                initAttributePage();
                return !attributePage.isAttributeVisible(object);
            case DATASET:
                initManagePage();
                return !datasetsTable.getAllItems().contains(object);
            case VARIABLE:
                initVariablePage();
                return !variablePage.isVariableVisible(object);
            case REPORT:
                initReportsPage();
                return !reportsPage.isReportVisible(object);
            case DASHBOARD_FILTER:
                initDashboardsPage();
                dashboardsPage.selectDashboard(DASHBOARD_NAME);
                return dashboardsPage.getContent().getFilterWidget(CssUtils.simplifyText(object)) == null;
            case SAVED_VIEW:
                initDashboardsPage();
                dashboardsPage.selectDashboard(DASHBOARD_NAME);
                return !dashboardsPage.getSavedViewWidget().openSavedViewMenu().getSavedViewPopupMenu()
                        .getAllSavedViewNames().contains(object);
            case DASHBOARD_GEO:
                initDashboardsPage();
                dashboardsPage.selectDashboard(DASHBOARD_NAME);
                return dashboardsPage.getContent().getGeoCharts().isEmpty();
            case DASHBOARD_REPORT:
                initDashboardsPage();
                dashboardsPage.selectDashboard(DASHBOARD_NAME);
                return dashboardsPage.getContent().getNumberOfReports() == 0;
            case SCHEDULE_EMAIL:
                initEmailSchedulesPage();
                return !emailSchedulesPage.isGlobalSchedulePresent(object);
            case DASHBOARD_EMBED:
                browser.get(object);
                return REQUESTED_DASHBOARD_NOT_EXIST.equals(
                        waitForElementVisible(By.cssSelector("#notFoundPage > p"), browser).getText().trim());
            case REPORT_FOLDER:
                initReportsPage();
                return !reportsPage.getCustomFolders().getAllFolderNames().contains(object);
            case DASHBOARD:
                initDashboardsPage();
                return !dashboardsPage.getDashboardsNames().contains(object);
            default:
                return false;
        }
    }

    private void dropAllComments(String[] objectLinks, DropStrategy strategy) throws ParseException,
        JSONException, IOException {
        for (String link: objectLinks) {
            dropObject(getIdentifierFromObjLink(link, ObjectType.COMMENT), strategy);
        }
    }

    private void dropObject(String identifier, DropStrategy strategy) throws JSONException {
        postMAQL(strategy.getMaql(identifier), STATUS_POLLING_CHECK_ITERATIONS);
    }

    private void tryDropObject(final String identifier, final DropStrategy strategy) throws JSONException,
            ParseException, IOException {
        String pollingUri = RolapRestUtils.executeMAQL(getRestApiClient(), testParams.getProjectId(),
                strategy.getMaql(identifier));
        RolapRestUtils.waitingForAsyncTask(getRestApiClient(), pollingUri);
        assertEquals(RolapRestUtils.getAsyncTaskStatus(getRestApiClient(), pollingUri), "ERROR");
        assertTrue(getErrorMessageFromPollingUri(pollingUri).startsWith(DROP_OBJECT_ERROR_MESSAGE));
    }

    private String getErrorMessageFromPollingUri(String pollingUri)
            throws ParseException, JSONException, IOException {
        return RestUtils.getJsonObject(getRestApiClient(), pollingUri)
                .getJSONObject("wTaskStatus")
                .getJSONArray("messages")
                .getJSONObject(0)
                .getJSONObject("error")
                .getString("message");
    }

    private void createDashboardWithGeoChart(String metric) {
        try {
            initDashboardsPage();
        } catch (AssertionError e) {
            // RED BAR dashboard no longer exist
            // we just need to create a new dashboard so ignore this issue
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
        }
        dashboardsPage.addNewDashboard(DASHBOARD_NAME);

        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardEditBar.addGeoChart(metric, null);
        dashboardEditBar.saveDashboard();
    }

    private void createDashboardWithAttributeFilter(DashFilterTypes filterType, String attribute)
            {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(DASHBOARD_NAME);

        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(filterType, attribute);
        dashboardEditBar.turnSavedViewOption(true);
        sleepTightInSeconds(3);
        dashboardEditBar.saveDashboard();
    }

    private String createSavedView(String... values) {
        dashboardsPage.selectDashboard(DASHBOARD_NAME);
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        dashboardsPage.getContent().getFirstFilter().changeAttributeFilterValue(values);
        String name = "Saved view " + System.currentTimeMillis();
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView(name);

        return name;
    }

    private String createReportUsing(UiReportDefinition reportDefinition) {
        String name = reportDefinition.getName();
        initReportsPage();
        createReport(reportDefinition, name);
        checkRedBar(browser);

        return name;
    }

    private String createMetricUsing(AttributeInfo attribute) {
        String name = "Metric " + System.currentTimeMillis();
        initMetricPage();
        metricPage.createDifferentMetric(name, WIN_RATE_METRIC, attribute.dataset, attribute.name,
                attribute.elements[0].name);

        return name;
    }

    private Pair<String, String> createFilterVariableUsingAttribute(AttributeInfo attribute) {
        String name = "Filter variable " + System.currentTimeMillis();
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        waitForDataPageLoaded(browser);
        sleepTightInSeconds(5);
        variablePage.createVariable(new AttributeVariable(name).withAttribute(attribute.name)
                .withAttributeElements(attribute.elements[0].name));

        String url = browser.getCurrentUrl();
        return Pair.of(url.substring(url.lastIndexOf("/") + 1), name);
    }

    private String createComputedAttributeUsing(AttributeInfo attribute) {
        String name = "CA " + System.currentTimeMillis();
        initAttributePage();
        attributePage.createAttribute();
        createAttributePage.selectAttribute(attribute.name);
        createAttributePage.selectMetric(WIN_RATE_METRIC);
        createAttributePage.setComputedAttributeName(name);
        createAttributePage.submit();
        waitForElementVisible(By.className("s-attributeBucketName"), browser);

        return name;
    }

    private String createUserFilterFrom(AttributeInfo attribute) throws IOException, JSONException {
        return DashboardsRestUtils.createSimpleMufObjByUri(getRestApiClient(),
                testParams.getProjectId(), "User filter "+ System.currentTimeMillis(),
                getConditionInUriFormat(attribute.getConnectionBetweenAttributeAndElements()));
    }

    private Map<String, Collection<String>> getConditionInUriFormat(final Map<String, Collection<String>> condition) {
        final String attributeUri = "/gdc/md/%s/obj/%s";
        final String elementUri = "/gdc/md/%s/obj/%s/elements?id=%s";
        final Map<String, Collection<String>> conditionInUriFormat = new HashMap<String, Collection<String>>();

        for (final String attributeId : condition.keySet()) {
            conditionInUriFormat.put(String.format(attributeUri, testParams.getProjectId(), attributeId),
                    condition.get(attributeId).stream()
                    .map(e -> String.format(elementUri, testParams.getProjectId(), attributeId, e))
                    .collect(Collectors.toList()));
        }

        return conditionInUriFormat;
    }

    @SuppressWarnings("unchecked")
    private static abstract class MetadataInfo {
        protected String name;
        protected String id;
        protected String identifier;

        public <T extends MetadataInfo> T withName(String name) {
            this.name = name;
            return (T) this;
        }

        public <T extends MetadataInfo> T withId(String id) {
            this.id = id;
            return (T) this;
        }

        public <T extends MetadataInfo> T withIdentifier(String identifier) {
            this.identifier = identifier;
            return (T) this;
        }
    }

    private static class FactInfo extends MetadataInfo {
        private String dataset;

        public FactInfo withDataset(String dataset) {
            this.dataset = dataset;
            return this;
        }
    }

    private static class MetricInfo extends MetadataInfo {
        private String affectedMetric;
        private String[] affectedReports;

        public MetricInfo withAffectedMetric(String affectedMetric) {
            this.affectedMetric = affectedMetric;
            return this;
        }

        public MetricInfo withAffectedReports(String... affectedReports) {
            this.affectedReports = affectedReports;
            return this;
        }
    }

    private static class ReportInfo extends MetadataInfo {
        private String reportDef;

        public ReportInfo withReportDef(String reportDef) {
            this.reportDef = reportDef;
            return this;
        }
    }

    private static class VariableInfo extends MetadataInfo {
        private String affectedMetric;
        private String affectedReport;

        public VariableInfo withAffectedMetric(String affectedMetric) {
            this.affectedMetric = affectedMetric;
            return this;
        }

        public VariableInfo withAffectedReport(String affectedReport) {
            this.affectedReport = affectedReport;
            return this;
        }
    }

    private static class DatasetInfo extends MetadataInfo {
        private String[] attributes;

        public DatasetInfo withAttributes(String... attributes) {
            this.attributes = attributes;
            return this;
        }
    }

    private static class AttributeInfo extends MetadataInfo {
        private String[] labelIds;
        private AttributeElementInfo[] elements;
        private String dataset;

        public AttributeInfo withLabelIds(String... labelIds) {
            this.labelIds = labelIds;
            return this;
        }

        public AttributeInfo withElements(AttributeElementInfo... elements) {
            this.elements = elements;
            return this;
        }

        public AttributeInfo withDataset(String dataset) {
            this.dataset = dataset;
            return this;
        }

        public Collection<String> getLabelUriFormats() {
            return Collections2.transform(Arrays.asList(labelIds), new Function<String, String>() {
                @Override
                public String apply(String labelId) {
                    return "/gdc/md/%s/obj/" + labelId;
                }
            });
        }

        public Map<String, Collection<String>> getConnectionBetweenAttributeAndElements() {
            return new HashMap<String, Collection<String>>() {
                private static final long serialVersionUID = 1L;
                {
                    put(id, Collections2.transform(Arrays.asList(elements),
                            new Function<AttributeElementInfo, String>() {
                        @Override
                        public String apply(AttributeElementInfo attributeElement) {
                            return attributeElement.id;
                        }
                    }));
                }
            };
        }
    }

    private static class AttributeElementInfo {
        private String name;
        private String id;

        public AttributeElementInfo(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    private static enum DropStrategy {
        CASCADE("DROP {%s} CASCADE"),
        ALL_IN("DROP ALL IN {%s}"),
        NATIVE("DROP {%s}");

        private String maql;

        private DropStrategy(String maql) {
            this.maql = maql;
        }

        public String getMaql(String identifier) {
            return String.format(maql, identifier);
        }
    }

    private static enum Places {
        GREY_PAGE,
        ATTRIBUTE,
        FACT,
        VARIABLE,
        METRIC,
        REPORT,
        SAVED_VIEW,
        DASHBOARD_FILTER,
        DATASET,
        DASHBOARD_GEO,
        SCHEDULE_EMAIL,
        DASHBOARD_REPORT,
        DASHBOARD_EMBED,
        REPORT_FOLDER,
        DASHBOARD;
    }

    private static enum ObjectType {
        DOMAIN("domain"),
        PROJECT_DASHBOARD("projectDashboard"),
        COMMENT("comment");

        private String key;

        private ObjectType(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
