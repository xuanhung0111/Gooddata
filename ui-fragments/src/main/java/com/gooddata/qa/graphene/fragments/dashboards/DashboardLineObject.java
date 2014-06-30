package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardLineObject extends AbstractFragment {

	@FindBy(xpath = "//span[text()='Horizontal']")
	private WebElement lineHorizontal;

	@FindBy(xpath = "//span[text()='Vertical']")
	private WebElement lineVertical;

	@FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'yui3-c-linedashboardwidget')]")
	private WebElement lineWidget;

	public void addLineHorizonalToDashboard() throws InterruptedException {
		waitForElementVisible(lineHorizontal).click();
		Thread.sleep(2000);
	}
	
	public void addLineVerticalToDashboard() throws InterruptedException {
		waitForElementVisible(lineVertical).click();
		Thread.sleep(2000);
	}
}
