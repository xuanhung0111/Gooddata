package com.gooddata.qa.utils.snowflake;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.http.client.methods.HttpRequestBase;

import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;

public class DataSourceUtils {
    public static final String DATA_LOADING_COMPONENT = "Automated Data Distribution";
    public static final String WAREHOUSE_NAME = "ATT_WAREHOUSE";
    public static final String SCHEMA_NAME = "PUBLIC";
    public static final String LOCAL_STAGE = "localStage";
    public Supplier<Parameters> defaultParameters;
    public ProcessExecutionDetail processExecutionDetail;
    public RestClient domainRestClient;
    public ConnectionInfo connectionInfo;

    public CommonRestRequest commonRestRequest;
    public HttpRequestBase setupDataSourceRequest;
    public DataSourceRestRequest dataSourceRestRequest;
    public String password;
    public String jdbcUrl;
    public String userName;
    public TestParameters testParams;

    public DataSourceUtils(final TestParameters testParams) {
        this.testParams = testParams;
        this.domainRestClient = new RestClient(
                new RestProfile(this.testParams.getHost(), this.testParams.getDomainUser(), this.testParams.getPassword(), true));
        this.dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, this.testParams.getProjectId());
        this.commonRestRequest = new CommonRestRequest(domainRestClient, this.testParams.getProjectId());
    }

    public DataSourceRestRequest getDataSourceRestRequest() {
        return dataSourceRestRequest;
    }

    public ConnectionInfo createConnectionInfo(String database) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        // data source info
        password = testParams.getSnowflakePassword();
        jdbcUrl = testParams.getSnowflakeJdbcUrl();
        userName = testParams.getSnowflakeUserName();
        connectionInfo.setWarehouse(WAREHOUSE_NAME);
        connectionInfo.setDatabase(database);
        connectionInfo.setSchema(SCHEMA_NAME);
        connectionInfo.setUserName(userName);
        connectionInfo.setPassword(password);
        connectionInfo.setUrl(jdbcUrl);
        return connectionInfo;
    }

    public String createDataSource(String database, String prefix, String datasourceName, ConnectionInfo info) {
        try {
            setupDataSourceRequest = dataSourceRestRequest.setupDataSourceRequest(info, datasourceName, prefix);
            return dataSourceRestRequest.createDataSource(commonRestRequest, setupDataSourceRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRestRequest(String user) {
        setDataSourceRestRequest(user);
        setCommonRestRequest(user);
    }

    public DataSourceRestRequest setDataSourceRestRequest(String user) {
        RestClient restClient = new RestClient(new RestClient.RestProfile(testParams.getHost(), user, testParams.getPassword(), true));
        dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        return dataSourceRestRequest;
    }

    public CommonRestRequest setCommonRestRequest(String user) {
        RestClient restClient = new RestClient(new RestClient.RestProfile(testParams.getHost(), user, testParams.getPassword(), true));
        commonRestRequest = new CommonRestRequest(restClient, testParams.getProjectId());
        return commonRestRequest;
    }


}
