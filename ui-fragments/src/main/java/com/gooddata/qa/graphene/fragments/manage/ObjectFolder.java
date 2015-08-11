package com.gooddata.qa.graphene.fragments.manage;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class ObjectFolder extends AbstractFragment {

    private static final String XPATH_FOLDER_LINK = "//a[@title='${folderName}']";

    private static final String XPATH_ADD_FOLDER_BUTTON = "//div[@id='${list}']//button[contains(@class,'s-btn-add_folder')]";

    private static final String XPATH_FOLDERS_LIST = "//div[@id='${list}']//ul[@class='listList']";

    @FindBy(xpath = "//input[contains(@class,'s-newFolderTitle')]")
    private WebElement folderNameInput;

    @FindBy(xpath = "//button[contains(@class,'s-newFolderButton')]")
    private WebElement addButton;

    @FindBy(xpath = "//button[contains(@class,'s-btn-cancel')]")
    private WebElement cancelButton;

    @FindBy(xpath = "//div[@id='dimensionList']//ul[@class='listList']")
    private WebElement foldersList;

    @FindBy(xpath = "//button[contains(@class,'s-btn-folder_settings') and not(contains(@class,'gdc-hidden'))]")
    private WebElement settingButton;

    @FindBy(xpath = "//input[@name='title']")
    private WebElement folderTitleInput;

    @FindBy(xpath = "//textarea[@name='description']")
    private WebElement folderDesTextarea;

    @FindBy(xpath = "//button[text()='Done']")
    private WebElement doneButton;

    @FindBy(xpath = "//div[@class='bd_controls']//button[contains(@class,'s-btn-cancel')]")
    private WebElement cancelSettingButton;

    @FindBy(xpath = "//div[@class='bd_controls']//button[contains(@class,'s-btn-delete')]")
    private WebElement deleteFolderButton;

    @FindBy(xpath = "//div[@class='bd_controls']//button[contains(@class,'s-confirm-btn-yes')]")
    private WebElement confirmDeleteFolder;

    @FindBy(xpath = "//button[contains(@class,'s-btn-cancel') and not(contains(@class,'gdc-hidden'))]")
    private WebElement confirmCancelDelete;

    @FindBy(xpath = "//div[contains(@class,'c-validationErrorMessages')]")
    private WebElement errorMessage;

    private WebElement getAddFolderButton(String page) {
        WebElement addFolderButton;
        if (page.equalsIgnoreCase("attributes")) {
            addFolderButton = root.findElement(By.xpath(XPATH_ADD_FOLDER_BUTTON
                    .replace("${list}", "dimensionList")));
        } else {
            addFolderButton = root.findElement(By.xpath(XPATH_ADD_FOLDER_BUTTON
                    .replace("${list}", "foldersList")));
        }
        return addFolderButton;
    }

    private List<WebElement> getFoldersList(String page) {
        By folders;
        if (page.equalsIgnoreCase("attributes")) {
            folders = By.xpath(XPATH_FOLDERS_LIST.replace("${list}", "dimensionList"));
        } else {
            folders = By.xpath(XPATH_FOLDERS_LIST.replace("${list}", "foldersList"));
        }
        waitForElementVisible(root.findElement(folders));
        return root.findElement(folders).findElements(By.tagName("li"));
    }

    public void checkFolderVisible(String folderName) {
        By folder = By.xpath(XPATH_FOLDER_LINK.replace("${folderName}",
                folderName));
        waitForElementVisible(folder, browser);
    }

    public void verifyFolderList(String page, List<String> folders) {
        if (folders != null && folders.size() > 0) {
            int i = 0;
            for (WebElement elem : getFoldersList(page)) {
                Assert.assertEquals(
                        elem.findElement(By.tagName("a")).getText(),
                        folders.get(i));
                i++;
            }
        }
    }

    public void addFolder(String page, String folderName, String expectedError) {
        waitForElementVisible(getAddFolderButton(page)).click();
        waitForElementVisible(folderNameInput).sendKeys(folderName);
        waitForElementVisible(addButton).click();
        if (expectedError != null) {
            waitForElementVisible(errorMessage);
            Assert.assertEquals(errorMessage.getText(), expectedError,
                    "Invalid error message");
            waitForElementVisible(cancelButton).click();
        } else {
            checkFolderVisible(folderName);
        }
    }

    public void editFolder(String oldName, String newName, String description) {
        By oldFolder = By.xpath(XPATH_FOLDER_LINK.replace("${folderName}",
                oldName));
        waitForElementVisible(oldFolder, browser).click();
        waitForElementVisible(settingButton).click();
        waitForElementVisible(cancelSettingButton).click();
        waitForElementVisible(settingButton).click();
        waitForElementVisible(folderTitleInput).clear();
        folderTitleInput.sendKeys(newName);
        waitForElementVisible(folderDesTextarea).sendKeys(description);
        waitForElementVisible(doneButton).click();
        waitForElementNotPresent(oldFolder);
        checkFolderVisible(newName);
    }

    public void deleteFolder(String folderName) {
        By folder = By.xpath(XPATH_FOLDER_LINK.replace("${folderName}",
                folderName));
        waitForElementVisible(folder, browser).click();
        waitForElementVisible(settingButton).click();
        waitForElementVisible(deleteFolderButton).click();
        waitForElementVisible(confirmCancelDelete).click();
        waitForElementVisible(deleteFolderButton).click();
        waitForElementVisible(confirmDeleteFolder).click();
        waitForElementNotPresent(folder);
    }

    public void checkEditorViewFolderList(String page, List<String> folders) {
        if (folders != null && folders.size() > 0) {
            List<String> folderList = new ArrayList<String>();
            for (WebElement elem : getFoldersList(page)) {
                folderList.add(elem.findElement(By.tagName("a")).getText());
            }
            for (String folder : folders) {
                Assert.assertEquals(folderList.contains(folder), true);
            }
        }
        if (page.equalsIgnoreCase("metrics")) {
            waitForElementVisible(getAddFolderButton(page));
        } else {
            waitForElementNotVisible(getAddFolderButton(page));
        }
    }

    public void checkEditorCannotEditFolder(String folder) {
        By folderLink = By.xpath(XPATH_FOLDER_LINK.replace("${folderName}", folder));
        waitForElementVisible(folderLink, browser).click();
        waitForElementNotPresent(settingButton);
    }
}
