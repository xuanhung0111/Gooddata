package com.gooddata.qa.graphene.fragments.dashboards.widget.addlink;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DashboardEditWidgetToolbarPanel;

public class AddLinkDialog extends AbstractFragment {
    
    private static final By LOCATOR = By.className("yui3-d-addlinkdialog");

    @FindBy(xpath = "//input[@name='addLinkTitle']")
    private WebElement addLinkTitle;

    @FindBy(xpath = "//textarea[@name='addLinkAddress']")
    private WebElement addLinkAddress;

    @FindBy(xpath = "//div[@class='bd_controls']/span/button[text()='Add']")
    private WebElement addButton;
    
    public static final AddLinkDialog openAddLinkDialogFor(WebElement element,
            SearchContext searchContext) {
        DashboardEditWidgetToolbarPanel.openAddLinkPanelFor(element, searchContext);

        return Graphene.createPageFragment(AddLinkDialog.class,
                waitForElementVisible(LOCATOR, searchContext));
    }

    public void addLink(String title, String webAdress) {
        waitForElementVisible(addLinkTitle).sendKeys(title);
        waitForElementVisible(addLinkAddress).sendKeys(webAdress);
        waitForElementVisible(addButton).click();
    }
}
