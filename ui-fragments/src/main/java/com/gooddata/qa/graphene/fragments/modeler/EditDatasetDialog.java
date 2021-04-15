package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.sdk.common.GoodDataRestException;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;

public class EditDatasetDialog extends AbstractFragment {
    private static final String EDIT_DATASET_DIALOG = "edit-dataset-dialog";
    private static final String ROW_TABLE_CONTENT = "%s.%s.%s";
    private static final By SAVE_CHANGE_BUTTON = By.className("s-save_changes");

    @FindBy(className = "s-cancel")
    WebElement cancelButton;

    @FindBy(className = "s-save_changes")
    WebElement saveChangeButton;

    @FindBy(className = "indigo-table-component")
    ViewDetailDialog viewDetailDialog;

    @FindBy(className = "add-attr")
    WebElement addAtrButton;

    @FindBy(className = "add-fact")
    WebElement addFactButton;

    @FindBy(className = "public_fixedDataTableCell_cellContent")
    List<WebElement> cellTableContent;

    @FindBy(className = "public_fixedDataTable_bodyRow")
    List<WebElement> rowsInTableDialog;

    @FindBy(css = ".s-csvmodeling .s-fields")
    WebElement fieldTab;

    @FindBy(css = ".s-csvmodeling .s-datamapping")
    WebElement dataMappingTab;

    @FindBy(className = "model-mapping")
    DataMapping dataMappingContent;

    @FindBy(className ="source-name")
    WebElement sourceName;

    @FindBy(className = "s-more-item")
    WebElement moreButton;

