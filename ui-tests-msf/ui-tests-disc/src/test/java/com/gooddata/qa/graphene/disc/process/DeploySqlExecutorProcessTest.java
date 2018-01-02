package com.gooddata.qa.graphene.disc.process;

import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class DeploySqlExecutorProcessTest extends AbstractEtlProcessTest {

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessWithConfiguration() {
        String processName = generateProcessName();
        initDiscProjectDetailPage().deploySqlExecutorProcess(processName);

        assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
        validateProcessMetadata(processName);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithConfiguration() {
        String processName = generateProcessName();
        initDiscProjectDetailPage().deploySqlExecutorProcess(processName);

        takeScreenshot(browser, "Process-deployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
        validateProcessMetadata(processName);

        String newProcessName = generateProcessName();
        projectDetailPage.getProcess(processName).redeploySqlExecutorProcess(newProcessName);

        takeScreenshot(browser, "Process-redeployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(newProcessName), "Process is not redeployed");
        validateProcessMetadata(newProcessName);
    }

    private void validateProcessMetadata(String processName) {
        EtlProcess process = getEtlProcessByName(processName, ProcessType.SQL_EXECUTOR);

        assertTrue(process != null, "Process is not deployed");
        assertTrue(process.getComponentName().equals(ProcessType.SQL_EXECUTOR.getValue()),
                format("Failed to deploy process type %s", ProcessType.SQL_EXECUTOR.getValue()));
    }
}
