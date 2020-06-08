package com.gooddata.qa.utils.http.report;

import com.gooddata.sdk.model.md.report.Report;
import com.gooddata.sdk.model.md.report.ReportDefinition;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.commons.lang.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.UUID;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;

public class ReportRestRequest extends CommonRestRequest {

    public ReportRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Lock/ unlock report
     * note: if there are dashboards contain this report
     * report cannot unlock
     *
     * @param reportTitle title of report
     * @param isLocked lock(true) / unlock(false)
     */
    public void setLockedReport(String reportTitle, boolean isLocked) throws JSONException, IOException {
        String reportUri = getReportByTitle(reportTitle).getUri();
        final JSONObject json = getJsonObject(reportUri);
        json.getJSONObject("report").getJSONObject("meta").put("locked", BooleanUtils.toInteger(isLocked));
        executeRequest(RestRequest.initPutRequest(reportUri, json.toString()), HttpStatus.OK);
    }

    /**
     * Set public / private report
     *
     * @param reportTitle title of report
     * @param isPrivate private(true) / public(false)
     */
    public void setPrivateReport(String reportTitle, boolean isPrivate) throws JSONException, IOException {
        String reportUri = getReportByTitle(reportTitle).getUri();
        final JSONObject json = getJsonObject(reportUri);
        json.getJSONObject("report").getJSONObject("meta").put("unlisted", BooleanUtils.toInteger(isPrivate));
        executeRequest(RestRequest.initPutRequest(reportUri, json.toString()), HttpStatus.OK);
    }

    public void updateReport(String title, ReportDefinition reportDefinition) throws IOException {
        ReportDefinition definition = getMdService().createObj(getProject(), reportDefinition);
        String reportUri = getReportByTitle(title).getUri();
        final JSONObject json = getJsonObject(reportUri);
        json.getJSONObject("report").getJSONObject("content").append("definitions", definition.getUri());
        executeRequest(RestRequest.initPutRequest(reportUri, json.toString()), HttpStatus.OK);
    }

    public Report getReportByTitle(String title) {
        return getMdService().getObj(getProject(), Report.class, title(title));
    }

    public void updateReport(String title, String reportDefinitionUrl) throws IOException {
        String reportUri = getReportByTitle(title).getUri();
        final JSONObject json = getJsonObject(reportUri);
        json.getJSONObject("report").getJSONObject("content").append("definitions", reportDefinitionUrl);
        executeRequest(RestRequest.initPutRequest(reportUri, json.toString()), HttpStatus.OK);
    }

    public void updateTitleReport(String title, String renameTitle) throws IOException {
        String reportUri = getReportByTitle(title).getUri();
        final JSONObject jsonObject = getJsonObject(reportUri);
        jsonObject.getJSONObject("report").getJSONObject("meta").put("title", renameTitle);
        executeRequest(RestRequest.initPutRequest(reportUri, jsonObject.toString()), HttpStatus.OK);
    }

    public String getReportDefinitionByTitle(String title) {
        return getMdService().getObjUri(getProject(), ReportDefinition.class, title(title));
    }

    public JSONArray getElementMappingJson(JSONArray elementMappingJson) {
        elementMappingJson.put(new JSONObject().put("charttype", "overbar")
                .put("id", getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri()));
        elementMappingJson.put(new JSONObject().put("charttype", "targetbar")
                .put("id", getMetricByTitle(METRIC_OPP_FIRST_SNAPSHOT).getUri()));
        return elementMappingJson;
    }

    public ReportRestRequest changeChartType(String title, String chartType) throws IOException {
        JSONObject stylesJson = new JSONObject();
        if (chartType == "thermometer") { // Bullet chart
            // Setting for colorMapping and elementMapping
            JSONArray colorMappingJson = new JSONArray();
            colorMappingJson.put(new JSONObject().put("guid", "guid7")
                    .put("uri", getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri()));
            colorMappingJson.put(new JSONObject().put("guid", "guid7")
                    .put("uri", getMetricByTitle(METRIC_OPP_FIRST_SNAPSHOT).getUri()));
            JSONArray elementMappingJson = new JSONArray();
            getElementMappingJson(elementMappingJson);
            JSONObject globalJson = new JSONObject().put("colorMapping", colorMappingJson).put("elementMapping", elementMappingJson);
            stylesJson.put("global", globalJson);
        } else if (chartType == "waterfall" || chartType == "funnel") {
            // Don't setting for colorMapping and color
            JSONArray elementMappingJson = new JSONArray();
            getElementMappingJson(elementMappingJson);
            JSONObject globalJson = new JSONObject().put("colorMapping", new JSONArray()).put("elementMapping", elementMappingJson);
            stylesJson.put("global", globalJson);
        }
        String tilteDefintion = "Test" + generateHashString();
        JSONObject reportJson = getJsonObject(getReportByTitle(title).getUri());
        JSONArray reportDefinitionUris = reportJson.getJSONObject("report")
                .getJSONObject("content").getJSONArray("definitions");
        String reportDefinitionUri = reportJson.getJSONObject("report").getJSONObject("content")
                .getJSONArray("definitions").get(reportDefinitionUris.length() - 1).toString();

        JSONObject reportDefinition = getJsonObject(reportDefinitionUri);
        JSONObject chartJson = reportDefinition.getJSONObject("reportDefinition").getJSONObject("content")
                .getJSONObject("chart");
        if (chartType == "thermometer") {
            chartJson.put("styles", stylesJson).put("type", chartType);
        } else if (chartType == "funnel" || chartType == "waterfall") {
            JSONObject bucketsJson = chartJson.getJSONObject("buckets").put("color", new JSONArray());
            chartJson.put("styles", stylesJson).put("type", chartType).put("buckets", bucketsJson);
        }else{
            chartJson.put("type", chartType);
        }
        // Update reportDefinition
        JSONObject contentJson = reportDefinition.getJSONObject("reportDefinition").getJSONObject("content").put("chart", chartJson);
        JSONObject metaJson = reportDefinition.getJSONObject("reportDefinition").getJSONObject("meta").put("title", tilteDefintion);
        reportDefinition.getJSONObject("reportDefinition").put("content", contentJson).put("meta", metaJson);
        new ReportDefinitionRestRequest(restClient, projectId).createReportDefinition(reportDefinition.toString());
        updateReport(title, getReportDefinitionByTitle(tilteDefintion));
        return this;
    }

    public String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
