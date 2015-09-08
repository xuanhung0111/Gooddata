package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class KpiAlertDialog extends AbstractFragment {

    public static final By LOCATOR = By.className("kpi-alert-dialog");

    public static final String TRIGGERED_WHEN_GOES_ABOVE = "goes above";
    public static final String TRIGGERED_WHEN_DROPS_BELOW = "drops below";

    @FindBy(className = "kpi-alert-dialog-text")
    protected WebElement header;

    @FindBy(className = "s-alert_select")
    private KpiAlertTriggeredWhenSelect triggeredWhenSelect;

    @FindBy(tagName = "input")
    private WebElement kpiAlertThresholdInput;

    @FindBy(className = "s-save_button")
    private WebElement setAlertButton;

    @FindBy(className = "s-delete_button")
    private WebElement deleteAlertButton;

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
}
