package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DataPage extends AbstractFragment {

    protected static final String XPATH_TAG_BY_LIST = "//ul/li[${index}]/a[text()='${tagName}']";
    protected static final String XPATH_TAG_BY_CLOUD =
            "//a[contains(@style, 'font-size: ${fontSize};') and text()='${tagName}']";
    protected static final String CSS_TARGET_FOLDER = "ul li:nth-child(${index})";
    private static final String XPATH_FOLDER_TITLE = "//a[@title='${targetFolder}']";
    private static final String XPATH_NEW_FOLDER_TITLE = "//a[@title='${folderName}']";

    @FindBy(css = "div#objectTypesList")
    protected WebElement objectTypesList;

    @FindBy(css = "div#foldersList span button.s-btn-add_folder")
    protected WebElement addFolderButton;

    @FindBy(css = "div#dimensionList span button.s-btn-add_folder")
    protected WebElement addDimensionButton;

    @FindBy(css = "div.s-btn-ipe-editor")
    protected WebElement addFolderDialog;

    @FindBy(css = "input.s-newFolderTitle")
    protected WebElement newFolderTitleInput;

    @FindBy(xpath = "//button[text()='Add' and contains(@class, s-ipeSaveButton)]")
    protected WebElement confirmAddNewFolderButton;

    @FindBy(css = "div#foldersList")
    protected WebElement foldersList;

    @FindBy(css = "div#dimensionList")
    protected WebElement dimensionList;

    @FindBy(css = "div.objectsCloud")
    protected WebElement objectsCloud;

    @FindBy(xpath = "//div[@class='c-modalDialog t-confirmDelete']")
    protected WebElement deleteConfirmDialog;

    @FindBy(xpath = "//div[contains(@class, 'hd')]/span")
    protected WebElement deleteConfirmDialogHeader;

    @FindBy(xpath = "//div[@class='bd']/div/div/form")
    protected WebElement deleteConfirmDialogMessage;

    @FindBy(xpath = "//button[text()='Delete']")
    protected WebElement deleteConfirmButton;

    @FindBy(xpath = "//button[text()='Cancel']")
    protected WebElement cancelDeleteButton;

    @FindBy(xpath = "//div[@class='c-ipeEditor c-ipeEditorIpe']")
    protected WebElement moveObjectsBetweenFoldersDialog;

    @FindBy(xpath = "//div[@class='massActions']/p[text()='Select:']")
    private WebElement selectMassActions;

    @FindBy(xpath = "//div[@class='massActions']/a[text()='All']")
    private WebElement selectAllAction;

    @FindBy(xpath = "//div[@class='massActions']/a[text()='None']")
    private WebElement selectNoneAction;

    @FindBy(xpath = "//div[@class='massActions']//button[text()='Move...']")
    private WebElement moveObjectsButton;

    @FindBy(xpath = "//div[@class='massActions']//button[text()='Delete...']")
    private WebElement deleteObjectsButton;

    @FindBy(xpath = "//div[@class='c-ipeEditorIn']/input")
    private WebElement moveObjectsDialogInput;

    @FindBy(xpath = "//div[@class='c-ipeEditorControls']/button[text()='Ok']")
    private WebElement moveObjectsDialogConfirmButton;

    @FindBy(xpath = "//div[@class='autocompletion']/div[@class='suggestions']")
    private WebElement suggestionFoldersList;

    @FindBy(css = "td.messageBox")
    private WebElement progressMessageBox;

    @FindBy(xpath = "//div[@id='status']/div/div/div[@class='leftContainer']")
    private WebElement statusMessageOnGreenBar;

    @FindBy(xpath = "//button[contains(@class, 'switch')]")
    private WebElement switchTagsDisplayedForm;

    @FindBy(xpath = "//button[text()='Show as Cloud']")
    private WebElement showAsCloudButton;

    @FindBy(xpath = "//button[text()='Show as List']")
    private WebElement showAsListButton;

    @FindBy(xpath = "//button[text()='Deselect all']")
    private WebElement deselectAllTagsButton;

    @FindBy(id = "p-dataPage")
    private ObjectFolder objectFolder;

    public ObjectFolder getObjectFolder() {
        return objectFolder;
    }

    public WebElement getMenuItem(ObjectTypes objectType) {
        return objectTypesList.findElement(By.xpath(objectType.getMenuItemXpath()));
    }

    public WebElement getAddFolderButton() {
        return addFolderButton;
    }

    public WebElement getAddDimensionButton() {
        return addDimensionButton;
    }

    public WebElement getAddFolderDialog() {
        return addFolderDialog;
    }

    public void setNewFolderTitle(String folderTitle) {
        newFolderTitleInput.sendKeys(folderTitle);
    }

    public WebElement getConfirmAddFolderButton() {
        return confirmAddNewFolderButton;
    }

    public WebElement getDimensionList() {
        return dimensionList;
    }

    public WebElement getFoldersList() {
        return foldersList;
    }

    public WebElement getFolderDimension(WebElement folderDimensionList, String folderName) {
        return folderDimensionList.findElement(By.xpath(XPATH_NEW_FOLDER_TITLE.replace(
                "${folderName}", folderName)));
    }

    public WebElement getMoveObjectsButton() {
        return moveObjectsButton;
    }

    public WebElement getSelectMassActions() {
        return selectMassActions;
    }

    public WebElement getAllAction() {
        return selectAllAction;
    }

    public WebElement getNoneAction() {
        return selectNoneAction;
    }

    public WebElement getDeleteObjectsButton() {
        return deleteObjectsButton;
    }

    public WebElement getMoveObjectsDialog() {
        return moveObjectsBetweenFoldersDialog;
    }

    public WebElement getProgressMessageBox() {
        return progressMessageBox;
    }

    public WebElement getStatusMessageOnGreenBar() {
        return statusMessageOnGreenBar;
    }

    public WebElement getTargetFolder(String folderIndex) {
        return suggestionFoldersList.findElement(By.cssSelector(CSS_TARGET_FOLDER.replace(
                "${index}", folderIndex)));
    }

    public WebElement getMoveObjectsDialogInput() {
        return moveObjectsDialogInput;
    }

    public WebElement getMoveObjectsDialogConfirmButton() {
        return moveObjectsDialogConfirmButton;
    }

    public WebElement getDeleteConfirmDialog() {
        return deleteConfirmDialog;
    }

    public WebElement getDeleteConfirmDialogHeader() {
        return deleteConfirmDialogHeader;
    }

    public WebElement getDeleteConfirmDialogMessage() {
        return deleteConfirmDialogMessage;
    }

    public WebElement getDeleteConfirmButton() {
        return deleteConfirmButton;
    }

    public WebElement getCancelDeleteButton() {
        return cancelDeleteButton;
    }

    public WebElement getFolder(String folderName) {
        return foldersList.findElement(By.xpath(XPATH_FOLDER_TITLE.replace("${targetFolder}",
                folderName)));
    }

    public WebElement getSwitchTagsDisplayedForm() {
        return switchTagsDisplayedForm;
    }

    public WebElement getShowAsCloudButton() {
        return showAsCloudButton;
    }

    public WebElement getShowAsListButton() {
        return showAsListButton;
    }

    public WebElement getTagByCloud(String tagName, String fontSize) {
        return objectsCloud.findElement(By.xpath(XPATH_TAG_BY_CLOUD
                .replace("${fontSize}", fontSize).replace("${tagName}", tagName)));
    }

    public WebElement getTagByList(String tagName, String index) {
        return objectsCloud.findElement(By.xpath(XPATH_TAG_BY_LIST.replace("${index}", index)
                .replace("${tagName}", tagName)));
    }

    public WebElement getDeselectAllButton() {
        return deselectAllTagsButton;
    }

    public void assertMassActions() {
        Assert.assertTrue(getSelectMassActions().isDisplayed());
        Assert.assertTrue(getAllAction().isDisplayed());
        Assert.assertTrue(getNoneAction().isDisplayed());
        Assert.assertTrue(getMoveObjectsButton().isDisplayed());
        Assert.assertTrue(getDeleteObjectsButton().isDisplayed());
    }
}
