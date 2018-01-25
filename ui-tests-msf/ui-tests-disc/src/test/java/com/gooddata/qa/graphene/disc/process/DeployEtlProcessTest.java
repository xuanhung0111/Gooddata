package com.gooddata.qa.graphene.disc.process;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.fragments.disc.process.AbstractProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.utils.http.disc.EtlProcessRestUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
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
                {ProcessType.ADS_INTEGRATOR}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithoutS3RegionAndServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_SECRET_KEY,
                DEFAULT_S3_REGION,
                DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithS3Region(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_SECRET_KEY,
                generateHashString(),
                DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_SECRET_KEY,
                DEFAULT_S3_REGION,
                true);
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void deployProcessWithS3RegionAndServerSideEncryption(ProcessType processType) {
        deployEtlProcessFromDiscAndCheckResult(generateProcessName(),
                processType,
                DEFAULT_S3_CONFIGURATION_PATH,
                DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_SECRET_KEY,
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
                .enterS3Region("")
                .submit();

        assertTrue(deployForm.isS3ConfigurationPathError());
        assertTrue(deployForm.isS3AccessKeyError());
        assertTrue(deployForm.isS3SecretKeyError());
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "processTypeProvider")
    public void redeployProcessWithConfiguration(ProcessType processType) {
        String processName = generateProcessName();
        deployEtlProcessFromDiscWithDefaultConfig(processName, processType);

        takeScreenshot(browser, "Process-deployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
        validateProcessMetadataWithDefaultConfig(processName, processType);

        String newProcessName = generateProcessName();
        validateAndRedeployEtlProcess(processName, newProcessName);

        takeScreenshot(browser, "Process-redeployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(newProcessName), "Process is not redeployed");
        validateProcessMetadataWithDefaultConfig(newProcessName, processType);
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
            assertTrue(processDetail.getMetadata("Author").equals(testParams.getUser()));
            assertTrue(processDetail.getMetadata("Component").equals(processType.getTitle()));
            assertTrue(processDetail.getMetadata("Configuration Path").equals(s3ConfigurationPath));
            assertTrue(EtlProcessRestUtils.ETL_PROCESS_TYPE_LABEL.equals(processDetail.getMetadata("Type")));
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
        validateProcessMetadata(processName, processType, DEFAULT_S3_CONFIGURATION_PATH, DEFAULT_S3_ACCESS_KEY,
                DEFAULT_S3_REGION, DEFAULT_S3_SERVER_SIDE_ENCRYPTION);
    }

    private void validateProcessMetadata(String processName,
                                           ProcessType processType,
                                           String s3ConfigurationPath,
                                           String s3AccessKey,
                                           String s3Region,
                                           boolean serverSideEncryption) {
        EtlProcess process = getEtlProcessByName(processName, processType);

        assertTrue(process != null, "Process is not deployed");
        assertTrue(process.getComponentName().equals(processType.getValue()),
                format("Failed to deploy process type %s", processType.getValue()));
        assertTrue(s3ConfigurationPath.equals(process.getS3ConfigurationPath()),
                format("Failed to deploy process type %s, Missing configuration path", processType.getValue()));
        assertTrue(s3AccessKey.equals(process.getS3AccessKey()),
                format("Failed to deploy process type %s, Missing access key", processType.getValue()));
        assertTrue(s3Region.equals(process.getS3Region()),
                format("Failed to deploy process type %s, Missing region", processType.getValue()));
        if (serverSideEncryption) {
            assertTrue(process.isServerSideEncryption(),
                    format("Failed to deploy process type %s, Missing server side encryption", processType.getValue()));
        } else {
            assertTrue(!process.isServerSideEncryption(),
                    format("Failed to deploy process type %s, Missing server side encryption", processType.getValue()));
        }
    }

    private void validateAndRedeployEtlProcess(String oldProcessName, String newProcessName) {
        DeployProcessForm deployProcessForm = projectDetailPage.getProcess(oldProcessName).clickRedeployButton();

        // Verify deployed data
        assertTrue(deployProcessForm.getS3ConfigurationPath().equals(DEFAULT_S3_CONFIGURATION_PATH),
                format("Expected '%s' but get '%s'", DEFAULT_S3_CONFIGURATION_PATH, deployProcessForm.getS3ConfigurationPath()));
        assertTrue(deployProcessForm.getS3AccessKey().equals(DEFAULT_S3_ACCESS_KEY),
                format("Expected '%s' but get '%s'", DEFAULT_S3_ACCESS_KEY, deployProcessForm.getS3AccessKey()));
        assertTrue(deployProcessForm.getS3Region().equals(DEFAULT_S3_REGION),
                format("Expected '%s' but get '%s'", DEFAULT_S3_REGION, deployProcessForm.getS3Region()));

        // Redeploy with new process name
        deployProcessForm.enterEtlProcessNameAndDeploy(newProcessName);
    }
}
