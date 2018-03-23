package com.gooddata.qa.graphene.rolap;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_VELOCITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WIN_RATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITY_LEVEL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_DATE_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NEW_LOST_DRILL_IN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NEW_WON_DRILL_IN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_SALES_SEASONALITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.UrlParserUtils.getObjId;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.manage.VariablesPage;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.http.InvalidStatusCodeException;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;

public class GoodSalesMetadataDeletedTest extends GoodSalesAbstractTest {

    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;
    private static final String DASHBOARD_NAME = "Test metadata deleted";
    private static final String COMMENT = "comment";
    private static final String OBJECT_ID_NOT_FOUND = "Object ID %s not found";
    private static final String REQUESTED_DASHBOARD_NOT_EXIST = "The dashboard you have requested does not exist.";
    private static final String DROP_OBJECT_ERROR_MESSAGE = "Can't delete object (%s) while referenced by "
            + "dependent objects (%s).";
    private VariableRestRequest varRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-metadata-deteled";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getVariableCreator().createQuoteVariable();
        getVariableCreator().createStatusVariable();
        getReportCreator().createNewLostDrillInReport();
        getReportCreator().createNewWonDrillInReport();
        getReportCreator().createActivitiesByTypeReport();
        getMetricCreator().createWinRateMetric();
        varRequest = new VariableRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteScheduleEmail() throws IOException, JSONException {
        try {
            String dashboardSchedule = "dashboard";
            String reportSchedule = "report";

            initDashboardsPage();
            addReportToNewDashboard(REPORT_ACTIVITIES_BY_TYPE, DASHBOARD_NAME);

            initEmailSchedulesPage()
                .scheduleNewDashboardEmail(singletonList(testParams.getUser()),
                        dashboardSchedule, "test", singletonList(DASHBOARD_NAME))
                .scheduleNewReportEmail(singletonList(testParams.getUser()),
                        reportSchedule, "test", REPORT_ACTIVITIES_BY_TYPE, ExportFormat.ALL);

            initEmailSchedulesPage().deleteSchedule(dashboardSchedule)
                .deleteSchedule(reportSchedule);

            assertFalse(isObjectDeleted(getReportByTitle(REPORT_ACTIVITIES_BY_TYPE).getUri()));
            assertFalse(isObjectDeleted(DashboardsRestUtils.getDashboardUri(getRestApiClient(), testParams.getProjectId(), 
                    DASHBOARD_NAME)));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteComment() throws IOException, JSONException {
        String[] objectLinks = {
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT,
                    getReportByTitle(REPORT_NEW_LOST_DRILL_IN).getUri()),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, 
                    getAttributeByTitle(ATTR_ACCOUNT).getUri()),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT,
                    getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri()),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, 
                    getDatasetByTitle("Account").getUri()),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, 
                    varRequest.getVariableUri(VARIABLE_QUOTA)),
            DashboardsRestUtils.addComment(getRestApiClient(), testParams.getProjectId(), COMMENT, 
                    getFactByTitle(FACT_VELOCITY).getUri())
        };

        for (String link: objectLinks) {
            dropObject(getIdentifierFromObjLink(link, COMMENT), DropStrategy.CASCADE);
        }

        assertFalse(isObjectDeleted(getReportByTitle(REPORT_NEW_LOST_DRILL_IN).getUri()));
        assertFalse(isObjectDeleted(getDatasetByTitle("Account").getUri()));
        assertFalse(isObjectDeleted(getAttributeByTitle(ATTR_ACCOUNT).getUri()));
        assertFalse(isObjectDeleted(getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri()));
        assertFalse(isObjectDeleted(getFactByTitle(FACT_VELOCITY).getUri()));
        assertFalse(isObjectDeleted(varRequest.getVariableUri(VARIABLE_QUOTA)));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteAttribute() throws JSONException, IOException {
        Attribute attributeIsWon = getAttributeByTitle(ATTR_IS_WON);
        String firstElement = getMdService()
                .getAttributeElements(attributeIsWon).stream()
                .findFirst()
                .get()
                .getTitle();
        String computedAttributeName = createComputedAttributeUsing(ATTR_IS_WON);
        String filterVariableName = createFilterVariableUsingAttribute(ATTR_IS_WON, firstElement).getRight();
        String metricName = createMetricUsing(ATTR_IS_WON, firstElement);
        String reportName = createReportUsing(new UiReportDefinition().withName("Report " + System.currentTimeMillis())
                .withHows(ATTR_IS_WON));
        String metricNameUri = getMetricByTitle(metricName).getUri();
        String reportNameUri = getReportByTitle(reportName).getUri();
        String filterVariableNameUri = varRequest.getVariableUri(filterVariableName);
        try {
            createDashboardWithAttributeFilter(DashAttributeFilterTypes.ATTRIBUTE, ATTR_IS_WON);
            String savedViewName = createSavedView("true");
            List<Pair<String, Integer>> labelUris = getObjectElementsByID(Integer.parseInt(getObjId(
                    getAttributeByTitle(ATTR_IS_WON).getDefaultDisplayForm().getUri())));

            dropObject(getAttributeByTitle(ATTR_IS_WON).getIdentifier(), DropStrategy.CASCADE);
            assertFalse(isObjectDeleted(getDatasetByTitle("Stage").getUri()));
            assertFalse(isObjectDeleted(getAttributeByTitle(computedAttributeName).getUri()));
            assertTrue(isObjectDeleted(filterVariableNameUri));
            assertTrue(isObjectDeleted(metricNameUri));
            assertTrue(isObjectDeleted(reportNameUri));

            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            assertTrue(dashboardsPage.getContent().getFilterWidget(CssUtils.simplifyText(ATTR_IS_WON)) == null);
            assertFalse(!dashboardsPage.getSavedViewWidget().openSavedViewMenu().getSavedViewPopupMenu()
                    .getAllSavedViewNames().contains(savedViewName));
            for (Pair<String, Integer> labelUri : labelUris) {
                assertTrue(isObjectDeleted(String.format(labelUri.getRight().toString(), testParams.getProjectId())));
            }
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteFact() throws JSONException, IOException {
        initFactPage();
        ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser).selectObject(FACT_VELOCITY);
        String metricName = FactDetailPage.getInstance(browser).createSimpleMetric(SimpleMetricTypes.SUM, FACT_VELOCITY);
        String metricUri = getMetricByTitle(metricName).getUri();
        String identifierVelocityFact = getFactByTitle(FACT_VELOCITY).getIdentifier(); 

        dropObject(identifierVelocityFact, DropStrategy.CASCADE);
        assertFalse(isObjectDeleted(getDatasetByTitle("stagehistory").getUri()));
        assertTrue(isObjectDeleted(metricUri));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deletePrompt() throws JSONException, IOException {
        final String ReportUsingQuotaVariable = "Quote Report";
        //Create metric and report using Quote variable
        getMetricCreator().createQuotaMetric();
        createReport(GridReportDefinitionContent.create(ReportUsingQuotaVariable,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_QUOTA)))));

        String quotaMetricUri = getMetricByTitle(METRIC_QUOTA).getUri();
        String reportUsingQuotaVariableUri = getReportByTitle(ReportUsingQuotaVariable).getUri();

        dropObject(getIdentifierFromObjLink(
                varRequest.getVariableUri(VARIABLE_QUOTA), "prompt"), DropStrategy.CASCADE);
        assertTrue(isObjectDeleted(quotaMetricUri));
        assertTrue(isObjectDeleted(reportUsingQuotaVariableUri));

        try {
            createDashboardWithAttributeFilter(DashAttributeFilterTypes.PROMPT, VARIABLE_STATUS);

            dropObject(getIdentifierFromObjLink(
                    varRequest.getVariableUri(VARIABLE_STATUS), "prompt"), DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(VARIABLE_STATUS));
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteMetric() throws JSONException, IOException {
        //create report using won opp. metric
        getReportCreator().createSalesSeasonalityReport();

        String metricNumberOfWonOppsUri = getMetricByTitle(METRIC_NUMBER_OF_WON_OPPS).getUri();
        String reportTop5WonByCashUri = getReportByTitle(REPORT_SALES_SEASONALITY).getUri();
        try {
            createDashboardWithGeoChart(METRIC_NUMBER_OF_WON_OPPS);
            dropObject(getMetricByTitle(METRIC_NUMBER_OF_WON_OPPS).getIdentifier(), DropStrategy.CASCADE);
            assertTrue(isObjectDeleted(metricNumberOfWonOppsUri));
            assertTrue(isObjectDeleted(reportTop5WonByCashUri));
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            assertTrue(dashboardsPage.getContent().getNumberOfReports() == 0);

        } finally {
            tryDeleteDashboard();
            getMetricCreator().createNumberOfOpportunitiesMetric();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteReport() throws JSONException, IOException {
        String reportSchedule = null;
        try {
            addReportToNewDashboard(REPORT_NEW_WON_DRILL_IN, DASHBOARD_NAME);
            reportSchedule = createReportSchedule(REPORT_NEW_WON_DRILL_IN);

            dropObject(getReportByTitle(REPORT_NEW_WON_DRILL_IN).getIdentifier(), DropStrategy.CASCADE);
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            assertTrue(dashboardsPage.getContent().getNumberOfReports() == 0);
            assertFalse(!initEmailSchedulesPage().isGlobalSchedulePresent(reportSchedule));
        } finally {
            tryDeleteDashboard();
            deleteSchedule(reportSchedule);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"group1"})
    public void deleteDashboard() throws IOException, JSONException {
        addReportToNewDashboard(REPORT_NEW_LOST_DRILL_IN, DASHBOARD_NAME);

        String url = browser.getCurrentUrl();
        System.out.println(url);
        String dashboardId = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("|"));
        String dashboardIdentifier = getIdentifierFromObjId(dashboardId, "projectDashboard");

        String previewUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();
        String dashboardSchedule = createDashboardSchedule(DASHBOARD_NAME);

        dropObject(dashboardIdentifier, DropStrategy.CASCADE);
        browser.get(previewUri);
        assertTrue(REQUESTED_DASHBOARD_NOT_EXIST.equals(
                waitForElementVisible(By.cssSelector("#notFoundPage > p"), browser).getText().trim()));
        assertTrue(!initEmailSchedulesPage().isGlobalSchedulePresent(dashboardSchedule));
    }

    @Test(dependsOnGroups = {"group1"}, alwaysRun = true)
    public void resetLDM() throws Throwable {
        deleteProject(testParams.getProjectId());
        createProject();
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithNoReportUsageUsingCascadeStrategy() throws IOException, JSONException {
        getReportCreator().createActivitiesByTypeReport();
        getReportCreator().createAmountByDateClosedReport();
        deleteDomainHelper(DropStrategy.CASCADE, REPORT_ACTIVITIES_BY_TYPE, REPORT_AMOUNT_BY_DATE_CLOSED);
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithNoReportUsageUsingAllInStrategy() throws IOException, JSONException {
        String report1 = createReportUsing(new UiReportDefinition().withName("Report1").withHows("Stage History"));
        String report2 = createReportUsing(new UiReportDefinition().withName("Report2").withHows("Stage History"));

        deleteDomainHelper(DropStrategy.ALL_IN, report1, report2);
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDomainWithReportUsageUsingCascadeStrategy() throws IOException, JSONException {
        getReportCreator().createActiveLevelReport();
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();

        moveReportsToFolder(folderName, REPORT_ACTIVITY_LEVEL);
        String folderIdentifier = getIdentifierFromObjId(folderId, "domain");

        try {
            addReportToNewDashboard(REPORT_ACTIVITY_LEVEL, DASHBOARD_NAME);
            dropObject(folderIdentifier, DropStrategy.CASCADE);
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            assertTrue(dashboardsPage.getContent().getNumberOfReports() == 0);
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"deleteDomainWithReportUsageUsingCascadeStrategy"}, groups = {"group2"})
    public void deleteDomainWithReportUsageUsingAllInStrategy() throws IOException, JSONException {
        getReportCreator().createActivitiesByTypeReport();
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();

        moveReportsToFolder(folderName, REPORT_ACTIVITIES_BY_TYPE);
        String folderIdentifier = getIdentifierFromObjId(folderId, "domain");

        try {
            addReportToNewDashboard(REPORT_ACTIVITIES_BY_TYPE, DASHBOARD_NAME);
            tryDropObject(folderIdentifier, DropStrategy.ALL_IN);
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithNoAttributeUsageUsingCascadeStrategy() throws IOException, JSONException {
        List<String> attributeUris = getDatasetByIdentifier("dataset.product").getAttributes();
        dropObject("dataset.product", DropStrategy.CASCADE);
        for (String attribute : attributeUris) {
            assertTrue(isObjectDeleted(attribute));
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithAttributeUsageUsingCascadeStrategy() throws IOException, JSONException {
        List<String> attributeUris = getDatasetByIdentifier("dataset.stage").getAttributes();
        String firstAttribute = getAttributeByUri(getDatasetByIdentifier("dataset.stage")
                .getAttributes().get(0)).getTitle();
        try {
            createDashboardWithAttributeFilter(DashAttributeFilterTypes.ATTRIBUTE, firstAttribute);
            
            dropObject("dataset.stage", DropStrategy.CASCADE);
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_NAME);
            assertTrue(dashboardsPage.getContent().getFilterWidget(CssUtils.simplifyText(firstAttribute)) == null);
            for (String attribute : attributeUris) {
                assertTrue(isObjectDeleted(attribute));
            }
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"resetLDM"}, groups = {"group2"})
    public void deleteDatasetWithAttributeUsageUsingAllInStrategy() throws JSONException,
            ParseException, IOException {
        try {
            createDashboardWithAttributeFilter(DashAttributeFilterTypes.ATTRIBUTE, getAttributeByUri(
            		getDatasetByIdentifier("dataset.stage").getAttributes().get(0)).getTitle());

            tryDropObject("dataset.stage", DropStrategy.ALL_IN);
        } finally {
            tryDeleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"deleteDatasetWithAttributeUsageUsingAllInStrategy"}, groups = {"group2"})
    public void deleteDatasetWithNoAttributeUsageUsingAllInStrategy() throws JSONException, IOException {
    	List<String> attributeUris = getDatasetByIdentifier("dataset.account").getAttributes();
        dropObject("dataset.account", DropStrategy.CASCADE);
        for (String attribute : attributeUris) {
            assertTrue(isObjectDeleted(attribute));
        }
    }

    @Test(dependsOnMethods = {"deleteDatasetWithNoAttributeUsageUsingAllInStrategy"}, groups = {"group2"})
    public void deleteConnectedDataset() throws IOException, JSONException {
        assertFalse(isObjectDeleted(getDatasetByTitle("OpportunitySnapshot").getUri()));
    }

    @Test(dependsOnGroups = {"group2"})
    public void deleteDatasetContinually() throws JSONException {
        dropObject("activity.dataset.dt", DropStrategy.CASCADE);
        dropObject("created.dataset.dt", DropStrategy.CASCADE);
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

        initEmailSchedulesPage().deleteSchedule(scheduleName);
    }

    private void deleteDomainHelper(DropStrategy strategy, String... reports) throws IOException, JSONException {
        Pair<String, String> folderIdAndName = createNewReportFolder();
        String folderId = folderIdAndName.getLeft();
        String folderName = folderIdAndName.getRight();
        moveReportsToFolder(folderName, reports);
        String folderIdentifier = getIdentifierFromObjId(folderId, "domain");
        List<String> reportUris = new ArrayList<>();
        for (String report : reports) {
            reportUris.add(getReportByTitle(report).getUri());
        }

        dropObject(folderIdentifier, strategy);
        assertTrue(!initReportsPage().getAllFolderNames().contains(folderName));
        for (String reportUri : reportUris) {
            assertTrue(isObjectDeleted(reportUri));
        }
    }

    private void moveReportsToFolder(String folder, String... reports) {
        initReportsPage().openFolder("All").moveReportsToFolder(folder, reports);
    }

    private Pair<String, String> createNewReportFolder() {
        String folderName = "New Folder";
        initReportsPage()
            .addNewFolder(folderName)
            .openFolder(folderName);
        String url = browser.getCurrentUrl();
        String folderId = url.substring(url.lastIndexOf("/") + 1);

        return Pair.of(folderId, folderName);
    }

    private String createDashboardSchedule(String dashboard) {
        String subject = "Dashboard Schedule " + System.currentTimeMillis();
        initEmailSchedulesPage().scheduleNewDashboardEmail(
                singletonList(testParams.getUser()), subject, "body", singletonList(dashboard));

        return subject;
    }

    private String createReportSchedule(String report) {
        String subject = "Report Schedule " + System.currentTimeMillis();
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(testParams.getUser()), subject, "body", report, ExportFormat.ALL);

        return subject;
    }

    private String getIdentifierFromObjId(String id, String type) throws IOException, JSONException {
        return getIdentifierFromObjLink(String.format("/gdc/md/%s/obj/%s", testParams.getProjectId(), id), type);
    }

    private String getIdentifierFromObjLink(String link, String type) throws IOException, JSONException {
        JSONObject json = RestUtils.getJsonObject(getRestApiClient(), link);
        return json.getJSONObject(type).getJSONObject("meta").getString("identifier");
    }

    private boolean isObjectDeleted(String object) throws IOException, JSONException {
        JSONObject json;
        try{
            json = RestUtils.getJsonObject(getRestApiClient(), object);
        } catch (InvalidStatusCodeException e) {
            return true;
        }
        if (!json.has("error") 
            || !object.endsWith("/obj/" + json.getJSONObject("error").getJSONArray("parameters").getString(0)) 
            || !OBJECT_ID_NOT_FOUND.equals(json.getJSONObject("error").getString("message")))
            return false;
        return true;
    }

    private void dropObject(String identifier, DropStrategy strategy) throws JSONException {
        postMAQL(strategy.getMaql(identifier), STATUS_POLLING_CHECK_ITERATIONS);
    }

    private void tryDropObject(String identifier, DropStrategy strategy) throws JSONException,
            ParseException, IOException {
        RolapRestRequest request = new RolapRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String pollingUri = request.executeMAQL(strategy.getMaql(identifier));
        request.waitingForAsyncTask(pollingUri);
        assertEquals(request.getAsyncTaskStatus(pollingUri), "ERROR");
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

    private void createDashboardWithAttributeFilter(DashAttributeFilterTypes filterType, String attribute) {
        initDashboardsPage()
                .addNewDashboard(DASHBOARD_NAME)
                .addAttributeFilterToDashboard(filterType, attribute)
                .turnSavedViewOption(true);
        sleepTightInSeconds(3);
        dashboardsPage.saveDashboard();
    }

    private String createSavedView(String... values) {
        dashboardsPage.selectDashboard(DASHBOARD_NAME);
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        dashboardsPage.getContent().getFirstFilter().changeAttributeFilterValues(values);
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

    private String createMetricUsing(String attributeTitle, String elementTitle) {
        String name = "Metric " + System.currentTimeMillis();
        initMetricPage().createDifferentMetric(name, METRIC_WIN_RATE, attributeTitle, elementTitle);

        return name;
    }

    private Pair<String, String> createFilterVariableUsingAttribute(String attributeTitle, String elementTitle) {
        String name = "Filter variable " + System.currentTimeMillis();
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        waitForDataPageLoaded(browser);
        sleepTightInSeconds(5);
        VariablesPage.getInstance(browser).createVariable(new AttributeVariable(name).withAttribute(attributeTitle)
                .withAttributeValues(elementTitle));

        String url = browser.getCurrentUrl();
        return Pair.of(url.substring(url.lastIndexOf("/") + 1), name);
    }

    private String createComputedAttributeUsing(String attributeTitle) {
        String name = "CA " + System.currentTimeMillis();
        initAttributePage()
            .moveToCreateAttributePage()
            .createComputedAttribute(new ComputedAttributeDefinition().withAttribute(attributeTitle)
                    .withMetric(METRIC_WIN_RATE)
                    .withName(name));
        return name;
    }

    private enum DropStrategy {
        CASCADE("DROP {%s} CASCADE"),
        ALL_IN("DROP ALL IN {%s}"),
        NATIVE("DROP {%s}");

        private String maql;

        DropStrategy(String maql) {
            this.maql = maql;
        }

        public String getMaql(String identifier) {
            return String.format(maql, identifier);
        }
    }
}
