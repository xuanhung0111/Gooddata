package com.gooddata.qa.graphene.add;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.EDITOR;
import static com.gooddata.qa.graphene.AbstractTest.Profile.VIEWER;
import static com.gooddata.qa.utils.ads.AdsHelper.OUTPUT_STAGE_METADATA_URI;
import static com.gooddata.qa.utils.ads.AdsHelper.OUTPUT_STAGE_URI;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.ads.AdsHelper;

public class DataloadResourcesPermissionTest extends AbstractDataloadProcessTest {

    private static final String INTERNAL_OUTPUT_STAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";
    private static final String OUTPUT_STAGE_MAPPING_URI = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    private static final String OUTPUT_STAGE_MODEL_URI = INTERNAL_OUTPUT_STAGE_URI + "model";

    private RestClient adminRestClient;
    private RestClient editorRestClient;
    private RestClient viewerRestClient;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);

        // this should be used after completing QA-7283
        adminRestClient = new RestClient(getProfile(ADMIN));
        editorRestClient = new RestClient(getProfile(EDITOR));
        viewerRestClient = new RestClient(getProfile(VIEWER));
    }

    @DataProvider(name = "accessDataloadResourceProvider")
    public Object[][] getAccessDataloadResourceProvider() throws ParseException, JSONException, IOException {
        String otherUser = createDynamicUserFrom(testParams.getUser());
        RestClient otherRestClient = new RestClient(new RestProfile(
                testParams.getHost(), otherUser, testParams.getPassword(), true));
        return new Object[][] {
            {viewerRestClient},
            {otherRestClient}
        };
    }

    @Test(dependsOnGroups = {"initDataload"}, dataProvider = "accessDataloadResourceProvider")
    public void cannotAccessDataloadResource(RestClient restClient) throws IOException, JSONException {
        final String errorMessage = "User does not have required permission ('canEnrichData')";
        final CommonRestRequest restRequest = new CommonRestRequest(restClient, testParams.getProjectId());

        JSONObject outputStageMapping = restRequest.getJsonObject(format(OUTPUT_STAGE_MAPPING_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageMapping.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputStageModel = restRequest.getJsonObject(format(OUTPUT_STAGE_MODEL_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageModel.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputState = restRequest.getJsonObject(format(AdsHelper.OUTPUT_STAGE_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputState.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputStageMetadata = restRequest.getJsonObject(
                format(AdsHelper.OUTPUT_STAGE_METADATA_URI, testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageMetadata.getJSONObject("error").getString("message"), errorMessage);
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void editorCanAccessDataloadResources() throws ParseException, JSONException, IOException {
        final CommonRestRequest restRequest = new CommonRestRequest(editorRestClient, testParams.getProjectId());
        JSONObject outputStageMapping = restRequest.getJsonObject(format(OUTPUT_STAGE_MAPPING_URI,
                testParams.getProjectId()));
        assertTrue(outputStageMapping.has("mappingDefinition"), "Output stage mapping show wrong!");

        JSONObject outputStageModel = restRequest.getJsonObject(
                format(OUTPUT_STAGE_MODEL_URI, testParams.getProjectId()));
        assertTrue(outputStageModel.has("outputStageModel"), "Output stage model show wrong!");

        JSONObject outputState = restRequest.getJsonObject(format(OUTPUT_STAGE_URI,
                testParams.getProjectId()));
        assertTrue(outputState.has("outputStage"), "Output stage show wrong!");

        JSONObject outputStageMetadata = restRequest.getJsonObject(
                format(OUTPUT_STAGE_METADATA_URI, testParams.getProjectId()));
        assertTrue(outputStageMetadata.has("outputStageMetadata"), "Output stage metadata show wrong!");
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void canAccessToProjectModelView() throws ParseException, JSONException, IOException {
        JSONObject adminModelView = new ModelRestRequest(adminRestClient, testParams.getProjectId())
                .getProductionProjectModelView(false);
        assertTrue(adminModelView.has("projectModelView"), "Model view structure shows wrong!");

        JSONObject editorModelView = new ModelRestRequest(editorRestClient, testParams.getProjectId())
                .getProductionProjectModelView(false);
        assertTrue(editorModelView.has("projectModelView"), "Model view structure shows wrong!");

        JSONObject viewerModelView = new ModelRestRequest(viewerRestClient, testParams.getProjectId())
                .getProductionProjectModelView(false);
        assertTrue(viewerModelView.has("projectModelView"), "Model view structure shows wrong!");
    }
}
