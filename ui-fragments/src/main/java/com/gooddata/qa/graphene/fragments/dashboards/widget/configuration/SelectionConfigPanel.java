package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SelectionConfigPanel extends AbstractFragment {
    @FindBy(xpath = "//div[./label[.='One value']]/input")
    private WebElement oneValue;

    @FindBy(xpath = "//div[./label[.='Multiple values']]/input")
    private WebElement multipleValue;

    public void changeSelectionToOneValue() {
        waitForElementVisible(oneValue).click();
    }

    public void changeSelectionToMultipleValues() {
        waitForElementVisible(multipleValue).click();
    }
}
