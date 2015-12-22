package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractBucket extends AbstractFragment {

    private static final String EMPTY = "s-bucket-empty";

    private static final By BY_BUCKET_INVITATION = cssSelector(".adi-bucket-invitation");
    private static final By BY_STACK_WARNING = className("adi-stack-warn");
    protected static final By BY_HEADER = className("adi-bucket-item-header");

    @FindBy(className = "adi-bucket-item")
    protected List<WebElement> items;

    public WebElement getInvitation() {
        return waitForElementVisible(BY_BUCKET_INVITATION, getRoot());
    }

    public boolean isEmpty() {
        return getRoot().getAttribute("class").contains(EMPTY);
    }

    public String getWarningMessage() {
        return waitForElementVisible(BY_STACK_WARNING, getRoot()).getText().trim();
    }
}
