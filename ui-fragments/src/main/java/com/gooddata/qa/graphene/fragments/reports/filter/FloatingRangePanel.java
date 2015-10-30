package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.filter.FloatingTime;
import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.CheckUtils;

public class FloatingRangePanel extends AbstractFragment {

    @FindBy(xpath = "//*[@class='s-current current']/ancestor::label")
    private WebElement timeCurrentLabel;

    @FindBy(xpath = "//*[@class='s-previous previous']/ancestor::label")
    private WebElement timePreviousLabel;

    @FindBy(xpath = "//*[@class='s-next next']/ancestor::label")
    private WebElement timeNextLabel;

    @FindBy(css = ".s-range")
    private WebElement rangeRadioButton;

    public void selectRange(FloatingTime from, FloatingTime to) {
        waitForElementVisible(rangeRadioButton).click();

        selectRange(Range.FROM, from);
        selectRange(Range.TO, to);
    }

    public void selectTime(final Time time) {
        Stream.of(timeCurrentLabel, timePreviousLabel, timeNextLabel)
                .map(CheckUtils::waitForElementVisible)
                .filter(e -> time.toString().equals(e.findElement(By.tagName("span")).getText()))
                .map(e -> e.findElement(By.tagName("input")))
                .findFirst()
                .get()
                .click();
    }

    private void selectRange(Range range, FloatingTime time) {
        Select timeSelect = range.getSelect(browser);
        timeSelect.selectByVisibleText(time.getTime().toString());

        int timeRange = time.getRangeNumber();
        if (timeRange == 0) {
            return;
        }

        WebElement rangeInput = range.getInput(browser);
        rangeInput.clear();
        rangeInput.sendKeys(String.valueOf(timeRange));
    }

    private enum Range {
        FROM(".start select", ".start input"),
        TO(".end select", ".end input");

        private String selectLocator;
        private String inputLocator;

        private Range(String selectLocator, String inputLocator) {
            this.selectLocator = selectLocator;
            this.inputLocator = inputLocator;
        }

        public Select getSelect(SearchContext browser) {
            return new Select(waitForElementVisible(By.cssSelector(selectLocator), browser));
        }

        public WebElement getInput(SearchContext browser) {
            return waitForElementVisible(By.cssSelector(inputLocator), browser);
        }
    }
}
