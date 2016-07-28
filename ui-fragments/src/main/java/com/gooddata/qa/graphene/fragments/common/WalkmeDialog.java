package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class WalkmeDialog extends AbstractFragment {

    private static final By LOCATOR = By.className("walkme-custom-balloon-inner-div");

    private static final By BY_NEXT_BUTTON = By.className("walkme-action-next");
    private static final By BY_BACK_BUTTON = By.className("walkme-action-back");
    private static final By BY_DONE_BUTTON = By.className("walkme-action-done");

    @FindBy(className = "walkme-custom-balloon-title")
    private WebElement title;

    @FindBy(className = "walkme-custom-balloon-content")
    private WebElement content;

    public static WalkmeDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(WalkmeDialog.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public String getContent() {
        return waitForElementVisible(content).getText();
    }

    public WalkmeDialog goNextStep() {
        return goToStep(BY_NEXT_BUTTON);
    }

    public WalkmeDialog goPreviousStep() {
        return goToStep(BY_BACK_BUTTON);
    }

    public void finish() {
        waitForElementVisible(BY_DONE_BUTTON, getRoot()).click();
        waitForFragmentNotVisible(this);
    }

    public boolean canFinish() {
        return isElementVisible(BY_DONE_BUTTON, getRoot());
    }

    public static boolean isPresent(final SearchContext context) {
        return isElementPresent(LOCATOR, context);
    }

    private WalkmeDialog goToStep(By locator) {
        waitForElementVisible(locator, getRoot()).click();
        return waitForFragmentVisible(this);
    }
}
