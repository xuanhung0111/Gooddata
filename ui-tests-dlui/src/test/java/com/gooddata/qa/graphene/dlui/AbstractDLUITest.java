package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.dlui.ADSInstance;
import com.gooddata.qa.graphene.entity.dlui.ProcessInfo;
import com.gooddata.qa.graphene.enums.dlui.DLUIProcessParameters;
import com.gooddata.qa.graphene.fragments.dlui.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.utils.webdav.WebDavClient;

public abstract class AbstractDLUITest extends AbstractProjectTest {



    private static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";

    private static final String ANNIE_DIALOG_HEADLINE = "Add data";

    private static final String ANNIE_DIALOG_EMPTY_STATE_MESSAGE =
            "Your project already contains all existing data. If you"
                    + " need to add more data, contact a project admin or GoodData Customer Support.";

    private static final String ANNIE_DIALOG_EMPTY_STATE_HEADING = "No additional data available.";

    private static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS =
            "DLUI/graph/CreateAndCopyDataToADS.grf";
    private static final int statusPollingCheckIterations = 60;

    private static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String PROCESS_EXECUTION_URI = DATALOAD_PROCESS_URI + "%s/executions";
    private static final String ADS_INSTANCES_URI = "gdc/datawarehouse/instances/";
    private static final String ADS_INSTANCE_SCHEMA_URI = "/" + ADS_INSTANCES_URI
            + "%s/schemas/default";
    private static final String OUTPUTSTAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";

    private String DATAlOAD_PROCESS_NAME = "Dataload Process";

    private JSONObject cloudConnectProcess = new JSONObject();
    private JSONObject processExecution = new JSONObject();

    @FindBy(css = ".s-btn-add_data")
    private WebElement addDataButton;

    @FindBy(tagName = "form")
    private InstanceFragment storageForm;

    @FindBy(css = ".annie-dialog-main")
    protected AnnieUIDialogFragment annieUIDialog;

    private ProjectInfo workingProject;

