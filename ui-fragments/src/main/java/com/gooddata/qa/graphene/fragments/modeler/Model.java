package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class Model extends AbstractFragment {
    private static final String ID_MODEL = "g[id = 'dataset.%s']";
    private static final String UNTITLED_ATTRIBUTE = "ds-item-attr%suntitledattribute";
    private static final String UNTITILED_FACT = "ds-item-fact%suntitledfact";
    private static final By TOOLTIP_CONTENT = By.className("tooltip-content");
    private static final By WARNING_MESSAGE = By.cssSelector(".ds-title .icon");

    @FindBy(css = ".ds-title .text .v-line")
    private WebElement datasetName;

    @FindBy(className = "actions")
    private ModelAction modelAction;

    @FindBy(css = ".ds-items")
    private ModelItems modelItems;

    @FindBy(css = ".ds-title .toggle")
    private WebElement toggleIcon;

    @FindBy(css = ".ds-title .icon")
    private WebElement warningIcon;

    public static Model getInstance(SearchContext searchContext, String id) {
        return Graphene.createPageFragment(
                Model.class, waitForElementVisible(cssSelector((format(ID_MODEL, id))), searchContext));
    }

    public String getDatasetTitle() {
        return datasetName.getText();
    }

    public ModelItems getListItems() {
        waitForElementVisible(modelItems.getRoot());
        return modelItems;
    }

    public Model clickCollapseButton() {
        Actions action = getActions();
        waitForElementVisible(toggleIcon);
        action.moveToElement(toggleIcon).click().build().perform();
        return this;
    }

    public String getAttributeText(String attribute) {
        return getListItems().getAttribute(getDatasetTitle().toLowerCase(), attribute).getText();
    }

    public boolean isAttributeExistOnModeler(String attribute) {
        return getListItems().isAttributeExist(getDatasetTitle().toLowerCase(), attribute);
    }

    public boolean isAttributeExistOnDataset(String datasetName, String attribute) {
        return getListItems().isAttributeExist(datasetName, attribute);
    }

    public boolean isFactExistOnDataset(String datasetName, String attribute) {
        return getListItems().isFactExist(datasetName, attribute);
    }

    public boolean isPrimaryKeyExistOnDataset(String datasetName, String key) {
        return getListItems().isPrimaryKeyExist(datasetName, key);
    }

    public boolean isFactExistOnModeler(String attribute) {
        return getListItems().isFactExist(getDatasetTitle().toLowerCase(), attribute);
        }

    public WebElement getAttribute(String attribute) {
        return getListItems().getAttribute(getDatasetTitle().toLowerCase(), attribute);
    }

    public Model deleteAttributeOnDataset(String attributeName) {
        WebElement attribute = getListItems().getAttribute(getDatasetTitle().toLowerCase(), attributeName);
        deleteItemProcess(attribute);
        return this;
    }

    public Model deleteFactOnDataset(String fact, String  datasetName) {
        WebElement factEl = getListItems().getFact(datasetName, fact);
        deleteItemProcess(factEl);
        return this;
    }

    public void deleteItemProcess(WebElement attributeEl) {
        getActions().moveToElement(attributeEl).click().build().perform();
        WebElement moreActionButton = attributeEl.findElement(By.className("more-action-container"));
        getActions().moveToElement(moreActionButton).click().build().perform();
        PaperScrollerBackground.getInstance(browser).getContextToolbar().deleteElement();
    }

    public Model moveAttributeOnDataset(String fromDatasetName, String attributeName, String toDatasetName) {
        WebElement attributeEl = getListItems().getAttribute(fromDatasetName, attributeName);
        processMoveElement(attributeEl, toDatasetName);
        return this;
    }

    public Model moveFactOnDataset(String fromDatasetName, String fact, String toDatasetName) {
        WebElement factEl = getListItems().getFact(fromDatasetName, fact);
        processMoveElement(factEl, toDatasetName);
        return this;
    }

    public void processMoveElement(WebElement element, String toDatasetName) {
        getActions().moveToElement(element).click().build().perform();
        WebElement moreActionButton = element.findElement(By.className("more-action-container"));
        getActions().moveToElement(moreActionButton).click().build().perform();
        EditDatasetDialog.MoveDeleteMenu.getInstance(browser).clickMoveButtonOnDataset();
        EditDatasetDialog.MoveFieldDataset.getInstance(browser).selectDataset(toDatasetName);
    }

    public String getReferenceText(String dataset) {
        return getListItems().getReference(dataset).getText();
    }

    public String getFactText(String fact) {
        return getListItems().getFact(getDatasetTitle().toLowerCase(), fact).getText();
    }

    public String getDateText() {
        return getListItems().getDate().getText();
    }

    public ModelAction getModelAction() {
        waitForElementVisible(modelAction.getRoot());
        return modelAction;
    }

    public AddMorePopUp openMorePopUpOnDataset() {
        Actions driverActions = new Actions(browser);
        driverActions.click(this.getModelAction().addMore()).perform();
        return AddMorePopUp.getInstance(browser);
    }

    public void setPrimaryKey(String attributeName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.setPrimaryKey(getDatasetTitle().toLowerCase(), attributeName);
    }

    public void searchPrimaryKey(String attributeName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.searchPrimaryKey(getDatasetTitle().toLowerCase(), attributeName);
    }

    public void deleteDataset() {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.deleteDataset();
    }

    public void addNewLabel(String attribute, String labelName){
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().addNewLabel(attribute, labelName);
    }

    public void addNewLabelForPrimaryKey(String attribute, String labelName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().addNewLabelForPrimaryKey(attribute, labelName);
    }

    public void deleteLabel(String label) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().deleteLabel(label);
    }

    public void deleteAttribute(String attribute) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().deleteAttribute(attribute);
    }

    public void editAttributeName(String attribute, String newName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeAttributeName(attribute, newName);
    }

    public void editAttributeNameAndNotSave(String attribute, String newName) {
        OverlayWrapper.getInstanceByIndex(browser,1).getEditDatasetDialog().changeAttributeNameAndNotSave(attribute, newName);
    }

    public void addAttribute(String attributeName) {
        OverlayWrapper.getInstanceByIndex(browser,1).getEditDatasetDialog().addAttribute(attributeName);
    }

    public boolean isAttributeExist(String attributeName) {
        return OverlayWrapper.getInstanceByIndex(browser, 1).getEditDatasetDialog().isAttributeExist(attributeName);
    }

    public void addFact(String factName) {
        OverlayWrapper.getInstanceByIndex(browser, 1).getEditDatasetDialog().addFact(factName);
    }

    public boolean isFactExist(String factName) {
        return OverlayWrapper.getInstanceByIndex(browser, 1).getEditDatasetDialog().isFactExist(factName);
    }

    public void editLabelName(String label, String newName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeLabelName(label, newName);
    }

    public void editDatatypeOfMainLabel(String attribute, String dataTypeClass) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeDatatypeOfMainLabel(attribute, dataTypeClass);
    }

    public void editDatatypeOfLabel(String attribute, String dataTypeClass) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeDatatypeOfLabel(attribute, dataTypeClass);
    }

    public int getSizeOfDataType() {
        EditDatasetDialog edit = EditDatasetDialog.getInstance(browser);
        return edit.getDatatypeSize();
    }

    public int getNumberOfAttrtibutes() {
        EditDatasetDialog edit = EditDatasetDialog.getInstance(browser);
        return edit.getNumberOfAttributes();
    }

    public String getTextDatatype(String attribute) {
        EditDatasetDialog edit = EditDatasetDialog.getInstance(browser);
        return edit.getTextDatatype(attribute);
    }

    public String getTextLabel(String label) {
        EditDatasetDialog edit = EditDatasetDialog.getInstance(browser);
        return edit.getTextLabel(label);
    }

    public void clickCancelEditPopUp() {
        EditDatasetDialog edit = EditDatasetDialog.getInstance(browser);
        edit.clickCancel();
    }

    public void openEditDialog() {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog();
    }

    //open Dialog in case there are some overlay wrapper on UI
    public void openEditDialog(int index) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog(index);
    }

    public WebElement getUntitledAttribute(String datasetName) {
        return this.getRoot().findElement(className(format(UNTITLED_ATTRIBUTE, datasetName)));
    }

    public WebElement getUntitledFact(String datasetName) {
        return this.getRoot().findElement(className(format(UNTITILED_FACT, datasetName)));
    }

    public String getModelId() {
        return this.getRoot().getAttribute("id");
    }

    public enum DATA_TYPE {
        INTEGER("Integer", "s-integer"),
        DECIMAL_12_2("Decimal(12,2)", "s-decimal_12_2_"),
        TEXT_128("Text(128)", "s-text_128_"),
        BIG_INTEGER("BigInteger", "s-biginteger");

        private final String name;
        private final String className;

        private DATA_TYPE(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }
    }

    public void hoverOnTooltipDataset() {
        WebElement pointConnection = browser.findElement(By.cssSelector(".joint-halo > div > div"));
        getActions().moveToElement(pointConnection).build().perform();
    }

    public String getConnectionStatusOnDataset() {
        return browser.findElement(TOOLTIP_CONTENT).getText();
    }

    public boolean isDatasetTooltipDisplayed() {
        return isElementVisible(TOOLTIP_CONTENT, browser);
    }

    public boolean isWarningMessageDisplayed() {
        try {
            String tooltip = waitForElementVisible(WARNING_MESSAGE, browser).getAttribute("data-tooltip-class-name");
            if (tooltip != null) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    public String getWarningMessageContent() {
        hoverOnWarningMessageIcon();
        return browser.findElement(TOOLTIP_CONTENT).getText();
    }

    public void hoverOnWarningMessageIcon() {
        getActions().moveToElement(warningIcon).build().perform();
    }
}
