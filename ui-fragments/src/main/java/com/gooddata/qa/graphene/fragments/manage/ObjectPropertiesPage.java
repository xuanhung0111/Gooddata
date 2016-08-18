package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.base.Predicate;

public abstract class ObjectPropertiesPage extends AbstractFragment {

    @FindBy(css = ".s-name")
    private WebElement objectName;

    @FindBy(css = ".s-description-ipe-placeholder")
    private WebElement descriptionIpePlaceholder;

    @FindBy(css = ".s-description")
    private WebElement descriptionIpe;

    @FindBy(css = "#p-objectPage .s-btn-delete")
    private WebElement deleteButton;

    @FindBy(xpath = "//span[text() = 'Add Tags']")
    private WebElement addTagButton;

    @FindBy(xpath = "//button[contains(@class, 's-btn-change_folder')]")
    private WebElement changeFolderButton;

    @FindBy(xpath = "//p[@class = 'folderText']/a")
    private WebElement locatedInFolder;

    @FindBy(css = "div#p-objectPage h1 a span")
    private WebElement backDataPageLink;

    @FindBy(xpath = "//div[@class = 'tag']")
    private List<WebElement> tagList;

    private static final By CONFIRM_DELETE_BUTTON_LOCATOR =
            By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    public static final String ROOT_XPATH_LOCATOR = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]";

    public void changeObjectFolder(final String newFolderName) {
        waitForElementVisible(changeFolderButton).click();
        IpeEditor.getInstance(browser).setText(newFolderName);

        final WebElement loadingWheelFolder = waitForElementPresent(By.cssSelector("span.loadingWheel"), browser);
        if (!loadingWheelFolder.getAttribute("class").contains("hidden")) {
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return loadingWheelFolder.getAttribute("class").contains("hidden");
                }
            });
        }

        try {
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return newFolderName.equals(locatedInFolder.getText());
                }
            });
        } catch (TimeoutException e) {
            System.out.println("Change folder doesn't work properly");
            throw e;
        }
    }

    public String changeObjectName(String newObjectName) {
        waitForElementVisible(objectName).click();
        IpeEditor.getInstance(browser).setText(newObjectName);
        waitForElementVisible(objectName);
        assertEquals(objectName.getText(), newObjectName, "Change name doesn't work properly");

        // name changed in UI is not sufficient for successful metric name change
        // since this change is done on background, navigating to different page may
        // interrupt it and metric name is not changed.
        // TODO: attach some notification class to gdc-client or consider changing via rest
        Sleeper.sleepTightInSeconds(1);
        return newObjectName;
    }

    public void addDescription(String description) {
        waitForElementVisible(descriptionIpePlaceholder).click();
        IpeEditor.getInstance(browser).setText(description);
        assertEquals(descriptionIpe.getText(), description, "Add description doesn't work properly");
    }

    public void addTag(String tagName) {
        int tagCountBefore = tagList.size();
        waitForElementVisible(addTagButton).click();
        IpeEditor.getInstance(browser).setText(tagName);
        int tagWords = 1;
        for (int i = 0; i < tagName.trim().length(); i++) {
            if (tagName.charAt(i) == ' ' && tagName.charAt(i + 1) != ' ') {
                tagWords++;
            }
        }
        int tagCountAfter = tagList.size();
        assertEquals(tagCountAfter, tagCountBefore + tagWords, "Add tag doesn't work properly");
        verifyTagElements(tagName);
    }

    public void verifyTagElements(String tagName) {
        String[] tagNameList = tagName.split("\\s+");
        boolean tagVisible = false;
        int matchingTag = 0;
        for (int i = 0; i < tagNameList.length; i++) {
            for (WebElement elem : tagList) {
                if (waitForElementVisible(elem).getAttribute("title").equalsIgnoreCase(
                        tagNameList[i])) {
                    matchingTag++;
                }
            }
            if (matchingTag == tagNameList.length) {
                tagVisible = true;
            }
        }
        assertTrue(tagVisible, "Add tag doesn't work properly");
    }

    public void verifyAllPropertiesAtOnce(String newObjectName, String description, String tagName) {
        verifyTagElements(tagName);
        assertEquals(waitForElementVisible(objectName).getText(), newObjectName,
                "Change name doesn't work properly");
        assertEquals(waitForElementVisible(descriptionIpe).getText(), description,
                "Add description doesn't work properly");
    }

    public String getObjectName() {
        return waitForElementVisible(objectName).getText();
    }

    public WebElement getBackDataPageLink() {
        return waitForElementVisible(backDataPageLink);
    }

    public void deleteObject() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(CONFIRM_DELETE_BUTTON_LOCATOR, browser).click();
        waitForDataPageLoaded(browser);
    }

    public boolean isDeleteButtonDisabled() {
        waitForElementVisible(deleteButton);

        // check that the button is truly non-clickable, i.e. that the delete
        // confirmation dialog does not appear
        deleteButton.click();
        waitForElementNotVisible(By.cssSelector(".t-confirmDelete"));

        return deleteButton.getAttribute("class").contains("disabled");
    }
}
