/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.OneNumberReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;

    private ZendeskHelper zendeskHelper;
    private boolean useApiProxy;

    private static final By BY_ONE_NUMBER_REPORT = By.id("oneNumberContainer");

    private static final String JSON_TICKET_CREATE = "{\"ticket\":{\"subject\":\"GD test ticket - %s\", " +
            "\"comment\": { \"body\": \"Description of automatically created ticket\" }}}";

    private static final String JSON_USER_CREATE = "{\"user\": {\"name\": \"GD test user\", \"email\": " +
            "\"qa+zendesk-test%s@gooddata.com\"}}";

    private static final String JSON_ORGANIZATION_CREATE =
            "{\"organization\": {\"name\": \"GD test organization - %s\"}}";

    private static final String TICKETS_REPORT_NAME = "Tickets count";
    private static final String USERS_REPORT_NAME = "Users count";
    private static final String ORGANIZATIONS_REPORT_NAME = "Organizations count";
    private static final String TICKET_EVENTS_REPORT_NAME = "Ticket events count";
    private static final String BACKLOG_TICKETS_REPORT_NAME = "Backlog Tickets count";

    private int createdZendeskTicketId;
    private int createdZendeskUserId;
    private int createdZendeskOrganizationId;
    private Map<String, Integer> reportMetricsResults;

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = testParams.loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = testParams.loadProperty("connectors.zendesk4.uploadUser");
        zendeskUploadUserPassword = testParams.loadProperty("connectors.zendesk4.uploadUserPassword");

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Insights - View Only", new String[]{
                "Overview", "Tickets", "Satisfaction", "Efficiency", "Agent Activity", "Learn More"
        });
        expectedDashboardsAndTabs.put("My dashboard", new String[]{"First Tab"});
        zendeskAPIUser = testParams.loadProperty("connectors.zendesk.apiUser");
        zendeskAPIPassword = testParams.loadProperty("connectors.zendesk.apiUserPassword");
        useApiProxy = Boolean.parseBoolean(testParams.loadProperty("http.client.useApiProxy"));

        reportMetricsResults = new HashMap<String, Integer>();
    }

    /**
    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createTicketsCountMetric() {
        goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(projectId);
        MetadataService md = goodDataClient.getMetadataService();
        Metric metric = new Metric(
                "Count tickets without deleted",
                "SELECT COUNT([/gdc/md/lemfbrirabuw9cinvjsbfs96qq4it49o/obj/1333]) WHERE " +
                        "[/gdc/md/lemfbrirabuw9cinvjsbfs96qq4it49o/obj/1305] NOT IN " +
                        "([/gdc/md/lemfbrirabuw9cinvjsbfs96qq4it49o/obj/1305/elements?id=27])", "#,##0");
        Metric m = md.createObj(project, metric);
        System.out.println("Metric for testing of non-deleted tickets created, id: " + m.getMeta().getIdentifier());
    }
    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createOrganizationsCountMetric() {
        goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(projectId);
        MetadataService md = goodDataClient.getMetadataService();
        Metric metric = new Metric("# Organizations - not deleted",
            String.format("SELECT [/gdc/md/%s/obj/18676] WHERE " +
                "[/gdc/md/%s/obj/27238] <> " +
                "[/gdc/md/%s/obj/27238/elements?id=1]", projectId, projectId, projectId), "#,##0");
        Metric m = md.createObj(project, metric);
        System.out.println("Metric for testing of non-deleted organizations created, id: " + m.getMeta().getIdentifier());
    }
    private GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = new GoodData(testParams.getHost(), testParams.getUser(), testParams.getPassword());
        }
        return goodDataClient;
    }
    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createOrganizationMetric() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        Map<String, String> data = new HashMap<String, String>();
        data.put("metric1", "# Organizations");
        data.put("attrFolder1", "Organizations");
        data.put("attribute1", "Organization Deleted");
        data.put("attrFolder2", "Organizations");
        data.put("attribute2", "Organization Deleted");
        data.put("attrValue1", "false");
        data.put("attrValue2", "false");
        metricEditorPage.createFilterMetric(MetricTypes.NOT_IN, "# Non-deleted organizations", data);
    }
    **/

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskTicketsReport() throws InterruptedException {
        createBasicReport("# Tickets", TICKETS_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskUsersReport() throws InterruptedException {
        createBasicReport("# Users", USERS_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskOrganizationsReport() throws InterruptedException {
        createBasicReport("# Organizations", ORGANIZATIONS_REPORT_NAME);
    }

    /**
     * TODO
    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskTicketEventsReport() throws InterruptedException {
        createBasicReport("# Ticket Events", TICKET_EVENTS_REPORT_NAME);
    }
     */

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createReportsForMetrics() throws InterruptedException {
        createBasicReport("# Backlog Tickets", BACKLOG_TICKETS_REPORT_NAME);
        //TODO other metrics will be added
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void initZendeskApiClient() {
        if (zendeskApiUrl.contains("staging") && !zendeskAPIUser.isEmpty() && !zendeskAPIPassword.isEmpty()) {
            zendeskHelper = new ZendeskHelper(new RestApiClient(zendeskApiUrl.replace("https://", ""),
                    zendeskAPIUser, zendeskAPIPassword, false, useApiProxy));
        } else {
            fail("Zendesk staging API is not used, tests for adding new objects will be skipped");
        }
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskTicketsReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testTicketsCount() throws IOException, JSONException, InterruptedException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME, "full_start");
        reportMetricsResults.put(TICKETS_REPORT_NAME, gdTicketsCount);
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskUsersReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testUsersCount() throws IOException, JSONException, InterruptedException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME, "full_start");
        reportMetricsResults.put(USERS_REPORT_NAME, gdUsersCount);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskOrganizationsReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCount() throws IOException, JSONException, InterruptedException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "full_start");
        reportMetricsResults.put(ORGANIZATIONS_REPORT_NAME, gdOrganizationsCount);
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ZendeskObject.ORGANIZATION);
    }

    /**
     * TODO
    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskTicketEventsReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketEventsCount() throws IOException, JSONException, InterruptedException {
        compareObjectsCount(getNumberFromGDReport(TICKET_EVENTS_REPORT_NAME), zendeskHelper.getNumberOfTicketEvents(),
                ZendeskHelper.ZendeskObject.TICKET_EVENT);
    }
    */

    @Test(dependsOnMethods = {"initZendeskApiClient", "createReportsForMetrics"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testReportMetricsCount() throws IOException, JSONException, InterruptedException {
        reportMetricsResults.put(BACKLOG_TICKETS_REPORT_NAME, getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "full_start"));
        //TODO other metrics will be added
        System.out.println(reportMetricsResults.toString());
    }

    @Test(dependsOnMethods = {"testTicketsCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewTicket() throws IOException, JSONException {
        createdZendeskTicketId = zendeskHelper.createNewTicket(
                String.format(JSON_TICKET_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewUser() throws IOException, JSONException {
        createdZendeskUserId = zendeskHelper.createNewUser(
                String.format(JSON_USER_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewOrganization() throws IOException, JSONException {
        createdZendeskOrganizationId = zendeskHelper.createNewOrganization(
                String.format(JSON_ORGANIZATION_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnGroups = {"newZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronization() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 1);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME).intValue() + 1,
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testUsersCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME).intValue() + 1,
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue() + 1,
                "Organizations count doesn't match after incremental sync");
    }

    /**
     * TODO
    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketEventsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        compareObjectsCount(getNumberFromGDReport(TICKET_EVENTS_REPORT_NAME), zendeskHelper.getNumberOfTicketEvents(),
                ZendeskHelper.ZendeskObject.TICKET_EVENT);
    }
    */

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "inc_added_objects"),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue() + 1,
                "Backlog tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testTicketsCountAfterIncrementalSync"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskTicket() throws IOException {
        zendeskHelper.deleteTicket(createdZendeskTicketId);
    }

    @Test(dependsOnMethods = {"testUsersCountAfterIncrementalSync"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskUser() throws IOException {
        zendeskHelper.deleteUser(createdZendeskUserId);
    }

    @Test(dependsOnMethods = {"testOrganizationsCountAfterIncrementalSync"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskOrganization() throws IOException {
        zendeskHelper.deleteOrganization(createdZendeskOrganizationId);
    }

    @Test(dependsOnGroups = {"deleteZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsDeletion() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 2);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testTicketsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME).intValue(),
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testUsersCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testOrganizationsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after incremental sync");
    }

    /**
     * TODO
    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testTicketEventsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        compareObjectsCount(getNumberFromGDReport(TICKET_EVENTS_REPORT_NAME), zendeskHelper.getNumberOfTicketEvents(),
                ZendeskHelper.ZendeskObject.TICKET_EVENT);
    }
    */

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testReportMetricsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "inc_after_delete"),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after ticket deleted");
    }

    @Test(dependsOnGroups = {"zendeskApiTests"}, groups = {"connectorWalkthrough"})
    public void testFullLoad() throws IOException, JSONException, InterruptedException {
        runConnectorProjectFullLoad();
    }

    @Test(dependsOnMethods = {"testFullLoad"}, groups = {"connectorWalkthrough"})
    public void testReportMetricsCountAfterFullLoad() throws IOException, JSONException, InterruptedException {
        assertEquals(getNumberFromGDReport(TICKETS_REPORT_NAME, "full_final"),
                reportMetricsResults.get(TICKETS_REPORT_NAME).intValue(),
                "Tickets count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(USERS_REPORT_NAME, "full_final"),
                reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "full_final"),
                reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "full_final"),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after full sync");
    }

    private void createBasicReport(String metric, String reportName) throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add(metric);
        createReport(reportName, ReportTypes.HEADLINE, what, null, reportName);
        waitForElementVisible(BY_ONE_NUMBER_REPORT, browser);
    }

    private int getNumberFromGDReport(String reportName, String screenshotName) {
        initReportsPage();
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        OneNumberReport report = Graphene.createPageFragment(OneNumberReport.class, browser.findElement(
                BY_ONE_NUMBER_REPORT));
        Screenshots.takeScreenshot(browser, reportName + "_" + screenshotName, this.getClass());
        System.out.println("Current value in report: " + report.getValue());
        return Integer.valueOf(report.getValue().replace(".00", "").replace(",", ""));
    }

    private void compareObjectsCount(int actual, int expected, ZendeskHelper.ZendeskObject objectName) {
        assertEquals(actual, expected, objectName.getPluralName() + " count don't match (against zendesk API)");
    }

    @Override
    public String openZendeskSettingsUrl() {
        openUrl(getIntegrationUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK, browser)).click();
        return browser.getCurrentUrl();
    }
}
