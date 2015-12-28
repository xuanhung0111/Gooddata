package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class AttributeValueSelectorPanel extends SelectItemPopupPanel {

    @FindBy(css = ".es_body[class*='s-item']:not(.gdc-hidden)")
    private List<WebElement> attributeValueItems;

    @FindBy(css = ".s-btn-all")
    private WebElement selectAllButton;

    @FindBy(css = ".s-btn-none")
    private WebElement clearButton;

    public void deselectAllValues() {
        waitForCollectionIsNotEmpty(attributeValueItems);
        waitForElementVisible(clearButton).click();
    }

    public void selectAllValues() {
        waitForCollectionIsNotEmpty(attributeValueItems);
        waitForElementVisible(selectAllButton).click();
    }

    public boolean areAllValuesSelected() {
        waitForCollectionIsNotEmpty(attributeValueItems);
        return attributeValueItems.stream()
                       .map(e -> e.findElement(By.tagName("input")))
                       .allMatch(WebElement::isSelected);
    }
}
