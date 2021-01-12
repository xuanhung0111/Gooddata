package com.gooddata.qa.utils.cloudresources;

import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.OPTIONAL_PREFIX;

import java.io.IOException;
import java.util.function.Supplier;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;

public class DataSourceUtils {

    public static final String WAREHOUSE_NAME = "ATT_WAREHOUSE";
    public static final String SCHEMA_NAME = "PUBLIC";
    public static final String LOCAL_STAGE = "localStage";

    public Supplier<Parameters> defaultParameters;
    private CommonRestRequest commonRestRequest;
    private DataSourceRestRequest dataSourceRestRequest;
    private TestParameters testParams;

    public DataSourceUtils(String user) {
        this.testParams = TestParameters.getInstance();
        RestClient restClient = new RestClient(
                new RestProfile(testParams.getHost(), user, testParams.getPassword(), true));
        this.dataSourceRestRequest = new DataSourceRestRequest(restClient, testParams.getProjectId());
        this.commonRestRequest = new CommonRestRequest(restClient, testParams.getProjectId());
    }

    public ConnectionInfo createSnowflakeConnectionInfo(String database, DatabaseType dbType) {
        return new ConnectionInfo()
                .setWarehouse(WAREHOUSE_NAME)
                .setDbType(dbType)
                .setDatabase(database)
                .setSchema(SCHEMA_NAME)
                .setUserName(testParams.getSnowflakeUserName())
                .setPassword(testParams.getSnowflakePassword())
                .setUrl(testParams.getSnowflakeJdbcUrl());
    }

    public ConnectionInfo createSnowflakeUseCustomSchema(String database, DatabaseType dbType, String schema) {
        return new ConnectionInfo()
                .setWarehouse(WAREHOUSE_NAME)
                .setDbType(dbType)
                .setDatabase(database)
                .setSchema(schema)
                .setUserName(testParams.getSnowflakeUserName())
                .setPassword(testParams.getSnowflakePassword())
                .setUrl(testParams.getSnowflakeJdbcUrl());
    }

    public ConnectionInfo createRedshiftConnectionInfo(String database, DatabaseType dbType, String schema) {
        return new ConnectionInfo()
                .setDbType(dbType)
                .setDatabase(database)
                .setSchema(schema)
                .setUserName(testParams.getRedshiftUserName())
                .setPassword(testParams.getRedshiftPassword())
                .setUrl(testParams.getRedshiftJdbcUrl());
    }

    public ConnectionInfo createBigQueryConnectionInfo(String project, DatabaseType dbType, String schema) {
        return new ConnectionInfo()
                .setDbType(dbType)
                .setSchema(schema)
                .setProject(project)
                .setClientEmail(testParams.getBigqueryClientEmail())
                .setPrivateKey(testParams.getBigqueryPrivateKey());
    }

    public String createDataSource(ConnectionInfo connectionInfo, String datasourceName, String... optionalPrefix) throws IOException {
        return dataSourceRestRequest.createDataSource(
                commonRestRequest, dataSourceRestRequest.setupDataSourceRequest(connectionInfo, datasourceName, optionalPrefix));
    }

    public String createDataSource(String datasourceTitle, ConnectionInfo connectionInfo) throws IOException {
        return createDataSource(connectionInfo, datasourceTitle, OPTIONAL_PREFIX);
    }

    public String createDefaultDataSource(String datasourceTitle, String databaseTitle, DatabaseType dbType) throws IOException {
        return createDataSource(datasourceTitle, createSnowflakeConnectionInfo(databaseTitle, dbType));
    }

    public DataSourceRestRequest getDataSourceRestRequest() {
        return dataSourceRestRequest;
    }

}
