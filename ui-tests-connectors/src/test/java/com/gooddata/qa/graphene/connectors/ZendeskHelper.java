package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ZendeskHelper {

    private final RestApiClient apiClient;

    private static final String TICKETS_URL = "/api/v2/tickets";
    private static final String USERS_URL = "/api/v2/users";
    private static final String ORGANIZATIONS_URL = "/api/v2/organizations";

    public static final String TICKET_OBJECT_NAME = "ticket";
    public static final String USER_OBJECT_NAME = "user";
    public static final String ORGANIZATION_OBJECT_NAME = "organization";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public ZendeskHelper(RestApiClient apiClient) {
        this.apiClient = apiClient;
        System.out.println("Initialized client for " + this.apiClient.getHttpHost());
    }

    public int getNumberOfTickets() throws IOException, JSONException {
        return getZendeskEntityCount(TICKETS_URL);
    }

    public int getNumberOfUsers() throws IOException, JSONException {
        return getZendeskEntityCount(USERS_URL);
    }

    public int getNumberOfOrganizations() throws IOException, JSONException {
        return getZendeskEntityCount(ORGANIZATIONS_URL);
    }

    public int createNewTicket(String jsonTicket) throws IOException, JSONException {
        return createNewZendeskObject(TICKETS_URL, jsonTicket, TICKET_OBJECT_NAME);
    }

    public int createNewUser(String jsonUser) throws IOException, JSONException {
        return createNewZendeskObject(USERS_URL, jsonUser, USER_OBJECT_NAME);
    }

    public int createNewOrganization(String jsonOrganization) throws IOException, JSONException {
        return createNewZendeskObject(ORGANIZATIONS_URL, jsonOrganization, ORGANIZATION_OBJECT_NAME);
    }

    public void deleteTicket(int ticketId) throws IOException {
        deleteZendeskEntity(TICKETS_URL, ticketId);
    }

    public void deleteUser(int userId) throws IOException {
        deleteZendeskEntity(USERS_URL, userId);
    }

    public void deleteOrganization(int organizationId) throws IOException {
        deleteZendeskEntity(ORGANIZATIONS_URL, organizationId);
    }

    public int createNewZendeskObject(String url, String jsonContent, String objectName)
            throws IOException, JSONException {
        HttpRequestBase postRequest = apiClient.newPostMethod(url, jsonContent);
        try {
            HttpResponse postResponse = apiClient.execute(postRequest, 201);
            String result = EntityUtils.toString(postResponse.getEntity());
            JSONObject json = new JSONObject(result);
            int id = json.getJSONObject(objectName).getInt("id");
            System.out.println("New Zendesk " + objectName + " created, id: " + id);
            return id;
        } finally {
            postRequest.releaseConnection();
        }
    }

    private int getZendeskEntityCount(String url) throws JSONException, IOException {
        HttpRequestBase getRequest = apiClient.newGetMethod(url);
        try {
            HttpResponse getResponse = apiClient.execute(getRequest, 200);
            String result = EntityUtils.toString(getResponse.getEntity());
            JSONObject json = new JSONObject(result);
            int count = json.getInt("count");
            System.out.println(count + " objects returned from " + url);
            return count;
        } finally {
            getRequest.releaseConnection();
        }
    }

    private void deleteZendeskEntity(String url, int id) throws IOException {
        final String objectUrl = url + "/" + id;
        HttpRequestBase deleteRequest = apiClient.newDeleteMethod(objectUrl);
        try {
            System.out.println("Going to delete object on url " + objectUrl);
            apiClient.execute(deleteRequest, 200);
            System.out.println("Deleted object on url " + objectUrl);
        } finally {
            deleteRequest.releaseConnection();
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        ZendeskHelper helper = new ZendeskHelper(new RestApiClient("gooddataqa3.zendesk-staging.com",
                "qa@gooddata.com", "12345", false));
        System.out.println(helper.getNumberOfTickets());
        System.out.println(helper.getNumberOfOrganizations());
        System.out.println(helper.getNumberOfUsers());

        final String JSON_TICKET_CREATE = "{\"ticket\":{\"subject\":\"GD test ticket\", " +
                "\"comment\": { \"body\": \"Description of automatically created ticket\" }}}";

        final String JSON_USER_CREATE = "{\"user\": {\"name\": \"GD test user\", \"email\": " +
                "\"qa+zendesk-test%s@gooddata.com\"}}";

        final String JSON_ORGANIZATION_CREATE = "{\"organization\": {\"name\": \"GD test organization - %s\"}}";
        int ticket = helper.createNewTicket(String.format(JSON_TICKET_CREATE, getCurrentTimeIdentifier()));
        int organization = helper.createNewOrganization(String.format(JSON_ORGANIZATION_CREATE, getCurrentTimeIdentifier()));
        int user = helper.createNewUser(String.format(JSON_USER_CREATE, getCurrentTimeIdentifier()));
        System.out.println(helper.getNumberOfTickets());
        System.out.println(helper.getNumberOfOrganizations());
        System.out.println(helper.getNumberOfUsers());
        helper.deleteTicket(ticket);
        helper.deleteOrganization(organization);
        helper.deleteUser(user);
    }

    public static String getCurrentTimeIdentifier() {
        return dateFormat.format(new Date());
    }
}