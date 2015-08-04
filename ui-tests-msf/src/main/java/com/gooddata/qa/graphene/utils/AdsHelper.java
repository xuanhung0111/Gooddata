package com.gooddata.qa.graphene.utils;

import static java.lang.String.format;
import static org.apache.commons.lang.Validate.notNull;
import static org.testng.Assert.assertEquals;

import com.gooddata.GoodData;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.warehouse.Warehouse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public final class AdsHelper {

    public static final String ADS_INSTANCES_URI = "gdc/datawarehouse/instances/";
    public static final String ADS_INSTANCE_SCHEMA_URI = "/" + ADS_INSTANCES_URI + "%s/schemas/default";
    public static final String OUTPUT_STAGE_URI = "/gdc/dataload/projects/%s/outputStage/";

    private static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";

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
}
