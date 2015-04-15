package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.DLUIProcessParameters;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.utils.webdav.WebDavClient;

public abstract class AbstractDLUITest extends AbstractProjectTest {

    private static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";
    private static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS = "DLUI/graph/CreateAndCopyDataToADS.grf";

    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;

    private static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String PROCESS_EXECUTION_URI = DATALOAD_PROCESS_URI + "%s/executions";
    private static final String ADS_INSTANCES_URI = "gdc/datawarehouse/instances/";
    private static final String ADS_INSTANCE_SCHEMA_URI = "/" + ADS_INSTANCES_URI + "%s/schemas/default";
    private static final String OUTPUTSTAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";

    private static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";

    private JSONObject cloudConnectProcess = new JSONObject();
    private JSONObject processExecution = new JSONObject();

    @FindBy(css = ".s-btn-add_data")
    private WebElement addDataButton;

    @FindBy(css = ".annie-dialog-main")
    protected AnnieUIDialogFragment annieUIDialog;

    private ProjectInfo workingProject;

    protected String maqlFilePath;
    protected String sqlFilePath;
    protected String zipFilePath;

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject = new ProjectInfo(projectTitle, testParams.getProjectId());
        return workingProject;
    }

    protected int createDataLoadProcess() {
        String processUri = String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
        objMap.put("type", "DATALOAD");
        objMap.put("name", DEFAULT_DATAlOAD_PROCESS_NAME);
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

    protected boolean dataloadProcessIsCreated() {
        return createDataLoadProcess() == HttpStatus.CONFLICT.value();
    }

    protected void createModelForGDProject(String maqlFile) {
        try {
            String maql = FileUtils.readFileToString(new File(maqlFile));
            postMAQL(maql, STATUS_POLLING_CHECK_ITERATIONS);
        } catch (IOException e) {
            throw new IllegalStateException("There is an exception during reading file to string!", e);
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception during creating model for GD project! ", e);
        }
    }

    protected void createADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI);
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        String adsUrl;
        try {
            adsUrl = storageForm.createStorage(adsInstance.getName(), adsInstance.getDescription(),
                    adsInstance.getAuthorizationToken());
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception during creating new ads instance! ", e);
        }

        adsInstance.withId(adsUrl.substring(adsUrl.lastIndexOf("/") + 1));
        System.out.println("adsId: " + adsInstance.getId());
    }

    protected void deleteADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getId());
        InstanceFragment storage = createPageFragment(InstanceFragment.class,
                waitForElementVisible(BY_GP_FORM_SECOND, browser));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorageSuccess();
    }

    protected int createCloudConnectProcess(ProcessInfo processInfo) {
        String uploadFilePath =
                uploadZipFileToWebDav(zipFilePath + CLOUDCONNECT_PROCESS_PACKAGE, null);
        String processesUri =
                String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        prepareCCProcessCreationBody(processInfo, uploadFilePath);
        String postBody = cloudConnectProcess.toString();
        System.out.println("postBody: " + postBody);

        HttpRequestBase postRequest = getRestApiClient().newPostMethod(processesUri, postBody);
        HttpResponse postResponse = getRestApiClient().execute(postRequest);
        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        System.out.println(postResponse.getFirstHeader("Location"));
        String processUri = postResponse.getFirstHeader("Location").getValue();
        processInfo.withProcessId(processUri.substring(processUri.lastIndexOf("/") + 1));
        System.out.println("Process id: " + processInfo.getProcessId());

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected int executeProcess(String processId, String adsUrl, String createTableSqlFile,
            String copyTableSqlFile) {
        String processExecutionUri =
                String.format(PROCESS_EXECUTION_URI, testParams.getProjectId(), processId);
        try {
            String createTableSql = FileUtils.readFileToString(new File(createTableSqlFile), StandardCharsets.UTF_8);
            String copyTableSql = FileUtils.readFileToString(new File(copyTableSqlFile), StandardCharsets.UTF_8);
            prepareProcessExecutionBody(adsUrl, createTableSql, copyTableSql);
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
            throw new IllegalStateException("There is an exception during reading file to string! ", e);
        }
    }

    protected int setDefaultSchemaForOutputStage(String adsId) {
        String schemaUri = String.format(ADS_INSTANCE_SCHEMA_URI, adsId);
        JSONObject outputStageObj = new JSONObject();
        try {
            outputStageObj.put("outputStage", new JSONObject().put("schema", schemaUri));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when set default schema for outputStage! ", e);
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

    private void prepareProcessExecutionBody(String adsUrl, String createTableSql,
            String copyTableSql) {
        LinkedHashMap<String, Object> objMap = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
        paramMap.put(DLUIProcessParameters.ADSUSER.getJsonObjectKey(), testParams.getUser());
        paramMap.put(DLUIProcessParameters.ADSPASSWORD.getJsonObjectKey(), testParams.getPassword());
        paramMap.put(DLUIProcessParameters.ADSURL.getJsonObjectKey(), adsUrl);
        paramMap.put(DLUIProcessParameters.CREATE_TABLE_SQL.getJsonObjectKey(), createTableSql);
        paramMap.put(DLUIProcessParameters.COPY_TABLE_SQL.getJsonObjectKey(), copyTableSql);
        objMap.put(DLUIProcessParameters.EXECUTABLE.getJsonObjectKey(),
                DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS);
        objMap.put("params", paramMap);
        try {
            processExecution.put("execution", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException("There is a problem with JSON object when executing an process! ", e);
        }
    }

    private void prepareCCProcessCreationBody(ProcessInfo processInfo, String uploadFilePath) {
        try {
            LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
            objMap.put("type", "GRAPH");
            objMap.put("name", processInfo.getProcessName());
            objMap.put("path", uploadFilePath.substring(uploadFilePath.indexOf("/uploads")) + "/"
                    + CLOUDCONNECT_PROCESS_PACKAGE);
            cloudConnectProcess.put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem when create JSON object for creating CloudConnect process! ", e);
        }
    }
}
