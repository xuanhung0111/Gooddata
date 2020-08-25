package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.indigo.analyze.CanvasSelect;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.openqa.selenium.By.className;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;

public class ConfigurationPanelBucket extends AbstractBucket {

    @FindBy(className = "adi-bucket-item")
    protected List<ItemConfiguration> itemsConfiguration;

    @FindBy(className = "s-properties-unsupported")
    private WebElement propertiesUnsupported;

    @FindBy(className = "s-config-section-colors_section")
    private ItemConfiguration itemColorsSection;

    @FindBy(className = "s-config-section-legend_section")
    private ItemConfiguration itemLegendSection;

    @FindBy(className = "s-config-section-map_section")
    private ItemConfiguration itemMapSection;

    @FindBy(className = "s-config-section-points_section")
    private ItemConfiguration itemPointSection;

    public ItemConfiguration getItemConfiguration(final String item) {
        return waitForCollectionIsNotEmpty(itemsConfiguration).stream()
                .filter(input -> item.equals(input.getHeader()))
                .findFirst()
                .get();
    }

    public ItemConfiguration openColorConfiguration() {
        return waitForFragmentVisible(itemColorsSection).expandConfiguration();
    }

    public ItemConfiguration openLegendConfiguration() {
        return waitForFragmentVisible(itemLegendSection).expandConfiguration();
    }

    public ItemConfiguration openMapConfiguration() {
        return waitForFragmentVisible(itemMapSection).expandConfiguration();
    }

    public ItemConfiguration openPointsConfiguration() {
        return waitForFragmentVisible(itemPointSection).expandConfiguration();
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

        private final By BY_CANVAS_SELECT = By.className("adi-bucket-dropdown");

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

        @FindBy(className = "icon-navigatedown")
        private WebElement navigateDownIcon;

        @FindBy(css = ".input-checkbox-label input")
        private WebElement groupPointItem;

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

        public boolean isPointsGroupChecked() {
            return waitForElementPresent(groupPointItem).isSelected();
        }

        public boolean isPointsGroupDisabled() {
            return isElementDisabled(waitForElementPresent(groupPointItem));
        }

        public boolean isToggleTurnOn() {
            return waitForElementPresent(switchToggle).isSelected();
        }

        public  boolean isToggleDisabled() {
            return isElementDisabled(waitForElementPresent(switchToggle));
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

        public CanvasSelect getCanvasSelect() {
            waitForElementVisible(className("icon-navigatedown"), getRoot()).click();
            return Graphene.createPageFragment(CanvasSelect.class,
                waitForElementVisible(BY_CANVAS_SELECT, browser));
        }

        public ConfigurationDialog openItemsSelectedInConfiguration() {
            waitForElementVisible(className("icon-navigatedown"), getRoot()).click();
            return ConfigurationDialog.getInstance(browser);
        }

        public ConfigurationDialog openPointsSizeSelected(int index) {
            // Index = 0 -> get smallest point
            // Index = 1 -> get largest point
            getRoot().findElements(By.className("adi-bucket-inputfield")).get(index).click();
            return ConfigurationDialog.getInstance(browser);
        }
        
        public ItemConfiguration collapseItemsSelected() {
            waitForElementVisible(className("icon-navigateup"), getRoot()).click();
            return this;
        }

        public String getDefaultValueInDropDownList() {
            return waitForElementVisible(className("dropdown-button"), getRoot()).getText();
        }

        public String getDefaultSmallestSize() {
            return getRoot().findElements(By.className("adi-bucket-inputfield")).get(0).getText();
        }

        public String getDefaultLargestSize() {
            return getRoot().findElements(By.className("adi-bucket-inputfield")).get(1).getText();
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
