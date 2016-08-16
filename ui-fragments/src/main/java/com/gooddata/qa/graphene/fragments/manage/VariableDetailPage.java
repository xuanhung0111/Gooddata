package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.util.stream.Collectors.toList;
import static java.lang.Integer.parseInt;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class VariableDetailPage extends ObjectPropertiesPage {

    private static final By LOCATOR = By.cssSelector("#p-objectPage.s-displayed");

    private static final By BY_VARIABLES_PAGE_LINK = By.cssSelector("#p-objectPage .interpolateProject");

    private static final By BY_NAME_INPUT = By.cssSelector(".s-name-ipe-editor input");
    private static final By BY_SAVE_NAME_BUTTON = By.cssSelector(".s-name-ipe-editor .s-ipeSaveButton");

    private static final By BY_DEFAULT_NUMERIC_VALUE_INPUT = By.cssSelector(".defaultValue input");

    private static final By BY_SAVE_CHANGES_BUTTON = By.cssSelector(".yui3-c-button-showInline.s-btn-save_changes");

    @FindBy(className = "s-name-ipe-placeholder")
    private WebElement nameTag;

    @FindBy(className = "usersTable")
    private UserSpecificTable userSpecificTable;

    public static VariableDetailPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(VariableDetailPage.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public String createNumericVariable(NumericVariable variable) {
        enterName(variable.getName())
                .selectVariableType(VariableTypes.NUMERICAL_VARIABLE)
                .setDefaultNumericValue(variable.getDefaultNumber())
                .setUserSpecificNumericValue(variable.getUserSpecificNumber())
                .saveChange();

        return getVariableUri();
    }

    public String createFilterVariable(AttributeVariable variable) {
        enterName(variable.getName())
                .selectVariableType(VariableTypes.FILTERED_VARIABLE)
                .selectAttribute(variable.getAttribute())
                .selectDefaultAttributeValues(variable.getAttributeValues())
                .selectUserSpecificAttributeValues(variable.getUserSpecificValues())
                .saveChange();

        return getVariableUri();
    }

    public VariableDetailPage setDefaultNumericValue(int value) {
        WebElement defaultValueInput = waitForElementVisible(BY_DEFAULT_NUMERIC_VALUE_INPUT, getRoot());
        defaultValueInput.clear();
        defaultValueInput.sendKeys(String.valueOf(value));
        // Should press TAB key here to make default value effect and save changes button enable
        defaultValueInput.sendKeys(Keys.TAB);

        return this;
    }

    public VariableDetailPage selectUserSpecificAttributeValues(String userProfile, Collection<String> values) {
        waitForFragmentVisible(userSpecificTable)
                .selectUserSpecificAttributeValues(userProfile, values);
        return this;
    }

    public VariableDetailPage saveChange() {
        waitForElementVisible(BY_SAVE_CHANGES_BUTTON, getRoot()).click();
        waitForElementNotPresent(BY_SAVE_CHANGES_BUTTON);
        return this;
    }

    public VariablesPage goToVariablesPage() {
        waitForElementVisible(BY_VARIABLES_PAGE_LINK, getRoot()).click();
        return Graphene.createPageFragment(VariablesPage.class,
                waitForElementVisible(By.cssSelector(VariablesPage.CSS_CLASS), browser));
    }

    public Collection<String> getDefaultAttributeValues() {
        return Stream.of(waitForElementVisible(By.cssSelector(".filterAnswer .answer"), getRoot()).getText().split(","))
                .map(value -> value.trim())
                .collect(toList());
    }

    public Collection<String> getUserSpecificAttributeValues(String userProfileUri) {
        return waitForFragmentVisible(userSpecificTable).getUserAttributeValues(userProfileUri);
    }

    public int getDefaultNumericValue() {
        return parseInt(waitForElementVisible(BY_DEFAULT_NUMERIC_VALUE_INPUT, getRoot()).getAttribute("value"));
    }

    public int getUserSpecificNumericValue(String userProfileUri) {
        return waitForFragmentVisible(userSpecificTable).getUserNumericValue(userProfileUri);
    }

    private VariableDetailPage enterName(String name) {
        if (!isElementPresent(BY_NAME_INPUT, browser)) {
            waitForElementVisible(nameTag).click();
        }

        final WebElement nameInput = waitForElementVisible(BY_NAME_INPUT, browser);
        nameInput.clear();
        nameInput.sendKeys(name);

        waitForElementVisible(BY_SAVE_NAME_BUTTON, browser).click();
        return this;
    }

    private VariableDetailPage selectVariableType(VariableTypes type) {
        final WebElement typeRadio = waitForElementVisible(type.getLocator(), getRoot());

        if (!typeRadio.isSelected()) typeRadio.click();
        return this;
    }

    private VariableDetailPage selectAttribute(String attribute) {
        waitForElementVisible(By.className("s-btn-select_attribute"), getRoot()).click();

        SelectItemPopupPanel.getInstance(browser)
                .searchAndSelectItem(attribute)
                .submitPanel();

        return this;
    }

    private VariableDetailPage selectDefaultAttributeValues(Collection<String> values) {
        if (values.isEmpty()) {
            return this;
        }

        waitForElementVisible(By.className("s-btn-edit"), getRoot()).click();
        SelectItemPopupPanel.getInstance(browser)
                .clearAllItems()
                .searchAndSelectItems(values)
                .submitPanel();

        return this;
    }

    private VariableDetailPage selectUserSpecificAttributeValues(Map<String, Collection<String>> userSpecificValues) {
        if (userSpecificValues.isEmpty()) {
            return this;
        }

        for (String user : userSpecificValues.keySet()) {
            waitForFragmentVisible(userSpecificTable).selectUserSpecificAttributeValues(user, userSpecificValues.get(user));
        }

        return this;
    }

    private VariableDetailPage setUserSpecificNumericValue(Map<String, Integer> userSpecificValue) {
        if (userSpecificValue.isEmpty()) {
            return this;
        }

        for (String user : userSpecificValue.keySet()) {
            waitForFragmentVisible(userSpecificTable).setUserSpecificNumericValue(user, userSpecificValue.get(user));
        }

        return this;
    }

    private String getVariableUri() {
        return browser.getCurrentUrl().split("objectPage\\|")[1].split("\\|\\|")[0];
    }

    public static class UserSpecificTable extends AbstractTable {

        private static final By BY_SET_NUMERIC_VALUE_BUTTON = By.className("s-btn-set");
        private static final By BY_SET_NUMERIC_VALUE_INPUT = By.cssSelector(".s-btn-ipe-editor input");
        private static final By BY_OK_BUTTON = By.cssSelector(".s-btn-ipe-editor .s-btn-ok");

        private UserSpecificTable selectUserSpecificAttributeValues(String userProfileUri, Collection<String> values) {
            waitForElementVisible(By.className("s-btn-choose"), getUserRow(userProfileUri)).click();

            SelectItemPopupPanel.getInstance(browser)
                    .clearAllItems()
                    .searchAndSelectItems(values)
                    .submitPanel();

            return this;
        }

        private UserSpecificTable setUserSpecificNumericValue(String userProfileUri, int value) {
            waitForElementVisible(BY_SET_NUMERIC_VALUE_BUTTON, getUserRow(userProfileUri)).click();
            waitForElementVisible(BY_SET_NUMERIC_VALUE_INPUT, browser).sendKeys(String.valueOf(value));
            waitForElementVisible(BY_OK_BUTTON, browser).click();

            return this;
        }

        private Collection<String> getUserAttributeValues(String userProfileUri) {
            return Stream.of(waitForElementVisible(By.cssSelector(".listVal"),
                            getUserRow(userProfileUri)).getText().split(","))
                    .map(value -> value.trim())
                    .collect(toList());
        }

        private int getUserNumericValue(String userProfileUri) {
            return parseInt(waitForElementVisible(By.cssSelector(".numberVal"), getUserRow(userProfileUri)).getText());
        }

        private WebElement getUserRow(String userProfileUri) {
            return getRows().stream()
                    .filter(r -> r.findElement(By.cssSelector(".user a")).getAttribute("href").contains(userProfileUri))
                    .findFirst()
                    .get();
        }
    }

    private static enum VariableTypes {
        NUMERICAL_VARIABLE("promptTypeScalar"),
        FILTERED_VARIABLE("promptTypeFilter");

        private String locator;

        private VariableTypes(String locator) {
            this.locator = locator;
        }

        private By getLocator() {
            return By.id(locator);
        }
    }
}
