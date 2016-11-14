package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.utils.http.RestUtils.ACCEPT_HEADER_VALUE_WITH_VERSION;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getProductionProjectModelView;
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
import com.gooddata.qa.graphene.ADSTables;
import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.ads.AdsHelper.AdsRole;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.process.ProcessRestUtils;

public class DataloadResourcesPermissionTest extends AbstractMSFTest {

    private static final String INTERNAL_OUTPUT_STAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";
    private static final String MAPPING_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    private static final String OUTPUT_STATE_MODEL_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "model";

    private RestApiClient editorRestApi;
    private RestApiClient viewerRestApi;

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload resources permission test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "initialData" })
    public void initialData() throws JSONException, IOException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
        enableDataExplorer();

        editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getPassword());
        viewerRestApi = getRestApiClient(testParams.getViewerUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = { "initialData" })
    public void cannotAccessToProjectMappingResourceOfOtherUser()
            throws ParseException, JSONException, IOException {
        String otherUser = createDynamicUserFrom(testParams.getUser());
        RestApiClient otherUserRestApiClient = getRestApiClient(otherUser, testParams.getPassword());

        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResource(otherUserRestApiClient,
                otherUserRestApiClient.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.FORBIDDEN);
    }

    @Test(dependsOnGroups = { "initialData" }, priority = 1)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        getAdsHelper().addUserToAdsInstance(ads, testParams.getEditorUser(), AdsRole.DATA_ADMIN);
        getAdsHelper().addUserToAdsInstance(ads, testParams.getViewerUser(), AdsRole.DATA_ADMIN);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void editorAccessToDataloadResources() throws ParseException, JSONException, IOException {
        deleteDataloadProcessAndCreateNewOne();
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(OUTPUT_STATE_MODEL_RESOURCE, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(AdsHelper.OUTPUT_STAGE_URI, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.OK);
        RestUtils.getResource(editorRestApi,
                editorRestApi.newGetMethod(format(AdsHelper.OUTPUT_STAGE_METADATA_URI, testParams.getProjectId())),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
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
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.FORBIDDEN);
    }

    @Test(dependsOnMethods = { "addUsersToProjects" }, priority = 2)
    public void allProjectMembersCanAccessToProjectModelView() throws ParseException, JSONException, IOException {
        getProductionProjectModelView(getRestApiClient(), testParams.getProjectId(), false);
        getProductionProjectModelView(editorRestApi, testParams.getProjectId(), false);
        getProductionProjectModelView(viewerRestApi, testParams.getProjectId(), false);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        getAdsHelper().removeAds(ads);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }
}