    protected String maqlFilePath;
    protected String sqlFilePath;
    protected String dluiZipFilePath;

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject =
                    new ProjectInfo().setProjectName(projectTitle).setProjectId(
                            testParams.getProjectId());
        return workingProject;
    }

    protected int createDataLoadProcess() {
        String processUri = String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
        objMap.put("type", "DATALOAD");
        objMap.put("name", DATAlOAD_PROCESS_NAME);
        JSONObject dataloadProcessObj = new JSONObject();
        try {
            dataloadProcessObj.put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when creating dataload process! ", e);
        }
        HttpRequestBase postRequest =
                getRestApiClient().newPostMethod(processUri, dataloadProcessObj.toString());
        HttpResponse postResponse = getRestApiClient().execute(postRequest);

        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected void createModelForGDProject(String maqlFile) {
        String maql = "";
        try {
            maql = FileUtils.readFileToString(new File(maqlFile));
            postMAQL(maql, statusPollingCheckIterations);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an exeception during reading file to string!", e);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exeception during creating model for GD project! ", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "There is an exeception during creating model for GD project! ", e);
        }
    }

    protected void createADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI);
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        String adsUrl = "";
        try {
            adsUrl =
                    storageForm
                            .createStorage(adsInstance.getAdsName(),
                                    adsInstance.getAdsDescription(),
                                    adsInstance.getAdsAuthorizationToken());
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exeception during creating new ads instance! ", e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "There is an exeception during creating new ads instance! ", e);
        }
        adsInstance.setAdsId(adsUrl.substring(adsUrl.lastIndexOf("/") + 1));
        System.out.println("adsId: " + adsInstance.getAdsId());
    }
    
    protected void deleteADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getAdsId());
        waitForElementVisible(BY_GP_FORM_SECOND, browser);
        InstanceFragment storage = createPageFragment(InstanceFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorageSuccess();
    }

    protected int createCloudConnectProcess(ProcessInfo processInfo) {
        String uploadFilePath =
                uploadZipFileToWebDav(dluiZipFilePath + CLOUDCONNECT_PROCESS_PACKAGE, null);

        String processesUri =
                String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        try {
            LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
            objMap.put("type", "GRAPH");
            objMap.put("name", processInfo.getProcessName());
            objMap.put("path", uploadFilePath.substring(uploadFilePath.indexOf("/uploads")) + "/"
                    + CLOUDCONNECT_PROCESS_PACKAGE);
            cloudConnectProcess.put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem when create JSON object for creating CloudConnect process! ",
                    e);
        }
        String postBody = cloudConnectProcess.toString();
        System.out.println("postBody: " + postBody);
        HttpRequestBase postRequest = getRestApiClient().newPostMethod(processesUri, postBody);
        HttpResponse postResponse = getRestApiClient().execute(postRequest);
        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        System.out.println(postResponse.getFirstHeader("Location"));
        String processUri = postResponse.getFirstHeader("Location").getValue();
        processInfo.setProcessId(processUri.substring(processUri.lastIndexOf("/") + 1));
        System.out.println("Process id: " + processInfo.getProcessId());

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected int executeProcess(String processId, String adsUrl, String createTableSqlFile,
            String copyTableSqlFile) {
        String processExecutionUri =
                String.format(PROCESS_EXECUTION_URI, testParams.getProjectId(), processId);
        String createTableSql = "";
        String copyTableSql = "";
        try {
            createTableSql = FileUtils.readFileToString(new File(createTableSqlFile), "utf-8");
            copyTableSql = FileUtils.readFileToString(new File(copyTableSqlFile), "utf-8");
            LinkedHashMap<String, Object> objMap = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
            paramMap.put(DLUIProcessParameters.ADSUSER.getJsonObjectKey(), testParams.getUser());
            paramMap.put(DLUIProcessParameters.ADSPASSWORD.getJsonObjectKey(),
                    testParams.getPassword());
            paramMap.put(DLUIProcessParameters.ADSURL.getJsonObjectKey(), adsUrl);
            paramMap.put(DLUIProcessParameters.CREATE_TABLE_SQL.getJsonObjectKey(), createTableSql);
            paramMap.put(DLUIProcessParameters.COPY_TABLE_SQL.getJsonObjectKey(), copyTableSql);
            objMap.put(DLUIProcessParameters.EXECUTABLE.getJsonObjectKey(),
                    DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS);
            objMap.put("params", paramMap);
            processExecution.put("execution", objMap);
            String postBody = processExecution.toString();
            System.out.println("postBody: " + postBody);
            HttpRequestBase postRequest =
                    getRestApiClient().newPostMethod(processExecutionUri, postBody);
            HttpResponse postResponse = getRestApiClient().execute(postRequest);
            int responseStatusCode = postResponse.getStatusLine().getStatusCode();

            System.out.println(postResponse.toString());
            EntityUtils.consumeQuietly(postResponse.getEntity());
            System.out.println("Response status: " + responseStatusCode);

            return responseStatusCode;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an exeception during reading file to string! ", e);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when executing an process! ", e);
        }
    }

    protected int setDefaultSchemaForOutputStage(String projectId, String adsId) {
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
        HttpRequestBase putRequest = getRestApiClient().newPutMethod(putUri, putBody);
        HttpResponse putResponse = getRestApiClient().execute(putRequest);
        int responseStatusCode = putResponse.getStatusLine().getStatusCode();

        System.out.println(putResponse.toString());
        EntityUtils.consumeQuietly(putResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected String uploadZipFileToWebDav(String zipFile, String webContainer) {
        WebDavClient webDav =
                WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = new File(zipFile);
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                    " Create WebDav storage structure");
        } else
            webDav.setWebDavStructure(webContainer);

        webDav.uploadFile(resourceFile);

        return webDav.getWebDavStructure();
    }

    protected void openAnnieDialog() {
        initManagePage();
        waitForElementVisible(addDataButton).click();
        browser.switchTo().frame(
                waitForElementVisible(By.xpath("//iframe[contains(@src,'dlui-annie')]"), browser));
        waitForElementVisible(annieUIDialog.getRoot());
    }

    protected void checkEmptyAnnieDialog() {
        assertEquals(annieUIDialog.getAnnieDialogHeadline(), ANNIE_DIALOG_HEADLINE);
        assertEquals(annieUIDialog.getEmptyStateHeading(), ANNIE_DIALOG_EMPTY_STATE_HEADING);
        assertEquals(annieUIDialog.getEmptyStateMessage(), ANNIE_DIALOG_EMPTY_STATE_MESSAGE);
    }
}
