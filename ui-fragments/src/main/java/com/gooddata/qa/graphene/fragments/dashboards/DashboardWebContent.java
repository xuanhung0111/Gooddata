package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class DashboardWebContent extends AbstractFragment {

    @FindBy(xpath = "//textarea[@name='addUrl']")
    private WebElement addURL;

    @FindBy(xpath = "//form/div/span[contains(@class,'right')]/button[text()='Save']")
    private WebElement saveWebContentButton;

    @FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'yui3-c-iframedashboardwidget')]")
    private WebElement iframeWidget;

    public void addWebContent() throws InterruptedException {
        waitForElementVisible(addURL);
        addURL.sendKeys("https://www.gooddata.com");
        waitForElementVisible(saveWebContentButton).click();
        waitForElementNotVisible(saveWebContentButton);
        Thread.sleep(2000);
    }
}
