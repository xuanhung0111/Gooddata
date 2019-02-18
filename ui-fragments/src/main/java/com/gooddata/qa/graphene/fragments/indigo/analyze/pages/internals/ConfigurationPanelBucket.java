package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

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

    public class ItemConfiguration extends AbstractFragment {

        @FindBy(className = "adi-bucket-item-header")
        private WebElement header;

        public String getHeader() { return waitForElementVisible(header).getText(); }
    }
}
