package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardTextObject extends AbstractFragment {
    
    private String textLabelLocator = "//div[contains(@class,'gdc-menu-simple')]//span[@title='${textLabel}']";

    private String textWidgetLocator =
            "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'${textWidgetType}')]";

    private String addLinkButtonLocator = "//div[contains(@class,'yui3-toolbar-icon-addLink')]";

    private String addLinkTitleLocator = "//input[@name='addLinkTitle']";

    private String addLinkAddressLocator = "//textarea[@name='addLinkAddress']";

    private String addButtonLocator = "//div[@class='bd_controls']/span/button[text()='Add']";

    public void addText(TextObject textObject, String text, String link) throws InterruptedException {
        waitForElementVisible(By.xpath(textLabelLocator.replace("${textLabel}", textObject.getName())), 
                browser).click();
        waitForElementVisible(By.cssSelector(".yui3-c-textdashboardwidget"), browser);
        waitForElementVisible(By.xpath(addLinkButtonLocator), browser).click();
        waitForElementVisible(By.xpath(addLinkTitleLocator), browser).sendKeys(text);
        waitForElementVisible(By.xpath(addLinkAddressLocator), browser).sendKeys(link);
        waitForElementVisible(By.xpath(addButtonLocator), browser).click();
        waitForElementVisible(By.xpath(textWidgetLocator.replace("${textWidgetType}", textObject.getLabel())), 
                browser);
        Thread.sleep(2000);
    }
}
