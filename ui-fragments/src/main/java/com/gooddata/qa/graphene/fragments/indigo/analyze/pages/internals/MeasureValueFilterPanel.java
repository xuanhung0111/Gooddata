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

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class MeasureValueFilterPanel extends AbstractFragment {

    @FindBy(className = "s-mvf-comparison-value-input")
    private WebElement inputValue;

    @FindBy(className = "s-mvf-operator-dropdown-button")
    private WebElement operatorDropdownButton;

    @FindBy(className = "s-mvf-warning-message")
    private WebElement warningMessage;

    public static final By LOCATOR = By.className("s-mvf-dropdown-body");
    private final By BY_OPERATORS_SELECT = By.className("s-mvf-operator-dropdown-body");
    private final String IS_OPEN = "is-dropdown-open";

    public static MeasureValueFilterPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(MeasureValueFilterPanel.class, waitForElementVisible(LOCATOR, context));
    }

    public MeasureValueFilterPanel addMeasureValueFilter(LogicalOperator logical, String valueComparison) {
        getOperatorsSelect().selectOperator(logical.toString());
        ElementUtils.clear(waitForElementVisible(inputValue));
        getActions().sendKeys(valueComparison + Keys.ENTER).perform();
        AnalysisPage.getInstance(browser).waitForReportComputing();
        return this;
    }

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
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
        BETWEEN("between"),
        NOT_BETWEEN("not_between"),
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
    }
}
