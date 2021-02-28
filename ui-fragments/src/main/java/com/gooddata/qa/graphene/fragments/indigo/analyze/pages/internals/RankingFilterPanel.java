package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class RankingFilterPanel extends AbstractFragment {

    @FindBy(className = "s-relative-range-input")
    private WebElement inputValue;

    @FindBy(className = "s-rf-operator-dropdown-button")
    private WebElement operatorDropdownButton;

    @FindBy(className = "s-rf-attribute-dropdown-button")
    private WebElement attributeDropdownButton;

    @FindBy(className = "s-rf-measure-dropdown-button")
    private WebElement measureDropdownButton;

    @FindBy(className = "gd-list-item-header")
    private WebElement warningMessage;

    @FindBy(css = ".s-rf-attribute-no-options-bubble .content")
    private WebElement messageNoOptionsBubble;

    @FindBy(css = ".s-rf-preview span")
    private WebElement preview;

   @FindBy(className = "s-apply")
    private WebElement applyButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    public static final By LOCATOR = className("s-rf-dropdown-body");

    private final By BY_OPERATORS_SELECT = cssSelector(".s-rf-operator-dropdown-body button span");
    private final By BY_ATTRIBUTES_SELECT = cssSelector(".s-rf-attribute-dropdown-body button span");
    private final By BY_MEASURES_SELECT = cssSelector(".s-rf-measure-dropdown-body button .gd-rf-measure-title");
    private static final By BY_NO_OPTION_BUBBLE = cssSelector(".bubble-content div.content");
    private final String IS_CLOSED = "icon-navigatedown";

    public static RankingFilterPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(RankingFilterPanel.class, waitForElementVisible(LOCATOR, context));
    }

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
    }

    public RankingFilterPanel inputOperator(String valueComparison) {
        waitForElementVisible(inputValue).clear();
        inputValue.sendKeys(valueComparison);
        return this;
    }

    public RankingFilterPanel selectOperator(String title) {
        if (waitForElementVisible(operatorDropdownButton).getAttribute("class").contains(IS_CLOSED)) {
            operatorDropdownButton.click();
        }
        getOperatorsSelect(title).click();
        return this;
    }

    public RankingFilterPanel outOf(String title) {
        if (waitForElementVisible(attributeDropdownButton).getAttribute("class").contains(IS_CLOSED)) {
            attributeDropdownButton.click();
        }
        getAttributesSelect(title).click();
        return this;
    }

    public RankingFilterPanel basedOn(String title) {
        if (waitForElementVisible(measureDropdownButton).getAttribute("class").contains(IS_CLOSED)) {
            measureDropdownButton.click();
        }
        getMeasuresSelect(title).click();
        return this;
    }

    public String getMessageNoOptionsBubble() {
        if (waitForElementVisible(attributeDropdownButton).getAttribute("class").contains(IS_CLOSED)) {
            getActions().moveToElement(attributeDropdownButton).perform();
            attributeDropdownButton.click();
        }
        return waitForElementVisible(BY_NO_OPTION_BUBBLE, browser).getText();
    }

    public String getPreview() {
        return waitForElementVisible(preview).getText();
    }

    public void apply() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private WebElement getOperatorsSelect(String title) {
        List<WebElement> list = waitForCollectionIsNotEmpty(browser.findElements(BY_OPERATORS_SELECT));
        return list.stream()
                .filter(element -> element.getText().contains(title))
                .findFirst()
                .get();
    }

    private WebElement getMeasuresSelect(String title) {
        List<WebElement> list = waitForCollectionIsNotEmpty(browser.findElements(BY_MEASURES_SELECT));
        return list.stream()
                .filter(element -> element.getText().contains(title))
                .findFirst()
                .get();
    }

    private WebElement getAttributesSelect(String title) {
        List<WebElement> list = waitForCollectionIsNotEmpty(browser.findElements(BY_ATTRIBUTES_SELECT));
        return list.stream()
                .filter(element -> element.getText().contains(title))
                .findFirst()
                .get();
    }
}
