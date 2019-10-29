package com.gooddata.qa.utils.cloudresources;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;

public class DataMappingUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataMappingUtils.class);
    private DataMappingRestRequest dataMappingRestRequest;
    private CommonRestRequest commonRestRequest;
    private TestParameters testParams;

    public DataMappingUtils(String user, List<Pair<String, String>> projectIdItems, List<Pair<String, String>> clientIdItems, String dataSourceId, String currentProject) {
        this.testParams = TestParameters.getInstance();
        RestClient restClient = new RestClient(new RestProfile(testParams.getHost(), user, testParams.getPassword(), true));
        this.dataMappingRestRequest = new DataMappingRestRequest(restClient, projectIdItems, clientIdItems, dataSourceId, currentProject);
        this.commonRestRequest = new CommonRestRequest(restClient, testParams.getProjectId());
        this.testParams = TestParameters.getInstance();
    }

    public void createDataMapping() {
        try {
            dataMappingRestRequest.createMappingItems(this.commonRestRequest);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Create DataMapping failed");
            throw new RuntimeException(e);
        }
    }

    public void updateClientIdDataMapping(Pair<String, String>clientIdItems) throws ParseException, JSONException, IOException {
        dataMappingRestRequest.updateClientIdItem(this.commonRestRequest, clientIdItems);
    }

    public void updateProjectIdDataMapping(Pair<String, String>projectIdItems) throws ParseException, JSONException, IOException {
        dataMappingRestRequest.updateProjectIdItem(this.commonRestRequest, projectIdItems);
    }

    public void deleteClientIdDataMapping(String clientIdItems) throws ParseException, JSONException, IOException {
        dataMappingRestRequest.deleteClientIdItem( clientIdItems);
    }

    public void deleteProjectIdDataMapping(String projectIdItems) throws ParseException, JSONException, IOException {
        dataMappingRestRequest.deleteProjectIdItem( projectIdItems);
    }

}
