package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.By;

import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class WarningAddUserPopUp extends AbstractFragment {
    private static final String OVERLAY_CUSTOM = "gd-message-overlay-custom";

    @FindBy(css = ".gd-message-text-showmore .s-message-text-header-value")
    private WebElement headerValue;

    @FindBy(css = ".s-message-text-content")
    private WebElement warningMessage;

    @FindBy(className = "icon-cross")
    private WebElement closeBtn;

    public static WarningAddUserPopUp getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(WarningAddUserPopUp.class, waitForElementVisible(className(OVERLAY_CUSTOM), searchContext));
    }

    public String getHeaderText() {
        return headerValue.getText();
    }

    public String getWarningText() {
        WebElement showMoreText = this.getRoot().findElement(By.xpath("//span[contains(text(), 'Show More')]"));
        showMoreText.click();
        return waitForElementVisible(warningMessage).getText();
    }
}
