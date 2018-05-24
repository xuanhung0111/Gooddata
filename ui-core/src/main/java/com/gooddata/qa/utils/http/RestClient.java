package com.gooddata.qa.utils.http;

import com.gooddata.GoodData;
import com.gooddata.account.AccountService;
import com.gooddata.connector.ConnectorService;
import com.gooddata.dataload.OutputStageService;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.dataset.DatasetService;
import com.gooddata.featureflag.FeatureFlagService;
import com.gooddata.gdc.DataStoreService;
import com.gooddata.gdc.GdcService;
import com.gooddata.http.client.GoodDataHttpClient;
import com.gooddata.http.client.LoginSSTRetrievalStrategy;
import com.gooddata.http.client.SSTRetrievalStrategy;
import com.gooddata.md.MetadataService;
import com.gooddata.md.maintenance.ExportImportService;
import com.gooddata.model.ModelService;
import com.gooddata.notification.NotificationService;
import com.gooddata.project.ProjectService;
import com.gooddata.report.ReportService;
import com.gooddata.warehouse.WarehouseService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import java.io.IOException;

import static java.lang.String.format;

public class RestClient {

    private final static int DEFAULT_PORT = 443;

    private RestProfile profile;
    private HttpClient client;
    private GoodData goodDataClient;

    public RestClient(RestProfile profile) {
        this(profile, null);
    }

    public RestClient(RestProfile profile, HttpRequestRetryHandler handler) {
        this.profile = profile;

        client = getGooddataHttpClient(profile.getUsername(), profile.getPassword(),
                profile.isUseSST(), handler);

        goodDataClient = new GoodData(profile.getHost().getHostName(), profile.getUsername(),
                profile.getPassword(), profile.getHost().getPort());
    }

    public HttpResponse execute(HttpRequestBase request) {
        try {
            return client.execute(profile.getHost(), request);
        } catch (IOException e) {
            throw new RuntimeException("Cannot execute request", e);
        }
    }

    public HttpResponse execute(HttpRequestBase request, int expectedStatusCode) {
        try {
            ResponseHandler<HttpResponse> handler = response -> {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != expectedStatusCode) {
                    String errorMsg = format("%s expected code [%d], but got [%d]",
                            request.getURI(), expectedStatusCode, statusCode);

                    // get error msg in response if any
                    String content = getResponseError(response);
                    if (StringUtils.isNotEmpty(content)) {
                        errorMsg += "\n" + content;
                    }
                    throw new InvalidStatusCodeException(errorMsg, statusCode);
                }
                return response;
            };

            return client.execute(profile.getHost(), request, handler);

        } catch (IOException e) {
            throw new RuntimeException("Cannot execute request", e);
        }
    }

    public HttpResponse execute(HttpRequestBase request, HttpStatus expectedStatus) {
        return execute(request, expectedStatus.value());
    }

    public ProjectService getProjectService() {
        return goodDataClient.getProjectService();
    }

    public AccountService getAccountService() {
        return goodDataClient.getAccountService();
    }

    public MetadataService getMetadataService() {
        return goodDataClient.getMetadataService();
    }

    public ModelService getModelService() {
        return goodDataClient.getModelService();
    }

    public GdcService getGdcService() {
        return goodDataClient.getGdcService();
    }

    public DataStoreService getDataStoreService() {
        return goodDataClient.getDataStoreService();
    }

    public DatasetService getDatasetService() {
        return goodDataClient.getDatasetService();
    }

    public ReportService getReportService() {
        return goodDataClient.getReportService();
    }

    public ProcessService getProcessService() {
        return goodDataClient.getProcessService();
    }

    public WarehouseService getWarehouseService() {
        return goodDataClient.getWarehouseService();
    }

    public ConnectorService getConnectorService() {
        return goodDataClient.getConnectorService();
    }

    public NotificationService getNotificationService() {
        return goodDataClient.getNotificationService();
    }

    public ExportImportService getExportImportService() {
        return goodDataClient.getExportImportService();
    }

    public FeatureFlagService getFeatureFlagService() {
        return goodDataClient.getFeatureFlagService();
    }

    public OutputStageService getOutputStageService() {
        return goodDataClient.getOutputStageService();
    }

    private static HttpHost parseHost(String host) {
        String[] parts = host.split(":");
        String hostName = parts[0];
        int port;
        if (parts.length == 2) {
            port = Integer.parseInt(parts[1]);
        } else {
            port = DEFAULT_PORT;
        }
        return new HttpHost(hostName, port, "https");
    }

    private HttpClient getGooddataHttpClient(String user, String password, boolean useSST,
                                             HttpRequestRetryHandler retryHandler) {
        final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setRetryHandler(retryHandler);
        httpClientBuilder.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        if (useSST) {
            HttpClient httpClient = httpClientBuilder.build();
            SSTRetrievalStrategy sstStrategy = new LoginSSTRetrievalStrategy(user, password);
            return new GoodDataHttpClient(httpClient, profile.getHost(), sstStrategy);
        } else {
            final CredentialsProvider provider = new BasicCredentialsProvider();
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(provider);
            return httpClientBuilder.build();
        }
    }

    private String getResponseError(HttpResponse response) {
        String content = "";
        try {
            content = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("error")) {
                return content;
            }
            return "";
        } catch (IOException ioe) {
            throw new RuntimeException("There is an error while reading response", ioe);
        } catch (JSONException je) {
            // Incase a bad request(400) returned, response content is html not json
            return content;
        }
    }

    public static class RestProfile {
        private HttpHost host;
        private String username;
        private String password;
        private boolean useSST;

        public RestProfile(String host, String username, String password, boolean useSST) {
            this.host = parseHost(host);
            this.username = username;
            this.password = password;
            this.useSST = useSST;
        }

        public void setHost(String host) {
            this.host = parseHost(host);
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setUseSST(boolean useSST) {
            this.useSST = useSST;
        }

        public HttpHost getHost() {
            return host;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isUseSST() {
            return useSST;
        }
    }
}
