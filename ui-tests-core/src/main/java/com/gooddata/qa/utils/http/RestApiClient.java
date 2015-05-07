/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http;


import com.gooddata.http.client.GoodDataHttpClient;
import com.gooddata.http.client.LoginSSTRetrievalStrategy;
import com.gooddata.http.client.SSTRetrievalStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;

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

    public static final String API_PROXY_HOST = "na-apiproxy-dev.na.getgooddata.com";

    private final HttpHost httpHost;
    private final HttpClient httpClient;

    public RestApiClient(String host, String user, String password, boolean useSST, boolean useApiProxy) {
        httpHost = new HttpHost(host, 443, "https");
        httpClient = getGooddataHttpClient(httpHost, user, password, useSST, useApiProxy);
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

    protected HttpClient getGooddataHttpClient(HttpHost hostGoodData, String user, String password, boolean useSST, boolean useApiProxy) {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        if (useSST) {
            HttpClient httpClient = httpClientBuilder.build();
            SSTRetrievalStrategy sstStrategy = new LoginSSTRetrievalStrategy(httpClient, hostGoodData, user, password);
            return new GoodDataHttpClient(httpClient, sstStrategy);
        } else {
            final CredentialsProvider provider = new BasicCredentialsProvider();
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
            if (useApiProxy) {
                System.out.println("Creating a client with basic authentication and proxy to " + API_PROXY_HOST);
                final HttpHost proxyHost = new HttpHost(API_PROXY_HOST, 80);
                httpClientBuilder.setProxy(proxyHost);
            }
            return httpClientBuilder.build();
        }
    }
}
