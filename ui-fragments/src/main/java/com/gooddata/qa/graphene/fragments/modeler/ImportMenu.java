package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ImportMenu extends AbstractFragment {
    private static final By MENU_CLASS = By.className("gd-list");

    @FindBy(className = "s-import_from_csv")
    WebElement importCSVButton;

    public static final ImportMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ImportMenu.class, waitForElementVisible(MENU_CLASS, searchContext));
    }

    public FileUploadDialog selectImportCSVFile() {
        waitForElementVisible(importCSVButton).click();
        return OverlayWrapper.getInstance(browser).getFileUploadDialog();
    }
}
