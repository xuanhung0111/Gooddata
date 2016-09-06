/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors.zendesk;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.OneNumberReportDefinitionContent;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.connectors.ZendeskHelper;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.report.ReportExportFormat;
import com.gooddata.report.ReportService;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@SuppressWarnings("serial")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;

    private ZendeskHelper zendeskHelper;
    private boolean useApiProxy;

    private static final By TABLE_REPORT_CONTAINER_LOCATION = By.id("gridContainer");
    private static final String EMPTY_VALUE = "";

    private static Map<String, FieldChange> AFTER_TICKET_CREATE_EVENTS = new HashMap<String, FieldChange>() {{
        put("type", new FieldChange("Ticket Type", "question", EMPTY_VALUE));
        put("priority", new FieldChange("Priority", "high", EMPTY_VALUE));
        put("organization", new FieldChange("Organization", "GoodData QA", EMPTY_VALUE, true, false));
        put("group", new FieldChange("Group", "Support", EMPTY_VALUE, true, false));
        put("tags", new FieldChange("Tags", "first, second, to-be-deleted", "N/A", false, true));
        put("status", new FieldChange("Status", "new", EMPTY_VALUE));
    }};

    private static Map<String, FieldChange> AFTER_TICKET_UPDATE_EVENTS = new HashMap<String, FieldChange>() {{
        put("type", new FieldChange("Ticket Type", "incident", "question"));
        put("tags", new FieldChange("Tags", "first, second", "first, second, to-be-deleted", false, true));
        put("status", new FieldChange("Status", "open", "new"));
    }};

    private static Map<String, FieldChange> AFTER_TICKET_DELETE_EVENTS = new HashMap<String, FieldChange>() {{
        put("status", new FieldChange("Status", "deleted", "open"));
    }};

    private static FieldChange TAGS_AFTER_FULL_LOAD = new FieldChange("Tags", "first, second", "N/A", false, true);

    private static final String JSON_USER_CREATE = getResourceAsString("/zendesk-api/user-create.json");

    private static final String JSON_ORGANIZATION_CREATE = getResourceAsString("/zendesk-api/organization-create.json");

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
    private Integer afterTicketCreateEventId;
    private Integer afterTicketUpdateEventId;
    private Integer afterTicketDeleteEventId;
    private int totalEventsBaseCount;

    private MetadataService mdService = null;
    private Project project = null;

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = testParams.loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = testParams.loadProperty("connectors.zendesk4.uploadUser");
        zendeskUploadUserPassword = testParams.loadProperty("connectors.zendesk4.uploadUserPassword");

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Insights - View Only", new String[]{
                "Overview", "Tickets", "Satisfaction", "Efficiency", "Agent Activity", "SLAs", "Learn More"
        });
        expectedDashboardsAndTabs.put("My dashboard", new String[]{"First Tab"});
        zendeskAPIUser = testParams.loadProperty("connectors.zendesk.apiUser");
        zendeskAPIPassword = testParams.loadProperty("connectors.zendesk.apiUserPassword");
        useApiProxy = Boolean.parseBoolean(testParams.loadProperty("http.client.useApiProxy"));

        reportMetricsResults = new HashMap<String, Integer>();
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void initializeGoodDataSDK() {
        goodDataClient = getGoodDataClient();
        mdService = goodDataClient.getMetadataService();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskTicketsReport() throws IOException {
        createOneNumberReportDefinition(TICKETS_REPORT_NAME, "# Tickets");
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskUsersReport() throws IOException {
        createOneNumberReportDefinition(USERS_REPORT_NAME, "# Users");
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskOrganizationsReport() throws IOException {
        createOneNumberReportDefinition(ORGANIZATIONS_REPORT_NAME, "# Organizations");
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskTicketEventsCountReport() throws IOException {
        String attribute = mdService.getObjUri(project, com.gooddata.md.Attribute.class,
                Restriction.identifier("attr.ticketattributeshistory.ticketattributeshistory"));
        Metric metric = mdService.createObj(project, new Metric("# All Ticket Events",
                "SELECT COUNT([" + attribute + "])", "#,##0"));
        createOneNumberReportDefinition(TICKET_EVENTS_COUNT_REPORT_NAME, metric);
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createReportsForMetrics() throws IOException {
        createOneNumberReportDefinition(BACKLOG_TICKETS_REPORT_NAME, "# Backlog Tickets");
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
    public void testTicketsCount() throws IOException, JSONException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME);
        reportMetricsResults.put(TICKETS_REPORT_NAME, gdTicketsCount);
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskUsersReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testUsersCount() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        reportMetricsResults.put(USERS_REPORT_NAME, gdUsersCount);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(), ZendeskHelper.ZendeskObject.USER);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskOrganizationsReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCount() throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        reportMetricsResults.put(ORGANIZATIONS_REPORT_NAME, gdOrganizationsCount);
        // Zendesk 4 project contains one dummy organization - ATP-2954
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations() + 1,
                ZendeskHelper.ZendeskObject.ORGANIZATION);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskTicketEventsCountReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketEventsCount() throws IOException, JSONException {
        totalEventsBaseCount = getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createReportsForMetrics"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testReportMetricsCount() throws IOException, JSONException {
        reportMetricsResults.put(BACKLOG_TICKETS_REPORT_NAME, getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME));
        //TODO other metrics will be added
        System.out.println(reportMetricsResults.toString());
    }

    @Test(dependsOnMethods = {"testTicketsCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewTicket() throws IOException, JSONException {
        createdZendeskTicketId = zendeskHelper.createNewTicket(
                format(createTicketJson(AFTER_TICKET_CREATE_EVENTS), ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewUser() throws IOException, JSONException {
        createdZendeskUserId = zendeskHelper.createNewUser(
                format(JSON_USER_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewOrganization() throws IOException, JSONException {
        createdZendeskOrganizationId = zendeskHelper.createNewOrganization(
                format(JSON_ORGANIZATION_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnGroups = {"newZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronization() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testTicketsCountAfterIncrementalSync() throws IOException, JSONException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME);
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME) + 1,
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testUsersCountAfterIncrementalSync() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME) + 1,
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterIncrementalSync()
            throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        compareObjectsCount(gdOrganizationsCount,
                zendeskHelper.getNumberOfOrganizations() + 1, // + 1 dummy organization - ATP-2954
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME) + 1,
                "Organizations count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"},
            groups = {"zendeskApiTests", "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testTicketEventsAfterIncrementalSync()
            throws IOException, JSONException {
        createTicketTagsReport(createdZendeskTicketId);

        afterTicketCreateEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId,
                DateTime.now().minusMinutes(10));
        checkTicketEventsReport(afterTicketCreateEventId, AFTER_TICKET_CREATE_EVENTS);
        checkTicketTagsReport(createdZendeskTicketId, AFTER_TICKET_CREATE_EVENTS.get("tags"));
        Screenshots.takeScreenshot(browser, "ticket-tags-after-create-ticket-report", this.getClass());
    }

    private void createReportTicketIdFilter(int ticketId) {
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Ticket Id", String.valueOf(ticketId)))
            .saveReport();
        waitForElementVisible(TABLE_REPORT_CONTAINER_LOCATION, browser);
    }

    private void createTicketTagsReport(int ticketId) {
        createReport(new UiReportDefinition().withName(TICKET_TAGS_REPORT_NAME)
                                           .withHows(new HowItem("Ticket Tag", HowItem.Position.LEFT))
                                           .withHows(new HowItem("Ticket Tag Deleted Flag", HowItem.Position.LEFT))
                                           .withHows(new HowItem("Ticket Id", HowItem.Position.LEFT)),
                     TICKET_TAGS_REPORT_NAME);
        createReportTicketIdFilter(ticketId);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterIncrementalSync()
            throws IOException, JSONException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnGroups = {"zendeskAfterCreateTests"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "updateZendeskObjects"})
    public void updateZendeskTicket() throws IOException, JSONException {
        zendeskHelper.updateTicket(createdZendeskTicketId, updateTicketJson(AFTER_TICKET_UPDATE_EVENTS));
    }

    @Test(dependsOnGroups = {"updateZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsUpdate() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsUpdate"},
            groups = {"zendeskApiTests", "zendeskAfterUpdateTests", "connectorWalkthrough"})
    public void testTicketEventsAfterTicketUpdate() throws IOException, JSONException {
        afterTicketUpdateEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId,
                DateTime.now().minusMinutes(10));
        checkTicketEventsReport(afterTicketUpdateEventId, AFTER_TICKET_UPDATE_EVENTS);
        checkTicketTagsReport(createdZendeskTicketId, AFTER_TICKET_UPDATE_EVENTS.get("tags"));
        Screenshots.takeScreenshot(browser, "ticket-tags-after-update-ticket-report", this.getClass());
    }

    @Test(dependsOnMethods = { "testTicketEventsAfterTicketUpdate" },
            groups = {"zendeskApiTests", "connectorWalkthrough", "deleteZendeskObjects"})
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
    public void testIncrementalSynchronizationAfterObjectsDeletion() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testTicketsCountAfterDeletion() throws IOException, JSONException {
        int gdTicketsCount = getNumberFromGDReport(TICKETS_REPORT_NAME);
        compareObjectsCount(gdTicketsCount, zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.ZendeskObject.TICKET);
        assertEquals(gdTicketsCount, reportMetricsResults.get(TICKETS_REPORT_NAME).intValue(),
                "Tickets count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testUsersCountAfterDeletion() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(), ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterDeletion() throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        compareObjectsCount(gdOrganizationsCount,
                zendeskHelper.getNumberOfOrganizations() + 1, // + 1 dummy organization - ATP-2954
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after incremental sync");
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"},
            groups = {"zendeskApiTests", "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testTicketEventsAfterDeletion() throws IOException, JSONException {
        afterTicketDeleteEventId = zendeskHelper.loadLastTicketEventId(createdZendeskTicketId,
                DateTime.now().minusMinutes(10));
        checkTicketEventsReport(afterTicketDeleteEventId, AFTER_TICKET_DELETE_EVENTS);

        assertEquals(getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME),
                totalEventsBaseCount + ticketEventChangesCount(AFTER_TICKET_CREATE_EVENTS,
                        AFTER_TICKET_UPDATE_EVENTS, AFTER_TICKET_DELETE_EVENTS),
                "Total count of TicketEvents after tests is different than expected.");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterDeletion() throws IOException, JSONException {
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after ticket deleted");
    }

    @Test(dependsOnGroups = {"zendeskAfterDeletionTests"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testFullLoad() throws IOException, JSONException {
        runConnectorProjectFullLoad();
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"testFullLoad"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testReportMetricsCountAfterFullLoad() throws IOException, JSONException {
        assertEquals(getNumberFromGDReport(TICKETS_REPORT_NAME),
                reportMetricsResults.get(TICKETS_REPORT_NAME).intValue(),
                "Tickets count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(USERS_REPORT_NAME),
                reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME),
                reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after full sync");
        assertEquals(getNumberFromGDReport(BACKLOG_TICKETS_REPORT_NAME),
                reportMetricsResults.get(BACKLOG_TICKETS_REPORT_NAME).intValue(),
                "Backlog tickets count doesn't match after full sync");

        checkTicketEventsReport(afterTicketCreateEventId, AFTER_TICKET_CREATE_EVENTS);
        checkTicketEventsReport(afterTicketUpdateEventId, AFTER_TICKET_UPDATE_EVENTS);
        checkTicketEventsReport(afterTicketDeleteEventId, AFTER_TICKET_DELETE_EVENTS);

        assertEquals(getNumberFromGDReport(TICKET_EVENTS_COUNT_REPORT_NAME),
                totalEventsBaseCount + ticketEventChangesCount(AFTER_TICKET_CREATE_EVENTS,
                        AFTER_TICKET_UPDATE_EVENTS, AFTER_TICKET_DELETE_EVENTS),
                "Total count of TicketEvents after tests is different than expected.");

        checkTicketTagsReport(createdZendeskTicketId, TAGS_AFTER_FULL_LOAD);
    }

    private void createOneNumberReportDefinition(String reportName, String metricTitle) {
        Metric metric = mdService.getObj(project, Metric.class, Restriction.title(metricTitle));
        createOneNumberReportDefinition(reportName, metric);
    }

    private void createOneNumberReportDefinition(String reportName, Metric metric) {
        if (mdService.find(project, ReportDefinition.class, Restriction.title(reportName)).isEmpty()) {
            ReportDefinition definition = 
                    OneNumberReportDefinitionContent.create(reportName, asList(METRIC_GROUP),
                            Collections.<GridElement>emptyList(),
                            singletonList(new MetricElement(metric)));
            mdService.createObj(project, definition);
        } else {
            System.out.println("Required report definition already exists: " + reportName);
        }
    }

    private int getNumberFromGDReport(String reportName) throws IOException {
        final ByteArrayOutputStream output = exportReport(reportName);

        ICsvListReader listReader = null;
        try {
            listReader = getCsvListReader(output);

            List<String> reportResult = listReader.read();
            return Integer.valueOf(reportResult.get(1));
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }

    private ByteArrayOutputStream exportReport(String reportName) {
        return exportReport(mdService.getObj(project, ReportDefinition.class, Restriction.title(reportName)));
    }

    private ByteArrayOutputStream exportReport(ReportDefinition rd) {
        ReportService reportService = goodDataClient.getReportService();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportReport(rd, ReportExportFormat.CSV, output).get();

        System.out.println("Going to read a csv file with following content: \n" + output.toString());

        return output;
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

    private String createTicketJson(final Map<String, FieldChange> expectedEvents) {
        try {
            return new JSONObject() {{
                put("ticket", new JSONObject() {{
                    put("subject", "GD test ticket - %s");
                    put("comment", new JSONObject() {{
                        put("body", "Description of automatically created ticket");
                    }});
                    for (Map.Entry<String, FieldChange> expectedEvent: expectedEvents.entrySet()) {
                        final String fieldName = expectedEvent.getKey();
                        final FieldChange fieldChange = expectedEvent.getValue();
                        if (fieldChange.toBeSend) {
                            put(fieldName, fieldChange.newValue);
                        }
                    }
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization!", e);
        }
    }

    private String updateTicketJson(final Map<String, FieldChange> expectedEvents) {
        try {
            return new JSONObject() {{
                put("ticket", new JSONObject() {{
                    for (Map.Entry<String, FieldChange> expectedEvent: expectedEvents.entrySet()) {
                        final String fieldName = expectedEvent.getKey();
                        final FieldChange fieldChange = expectedEvent.getValue();
                        if (fieldChange.toBeSend) {
                            put(fieldName, fieldChange.newValue);
                        }
                    }
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization!", e);
        }
    }

    private int ticketEventChangesCount(@SuppressWarnings("unchecked") Map<String, FieldChange>... ticketEvents) {
        // TODO: what if some new field to ticket form is added?
        int changesCount = 0;

        for (Map<String, FieldChange> changes : ticketEvents) {
            for (String fieldName : changes.keySet()) {
                if (changes.get(fieldName).toBeChecked) {
                    changesCount++;
                }
            }
        }

        return changesCount;
    }

    private void checkTicketEventsReport(int ticketEventId,
            Map<String, FieldChange> expectedValues) throws IOException {
        Map<String, FieldChange> reportValues = parseTicketEventFromReport(String.valueOf(ticketEventId));

        for (String zendeskFieldName : expectedValues.keySet()) {
            FieldChange expectedReportField = expectedValues.get(zendeskFieldName);

            if (expectedReportField.toBeChecked) {
                FieldChange reportField = reportValues.get(expectedReportField.fieldAlias);
                assertNotNull(reportField, "Report does not contain changed field \"" + zendeskFieldName + "\"");
                assertEquals(reportField.newValue, expectedReportField.newValue,
                        "Report has incorrect NEW value for field \"" + zendeskFieldName + "\"");
                assertEquals(reportField.oldValue, expectedReportField.oldValue,
                        "Report has incorrect OLD value for field \"" + zendeskFieldName + "\"");
            }
        }
    }

    private ReportDefinition createZendeskTicketEventsReport(final String eventId) {
        final Attribute textFieldAttr = mdService.getObj(project, Attribute.class, Restriction.title("Text Field"));
        final Attribute newValAttr = mdService.getObj(project, Attribute.class, Restriction.title("[Text Field] New Value"));
        final Attribute oldValAttr = mdService.getObj(project, Attribute.class, Restriction.title("[Text Field] Previous Value"));
        final Attribute eventAttr = mdService.getObj(project, Attribute.class, Restriction.title("Ticket Updates"));
        final Attribute ticketIdAttr = mdService.getObj(project, Attribute.class, Restriction.title("Ticket Id"));

        final Collection<AttributeElement> filteredElems
                = Collections2.filter(mdService.getAttributeElements(eventAttr), new Predicate<AttributeElement>() {
            @Override
            public boolean apply(AttributeElement attributeElement) {
                return eventId.equals(attributeElement.getTitle());
            }
        });

        assertEquals(filteredElems.size(), 1, "should find exactly on attribute element for event attribute");

        final AttributeElement eventIdElem = filteredElems.iterator().next();

        return mdService.createObj(project, GridReportDefinitionContent.create(
                TICKET_EVENTS_REPORT_NAME + "_by_" + eventId,
                Collections.<GridElement>emptyList(),
                asList(new AttributeInGrid(textFieldAttr.getDefaultDisplayForm().getUri(), textFieldAttr.getTitle()),
                        new AttributeInGrid(newValAttr.getDefaultDisplayForm().getUri(), newValAttr.getTitle()),
                        new AttributeInGrid(oldValAttr.getDefaultDisplayForm().getUri(), oldValAttr.getTitle()),
                        new AttributeInGrid(eventAttr.getDefaultDisplayForm().getUri(), eventAttr.getTitle()),
                        new AttributeInGrid(ticketIdAttr.getDefaultDisplayForm().getUri(), ticketIdAttr.getTitle())),
                Collections.<MetricElement>emptyList(),
                singletonList(new Filter(String.format("(SELECT [%s]) IN ([%s])", eventAttr.getUri(), eventIdElem.getUri())))));
    }

    private Map<String, FieldChange> parseTicketEventFromReport(String ticketEventIdString) throws IOException {

        final ByteArrayOutputStream output = exportReport(createZendeskTicketEventsReport(ticketEventIdString));

        ICsvListReader listReader = null;
        try {
            listReader = getCsvListReader(output);

            final Map<String, FieldChange> actualValues = new HashMap<String, FieldChange>();
            List<String> reportResult;

            System.out.println("fieldNameAlias|newValue|oldValue");
            while ((reportResult = listReader.read()) != null) {
                if (reportResult.size() < 5) {
                    fail("Ticket Event report has incorrect count of cells.");
                }
                final String fieldNameAlias = StringUtils.trimToEmpty(reportResult.get(0));
                String newValue = StringUtils.trimToEmpty(reportResult.get(1));
                if (newValue.contains("] ")) {
                    newValue = newValue.substring(newValue.indexOf("]") + 2); // format: [Status] open;
                }
                String oldValue = StringUtils.trimToEmpty(reportResult.get(2));
                if (oldValue.contains("] ")) {
                    oldValue = oldValue.substring(oldValue.indexOf("]") + 2); // format: [Status] new
                }
                final String reportTicketEventId = StringUtils.trimToEmpty(reportResult.get(3));
                if (reportTicketEventId.equals(ticketEventIdString)) {
                    System.out.println(fieldNameAlias + "|" + newValue + "|" + oldValue);
                    actualValues.put(fieldNameAlias, new FieldChange(fieldNameAlias, newValue, oldValue));
                }
            }
            return actualValues;
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }

    private static CsvListReader getCsvListReader(ByteArrayOutputStream output) throws IOException {
        final CsvListReader reader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray())),
                CsvPreference.STANDARD_PREFERENCE);
        reader.getHeader(true);
        return reader;
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
        initReportsPage().openReport(TICKET_TAGS_REPORT_NAME);
        TableReport report = Graphene.createPageFragment(TableReport.class,
                browser.findElement(TABLE_REPORT_CONTAINER_LOCATION));

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
            exception.printStackTrace();
            fail("Tags report has incorrect count of cells.");
        }

        return actualValues;
    }

    private static class FieldChange {
        private final String fieldAlias;
        private final String newValue;
        private final String oldValue;
        private final boolean toBeChecked;
        private final boolean toBeSend;

        public FieldChange(String fieldAlias, String newValue, String oldValue) {
            this(fieldAlias, newValue, oldValue, true, true);
        }

        public FieldChange(String fieldAlias, String newValue, String oldValue, boolean toBeChecked, boolean toBeSend) {
            this.fieldAlias = fieldAlias;
            this.newValue = newValue;
            this.oldValue = oldValue;
            this.toBeChecked = toBeChecked;
            this.toBeSend = toBeSend;
        }
    }
}
