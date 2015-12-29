package com.gooddata.qa.graphene.utils;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.dto.Processes;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;

public class ProcessUtils {

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    protected static final String PROCESS_EXECUTIONS_URI = DATALOAD_PROCESS_URI + "%s/executions";
    private static final String ERROR_KEY = "error";

    private ProcessUtils() {}

    public static int createCloudConnectProcess(RestApiClient restApiClient,
            ProcessInfo processInfo, String uploadFile) {
        String postBody =
                prepareProcessCreationBody("GRAPH", processInfo.getProcessName(), uploadFile)
                        .toString();
        return createProcess(restApiClient, processInfo, postBody);
    }

    public static int createDataloadProcess(RestApiClient restApiClient, ProcessInfo processInfo) {
        String postBody =
                prepareProcessCreationBody("DATALOAD", processInfo.getProcessName()).toString();

        return createProcess(restApiClient, processInfo, postBody);

    }

    public static Processes getProcessesList(RestApiClient restApiClient, String projectId)
            throws IOException, JSONException {
        String processesUri = format("/gdc/projects/%s/dataload/processes", projectId);
        ObjectMapper mapper = new ObjectMapper();

        JSONObject json =
                new JSONObject(RestUtils.getResource(restApiClient, processesUri,
                        HttpStatus.OK));
        try {
            return mapper.readValue(json.toString(), Processes.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse processes from response.");
        }
    }

    public static void deleteDataloadProcess(RestApiClient restApiClient,
            String dataloadProcessUri, String projectId) {
        if (StringUtils.isEmpty(dataloadProcessUri)) {
            return;
        }

        System.out.println("Deleting dataload process for project: " + projectId);
        HttpRequestBase deleteRequest = restApiClient.newDeleteMethod(dataloadProcessUri);
        try {
            HttpResponse deleteResponse =
                    restApiClient.execute(deleteRequest, HttpStatus.NO_CONTENT,
                            "Could not delete dataload process!");
            EntityUtils.consumeQuietly(deleteResponse.getEntity());
        } finally {
            deleteRequest.releaseConnection();
        }
    }

    public static String failedToCreateDataloadProcessExecution(RestApiClient restApiClient,
            HttpStatus expectedStatusCode, ProcessInfo processInfo, Collection<ExecutionParameter> params) {
        String errorMessage =
                createProcessExecution(expectedStatusCode, restApiClient, processInfo, "", params);

        System.out.println("Failed to create process execution with error: " + errorMessage);

        return errorMessage;
    }

    public static String executeProcess(RestApiClient restApiClient, ProcessInfo processInfo,
            String executable, Collection<ExecutionParameter> params) {
        String pollingUri =
                createProcessExecution(HttpStatus.CREATED, restApiClient, processInfo, executable,
                        params);

        System.out.println("Execution polling uri: " + pollingUri);

        return pollingUri;
    }

    public static int getExecutionStatusCode(RestApiClient restApiClient, String executionLink) {
        HttpRequestBase getRequest = restApiClient.newGetMethod(executionLink);
        HttpResponse getResponse;
        getResponse = restApiClient.execute(getRequest);

        int responseStatusCode = getResponse.getStatusLine().getStatusCode();
        System.out.println("Reponse status: " + responseStatusCode);

        EntityUtils.consumeQuietly(getResponse.getEntity());

        return responseStatusCode;
    }

    public static String getExecutionStatus(RestApiClient restApiClient, String executionUri) {
        String status = "";
        HttpRequestBase getRequest = restApiClient.newGetMethod(executionUri + "/detail");
        HttpResponse getResponse;
        getResponse = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code!");

        int responseStatusCode = getResponse.getStatusLine().getStatusCode();
        assertEquals(responseStatusCode, HttpStatus.OK.value());
        try {
            status =
                    new JSONObject(EntityUtils.toString(getResponse.getEntity())).getJSONObject(
                            "executionDetail").getString("status");
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when parsing response body!", e);
        } finally {
            EntityUtils.consumeQuietly(getResponse.getEntity());
        }

        return status;
    }

    public static int waitForRunningExecutionByStatusCode(RestApiClient restApiClient,
            String executionLink) {
        return RestUtils.waitingForAsyncTask(restApiClient, executionLink);
    }

    public static String waitForRunningExecutionByStatus(RestApiClient restApiClient,
            String executionUri) {
        String status = getExecutionStatus(restApiClient, executionUri);
        while ("QUEUED".equals(status) || "RUNNING".equals(status)) {
            System.out.println("Current execution status is: " + status);
            sleepTight(2000);
            status = getExecutionStatus(restApiClient, executionUri);
        }

        return status;
    }

    public static boolean isExecutionSuccessful(RestApiClient restApiClient, String executionUri) {
        ProcessUtils.waitForRunningExecutionByStatusCode(restApiClient, executionUri);

        return "OK".equals(getExecutionStatus(restApiClient, executionUri));
    }

    public static JSONObject prepareProcessCreationBody(String processType, String processName) {
        return prepareProcessCreationBody(processType, processName, "");
    }

    public static JSONObject prepareProcessCreationBody(String processType, String processName,
            String uploadFile) {
        try {
            Map<String, String> objMap = new HashMap<String, String>();
            objMap.put("type", processType);
            objMap.put("name", processName);
            if (!uploadFile.isEmpty())
                objMap.put("path", uploadFile.substring(uploadFile.indexOf("/uploads")));
            return new JSONObject().put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem when create JSON object for creating CloudConnect process! ",
                    e);
        }
    }

