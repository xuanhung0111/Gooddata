package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;

public class PivotAggregationPopup extends AbstractFragment {
    public static final By LOCATOR = By.className("s-table-header-menu-content");

    @FindBy(className = "gd-menu-item")
    private List<WebElement> items;

    public List<String> getItemsList() {
        return waitForCollectionIsNotEmpty(items).stream().map(WebElement::getText).collect(toList());
    }

    public void selectItem(AggregationItem item) {
        waitForElementVisible(cssSelector(
            format(".s-menu-aggregation-%s .s-menu-aggregation-inner", item.getMetadataName())), getRoot()).click();
    }

    public Boolean isItemChecked(AggregationItem type) {
        int indexItem = getElementTexts(waitForCollectionIsNotEmpty(items)).indexOf(type.getFullName());
        return waitForCollectionIsNotEmpty(items)
            .get(indexItem).getAttribute("class").contains("is-checked");
    }
}
