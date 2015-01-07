package com.gooddata.qa.graphene.disc;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;

public class DeployProcesses extends AbstractDeployProcesses {


    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-deploy-process";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployCloudConnectInProjectsPage() throws JSONException, InterruptedException {
        try {
            deployInProjectsPage(getProjects(), DeployPackages.CLOUDCONNECT,
                    "CloudConnect - Projects List Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployRubyInProjectsPage() throws JSONException, InterruptedException {
        try {
            deployInProjectsPage(getProjects(), DeployPackages.RUBY, "Ruby - Projects List Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployCloudConnectInProjectDetailPage() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT,
                    "CloudConnect - Project Detail Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployRubyInProjectDetailPage() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.RUBY, "Ruby - Project Detail Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployProcessWithDifferentPackage() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Redeploy process with different package";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.CLOUDCONNECT, processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployProcessWithDifferentProcessType() throws JSONException,
            InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Redeploy process with different process type";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.EXECUTABLES_RUBY, processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployProcessWithSamePackage() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Deploy process";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            String newProcessName = "Redeploy process with the same package";
            redeployProcess(processName, DeployPackages.EXECUTABLES_GRAPH, newProcessName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void emptyInputErrorDeployment() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());
        deployForm.assertErrorOnDeployForm("", ProcessTypes.DEFAULT, "");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void notZipFileErrorDeployment() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());
        deployForm.assertErrorOnDeployForm(zipFilePath + "not-zip-file.7z", ProcessTypes.DEFAULT,
                "Not zip file");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void tooLargeZipFileErrorDeployment() throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(getProjects());
        deployForm.assertErrorOnDeployForm(zipFilePath + "too-large-file.zip",
                ProcessTypes.DEFAULT, "Too large file");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployWithoutExecutablesInProjectsPage() throws JSONException, InterruptedException {
        failedDeployInProjectsPage(getProjects(), DeployPackages.NOT_EXECUTABLE,
                ProcessTypes.DEFAULT, "Not Executables");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployCloudConnectWithRubyTypeInProjectsPage() throws JSONException,
            InterruptedException {
        failedDeployInProjectsPage(getProjects(), DeployPackages.CLOUDCONNECT,
                ProcessTypes.RUBY, "CloudConnect with Ruby type");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployRubyWithCloudConnectTypeInProjectsPage() throws JSONException,
            InterruptedException {
        failedDeployInProjectsPage(getProjects(), DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Ruby with CloudConnect type");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployWithoutExecutablesInProjectDetailPage() throws JSONException,
            InterruptedException {
        openProjectDetailPage(getWorkingProject());
        failedDeployInProjectDetailPage(DeployPackages.NOT_EXECUTABLE, ProcessTypes.DEFAULT,
                "Not Executable");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployCloudConnectWithRubyTypeInProjectDetailPage() throws JSONException,
            InterruptedException {
        openProjectDetailPage(getWorkingProject());
        failedDeployInProjectDetailPage(DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                "Deploy CloudConnect package with ruby type");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void deployRubyWithCloudConnectTypeInProjectDetailPage() throws JSONException,
            InterruptedException {
        openProjectDetailPage(getWorkingProject());
        failedDeployInProjectDetailPage(DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Deploy Ruby package with graph type");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployWithoutExecutables() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Redeploy process without executables";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.NOT_EXECUTABLE, ProcessTypes.GRAPH,
                    processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployCloudConnectWithRubyType() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Redeploy CloudConnect process with Ruby type";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                    processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void redeployRubyWithCloudConnectType() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Redploy Ruby process with Graph type";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);
            failedRedeployProcess(processName, DeployPackages.RUBY, ProcessTypes.GRAPH, processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkDeployDialogMessageInProjectDetail() throws InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());
            checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages.BASIC,
                    ProcessTypes.GRAPH);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkDeployDialogMessageInProjectsPage() throws InterruptedException {
        try {
            checkSuccessfulDeployDialogMessageInProjectsPage(getProjects(),
                    DeployPackages.BASIC, ProcessTypes.GRAPH);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkFailedDeployMessageInProjectsPage() throws InterruptedException {
        checkFailedDeployDialogMessageInProjectsPage(getProjects(), DeployPackages.BASIC,
                ProcessTypes.RUBY);
    }

}
