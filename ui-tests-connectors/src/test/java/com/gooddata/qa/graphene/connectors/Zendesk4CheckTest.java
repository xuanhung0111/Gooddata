/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.OneNumberReport;
import com.gooddata.qa.utils.http.RestApiClient;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

@Test(groups = {"connectors", "zendesk4"}, description = "Checklist tests for Zendesk4 REST API")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;

    private ZendeskHelper zendeskHelper;

    private static final By BY_ONE_NUMBER_REPORT = By.id("oneNumberContainer");

    private static final String JSON_TICKET_CREATE = "{\"ticket\":{\"subject\":\"GD test ticket\", " +
            "\"comment\": { \"body\": \"Description of automatically created ticket\" }}}";

    private static final String JSON_USER_CREATE = "{\"user\": {\"name\": \"GD test user\", \"email\": " +
            "\"qa+zendesk-test@gooddata.com\"}}";

    private static final String JSON_ORGANIZATION_CREATE = "{\"organization\": {\"name\": \"GD test organization\"}}";

    private static final String TICKETS_REPORT_NAME = "Tickets count";
    private static final String USERS_REPORT_NAME = "Users count";
    private static final String ORGANIZATIONS_REPORT_NAME = "Organizations count";

    @BeforeClass
    public void loadRequiredProperties() {
        super.loadRequiredProperties();

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Agent Performance", new String[]{
                "Public comments", "Comments by hour", "Group reassignments", "Comments by group"
        });
        expectedDashboardsAndTabs.put("Customer Support", new String[]{
                "Open tickets", "Problem tickets", "Time metrics", "Channels", "Ticket trends", "(Beta) Explorers",
                "Monthly"
        });
        expectedDashboardsAndTabs.put("Events", new String[]{
                "Events", "Update channel", "Satisfaction"
        });
        expectedDashboardsAndTabs.put("Users & Organizations", new String[]{
                "Users created", "Last login", "Tickets by user", "Top time zones"
        });
        zendeskAPIUser = loadProperty("connectors.zendesk.apiUser");
        zendeskAPIPassword = loadProperty("connectors.zendesk.apiUserPassword");
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskTicketsReport() throws InterruptedException {
        createBasicReport("Count tickets", TICKETS_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskUsersReport() throws InterruptedException {
        createBasicReport("Count users", USERS_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"connectorWalkthrough"})
    public void createZendeskOrganizationsReports() throws InterruptedException {
        createBasicReport("Count organizations", ORGANIZATIONS_REPORT_NAME);
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void initZendeskApiClient() {
        if (zendeskApiUrl.contains("staging") && !zendeskAPIUser.isEmpty() && !zendeskAPIPassword.isEmpty()) {
            zendeskHelper = new ZendeskHelper(
                new RestApiClient(zendeskApiUrl.replace("https://", ""), zendeskAPIUser, zendeskAPIPassword, false));
        } else {
            fail("Zendesk staging API is not used, tests for adding new objects will be skipped");
        }
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskTicketsReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testTicketsCount() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(TICKETS_REPORT_NAME), zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.TICKET_OBJECT_NAME);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskUsersReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testUsersCount() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(USERS_REPORT_NAME), zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.USER_OBJECT_NAME);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskOrganizationsReports"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCount() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME), zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ORGANIZATION_OBJECT_NAME);
    }

    @Test(dependsOnMethods = {"testTicketsCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewTicket() throws IOException, JSONException {
        zendeskHelper.createNewTicket(JSON_TICKET_CREATE);
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewUser() throws IOException, JSONException {
        zendeskHelper.createNewUser(JSON_USER_CREATE);
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewOrganization() throws IOException, JSONException {
        zendeskHelper.createNewOrganization(JSON_ORGANIZATION_CREATE);
    }

    @Test(dependsOnGroups = {"newZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronization() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 1);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testTicketsCountAfterIncrementalSync() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(TICKETS_REPORT_NAME), zendeskHelper.getNumberOfTickets(),
                ZendeskHelper.TICKET_OBJECT_NAME);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testUsersCountAfterIncrementalSync() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(USERS_REPORT_NAME), zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.USER_OBJECT_NAME);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterIncrementalSync() throws IOException, JSONException {
        compareObjectsCount(getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME), zendeskHelper.getNumberOfOrganizations(),
                ZendeskHelper.ORGANIZATION_OBJECT_NAME);
    }

    private void createBasicReport(String metric, String reportName) throws InterruptedException {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded();
        waitForElementVisible(reportPage.getRoot());
        List<String> what = new ArrayList<String>();
        what.add(metric);
        reportPage.createReport(reportName, ReportTypes.HEADLINE, what, null);
        waitForElementVisible(BY_ONE_NUMBER_REPORT);
    }

    private int getNumberFromGDReport(String reportName) {
        initReportsPage();
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded();
        waitForElementVisible(reportPage.getRoot());
        OneNumberReport report = Graphene.createPageFragment(OneNumberReport.class, browser.findElement(
                BY_ONE_NUMBER_REPORT));
        return Integer.valueOf(report.getValue());
    }

    private void compareObjectsCount(int actual, int expected, String objectName) {
        assertEquals(actual, expected, objectName + "s count don't match");
    }

    @Override
    public String openZendeskSettingsUrl() {
        openUrl(getIntegrationUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK)).click();
        return browser.getCurrentUrl();
    }
}
