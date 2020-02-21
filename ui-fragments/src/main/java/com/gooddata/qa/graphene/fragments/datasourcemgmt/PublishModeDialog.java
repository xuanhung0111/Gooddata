package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

public class PublishModeDialog extends AbstractFragment {
    @FindBy(className = "s-dialog-submit-button")
    private List<WebElement> modeButton;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement cancelButton;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement publishButton;

    @FindBy(css = ".gd-dialog-content span")
    private WebElement message;

    public static PublishModeDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(PublishModeDialog.class,
                waitForElementVisible(className("s-dataset-delete-dialog"), context));
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickPublish () {
        waitForElementVisible(publishButton).click();
        waitForFragmentNotVisible(this);
    }

    public PublishModeDialog selectMode(int index) {
        waitForCollectionIsNotEmpty(modeButton);
        modeButton.get(index).click();
        return this;
    }

    public String getMessage() {
        return waitForElementVisible(message).getText();
    }
}
