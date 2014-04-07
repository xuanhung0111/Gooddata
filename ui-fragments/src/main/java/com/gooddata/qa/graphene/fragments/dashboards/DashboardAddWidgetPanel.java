package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardAddWidgetPanel extends AbstractFragment {

	@FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]")
	private WebElement widgetConfigPanel;

	@FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//div[contains(@class,'configPanel-views')]//button")
	private WebElement widgetConfigMetricButton;

	@FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//button[contains(@class,'s-btn-apply')]")
	private WebElement widgetConfigApplyButton;

	private static final String widgetLocator = "//div[contains(@class,'yui3-c-adddashboardwidgetpickerpanel')]//div[contains(@class,'add-dashboard-item')]/div[contains(text(), '${widgetLabel}')]/../button";
	private static final String widgetMetricLocator = "//div[contains(@class,'yui3-widget-stacked shelterPlugin-plugged')]//div[contains(@class,'yui3-c-picker-content')]//div[contains(@class,'yui3-c-simplecolumn')]//div[contains(@class,'c-label') and contains(@class,'s-enabled')]/span[text()='${metricLabel}']";

	public void addWidget(WidgetTypes type, String metricLabel)
			throws InterruptedException {
		By widgetType = By.xpath(widgetLocator.replace("${widgetLabel}",
                type.getLabel()));
		waitForElementVisible(widgetType).click();
		// TODO fragments for widget config panel + metric selection can be used
		// - but better IDs and UI organization is required
		waitForElementVisible(widgetConfigPanel);
		waitForElementVisible(widgetConfigMetricButton).click();
		By metricInWidget = By.xpath(widgetMetricLocator.replace(
				"${metricLabel}", metricLabel));
		waitForElementVisible(metricInWidget).click();
		Thread.sleep(3000);
		waitForElementVisible(widgetConfigApplyButton).click();
		waitForElementNotVisible(widgetConfigPanel);
	}

}
