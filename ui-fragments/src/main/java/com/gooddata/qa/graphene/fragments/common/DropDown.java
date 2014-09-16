package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DropDown extends AbstractFragment {

    @FindBy(css = ".gd-list-view")
    private WebElement listView;

    @FindBy(xpath = "//div[contains(@class,'gd-dropdown-search')]/input")
    private WebElement searchInput;

    private static final String itemLocator = ".gd-list-view-item.s-%s";

    public void selectItem(String name) {
        String itemCssSelector = String.format(itemLocator, simplifyText(name));
        waitForElementVisible(listView);
        By item = By.cssSelector(String.format(itemCssSelector));
        waitForElementVisible(item, browser).click();
    }

    public void searchItem(String name) {
        waitForElementVisible(searchInput);
        searchInput.sendKeys(name);
        waitForElementVisible(listView);
    }

    public void searchAndSelectItem(String name) {
        searchItem(name);
        selectItem(name);
    }

    private String simplifyText(String text) {
        return text.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
    }
}
