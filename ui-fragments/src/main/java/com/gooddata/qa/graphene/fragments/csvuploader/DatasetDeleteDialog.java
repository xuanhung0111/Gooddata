package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Fragment representing dialog shown after user clicks on the delete button in the dataset list page.
 * This dialog should contain 2 buttons:
 * <ul>
 *     <li><b>Delete</b> - to continue deleting dataset</li>
 *     <li><b>Cancel</b> - to cancel dataset deletion</li>
 * </ul>
 */
public class DatasetDeleteDialog extends AbstractFragment {

    @FindBy(className = "s-dialog-submit-button")
    private WebElement deleteButton;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    public void clickDelete() {
        waitForElementVisible(deleteButton).click();
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
    }
}
