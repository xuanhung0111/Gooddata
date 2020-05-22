package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class Model extends AbstractFragment {
    private static final String ID_MODEL = "g[id = 'dataset.%s']";
    private static final String UNTITLED_ATTRIBUTE = "ds-item-attr%suntitledattribute";
    private static final String UNTITILED_FACT = "ds-item-fact%suntitledfact";

    @FindBy(css = ".ds-title .text .v-line")
    private WebElement datasetName;

    @FindBy(className = "actions")
    private ModelAction modelAction;

    @FindBy(css = ".ds-items")
    private ModelItems modelItems;

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

    public String getAttributeText(String attribute) {
        return getListItems().getAttribute(getDatasetTitle(), attribute).getText();
    }

    public String getReferenceText(String dataset) {
        return getListItems().getReference(dataset).getText();
    }

    public String getFactText(String fact) {
        return getListItems().getFact(getDatasetTitle(), fact).getText();
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
        addMore.setPrimaryKey(getDatasetTitle(), attributeName);
    }

    public void deleteDataset() {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.deleteDataset();
    }

    public void addNewLabel(String attribute, String labelName){
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().addNewLabel(attribute, labelName);
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

    public void editLabelName(String label, String newName) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeLabelName(label, newName);
    }

    public void editDatatypeOfMainLabel(String attribute, String dataTypeClass) {
        AddMorePopUp addMore = openMorePopUpOnDataset();
        addMore.editDatasetDialog().changeDatatypeOfMainLabel(attribute, dataTypeClass);
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

    public WebElement getUntitledAttribute(String datasetName) {
        return this.getRoot().findElement(className(format(UNTITLED_ATTRIBUTE, datasetName)));
    }

    public WebElement getUntitledFact(String datasetName) {
        return this.getRoot().findElement(className(format(UNTITILED_FACT, datasetName)));
    }

    public enum DATA_TYPE {
        INTEGER("Integer", "s-integer"),
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
}
