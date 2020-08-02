package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;

public class OverlayWrapper extends AbstractFragment {
    private static final String OVERLAY_WRAPPER = "overlay-wrapper";

    @FindBy(className = "dataset-change-primary-key-dialog")
    private ChangePrimaryKeyDialog changePrimaryKeyDialog;

    @FindBy(className = "edit-dataset-dialog")
    private EditDatasetDialog editDatasetDialog;

    @FindBy(className = "edit-date-dimension-dialog")
    private EditDateDimensionDialog editDateDimensionDialog;

    @FindBy(className = "gd-confirm")
    private ConfirmDeleteDatasetDialog confirmDeleteDatasetDialog;

    @FindBy(className = "import-csv")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "preview-csv-dialog")
    private PreviewCSVDialog previewCSVDialog;

    @FindBy(css = ".gdc-ldm-waiting-dialog .waiting-dialog-content")
    private WaitingDialog waitingDialog;

    @FindBy(className = "gd-list")
    private ImportMenu importMenu;

    @FindBy(className = "create-output-stage-dialog")
    private OutputStage outputStage;

    @FindBy(className = "actions-menu-generate-output-stage-item")
    private WebElement dropDownOutputStage;

    public static OverlayWrapper getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                OverlayWrapper.class, waitForElementVisible(className(OVERLAY_WRAPPER), searchContext));
    }

    public ImportMenu getImportMenu() {
        waitForFragmentVisible(importMenu);
        return importMenu;
    }

    public WaitingDialog getWaitingDialog() {
        waitForFragmentVisible(waitingDialog);
        return waitingDialog;
    }

    public PreviewCSVDialog getPreviewCSVDialog() {
        waitForFragmentVisible(previewCSVDialog);
        return previewCSVDialog;
    }

    public FileUploadDialog getFileUploadDialog() {
        waitForFragmentVisible(fileUploadDialog);
        return fileUploadDialog;
    }

    public ChangePrimaryKeyDialog getChangePrimaryKeyDialog() {
        waitForElementVisible(changePrimaryKeyDialog.getRoot());
        return changePrimaryKeyDialog;
    }

    public EditDatasetDialog getEditDatasetDialog() {
        waitForElementVisible(editDatasetDialog.getRoot());
        return editDatasetDialog;
    }

    public EditDateDimensionDialog getDateDimensionDialog() {
        waitForElementVisible(editDateDimensionDialog.getRoot());
        return editDateDimensionDialog;
    }

    public ConfirmDeleteDatasetDialog getConfirmDeleteDatasetDialog() {
        waitForElementVisible(confirmDeleteDatasetDialog.getRoot());
        return confirmDeleteDatasetDialog;
    }

    public OutputStage openOutputStage() {
        waitForElementVisible(dropDownOutputStage).click();
        return outputStage;
    }

    public void selectOption(String option) {
        waitForElementVisible(outputStage.getRoot());
        outputStage.selectProperty();
        this.getRoot().findElement(By.xpath("//div[@class='gd-list-item "+ option +"']")).click();
    }

    public enum PROPERTIES_OPTION {
        CREATE_VIEW("s-create_views"),
        CREATE_TABLE("s-create_tables"),
        ALTER_TABLE("s-alter_tables");

        private final String name;

        private PROPERTIES_OPTION(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
