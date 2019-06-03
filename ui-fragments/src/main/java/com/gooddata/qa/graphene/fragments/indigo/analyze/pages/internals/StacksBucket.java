package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class StacksBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-invitation")
    private WebElement bucketInvitation;

    private static final String BUCKET_WITH_WARN_MESSAGE = "bucket-with-warn-message";

    public static final String CSS_SELECTOR = ".s-bucket-stack, .s-bucket-segment";

    public boolean isDisabled() {
        return getRoot().getAttribute("class").contains(BUCKET_WITH_WARN_MESSAGE);
    }

    public String getAttributeName() {
        if (isEmpty()) {
            return "";
        }
        return waitForElementVisible(BY_HEADER, get()).getText().trim();
    }

    public WebElement get() {
        return waitForElementVisible(items.get(0));
    }

    public StacksBucket expandStackConfigurationPanel() {
        if (isConfigurationPanelCollapsed()) {
            getRoot().click();
            waitForElementAttributeNotContainValue(getRoot(), "class", "bucket-collapsed");
        }
        assertFalse(isConfigurationPanelCollapsed(), "Stack Configuration Panel Bucket should be expanded");
        return this;
    }

    public Boolean isOptionCheckPresent(OptionalStacking optional) {
        return isElementPresent(By.cssSelector(optional.toString()), browser);
    }

    public StacksBucket checkOption(OptionalStacking optional) {
        if (!isOptionCheck(optional)) {
            waitForElementVisible(By.cssSelector(optional.getOptionLabel()), getRoot()).click();
        }
        return this;
    }

    public StacksBucket uncheckOption(OptionalStacking optional) {
        if (isOptionCheck(optional)) {
            waitForElementVisible(By.cssSelector(optional.getOptionLabel()), getRoot()).click();
        }
        return this;
    }

    public Boolean isOptionCheck(OptionalStacking optional) {
        return waitForElementPresent(By.cssSelector(optional.toString()), getRoot()).isSelected();
    }

    private boolean isConfigurationPanelCollapsed() {
        return getRoot().getAttribute("class").contains("bucket-collapsed");
    }
}
