package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import org.json.JSONException;
import org.testng.annotations.Test;

public class AbstractDatasourceManagementTest extends AbstractUITest {
    @Test
    protected void signInDomain() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    protected DataSourceManagementPage initDatasourceManagementPage() {
        openUrl(DataSourceManagementPage.URI);
        return DataSourceManagementPage.getInstance(browser);
    }
}
