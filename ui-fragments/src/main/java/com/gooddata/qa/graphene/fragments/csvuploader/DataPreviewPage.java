package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class DataPreviewPage extends AbstractFragment {

    @FindBy(css = ".s-data-type-picker:last-of-type")
    private WebElement factColumnPicker;

    @FindBy(className = "s-integration-button")
    private WebElement triggerIntegrationButton;

    public DataPreviewPage selectFact() {
        waitForElementVisible(factColumnPicker);
        final Select select = new Select(factColumnPicker.findElement(By.tagName("select")));
        select.selectByValue("FACT");
        return this;
    }

    public void triggerIntegration() {
        waitForElementVisible(triggerIntegrationButton).click();
    }
}
