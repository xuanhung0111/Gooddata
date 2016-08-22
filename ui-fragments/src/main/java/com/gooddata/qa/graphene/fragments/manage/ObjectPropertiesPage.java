package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static java.util.stream.Collectors.toList;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.google.common.base.Predicate;

public abstract class ObjectPropertiesPage extends AbstractFragment {

    public static final By LOCATOR = By.cssSelector("#p-objectPage.s-displayed");

    private static final By BY_DELETE_BUTTON = By.cssSelector(".specialButton .s-btn-delete");
    private static final By BY_CONFIRM_DELETE_BUTTON = By.cssSelector(".t-confirmDelete .s-btn-delete");
    private static final By BY_CANCEL_DELETE_BUTTON = By.cssSelector(".t-confirmDelete .s-btn-cancel");

    @FindBy(css = "#p-objectPage .interpolateProject")
    private WebElement dataPageLink;

    // There are 2 tags contain name value. Tag contains 's-name-ipe-placeholder' class will be visible
    // if name field is empty or the one contains 's-name' will be visible
    @FindBy(css = "[class*='s-name'][style*='display: block']")
    private WebElement nameField;

    // There are 2 tags contain description value. Tag contains 's-description-ipe-placeholder' class will be visible
    // if description field is empty or the one contains 's-description' will be visible
    @FindBy(css = "[class*='s-description'][style*='display: block']")
    private WebElement descriptionField;

    @FindBy(className = "s-btn-add_tags")
    private WebElement addTagButton;

    @FindBy(css = ".c-tagger .tag")
    private Collection<WebElement> tags;

    @FindBy(className = "s-btn-change_folder")
    private WebElement changeFolderButton;

    @FindBy(className = "s-btn-add_comment")
    private WebElement addCommentButton;

    @FindBy(css = ".item.comment")
    private Collection<WebElement> commentItems;

    public ObjectPropertiesPage changeName(String name) {
        if (!IpeEditor.isPresent(browser)) {
            waitForElementVisible(nameField).click();
        }

        IpeEditor.getInstance(browser).setText(name);
        return this;
    }

    public String getName() {
        return waitForElementVisible(nameField).getText();
    }

    public boolean isNameFieldDisabled() {
        waitForElementVisible(nameField).click();
        if (!IpeEditor.isPresent(browser)) return true;

        IpeEditor.getInstance(browser).cancel();
        return false;
    }

    public ObjectPropertiesPage changeDescription(String description) {
        waitForElementVisible(descriptionField).click();
        IpeEditor.getInstance(browser).setText(description);

        return this;
    }

    public String getDescription() {
        return waitForElementVisible(descriptionField).getText();
    }

    public boolean isDescriptionFieldDisabled() {
        // This element is just PRESENT in case: EMPTY value and cannot edit.
        // Any actions on it will raise an exception. In this case, consider is as disabled
        if (!isElementVisible(descriptionField)) return true;

        waitForElementVisible(descriptionField).click();
        if (!IpeEditor.isPresent(browser)) return true;

        IpeEditor.getInstance(browser).cancel();
        return false;
    }

    public ObjectPropertiesPage addTag(String tag) {
        waitForElementVisible(addTagButton).click();
        IpeEditor.getInstance(browser).setText(tag);

        return this;
    }

    public Collection<String> getTags() {
        return getElementTexts(tags);
    }

    public boolean canAddTag() {
        return isElementVisible(addTagButton);
    }

    public ObjectPropertiesPage addComment(String comment) {
        int currentComments = commentItems.size();

        waitForElementVisible(addCommentButton).click();
        IpeEditor.getInstance(browser).setText(comment);

        Predicate<WebDriver> commentAdded = browser ->
                waitForCollectionIsNotEmpty(commentItems).size() > currentComments;
        Graphene.waitGui().until(commentAdded);

        return this;
    }

    public Collection<String> getComments() {
        return waitForCollectionIsNotEmpty(commentItems)
                .stream()
                .map(e -> e.findElement(By.className("content")))
                .map(WebElement::getText)
                .collect(toList());
    }

    public void deleteObject() {
        waitForElementVisible(BY_DELETE_BUTTON, getRoot()).click();
        waitForElementVisible(BY_CONFIRM_DELETE_BUTTON, browser).click();
        waitForDataPageLoaded(browser);
    }

    public ObjectPropertiesPage deleteObjectButCancel() {
        waitForElementVisible(BY_DELETE_BUTTON, getRoot()).click();
        waitForElementVisible(BY_CANCEL_DELETE_BUTTON, browser);
        return this;
    }

    public ObjectPropertiesPage changeFolder(String targetFolder) {
        waitForElementVisible(changeFolderButton).click();
        IpeEditor.getInstance(browser).setText(targetFolder);

        Predicate<WebDriver> folderChanged = browser ->
                targetFolder.equals(browser.findElement(By.cssSelector(".folderText a")).getText());
        Graphene.waitGui().until(folderChanged);

        return this;
    }

    public void clickDataPageLink() {
        waitForElementVisible(dataPageLink).click();
    }

    public boolean isDeleteButtonDisabled() {
        WebElement deleteButton = waitForElementVisible(BY_DELETE_BUTTON, getRoot());

        // check that the button is truly non-clickable, i.e. that the delete
        // confirmation dialog does not appear
        deleteButton.click();
        waitForElementNotVisible(BY_CONFIRM_DELETE_BUTTON);

        return deleteButton.getAttribute("class").contains("disabled");
    }
}
