package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.SnowflakeDetail;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.SnowflakeEdit;
import org.json.JSONException;
import org.testng.annotations.Test;

public class SnowflakeDataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private SnowflakeEdit snowflakeEdit;
    private SnowflakeDetail snowflakeDetail;

    @Test(dependsOnMethods = {"signInDomain"})
    public void initTest() throws JSONException {
        dataSourceManagementPage = initDatasourceManagementPage();
    }
}
