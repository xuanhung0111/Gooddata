package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class EmbedDashboardDialog extends AbstractFragment {

    public static final By LOCATOR = By.className("c-embedDialog");

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Auto']")
    private WebElement autoOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Custom']")
    private WebElement customOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Web Tab']")
    private WebElement webTabOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='VisualForce Page']")
    private WebElement visualForcePageOption;

    @FindBy(xpath = "//div[contains(@class,'embedUriArea')]/textarea")
    private WebElement embedCode;

    @FindBy(xpath = "//div[contains(@class,'embedUriPlainArea')]/input")
    private WebElement previewURIInBrowser;

    @FindBy(xpath = "//label[contains(text(), 'Set URL Parameter Filters')]")
    private WebElement filterContentLabel;

    private By SELECT_ATTRIBUTE_BUTTON_LOCATOR = By.cssSelector(".s-btn-select_attribute___");
    private By ADD_FILTER_BUTTON_LOCATOR = By.cssSelector(".s-btn-add_filter");
    private By ATTRIBUTE_VALUES_TEXT_BOX_LOCATOR = By.cssSelector(".attributeValue input");

    public String getEmbedCode() {
        return embedCode.getAttribute("value");
    }

    public String getPreviewURI() {
        return previewURIInBrowser.getAttribute("value");
    }

    public void expandFiltersSection() {
        waitForElementVisible(filterContentLabel).click();
    }

    public EmbedDashboardDialog selectFilterAttribute(String attributeName, String... attributeValues) {
        if (attributeName.isEmpty())
            return this;
        if (!isElementVisible(ADD_FILTER_BUTTON_LOCATOR, getRoot())) {
            expandFiltersSection();
        }
        for (String attributeValue : attributeValues) {
            waitForElementPresent(ADD_FILTER_BUTTON_LOCATOR, getRoot()).click();
            waitForElementVisible(SELECT_ATTRIBUTE_BUTTON_LOCATOR, getRoot()).click();
            SelectItemPopupPanel panel =
                    Graphene.createPageFragment(SelectItemPopupPanel.class,
                            waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser));
            panel.searchAndSelectItem(attributeName).submitPanel();
            Optional<WebElement> attributeValueInput =
                    getRoot().findElements(ATTRIBUTE_VALUES_TEXT_BOX_LOCATOR).stream()
                            .filter((WebElement input) -> input.getAttribute("value").contains("_wildcard"))
                            .findFirst();
            assertTrue(attributeValueInput.isPresent(), "Could not find the attribute value input!");
            attributeValueInput.get().sendKeys(attributeValue);
        }
        getRoot().click();
        return this;
    }
}
