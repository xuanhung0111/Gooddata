/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.http.RestApiClient;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.fail;

@Test(groups = {"connectors", "zendesk4", "apiTests"}, description = "Checklist tests for Zendesk Helper")
public class Zendesk4BasicApiTest extends AbstractTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;
    private String zendeskApiUrl;
    private boolean useApiProxy;

    private ZendeskHelper zendeskHelper;

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

    private int createdZendeskTicketId;
    private int createdZendeskUserId;
    private int createdZendeskOrganizationId;

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = loadProperty("connectors.zendesk.apiUrl");
        zendeskAPIUser = loadProperty("connectors.zendesk.apiUser");
        zendeskAPIPassword = loadProperty("connectors.zendesk.apiUserPassword");
        useApiProxy = Boolean.parseBoolean(loadProperty("http.client.useApiProxy"));
    }

    @Test
    public void initGd() throws JSONException {
        signInAtGreyPages(user, password);
    }

    @Test(dependsOnMethods = {"initGd"}, groups = {"zendeskApiTests"})
    public void initZendeskApiClient() {
        if (zendeskApiUrl.contains("staging") && !zendeskAPIUser.isEmpty() && !zendeskAPIPassword.isEmpty()) {
            zendeskHelper = new ZendeskHelper(new RestApiClient(zendeskApiUrl.replace("https://", ""),
                    zendeskAPIUser, zendeskAPIPassword, false, useApiProxy));
        } else {
            fail("Zendesk staging API is not used, tests for adding new objects will be skipped");
        }
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "initGd"}, groups = {"zendeskApiTests"})
    public void testTicketsCount() throws IOException, JSONException {
        zendeskHelper.getNumberOfTickets();
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "initGd"}, groups = {"zendeskApiTests"})
    public void testUsersCount() throws IOException, JSONException {
        zendeskHelper.getNumberOfUsers();
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "initGd"},
            groups = {"zendeskApiTests"})
    public void testOrganizationsCount() throws IOException, JSONException {
        zendeskHelper.getNumberOfOrganizations();
    }

    @Test(dependsOnMethods = {"testTicketsCount"}, groups = {"zendeskApiTests", "newZendeskObjects"})
    public void testAddNewTicket() throws IOException, JSONException {
        createdZendeskTicketId = zendeskHelper.createNewTicket(
                String.format(JSON_TICKET_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects"})
    public void testAddNewUser() throws IOException, JSONException {
        createdZendeskUserId = zendeskHelper.createNewUser(
                String.format(JSON_USER_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects"})
    public void testAddNewOrganization() throws IOException, JSONException {
        createdZendeskOrganizationId = zendeskHelper.createNewOrganization(
                String.format(JSON_ORGANIZATION_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testAddNewTicket"}, groups = {"zendeskApiTests"})
    public void deleteZendeskTicket() throws IOException {
        zendeskHelper.deleteTicket(createdZendeskTicketId);
    }

    @Test(dependsOnMethods = {"testAddNewUser"}, groups = {"zendeskApiTests"})
    public void deleteZendeskUser() throws IOException {
        zendeskHelper.deleteUser(createdZendeskUserId);
    }

    @Test(dependsOnMethods = {"testAddNewOrganization"}, groups = {"zendeskApiTests"})
    public void deleteZendeskOrganization() throws IOException {
        zendeskHelper.deleteOrganization(createdZendeskOrganizationId);
    }
}
