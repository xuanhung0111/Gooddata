package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.openqa.selenium.By.className;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;

public class ConfigurationPanelBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-item")
    protected List<ItemConfiguration> itemsConfiguration;

    @FindBy(className = "s-properties-unsupported")
    private WebElement propertiesUnsupported;

    @FindBy(className = "s-config-section-colors_section")
    private ItemConfiguration itemColorsSection;

    public ItemConfiguration getItemConfiguration(final String item) {
        return waitForCollectionIsNotEmpty(itemsConfiguration).stream()
                .filter(input -> item.equals(input.getHeader()))
                .findFirst()
                .get();
    }

    public ItemConfiguration openColorConfiguration() {
        return waitForFragmentVisible(itemColorsSection).expandConfiguration();
    }

    public String getPropertiesUnsupported() {
        return waitForElementVisible(propertiesUnsupported).getText();
    }

    public List<String> getItemNames() {
        return waitForCollectionIsNotEmpty(itemsConfiguration).stream()
                .map(ItemConfiguration::getHeader)
                .collect(toList());
    }

    private boolean isConfigurationPanelCollapsed() {
        return getRoot().getAttribute("class").contains("bucket-collapsed");
    }

    public ConfigurationPanelBucket expandConfigurationPanel() {
        if (isConfigurationPanelCollapsed()) {
            getRoot().click();
            waitForElementAttributeNotContainValue(getRoot(), "class", "bucket-collapsed");
        }
        assertFalse(isConfigurationPanelCollapsed(), "Configuration Panel Bucket should be expanded");
        return this;
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

        @FindBy(className = "gd-color-reset-colors-section")
        private WebElement resetColor;

        @FindBy(className = "gd-colored-items-list")
        private ColorSnippet listColorSnippet;

        @FindBy(xpath = "//span[.='Min']/following-sibling::input")
        private WebElement minInputField;

        @FindBy(xpath = "//span[.='Max']/following-sibling::input")
        private WebElement maxInputField;

        public boolean isResetButtonVisibled() {
            return isElementVisible(getRoot().findElement(className("s-reset_colors")));
        }

        public boolean isScrollBarOnColourConfigurationVisible() {
            return isElementVisible(className("public_Scrollbar_main"), getRoot());
        }

        private boolean isConfigurationCollapsed() {
            return waitForElementVisible(header).getAttribute("class").contains("collapsed");
        }

        public boolean isSearchTextBoxOnColourConfigurationVisible() {
            return isElementVisible(className("gd-dropdown-searchfield"), getRoot());
        }

        public boolean isColorSnippetListVisible() {
            List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                    .findElements(className("icon-navigatedown")));
            return list.size() > 0;
        }

        private boolean isToggleTurnOn() {
            return waitForElementPresent(switchToggle).isSelected();
        }

        public String getResetButtonText() {
            return waitForElementVisible(resetColor).getText();
        }

        public String getNoDataOnResultSearchText() {
            return waitForElementVisible(className("gd-no-data"), getRoot()).getText();
        }

        public String getResultSearchText() {
            return waitForElementVisible(className("s-colored-items-list-item"), getRoot()).getText();
        }

        public String getHeader() {
            return waitForElementVisible(header).getText();
        }

        public String getColor(String color) {
            return waitForCollectionIsNotEmpty(getRoot().findElements(By.cssSelector(".gd-color-config-item-sample")))
                    .stream().filter(colorItem -> colorItem.getAttribute("class").contains(color))
                    .findFirst().get().getAttribute("style")
                    .replaceAll(".*background-color: ([^;]*);.*", "$1").replace(" ", "");
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

        public ItemConfiguration resetColor() {
            waitForElementVisible(resetColor).click();
            return this;
        }

        public ItemConfiguration searchItem(String input) {
            waitForElementVisible(className("gd-input-field"), getRoot()).sendKeys(input);
            return this;
        }

        public ColorSnippet openColorsPaletteDialog(String colorCss) {
            return waitForFragmentVisible(listColorSnippet).expandColorPaletteDialog(colorCss);
        }

        private void clickItemHeader() {
            waitForElementVisible(header).click();
        }

        private void clickToggle() {
            waitForElementPresent(switchToggle).findElement(BY_PARENT).click();
        }

        public class ColorSnippet extends AbstractFragment {

            public ColorSnippet expandColorPaletteDialog(String cssColor) {
                waitForElementVisible(className("s-color-" + cssColor), getRoot()).click();
                return this;
            }

            public ColorsPaletteDialog getColorsPaletteDialog() {
                return ColorsPaletteDialog.getInstance(browser);
            }
        }
    }
}
