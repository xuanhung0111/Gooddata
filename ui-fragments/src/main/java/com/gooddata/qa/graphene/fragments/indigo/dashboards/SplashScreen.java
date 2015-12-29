package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

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

    public void startEditingWidgets() {
        waitForElementVisible(createKpiDashboardButton).click();
    }

    public String getMobileMessage() {
        return waitForElementVisible(mobileMessage).getText();
    }

}
