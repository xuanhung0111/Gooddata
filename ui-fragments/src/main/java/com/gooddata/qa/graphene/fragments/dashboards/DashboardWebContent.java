package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardWebContent extends AbstractFragment {

    @FindBy(xpath = "//textarea[@name='addUrl']")
    private WebElement addURL;

    @FindBy(xpath = "//form/div/span[contains(@class,'right')]/button[text()='Save']")
    private WebElement saveWebContentButton;

    @FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'yui3-c-iframedashboardwidget')]")
    private WebElement iframeWidget;

    public void addWebContent(String embedCode) {
        waitForElementVisible(addURL);
        addURL.sendKeys(embedCode);
        waitForElementVisible(saveWebContentButton).click();
        waitForElementNotVisible(saveWebContentButton);
        sleepTightInSeconds(2);
    }
}
