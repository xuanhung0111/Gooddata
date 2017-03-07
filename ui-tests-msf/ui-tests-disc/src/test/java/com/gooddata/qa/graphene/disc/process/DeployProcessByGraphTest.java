package com.gooddata.qa.graphene.disc.process;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.disc.common.AbstractDiscTest;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;

public class DeployProcessByGraphTest extends AbstractDiscTest {

    private static final String ZIP_FILE_INPUT_ERROR_MESSAGE = "Select a ZIP file that is smaller than 5 MB.";
    private static final String FAILED_DEPLOY_MESSAGE = "Failed to (re-)?deploy .*\\. Reasons?: Process contains no executables.";

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithSamePackage() {
        String processName = generateProcessName();
        initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());

        takeScreenshot(browser, "Process-deployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(processName), "Process is not deployed");

        String newProcessName = generateProcessName();
        projectDetailPage.getProcess(processName)
                .redeployWithZipFile(newProcessName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());

        takeScreenshot(browser, "Process-redeployed-successfully", getClass());
        assertTrue(projectDetailPage.hasProcess(newProcessName), "Process is not redeployed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void emptyInputErrorDeployment() {
        DeployProcessForm deployForm = initDiscProjectDetailPage().clickDeployButton();

        deployForm.submit();
        takeScreenshot(browser, "Package-input-shows-error", getClass());
        assertTrue(deployForm.isPackageInputError(), "Package input not show error");
        assertEquals(getBubbleMessage(browser), ZIP_FILE_INPUT_ERROR_MESSAGE);

        deployForm.inputPackageFile(PackageFile.BASIC.loadFile()).submit();
        takeScreenshot(browser, "Process-name-input-shows-error", getClass());
        assertTrue(deployForm.isProcessNameInputError(), "Process name input not show error");
        assertEquals(getBubbleMessage(browser), "Process Name cannot be empty.");
    }

    @DataProvider(name = "invalidFileProvider")
    public Object[][] getInvalidFileProvider() throws IOException {
        return new Object[][] {
            {createFile("non-Zip-File.7z")},
            {createLargeZipFile()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidFileProvider")
    public void deployInvalidPackageFile(File packageFile) throws IOException {
        DeployProcessForm deployForm = initDiscProjectDetailPage()
                .clickDeployButton()
                .inputPackageFile(packageFile);

        takeScreenshot(browser, "Error-shows-with-" + packageFile.getName(), getClass());
        assertTrue(deployForm.isPackageInputError(), "Package input not show error");
        assertEquals(getBubbleMessage(browser), ZIP_FILE_INPUT_ERROR_MESSAGE);
    }

    @DataProvider(name = "fileWithoutExecutableProvider")
    public Object[][] getFileWithoutExecutableProvider() throws IOException {
        return new Object[][] {
            {createFile("file-Without-Executable.zip")},
            {PackageFile.RUBY.loadFile()}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "fileWithoutExecutableProvider")
    public void deployFileWithoutExecutableInProjectDetail(File fileWithoutExecutable) {
        initDiscProjectDetailPage()
                .deployProcessWithZipFile(generateProcessName(), ProcessType.CLOUD_CONNECT, fileWithoutExecutable);

        takeScreenshot(browser, "No-executable-error-dialog-shows-for-" + fileWithoutExecutable.getName(), getClass());
        assertTrue(ConfirmationDialog.getInstance(browser).getMessage().matches(FAILED_DEPLOY_MESSAGE),
                "Error message not show correctly");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "fileWithoutExecutableProvider")
    public void deployFileWithoutExecutableInProjectsPage(File fileWithoutExecutable) {
        initDiscProjectsPage().markProjectCheckbox(projectTitle)
                .deployProcessWithZipFile(generateProcessName(), ProcessType.CLOUD_CONNECT, fileWithoutExecutable);

        takeScreenshot(browser, "No-executable-error-bar-shows-for-" + fileWithoutExecutable.getName(), getClass());
        assertTrue(projectsPage.getErrorBarMessage().matches(FAILED_DEPLOY_MESSAGE),
                "Error message not show correctly");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "fileWithoutExecutableProvider")
    public void redeployFileWithoutExecutable(File fileWithoutExecutable) {
        String processName = generateProcessName();

        initDiscProjectDetailPage()
                .deployProcessWithZipFile(processName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());

        projectDetailPage.getProcess(processName)
                .redeployWithZipFile(processName, ProcessType.CLOUD_CONNECT, fileWithoutExecutable);
        assertTrue(ConfirmationDialog.getInstance(browser).getMessage().matches(FAILED_DEPLOY_MESSAGE),
                "Error message not show correctly");
    }

    private File createFile(String fileName) throws IOException {
        File file = new File(getTargetFolder() + "/" + fileName);
        file.createNewFile();
        return file;
    }

    private File createLargeZipFile() throws IOException {
        String path = getTargetFolder() + "/largeFile.zip";

        try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
            file.setLength(6000000);
        }
        return new File(path);
    }

    private String getTargetFolder() {
        return System.getProperty("user.dir") + "/target";
    }
}
