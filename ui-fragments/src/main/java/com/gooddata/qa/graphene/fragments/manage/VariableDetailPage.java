package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.clickElementByVisibleLocator;
import static java.util.stream.Collectors.toList;
import static java.lang.Integer.parseInt;
import static java.util.Objects.isNull;

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
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class VariableDetailPage extends ObjectPropertiesPage {

    private static final By BY_DEFAULT_NUMERIC_VALUE_INPUT = By.cssSelector(".defaultValue input");
    private static final By BY_EDIT_ATTRIBUTE_VALUES_BUTTON = By.className("s-btn-edit");
    private static final By BY_SAVE_CHANGES_BUTTON = By.cssSelector(".yui3-c-button-showInline.s-btn-save_changes");

    @FindBy(className = UserSpecificTable.CLASS_NAME)
    private UserSpecificTable userSpecificTable;

    public static VariableDetailPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(VariableDetailPage.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public String createNumericVariable(NumericVariable variable) {
        changeName(variable.getName());

        selectVariableType(VariableTypes.NUMERICAL_VARIABLE)
                .setDefaultNumericValue(variable.getDefaultNumber())
                .setUserSpecificNumericValue(variable.getUserSpecificNumber())
                .saveChange();

        return getVariableUri();
    }

    public String createFilterVariable(AttributeVariable variable) {
        changeName(variable.getName());

        selectVariableType(VariableTypes.FILTERED_VARIABLE)
                .selectAttribute(variable.getAttribute())
                .selectDefaultAttributeValues(variable.getAttributeValues())
                .selectUserSpecificAttributeValues(variable.getUserSpecificValues())
                .saveChange();

        return getVariableUri();
    }

    public boolean canEditDefaultNumericValue() {
        return isNull(waitForElementVisible(BY_DEFAULT_NUMERIC_VALUE_INPUT, getRoot()).getAttribute("disabled"));
    }

    public VariableDetailPage setDefaultNumericValue(int value) {
        WebElement defaultValueInput = waitForElementVisible(BY_DEFAULT_NUMERIC_VALUE_INPUT, getRoot());
        defaultValueInput.clear();
        defaultValueInput.sendKeys(String.valueOf(value));
        // Should press TAB key here to make default value effect and save changes button enable
        defaultValueInput.sendKeys(Keys.TAB);

        return this;
    }

    public VariableDetailPage setUserSpecificNumericValue(String userProfileUri, int value) {
        waitForFragmentVisible(userSpecificTable.setUserSpecificNumericValue(userProfileUri, value));
        return this;
    }

    public boolean canEditDefaultAttributeValues() {
        return isElementVisible(BY_EDIT_ATTRIBUTE_VALUES_BUTTON, getRoot());
    }

    public SelectItemPopupPanel clickEditAttributeValuesButton() {
        waitForElementVisible(BY_EDIT_ATTRIBUTE_VALUES_BUTTON, getRoot()).click();
        return SelectItemPopupPanel.getInstance(browser);
    }

    public VariableDetailPage selectDefaultAttributeValues(Collection<String> values) {
        if (values.isEmpty()) {
            return this;
        }

        clickEditAttributeValuesButton()
                .clearAllItems()
                .searchAndSelectItems(values)
                .submitPanel();

        return this;
    }

    public VariableDetailPage selectUserSpecificAttributeValues(String userProfileUri, Collection<String> values) {
        waitForFragmentVisible(userSpecificTable)
                .selectUserSpecificAttributeValues(userProfileUri, values);
        return this;
    }

    public boolean isUserSpecificTableDisplayed() {
        return UserSpecificTable.isVisible(browser);
    }

    public VariableDetailPage saveChange() {
        waitForElementVisible(BY_SAVE_CHANGES_BUTTON, getRoot()).click();
        waitForElementNotPresent(BY_SAVE_CHANGES_BUTTON);
        return this;
    }

    public VariablesPage goToVariablesPage() {
        clickDataPageLink();
        return VariablesPage.getInstance(browser);
    }

    public Collection<String> getDefaultAttributeValues() {
        return Stream.of(waitForElementVisible(By.cssSelector(".filterAnswer .answer span"), getRoot())
                        .getAttribute("title").split(","))
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

    public boolean canSelectAttributeVariableType() {
        return VariableTypes.FILTERED_VARIABLE.canSelect(browser);
    }

    public boolean canSelectNumericVariableType() {
        return VariableTypes.NUMERICAL_VARIABLE.canSelect(browser);
    }

    public VariableDetailPage restoreUserSpecificValuesToDefault(String userProfileUri) {
        waitForFragmentVisible(userSpecificTable).restoreUserSpecificValuesToDefault(userProfileUri);
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

        public static final String CLASS_NAME = "usersTable";

        private static final By BY_BUTTON_CHOOSE = By.className("s-btn-choose");
        private static final By BY_ATTRIBUTE_VALUES = By.className("listVal");

        private static boolean isVisible(SearchContext searchContext) {
            return isElementVisible(By.className(CLASS_NAME), searchContext);
        }

        private UserSpecificTable selectUserSpecificAttributeValues(String userProfileUri, Collection<String> values) {
            clickElementByVisibleLocator(getUserRow(userProfileUri), BY_BUTTON_CHOOSE, BY_ATTRIBUTE_VALUES);

            SelectItemPopupPanel.getInstance(browser)
                    .clearAllItems()
                    .searchAndSelectItems(values)
                    .submitPanel();

            return this;
        }

        private UserSpecificTable setUserSpecificNumericValue(String userProfileUri, int value) {
            waitForElementVisible(By.className("s-btn-set"), getUserRow(userProfileUri)).click();
            IpeEditor.getInstance(browser).setText(String.valueOf(value));

            return this;
        }

        private UserSpecificTable restoreUserSpecificValuesToDefault(String userProfileUri) {
            waitForElementVisible(By.className("s-btn-default"), getUserRow(userProfileUri)).click();
            return this;
        }

        private Collection<String> getUserAttributeValues(String userProfileUri) {
            return Stream.of(waitForElementVisible(BY_ATTRIBUTE_VALUES, getUserRow(userProfileUri)).getText().split(","))
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

        private boolean canSelect(SearchContext searchContext) {
            return isNull(waitForElementVisible(getLocator(), searchContext).getAttribute("disabled"));
        }
    }
}
