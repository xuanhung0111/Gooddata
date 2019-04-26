package com.gooddata.qa.graphene.fragments.indigo.analyze.dialog;

import com.gooddata.qa.graphene.fragments.common.AbstractDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ExportXLSXDialog extends AbstractDialog {

    private static final By ROOT_LOCATOR = By.className("gd-export-dialog");

    @FindBy(className = "s-dialog-submit-button")
    private WebElement exportButton;

    public static final ExportXLSXDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ExportXLSXDialog.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public void confirmExport() {
        waitForElementVisible(exportButton).click();
    }
}
