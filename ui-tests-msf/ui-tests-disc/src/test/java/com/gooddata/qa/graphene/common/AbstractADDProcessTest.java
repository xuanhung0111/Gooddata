package com.gooddata.qa.graphene.common;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static java.lang.String.format;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.time.LocalTime;

/**
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
public class AbstractADDProcessTest extends AbstractProcessTest {

    protected static final String WAREHOUSE_NAME = "ATT_WAREHOUSE";
    protected static final String SCHEMA_NAME = "PUBLIC";
    protected static final String DATABASE_NAME = "ATT_DATABASE";
    protected static final String JDBC_URL = "jdbc:snowflake://gooddata.snowflakecomputing.com";
    protected static final String USER_NAME = "autoqasnowflake";
    protected static final String PASSWORD = "Automation2017"; // replace later
    protected static final String OPTIONAL_PREFIX = "PRE_";
    protected CommonRestRequest commonRestRequest;
    protected DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dataSourceRestRequest = new DataSourceRestRequest(getAdminRestClient(), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    protected String generateScheduleName() {
        return "schedule-" + generateHashString();
    }

    protected String generateDataSourceTitle() {
        return "data-source-" + generateHashString();
    }

    protected String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    protected String createDataSource(String datasourceTitle, ConnectionInfo connectionInfo) throws IOException {
        HttpRequestBase httpRequestBase = dataSourceRestRequest.setupDataSourceRequest(connectionInfo, datasourceTitle, OPTIONAL_PREFIX);
        return dataSourceRestRequest.createDataSource(commonRestRequest, httpRequestBase);
    }

    protected String createDefaultDataSource(String datasourceTitle) throws IOException {
        return createDataSource(datasourceTitle, createDefaultConnectionInfo());
    }

    protected ConnectionInfo createDefaultConnectionInfo() {
        return new ConnectionInfo().setWarehouse(WAREHOUSE_NAME)
                .setDatabase(DATABASE_NAME)
                .setSchema(SCHEMA_NAME)
                .setUserName(USER_NAME)
                .setPassword(PASSWORD)
                .setUrl(JDBC_URL);
    }
}
