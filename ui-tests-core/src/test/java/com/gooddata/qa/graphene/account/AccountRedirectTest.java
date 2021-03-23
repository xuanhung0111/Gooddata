package com.gooddata.qa.graphene.account;

import java.util.UUID;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.graphene.AbstractUITest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import java.io.IOException;
import org.testng.SkipException;
import static org.testng.Assert.assertEquals;

public class AccountRedirectTest extends AbstractUITest {
    private static final String HOST_HEADER = "gooddata.com";
    private String rootUrl;

    @BeforeClass
    public void initProperties() {
        rootUrl = getRootUrl();
    }

    @DataProvider(name = "url-types")
    public Object[][] getUrlTypes() {
        // See rewrite rules in https://github.com/gooddata/gdc-client/blob/develop/httpd/gdc-client/client.conf
        return new Object[][]{
                {"invite/request-%s", "account.html#/registration/confirm/request-%s", 302},
                {"i/request-%s", "account.html#/registration/confirm/request-%s", 302},
                {"l/request-%s", "account.html#/resetPassword/request-%s", 302},
                {"p/request-%s", "account.html#/invitation/request-%s", 302},
                {"login.html/request-%s", "account.html#/login/", 302},
                {"registration.html/request-%s", "account.html#/registration/", 302},
                {"features.html/request-%s", "labs/apps/feature_flags/", 302},
                {"client", "", 301},
                {"client/request-%s", "request-%s", 301}
        };
    }

    @Test(dataProvider = "url-types")
    public void verifyHostPoisoning(String baseTemplate, String expectedLocationTemplate, int expectedStatus) {
        // Note: the platform apache returns absolute redirects, whereas client-demo (grizzly) can return
        // relative redirects and proxied host-based url's so we want to skip the test for client demo
        if (testParams.isClientDemoEnvironment()) {
            throw new SkipException("The test is not intended to be run on client-demo");
        }

        String uuid = UUID.randomUUID().toString().substring(0,10);

        String base = String.format(baseTemplate, uuid);
        String expectedLocation = String.format(expectedLocationTemplate, uuid);

        String uri = rootUrl + base;
        String hostByHeader = "https://" + HOST_HEADER + "/";

        HttpClient client = getClientWithRedirectsDisabled();

        try {
            // first call with Host header
            System.out.println("Checking (1st) " + uri);
            HttpGet firstRequest = new HttpGet(uri);
            firstRequest.addHeader("Host", HOST_HEADER);
            HttpResponse firstResponse = client.execute(firstRequest);
            int firstStatus = firstResponse.getStatusLine().getStatusCode();
            String firstLocation = getResponseLocation(firstResponse);

            // first call favors Host header, but should not be cached
            assertEquals(firstStatus, expectedStatus);
            assertEquals(firstLocation, hostByHeader + expectedLocation);

            System.out.println("Checking (2nd) " + uri);
            HttpGet secondRequest = new HttpGet(uri);
            HttpResponse secondResponse = client.execute(secondRequest);
            int secondStatus = secondResponse.getStatusLine().getStatusCode();
            String secondLocation = getResponseLocation(secondResponse);

            // second call, without host header should respond correct url
            assertEquals(secondStatus, expectedStatus, secondStatus);
            assertEquals(secondLocation, rootUrl + expectedLocation);
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute request", e);
        }
    }

    private HttpClient getClientWithRedirectsDisabled() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.disableRedirectHandling();
        httpClientBuilder.setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return httpClientBuilder.build();
    }

    private String getResponseLocation(HttpResponse response) {
        Header[] values = response.getHeaders("Location");
        return values[0].getValue();
    }
}
