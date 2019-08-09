package com.gooddata.qa.utils.snowflake;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;

public class DataMappingRestRequest extends CommonRestRequest {

    private String dataSourceId;
    private List<Pair<String,String>> clientIdItems;
    private List<Pair<String,String>> projectIdItems;
    private static final String DATA_SOURCE_REST_URI = "/gdc/dataload/dataSources/";

    public DataMappingRestRequest(RestClient restClient, List<Pair<String, String>> projectIdItem,
            List<Pair<String, String>> clientIdItem, String dataSourceId, String currentProjectId) {
        super(restClient, currentProjectId);
        this.dataSourceId = dataSourceId;
        this.projectIdItems = projectIdItem;
        this.clientIdItems = clientIdItem;
    }

    public List<Pair<String,String>> getClientId() {
        return clientIdItems;
    }

    public void setClientId(List<Pair<String,String>> clientId) {
        this.clientIdItems = clientId;
    }

    public List<Pair<String,String>> getProjectIdItems() {
        return projectIdItems;
    }

    public void setProjectId(List<Pair<String,String>> projectIdItems) {
        this.projectIdItems = projectIdItems;
    }

    /**
     * Create MappingItem. Using clienId or projectId
     *
     * @param value
     * @return HttpRequest setupMappingItem
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     */
    public void createMappingItems(final CommonRestRequest commonRestClient) throws ParseException, JSONException, IOException {
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataMapping", new JSONObject() {{
                put("items", new JSONArray() {{
                        if(projectIdItems.isEmpty() == false) {
                            for(Pair<String, String> projectIdItem : projectIdItems) {
                                put( new JSONObject() {{
                                    put("mappingItem", new JSONObject() {{
                                        put("projectId", projectIdItem.getLeft());
                                        put("value", projectIdItem.getRight());
                                    }});
                            }});}}
                        if(clientIdItems.isEmpty() == false) {
                            for(Pair<String, String> clientIdItem : clientIdItems) {
                                put( new JSONObject() {{
                                    put("mappingItem", new JSONObject() {{
                                        put("clientId", clientIdItem.getLeft());
                                        put("value", clientIdItem.getRight());
                                    }});
                            }});}}
                }});
                }});
        }};
        HttpRequestBase jsonRequest =  RestRequest.initPostRequest(DATA_SOURCE_REST_URI + dataSourceId + "/dataMapping/bulk/upsert",
                dataSourceJson.toString());
        commonRestClient.getJsonObject(jsonRequest, HttpStatus.OK);
    }

    /**
     * Update ProjectId Mapping Item .
     *
     * @param commonRestClient
     * @param Pair<String,String> item
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     */
    public void updateProjectIdItem(final CommonRestRequest commonRestClient, Pair<String,String> item) throws ParseException, JSONException, IOException {
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataMapping", new JSONObject() {{
                put("items", new JSONArray() {{
                    put( new JSONObject() {{
                        put("mappingItem", new JSONObject() {{
                            put("projectId", item.getLeft());
                        put("value", item.getRight());
                        }});
                    }});
                }});
                }});
        }};
        HttpRequestBase jsonRequest =  RestRequest.initPostRequest(DATA_SOURCE_REST_URI + dataSourceId + "/dataMapping/bulk/upsert",
                dataSourceJson.toString());
        commonRestClient.getJsonObject(jsonRequest, HttpStatus.OK);
    }

    /**
     * Update ClientId Mapping Item .
     *
     * @param commonRestClient
     * @param Pair<String,String> item
     * @throws IOException
     * @throws JSONException
     * @throws ParseException
     */
    public void updateClientIdItem(final CommonRestRequest commonRestClient, Pair<String,String> item) throws ParseException, JSONException, IOException {
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataMapping", new JSONObject() {{
                put("items", new JSONArray() {{
                    put( new JSONObject() {{
                        put("mappingItem", new JSONObject() {{
                            put("clientId", item.getLeft());
                        put("value", item.getRight());
                        }});
                    }});
                }});
                }});
        }};
        HttpRequestBase jsonRequest =  RestRequest.initPostRequest(DATA_SOURCE_REST_URI + dataSourceId + "/dataMapping/bulk/upsert",
                dataSourceJson.toString());
        commonRestClient.getJsonObject(jsonRequest, HttpStatus.OK);
    }

    /**
     * delete data mapping by project Id DELETE method.
     *
     * @param String idDataMapping
     */
    public void deleteProjectIdItem(String idDataMapping) {
        HttpRequestBase deleteRequest = RestRequest.initDeleteRequest(
                DATA_SOURCE_REST_URI + dataSourceId + "/dataMapping" + "?" + "projectId=" + idDataMapping);
        restClient.execute(deleteRequest, HttpStatus.NO_CONTENT);
    }

    /**
     * delete data mapping by client Id DELETE method.
     *
     * @param String idDataMapping
     */
    public void deleteClientIdItem(String idDataMapping) {
            HttpRequestBase deleteRequest = RestRequest.initDeleteRequest(
                    DATA_SOURCE_REST_URI + dataSourceId + "/dataMapping" + "?" + "clientId=" + idDataMapping);
            restClient.execute(deleteRequest, HttpStatus.NO_CONTENT);
    }

}
