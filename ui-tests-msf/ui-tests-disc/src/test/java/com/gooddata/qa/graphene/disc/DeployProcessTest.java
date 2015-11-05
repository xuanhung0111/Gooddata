package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;

public class DeployProcessTest extends AbstractDeployProcessTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-deploy-process";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployCloudConnectInProjectsPage() {
        try {
            deployInProjectsPage(getProjects(), DeployPackages.CLOUDCONNECT, "CloudConnect - Projects List Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployRubyInProjectsPage() {
        try {
            deployInProjectsPage(getProjects(), DeployPackages.RUBY, "Ruby - Projects List Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployCloudConnectInProjectDetailPage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, "CloudConnect - Project Detail Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployRubyInProjectDetailPage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.RUBY, "Ruby - Project Detail Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployProcessWithDifferentPackage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redeploy process with different package";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.CLOUDCONNECT, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployProcessWithDifferentProcessType() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redeploy process with different process type";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.EXECUTABLES_RUBY, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployProcessWithSamePackage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Deploy process";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            String newProcessName = "Redeploy process with the same package";
            redeployProcess(processName, DeployPackages.EXECUTABLES_GRAPH, newProcessName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void emptyInputErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());
        deployForm.tryToDeployProcess("", ProcessTypes.DEFAULT, "");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
        assertTrue(deployForm.inputProcessNameHasError(), "Error is not shown for process name input!");
        assertTrue(deployForm.isCorrectInvalidProcessNameError(), "Incorrect process name validation error!");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void notZipFileErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/not-zip-file.7z");
        deployForm.tryToDeployProcess(filePath, ProcessTypes.DEFAULT, "Not zip file");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void tooLargeZipFileErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/too-large-file.zip");
        deployForm.tryToDeployProcess(filePath, ProcessTypes.DEFAULT, "Too large file");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployWithoutExecutablesInProjectsPage() {
        failedDeployInProjectsPage(getProjects(), DeployPackages.NOT_EXECUTABLE, ProcessTypes.DEFAULT,
                "Not Executables");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployCloudConnectWithRubyTypeInProjectsPage() {
        failedDeployInProjectsPage(getProjects(), DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                "CloudConnect with Ruby type");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployRubyWithCloudConnectTypeInProjectsPage() {
        failedDeployInProjectsPage(getProjects(), DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Ruby with CloudConnect type");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployWithoutExecutablesInProjectDetailPage() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.NOT_EXECUTABLE, ProcessTypes.DEFAULT, "Not Executable");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployCloudConnectWithRubyTypeInProjectDetailPage() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                "Deploy CloudConnect package with ruby type");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deployRubyWithCloudConnectTypeInProjectDetailPage() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Deploy Ruby package with graph type");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployWithoutExecutables() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redeploy process without executables";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.NOT_EXECUTABLE, ProcessTypes.GRAPH, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployCloudConnectWithRubyType() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redeploy CloudConnect process with Ruby type";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redeployRubyWithCloudConnectType() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redploy Ruby process with Graph type";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);
            failedRedeployProcess(processName, DeployPackages.RUBY, ProcessTypes.GRAPH, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    /*
     * The tests for checking deploy dialog message are disabled until MSF-7601, MSF-6156 are
     * considered by MSF team
     */
    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkDeployDialogMessageInProjectDetail() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages.BASIC, ProcessTypes.GRAPH);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkFailedDeployMessageInProjectDetail() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            checkFailedDeployDialogMessageInProjectDetail(DeployPackages.BASIC, ProcessTypes.RUBY);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkDeployDialogMessageInProjectsPage() {
        try {
            checkSuccessfulDeployDialogMessageInProjectsPage(getProjects(), DeployPackages.BASIC,
                    ProcessTypes.GRAPH);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkFailedDeployMessageInProjectsPage() {
        try {
            checkFailedDeployDialogMessageInProjectsPage(getProjects(), DeployPackages.BASIC, ProcessTypes.RUBY);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

}
