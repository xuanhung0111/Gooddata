package com.gooddata.qa.graphene.disc.process;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.fragments.disc.process.AbstractProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.disc.EtlProcessRestRequest.ETL_PROCESS_TYPE_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class DeployEtlProcessTest extends AbstractEtlProcessTest {

    @DataProvider(name = "processTypeProvider")
    public Object[][] getProcessTypeProvider() {
        return new Object[][] {
                {ProcessType.CSV_DOWNLOADER},
                {ProcessType.SQL_DOWNLOADER},
                {ProcessType.GOOGLE_ANALYTICS_DOWNLOADER},
                {ProcessType.SALESFORCE_DOWNLOADER},
                {ProcessType.ADS_INTEGRATOR}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithoutS3RegionAndServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey,
                defaultS3SecretKey,
                DEFAULT_S3_REGION,
                DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithS3Region(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey,
                defaultS3SecretKey,
                generateHashString(),
                DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey,
                defaultS3SecretKey,
                DEFAULT_S3_REGION,
                true);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithS3RegionAndServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey,
                defaultS3SecretKey,
                generateHashString(),
                true);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithoutConfiguration(ProcessType processType) {
        String processName = generateProcessName();
        DeployProcessForm deployForm = initDiscProjectDetailPage().clickDeployButton();
        deployForm.selectProcessType(processType)
                .enterProcessName(processName)
                .enterS3ConfigurationPath("")
                .enterS3AccessKey("")
                .enterS3SecretKey("")
                .clickSubmitButton();

        assertTrue(deployForm.isS3ConfigurationPathError(), "S3 configuration path should show error");
        assertTrue(deployForm.isS3AccessKeyError(), "S3 access key should show error");
        assertTrue(deployForm.isS3SecretKeyError(), "S3 secret key should show error");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void redeployProcess(ProcessType processType) {
        // Deploy process
        String processName = generateProcessName();
        deployEtlProcessFromDiscWithDefaultConfig(processName, processType);

        // Validate deploy process result
        takeScreenshot(browser, "Process-deployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
        validateProcessMetadataWithDefaultConfig(processName, processType);

        // Redeploy process
        String newProcessName = generateProcessName();
        validateAndRedeployEtlProcess(processName, newProcessName);

        // Validate redeploy process result
        takeScreenshot(browser, "Process-redeployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(newProcessName), "Process is not redeployed");
        validateProcessMetadataWithDefaultConfig(newProcessName, processType);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void redeployProcessWithEnableAdditionalParams(ProcessType processType) {
        testRedeployProcessWithAdditionalParams(processType, true);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void redeployProcessWithDisableAdditionalParams(ProcessType processType) {
        testRedeployProcessWithAdditionalParams(processType, false);
    }

    private void testRedeployProcessWithAdditionalParams(ProcessType processType, boolean enableAdditionalParams) {
        // Deploy process
        String processName = generateProcessName();
        String region = "";
        String newRegion = "";
        boolean encryption = !enableAdditionalParams;
        if (enableAdditionalParams) {
            newRegion = generateHashString();
        } else {
            region = generateHashString();
        }
        deployEtlProcess(processName,
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey,
                defaultS3SecretKey,
                region,
                encryption);

        // Validate deploy process result
        takeScreenshot(browser, "Process-deployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
        validateProcessMetadata(processName, processType, DEFAULT_S3_CONFIGURATION_PATH, defaultS3AccessKey,
                region, encryption);

        // Redeploy process
        String newProcessName = generateProcessName();
        validateAndRedeployEtlProcessWithAdditionalParams(processName, newProcessName, DEFAULT_S3_CONFIGURATION_PATH,
                defaultS3AccessKey, region, newRegion, enableAdditionalParams);

        // Validate redeploy process result
        takeScreenshot(browser, "Process-redeployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(newProcessName), "Process is not redeployed");
        validateProcessMetadata(newProcessName, processType, DEFAULT_S3_CONFIGURATION_PATH, defaultS3AccessKey,
                newRegion, enableAdditionalParams);
    }

    private void deployEtlProcessFromDiscAndCheckResult(String processName,
                                                          ProcessType processType,
                                                          String s3ConfigurationPath,
                                                          String s3AccessKey,
                                                          String s3SecretKey,
                                                          String s3Region,
                                                          boolean serverSideEncryption) {
        try {
            initDiscProjectDetailPage().deployEtlProcess(processName,
                    processType,
                    s3ConfigurationPath,
                    s3AccessKey,
                    s3SecretKey,
                    s3Region,
                    serverSideEncryption);

            // Check process name
            assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
            // Check process metadata
            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(processName);
            processDetail.openTab(AbstractProcessDetail.Tab.METADATA);
            assertTrue(processDetail.isTabActive(AbstractProcessDetail.Tab.METADATA),
                    "Process metadata tab is not active");
            assertEquals(processDetail.getMetadata("Author"), testParams.getUser());
            assertEquals(processDetail.getMetadata("Component"), processType.getTitle());
            assertEquals(processDetail.getMetadata("Configuration Path"), s3ConfigurationPath);
            assertEquals(processDetail.getMetadata("Type"), ETL_PROCESS_TYPE_LABEL);
            validateProcessMetadata(processName, processType, s3ConfigurationPath, s3AccessKey, s3Region,
                    serverSideEncryption);
        } finally {
            DataloadProcess process = getProcessByName(processName);
            if (process != null) {
                getProcessService().removeProcess(process);
            }
        }
    }

    private void validateProcessMetadataWithDefaultConfig(String processName, ProcessType processType) {
        validateProcessMetadata(processName, processType, DEFAULT_S3_CONFIGURATION_PATH, defaultS3AccessKey,
                DEFAULT_S3_REGION, DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    private void validateProcessMetadata(String processName,
                                           ProcessType processType,
                                           String s3ConfigurationPath,
                                           String s3AccessKey,
                                           String s3Region,
                                           boolean serverSideEncryption) {
        EtlProcess process = getEtlProcessByName(processName, processType);

        assertNotNull(process);
        assertEquals(process.getComponentName(), processType.getValue());
        assertEquals(s3ConfigurationPath, process.getS3ConfigurationPath());
        assertEquals(s3AccessKey, process.getS3AccessKey());
        assertEquals(s3Region, process.getS3Region());
        if (serverSideEncryption) {
            assertTrue(process.isServerSideEncryption());
        } else {
            assertFalse(process.isServerSideEncryption());
        }
    }

    private void validateAndRedeployEtlProcess(String oldProcessName, String newProcessName) {
        DeployProcessForm deployProcessForm = projectDetailPage.getProcess(oldProcessName).clickRedeployButton();

        // Verify deployed data
        assertEquals(deployProcessForm.getS3ConfigurationPath(), DEFAULT_S3_CONFIGURATION_PATH);
        assertEquals(deployProcessForm.getS3AccessKey(), defaultS3AccessKey);
        assertEquals(deployProcessForm.getS3Region(), DEFAULT_S3_REGION);

        // Redeploy with new process name
        deployProcessForm.enterEtlProcessNameAndDeploy(newProcessName);
    }

    private void validateAndRedeployEtlProcessWithAdditionalParams(String currentProcessName, String newProcessName,
                                                                   String configurationPath, String accessKey,
                                                                   String region, String newRegion,
                                                                   boolean enableAdditionalParams) {
        DeployProcessForm deployProcessForm = projectDetailPage.getProcess(currentProcessName).clickRedeployButton();

        // Verify deployed data
        assertEquals(deployProcessForm.getS3ConfigurationPath(), configurationPath);
        assertEquals(deployProcessForm.getS3AccessKey(), accessKey);
        if (StringUtils.isNotEmpty(region)) {
            assertEquals(deployProcessForm.getS3Region(), region);
        }
        if (enableAdditionalParams) {
            assertFalse(deployProcessForm.isAdditionalParamsExpand());
        } else {
            assertTrue(deployProcessForm.isAdditionalParamsExpand());
            assertTrue(deployProcessForm.isEnableServerSideEncryption());
        }

        // Redeploy process
        if (enableAdditionalParams) {
            deployProcessForm.enterS3RegionAndEnableEncryptAndDeploy(newProcessName, newRegion);
        } else {
            deployProcessForm.removeS3RegionAndDisableEncryptAndDeploy(newProcessName);
        }
    }
}
