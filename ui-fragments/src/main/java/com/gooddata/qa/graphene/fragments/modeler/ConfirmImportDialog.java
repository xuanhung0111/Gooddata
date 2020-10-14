package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ConfirmImportDialog extends AbstractFragment {
    @FindBy(className = "s-proceed")
    private WebElement proceedButton;

    public static ConfirmImportDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ConfirmImportDialog.class, waitForElementVisible(className("gd-confirm"), searchContext));
    }

    public void proceedImportJson() {
        waitForElementVisible(proceedButton).click();
    }
}