    public static EditDatasetDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                EditDatasetDialog.class, waitForElementVisible(className(EDIT_DATASET_DIALOG), searchContext));
    }

    public ViewDetailDialog getViewDetailDialog() {
        waitForElementVisible(viewDetailDialog.getRoot());
        return viewDetailDialog;
    }

    public void saveChanges() {
        waitForElementVisible(saveChangeButton).click();
        waitForFragmentNotVisible(this);
    }

    public DataMapping clickOnDataMappingTab() {
        waitForElementVisible(dataMappingTab).click();
        waitForFragmentVisible(dataMappingContent);
        return dataMappingContent.getInstance(browser);
    }

    public void addNewLabel(String attribute, String labelName){
        ViewDetailDialog viewDetail = getViewDetailDialog();
        hoverOnAttributeOrFactInDialog(attribute);
        viewDetail.addNewLabel(attribute, labelName);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public void addNewLabelTypeHyperlink(String attribute, String labelName){
        ViewDetailDialog viewDetail = getViewDetailDialog();
        hoverOnAttributeOrFactInDialog(attribute);
        viewDetail.addNewLabelTypeHyperlink(attribute, labelName);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void addNewLabelForPrimaryKey(String attribute, String labelName){
        ViewDetailDialog viewDetail = getViewDetailDialog();
        hoverOnAttributeOrFactInDialog(attribute);
        viewDetail.addNewLabelForPrimaryKey(attribute, labelName);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public void deleteLabel(String label) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.deleteLabel(label);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void deleteAttribute(String attribute) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        hoverOnAttributeOrFactInDialog(attribute);
        viewDetail.deleteAttribute(attribute);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void deleteAttributesOrFactInEditDatasetDialog(String items) {
        hoverOnAttributeOrFactInDialog(items);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickDeleteItem();
    }

    public boolean isDeleteButtonPresentOnLabel(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        return MoveDeleteMenu.getInstance(browser).isDeleteButtonPresent();
    }

    public boolean isDefaultLabelPresent(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        return MoveDeleteMenu.getInstance(browser).isDefaultLabelOptionPresent();
    }

    public boolean isSortLabelPresent(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        return MoveDeleteMenu.getInstance(browser).isSortLabelOptionPresent();
    }

    public void setDefaultLabel(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickSetDefaultLabel();
        saveChanges();
    }

    public void clickSortAttributeLabel(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickSortAttributeLabel();
        saveChanges();
    }

    public SortOrderMenu setChangeSortOrder(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickChangeSortOrder();
        return SortOrderMenu.getInstance(browser);
    }

    public void deleteLabelInDatasetDialog(String label, String modelName) {
        hoverOnLabel(label, modelName);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickDeleteItem();
    }

    public MoveFieldDataset moveLabelToDataset(String label) {
        hoverOnAttributeOrFactInDialog(label);
        moreButton.click();
        MoveDeleteMenu.getInstance(browser).clickMoveItem();
        return MoveFieldDataset.getInstance(browser);
    }

    public MoveDeleteMenu getMoveFiledInDataset(String label) {
        hoverOnAttributeOrFactInDialog(label);
        moreButton.click();
        return MoveDeleteMenu.getInstance(browser);
    }

    public boolean isMoreMenuIconVisible() {
        return isElementVisible(By.className("s-more-item"), this.getRoot());
    }

    public void changeAttributeName(String attribute, String newName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editAttributeName(attribute, newName);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public void changeAttributeNameAndNotSave(String attribute, String newName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editAttributeName(attribute, newName);
    }

    public void addAttribute(String attributeName) {
        addAtrButton.click();
        waitingForNewRowAttributeInput();
        Actions driverActions = new Actions(browser);
        driverActions.sendKeys(attributeName).sendKeys(Keys.ENTER).build().perform();
    }

    private void waitingForNewRowAttributeInput() {
        int currentNumFact = cellTableContent.stream().filter(el -> el.getText().equals("Fact"))
                .collect(Collectors.toList()).size();
        int currentNumAttr = cellTableContent.stream().filter(el -> el.getText().equals("Attribute")).collect(
                Collectors.toList()).size();
        if (currentNumFact == 0 && currentNumAttr == 0) 
            waitForElementPresent(rowsInTableDialog.stream().filter(el -> el.getAttribute("aria-rowindex")
                    .equals("2")).findFirst().get());
        waitForElementPresent(rowsInTableDialog.stream().filter(el -> el.getAttribute("aria-rowindex")
                .equals(Integer.toString(currentNumAttr * 2 + currentNumFact + 1))).findFirst().get());
    }

    private void hoverOnAttributeOrFactInDialog(String item) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(rowsInTableDialog.stream().filter(
                el -> el.getText().contains(item)).findFirst().get()).build().perform();
    }

    public void hoverOnLabel(String label, String modelName) {
        getActions().moveToElement(rowsInTableDialog.stream().filter(
            el -> el.getText().contains("label." + modelName + "." + label)).findFirst().get()).build().perform();
    }

    public void clickOnLabelInDataset(String label, String modelName) {
        clickOnRowInDatasetDetails(String.format(ROW_TABLE_CONTENT, "label", modelName, label));
    }

    public void clickOnAttributeInDataset(String attribute, String modelName) {
        clickOnRowInDatasetDetails(String.format(ROW_TABLE_CONTENT, "attr", modelName, attribute));
    }

    public void clickOnFactInDataset(String fact, String modelName) {
        clickOnRowInDatasetDetails(String.format(ROW_TABLE_CONTENT, "fact", modelName, fact));
    }

    private void clickOnRowInDatasetDetails(String rowContent) {
        getActions().moveToElement(rowsInTableDialog.stream().filter(
                el -> el.getText().contains(rowContent)).findFirst().get()).click().build().perform();
    }

    public void addFact(String factName) {
        addFactButton.click();
        waitingForNewRowAttributeInput();
        Actions driverActions = new Actions(browser);
        driverActions.sendKeys(factName).sendKeys(Keys.ENTER).build().perform();
    }

    public boolean isAttributeExist(String attributeName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.isAttributeExist(attributeName);
    }

    public boolean isFactExist(String factName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.isFactExist(factName);
    }

    public int getDatatypeSize() {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.getSizeDatatype();
    }

    public void changeLabelName(String label, String newName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editLabelName(label, newName);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public void changeDatatypeOfMainLabel(String attribute, String dataTypeClass) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editDatatypeOfLabel(attribute + " (default)", dataTypeClass);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public void changeDatatypeOfLabel(String attribute, String dataTypeClass) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editDatatypeOfLabel(attribute, dataTypeClass);
        saveChangeButton.click();
        sleepTightInSeconds(3);
//        waitForFragmentNotVisible(this);
    }

    public String getTextLabel(String label) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.getTextLabel(label);
    }

    public String getTextSourceName() {
        waitForElementVisible(sourceName);
        return sourceName.getText();
    }

    public String getTextDatatype(String attribute) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.getTextDataType(attribute);
    }

    public int getNumberOfAttributes() {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.getNumberOfAttributes();
    }

    public void clickCancel() {
        cancelButton.click();
        waitForFragmentNotVisible(this);
    }

    public boolean isSaveChangeButtonExist() {
       return isElementVisible(SAVE_CHANGE_BUTTON, getRoot());
    }

    public static class MoveDeleteMenu extends AbstractFragment {
        private static final String MOVE_DELETE_MENU = "move-delete-menu";

        @FindBy(className = "dataset-detail-move-item")
        WebElement moveButton;

        @FindBy(className = "btn-move")
        WebElement moveButtonDataset;

        @FindBy(className = "dataset-detail-delete-item")
        WebElement deleteButton;

        @FindBy(className = "dataset-detail-set-default-item")
        WebElement setDefaultLabel;

        @FindBy(className = "dataset-detail-change-sort-order")
        WebElement changeSortOrderLabel;

        @FindBy(className = "dataset-detail-sort-label")
        WebElement sortLabel;

        public static MoveDeleteMenu getInstance(SearchContext searchContext) {
            return Graphene.createPageFragment(
                    MoveDeleteMenu.class, waitForElementVisible(className(MOVE_DELETE_MENU), searchContext));
        }

        public void clickDeleteItem() {
            waitForElementVisible(deleteButton).click();
            waitForFragmentNotVisible(this);
        }

        public void clickMoveItem() {
            waitForElementVisible(moveButton).click();
        }

        public void clickSetDefaultLabel() {
            waitForElementVisible(setDefaultLabel).click();
        }

        public void clickSortAttributeLabel() {
            waitForElementVisible(sortLabel).click();
        }

        public void clickChangeSortOrder() {
            waitForElementVisible(changeSortOrderLabel).click();
        }

        public boolean isDeleteButtonPresent() {
            return isElementPresent(By.className("dataset-detail-delete-item"), this.getRoot());
        }

        public boolean isMoveOptionVisible() {
            return isElementVisible(By.className("s-move"), getRoot());
        }

        public boolean isDefaultLabelOptionPresent() {
            return isElementVisible(By.className("dataset-detail-set-default-item"), getRoot());
        }

        public boolean isSortLabelOptionPresent() {
            return isElementVisible(By.className("dataset-detail-sort-label"), getRoot());
        }

        public void clickMoveButtonOnDataset() {
            waitForElementVisible(moveButtonDataset).click();
        }
    }

    public static class MoveFieldDataset extends AbstractFragment {
        private static final String MOVE_TO_DATASET = "move-field-to-dataset";

        @FindBy(className = "gd-input-field")
        private WebElement searchInputField;

        @FindBy(className = "s-move")
        private WebElement moveBtn;

        @FindBy(className = "s-cancel")
        private WebElement cancelBtn;

        @FindBy(css = ".initial-page-select-data-source-detail-item .input-radio-label .input-label-text")
        List<WebElement> listDatasets;

        public static MoveFieldDataset getInstance(SearchContext searchContext) {
            return Graphene.createPageFragment(
                    MoveFieldDataset.class, waitForElementVisible(className(MOVE_TO_DATASET), searchContext));
        }

        public MoveFieldDataset searchDataset(String datasetName) {
            waitForElementVisible(searchInputField).sendKeys(datasetName);
            return this;
        }

        public MoveFieldDataset selectDataset(String datasetName) {
            WebElement datasetEl = listDatasets.stream().filter(el -> el.getText().equals(datasetName)).findFirst().get();
            ((JavascriptExecutor) browser).executeScript("arguments[0].click();", datasetEl);
            moveBtn.click();
            waitForFragmentNotVisible(this);
            return this;
        }
    }

    public static class SortOrderMenu extends AbstractFragment {
        private static final String SORT_ORDER_MENU = "change-sort-order-menu";

        @FindBy(className = "s-apply")
        private WebElement applyBtn;

        @FindBy(className = "s-cancel")
        private WebElement cancelBtn;

        @FindBy(className = "asc")
        WebElement ascendingSort;

        @FindBy(className = "desc")
        WebElement descendingSort;

        public static SortOrderMenu getInstance(SearchContext searchContext) {
            return Graphene.createPageFragment(
                    SortOrderMenu.class, waitForElementVisible(className(SORT_ORDER_MENU), searchContext));
        }

        public void sortAscending() {
            waitForElementVisible(ascendingSort).click();
            applyBtn.click();
            waitForFragmentNotVisible(this);
        }

        public void sortDescending() {
            waitForElementVisible(descendingSort).click();
            applyBtn.click();
            waitForFragmentNotVisible(this);
        }

        public void clickCancelButton() {
            waitForElementVisible(cancelBtn).click();
            waitForFragmentNotVisible(this);
        }
    }

    public Optional<String> checkIdOfAttributeDatasetSameWithTitleDataset(String id) {
        return getRoot().findElements(By.className("public_fixedDataTable_bodyRow")).stream()
                .filter(el -> isElementVisible(el))
                .map(el -> el.getText().split("\n")[2]).collect(Collectors.toList())
                .stream().filter(content -> !content.contains(id) && content != "").findAny();
    }
}
