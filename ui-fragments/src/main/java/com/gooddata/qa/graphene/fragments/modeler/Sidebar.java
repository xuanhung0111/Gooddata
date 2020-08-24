package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

public class Sidebar extends AbstractFragment {
    private static final By SIDEBAR = By.className("gdc-ldm-sidebar");

    @FindBy(className = "gdc-ldm-add-section")
    private AddSection addSection;

    @FindBy(className = "s-import_datasets")
    private WebElement importDatasetButton;

    @FindBy(className = "s-import_from_csv")
    private WebElement importCSVButton;

    public static final Sidebar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Sidebar.class, waitForElementVisible(SIDEBAR, searchContext));
    }

    public AddSection getAddSection() {
        return addSection;
    }

    public FileUploadDialog openCSVDialog() {
        importDatasetButton.click();
        ImportMenu importMenu = OverlayWrapper.getInstance(browser).getImportMenu();
        FileUploadDialog dialog  = importMenu.selectImportCSVFile();
        return dialog;
    }
}
