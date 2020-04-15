package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

public class AddMorePopUp extends AbstractFragment {
    private static final String ADD_MORE_POPUP = ".joint-popup > .add-more-popup";

    @FindBy(className = "btn-set-primary-key")
    private WebElement btnSetPrimaryKey;

    @FindBy(className = "btn-edit")
    private WebElement btnEdit;

    @FindBy(className = "btn-delete")
    private WebElement btnDelete;

    public static AddMorePopUp getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                AddMorePopUp.class, waitForElementVisible(cssSelector(ADD_MORE_POPUP), searchContext));
    }

    public void setPrimaryKey(String datasetName, String attributeName) {
        btnSetPrimaryKey.click();
        OverlayWrapper.getInstance(browser).getChangePrimaryKeyDialog().setPrimaryKey(datasetName, attributeName);
    }

    public void deleteDataset() {
        btnDelete.click();
        OverlayWrapper.getInstance(browser).getConfirmDeleteDatasetDialog().clickDeleteDataset();
    }

    public EditDatasetDialog viewDetail() {
        return OverlayWrapper.getInstance(browser).getEditDatasetDialog();
    }

    public EditDatasetDialog editDatasetDialog() {
        btnEdit.click();
        return OverlayWrapper.getInstance(browser).getEditDatasetDialog();
    }
}
