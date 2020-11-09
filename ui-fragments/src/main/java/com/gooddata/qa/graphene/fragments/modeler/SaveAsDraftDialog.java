package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class SaveAsDraftDialog extends AbstractFragment {
    private static final By SAVE_AS_DRAFT_DIALOG = By.className("timer-detail");

    @FindBy(className = "s-discard_draft")
    private WebElement discardButton;

    public static final SaveAsDraftDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SaveAsDraftDialog.class, waitForElementVisible(SAVE_AS_DRAFT_DIALOG, searchContext));
    }

    public void discardDraft() {
        waitForElementVisible(discardButton);
        discardButton.click();
    }
}
