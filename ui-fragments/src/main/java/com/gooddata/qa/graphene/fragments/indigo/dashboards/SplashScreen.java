package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SplashScreen extends AbstractFragment {

    @FindBy(className = "s-create_kpi_dashboard")
    private WebElement createKpiDashboardButton;

    @FindBy(className = "splashscreen-mobile-message")
    private WebElement mobileMessage;

    public void waitForCreateKpiDashboardButtonMissing() {
        waitForElementNotVisible(createKpiDashboardButton);
    }

    public void waitForCreateKpiDashboardButtonVisible() {
        waitForElementVisible(createKpiDashboardButton);
    }

    public IndigoDashboardsPage startEditingWidgets() {
        // wait until initial animation (css-based) is finished before clicking
        sleepTightInSeconds(1);
        waitForElementVisible(createKpiDashboardButton).click();
        return IndigoDashboardsPage.getInstance(browser);
    }

    public String getMobileMessage() {
        return waitForElementVisible(mobileMessage).getText();
    }

}
