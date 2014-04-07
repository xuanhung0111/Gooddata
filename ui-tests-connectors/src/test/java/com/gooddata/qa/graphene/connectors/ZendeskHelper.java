package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ZendeskHelper {

    private final RestApiClient apiClient;

    private static final String TICKETS_URL = "/api/v2/tickets.json";
    private static final String USERS_URL = "/api/v2/users.json";
    private static final String ORGANIZATIONS_URL = "/api/v2/organizations.json";

    public static final String TICKET_OBJECT_NAME = "ticket";
    public static final String USER_OBJECT_NAME = "user";
    public static final String ORGANIZATION_OBJECT_NAME = "organization";

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

    public void createNewTicket(String jsonTicket) throws IOException, JSONException {
        createNewZendeskObject(TICKETS_URL, jsonTicket, TICKET_OBJECT_NAME);
    }

    public void createNewUser(String jsonUser) throws IOException, JSONException {
        createNewZendeskObject(USERS_URL, jsonUser, USER_OBJECT_NAME);
    }

    public void createNewOrganization(String jsonOrganization) throws IOException, JSONException {
        createNewZendeskObject(ORGANIZATIONS_URL, jsonOrganization, ORGANIZATION_OBJECT_NAME);
    }

    public int createNewZendeskObject(String url, String jsonContent, String objectName)
            throws IOException, JSONException {
        HttpRequestBase postRequest = apiClient.newPostMethod(url, jsonContent);
        HttpResponse postResponse = apiClient.execute(postRequest, 201);
        String result = EntityUtils.toString(postResponse.getEntity());
        JSONObject json = new JSONObject(result);
        int id = json.getJSONObject(objectName).getInt("id");
        System.out.println("New Zendesk " + objectName + " created, id: " + id);
        return id;
    }

    private int getZendeskEntityCount(String url) throws JSONException, IOException {
        HttpRequestBase getRequest = apiClient.newGetMethod(url);
        HttpResponse getResponse = apiClient.execute(getRequest, 200);
        String result = EntityUtils.toString(getResponse.getEntity());
        JSONObject json = new JSONObject(result);
        int count = json.getInt("count");
        System.out.println(count + " objects returned from " + url);
        return count;
    }

    public static void main(String[] args) throws IOException, JSONException {
        ZendeskHelper helper = new ZendeskHelper(new RestApiClient("gooddataqa3.zendesk-staging.com",
                "qa@gooddata.com", "12345", false));
        System.out.println(helper.getNumberOfTickets());
        System.out.println(helper.getNumberOfOrganizations());
        System.out.println(helper.getNumberOfUsers());
    }
}
