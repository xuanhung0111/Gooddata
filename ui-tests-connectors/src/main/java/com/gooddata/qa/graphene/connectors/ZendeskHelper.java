package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.http.RestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

public class ZendeskHelper {

    public enum ZendeskObject {
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

    public int getNumberOfTickets() throws IOException, JSONException {
        return getZendeskEntityCount(TICKETS_INC_URL, ZendeskObject.TICKET);
    }

    public int getNumberOfUsers() throws IOException, JSONException {
        return getZendeskEntityCount(USERS_INC_URL, ZendeskObject.USER);
    }

    public int getNumberOfOrganizations() throws IOException, JSONException {
        return getZendeskEntityCount(ORGANIZATIONS_INC_URL, ZendeskObject.ORGANIZATION);
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

    private int createNewZendeskObject(String url, String jsonContent, ZendeskObject objectName)
            throws IOException, JSONException {
        final int id = getJsonObject(apiClient, apiClient.newPostMethod(url, jsonContent), HttpStatus.CREATED)
            .getJSONObject(objectName.getName())
            .getInt("id");
        System.out.println("New Zendesk " + objectName.getName() + " created, id: " + id);
        return id;
    }

    private int getZendeskEntityCount(String url, ZendeskObject objectType)
            throws JSONException, IOException {
        return getSetOfActiveZendeskEntities(url, objectType, 1).size();
    }

    public OptionalInt loadLastTicketEventId(int ticketId, DateTime startDateTime)
            throws JSONException, IOException {
        JSONObject ticketsEventsPageJson;
        JSONArray ticketEventsJson;
        long startTimestampInUTC = startDateTime.toDateTime(DateTimeZone.UTC).getMillis() / 1000L;
        String jsonUrl = TICKET_EVENTS_INC_URL + "?start_time=" + startTimestampInUTC;
        int lastTicketEventId = 0;
        long lastEventTimestamp = 0;

        do {
            ticketsEventsPageJson = retrieveEntitiesJsonFromUrl(jsonUrl);
            jsonUrl = ticketsEventsPageJson.getString("next_page");
            ticketEventsJson = ticketsEventsPageJson.getJSONArray(ZendeskObject.TICKET_EVENT.getPluralName());

            for (int i = 0; i < ticketEventsJson.length(); i++) {
                JSONObject ticketEventJson = ticketEventsJson.getJSONObject(i);

                if (ticketEventJson.getInt("ticket_id") == ticketId
                        && ticketEventJson.getLong("timestamp") > lastEventTimestamp) {
                    lastTicketEventId = ticketEventJson.getInt("id");
                    lastEventTimestamp = ticketEventJson.getLong("timestamp");
                }
            }
        } while (ticketsEventsPageJson.getInt("count") == 1000 && jsonUrl != null);

        return lastTicketEventId == 0 ? OptionalInt.empty() : OptionalInt.of(lastTicketEventId);
    }

    private JSONObject retrieveEntitiesJsonFromUrl(String url)
            throws IOException, JSONException {
        final Supplier<HttpRequestBase> request = () -> apiClient.newGetMethod(url);
        int retryCounter = 0;
        int statusCode;

        while ((statusCode = executeRequest(apiClient, request.get())) == 429 && retryCounter < 5) {
            System.out.println("API limits reached, retrying ... ");
            sleepTightInSeconds(30);
            retryCounter++;
        }
        assertThat("Invalid status code returned from GET on " + url, statusCode, is(200));
        final JSONObject json = getJsonObject(apiClient, url);
        System.out.println("Total " + json.getInt("count") + " entities returned from " + url);
        return json;
    }

    private Set<Integer> getSetOfActiveZendeskEntities(String url, ZendeskObject objectType, int pageNumber)
            throws JSONException, IOException {
            JSONObject json = retrieveEntitiesJsonFromUrl(url);
            Set<Integer> nonDeletedObjects = new HashSet<>();
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
        System.out.println("Going to delete object on url " + objectUrl);
        assertThat("Invalid status code returned from DELETE on " + objectUrl,
                executeRequest(apiClient, apiClient.newDeleteMethod(objectUrl)),
                isOneOf(204, 200));
        System.out.println("Deleted object on url " + objectUrl);
    }

    private void updateZendeskObject(String url, String jsonContent, ZendeskObject objectName)
            throws IOException, JSONException {
        final int id = getJsonObject(apiClient, apiClient.newPutMethod(url, jsonContent))
            .getJSONObject(objectName.getName()).getInt("id");
        System.out.println("Zendesk " + objectName.getName() + " was updated, id: " + id);
    }

    public static String getCurrentTimeIdentifier() {
        return dateFormat.format(new Date());
    }
}