package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class EditDatasetDialog extends AbstractFragment {
    private static final String EDIT_DATASET_DIALOG = "edit-dataset-dialog";

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

    public static EditDatasetDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                EditDatasetDialog.class, waitForElementVisible(className(EDIT_DATASET_DIALOG), searchContext));
    }

    public ViewDetailDialog getViewDetailDialog() {
        waitForElementVisible(viewDetailDialog.getRoot());
        return viewDetailDialog;
    }

    public void addNewLabel(String attribute, String labelName){
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.addNewLabel(attribute, labelName);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void deleteLabel(String label) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.deleteLabel(label);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void deleteAttribute(String attribute) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.deleteAttribute(attribute);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void changeAttributeName(String attribute, String newName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editAttributeName(attribute, newName);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }

    public void changeAttributeNameAndNotSave(String attribute, String newName) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editAttributeName(attribute, newName);
    }

    public void addAttribute(String attributeName) {
        addAtrButton.click();
        Actions driverActions = new Actions(browser);
        driverActions.sendKeys(attributeName).sendKeys(Keys.ENTER).build().perform();
    }

    public void addFact(String factName) {
        addFactButton.click();
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
        waitForFragmentNotVisible(this);
    }

    public void changeDatatypeOfMainLabel(String attribute, String dataTypeClass) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        viewDetail.editDatatypeOfLabel(attribute, dataTypeClass);
        saveChangeButton.click();
        waitForFragmentNotVisible(this);
    }


    public String getTextLabel(String label) {
        ViewDetailDialog viewDetail = getViewDetailDialog();
        return viewDetail.getTextLabel(label);
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
}
