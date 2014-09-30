/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.entity.HowItem;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.enums.FilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.reports.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.assertEquals;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;

    private ZendeskHelper zendeskHelper;
    private boolean useApiProxy;

    private static final By TABLE_REPORT_CONTAINER_LOCATION = By.id("gridContainer");
    private static final By BY_ONE_NUMBER_REPORT = By.id("oneNumberContainer");
    private static final String EMPTY_VALUE = "(empty value)";
    private static Map<String, FieldChange> AFTER_CREATE_EVENTS = new HashMap<String, FieldChange>();
    private static Map<String, FieldChange> AFTER_UPDATE_EVENTS = new HashMap<String, FieldChange>();
    private static Map<String, FieldChange> AFTER_DELETE_EVENTS = new HashMap<String, FieldChange>();
    private static FieldChange TAGS_AFTER_FULL_LOAD;

    private static final String JSON_USER_CREATE = "{\"user\": {\"name\": \"GD test user\", \"email\": " +
            "\"qa+zendesk-test%s@gooddata.com\"}}";

    private static final String JSON_ORGANIZATION_CREATE =
            "{\"organization\": {\"name\": \"GD test organization - %s\"}}";

    private static final String TICKETS_REPORT_NAME = "Tickets count";
    private static final String USERS_REPORT_NAME = "Users count";
    private static final String ORGANIZATIONS_REPORT_NAME = "Organizations count";
    private static final String BACKLOG_TICKETS_REPORT_NAME = "Backlog Tickets count";
    private static final String TICKET_EVENTS_COUNT_REPORT_NAME = "Ticket events total count";
    private static final String TICKET_EVENTS_REPORT_NAME = "Ticket events for ticket ID";
    private static final String TICKET_TAGS_REPORT_NAME = "Ticket tags for ticket ID";

    private int createdZendeskTicketId;
    private int createdZendeskUserId;
    private int createdZendeskOrganizationId;
    private Map<String, Integer> reportMetricsResults;
    private Integer afterUpdateEventId;
    private Integer afterCreateEventId;
    private Integer afterDeleteEventId;
    private int totalEventsBaseCount;

    static {
        AFTER_CREATE_EVENTS.put("type", new FieldChange("question", EMPTY_VALUE));
        AFTER_CREATE_EVENTS.put("priority", new FieldChange("high", EMPTY_VALUE));
        AFTER_CREATE_EVENTS.put("tags", new FieldChange("first, second, to-be-deleted", "N/A", false));
        AFTER_CREATE_EVENTS.put("status", new FieldChange("new", EMPTY_VALUE));

        AFTER_UPDATE_EVENTS.put("type", new FieldChange("incident", "question"));
        AFTER_UPDATE_EVENTS.put("tags", new FieldChange("first, second", "first, second, to-be-deleted", false));
        AFTER_UPDATE_EVENTS.put("status", new FieldChange("open", "new"));

        AFTER_DELETE_EVENTS.put("status", new FieldChange("deleted", "open"));

        TAGS_AFTER_FULL_LOAD = new FieldChange("first, second", "N/A", false);
    }

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

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskTicketEventsReport() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String ticketEventsCountMetric = "# All Ticket Events";
        Map<String, String> data = new HashMap<String, String>();
        data.put("attribute0", "Ticket Text Field Change");
        data.put("attrFolder0", "Ticket Text Field Changes");
        data.put("attribute1", "Ticket Text Field Change");
        data.put("attrFolder1", "Ticket Text Field Changes");
        metricEditorPage.createAggregationMetric(MetricTypes.COUNT, ticketEventsCountMetric, data);

        List<String> what = new ArrayList<String>();
        what.add(ticketEventsCountMetric);
        createReport(TICKET_EVENTS_COUNT_REPORT_NAME, ReportTypes.HEADLINE, what, new ArrayList<String>(), TICKET_EVENTS_COUNT_REPORT_NAME);
        waitForElementVisible(BY_ONE_NUMBER_REPORT, browser);
    }

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

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskTicketEventsReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketEventsCount() throws IOException, JSONException, InterruptedException {
        totalEventsBaseCount = getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME, "full_start"); //TODO: this is not a test :)
    }

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
                String.format(createJson(AFTER_CREATE_EVENTS), ZendeskHelper.getCurrentTimeIdentifier()));
    }

    private String createJson(Map<String, FieldChange> expectedEvents) {
        StringBuilder json = new StringBuilder();
        json.append("{\"ticket\": {");
        json.append("\"subject\": \"GD test ticket - %s\", ");
        json.append("\"comment\": { \"body\": \"Description of automatically created ticket\"}");
        for (String fieldName : expectedEvents.keySet()) {
            json.append(", \"").append(fieldName).append("\": \"").append(expectedEvents.get(fieldName).newValue).append("\" ");
        }
        json.append("}}");

        return json.toString();
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

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testTicketsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME) + 1,
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testUsersCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME) + 1,
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "inc_added_objects");
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME) + 1,
                "Organizations count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testTicketEventsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        createTicketEventsReport(createdZendeskTicketId);
        createTicketTagsReport(createdZendeskTicketId);

        afterCreateEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId, DateTime.now().minusMinutes(10));
        checkTicketEventsReport(createdZendeskTicketId, afterCreateEventId, AFTER_CREATE_EVENTS);
        Screenshots.takeScreenshot(browser, "ticket-events-after-create-ticket-report", this.getClass());
        checkTicketTagsReport(createdZendeskTicketId, AFTER_CREATE_EVENTS.get("tags"));
        Screenshots.takeScreenshot(browser, "ticket-tags-after-create-ticket-report", this.getClass());
    }

    private void createTicketEventsReport(int ticketId) throws InterruptedException {
        List<HowItem> how = new ArrayList<HowItem>();
        how.add(new HowItem("Ticket Text Field Change", HowItem.Position.LEFT));
        how.add(new HowItem("[Text Field] New Value", HowItem.Position.LEFT));
        how.add(new HowItem("[Text Field] Previous Value", HowItem.Position.LEFT));
        how.add(new HowItem("Ticket Updates", HowItem.Position.LEFT));
        how.add(new HowItem("Ticket Id", HowItem.Position.LEFT));

        createReportTmp(TICKET_EVENTS_REPORT_NAME, ReportTypes.TABLE, new ArrayList<String>(), how, TICKET_EVENTS_REPORT_NAME);
        Map<String, String> data = new HashMap<String, String>();
        data.put("attribute", "Ticket Id");
        data.put("attributeElements", String.valueOf(ticketId));
        reportPage.addFilter(FilterTypes.ATTRIBUTE, data);
        reportPage.saveReport();
        waitForElementVisible(TABLE_REPORT_CONTAINER_LOCATION, browser);
    }

    private void createTicketTagsReport(int ticketId) throws InterruptedException {
        List<HowItem> how = new ArrayList<HowItem>();
        how.add(new HowItem("Ticket Tag", HowItem.Position.LEFT));
        how.add(new HowItem("Ticket Tag Deleted Flag", HowItem.Position.LEFT));
        how.add(new HowItem("Ticket Id", HowItem.Position.LEFT));

        createReportTmp(TICKET_TAGS_REPORT_NAME, ReportTypes.TABLE, new ArrayList<String>(), how, TICKET_TAGS_REPORT_NAME);
        Map<String, String> data = new HashMap<String, String>();
        data.put("attribute", "Ticket Id");
        data.put("attributeElements", String.valueOf(ticketId));
        reportPage.addFilter(FilterTypes.ATTRIBUTE, data);
        reportPage.saveReport();
        waitForElementVisible(TABLE_REPORT_CONTAINER_LOCATION, browser);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterIncrementalSync() throws IOException, JSONException, InterruptedException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "inc_added_objects"),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME) + 1,
                "Backlog tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnGroups = {"zendeskAfterCreateTests"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "updateZendeskObjects"})
    public void updateZendeskTicket() throws IOException, JSONException, InterruptedException {
        zendeskHelper.updateTicket(createdZendeskTicketId, updateJson(AFTER_UPDATE_EVENTS));
    }

    private String updateJson(Map<String, FieldChange> expectedEvents) {
        String comma = "";
        StringBuilder json = new StringBuilder();
        json.append("{\"ticket\": {");
        for (String fieldName : expectedEvents.keySet()) {
            json.append(comma).append("\"").append(fieldName).append("\": \"").append(expectedEvents.get(fieldName).newValue).append("\" ");
            comma = ",";
        }
        json.append("}}");

        return json.toString();
    }

    @Test(dependsOnGroups = {"updateZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsUpdate() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 2);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsUpdate"}, groups = {"zendeskApiTests",
            "zendeskAfterUpdateTests", "connectorWalkthrough"})
    public void testTicketEventsCountAfterTicketUpdate() throws IOException, JSONException, InterruptedException {
        afterUpdateEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId, DateTime.now().minusMinutes(10));
        checkTicketEventsReport(createdZendeskTicketId, afterUpdateEventId, AFTER_UPDATE_EVENTS);
        Screenshots.takeScreenshot(browser, "ticket-events-after-update-ticket-report", this.getClass());
        checkTicketTagsReport(createdZendeskTicketId, AFTER_UPDATE_EVENTS.get("tags"));
        Screenshots.takeScreenshot(browser, "ticket-tags-after-update-ticket-report", this.getClass());
    }

    @Test(dependsOnGroups = {"zendeskAfterUpdateTests"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskTicket() throws IOException {
        zendeskHelper.deleteTicket(createdZendeskTicketId);
    }

    @Test(dependsOnGroups = {"zendeskAfterUpdateTests"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskUser() throws IOException {
        zendeskHelper.deleteUser(createdZendeskUserId);
    }

    @Test(dependsOnGroups = {"zendeskAfterUpdateTests"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskOrganization() throws IOException {
        zendeskHelper.deleteOrganization(createdZendeskOrganizationId);
    }

    @Test(dependsOnGroups = {"deleteZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsDeletion() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 3);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testTicketsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME).intValue(),
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testUsersCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME, "inc_after_delete");
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after incremental sync");
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testTicketEventsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        afterDeleteEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId, DateTime.now().minusMinutes(10));
        checkTicketEventsReport(createdZendeskTicketId, afterDeleteEventId, AFTER_DELETE_EVENTS);
        Screenshots.takeScreenshot(browser, "ticket-events-after-delete-ticket-report", this.getClass());

        assertEquals(getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME, "after_incremental"),
                totalEventsBaseCount + ticketEventsChangesCount(AFTER_CREATE_EVENTS, AFTER_UPDATE_EVENTS, AFTER_DELETE_EVENTS),
                "Total count of TicketEvents after tests is different than expected.");
    }

    private int ticketEventsChangesCount(Map<String, FieldChange>... ticketEvents) {
        int changesCount = 1; // Everytime there is "Organization" field change, TODO: what if some new field to ticket form is added?

        for (Map<String, FieldChange> changes : ticketEvents) {
            for (String fieldName : changes.keySet()) {
                if (changes.get(fieldName).toBeChecked) {
                    changesCount++;
                }
            }
        }

        return changesCount;
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterDeletion() throws IOException, JSONException, InterruptedException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME, "inc_after_delete"),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after ticket deleted");
    }

    @Test(dependsOnGroups = {"zendeskAfterDeletionTests"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testFullLoad() throws IOException, JSONException, InterruptedException {
        runConnectorProjectFullLoad();
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"testFullLoad"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
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

        checkTicketEventsReport(createdZendeskTicketId, afterCreateEventId, AFTER_CREATE_EVENTS);
        checkTicketEventsReport(createdZendeskTicketId, afterUpdateEventId, AFTER_UPDATE_EVENTS);
        checkTicketEventsReport(createdZendeskTicketId, afterDeleteEventId, AFTER_DELETE_EVENTS);
        Screenshots.takeScreenshot(browser, "ticket-events-after-full-load-report", this.getClass());

        assertEquals(getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME, "after_full_load"),
                totalEventsBaseCount + ticketEventsChangesCount(AFTER_CREATE_EVENTS, AFTER_UPDATE_EVENTS, AFTER_DELETE_EVENTS),
                "Total count of TicketEvents after tests is different than expected.");

        checkTicketTagsReport(createdZendeskTicketId, TAGS_AFTER_FULL_LOAD);
    }

    private void createBasicReport(String metric, String reportName) throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add(metric);
        createReport(reportName, ReportTypes.HEADLINE, what, new ArrayList<String>(), reportName);
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

    private void checkTicketEventsReport(int ticketId, int ticketEventId, Map<String, FieldChange> expectedValues) {
        Map<String, FieldChange> reportValues = parseTicketEventFromReport(String.valueOf(ticketId), String.valueOf(ticketEventId));

        for (String fieldName : expectedValues.keySet()) {
            FieldChange expectedField = expectedValues.get(fieldName);

            if (expectedField.toBeChecked) {
                FieldChange reportField = reportValues.get(fieldName);
                assertNotNull(reportField, "Report does not contain changed field \"" + fieldName + "\"");
                assertEquals(reportField.newValue, expectedField.newValue, "Report has incorrect NEW value for field \"" + fieldName + "\"");
                assertEquals(reportField.oldValue, expectedField.oldValue, "Report has incorrect OLD value for field \"" + fieldName + "\"");
            }
        }
    }

    private Map<String, FieldChange> parseTicketEventFromReport(String ticketIdString, String ticketEventIdString) {
        initReportsPage();
        reportsPage.getReportsList().openReport(TICKET_EVENTS_REPORT_NAME);
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        TableReport report = Graphene.createPageFragment(TableReport.class, browser.findElement(TABLE_REPORT_CONTAINER_LOCATION));

        List<String> cellValues = report.getAttributeElements();
        int i = 0;
        Map<String, FieldChange> actualValues = new HashMap<String, FieldChange>();
        String reportTicketEventId;
        String reportTicketId;
        String fieldName;
        String newValue;
        String oldValue;

        try {
            while (i < cellValues.size()) { // format of report: "28779297:status", "[Status] open", "[Status] new", "reportTicketEventId", "reportTicketId"
                fieldName = cellValues.get(i);
                fieldName = fieldName.substring(fieldName.indexOf(":") + 1); // format: 28779297:status
                i++;
                newValue = cellValues.get(i);
                newValue = newValue.substring(newValue.indexOf("]") + 2); // format: [Status] open
                i++;
                oldValue = cellValues.get(i);
                if (oldValue.contains("] ")) {
                    oldValue = oldValue.substring(oldValue.indexOf("]") + 2); // format: [Status] new
                }
                i++;
                reportTicketEventId = cellValues.get(i);
                i++;
                reportTicketId = cellValues.get(i);
                i++;

                if (reportTicketId.equals(ticketIdString) && reportTicketEventId.equals(ticketEventIdString)) {
                    actualValues.put(fieldName, new FieldChange(newValue, oldValue));
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            fail("Ticket Event report has incorrect count of cells.");
        }

        return actualValues;
    }

    private void checkTicketTagsReport(int ticketId, FieldChange tagsChanges) {
        Map<String, Boolean> reportValues = parseTagsChangesFromReport(String.valueOf(ticketId));

        Set<String> newTags = new HashSet<String>();
        String tagName;
        for (String tag : tagsChanges.newValue.split(",")) {
            tagName = tag.trim();
            newTags.add(tagName);
            assertNotNull(reportValues.get(tagName), "Report does not contain tag \"" + tagName + "\"");
            assertFalse(reportValues.get(tagName), "Tag \"" + tagName + "\" must not be deleted.");
        }

        for (String tag : tagsChanges.oldValue.split(",")) {
            tagName = tag.trim();
            if (!newTags.contains(tagName)) {
                assertNotNull(reportValues.get(tagName), "Report does not contain tag \"" + tagName + "\"");
                assertTrue(reportValues.get(tagName), "Tag \"" + tagName + "\" has to be deleted.");
            }
        }
    }

    private Map<String, Boolean> parseTagsChangesFromReport(final String ticketIdString) {
        initReportsPage();
        reportsPage.getReportsList().openReport(TICKET_TAGS_REPORT_NAME);
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        TableReport report = Graphene.createPageFragment(TableReport.class, browser.findElement(TABLE_REPORT_CONTAINER_LOCATION));

        List<String> cellValues = report.getAttributeElements();
        int i = 0;
        Map<String, Boolean> actualValues = new HashMap<String, Boolean>();

        try {
            while (i < cellValues.size()) { // format: "tag-name", "false", "ticketId"
                if (cellValues.get(i + 2).equals(ticketIdString)) {
                    actualValues.put(cellValues.get(i), Boolean.parseBoolean(cellValues.get(i + 1)));
                }
                i += 3;
            }
        } catch (IndexOutOfBoundsException exception) {
            fail("Tags report has incorrect count of cells.");
        }

        return actualValues;
    }

    private static class FieldChange {
        public final String newValue;
        public final String oldValue;
        public boolean toBeChecked = true;

        public FieldChange(String newValue, String oldValue) {
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        public FieldChange(String newValue, String oldValue, boolean toBeChecked) {
            this(newValue, oldValue);
            this.toBeChecked = toBeChecked;
        }
    }

}
