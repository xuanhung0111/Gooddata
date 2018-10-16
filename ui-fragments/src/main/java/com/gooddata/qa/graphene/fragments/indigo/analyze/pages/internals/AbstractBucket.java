package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import java.util.List;

import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractBucket extends AbstractFragment {

    private static final String EMPTY = "s-bucket-empty";

    private static final By BY_BUCKET_INVITATION = className("s-bucket-dropzone");
    private static final By BY_STACK_WARNING = className("adi-stack-warn");
    protected static final By BY_HEADER = className("adi-bucket-item-header");

    @FindBy(className = "adi-bucket-item")
    protected List<WebElement> items;

    public WebElement getInvitation() {
        return waitForElementPresent(BY_BUCKET_INVITATION, getRoot());
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public String getWarningMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }

    public <T extends AbstractBucket> T setTitleItemBucket(String currentTitle, String newTitle) {
        WebElement itemWebElement = getItemBucket(currentTitle);
        ElementUtils.scrollElementIntoView(itemWebElement, browser);
        ElementUtils.clear(itemWebElement);
        if (!newTitle.isEmpty()) {
            getActions().sendKeys(newTitle + Keys.ENTER).perform();
        }
        return (T) this;
    }

    private WebElement getItemBucket(final String item) {
        return waitForCollectionIsNotEmpty(items)
                .stream()
                .filter(e -> item.equals(e.getText()))
                .findFirst()
                .get();
    }
}