    private static int createProcess(RestApiClient restApiClient, ProcessInfo processInfo,
            String requestBody) {
        String processesUri = String.format(DATALOAD_PROCESS_URI, processInfo.getProjectId());

        HttpRequestBase postRequest = restApiClient.newPostMethod(processesUri, requestBody);
        try {
            HttpResponse postResponse = restApiClient.execute(postRequest);
            int responseStatusCode = postResponse.getStatusLine().getStatusCode();
            System.out.println("Response status: " + responseStatusCode);

            if (postResponse.getFirstHeader("Location") != null) {
                String processUri = postResponse.getFirstHeader("Location").getValue();
                processInfo.withProcessId(processUri.substring(processUri.lastIndexOf("/") + 1));
                System.out.println("Process is created with id: " + processInfo.getProcessId());
            }
            EntityUtils.consumeQuietly(postResponse.getEntity());

            return responseStatusCode;
        } finally {
            postRequest.releaseConnection();
        }
    }

    private static String createProcessExecution(HttpStatus expectedStatusCode,
            RestApiClient restApiClient, ProcessInfo processInfo, String executable,
            Collection<ExecutionParameter> params) {
        String postBody = prepareProcessExecutionBody(executable, params);
        String processExecutionUri =
                String.format(PROCESS_EXECUTIONS_URI, processInfo.getProjectId(),
                        processInfo.getProcessId());

        HttpRequestBase postRequest = restApiClient.newPostMethod(processExecutionUri, postBody);
        try {
            HttpResponse postResponse =
                    restApiClient.execute(postRequest, expectedStatusCode,
                            "Execution is not created!");
            int responseStatusCode = postResponse.getStatusLine().getStatusCode();
            System.out.println("Response status: " + responseStatusCode);

            JSONObject responseObject =
                    new JSONObject(EntityUtils.toString(postResponse.getEntity()));

            EntityUtils.consumeQuietly(postResponse.getEntity());

            if (expectedStatusCode != HttpStatus.CREATED)
                return responseObject.getJSONObject(ERROR_KEY).getString("message");

            return responseObject.getJSONObject("executionTask").getJSONObject("links")
                    .getString("poll");
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when executing a process!", e);
        } finally {
            postRequest.releaseConnection();
        }
    }

    private static String prepareProcessExecutionBody(String executable,
            Collection<ExecutionParameter> params) {
        JSONObject processExecution = new JSONObject();
        Map<String, Object> objMap = new HashMap<String, Object>();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (ExecutionParameter param : params) {
            paramMap.put(param.getName(), param.getValue());
        }
        objMap.put("executable", executable);
        objMap.put("params", paramMap);
        try {
            processExecution.put("execution", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when executing an process! ", e);
        }

        return processExecution.toString();
    }
}
