package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class StacksBucket extends AbstractFragment {

    @FindBy(className = "adi-bucket-item")
    private WebElement item;

    @FindBy(className = "adi-bucket-invitation")
    private WebElement bucketInvitation;

    private static final String BUCKET_WITH_WARN_MESSAGE = "bucket-with-warn-message";
    private static final String EMPTY = "s-bucket-empty";
    private static final By BY_HEADER = By.className("adi-bucket-item-header");
    private static final By BY_STACK_WARNING = By.className("adi-stack-warn");

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public void addCategory(WebElement category) {
        new Actions(browser).dragAndDrop(category, waitForElementVisible(getRoot())).perform();
    }

    public boolean isWarningMessageShown() {
        return false;
    }

    public void replaceStackBy(WebElement category) {
        addCategory(category);
    }

    public boolean isStackByDisabled() {
        return getRoot().getAttribute("class").contains(BUCKET_WITH_WARN_MESSAGE);
    }

    public String getStackByMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }

    public String getAddedStackByName() {
        if (isEmpty()) {
            return "";
        }
        return waitForElementVisible(BY_HEADER, waitForElementVisible(item)).getText().trim();
    }
}
