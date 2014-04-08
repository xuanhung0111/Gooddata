/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http;


import com.gooddata.http.client.GoodDataHttpClient;
import com.gooddata.http.client.LoginSSTRetrievalStrategy;
import com.gooddata.http.client.SSTRetrievalStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Wrapper for Gooddata REST API client which simplifies its usage
 *
 * @see #execute(org.apache.http.client.methods.HttpRequestBase)
 * @see #newGetMethod(String)
 * @see #newDeleteMethod(String)
 * @see #newPostMethod(String, String)
 * @see #newPutMethod(String, String)
 */
public class RestApiClient {

    private final HttpHost httpHost;
    private final HttpClient httpClient;

    public RestApiClient(String host, String user, String password) {
        httpHost = new HttpHost(host, 443, "https");
        httpClient = getGooddataHttpClient(httpHost, user, password);
    }

    public HttpHost getHttpHost() {
        return httpHost;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public HttpResponse execute(HttpRequestBase request) {
        try {
            return httpClient.execute(httpHost, request);
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute request", e);
        }
    }

    public HttpResponse execute(HttpRequestBase request, int expectedStatusCode) {
        HttpResponse response = execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != expectedStatusCode) {
            throw new InvalidStatusCodeException("Unexpected status code", statusCode);
        }
        return response;
    }

    public HttpGet newGetMethod(String uri) {
        HttpGet getMethod = new HttpGet(uri);
        setAcceptHeader(getMethod);
        return getMethod;
    }

    public HttpPost newPostMethod(String uri, String content) {
        HttpPost postMethod = new HttpPost(uri);
        postMethod.setEntity(getEntity(content));
        setAcceptHeader(postMethod);
        return postMethod;
    }

    public HttpPut newPutMethod(String uri, String content) {
        HttpPut putMethod = new HttpPut(uri);
        putMethod.setEntity(getEntity(content));
        setAcceptHeader(putMethod);
        return putMethod;
    }

    public HttpDelete newDeleteMethod(String uri) {
        HttpDelete deleteMethod = new HttpDelete(uri);
        setAcceptHeader(deleteMethod);
        return deleteMethod;
    }

    protected AbstractHttpEntity getEntity(String content) {
        try {
            StringEntity entity = new StringEntity(content);
            entity.setContentType("application/json");
            return entity;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setAcceptHeader(HttpRequestBase requestBase) {
        requestBase.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
    }

    protected HttpClient getGooddataHttpClient(HttpHost hostGoodData, String user, String password) {
        SSTRetrievalStrategy sstStrategy = new LoginSSTRetrievalStrategy(new DefaultHttpClient(), hostGoodData, user, password);
        return new GoodDataHttpClient(new DefaultHttpClient(), sstStrategy);
    }
}
