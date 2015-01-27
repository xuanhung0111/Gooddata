package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AttributeFilterPickerPanel extends AbstractFragment {

    public static final By LOCATOR = By.cssSelector(".adi-attr-filter-picker");

    @FindBy(css = ".s-btn-select_all")
    private WebElement selectAllButton;

    @FindBy(css = ".s-btn-clear")
    private WebElement clearButton;

    @FindBy(css = ".s-filter-item>div>span")
    private List<WebElement> items;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(css = ".s-btn-apply")
    private WebElement applyButton;

    private static final By BY_INPUT = By.cssSelector("input");

    public void select(String... values) {
        if (values.length == 1 && "All".equals(values[0])) {
            selectAll();
            return;
        }
        waitForElementVisible(clearButton).click();
        for (String value : values) {
            for (WebElement e : items) {
                if (value.equals(e.getText()))
                    e.findElement(BY_INPUT).click();
            }
        }
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void selectAll() {
        waitForElementVisible(selectAllButton).click();
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void discard() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void assertPanel() {
        waitForElementVisible(selectAllButton);
        waitForElementVisible(clearButton);
        waitForElementVisible(applyButton);
        waitForElementVisible(cancelButton);
    }
}
