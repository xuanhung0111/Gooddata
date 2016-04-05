package com.gooddata.qa.graphene.dlui;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.utils.AdsHelper.AdsRole;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.process.ProcessRestUtils;

public class DataloadResourcesPermissionTest extends AbstractMSFTest {

    private RestApiClient editorRestApi;
    private RestApiClient viewerRestApi;

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload resources permission test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "initialData" })
    public void initialData() throws JSONException, IOException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
        enableDataExplorer();

        editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        viewerRestApi = getRestApiClient(testParams.getViewerUser(), testParams.getViewerPassword());
    }

    @Test(dependsOnGroups = { "initialData" })
    public void cannotAccessToProjectMappingResourceOfOtherUser()
            throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.FORBIDDEN);
    }

    @Test(dependsOnGroups = { "initialData" }, priority = 1)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        addUsersWithOtherRolesToProject();
        getAdsHelper().addUserToAdsInstance(ads, testParams.getEditorUser(), AdsRole.DATA_ADMIN);
        getAdsHelper().addUserToAdsInstance(ads, testParams.getViewerUser(), AdsRole.DATA_ADMIN);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void editorAccessToDataloadResources() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(OUTPUT_STATE_MODEL_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(OUTPUTSTAGE_URI, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(OUTPUT_STAGE_METADATA_URI, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.OK);

        final String uri = DataloadProcess.TEMPLATE
                .expand(testParams.getProjectId(), getDataloadProcessId()).toString() + "/executions";
        final String executionUri = ProcessRestUtils.executeProcess(restApiClient, uri, "", SYNCHRONIZE_ALL_PARAM);
        assertTrue(isExecutionSuccessful(restApiClient, executionUri),
                "Process execution is not successful!");
        RestUtils.getResource(editorRestApi, executionUri, HttpStatus.NO_CONTENT);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void viewerCannotAccessToMappingResource() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResource(viewerRestApi,
                viewerRestApi.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.FORBIDDEN);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void allProjectMembersCanAccessToProjectModelView() throws ParseException, JSONException, IOException {
        accessToProjectModelView(getRestApiClient());
        accessToProjectModelView(editorRestApi);
        accessToProjectModelView(viewerRestApi);
    }

    @AfterClass
    public void cleanUp() {
        getAdsHelper().removeAds(ads);
    }

    private void accessToProjectModelView(RestApiClient restApiClient) throws ParseException, IOException {
        RestUtils.getResource(restApiClient, format(PROJECT_MODEL_VIEW, testParams.getProjectId()),
                HttpStatus.ACCEPTED);
    }
}
