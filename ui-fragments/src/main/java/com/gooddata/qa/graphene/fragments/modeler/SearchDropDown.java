package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollBarIconToViewElement;

import static java.lang.String.format;

import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class SearchDropDown extends AbstractFragment {
    @FindBy(className= "gd-list-footer")
    WebElement totalRows;

    @FindBy(css = ".search-model-dropdown-item .item-id")
    List<WebElement> listItemNames;

    @FindBy(className = "public_Scrollbar_face")
    WebElement scrollBar;
    
    String ITEM = "//div[contains(string(), \"%s\") and contains(@class, 'search-model-dropdown-item')]";

    public static SearchDropDown getInstance(SearchContext context) {
        return Graphene.createPageFragment(SearchDropDown.class,
                waitForElementVisible(className("search-model-dropdown-body"), context));
    }

    public String getTextTotalRows() {
        return totalRows.getText();
    }

    public String getSearchItemName(String itemId) {
        log.info("****Looking for items: " + itemId);
        WebElement searchItem = browser.findElement(By.xpath(format(ITEM, itemId)));
        if (isElementVisible(searchItem)) {
            return searchItem.findElement(By.className("item-title")).getText();
        } else {
            scrollBarIconToViewElement(searchItem, browser, 10, 30000);
            return searchItem.findElement(By.className("item-title")).getText();
        }
    }

    public WebElement getSearchItem(String itemId) {
        WebElement element = listItemNames.stream().filter(el -> el.getText().equals(itemId)).findFirst().get();
        scrollElementIntoView(element, browser);
        WebElement el = browser.findElement(By.xpath(format(ITEM, itemId)));
        return browser.findElement(By.xpath(format(ITEM, itemId)));
    }

    public String getItemName(WebElement item){
        scrollBarIconToViewElement(item, browser, 5, 30000);
        WebElement itemName = item.findElement(By.className("item-title"));
        return itemName.getText();
    }

    public void selectItem(String itemId) {
        WebElement item = getSearchItem(itemId);
        scrollElementIntoView(item,browser);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(item).click().build().perform();
    }

    public enum SEARCH_ITEM {
        DATASET("dataset.%s"),
        ATTRIBUTE("attr.%s.%s"),
        LABEL("label.%s.%s"),
        OPT_LABEL("label.%s.%s.%s"),
        FACT("fact.%s.%s"),
        DATE_DATASET("%s");

        private final String classId;

        private SEARCH_ITEM(String className) {
            this.classId= className;
        }

        public String getClassId() {
            return classId;
        }
    }
}
