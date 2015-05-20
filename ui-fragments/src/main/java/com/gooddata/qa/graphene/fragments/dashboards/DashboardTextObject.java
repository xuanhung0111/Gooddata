package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.addlink.AddLinkDialog;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardTextObject extends AbstractFragment {
    
    private String textLabelLocator = "//div[contains(@class,'gdc-menu-simple')]//span[@title='${textLabel}']";

    private String textWidgetLocator =
            "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div"
            + "/div[contains(@class,'${textWidgetType}')]";

    public void addText(TextObject textObject, String text, String link) throws InterruptedException {
        waitForElementVisible(By.xpath(textLabelLocator.replace("${textLabel}", textObject.getName())), 
                browser).click();
        WebElement dashboardTextObject = waitForElementVisible(By.cssSelector(".yui3-c-textdashboardwidget"), 
                browser);
        AddLinkDialog addLinkDialog = AddLinkDialog.openAddLinkDialogFor(dashboardTextObject, browser);
        addLinkDialog.addLink(text, link);
        waitForElementVisible(By.xpath(textWidgetLocator.replace("${textWidgetType}", textObject.getLabel())), 
                browser);
        Thread.sleep(2000);
    }
}
