package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class AttributeFilter extends AbstractReactDropDown {

    @FindBy(className = "button-title")
    private WebElement buttonTitle;

    @FindBy(className = "button-subtitle")
    private WebElement buttonText;

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".attribute-filter-button";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .attributevalues-list";
    }

    @Override
    protected String getNoMatchingDataMessageCssSelector() {
        return ".gd-list-noResults";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not(.is-header):not(.s-attribute-filter-dropdown-configuration-button)";
    }

    @Override
    protected void waitForSelectionIsApplied(String name) {
        // ignore this in attribute filter
    }

    public AttributeFilter selectByNames(String... names) {
        Stream.of(names).forEach(name -> {
            selectByName(name);
            apply();
        });
        return this;
    }

    public void selectAllValues() {
        ensureDropdownOpen();
        waitForElementVisible(className("s-select_all"), browser).click();
        apply();
    }

    public AttributeFilter clearAllCheckedValues() {
        ensureDropdownOpen();
        waitForElementVisible(cssSelector(".s-clear"), browser).click();
        return this;
    }

    public String getTitle() {
        waitForElementVisible(buttonTitle);
        return buttonTitle.getText();
    }

    public String getSelection() {
        waitForElementVisible(buttonText);
        return buttonText.getText();
    }

    public String getSelectedItems() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items"))).getText();
    }

    public String getSelectedItemsCount() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items-count"))).getText();
    }

    public boolean isActive() {
        return waitForElementPresent(cssSelector(getDropdownButtonCssSelector()), getRoot()).getAttribute("class")
                .contains("is-active");
    }

    public List<String> getFilterInnerMessage() {
        getActions().moveToElement(waitForElementVisible(By.className("gd-filtered-message"), browser)).perform();
        return Arrays.asList(getBubbleMessage(browser).replace("Items are filtered by: ", "").split(",\\s").clone());
    }

    public String getFilterMessage() {
        waitForElementVisible(By.className("gd-filtered-message"), browser).getText();
        return waitForElementVisible(By.className("gd-filtered-message"), browser).getText();
    }

    public void apply() {
        waitForElementVisible(cssSelector("button.s-apply_button"), browser).click();
    }

    public boolean isConfigurationDisplay() {
        return isElementPresent(By.className("attribute-filter-dropdown-configuration-button"), browser);
    }

    public AttributeFilter waitForLoading() {
        Graphene.waitGui().until(browser -> !isElementPresent(By.className("s-loading"), buttonText));
        return this;
    }

    public AttributeFilterConfiguration getAttributeFilterConfiguration() {
        return Graphene.createPageFragment(AttributeFilter.AttributeFilterConfiguration.class,
                waitForElementVisible(className("attribute-filter-dropdown-configuration-button"), browser));
    }

    public AttributeFilter setDependentFilter(String attribute, String... connectingAttribute) {
        AttributeFilterConfiguration attributeFilterConfiguration = getAttributeFilterConfiguration();
        attributeFilterConfiguration.selectAttributeByName(attribute);
        if (connectingAttribute.length != 0) {
            attributeFilterConfiguration.getConnectingAttribute().selectByName(connectingAttribute[0]);
        }
        attributeFilterConfiguration.save();
        return this;
    }

    public static class AttributeFilterConfiguration extends AbstractReactDropDown {

        @Override
        protected String getDropdownButtonCssSelector() {
            return ".s-attribute-filter-dropdown-configuration-button";
        }

        @Override
        protected String getDropdownCssSelector() {
            return ".s-attribute-filter-dropdown-configuration";
        }

        @Override
        protected String getListItemsCssSelector() {
            return ".s-attribute-filter-dropdown-configuration-item";
        }

        @Override
        protected String getSearchInputCssSelector() {
            return null;
        }

        @Override
        protected void waitForSelectionIsApplied(String name) {
            Graphene.waitGui().until(browser -> getElementByName(name).getAttribute("class").contains("is-selected"));
        }

        public void save() {
            waitForElementVisible(className("s-attribute-filter-dropdown-configuration-save-button"), browser).click();
            waitForElementNotPresent(cssSelector(this.getDropdownCssSelector()));
        }

        public void cancel() {
            waitForElementVisible(className("s-attribute-filter-dropdown-configuration-cancel-button"), browser).click();
            waitForElementNotPresent(cssSelector(this.getDropdownCssSelector()));
        }

        public ConnectingAttribute getConnectingAttribute() {
            return Graphene.createPageFragment(AttributeFilter.ConnectingAttribute.class,
                    waitForElementVisible(className("s-connecting-attributes-dropdown"), browser));
        }

        public boolean isItemEnabled(String attribute) {
            ensureDropdownOpen();
            boolean isEnable = getElementByName(attribute).findElement(By.cssSelector("input")).isEnabled();
            cancel();
            return isEnable;
        }
    }

    private static class ConnectingAttribute extends AbstractReactDropDown {

        @Override
        protected String getDropdownButtonCssSelector() {
            return "button";
        }

        @Override
        protected String getDropdownCssSelector() {
            return ".s-connecting-attributes-dropdown-body";
        }

        @Override
        protected String getSearchInputCssSelector() {
            return null;
        }

    }
}
