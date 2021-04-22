package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class ImportBigQueryDialog extends AbstractFragment {

    public static final String IMPORT_DIALOG = "import-service-account-file";

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeDialog;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "gd-button-link-dimmed")
    private WebElement connectManualLink;

    public static final ImportBigQueryDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ImportBigQueryDialog.class, waitForElementVisible(className(IMPORT_DIALOG), searchContext));
    }

    public void closeImportDialog() {
        waitForElementVisible(closeDialog).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCancelImportDialog() {
        waitForElementVisible(closeDialog).click();
        waitForFragmentNotVisible(this);
    }

    public void clickConnectManualLink() {
        waitForElementVisible(connectManualLink).click();
        waitForFragmentNotVisible(this);
    }
}
