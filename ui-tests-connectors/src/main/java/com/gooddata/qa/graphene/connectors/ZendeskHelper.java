package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class ZendeskHelper {

    public static enum ZendeskObject {
        TICKET,
        USER,
        ORGANIZATION,
        TICKET_EVENT;

        public String getName() {
            return this.toString().toLowerCase();
        }

        public String getPluralName() {
            return this.getName() + "s";
        }
    }

    private final RestApiClient apiClient;

    private static final String TICKETS_INC_URL = "/api/v2/incremental/tickets.json?start_time=0";
    private static final String USERS_INC_URL = "/api/v2/incremental/users.json?start_time=0";
    private static final String ORGANIZATIONS_INC_URL = "/api/v2/incremental/organizations.json?start_time=0";
    private static final String TICKET_EVENTS_INC_URL = "/api/v2/incremental/ticket_events.json";

    private static final String TICKETS_URL = "/api/v2/tickets";
    private static final String USERS_URL = "/api/v2/users";
    private static final String ORGANIZATIONS_URL = "/api/v2/organizations";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

    public ZendeskHelper(RestApiClient apiClient) {
        this.apiClient = apiClient;
        System.out.println("Initialized client for " + this.apiClient.getHttpHost());
    }

    public int getNumberOfTickets() throws IOException, JSONException, InterruptedException {
        return getZendeskEntityCount(TICKETS_INC_URL, ZendeskObject.TICKET);
    }

    public int getNumberOfUsers() throws IOException, JSONException, InterruptedException {
        return getZendeskEntityCount(USERS_INC_URL, ZendeskObject.USER);
    }

    public int getNumberOfOrganizations() throws IOException, JSONException, InterruptedException {
        return getZendeskEntityCount(ORGANIZATIONS_INC_URL, ZendeskObject.ORGANIZATION);
    }

    public int getNumberOfTicketEvents() throws IOException, JSONException, InterruptedException {
        return getZendeskEntityCount(TICKET_EVENTS_INC_URL, ZendeskObject.TICKET_EVENT);
    }

    public int createNewTicket(String jsonTicket) throws IOException, JSONException {
        return createNewZendeskObject(TICKETS_URL, jsonTicket, ZendeskObject.TICKET);
    }

    public void updateTicket(int tickedId, String jsonTicketUpdate) throws IOException, JSONException {
        updateZendeskObject(TICKETS_URL + "/" + tickedId + ".json", jsonTicketUpdate, ZendeskObject.TICKET);
    }

    public int createNewUser(String jsonUser) throws IOException, JSONException {
        return createNewZendeskObject(USERS_URL, jsonUser, ZendeskObject.USER);
    }

    public int createNewOrganization(String jsonOrganization) throws IOException, JSONException {
        return createNewZendeskObject(ORGANIZATIONS_URL, jsonOrganization, ZendeskObject.ORGANIZATION);
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

    public int createNewZendeskObject(String url, String jsonContent, ZendeskObject objectName)
            throws IOException, JSONException {
        HttpRequestBase postRequest = apiClient.newPostMethod(url, jsonContent);
        try {
            HttpResponse postResponse = apiClient.execute(postRequest);
            checkStatusCode(postResponse, 201);
            String result = EntityUtils.toString(postResponse.getEntity());
            JSONObject json = new JSONObject(result);
            int id = json.getJSONObject(objectName.getName()).getInt("id");
            System.out.println("New Zendesk " + objectName.getName() + " created, id: " + id);
            return id;
        } finally {
            postRequest.releaseConnection();
        }
    }

    private int getZendeskEntityCount(String url, ZendeskObject objectType)
            throws JSONException, IOException, InterruptedException {
        return getSetOfActiveZendeskEntities(url, objectType, 1).size();
    }

    public Integer loadLastTicketEventId(int ticketId, DateTime startDateTime)
            throws JSONException, IOException, InterruptedException {
        JSONObject ticketsEventsPageJson;
        JSONArray ticketEventsJson;
        long startTimestampInUTC = startDateTime.toDateTime(DateTimeZone.UTC).getMillis() / 1000L;
        String jsonUrl = TICKET_EVENTS_INC_URL + "?start_time=" + startTimestampInUTC;
        int lastTicketEventId = 0;

        do {
            ticketsEventsPageJson = retrieveEntitiesJsonFromUrl(jsonUrl);
            jsonUrl = ticketsEventsPageJson.getString("next_page");
            ticketEventsJson = ticketsEventsPageJson.getJSONArray(ZendeskObject.TICKET_EVENT.getPluralName());

            for (int i = 0; i < ticketEventsJson.length(); i++) {
                JSONObject ticketEventJson = ticketEventsJson.getJSONObject(i);

                if (ticketEventJson.getInt("ticket_id") == ticketId
                        && ticketEventJson.getInt("id") > lastTicketEventId) {
                    lastTicketEventId = ticketEventJson.getInt("id");
                }
            }
        } while (ticketsEventsPageJson.getInt("count") == 1000 && jsonUrl != null);

        return lastTicketEventId == 0 ? null : lastTicketEventId;
    }

    private JSONObject retrieveEntitiesJsonFromUrl(String url)
            throws InterruptedException, IOException, JSONException {
        HttpRequestBase getRequest = apiClient.newGetMethod(url);
        try {
            HttpResponse getResponse = apiClient.execute(getRequest);
            int retryCounter = 0;
            while (getResponse.getStatusLine().getStatusCode() == 429 && retryCounter < 5) {
                System.out.println("API limits reached, retrying ... ");
                Thread.sleep(30000);
                getRequest.releaseConnection();
                getRequest = apiClient.newGetMethod(url);
                getResponse = apiClient.execute(getRequest);
                retryCounter++;
            }
            checkStatusCode(getResponse, 200);
            String result = EntityUtils.toString(getResponse.getEntity());
            JSONObject json = new JSONObject(result);
            System.out.println("Total " + json.getInt("count") + " entities returned from " + url);
            return json;
        } finally {
            getRequest.releaseConnection();
        }
    }

    private Set<Integer> getSetOfActiveZendeskEntities(String url, ZendeskObject objectType, int pageNumber)
            throws JSONException, IOException, InterruptedException {
            JSONObject json = retrieveEntitiesJsonFromUrl(url);
            Set<Integer> nonDeletedObjects = new HashSet<Integer>();
            int count = json.getInt("count");
            System.out.println(count + " " + objectType.getPluralName() + " returned from " + url);
            int deletedObjects = 0;
            JSONArray array = json.getJSONArray(objectType.getPluralName());
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                switch (objectType) {
                    case TICKET:
                        if (object.getString("status").equals("deleted")) {
                            deletedObjects++;
                        } else {
                            nonDeletedObjects.add(object.getInt("id"));
                        }
                        break;
                    case USER:
                        if (!object.getBoolean("active")) {
                            deletedObjects++;
                        } else {
                            nonDeletedObjects.add(object.getInt("id"));
                        }
                        break;
                    case ORGANIZATION:
                        if (!object.getString("deleted_at").isEmpty() &&
                                !object.getString("deleted_at").equals("null")) {
                            deletedObjects++;
                        } else {
                            nonDeletedObjects.add(object.getInt("id"));
                        }
                        break;
                    case TICKET_EVENT:
                        //TODO - more events are sent at single event
                        JSONArray childEvents = object.getJSONArray("child_events");
                        for (int j = 0; j < childEvents.length(); j++) {
                            nonDeletedObjects.add(childEvents.getJSONObject(j).getInt("id"));
                        }
                        break;
                }
            }
            System.out.println("Found " + deletedObjects + " deleted " + objectType.getPluralName());
            if (count == 1000 && json.getString("next_page") != null) {
                System.out.println("Next page found...");
                nonDeletedObjects.addAll(getSetOfActiveZendeskEntities(json.getString("next_page"),
                        objectType, pageNumber + 1));
            }
            System.out.println("Returning " + nonDeletedObjects.size() + " " + objectType.getPluralName() +
                    " after iteration on page " + pageNumber);

            return nonDeletedObjects;
    }

    private void deleteZendeskEntity(String url, int id) throws IOException {
        final String objectUrl = url + "/" + id;
        HttpRequestBase deleteRequest = apiClient.newDeleteMethod(objectUrl);
        try {
            System.out.println("Going to delete object on url " + objectUrl);
            HttpResponse deleteResponse = apiClient.execute(deleteRequest);
            checkStatusCode(deleteResponse, 200);
            System.out.println("Deleted object on url " + objectUrl);
        } finally {
            deleteRequest.releaseConnection();
        }
    }

    public void updateZendeskObject(String url, String jsonContent, ZendeskObject objectName)
            throws IOException, JSONException {
        HttpRequestBase putRequest = apiClient.newPutMethod(url, jsonContent);
        try {
            HttpResponse putResponse = apiClient.execute(putRequest);
            checkStatusCode(putResponse, 200);
            JSONObject json = new JSONObject(EntityUtils.toString(putResponse.getEntity()));
            int id = json.getJSONObject(objectName.getName()).getInt("id");
            System.out.println("Zendesk " + objectName.getName() + " was updated, id: " + id);
        } finally {
            putRequest.releaseConnection();
        }
    }

    public static String getCurrentTimeIdentifier() {
        return dateFormat.format(new Date());
    }

    private void checkStatusCode(HttpResponse response, int expectedStatus) {
        if (response.getStatusLine().getStatusCode() != expectedStatus) {
            System.out.println("Response status reason phrase: " + response.getStatusLine().getReasonPhrase());
        }
        assertEquals(response.getStatusLine().getStatusCode(), expectedStatus, "Invalid status code");
    }
}