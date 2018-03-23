package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.xpath;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;

public class DataPage extends AbstractFragment {

    @FindBy(xpath = "//*[contains(@class,'listTitle')]/..[contains(@style,'display: block')]//button[contains(@class,'s-btn-add_folder')]")
    private WebElement addFolderButton;

    @FindBy(css = "div.objectsCloud")
    private WebElement objectsCloud;

    @FindBy(className = "all")
    private WebElement selectAllAction;

    @FindBy(className = "none")
    private WebElement selectNoneAction;

    @FindBy(css = ".massMove button")
    private WebElement moveObjectsButton;

    @FindBy(css = ".massDelete button")
    private WebElement deleteObjectsButton;

    @FindBy(xpath = "//div[@id='status']/div/div/div[@class='leftContainer']")
    private WebElement statusMessageOnGreenBar;

    @FindBy(className = "switch")
    private WebElement switchTagsDisplayedForm;

    @FindBy(className = "deselect")
    private WebElement deselectAllTagsButton;

    private static final String XPATH_TAG_BY_LIST = "//ul/li[${index}]/a[text()='${tagName}']";
    private static final String XPATH_TAG_BY_CLOUD =
            "//a[contains(@style, 'font-size: ${fontSize};') and text()='${tagName}']";

    private static final By FOLDERS_XPATH_LOCATOR = xpath("//*[contains(@style,'display: block') and ./*[contains(@class,'listList')]]//li/a");
    protected static final By ROOT_LOCATOR = cssSelector("#p-dataPage.s-displayed");

    public static DataPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DataPage.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public ObjectFolder getObjectFolder() {
        return Graphene.createPageFragment(ObjectFolder.class, waitForElementVisible(By.id("p-dataPage"), browser));
    }

    public List<WebElement> getFolders() {
        return getRoot().findElements(FOLDERS_XPATH_LOCATOR);
    }

    public void openPage(ObjectTypes objectType) {
        waitForElementVisible(cssSelector("div#objectTypesList"), getRoot())
            .findElement(xpath(objectType.getMenuItemXpath()))
            .click();
        waitForDataPageLoaded(browser);
    }

    public void createNewFolder(String name) {
        waitForElementVisible(addFolderButton).click();
        IpeEditor.getInstance(browser).setText(name);
        // buffer time to make sure new folder is available in UI
        sleepTightInSeconds(3);
    }

    public WebElement getMoveObjectsButton() {
        return waitForElementVisible(moveObjectsButton);
    }

    public WebElement getAllAction() {
        return waitForElementVisible(selectAllAction);
    }

    public WebElement getNoneAction() {
        return waitForElementVisible(selectNoneAction);
    }

    public WebElement getDeleteObjectsButton() {
        return waitForElementVisible(deleteObjectsButton);
    }

    public String getStatusMessageOnGreenBar() {
        return waitForElementVisible(statusMessageOnGreenBar).getText();
    }

    public WebElement getSwitchTagsDisplayedForm() {
        return waitForElementVisible(switchTagsDisplayedForm);
    }

    public WebElement getTagByCloud(String tagName, String fontSize) {
        return objectsCloud.findElement(By.xpath(XPATH_TAG_BY_CLOUD
                .replace("${fontSize}", fontSize).replace("${tagName}", tagName)));
    }

    public WebElement getTagByList(String tagName, String index) {
        return objectsCloud.findElement(By.xpath(XPATH_TAG_BY_LIST.replace("${index}", index)
                .replace("${tagName}", tagName)));
    }

    public void deselectAllTags() {
        waitForElementVisible(deselectAllTagsButton).click();
    }
}
