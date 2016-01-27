package com.gooddata.qa.utils.http;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public final class RestUtils {

    public static final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";

    private RestUtils() {
    }

    public static void executeRequest(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) {
        try {
            final HttpResponse response = restApiClient.execute(request, expectedStatusCode, "Invalid status code");
            EntityUtils.consumeQuietly(response.getEntity());
        } finally {
            request.releaseConnection();
        }
    }

    public static int executeRequest(final RestApiClient restApiClient, final HttpRequestBase request) {
        try {
            final HttpResponse response = restApiClient.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(response.getEntity());
            return statusCode;
        } finally {
            request.releaseConnection();
        }
    }

    public static String getResource(final RestApiClient restApiClient, final HttpRequestBase request,
            final Consumer<HttpRequestBase> setupRequest, final HttpStatus expectedStatusCode)
                    throws ParseException, IOException {
        setupRequest.accept(request);

        try {
            final HttpResponse response = restApiClient.execute(request, expectedStatusCode, "Invalid status code");
            final HttpEntity entity = response.getEntity();

            final String ret = isNull(entity) ? "" : EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            return ret;

        } finally {
            request.releaseConnection();
        }
    }

    public static String getResource(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(restApiClient,
                request,
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                expectedStatusCode);
    }

    public static String getResource(final RestApiClient restApiClient, final String uri,
            final HttpStatus expectedStatusCode) throws ParseException, IOException {
        return getResource(restApiClient,
                restApiClient.newGetMethod(uri),
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                expectedStatusCode);
    }

    public static JSONObject getJsonObject(final RestApiClient restApiClient, final String uri)
            throws IOException, JSONException {
        return getJsonObject(restApiClient, uri, HttpStatus.OK);
    }

    public static JSONObject getJsonObject(final RestApiClient restApiClient, final String uri,
            final HttpStatus expectedStatusCode) throws IOException, JSONException {
        return new JSONObject(getResource(restApiClient, uri, expectedStatusCode));
    }

    public static JSONObject getJsonObject(final RestApiClient restApiClient, final HttpRequestBase request,
            final HttpStatus expectedStatusCode) throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(restApiClient, request, expectedStatusCode));
    }

    public static JSONObject getJsonObject(final RestApiClient restApiClient, final HttpRequestBase request)
            throws ParseException, JSONException, IOException {
        return new JSONObject(getResource(restApiClient, request, HttpStatus.OK));
    }

    public static void deleteObject(final RestApiClient restApiClient, final String uri) {
        executeRequest(restApiClient, restApiClient.newDeleteMethod(uri));
    }
}
