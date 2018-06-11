package com.gooddata.qa.graphene.common;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.utils.http.disc.EtlProcessRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Optional;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

/**
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
public class AbstractEtlProcessTest extends AbstractProcessTest {

    public static final String DEFAULT_S3_CONFIGURATION_PATH = "s3://msf-dev-grest/Do_Not_Delete_Using_In_ETL_tests/graphene_tests/data_test/";
    public static final String DEFAULT_S3_ACCESS_KEY = "AKIAJDZHMIHRXSPO4BYA";
    public static final String DEFAULT_S3_SECRET_KEY = "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";
    public static final String DEFAULT_S3_REGION = "";
    public static final boolean DEFAULT_S3_SERVER_SIDE_ENCRYPTION = false;

    protected EtlProcessRestRequest etlProcessRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ETL_COMPONENT, true);
        etlProcessRequest = new EtlProcessRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
    }

    protected void deployEtlProcessFromDiscWithDefaultConfig(String processName, ProcessType processType) {
        initDiscProjectDetailPage().deployEtlProcess(processName,
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_SECRET_KEY,
                DEFAULT_S3_REGION,
                DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    protected void createEtlProcessWithDefaultConfig(String processName, DeployProcessForm.ProcessType processType) {
        etlProcessRequest.createEtlProcess(processName, processType, DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY, DEFAULT_S3_SECRET_KEY);
    }

    protected DataloadProcess getProcessByName(String processName) {
        Optional<DataloadProcess> process =  getProcessService().listProcesses(getProject()).stream()
                .filter(dataloadProcess -> processName.equals(dataloadProcess.getName()))
                .findFirst();
        return process.isPresent()? process.get() : null;
    }

    protected EtlProcess getEtlProcessByName(String processName, ProcessType processType) {
        DataloadProcess dataloadProcess = getProcessByName(processName);
        if (dataloadProcess != null) {
            final CommonRestRequest restRequest = new CommonRestRequest(
                    new RestClient(getProfile(ADMIN)), testParams.getProjectId());
            try {
                final JSONObject json = restRequest.getJsonObject(
                        RestRequest.initGetRequest(dataloadProcess.getUri())).getJSONObject("process");
                JSONObject componentObj = json.getJSONObject("component");
                String s3ConfigurationPath = "";
                String s3AccessKey = "";
                String s3Region = "";
                boolean serverSideEncryption = false;
                if (processType != ProcessType.SQL_EXECUTOR) {
                    JSONObject s3Object = componentObj.getJSONObject("configLocation").getJSONObject("s3");
                    s3ConfigurationPath = s3Object.getString("path");
                    s3AccessKey = s3Object.getString("accessKey");
                    if (!s3Object.isNull("region")) {
                        s3Region = s3Object.getString("region");
                    }
                    if (!s3Object.isNull("serverSideEncryption")) {
                        serverSideEncryption = s3Object.getBoolean("serverSideEncryption");
                    }
                }
                return new EtlProcess(dataloadProcess,
                        componentObj.getString("name"),
                        componentObj.getString("version"),
                        s3ConfigurationPath,
                        s3AccessKey,
                        s3Region,
                        serverSideEncryption);
            } catch (JSONException | IOException e) {
                throw new IllegalStateException("Error during get ETL process metadata", e);
            }
        }
        return null;
    }

    protected class EtlProcess {
        private DataloadProcess dataloadProcess;
        private String componentName;
        private String componentVersion;
        private String s3ConfigurationPath;
        private String s3AccessKey;
        private String s3Region;
        private boolean serverSideEncryption;

        public EtlProcess(DataloadProcess dataloadProcess,
                          String componentName,
                          String componentVersion,
                          String s3ConfigurationPath,
                          String s3AccessKey,
                          String s3Region,
                          boolean serverSideEncryption) {
            this.dataloadProcess = dataloadProcess;
            this.componentName = componentName;
            this.componentVersion = componentVersion;
            this.s3ConfigurationPath = s3ConfigurationPath;
            this.s3AccessKey = s3AccessKey;
            this.s3Region = s3Region;
            this.serverSideEncryption = serverSideEncryption;
        }

        public DataloadProcess getDataloadProcess() {
            return dataloadProcess;
        }

        public String getComponentName() {
            return componentName;
        }

        public String getComponentVersion() {
            return componentVersion;
        }

        public String getS3ConfigurationPath() {
            return s3ConfigurationPath;
        }

        public String getS3AccessKey() {
            return s3AccessKey;
        }

        public String getS3Region() {
            return s3Region;
        }

        public boolean isServerSideEncryption() {
            return serverSideEncryption;
        }

    }
}