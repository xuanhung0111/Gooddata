package com.gooddata.qa.graphene.fragments.profile;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.clickElementByVisibleLocator;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static java.util.stream.Collectors.toList;
import static java.lang.Integer.parseInt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.google.common.base.Predicate;

public class UserProfilePage extends AbstractFragment {

    public static final By USER_PROFILE_PAGE_LOCATOR = By.cssSelector("#p-profilePage");
    private static final By PHONE_LOCATOR = cssSelector(".phone div.value");
    private static final By COMPANY_LOCATOR = cssSelector(".company div.value");
    private static final By BY_MUF_DESCRIPTION = By.cssSelector(".mufView .description");
    private static final By BY_SET_MUF_LINK = By.cssSelector(".mufView a");

    @FindBy(css = ".fullname")
    private WebElement fullname;

    @FindBy(css = ".email a")
    private WebElement email;

    @FindBy(css = ".usersTable")
    private UserVariableTable userVariableTable;

    @FindBy(css = ".item")
    private List<WebElement> recentActivityItems;

    @FindBy(css = ".role")
    private WebElement role;

    @FindBy(className = "s-btn-save_changes")
    private WebElement saveChangesButton;

    @FindBy(css = "td.col-filtername")
    private Collection<WebElement> mufs;

    @FindBy(css = "td.col-filterexpression")
    private Collection<WebElement> mufExpressions;

    public static final UserProfilePage getInstance(SearchContext context) {
        return Graphene.createPageFragment(UserProfilePage.class, waitForElementVisible(id("p-profilePage"), context));
    }

    public PersonalInfo getUserInfo() {
        PersonalInfo info = new PersonalInfo()
                .withFullName(waitForElementVisible(fullname).getText())
                .withEmail(waitForElementVisible(email).getText());

        if (isElementVisible(COMPANY_LOCATOR, getRoot())) {
            info.withCompany(waitForElementVisible(COMPANY_LOCATOR, getRoot()).getText());
        }

        if (isElementVisible(PHONE_LOCATOR, getRoot())) {
            info.withPhoneNumber(waitForElementVisible(PHONE_LOCATOR, getRoot()).getText());
        }

        return info;
    }

    public List<String> getAllUserVariables() {
        return waitForFragmentVisible(userVariableTable).getAllItems();
    }

    public int getRecentActivityItems() {
        return recentActivityItems.size();
    }

    public boolean isItemDisplayedInRecentActivity(final String itemName) {
        return recentActivityItems.stream()
                .map(e -> e.findElement(By.cssSelector(".title")))
                .map(WebElement::getText)
                .filter(e -> e.equals(itemName))
                .findFirst()
                .isPresent();
    }

    public String getUserRole() {
        return waitForElementPresent(role).getText();
    }

    public UserProfilePage selectAttributeValuesFor(String variable, Collection<String> values) {
        waitForFragmentVisible(userVariableTable).selectAttributeValuesFor(variable, values);
        return this;
    }

    public Collection<String> getAttributeValuesOf(String variable) {
        return waitForFragmentVisible(userVariableTable).getAttributeValuesOf(variable);
    }

    public UserProfilePage setNumericValueFor(String variable, int value) {
        waitForFragmentVisible(userVariableTable).setNumericValueFor(variable, value);
        return this;
    }

    public int getNumericValueOf(String variable) {
        return waitForFragmentVisible(userVariableTable).getNumericValueOf(variable);
    }

    public UserProfilePage saveChanges() {
        waitForElementVisible(saveChangesButton).click();

        Predicate<WebDriver> saved = browser -> saveChangesButton.getAttribute("class").contains("disabled");
        Graphene.waitGui().until(saved);

        return this;
    }

    public boolean isMufSectionDisplayed() {
        return isElementPresent(BY_MUF_DESCRIPTION, getRoot());
    }

    public String getMufDescription() {
        return waitForElementVisible(BY_MUF_DESCRIPTION, getRoot()).getText();
    }

    public String getSetMufLink() {
        return waitForElementVisible(BY_SET_MUF_LINK, getRoot()).getAttribute("href");
    }

    public Collection<String> getAvailableMufs() {
        return getItemsFromMufTable(mufs);
    }

    public Collection<String> getAvailableMufExpressions() {
        return getItemsFromMufTable(mufExpressions);
    }

    private Collection<String> getItemsFromMufTable(Collection<WebElement> itemElements) {
        // If user is non-muf user, then the Muf table will not appear in User profile page.
        // So consider all muf items are empty in this case.

        // If user is muf user, Muf table always shows in User profile page.
        // Then we should wait for all muf items loaded to make sure no item is missing.
        if (!isElementPresent(By.className("mufTable"), getRoot())) return Collections.emptyList();
        return getElementTexts(waitForCollectionIsNotEmpty(itemElements));
    }

    public static class UserVariableTable extends AbstractTable {

        private static final By BY_BUTTON_CHOOSE = By.className("s-btn-choose");
        private static final By BY_VARIABLE_VALUES = By.className("promptValues");

        private List<String> getAllItems() {
            return getElementTexts(rows, e -> e.findElement(BY_LINK));
        }

        private void selectAttributeValuesFor(String variable, Collection<String> values) {
            clickElementByVisibleLocator(getRowOf(variable), BY_BUTTON_CHOOSE, BY_VARIABLE_VALUES);

            SelectItemPopupPanel.getInstance(browser)
                    .clearAllItems()
                    .searchAndSelectItems(values)
                    .submitPanel();
        }

        private Collection<String> getAttributeValuesOf(String variable) {
            return Stream.of(waitForElementVisible(BY_VARIABLE_VALUES, getRowOf(variable)).getText().split(","))
                    .map(value -> value.trim())
                    .collect(toList());
        }

        private int getNumericValueOf(String variable) {
            return parseInt(waitForElementVisible(BY_VARIABLE_VALUES, getRowOf(variable)).getText());
        }

        private void setNumericValueFor(String variable, int value) {
            waitForElementVisible(By.className("s-btn-set"), getRowOf(variable)).click();
            IpeEditor.getInstance(browser).setText(String.valueOf(value));
        }

        private WebElement getRowOf(final String variable) {
            return getRows().stream()
                    .filter(e -> variable.equals(e.findElement(BY_LINK).getText()))
                    .findFirst()
                    .get();
        }
    }
}
