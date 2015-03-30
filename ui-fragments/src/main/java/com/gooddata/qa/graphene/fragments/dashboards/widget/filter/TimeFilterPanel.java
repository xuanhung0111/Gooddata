package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TimeFilterPanel extends FilterPanel {

    @FindBy(css = "div.fromInput input.input")
    private WebElement filterTimeFromInput;

    @FindBy(css = "div.toInput input.input")
    private WebElement filterTimeToInput;

    @FindBy(css = "div.fromInput label.label")
    private WebElement fromLabel;

    private static final String TIME_LINE_LOCATOR = "//div[text()='${time}']";

    public void changeValueByClickInTimeLine(String dataRange) {
        waitForElementVisible(By.xpath(TIME_LINE_LOCATOR.replace("${time}", dataRange)), browser).click();
        submit();
        waitForElementNotVisible(this.getRoot());
    }

    public void changeValueByEnterFromDateAndToDate(String startTime, String endTime) {
        waitForElementVisible(filterTimeFromInput).clear();
        waitForElementVisible(filterTimeToInput).clear();
        filterTimeFromInput.sendKeys(startTime);
        filterTimeFromInput.click();
        filterTimeToInput.sendKeys(endTime);
        filterTimeToInput.click();
        submit();
    }
}
