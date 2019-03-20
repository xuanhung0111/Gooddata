package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.support.FindBy;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.openqa.selenium.By.cssSelector;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;

public class ConfigurationPanelBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-item")
    protected List<ItemConfiguration> itemsConfiguration;

    @FindBy(className = "s-properties-unsupported")
    private WebElement propertiesUnsupported;

    public ItemConfiguration getItemConfiguration(final String item) {
        return waitForCollectionIsNotEmpty(itemsConfiguration).stream()
                .filter(input -> item.equals(input.getHeader()))
                .findFirst()
                .get();
    }

    public String getPropertiesUnsupported() {
        return waitForElementVisible(propertiesUnsupported).getText();
    }

    public List<String> getItemNames() {
        return waitForCollectionIsNotEmpty(itemsConfiguration).stream()
                .map(ItemConfiguration::getHeader)
                .collect(toList());
    }

    public ConfigurationPanelBucket expandConfigurationPanel() {
        if (isConfigurationPanelCollapsed()) {
            getRoot().click();
            waitForElementAttributeNotContainValue(getRoot(), "class", "bucket-collapsed");
        }
        assertFalse(isConfigurationPanelCollapsed(), "Configuration Panel Bucket should be expanded");
        return this;
    }

    public ConfigurationPanelBucket openColorConfiguration() {
        waitForElementPresent(cssSelector(".s-config-section-colors_section"), getRoot()).click();
        return this;
    }

    public ConfigurationPanelBucket resetColor() {
        waitForElementPresent(cssSelector(".s-reset-colors-button.s-reset_colors"), browser).click();
        return this;
    }

    /**
     * @param item : A color position in color palette
     * @return : AnalysisPage with a color is selected
     */
    public ConfigurationPanelBucket getItemColorPicker(int item) {
        waitForElementPresent(cssSelector(String.format(".s-color-list-item-%s", item)), browser).click();
        return this;
    }

    /**
     * @param item : Input position of list color items to match color column of chart.
     * @return AnalysisPage with a color is selected
     */
    public ConfigurationPanelBucket openColorPicker(int item) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                .findElements(cssSelector(".gd-color-config-item-sample")));
        list.get(item).click();
        return this;
    }

    public ConfigurationPanelBucket setColorCustomPicker(String hexColor) {
        waitForElementPresent(cssSelector(".s-color-picker-hex input.gd-input-field"), browser).clear();
        waitForElementPresent(cssSelector(".s-color-picker-hex input.gd-input-field"), browser).sendKeys(hexColor);
        return this;
    }

    public ConfigurationPanelBucket openColorCustomPicker() {
        waitForElementPresent(cssSelector(".s-custom-section-button"), browser).click();
        return this;
    }

    private boolean isConfigurationPanelCollapsed() {
        return getRoot().getAttribute("class").contains("bucket-collapsed");
    }

    public enum Items {
        Y_AXIS("Y-Axis"),
        COLORS("Colors"),
        X_AXIS("X-Axis"),
        LEGEND("Legend"),
        CANVAS("Canvas");

        private String item;

        Items(String item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return item;
        }
    }

    public class ItemConfiguration extends AbstractFragment {

        @FindBy(className = "adi-bucket-item-header")
        private WebElement header;

        @FindBy(className = "s-checkbox-toggle")
        private WebElement switchToggle;

        @FindBy(xpath = "//span[.='Min']/following-sibling::input")
        private WebElement minInputField;

        @FindBy(xpath = "//span[.='Max']/following-sibling::input")
        private WebElement maxInputField;

        public String getHeader() {
            return waitForElementVisible(header).getText();
        }

        public ItemConfiguration expandConfiguration() {
            if (isConfigurationCollapsed()) {
                clickItemHeader();
            }
            return this;
        }

        public ItemConfiguration switchOff() {
            if (isToggleTurnOn()) {
                clickToggle();
                waitForElementAttributeNotContainValue(switchToggle, "class", "bucket-collapsed");
            }
            assertFalse(isToggleTurnOn(), "Item Configuration should be expanded");
            return this;
        }

        public ItemConfiguration setMinMaxValueOnAxis(String minValue, String maxValue) {
            waitForElementVisible(minInputField).clear();
            minInputField.sendKeys(minValue + Keys.ENTER);
            waitForElementVisible(maxInputField).clear();
            maxInputField.sendKeys(maxValue + Keys.ENTER);
            return this;
        }

        private boolean isConfigurationCollapsed() {
            return waitForElementVisible(header).getAttribute("class").contains("collapsed");
        }

        private void clickItemHeader() {
            waitForElementVisible(header).click();
        }

        private boolean isToggleTurnOn() {
            return waitForElementPresent(switchToggle).isSelected();
        }

        private void clickToggle() {
            waitForElementPresent(switchToggle).findElement(BY_PARENT).click();
        }
    }
}
