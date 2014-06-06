package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ProjectDetailPage extends AbstractFragment {

	private final static String XPATH_PROCESS_TAB_BUTTON = "//div[@class='project-detail-content']//div[${processIndex}]//div[contains(@class, 'tab-buttons')]/a[${index}]";
	private final static String XPATH_PROCESS_REDEPLOY_BUTTON = "//div[@class='project-detail-content']/div/div[${processIndex}]//a[text()='Re-deploy']";

	private final static By BY_PROCESS_NAME = By.cssSelector(".secondary-title");

	@FindBy(xpath = "//div[@class='project-header']")
	protected WebElement projectDetailHeader;

	@FindBy(css = ".page-header .s-btn-deploy_process")
	protected WebElement deployProcessButton;

	@FindBy(css = ".process-detail")
	protected List<WebElement> processes;
    
    @FindBy(css = ".executables-table")
    protected ExecutablesTable executablesTable;
    
    @FindBy(xpath = "//div[@class='project-detail-content']/div/div[${processIndex}]//a[text()='Re-deploy']")
    protected WebElement redeployButton;
    
    @FindBy(css = ".error_dialog .dialog-body")
    protected WebElement deployErrorDialog;

	public WebElement getDeployProcessButton() {
		return deployProcessButton;
	}

	public List<WebElement> getProcessesList() {
		return processes;
	}
	
	public WebElement getDeployErrorDialog() {
		return deployErrorDialog;
	}

	public int getNumberOfProcesses() {
		if (processes == null) {
			throw new NullPointerException();
		}
		return processes.size();
	}

	public WebElement getProcess(int processIndex) {
		if (processIndex < 0 || processIndex > getNumberOfProcesses()) {
			throw new IndexOutOfBoundsException();
		}
		return processes.get(processIndex);
	}

	public WebElement getScheduleTab(int processIndex) {
		return waitForElementVisible(By.xpath(XPATH_PROCESS_TAB_BUTTON.replace("${processIndex}", String.valueOf(processIndex)).replace("${index}", "1")));
	}
	
	public WebElement getExecutableTab(int processIndex) {
		return waitForElementVisible(By.xpath(XPATH_PROCESS_TAB_BUTTON.replace("${processIndex}", String.valueOf(processIndex)).replace("${index}", "2")));
	}
	
	public WebElement getRedeployButton(String processName) {
		for (int i = 0; i < getNumberOfProcesses(); i++) {
			waitForElementVisible(getProcess(i));
			if (getProcess(i).findElement(BY_PROCESS_NAME).getText().equals(processName))
			{
				return getProcess(i).findElement(By.xpath(XPATH_PROCESS_REDEPLOY_BUTTON.replace("${processIndex}", String.valueOf(i+1))));
			}
		}
		return getProcess(getNumberOfProcesses()-1).findElement(By.xpath(XPATH_PROCESS_REDEPLOY_BUTTON.replace("${processIndex}", "not found")));
	}
	
	protected void assertExecutablesList(DISCProcessTypes processType, List<String> executablesList) {
		executablesTable.assertExecutablesList(processType, executablesList);
	}

	public boolean assertProcessInList(String processName, DISCProcessTypes processType, List<String> executablesList) {
		for (int i = 0; i < this.getNumberOfProcesses(); i++ ) {
			if (getProcess(i).findElement(BY_PROCESS_NAME).getText().equals(processName))
			{
				getScheduleTab(i+1).click();
				Assert.assertEquals(getScheduleTab(i+1).getText(), "0 schedules");
				getExecutableTab(i+1).click();
				String executableTitle = processType.getProcessTypeExecutable();
				if(executablesList.size() > 1)
					executableTitle = processType.getProcessTypeExecutable() + "s";
				Assert.assertEquals(getExecutableTab(i+1).getText(), String.format("%d %s total", executablesList.size(), executableTitle));
				waitForElementVisible(executablesTable.getRoot());
				assertExecutablesList(processType, executablesList);
				return true;
			}
		}
		return false;
	}
}
