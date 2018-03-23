package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

public abstract class AbstractSndPanel extends AbstractFragment {

    protected static final By LOCATOR = By.cssSelector(".c-sliceAndDice.visible");

    @FindBy(className = "sndViewSelector")
    private Select viewBySelect;

    @FindBy(className = "s-btn-done")
    private WebElement doneButton;

    /**
     * Represent for View Groups container (1st container) in whole panel.
     * This will be default Search Context for all child items inside
     *
     * @return root of View Groups container (1st container in whole SND)
     */
    protected abstract WebElement getViewGroupsContainerRoot();

    /**
     * Represent for Item container (2nd container) in whole panel.
     * This will be default Search Context for all child items inside
     *
     * @return root of Item container (2nd container in whole SND)
     */
    protected abstract WebElement getItemContainerRoot();

    /**
     * Represent for Item Detail container (3rd container) in whole panel.
     * This will be default Search Context for all child items inside
     *
     * @return root of Item Detail container (3rd container in whole SND)
     */
    protected abstract WebElement getItemDetailContainerRoot();

    protected WebElement findItemElement(String item) {
        final By itemLocator = By.className("s-grid-" + simplifyText(item));
        final By scrollBarLocator = By.className("scrollbar");

        if (!isElementPresent(scrollBarLocator, getItemContainerRoot())) {
            return getItemContainerRoot().findElement(itemLocator);
        }

        if (isItemFullyVisible(itemLocator)) {
            return getItemContainerRoot().findElement(itemLocator);
        }

        WebElement eraser = getItemContainerRoot().findElement(By.className("c-eraser"));
        if (isElementVisible(eraser)) {
            eraser.click();
            waitForElementVisible(scrollBarLocator, getItemContainerRoot());
        }

        if (!isItemFullyVisible(itemLocator)) {
            search(item);
        }
        return getItemContainerRoot().findElement(itemLocator);
    }

    public void switchViewBy(String view) {
        viewBySelect.selectByVisibleText(view);
    }

    public Collection<String> getViewGroups() {
        return getElementTexts(getViewGroupsContainerRoot().findElements(By.className("element")));
    }

    public AbstractSndPanel selectViewGroup(String group) {
        WebElement groupElement = getViewGroupsContainerRoot()
                .findElement(By.className("s-grid-" + simplifyText(group)));

        groupElement.click();
        waitForElementAttributeContainValue(groupElement, "class", "highlight");
        return this;
    }

    public void trySelectItem(String item) {
        WebElement checkbox = findItemElement(item).findElement(By.tagName("input"));
        if (checkbox.isSelected()) {
            throw new IllegalStateException("Item: " + item + " is already selected");
        }
        checkbox.click();
    }

    public AbstractSndPanel selectItem(String item) {
        return selectItem(item, WebElement::click);
    }

    public AbstractSndPanel selectItems(String... items) {
        Stream.of(items).forEach(this::selectItem);
        return this;
    }

    public AbstractSndPanel selectInapplicableItem(String item) {
        return selectItem(item, e -> getActions().keyDown(Keys.SHIFT).click(e)
                .keyUp(Keys.SHIFT).perform());
    }

    public AbstractSndPanel deselectItem(String item) {
        WebElement itemElement = findItemElement(item);
        WebElement checkbox = itemElement.findElement(By.tagName("input"));

        if (!checkbox.isSelected()) {
            throw new IllegalStateException("Item: " + item + " is already deselected");
        }

        checkbox.click();
        waitForElementAttributeNotContainValue(itemElement, "class", "sndInReport");
        return this;
    }

    public void done() {
        waitForElementEnabled(doneButton).click();
        waitForFragmentNotVisible(this);
    }


    // Use to search when expected item out of viewport.
    // Not recommend when item container shows lack of item because Scrollbar is not visible in this context.
    private void search(String item) {
        getItemContainerRoot().findElement(By.className("gdc-input")).sendKeys(item);
        waitForElementNotVisible(By.className("scrollbar"), getItemContainerRoot());
    }

    private AbstractSndPanel selectItem(String item, Consumer<WebElement> howToSelect) {
        WebElement itemElement = findItemElement(item);

        if (itemElement.findElement(By.tagName("input")).isSelected()) {
            throw new IllegalStateException("Item: " + item + " is already selected");
        }

        howToSelect.accept(itemElement);
        waitForElementAttributeContainValue(itemElement, "class", "sndInReport");
        return this;
    }

    private boolean isItemFullyVisible(By itemLocator) {
        if (isElementVisible(itemLocator, getItemContainerRoot())) {
            WebElement element = getItemContainerRoot().findElement(itemLocator);
            int elementBottomY = element.getLocation().getY() + element.getSize().getHeight();
            int containerBottomY = getItemContainerRoot().getLocation().getY() + getItemContainerRoot().getSize().getHeight(); 
            if (elementBottomY <= containerBottomY) {
                return true;
            }
        }
        return false;
    }
}
