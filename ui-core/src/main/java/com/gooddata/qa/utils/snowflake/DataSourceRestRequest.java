package com.gooddata.qa.utils.snowflake;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class DataSourceRestRequest extends CommonRestRequest {

    private static final String DATA_SOURCE_REST_URI = "/gdc/dataload/dataSources";

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
    public static String createDataSource(final CommonRestRequest commonRestClient,
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

        // Setup Json body of Data Source
        String finalPrefix = prefix;
        JSONObject dataSourceJson = new JSONObject() {{
            put("dataSource", new JSONObject() {{
                put("name", dataSourceName);
                put("prefix", finalPrefix);
                put("connectionInfo", new JSONObject() {{
                    put("snowflake", new JSONObject() {{
                        put("warehouse", connectionInfo.getWarehouse());
                        put("schema", connectionInfo.getSchema());
                        put("database", connectionInfo.getDatabase());
                        put("url", connectionInfo.getUrl());
                        put("userName", connectionInfo.getUserName());
                        put("password", connectionInfo.getPassword());
                    }});
                }});
            }});
        }};

        return RestRequest.initPostRequest(DATA_SOURCE_REST_URI, dataSourceJson.toString());
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
}
