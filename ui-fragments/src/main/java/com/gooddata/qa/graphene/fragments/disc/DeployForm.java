package com.gooddata.qa.graphene.fragments.disc;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DeployForm extends AbstractFragment {

	private static final By BY_DEPLOY_DIALOG_HEADER = By
			.xpath("//div[@class='deploy-process-dialog-area']/h2[@class='main-dialog-title']");
	private static final By BY_FILE_INPUT_ERROR = By
			.cssSelector(".select-zip .zip-name-text input");
	private static final By BY_INPUT_ERROR_BUBBLE = By.cssSelector(".bubble-overlay");

	private static final String XPATH_PROCESS_TYPE_OPTION = "//select/option[@value='${processType}']";

	@FindBy(css = "div.deploy-process-dialog-area")
	protected WebElement deployProcessDialog;

	@FindBy(css = "div.deploy-process-button-area")
	protected WebElement deployProcessDialogButton;

	@FindBy(xpath = "//div[@class='select-zip']/div/input")
	protected WebElement zipFileInput;

	@FindBy(xpath = "//div[@class='select-zip']//input[contains(@class, 'has-error')]")
	protected WebElement zipFileInputError;

	@FindBy(xpath = "//div[@class='deploy-process-dialog-area']/div[contains(@class, 'whole-line-text')]/div[contains(@class, 'bubble-overlay')]")
	protected WebElement processNameErrorBubble;

	@FindBy(xpath = "//div[@class='deploy-process-dialog-area']/div[contains(@class, 'whole-line-text')]/input")
	protected WebElement processNameInput;

	@FindBy(css = "div.deploy-process-button-area button.button-positive")
	protected WebElement deployConfirmButton;

	@FindBy(css = "div.deploy-process-button-area button.s-btn-cancel")
	protected WebElement deployCancelButton;

	public String getDeployDialogHeader() {
		return waitForElementVisible(BY_DEPLOY_DIALOG_HEADER, browser).getText();
	}

	public void setZipFile(String zipFilePath) {
		zipFileInput.sendKeys(zipFilePath);
	}

	public void setProcessType(DISCProcessTypes processType) {
		if (processType != DISCProcessTypes.DEFAULT)
			deployProcessDialog
					.findElement(
							By.xpath(XPATH_PROCESS_TYPE_OPTION.replace("${processType}",
									processType.name()))).click();
	}

	public void setProcessName(String processName) {
		processNameInput.clear();
		processNameInput.sendKeys(processName);
	}

	public WebElement getProcessName() {
		return processNameInput;
	}

	public WebElement getDeployConfirmButton() {
		return deployConfirmButton;
	}

	public WebElement getDeployCancelButton() {
		return deployCancelButton;
	}

	public WebElement getDeployProcessDialog() {
		return deployProcessDialog;
	}

	public WebElement getDeployProcessDialogButton() {
		return deployProcessDialogButton;
	}

	public void setDeployProcessInput(String zipFilePath, DISCProcessTypes processType,
			String processName) {
		setZipFile(zipFilePath);
		setProcessType(processType);
		setProcessName(processName);
	}

	public boolean inputFileHasError() {
		return deployProcessDialog.findElement(BY_FILE_INPUT_ERROR).getAttribute("class")
				.contains("has-error");
	}

	public boolean inputProcessNameHasError() {
		return processNameInput.getAttribute("class").contains("has-error");
	}

	public WebElement getFileInputErrorBubble() {
		return deployProcessDialog.findElement(BY_INPUT_ERROR_BUBBLE);
	}

	public WebElement getProcessNameErrorBubble() {
		return processNameErrorBubble;
	}

	public void assertErrorOnDeployForm(String zipFilePath, DISCProcessTypes processType,
			String processName) {
		setDeployProcessInput(zipFilePath, processType, processName);
		getDeployConfirmButton().click();
		if (zipFilePath.isEmpty()) {
			waitForElementVisible(getFileInputErrorBubble());
			Assert.assertTrue(inputFileHasError());
			Assert.assertEquals(getFileInputErrorBubble().getText(),
					"A zip file is required. The file must be smaller than 1MB.");
		}
		if (processName.isEmpty()) {
			Assert.assertTrue(inputProcessNameHasError());
			getProcessName().click();
			waitForElementVisible(getProcessNameErrorBubble());
			Assert.assertEquals(getProcessNameErrorBubble().getText(), "A process name is required");
		}
	}
}
