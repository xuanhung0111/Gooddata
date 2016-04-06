package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.dlui.Field;

public class SelectionArea extends AbstractFragment {

    private By BY_ICON_CROSS = By.cssSelector(".icon-cross");

    private String XPATH_SELECTED_FIELD =
            "//.[contains(@class, 'checked-item') and text()='${selectedFieldTitle}']/..";

    @FindBy(xpath = "//.[contains(@class, 'checked-item-title')]")
    private List<WebElement> selectedFields;

    public void deselectFields(Field... fields) {
        for (Field field : fields) {
            WebElement selectedField =
                    waitForElementVisible(
                            By.xpath(XPATH_SELECTED_FIELD.replace("${selectedFieldTitle}",
                                    field.getName())), getRoot());
            waitForElementVisible(BY_ICON_CROSS, selectedField).click();
            waitForElementNotPresent(selectedField);
        }
    }

    public List<WebElement> getSelectedFields() {
        waitForCollectionIsNotEmpty(selectedFields);
        return selectedFields;
    }
}
