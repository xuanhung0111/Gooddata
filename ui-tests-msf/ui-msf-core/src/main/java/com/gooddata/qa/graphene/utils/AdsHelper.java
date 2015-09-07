package com.gooddata.qa.graphene.utils;

import static java.lang.String.format;
import static org.apache.commons.lang.Validate.notNull;
import static org.testng.Assert.assertEquals;
import com.gooddata.GoodData;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.warehouse.Warehouse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public final class AdsHelper {

    public static final String ADS_INSTANCES_URI = "/gdc/datawarehouse/instances/";
    public static final String ADS_INSTANCE_SCHEMA_URI = ADS_INSTANCES_URI + "%s/schemas/default";
    public static final String OUTPUT_STAGE_URI = "/gdc/dataload/projects/%s/outputStage/";
    public static final String ADS_INSTANCES_USERS_URI = ADS_INSTANCES_URI + "%s/users";

    private static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";
    
    private static final String ADD_USER_CONTENT_BODY;
    
    static {
        try {
            ADD_USER_CONTENT_BODY = new JSONObject() {{
                put("user", new JSONObject() {{
                    put("role", "${role}");
                    put("login", "${email}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    private final GoodData gdClient;
    private final RestApiClient restApiClient;

    public AdsHelper(GoodData gdClient, RestApiClient restApiClient) {
        notNull(gdClient, "gdClient cannot be null!");
        notNull(restApiClient, "restApiClient cannot be null!");

        this.gdClient = gdClient;
        this.restApiClient = restApiClient;
    }

    public Warehouse createAds(String adsName, String adsToken) {
        final Warehouse adsInstance = new Warehouse(adsName, adsToken);
        return gdClient.getWarehouseService().createWarehouse(adsInstance).get();
    }

    public void removeAds(Warehouse adsInstance) {
        if (adsInstance != null) {
            gdClient.getWarehouseService().removeWarehouse(adsInstance);
        }
    }

    public void associateAdsWithProject(Warehouse adsInstance, String projectId) {
        String schemaUri = format(ADS_INSTANCE_SCHEMA_URI, adsInstance.getId());
        JSONObject outputStageObj = new JSONObject();
        try {
            outputStageObj.put("outputStage", new JSONObject().put("schema", schemaUri));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when set default schema for outputStage! ", e);
        }

        String outputStageUri = format(OUTPUT_STAGE_URI, projectId);
        HttpRequestBase putRequest = restApiClient.newPutMethod(outputStageUri, outputStageObj.toString());
        putRequest.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION);

        HttpResponse putResponse = restApiClient.execute(putRequest);
        int responseStatusCode = putResponse.getStatusLine().getStatusCode();

        System.out.println(putResponse.toString());
        EntityUtils.consumeQuietly(putResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);
        assertEquals(responseStatusCode, HttpStatus.OK.value(), "Default schema is not set successfully!");
    }

    public void addUserToAdsInstance(Warehouse adsInstance, String user, AdsRole role) {
        String adsUserUri = String.format(ADS_INSTANCES_USERS_URI, adsInstance.getId());
        String contentBody = ADD_USER_CONTENT_BODY.replace("${role}", role.getName()).replace("${email}", user);
        System.out.println("Content of json: " + contentBody);
        HttpRequestBase postRequest = restApiClient.newPostMethod(adsUserUri, contentBody);
        try {
            HttpResponse postResponse =
                    restApiClient.execute(postRequest, HttpStatus.ACCEPTED, "Invalid status code");
            JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
            String pollingUri =
                    json.getJSONObject("asyncTask").getJSONObject("links").getString("poll");
            assertEquals(RestUtils.waitingForAsyncTask(restApiClient, pollingUri), HttpStatus.CREATED.value(),
                    "User isn't added properly into the ads instance");
            System.out.println(format("Successfully added user %s to ads instance %s", user, adsInstance.getId()));

            EntityUtils.consumeQuietly(postResponse.getEntity());
        } catch (Exception e) {
            throw new IllegalStateException("There is a exception when adding user to ads instance", e);
        } finally {
            postRequest.releaseConnection();
        }
    }

    public enum AdsRole {
        ADMIN("admin"),
        DATA_ADMIN("dataAdmin");

        private String role;

        AdsRole(String role) {
            this.role = role;
        }

        public String getName() {
            return this.role;
        }
    }
}
