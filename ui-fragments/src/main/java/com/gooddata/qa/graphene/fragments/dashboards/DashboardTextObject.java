package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardTextObject extends AbstractFragment {

    private String textWidgetLocator =
            "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'${textWidgetType}')]";

    @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-addLink')]")
    private WebElement addLinkButton;

    @FindBy(xpath = "//input[@name='addLinkTitle']")
    private WebElement addLinkTitle;

    @FindBy(xpath = "//textarea[@name='addLinkAddress']")
    private WebElement addLinkAddress;

    @FindBy(xpath = "//div[@class='bd_controls']/span/button[text()='Add']")
    private WebElement addButton;

    public void addText(TextObject textObject, String text, String link)
            throws InterruptedException {
        waitForElementVisible(addLinkButton).click();
        waitForElementVisible(addLinkTitle);
        addLinkTitle.sendKeys(text);
        waitForElementVisible(addLinkAddress);
        addLinkAddress.sendKeys(link);
        waitForElementVisible(addButton).click();
        WebElement textWidget = browser.findElement(
                By.xpath(textWidgetLocator.replace("${textWidgetType}", textObject.getLabel())));
        waitForElementVisible(textWidget);
        Thread.sleep(2000);
    }
}
