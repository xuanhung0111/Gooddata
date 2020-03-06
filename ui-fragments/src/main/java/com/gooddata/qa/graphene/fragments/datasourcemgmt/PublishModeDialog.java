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
    private static final String PUBLISH_MODE_CLASS = "s-select-publish-mode-dialog";
    @FindBy(className = "input-radio-label")
    private List<WebElement> modeButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-publish-button")
    private WebElement publishButton;

    @FindBy(css = ".s-overwrite-warning .gd-message-text")
    private WebElement warningMessage;

    public static PublishModeDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(PublishModeDialog.class,
                waitForElementVisible(className(PUBLISH_MODE_CLASS), context));
    }

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickPublish() {
        waitForElementVisible(publishButton).click();
        waitForFragmentNotVisible(this);
    }

    public PublishModeDialog selectMode(String mode) {
        waitForCollectionIsNotEmpty(modeButton).stream().filter(e -> e.getText().contains(mode)).findFirst().get().click();
        return this;
    }

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
    }

    public enum PublishMode {
        PRESERVE_DATA("Preserve data"),
        OVERWRITE("Overwrite");

        private String type;

        PublishMode(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
