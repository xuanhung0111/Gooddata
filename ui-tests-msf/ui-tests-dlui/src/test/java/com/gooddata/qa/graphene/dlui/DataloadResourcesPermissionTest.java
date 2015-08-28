package com.gooddata.qa.graphene.dlui;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.utils.AdsHelper.AdsRole;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;

public class DataloadResourcesPermissionTest extends AbstractMSFTest {

    private RestApiClient editorRestApi;
    private RestApiClient viewerRestApi;

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload resources permission test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "initialData" })
    public void initialData() throws JSONException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();

        editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        viewerRestApi = getRestApiClient(testParams.getViewerUser(), testParams.getViewerPassword());
    }

    @Test(dependsOnGroups = { "initialData" })
    public void cannotAccessToProjectMappingResourceOfOtherUser()
            throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.FORBIDDEN,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
    }

    @Test(dependsOnGroups = { "initialData" }, priority = 1)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        RestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), testParams.getEditorUser(), 
                UserRoles.EDITOR);
        RestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), testParams.getViewerUser(), 
                UserRoles.VIEWER);
        adsHelper.addUserToAdsInstance(ads, testParams.getEditorUser(), AdsRole.DATA_ADMIN);
        adsHelper.addUserToAdsInstance(ads, testParams.getViewerUser(), AdsRole.DATA_ADMIN);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void editorAccessToDataloadResources() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.OK,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(OUTPUT_STATE_MODEL_RESOURCE, testParams.getProjectId()), HttpStatus.OK,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(OUTPUTSTAGE_URI, testParams.getProjectId()), HttpStatus.OK,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(OUTPUT_STAGE_METADATA_URI, testParams.getProjectId()), HttpStatus.OK,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
        RestUtils.getResource(editorRestApi, executeDataloadProcessSuccessfully(editorRestApi),
                HttpStatus.NO_CONTENT);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void viewerCannotAccessToMappingResource() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResourceWithCustomAcceptHeader(viewerRestApi,
                String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.FORBIDDEN,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void allProjectMembersCanAccessToProjectModelView() throws ParseException, JSONException, IOException {
        accessToProjectModelView(getRestApiClient());
        accessToProjectModelView(editorRestApi);
        accessToProjectModelView(viewerRestApi);
    }

    @AfterClass
    public void cleanUp() {
        deleteADSInstance(ads);
    }

    private void accessToProjectModelView(RestApiClient restApiClient) {
        RestUtils.getResource(restApiClient, String.format(PROJECT_MODEL_VIEW, testParams.getProjectId()),
                HttpStatus.ACCEPTED);
    }
}
