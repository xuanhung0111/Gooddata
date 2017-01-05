package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ScheduleParameter extends AbstractFragment {

    @FindBy(css = ".param-name input")
    private WebElement name;

    @FindBy(css = ".param-value input")
    private WebElement value;

    @FindBy(className = "action-icon")
    private WebElement deleteButton;

    public ScheduleParameter editNameValuePair(String name, String value) {
        setName(name).setValue(value);
        return this;
    }

    public Pair<String, String> getNameValuePair() {
        return Pair.of(getName(), getValue());
    }

    public String getName() {
        return waitForElementVisible(name).getAttribute("value");
    }

    public String getValue() {
        return waitForElementVisible(value).getAttribute("value");
    }

    public boolean isSecure() {
        return waitForElementVisible(name).getAttribute("placeholder").equals("Secure parameter name");
    }

    public void delete() {
        waitForElementVisible(deleteButton).click();
    }

    private ScheduleParameter setName(String paramName) {
        waitForElementVisible(name).clear();
        name.sendKeys(paramName);
        return this;
    }

    private ScheduleParameter setValue(String paramValue) {
        waitForElementVisible(value).clear();
        value.sendKeys(paramValue);
        return this;
    }
}
