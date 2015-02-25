package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class DeployForm extends AbstractFragment {

    private static final By BY_FILE_INPUT_ERROR = By
            .cssSelector(".select-zip .zip-name-text input");
    private static final By BY_BUBBLE_ERROR = By
            .cssSelector("div.bubble-negative.isActive div.content");
    private static final String XPATH_PROCESS_TYPE_OPTION =
            "//select/option[@value='${processType}']";

    @FindBy(css = "div.deploy-process-dialog-area")
    private WebElement deployProcessDialog;

    @FindBy(css = "div.select-zip>div>input")
    private WebElement zipFileInput;

    @FindBy(css = "div.deploy-process-dialog-area>div.whole-line-text>input")
    private WebElement processNameInput;

    @FindBy(css = "div.deploy-process-button-area button.button-positive")
    private WebElement deployConfirmButton;

    public void checkDeployProgress(String zipFile, ProcessTypes processType, String processName,
            final String progressDialogMessage) {
        setDeployProcessInput(zipFile, processType, processName);
        assertFalse(inputFileHasError());
        assertFalse(inputProcessNameHasError());
        getDeployConfirmButton().click();
        try {
            assertEquals(getDeployProcessDialog().getText(), progressDialogMessage);
        } catch (NoSuchElementException e) {
            System.out.println("WARNING: Cannot get deploy progress message!");
        }
    }

    public void deployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        System.out.println("Deploy progress is finished!");
        waitForElementNotPresent(getRoot());
    }

    public void tryToDeployProcess(String zipFile, ProcessTypes processType, String processName) {
        waitForElementVisible(getRoot());
        setDeployProcessInput(zipFile, processType, processName);
        waitForElementVisible(deployConfirmButton).click();
    }

    public void redeployProcess(String zipFile, ProcessTypes processType, String processName) {
        tryToDeployProcess(zipFile, processType, processName);
        System.out.println("Re-deploy progress is finished!");
    }

    public void assertInvalidPackageError() {
        assertTrue(inputFileHasError());
        assertEquals(getErrorBubble().getText(),
                "A zip file is required. The file must be smaller than 1MB.");
    }

    public void assertInvalidProcessNameError() {
        assertTrue(inputProcessNameHasError());
        getProcessName().click();
        assertEquals(getErrorBubble().getText(), "A process name is required");
    }

    public WebElement getDeployProcessDialog() {
        return waitForElementVisible(deployProcessDialog);
    }

    public void setDeployProcessInput(String zipFilePath, ProcessTypes processType,
            String processName) {
        setZipFile(zipFilePath);
        setProcessType(processType);
        setProcessName(processName);
    }

    public boolean inputFileHasError() {
        return waitForElementVisible(deployProcessDialog).findElement(BY_FILE_INPUT_ERROR)
                .getAttribute("class").contains("has-error");
    }

    public boolean inputProcessNameHasError() {
        return waitForElementVisible(processNameInput).getAttribute("class").contains("has-error");
    }

    public WebElement getDeployConfirmButton() {
        return waitForElementVisible(deployConfirmButton);
    }

    private void setZipFile(String zipFilePath) {
        zipFileInput.sendKeys(zipFilePath);
    }

    private void setProcessType(ProcessTypes processType) {
        if (processType != ProcessTypes.DEFAULT)
            deployProcessDialog
                    .findElement(
                            By.xpath(XPATH_PROCESS_TYPE_OPTION.replace("${processType}",
                                    processType.name()))).click();
    }

    private void setProcessName(String processName) {
        processNameInput.clear();
        processNameInput.sendKeys(processName);
    }

    private WebElement getProcessName() {
        return waitForElementVisible(processNameInput);
    }

    private WebElement getErrorBubble() {
        return waitForElementVisible(BY_BUBBLE_ERROR, browser);
    }

}
