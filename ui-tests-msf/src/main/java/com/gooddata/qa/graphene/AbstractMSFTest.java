package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;

public class AbstractMSFTest extends AbstractProjectTest {

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String ADS_INSTANCES_URI = "gdc/datawarehouse/instances/";
    private static final String ADS_INSTANCE_SCHEMA_URI = "/" + ADS_INSTANCES_URI
            + "%s/schemas/default";
    private static final String OUTPUTSTAGE_URI = "/gdc/dataload/projects/%s/outputStage/";
    private static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";

    protected ProjectInfo workingProject;
    protected String maqlFilePath;
    protected String sqlFilePath;
    protected String zipFilePath;

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject = new ProjectInfo(projectTitle, testParams.getProjectId());
        return workingProject;
    }

    protected void updateModelOfGDProject(String maqlFile) {
        System.out.println("Update model of GD project!");
        String pollingUri = sendRequestToUpdateModel(maqlFile);
        RestUtils.waitingForAsyncTask(getRestApiClient(), pollingUri);
        assertEquals(RestUtils.getAsyncTaskStatus(getRestApiClient(), pollingUri), "OK",
                "Model is not updated successfully!");
    }

    protected void dropAddedFieldsInLDM(String maqlFile) {
        String pollingUri = sendRequestToUpdateModel(maqlFile);
        RestUtils.waitingForAsyncTask(getRestApiClient(), pollingUri);
        if (!"OK".equals(RestUtils.getAsyncTaskStatus(getRestApiClient(), pollingUri))) {
            HttpRequestBase getRequest = getRestApiClient().newGetMethod(pollingUri);
            HttpResponse getResponse = getRestApiClient().execute(getRequest);
            String errorMessage = "";
            try {
                errorMessage =
                        new JSONObject(EntityUtils.toString(getResponse.getEntity()))
                                .getJSONObject("wTaskStatus").getJSONArray("messages")
                                .getJSONObject(0).getJSONObject("error").get("message").toString();
            } catch (Exception e) {
                throw new IllegalStateException("There is an exeption when getting error message!",
                        e);
            }

            EntityUtils.consumeQuietly(getResponse.getEntity());

            System.out.println("LDM update is failed with error message: " + errorMessage);
            assertEquals(errorMessage, "The object (%s) doesn't exist.");
        }
    }

    protected void createADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI);
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        String adsUrl;
        try {
            adsUrl =
                    storageForm.createStorage(adsInstance.getName(), adsInstance.getDescription(),
                            adsInstance.getAuthorizationToken());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exception during creating new ads instance! ", e);
        }

        adsInstance.withId(adsUrl.substring(adsUrl.lastIndexOf("/") + 1));
        System.out.println("adsId: " + adsInstance.getId());
    }

    protected void addUserToAdsInstance(ADSInstance adsInstance, String userUri, String user,
            String userRole) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getId() + "/users");
        storageUsersForm.verifyValidAddUserForm();
        try {
            storageUsersForm.fillAddUserToStorageForm(userRole, null, user, true);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exeception when adding user to ads instance!", e);
        }
        takeScreenshot(browser, "datawarehouse-add-user-filled-form", this.getClass());
        assertTrue(browser.getCurrentUrl().contains(userUri.replace("/gdc/account/profile/", "")),
                "The user is not added to ads instance successfully!");
    }

    protected void deleteADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getId());
        InstanceFragment storage =
                createPageFragment(InstanceFragment.class,
                        waitForElementVisible(BY_GP_FORM_SECOND, browser));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorageSuccess();
    }

    protected void setDefaultSchemaForOutputStage(RestApiClient restApiClient, String adsId) {
        String schemaUri = String.format(ADS_INSTANCE_SCHEMA_URI, adsId);
        JSONObject outputStageObj = new JSONObject();
        try {
            outputStageObj.put("outputStage", new JSONObject().put("schema", schemaUri));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when set default schema for outputStage! ",
                    e);
        }

        String putUri = String.format(OUTPUTSTAGE_URI, getWorkingProject().getProjectId());
        String putBody = outputStageObj.toString();
        HttpRequestBase putRequest = restApiClient.newPutMethod(putUri, putBody);
        putRequest.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION);

        HttpResponse putResponse = restApiClient.execute(putRequest);
        int responseStatusCode = putResponse.getStatusLine().getStatusCode();

        System.out.println(putResponse.toString());
        EntityUtils.consumeQuietly(putResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);
        assertEquals(responseStatusCode, HttpStatus.OK.value(),
                "Default schema is not set successfully!");
    }

    protected String uploadZipFileToWebDavWithoutWebContainer(String zipFile) {
        WebDavClient webDav =
                WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = new File(zipFile);
        openUrl(PAGE_GDC);
        waitForElementPresent(gdcFragment.getRoot());
        assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                " Create WebDav storage structure");

        webDav.uploadFile(resourceFile);

        return webDav.getWebDavStructure();
    }

    private String sendRequestToUpdateModel(String maqlFile) {
        String maql = "";
        String pollingUri = "";
        try {
            maql = FileUtils.readFileToString(new File(maqlFile));
            pollingUri =
                    RestUtils.executeMAQL(getRestApiClient(), getWorkingProject().getProjectId(),
                            maql);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exeception during LDM update!", e);
        }

        return pollingUri;
    }
}
