package com.gooddata.qa.graphene.dlui;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;

public class DataloadResourcesPermissionTest extends AbstractMSFTest {

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload resources permission test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "initialData" })
    public void initialData() throws JSONException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
    }

    @Test(dependsOnGroups = { "initialData" }, groups = { "DataloadResourcesPermissionTest" })
    public void cannotAccessToProjectMappingResourceOfOtherUser()
            throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        RestApiClient editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.FORBIDDEN,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
    }

    @Test(dependsOnGroups = { "initialData" }, groups = { "DataloadResourcesPermissionTest" }, priority = 1)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(), testParams.getUser(),
                testParams.getPassword(), testParams.getEditorProfileUri(), UserRoles.EDITOR);
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(), testParams.getUser(),
                testParams.getPassword(), testParams.getViewerProfileUri(), UserRoles.VIEWER);
        addUserToAdsInstance(adsInstance, testParams.getEditorProfileUri(), testParams.getEditorUser(),
                "dataAdmin");
        addUserToAdsInstance(adsInstance, testParams.getViewerProfileUri(), testParams.getViewerUser(),
                "dataAdmin");
    }

    @Test(dependsOnMethods = { "addUsersToProjects" },
            groups = { "DataloadResourcesPermissionTest" },priority = 2)
    public void editorAccessToDataloadResources() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestApiClient editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
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

    @Test(dependsOnMethods = { "addUsersToProjects" },
            groups = { "DataloadResourcesPermissionTest" }, priority = 2)
    public void viewerCannotAccessToMappingResource() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestApiClient viewerRestApi = getRestApiClient(testParams.getViewerUser(), testParams.getViewerPassword());
        RestUtils.getResourceWithCustomAcceptHeader(viewerRestApi,
                String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.FORBIDDEN,
                ACCEPT_APPLICATION_JSON_WITH_VERSION);
    }

    @Test(dependsOnGroups = "DataloadResourcesPermissionTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }
}
