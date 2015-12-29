package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class StacksBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-invitation")
    private WebElement bucketInvitation;

    private static final String BUCKET_WITH_WARN_MESSAGE = "bucket-with-warn-message";

    public static final String CSS_CLASS = "s-bucket-stacks";

    public boolean isStackByDisabled() {
        return getRoot().getAttribute("class").contains(BUCKET_WITH_WARN_MESSAGE);
    }

    public String getAddedStackByName() {
        if (isEmpty()) {
            return "";
        }
        return waitForElementVisible(BY_HEADER, get()).getText().trim();
    }

    public WebElement get() {
        return waitForElementVisible(items.get(0));
    }
}
