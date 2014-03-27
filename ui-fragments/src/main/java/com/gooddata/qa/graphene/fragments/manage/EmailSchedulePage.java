package com.gooddata.qa.graphene.fragments.manage;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.List;

public class EmailSchedulePage extends AbstractFragment {
	
	@FindBy(css = ".s-btn-schedule_new_email")
	private WebElement addScheduleButton;
	
	@FindBy(css = ".noSchedulesMsg")
	private WebElement noSchedulesMessage;
	
	@FindBy(css = ".listTable")
	private WebElement schedulesTable;
	
	@FindBy(css = ".detailView")
	private WebElement scheduleDetail;
	
	@FindBy(name = "emailAddresses")
	private WebElement emailToInput;
	
	@FindBy(name = "emailSubject")
	private WebElement emailSubjectInput;
	
	@FindBy(name = "emailBody")
	private WebElement emailMessageInput;
	
	@FindBy(css = ".objectSelect .dashboards")
	private WebElement dashboardsSelector;
	
	@FindBy(css = ".objectSelect .reports")
	private WebElement reportsSelector;
	
	@FindBy(css = ".dashboards .picker .c-checkBox")
	private List<WebElement> dashboardsList;
	
	@FindBy(css = ".reports .picker .c-checkBox")
	private List<WebElement> reportsList;
	
	@FindBy(css = ".reports .exportFormat .c-checkBox")
	private List<WebElement> formatsList;
	
	@FindBy(css = ".s-btn-save")
	private WebElement saveButton;
	
	public int getNumberOfSchedules() {
		waitForElementPresent(schedulesTable);
		int schedulesCount = schedulesTable.findElement(By.tagName("tbody")).findElements(By.tagName("tr")).size();
		if (schedulesCount == 0 && noSchedulesMessage.isDisplayed()) {
			return 0;
		} else {
			return schedulesCount;
		}
	}
	
	public String getNoSchedulesMessage() {
		return waitForElementVisible(noSchedulesMessage).getText();
	}
	
	public void scheduleNewDahboardEmail(String emailTo, String emailSubject, String emailBody, String dashboardName) {
		Graphene.guardAjax(waitForElementVisible(addScheduleButton)).click();
		waitForElementVisible(scheduleDetail);
		waitForElementVisible(emailToInput).sendKeys(emailTo);
		emailSubjectInput.sendKeys(emailSubject);
		emailMessageInput.sendKeys(emailBody);
		waitForElementVisible(dashboardsSelector);
		waitForEmailSchedulePageLoaded();
		Assert.assertTrue(dashboardsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"), "Dashboards selector is not selected by default");
		selectDashboard(dashboardName);
		// TODO - schedule (will be sent in the nearest time slot now)
		Graphene.guardAjax(waitForElementVisible(saveButton)).click();
		waitForElementNotVisible(scheduleDetail);
		waitForElementVisible(schedulesTable);
	}
	
	public void scheduleNewReportEmail(String emailTo, String emailSubject, String emailBody, String reportName, ExportFormat format) {
		Graphene.guardAjax(waitForElementVisible(addScheduleButton)).click();
		waitForElementVisible(scheduleDetail);
		waitForElementVisible(emailToInput).sendKeys(emailTo);
		emailSubjectInput.sendKeys(emailSubject);
		emailMessageInput.sendKeys(emailBody);
		waitForElementVisible(reportsSelector).click();
		waitForEmailSchedulePageLoaded();
		Assert.assertTrue(reportsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"), "Reports selector is not selected");
		selectReport(reportName);
		selectReportFormat(format);
		// TODO - schedule (will be sent in the nearest time slot now)
		Graphene.guardAjax(waitForElementVisible(saveButton)).click();
		waitForElementNotVisible(scheduleDetail);
		waitForElementVisible(schedulesTable);
	}

	public String getScheduleMailUriByName(String scheduleName) {
		waitForElementPresent(schedulesTable);

		String anchorSelector = "tbody td.title.s-title-" + CssUtils.simplifyText(scheduleName) + " a";
		WebElement aElement = schedulesTable.findElement(By.cssSelector(anchorSelector));
		String hRef = aElement.getAttribute("href");

		String[] hRefParts = hRef.split("\\|");
		return hRefParts[hRefParts.length-1];
	}

	private void selectDashboard(String dashboardName) {
		if (dashboardsList != null && dashboardsList.size() > 0) {
			for (WebElement elem : dashboardsList) {
				if (elem.findElement(By.tagName("label")).getText().equals(dashboardName)) {
					elem.findElement(By.tagName("input")).click();
					return;
				}
			}
			Assert.fail("Requested dashboard wasn't found");
		} else {
			Assert.fail("No dashboards are available");
		}
	}
	
	private void selectReport(String reportName) {
		if (reportsList != null && reportsList.size() > 0) {
			for (WebElement elem : reportsList) {
				if (elem.findElement(By.tagName("label")).getText().equals(reportName)) {
					elem.findElement(By.tagName("input")).click();
					return;
				}
			}
			Assert.fail("Requested report wasn't found");
		} else {
			Assert.fail("No reports are available");
		}
	}
	
	private void selectReportFormat(ExportFormat format) {
		if (formatsList != null && formatsList.size() > 0) {
			By checkboxLocator = By.tagName("input");
			switch (format) {
			case ALL:
				for (int i = 1; i < formatsList.size(); i++) {
					formatsList.get(i).findElement(checkboxLocator).click();
				}
				break;
			case PDF:
				formatsList.get(1).findElement(checkboxLocator).click();
				break;
			case CSV:
				formatsList.get(3).findElement(checkboxLocator).click();
				break;
			case EXCEL_XLS:
				formatsList.get(2).findElement(checkboxLocator).click();
				break;
			default:
				System.out.println("Invalid format!!!");
				break;
			}
		}
	}
	
}
