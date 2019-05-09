package com.gooddata.qa.utils.http.report;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.springframework.http.HttpStatus;

import static java.lang.String.format;

public class ReportDefinitionRestRequest extends CommonRestRequest {

    private static final String OBJECT_URI = "/gdc/md/%s/obj/";

    public ReportDefinitionRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    public void createReportDefinition(String reportDefinition) {
        executeRequest(RestRequest.initPostRequest(format(OBJECT_URI, projectId), reportDefinition), HttpStatus.OK);
    }
}
