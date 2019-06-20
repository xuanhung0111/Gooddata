package com.gooddata.qa.graphene.disc.process;

import static com.gooddata.qa.graphene.AbstractTest.Profile.EDITOR;
import static com.gooddata.qa.utils.snowflake.DataSourceRestRequest.DATA_SOURCE_REST_URI;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.common.AbstractADDProcessTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class DeployProcessTest extends AbstractADDProcessTest {

    private static final String SUPPORT_URI =
            "https://help.gooddata.com/display/doc/Data+Preparation+and+Distribution#DataPreparationandDistribution-Bricks";

    @Override
    protected void addUsersWithOtherRolesToProject()
            throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkUIForDeployDialogWithAdminUser() throws IOException {
        String prefix = generateHashString();
        String specialDataSourceTitle = "♔ ♕ ♖ ♗ ♘ ♙ ♚ ♛ ♜ ♝ ♞ ♟" + prefix;
        String dataSourceTitle = generateDataSourceTitle();
        String specialDataSourceID = createDefaultDataSource(specialDataSourceTitle);
        DataSourceRestRequest dataSourceRestRequestByEditor =
                new DataSourceRestRequest(new RestClient(getProfile(EDITOR)), testParams.getProjectId());
        ConnectionInfo connectionInfo = new ConnectionInfo().setWarehouse(WAREHOUSE_NAME)
                .setDatabase(DATABASE_NAME)
                .setSchema(SCHEMA_NAME)
                .setUserName(USER_NAME)
                .setPassword(PASSWORD)
                .setUrl(JDBC_URL);
        HttpRequestBase httpRequestBase = dataSourceRestRequestByEditor
                .setupDataSourceRequest(connectionInfo, dataSourceTitle, OPTIONAL_PREFIX);
        dataSourceRestRequestByEditor.createDataSource(
                new CommonRestRequest(new RestClient(getProfile(EDITOR)), testParams.getProjectId()), httpRequestBase);
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            assertTrue(projectDetailPage.isDeployProcessFormVisible(), "Deploy Process Form should be displayed");
            assertEquals(deployForm.getRedirectedPageFromLearnMore(), SUPPORT_URI);
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
            assertTrue(deploySDDProcessDialog.getRedirectedPageFromManageDataSource().contains(DATA_SOURCE_REST_URI));
            deploySDDProcessDialog.selectDataSource("_______________________" + prefix); // get css from specialTitle
            assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), specialDataSourceTitle);
            assertFalse(dataSourceRestRequest.getAllDataSourceNames().contains(dataSourceTitle), "Editor's Data source shouldn't display");
        } finally {
            dataSourceRestRequest.deleteDataSource(specialDataSourceID);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkUIForDeployDialogWithDomainUser() throws IOException {
        String prefix = generateHashString();
        String specialDataSourceTitle = "<h1 style=\"color:red\">This is dataSource</h1>" + prefix;
        String dataSourceID = createDefaultDataSource(specialDataSourceTitle);
        //create segment
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            assertTrue(projectDetailPage.isDeployProcessFormVisible(), "Deploy Process Form should be displayed");
            assertEquals(deployForm.getRedirectedPageFromLearnMore(), SUPPORT_URI);
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
            assertTrue(deploySDDProcessDialog.getRedirectedPageFromManageDataSource().contains(DATA_SOURCE_REST_URI));
            deploySDDProcessDialog.selectDataSource("_h1_style__color_red__this_is_datasource__h1_" + prefix);
            assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), specialDataSourceTitle);
            //Will be refactored at next PR, because can't mix LCM & DISC together
            deploySDDProcessDialog.selectScope(DeploySDDProcessDialog.Scope.SEGMENT).selectSegment("att_segment_08c61");
        } finally {
            dataSourceRestRequest.deleteDataSource(dataSourceID);
        }
    }
//    Issue: domain user, will be unmark next PR
//    @Test(dependsOnGroups = {"createProject"})
//    public void checkUIForDeployDialogWithoutSegmentAndDataSource() {
//        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
//        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
//        DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
//        deploySDDProcessDialog.getSelectedDataSourceName();
//        deploySDDProcessDialog.selectScope(DeploySDDProcessDialog.Scope.SEGMENT).getSegment();
//    }
}
