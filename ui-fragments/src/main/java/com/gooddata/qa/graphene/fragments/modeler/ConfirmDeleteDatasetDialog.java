package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class ConfirmDeleteDatasetDialog extends AbstractFragment {
    private static final String CONFIRM_DELETE_DIALOG = "gd-confirm";

    @FindBy(className = "s-delete")
    WebElement btnDelete;

    public static ConfirmDeleteDatasetDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ConfirmDeleteDatasetDialog.class, waitForElementVisible(className(CONFIRM_DELETE_DIALOG), searchContext));
    }

    public void clickDeleteDataset() {
        btnDelete.click();
    }
}
