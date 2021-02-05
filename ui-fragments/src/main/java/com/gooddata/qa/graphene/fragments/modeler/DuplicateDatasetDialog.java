package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class DuplicateDatasetDialog extends AbstractFragment {

    @FindBy(className = "gd-dialog-content")
    private WebElement dialogContent;

    @FindBy(className = "gd-dialog-close")
    private WebElement dialogClose;

    @FindBy(className = "s-close")
    private WebElement closeBtn;

    public static DuplicateDatasetDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DuplicateDatasetDialog.class, waitForElementVisible(className("duplicated-dataset-dialog"), searchContext));
    }

    public String getDialogContent() {
        return waitForElementVisible(dialogContent).getText();
    }

    public void clickXButton() {
        waitForElementVisible(dialogClose).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCloseDialog() {
        waitForElementVisible(closeBtn).click();
        waitForFragmentNotVisible(this);
    }
}
