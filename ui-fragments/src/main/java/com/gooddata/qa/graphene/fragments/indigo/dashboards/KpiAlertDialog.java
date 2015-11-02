package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class KpiAlertDialog extends AbstractFragment {

    private static final String MAIN_CLASS = "kpi-alert-dialog";
    private static final String ALERT_DIALOG_MESSAGE_CLASS = "gd-message";

    private static final By ALERT_DIALOG_MESSAGE = By.cssSelector("." + MAIN_CLASS + " ." + ALERT_DIALOG_MESSAGE_CLASS);
    public static final By LOCATOR = By.className(MAIN_CLASS);

    public static final String TRIGGERED_WHEN_GOES_ABOVE = "above";
    public static final String TRIGGERED_WHEN_DROPS_BELOW = "below";

    @FindBy(className = "kpi-alert-dialog-text")
    protected WebElement header;

    @FindBy(className = "s-alert_select")
    private KpiAlertTriggeredWhenSelect triggeredWhenSelect;

    @FindBy(tagName = "input")
    private WebElement kpiAlertThresholdInput;

    @FindBy(className = "text-info")
    private WebElement kpiAlertDialogTextInfo;

    @FindBy(className = ALERT_DIALOG_MESSAGE_CLASS)
    private WebElement kpiAlertDialogMessage;

    @FindBy(className = "s-save_button")
    private WebElement setAlertButton;

    @FindBy(className = "s-delete_button")
    private WebElement deleteAlertButton;

    @FindBy(className = "s-apply-alert-filters")
    private WebElement applyAlertFiltersButton;

    @FindBy(className = "s-cancel")
    private WebElement discardAlertButton;

    public String getDialogHeader() {
        return waitForElementVisible(header).getText();
    }

    public KpiAlertDialog selectTriggeredWhen(String triggeredWhen) {
        waitForFragmentVisible(triggeredWhenSelect)
            .selectByName(triggeredWhen);

        return this;
    }

    public String getTriggeredWhen() {
        return waitForFragmentVisible(triggeredWhenSelect).getSelection();
    }

    public String getThreshold() {
        return waitForElementVisible(kpiAlertThresholdInput).getAttribute("value");
    }

    public KpiAlertDialog setThreshold(String threshold) {
        waitForElementVisible(kpiAlertThresholdInput);
        kpiAlertThresholdInput.clear();
        kpiAlertThresholdInput.sendKeys(threshold);

        return this;
    }

    public KpiAlertDialog setAlert() {
        waitForElementVisible(setAlertButton).click();
        waitForElementNotPresent(setAlertButton);
        return this;
    }

    public KpiAlertDialog deleteAlert() {
        waitForElementVisible(deleteAlertButton).click();
        waitForElementNotPresent(deleteAlertButton);
        return this;
    }

    public KpiAlertDialog discardAlert() {
        waitForElementVisible(discardAlertButton).click();
        waitForElementNotPresent(discardAlertButton);
        return this;
    }

    public String getAlertDialogText() {
        return waitForElementPresent(kpiAlertDialogTextInfo).getText();
    }

    public boolean hasAlertMessage() {
        return isElementPresent(ALERT_DIALOG_MESSAGE, browser);
    }

    public String getAlertMessageText() {
        return waitForElementVisible(kpiAlertDialogMessage).getText();
    }

    public KpiAlertDialog applyAlertFilters() {
        waitForElementVisible(applyAlertFiltersButton).click();
        return this;
    }
}
