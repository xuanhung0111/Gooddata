package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class OverlayWrapper extends AbstractFragment {
    private static final String OVERLAY_WRAPPER = "overlay-wrapper";

    @FindBy(className = "dataset-change-primary-key-dialog")
    private ChangePrimaryKeyDialog changePrimaryKeyDialog;

    @FindBy(className = "edit-dataset-dialog")
    private EditDatasetDialog editDatasetDialog;

    @FindBy(className = "gd-confirm")
    private ConfirmDeleteDatasetDialog confirmDeleteDatasetDialog;

    public static OverlayWrapper getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                OverlayWrapper.class, waitForElementVisible(className(OVERLAY_WRAPPER), searchContext));
    }

    public ChangePrimaryKeyDialog getChangePrimaryKeyDialog() {
        waitForElementVisible(changePrimaryKeyDialog.getRoot());
        return changePrimaryKeyDialog;
    }

    public EditDatasetDialog getEditDatasetDialog() {
        waitForElementVisible(editDatasetDialog.getRoot());
        return editDatasetDialog;
    }

    public ConfirmDeleteDatasetDialog getConfirmDeleteDatasetDialog() {
        waitForElementVisible(confirmDeleteDatasetDialog.getRoot());
        return confirmDeleteDatasetDialog;
    }
}
