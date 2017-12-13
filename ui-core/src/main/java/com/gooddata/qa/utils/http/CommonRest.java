package com.gooddata.qa.utils.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.function.Consumer;

import static com.gooddata.qa.utils.http.RestRequest.initGetRequest;
import static java.util.Objects.isNull;

public class CommonRest {

    protected String projectId;
    protected RestClient client;

    protected CommonRest(String projectId, RestClient client) {
        this.projectId = projectId;
        this.client = client;
    }

    /**
     * Execute request
     *
     * @param request
     * @return status code
     */
    protected int executeRequest(HttpRequestBase request) {
        try {
            HttpResponse response = client.execute(request);
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
    protected void executeRequest(HttpRequestBase request, HttpStatus expectedStatusCode) {
        try {
            final HttpResponse response = client.execute(request, expectedStatusCode);
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param setupRequest setup request before executing like configure header, ...
     * @param expectedStatusCode
     * @return entity from response in String form
     */
    protected String getResource(HttpRequestBase request, Consumer<HttpRequestBase> setupRequest,
                                 HttpStatus expectedStatusCode) throws ParseException, IOException {
        setupRequest.accept(request);
        try {
            final HttpResponse response = client.execute(request, expectedStatusCode);
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
    protected String getResource(HttpRequestBase request, HttpStatus expectedStatusCode) throws ParseException, IOException {
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
    protected String getResource(String uri, HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(initGetRequest(uri), req -> req.setHeader(
                "Accept", ContentType.APPLICATION_JSON.getMimeType()), expectedStatusCode);
    }

    /**
     * Get json object from uri, expected status code is OK (200)
     *
     * @param uri
     * @return
     */
    protected JSONObject getJsonObject(String uri) throws IOException, JSONException {
        return getJsonObject(uri, HttpStatus.OK);
    }

    /**
     * Get json object from uri with expected status code
     *
     * @param uri
     * @param expectedStatusCode
     * @return
     */
    protected JSONObject getJsonObject(String uri, HttpStatus expectedStatusCode) throws IOException, JSONException {
        return new JSONObject(getResource(uri, expectedStatusCode));
    }

    /**
     * Get json object from request with expected status code
     *
     * @param request
     * @param expectedStatusCode
     * @return
     */
    protected JSONObject getJsonObject(HttpRequestBase request, HttpStatus expectedStatusCode) throws
            ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, expectedStatusCode));
    }

    /**
     * Get json object from request, expected status code is OK (200)
     *
     * @param request
     * @return
     */
    protected JSONObject getJsonObject(HttpRequestBase request) throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(request, HttpStatus.OK));
    }
}
