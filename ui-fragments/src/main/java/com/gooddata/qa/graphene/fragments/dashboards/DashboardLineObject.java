package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardLineObject extends AbstractFragment {

    @FindBy(xpath = "//span[text()='Horizontal']")
    private WebElement lineHorizontal;

    @FindBy(xpath = "//span[text()='Vertical']")
    private WebElement lineVertical;

    @FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'yui3-c-linedashboardwidget')]")
    private WebElement lineWidget;

    public void addLineHorizonalToDashboard() {
        waitForElementVisible(lineHorizontal).click();
        sleepTightInSeconds(2);
    }

    public void addLineVerticalToDashboard() {
        waitForElementVisible(lineVertical).click();
        sleepTightInSeconds(2);
    }
}
