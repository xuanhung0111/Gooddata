package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class DeleteDependencyDialog extends AbstractFragment {
    private static final String DIALOG = "delete-dependencies-confirm-dialog";

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeBtn;

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelBtn;

    @FindBy(className = "s-publish_anyway")
    private WebElement publishAnywayBtn;

    public static DeleteDependencyDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DeleteDependencyDialog.class, waitForElementVisible(className(DIALOG), searchContext));
    }

    public void clickPublishAnyway() {
       waitForElementPresent(publishAnywayBtn).click();
       waitForFragmentNotVisible(this);
    }

    public void clickCancelButton() {
        waitForElementPresent(cancelBtn).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCloseButton() {
        waitForElementPresent(closeBtn).click();
        waitForFragmentNotVisible(this);
    }
}
