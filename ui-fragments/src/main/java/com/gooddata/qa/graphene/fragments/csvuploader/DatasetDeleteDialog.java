package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

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
    
    @FindBy(css = ".gd-dialog-content span")
    private WebElement message;

    public static DatasetDeleteDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DatasetDeleteDialog.class,
                waitForElementVisible(className("s-dataset-delete-dialog"), context));
    }

    public void clickDelete() {
        waitForElementVisible(deleteButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public String getMessage() {
        return waitForElementVisible(message).getText();
    }
}
