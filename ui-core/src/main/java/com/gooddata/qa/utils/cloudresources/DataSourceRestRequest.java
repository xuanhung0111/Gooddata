package com.gooddata.qa.utils.cloudresources;

import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;

import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSourceRestRequest extends CommonRestRequest {

    public static final String DATA_SOURCE_REST_URI = "/gdc/dataload/dataSources";

    public DataSourceRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Create data source to work with Snowflake.
     *
     * @param setupDataSourceRequest
     * @param commonRestClient
     * @return data source identify
     */
    public String createDataSource(final CommonRestRequest commonRestClient,
                                          HttpRequestBase setupDataSourceRequest) throws IOException {
        JSONObject jsonObj = commonRestClient.getJsonObject(setupDataSourceRequest, HttpStatus.CREATED);
        // execute then return data source Id
        String dataSourceId = jsonObj.getJSONObject("dataSource").getString("id");
        return dataSourceId;
    }

    /**
     * setup data source POST method.
     *
     * @param connectionInfo
     * @param dataSourceName
     * @param optionalPrefix
     * @return HttpRequestBase POST request
     */

    public HttpRequestBase setupDataSourceRequest(ConnectionInfo connectionInfo, String dataSourceName,
            String... optionalPrefix) {
        // get prefix if exist.
        String prefix = null;
        if (optionalPrefix.length > 0) {
            prefix = optionalPrefix[0];
        }
        if (connectionInfo.getDbType() == DatabaseType.BIGQUERY) {
            return setupBigQueryDataSourceRequest(connectionInfo, dataSourceName, prefix);
        }

        // Setup Json body of Data Source
        String finalPrefix = prefix;
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataSource", new JSONObject() {{
                put("name", dataSourceName);
                put("prefix", finalPrefix);
                put("connectionInfo", new JSONObject() {{
                    put(connectionInfo.getDbType() == DatabaseType.SNOWFLAKE ? DatabaseType.SNOWFLAKE.toString() 
                        : DatabaseType.REDSHIFT.toString() , new JSONObject() {{
                        if (connectionInfo.getDbType() == DatabaseType.SNOWFLAKE) put("warehouse", connectionInfo.getWarehouse());
                        put("schema", connectionInfo.getSchema());
                        put("database", connectionInfo.getDatabase());
                        put("url", connectionInfo.getUrl());
                        put("authentication", new JSONObject() {{
                            put("basic", new JSONObject() {{
                                put("userName", connectionInfo.getUserName());
                                put("password", connectionInfo.getPassword());
                            }});
                        }});
                    }});
                }});
            }});
        }};

        return RestRequest.initPostRequest(DATA_SOURCE_REST_URI, dataSourceJson.toString());
    }

    private HttpRequestBase setupBigQueryDataSourceRequest(ConnectionInfo connectionInfo, String dataSourceName, String finalPrefix) {
        // Setup Json body of Data Source
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataSource", new JSONObject() {{
                put("name", dataSourceName);
                put("prefix", finalPrefix);
                put("connectionInfo", new JSONObject() {{
                    put(getDbType(connectionInfo), new JSONObject() {{
                        put("schema", connectionInfo.getSchema());
                        put("project", connectionInfo.getProject());
                        put("authentication", new JSONObject() {{
                            if (connectionInfo.getDbType() == DatabaseType.BIGQUERY) {
                                put("serviceAccount", new JSONObject() {{
                                    put("clientEmail", connectionInfo.getClientEmail());
                                    put("privateKey", connectionInfo.getPrivateKey());
                                }});
                            }
                        }});
                    }});
                }});
            }});
        }};
        return RestRequest.initPostRequest(DATA_SOURCE_REST_URI, dataSourceJson.toString());
    }

    private String getDbType(ConnectionInfo connectionInfo) {
        return connectionInfo.getDbType().toString();
    }

    /**
     * Delete data source to work with Snowflake.
     *
     * @param dataSourceId
     */

    public void deleteDataSource(String dataSourceId) {
        HttpRequestBase deleteRequest = RestRequest.initDeleteRequest(DATA_SOURCE_REST_URI + "/" + dataSourceId);
        restClient.execute(deleteRequest, HttpStatus.NO_CONTENT);
    }

    public List<String> getAllDataSourceNames() throws IOException {
        final JSONObject json = getJsonObject(
            RestRequest.initGetRequest(DATA_SOURCE_REST_URI + "?offset=0&limit=300"));
        JSONArray items = json.getJSONObject("dataSources").getJSONArray("items");
        List<String> dataSourceTitles = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i).getJSONObject("dataSource");
            dataSourceTitles.add(item.getString("name"));
        }
        return dataSourceTitles;
    }
}
