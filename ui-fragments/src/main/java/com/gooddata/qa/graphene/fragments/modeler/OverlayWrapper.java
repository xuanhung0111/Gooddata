package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.modeler.datasource.BubleContent;
import com.gooddata.qa.graphene.fragments.modeler.datasource.DropDownDSContent;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static org.openqa.selenium.By.className;

public class OverlayWrapper extends AbstractFragment {
    private static final String OVERLAY_WRAPPER = "overlay-wrapper";

    @FindBy(css = ".overlay .indigo-table-dropdown-body")
    private IndigoTableDropDown indigoTableDropDown;

    @FindBy(css = ".gdc-ldm-waiting-dialog .s-dialog-close-button")
    private WebElement closeWaitingDialog;

    @FindBy(className = "dataset-change-primary-key-dialog")
    private ChangePrimaryKeyDialog changePrimaryKeyDialog;

    @FindBy(className = "edit-dataset-dialog")
    private EditDatasetDialog editDatasetDialog;

    @FindBy(className = "edit-date-dimension-dialog")
    private EditDateDimensionDialog editDateDimensionDialog;

    @FindBy(className = "gd-confirm")
    private ConfirmDeleteDatasetDialog confirmDeleteDatasetDialog;

    @FindBy(className = "gd-confirm")
    private ConfirmImportDialog confirmImportDialog;

    @FindBy(className = "import-csv")
    private FileUploadDialog fileUploadDialog;

    @FindBy(className = "preview-csv-dialog")
    private PreviewCSVDialog previewCSVDialog;

    @FindBy(css = ".gdc-ldm-waiting-dialog .waiting-dialog-content")
    private WaitingDialog waitingDialog;

    @FindBy(className = "create-output-stage-dialog")
    private OutputStage outputStage;

    @FindBy(className = "actions-menu-generate-output-stage-item")
    private WebElement dropDownOutputStage;

    @FindBy(className = "actions-menu-export-model-item")
    private WebElement dropDownExportJson;

    @FindBy(className = "actions-menu-generate-import-model-item")
    private WebElement dropDowImportJson;

    @FindBy(css = ".gd-message .s-message-text-header-value")
    private WebElement successMessage;

    @FindBy(css = ".gd-message .gd-message-dismiss-container .icon-cross")
    private WebElement closeToastMessage;

    @FindBy(className = "add-label-menu")
    private TextEditorWrapper textEditorWrapper;

    @FindBy(className = "timer-detail")
    private SaveAsDraftDialog saveAsDraftDialog;

    @FindBy(className = "s-message-text-header-value")
    private WebElement messageConnectDatasource;

    @FindBy(className = "bubble-content")
    private WebElement popUpTable;

    @FindBy(className = "ldm-control-load-menu")
    private ControlLoadMenu controlMenu;

    @FindBy(className = "dropdown-body")
    private DropDownDSContent dsDropdown;

    @FindBy(className = "icon-navigateup")
    private WebElement navigateUpBtn;

    public static OverlayWrapper getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                OverlayWrapper.class, waitForElementVisible(className(OVERLAY_WRAPPER), searchContext));
    }

    public DropDownDSContent getDSDropdown() {
        waitForFragmentVisible(dsDropdown);
        return dsDropdown;
    }

    public String getTextPublishSuccess() {
        waitForFragmentNotVisible(waitingDialog);
        return waitForElementVisible(successMessage).getText();
    }

    public String getTextDeleteSuccess() {
        return waitForElementVisible(successMessage).getText();
    }

    public String getLinkPublishSuccess() {
        return waitForElementVisible(successMessage).findElement(By.tagName("a")).getAttribute("href");
    }

    public String getMoveLabelSuccess() {
        String message = waitForElementVisible(successMessage).getText();
        waitForFragmentNotVisible(this);
        return message;
    }

    public void closePublishSuccess() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(closeToastMessage).click().build().perform();
        waitForElementNotVisible(closeToastMessage);
    }

    public static OverlayWrapper getInstanceByIndex(SearchContext searchContext, int index) {
        List<WebElement> wrapperList = searchContext.findElements(className(OVERLAY_WRAPPER));
        return Graphene.createPageFragment(
                OverlayWrapper.class, wrapperList.get(index));
    }

    public String getMessageConnectDatasource() {
        return waitForElementVisible(messageConnectDatasource).getText();
    }

    public IndigoTableDropDown getIndigoTableDropDown() {
        waitForFragmentVisible(indigoTableDropDown);
        return indigoTableDropDown;
    }

    public ControlLoadMenu getControlLoadMenu() {
        waitForFragmentVisible(controlMenu);
        return controlMenu;
    }
    public WaitingDialog getWaitingDialog() {
        waitForFragmentVisible(waitingDialog);
        return waitingDialog;
    }

    public void waittingDialog() {
        waitForFragmentVisible(waitingDialog);
        waitingDialog.waitForLoading();
    }

    public PreviewCSVDialog getPreviewCSVDialog() {
        waitForFragmentVisible(previewCSVDialog);
        return previewCSVDialog;
    }

    public TextEditorWrapper getTextEditorWrapper() {
        waitForFragmentVisible(textEditorWrapper);
        return textEditorWrapper;
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

    public ConfirmImportDialog getConfirmImportDialog() {
        waitForElementVisible(confirmImportDialog.getRoot());
        return confirmImportDialog;
    }

    public OutputStage openOutputStage() {
        waitForElementVisible(dropDownOutputStage).click();
        return outputStage;
    }

    public BubleContent getPopUpTable() {
        return BubleContent.getInstance(browser);
    }
    public void discardDraft() {
        waitForFragmentVisible(saveAsDraftDialog).discardDraft();
    }

    public void exportJson() {
        waitForElementVisible(dropDownExportJson).click();
    }

    public void importJson(String jsonFilePath) {
        waitForElementVisible(dropDowImportJson).click();
        getConfirmImportDialog().proceedImportJson();
        Modeler.getInstance(browser).pickJsonFile(jsonFilePath);
        waitForFragmentNotVisible(this);
    }

    public OverlayWrapper closeWaitingDialog() {
        Actions driverActions = new Actions(browser);
        waitForElementVisible(closeWaitingDialog);
        driverActions.moveToElement(closeWaitingDialog).pause(2000).click().build().perform();
        waitForFragmentNotVisible(this);
        return this;
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

    public void closeDropdownContentOfAttribute() {
        if (isElementVisible(navigateUpBtn)) {
            navigateUpBtn.click();
        }
    }
}
