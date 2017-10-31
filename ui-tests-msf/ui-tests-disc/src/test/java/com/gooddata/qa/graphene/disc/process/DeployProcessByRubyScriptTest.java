package com.gooddata.qa.graphene.disc.process;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.process.AbstractProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;

public class DeployProcessByRubyScriptTest extends AbstractProcessTest {

    private static final String FAILED_DEPLOY_MESSAGE = "Failed to (re-)?deploy .*\\. Reasons?: Process contains no executables.";

    @BeforeClass(alwaysRun = true)
    public void disableDynamicUser() {
        // deploy ruby script must be done by a special account
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectsPage() {
        String processName = generateProcessName();

        initDiscProjectsPage()
                .markProjectCheckbox(projectTitle)
                .deployProcessWithZipFile(processName, ProcessType.RUBY_SCRIPTS, PackageFile.RUBY.loadFile());
        assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployProcessInProjectDetailPage() {
        String processName = generateProcessName();

        initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.RUBY_SCRIPTS, PackageFile.RUBY.loadFile());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployGraphFileWithRubyScriptTypeInProjectsPage() {
        initDiscProjectsPage().markProjectCheckbox(projectTitle)
                .deployProcessWithZipFile(generateProcessName(), ProcessType.RUBY_SCRIPTS, PackageFile.BASIC.loadFile());
        assertTrue(projectsPage.getErrorBarMessage().matches(FAILED_DEPLOY_MESSAGE), "Error message not show correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployGraphFileWithRubyScriptTypeInProjectDetailPage() {
        initDiscProjectDetailPage()
                .deployProcessWithZipFile(generateProcessName(), ProcessType.RUBY_SCRIPTS, PackageFile.BASIC.loadFile());
        assertTrue(ConfirmationDialog.getInstance(browser).getMessage().matches(FAILED_DEPLOY_MESSAGE),
                "Error message not show correctly");
    }

}
