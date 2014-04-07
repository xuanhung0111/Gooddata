package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DashboardAddReportPanel extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'s-report-picker')]//input")
    private WebElement reportSearchInput;

    @FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'yui3-c-reportdashboardwidget')]")
    private WebElement reportWidget;

    @FindBy(xpath = "//div[contains(@class,'yui3-selectionbox-resize-r')]")
    private WebElement resizeIcon;

    private static final String reportToAddLocator = "//div[contains(@class,'s-report-picker')]//div[contains(@class,'yui3-c-label-content')]/span[@title='${reportName}']";
    private static final String reportOnDashboardLocator = "//div[@id='p-projectDashboardPage']//div[contains(@class,'yui3-c-reportdashboardwidget')]//a[@title='${reportName}']";

    public void addReport(String reportName) {
        waitForElementVisible(reportSearchInput).clear();
        reportSearchInput.sendKeys(reportName);
        By reportToAdd = By.xpath(reportToAddLocator.replace("${reportName}",
                reportName));
        waitForElementVisible(reportToAdd).click();
        waitForDashboardPageLoaded();
        By reportOnDashboard = By.xpath(reportOnDashboardLocator.replace(
                "${reportName}", reportName));
        waitForElementPresent(reportOnDashboard);
    }
}
