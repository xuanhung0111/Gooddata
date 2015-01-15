package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.service.model.dataload.process.Processes;
import com.gooddata.service.model.dataload.process.Process;
import com.google.common.base.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.sisu.bean.IgnoreSetters;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataloadSchedulesTests extends AbstractSchedulesTests {

    private final String PROCESS_NAME = "Dataload process";
    private final ObjectMapper mapper = new ObjectMapper();

    private final int STATUS_POLLING_CHECK_ITERATIONS = 60;

    @BeforeClass
    public void setUp() {
        projectTitle = "Disc-test-dataload-schedule";
        createDataloadProcessIfDoesntExist();
        createDatasets();
    }

    @AfterClass
    public void tearDown() {
        deleteDataloadProcess();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule", "tests"})
    public void createDataloadScheduleWithAllDatasets() throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setSynchronizeAllDatasets(true)
                .setScheduleName("All datasets");
        createAndAssertSchedule(scheduleBuilder);
    }

    @Test(dependsOnMethods = {"createProject", "createDataloadScheduleWithAllDatasets"}, groups = {"schedule", "tests"})
    public void createDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("1 dataset")
                .setDataloadDatasetsOverlap(true);
        createAndAssertSchedule(scheduleBuilder);
    }

    @Test(dependsOnMethods = {"createDataloadScheduleWithCustomDatasets"}, groups = {"schedule", "tests"})
    public void editDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {

        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("1 dataset (1)")
                .setDataloadDatasetsOverlap(true);
        createAndAssertSchedule(scheduleBuilder);

        scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        scheduleDetail.changeAndCheckDatasetDialog(scheduleBuilder);
    }

    private void createDatasets() {
        HttpRequestBase getRequest = getRestApiClient().newGetMethod(String.format("/gdc/md/%s/ldm/singleloadinterface/dataset.salesforce", getWorkingProject().getProjectId()));
        HttpResponse getResponse = getRestApiClient().execute(getRequest);

        // dataset already exists
        if (getResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            EntityUtils.consumeQuietly(getResponse.getEntity());
            return;
        }

        try {
            String maql = IOUtils.toString(getClass().getResource("/create-datasets.txt"));
            postMAQL(maql, STATUS_POLLING_CHECK_ITERATIONS);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create datasets", e);
        }
    }


    private void createDataloadProcessIfDoesntExist() {
        if (createDataloadProcess() == HttpStatus.CONFLICT.value()) {
            deleteDataloadProcess();
        }

        createDataloadProcess();
    }

    private int createDataloadProcess() {
        System.out.println("Creating dataload process for project: " + getWorkingProject().getProjectId());

        HttpRequestBase postRequest = getRestApiClient().newPostMethod(getProcessesUri(), "{\"process\": {\"type\":\"DATALOAD\",\"name\":\"" + PROCESS_NAME + "\"}}");
        HttpResponse postResponse = getRestApiClient().execute(postRequest);

        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println(" - status: " + responseStatusCode);

        return responseStatusCode;
    }

    private String getDataloadProcessUri() {
        String processesUri = getProcessesUri();

        System.out.println("Getting dataload process for project: " + getWorkingProject().getProjectId());

        HttpRequestBase postRequest = getRestApiClient().newGetMethod(processesUri);
        HttpResponse postResponse = getRestApiClient().execute(postRequest);

        int responseStatusCode = postResponse.getStatusLine().getStatusCode();
        System.out.println(" - status: " + responseStatusCode);

        exitOnError(postResponse, responseStatusCode);

        try {

            Processes processes = mapper.readValue(EntityUtils.toString(postResponse.getEntity()), Processes.class);
            for (Process process : processes.getItems()) {
                if ("DATALOAD".equals(process.getType())) {
                    return process.getLinks().getSelf();
                }
            }

            return null;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to parse processes from response.");
        }
    }

    private void exitOnError(HttpResponse postResponse, int responseStatusCode) {
        if (responseStatusCode != HttpStatus.OK.value()) {
            String error;
            try {
                error = EntityUtils.toString(postResponse.getEntity());
            } catch (IOException e) {
                error = "unknown error :/";
            }
            throw new IllegalStateException(String.format("Unknown response from backend when fetching dataload process for project %s (%s)", getWorkingProject().getProjectId(), error));
        }
    }

    private void deleteDataloadProcess() {
        String dataloadProcessUri = getDataloadProcessUri();
        if (StringUtils.isEmpty(dataloadProcessUri)) {
            return;
        }

        System.out.println("Deleting dataload process for project: " + getWorkingProject().getProjectId());

        HttpRequestBase deleteRequest = getRestApiClient().newDeleteMethod(dataloadProcessUri);
        HttpResponse deleteResponse = getRestApiClient().execute(deleteRequest);

        int responseStatusCode = deleteResponse.getStatusLine().getStatusCode();

        EntityUtils.consumeQuietly(deleteResponse.getEntity());
        System.out.println(" - status: " + responseStatusCode);
    }

    private String getProcessesUri() {
        return String.format("/gdc/projects/%s/dataload/processes", getWorkingProject().getProjectId());
    }

}