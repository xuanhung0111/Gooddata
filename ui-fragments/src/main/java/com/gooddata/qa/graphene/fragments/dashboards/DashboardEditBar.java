package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.testng.Assert;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardFilter;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardLineObject;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTextObject;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardWebContent;

public class DashboardEditBar extends AbstractFragment {

	@FindBy(css = ".s-btn-save")
	private WebElement saveButton;

	@FindBy(css = ".s-btn-cancel")
	private WebElement cancelButton;

	@FindBy(css = ".s-btn-actions")
	private WebElement actionsMenu;

	@FindBy(css = ".s-delete")
	private WebElement deleteButton;

	@FindBy(css = ".s-btn-report")
	private WebElement reportMenuButton;

	@FindBy(css = ".s-btn-widget")
	private WebElement widgetMenuButton;

	@FindBy(xpath = "//div[contains(@class,'gdc-overlay-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
	private DashboardWidgets dashboardWidgets;

	@FindBy(xpath = "//span[text()='Web Content']")
	private WebElement addwebContent;

	@FindBy(xpath = "//div[contains(@class,'yui3-d-modaldialog')]")
	private DashboardWebContent dashboardWebContent;

	@FindBy(xpath = "//div[contains(@class,'gdc-overlay-simple')]")
	private DashboardReport dashboardReport;

	@FindBy(xpath = "//button[contains(@class,'s-btn-text')]")
	private WebElement addText;

	@FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]")
	private DashboardTextObject dashboardTextObject;

	@FindBy(xpath = "//span[text()='Line']")
	private WebElement addLine;

	@FindBy(xpath = "//div[contains(@class,'gdc-menu-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
	private DashboardLineObject dashboardLineObject;

	@FindBy(xpath = "//div[contains(@class,'c-confirmDeleteDialog')]//button[contains(@class,'s-btn-delete')]")
	private WebElement deleteDashboardDialogButton;

	@FindBy(xpath = "//div[contains(@class,'yui3-c-dashboardcollectionwidget-content')]/div[contains(@class,'yui3-c-dashboardwidget')]")
	private List<WebElement> listDashboardWidgets;

	@FindBy(xpath = "//span[text()='Filter']")
	private WebElement addFilterMenu;

	@FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]")
	private DashboardFilter dashboardFilter;

	public void addReportToDashboard(String reportName) {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(reportMenuButton).click();
		waitForElementVisible(dashboardReport.getRoot());
		dashboardReport.addReport(reportName);
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");
	}

	public void addWidgetToDashboard(WidgetTypes widgetype, String metricLabel)
			throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(widgetMenuButton).click();
		waitForElementVisible(dashboardWidgets.getRoot());
		dashboardWidgets.addWidget(widgetype, metricLabel);
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");
	}

	public void addWebContentToDashboard() throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(addwebContent).click();
		waitForElementVisible(dashboardWebContent.getRoot());
		dashboardWebContent.addWebContent();
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");

	}

	public void addListFilterToDashboard(DashFilterTypes type, String name)
			throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(addFilterMenu).click();
		waitForElementVisible(dashboardFilter.getRoot());
		dashboardFilter.addListFilter(type, name);
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");
	}

	public void addTimeFilterToDashboard(int dateDimensionIndex)
			throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(addFilterMenu).click();
		waitForElementVisible(dashboardFilter.getRoot());
		dashboardFilter.addTimeFilter(dateDimensionIndex);
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");
	}

	public void addTextToDashboard(TextObject textObject, String text,
			String link) throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(addText).click();
		waitForElementVisible(dashboardTextObject.getRoot());
		dashboardTextObject.addText(textObject, text, link);
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 1,
				"Widget wasn't added");
	}

	public void addLineToDashboard() throws InterruptedException {
		int widgetCountBefore = listDashboardWidgets.size();
		waitForElementVisible(addLine).click();
		waitForElementVisible(dashboardLineObject.getRoot());
		dashboardLineObject.addLineHorizonalToDashboard();
		waitForElementVisible(addLine).click();
		waitForElementVisible(dashboardLineObject.getRoot());
		dashboardLineObject.addLineVerticalToDashboard();
		int widgetCountAfter = listDashboardWidgets.size();
		Assert.assertEquals(widgetCountAfter, widgetCountBefore + 2,
				"Widget wasn't added");
	}

	public void saveDashboard() {
		waitForElementVisible(saveButton);
		Graphene.guardAjax(saveButton).click();
		waitForElementNotVisible(this.getRoot());
	}

	public void deleteDashboard() throws InterruptedException {
		waitForElementVisible(actionsMenu).click();
		waitForElementVisible(deleteButton).click();
		Thread.sleep(3000);
		waitForElementVisible(deleteDashboardDialogButton).click();
		waitForElementNotPresent(this.getRoot());
	}

	public void moveWidget(WebElement widget, int x, int y) {
		waitForElementVisible(widget);
		Actions builder = new Actions(browser);
		builder.dragAndDropBy(widget, x, y).build().perform();

	}

}
