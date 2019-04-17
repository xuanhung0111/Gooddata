package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.testng.Assert.assertFalse;

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

    public enum OptionalStacking {
        //Because Input's css (".s-stack-to-percent") has opacity = 0 so it isn't visible and unable to click.
        MEASURES(".s-stack-measures", ".s-stack-measures + span.input-label-text"),
        PERCENT(".s-stack-to-percent", ".s-stack-to-percent + span.input-label-text");

        private String option;
        private String optionLabel;

        OptionalStacking(String option, String optionLabel) {
            this.option = option;
            this.optionLabel = optionLabel;
        }

        @Override
        public String toString() {
            return option;
        }

        public String getOptionLabel() { return optionLabel; }
    }

    public Boolean isOptionCheck(OptionalStacking optional) {
        return waitForElementPresent(By.cssSelector(optional.toString()), getRoot()).isSelected();
    }

    private boolean isConfigurationPanelCollapsed() {
        return getRoot().getAttribute("class").contains("bucket-collapsed");
    }
}
