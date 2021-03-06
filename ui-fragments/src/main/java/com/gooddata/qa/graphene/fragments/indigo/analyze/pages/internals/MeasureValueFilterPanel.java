package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class MeasureValueFilterPanel extends AbstractFragment {

    @FindBy(className = "s-mvf-comparison-value-input")
    private WebElement inputValue;

    @FindBy(className = "s-mvf-operator-dropdown-button")
    private WebElement operatorDropdownButton;

    @FindBy(className = "s-mvf-warning-message")
    private WebElement warningMessage;

    @FindBy(className = "s-mvf-range-from-input")
    private WebElement inputRangeFrom;

    @FindBy(className = "s-mvf-range-to-input")
    private WebElement inputRangeTo;

    @FindBy(css = TREAT_NULL_VALUES_AS_ZERO_CHECKBOX)
    private WebElement treatNullValuesAsZeroCheckbox;

    public static final By LOCATOR = By.className("s-mvf-dropdown-body");

    private final By BY_OPERATORS_SELECT = By.className("s-mvf-operator-dropdown-body");
    private final String IS_OPEN = "is-dropdown-open";
    private final String TREAT_NULL_VALUES_AS_ZERO_CHECKBOX = ".s-treat-null-values-as-zero .input-checkbox";

    public static MeasureValueFilterPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(MeasureValueFilterPanel.class, waitForElementVisible(LOCATOR, context));
    }

    public MeasureValueFilterPanel addMeasureValueFilter(LogicalOperator logical, String valueComparison) {
        selectLogicalOperator(logical);
        ElementUtils.clear(waitForElementVisible(inputValue));
        getActions().sendKeys(valueComparison + Keys.ENTER).perform();
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public MeasureValueFilterPanel addMeasureValueFilter(LogicalOperator.Range logical, Pair<Integer, Integer> valueComparison) {
        getOperatorsSelect().selectOperator(logical.toString());
        ElementUtils.clear(waitForElementVisible(inputRangeFrom));
        getActions().sendKeys(valueComparison.getKey().toString() + Keys.ENTER).perform();

        ElementUtils.clear(waitForElementVisible(inputRangeTo));
        getActions().sendKeys(valueComparison.getValue().toString() + Keys.ENTER).perform();

        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public MeasureValueFilterPanel selectLogicalOperator(LogicalOperator logical) {
        getOperatorsSelect().selectOperator(logical.toString());
        return this;
    }

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
    }

    public Boolean isTreatNullValuesCheckboxPresent() {
        return isElementPresent(By.cssSelector(TREAT_NULL_VALUES_AS_ZERO_CHECKBOX), getRoot());
    }

    public String getTreatNullValuesAsZero() {
        return getRoot().findElement(By.className("input-label-text")).getText();
    }

    public boolean isTreatNullValuesCheckboxChecked() {
        return waitForElementPresent(treatNullValuesAsZeroCheckbox).isSelected();
    }

    private OperatorsSelect getOperatorsSelect() {
        if (!waitForElementVisible(operatorDropdownButton).getAttribute("class").contains(IS_OPEN)) {
            operatorDropdownButton.click();
        }
        return Graphene.createPageFragment(OperatorsSelect.class,
            waitForElementVisible(BY_OPERATORS_SELECT, browser));
    }

    private class OperatorsSelect extends AbstractFragment{
        public void selectOperator(String logicalOperator) {
            By selector = By.cssSelector(".s-" + logicalOperator);
            waitForElementVisible(selector, getRoot()).click();
        }
    }

    public enum LogicalOperator {
        ALL("all"),
        GREATER_THAN("greater_than"),
        GREATER_THAN_OR_EQUAL_TO("greater_than_or_equal_to"),
        LESS_THAN("less_than"),
        LESS_THAN_OR_EQUAL_TO("less_than_or_equal_to"),
        EQUAL_TO("equal_to"),
        NOT_EQUAL_TO("not_equal_to");

        private String operator;

        LogicalOperator(String time) {
            this.operator = time;
        }

        @Override
        public String toString() {
            return "mvf-operator-" + operator;
        }

        public enum Range {
            BETWEEN("between"),
            NOT_BETWEEN("not_between");

            private String operator;

            Range(String time) {
                this.operator = time;
            }

            @Override
            public String toString() {
                return "mvf-operator-" + operator;
            }
        }
    }
}
