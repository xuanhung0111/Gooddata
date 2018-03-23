package com.gooddata.qa.utils.http;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.Report;
import com.gooddata.project.Project;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.http.RestRequest.initGetRequest;
import static com.gooddata.qa.utils.http.RestRequest.initPostRequest;
import static java.util.Objects.isNull;

public class CommonRestRequest {

    private static final Logger log = Logger.getLogger(CommonRestRequest.class.getName());

    protected RestClient restClient;
    protected String projectId;

    public CommonRestRequest(RestClient restClient, String projectId) {
        this.restClient = restClient;
        this.projectId = projectId;
    }

    public String getProjectId() {
        return projectId;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * Execute request
     *
     * @param request
     * @return status code
     */
    public int executeRequest(HttpRequestBase request) {
        try {
            HttpResponse response = restClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(response.getEntity());
            return statusCode;
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Execute request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     */
    public void executeRequest(HttpRequestBase request, HttpStatus expectedStatusCode) {
        try {
            final HttpResponse response = restClient.execute(request, expectedStatusCode);
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param setupRequest        setup request before executing like configure header, ...
     * @param expectedStatusCode
     * @return entity from response in String form
     */
    public String getResource(HttpRequestBase request, Consumer<HttpRequestBase> setupRequest,
                              HttpStatus expectedStatusCode) throws ParseException, IOException {
        setupRequest.accept(request);
        try {
            final HttpResponse response = restClient.execute(request, expectedStatusCode);
            final HttpEntity entity = response.getEntity();

            final String ret = isNull(entity) ? "" : EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            return ret;

        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     * @return entity from response in json form
     */
    public String getResource(HttpRequestBase request, HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(request, req -> req.setHeader(
                "Accept", ContentType.APPLICATION_JSON.getMimeType()), expectedStatusCode);
    }

    /**
     * Get resource from uri with expected status code
     *
     * @param uri
     * @param expectedStatusCode
     * @return entity from response in json form
     */
    public String getResource(String uri, HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(initGetRequest(uri), req -> req.setHeader(
                "Accept", ContentType.APPLICATION_JSON.getMimeType()), expectedStatusCode);
    }

    /**
     * Get json object from uri, expected status code is OK (200)
     *
     * @param uri
     * @return
     */
    public JSONObject getJsonObject(String uri) throws IOException, JSONException {
        return getJsonObject(uri, HttpStatus.OK);
    }

    /**
     * Get json object from uri with expected status code
     *
     * @param uri
     * @param expectedStatusCode
     * @return
     */
    public JSONObject getJsonObject(String uri, HttpStatus expectedStatusCode) throws IOException,
            JSONException {
        return new JSONObject(getResource(uri, expectedStatusCode));
    }

    /**
     * Get json object from request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     * @return
     */
    public JSONObject getJsonObject(HttpRequestBase request, HttpStatus expectedStatusCode) throws
            ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, expectedStatusCode));
    }

    /**
     * Get json object from request, expected status code is OK (200)
     *
     * @param request
     * @return
     */
    public JSONObject getJsonObject(HttpRequestBase request) throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, HttpStatus.OK));
    }

    public Attribute getAttributeByTitle(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title));
    }

    public Attribute getAttributeByUri(String uri) {
        return getMdService().getObjByUri(uri, Attribute.class);
    }

    public Attribute getAttributeByIdentifier(String id) {
        return getMdService().getObj(getProject(), Attribute.class, identifier(id));
    }

    public Metric getMetricByTitle(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    public Metric getMetricByIdentifier(String id) {
        return getMdService().getObj(getProject(), Metric.class, identifier(id));
    }

    public Fact getFactByTitle(String title) {
        return getMdService().getObj(getProject(), Fact.class, title(title));
    }

    public Fact getFactByIdentifier(String id) {
        return getMdService().getObj(getProject(), Fact.class, identifier(id));
    }

    public Dataset getDatasetByIdentifier(String id) {
        return getMdService().getObj(getProject(), Dataset.class, identifier(id));
    }

    public Dataset getDatasetByTitle(String title) {
        return getMdService().getObj(getProject(), Dataset.class, title(title));
    }

    public List<String> getObjIdentifiers(List<String> uris) {
        try {
            JSONArray array = getJsonObject(
                    initPostRequest(
                            String.format("/gdc/md/%s/identifiers", projectId),
                            new JSONObject().put("uriToIdentifier", uris).toString()))
                    .getJSONArray("identifiers");

            List<String> foundIds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                foundIds.add(array.getJSONObject(i).getString("identifier"));
            }

            return foundIds;
        } catch (IOException | JSONException e) {
            throw new RuntimeException("there is an error while searching obj", e);
        }
    }

    public Report getReportByTitle(String title) {
        return getMdService().getObj(getProject(), Report.class, title(title));
    }

    public int waitingForAsyncTask(String pollingUri) {
        HttpRequestBase request = RestRequest.initGetRequest(pollingUri);
        while (executeRequest(request) == HttpStatus.ACCEPTED.value()) {
            log.info("Async task is running...");
            sleepTightInSeconds(2);
        }

        return executeRequest(request);
    }

    /**
     * Get asynchronous task status
     *
     * @param pollingUri
     * @return status
     */
    public String getAsyncTaskStatus(String pollingUri) throws IOException, JSONException {
        final JSONObject taskObject = getJsonObject(pollingUri);

        String key = "";
        if (!taskObject.isNull("wTaskStatus")) key = "wTaskStatus";
        else if (!taskObject.isNull("taskState")) key = "taskState";
        else throw new IllegalStateException("The status object is not existing! The current response is: "
                    + taskObject.toString());

        final String status = taskObject.getJSONObject(key).getString("status");
        log.info("Async task status is: " + status);
        return status;
    }

    protected Project getProject() {
        return restClient.getProjectService().getProjectById(projectId);
    }

    protected MetadataService getMdService() {
        return restClient.getMetadataService();
    }
}
