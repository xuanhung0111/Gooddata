package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static org.openqa.selenium.By.cssSelector;

public class SaveAsDraftDialog extends AbstractFragment {
    private static final By SAVE_AS_DRAFT_DIALOG = By.className("timer-detail");

    @FindBy(className = "s-discard_draft")
    private WebElement discardButton;

    @FindBy(className = "content")
    private WebElement contentDialog;

    @FindBy(className = "draft-history")
    private WebElement draftHistory;

    public static final SaveAsDraftDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveAsDraftDialog.class, waitForElementVisible(SAVE_AS_DRAFT_DIALOG, searchContext));
    }

    public void discardDraft() {
        waitForElementVisible(discardButton);
        discardButton.click();
        waitForElementNotVisible(cssSelector(".gd-spinner.large"), browser);
    }

    public String getContentDialog() {
        return waitForElementVisible(contentDialog).getText();
    }

    public boolean isDialogVisible() {
        return isElementVisible(SAVE_AS_DRAFT_DIALOG, browser);
    }

    public String getDraftHistory() {
        return waitForElementVisible(draftHistory).getText().split("Draft last saved:")[1];
    }
}
