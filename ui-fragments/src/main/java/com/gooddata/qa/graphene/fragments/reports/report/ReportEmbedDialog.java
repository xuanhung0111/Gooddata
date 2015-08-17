package com.gooddata.qa.graphene.fragments.reports.report;

import java.util.Optional;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.assertTrue;

public class ReportEmbedDialog extends AbstractFragment {

    @FindBy(css = ".embedUriArea textarea")
    private WebElement embedHtmlCodeArea;

    @FindBy(css = ".embedUriPlainArea .input")
    private WebElement embedUriArea;

    @FindBy(css = ".c-embedDialog .s-btn-close")
    private WebElement embedDialogCloseButton;

    @FindBy(xpath = "//label[contains(text(), 'Set URL Parameter Filters')]")
    private WebElement filterContentLabel;

    private By SELECT_ATTRIBUTE_BUTTON_LOCATOR = By.cssSelector(".s-btn-select_attribute___");
    private By ADD_FILTER_BUTTON_LOCATOR = By.cssSelector(".s-btn-add_filter");
    private By ATTRIBUTE_VALUES_TEXT_BOX_LOCATOR = By.xpath("//.[contains(@class, 'attributeValue')]/input");

    public String getHtmlCode() {
        return waitForElementVisible(embedHtmlCodeArea).getAttribute("value");
    }

    public String getEmbedUri() {
        return waitForElementVisible(embedUriArea).getAttribute("value");
    }

    public void closeEmbedDialog() {
        waitForElementVisible(embedDialogCloseButton).click();
    }

    public void expandFiltersSection() {
        waitForElementVisible(filterContentLabel).click();
    }

    public void selectFilterAttribute(String attributeName, String... attributeValues) {
        if (attributeName.isEmpty())
            return;
        expandFiltersSection();
        for (String attributeValue : attributeValues) {
            waitForElementPresent(ADD_FILTER_BUTTON_LOCATOR, getRoot()).click();
            waitForElementVisible(SELECT_ATTRIBUTE_BUTTON_LOCATOR, getRoot()).click();
            SelectItemPopupPanel panel =
                    Graphene.createPageFragment(SelectItemPopupPanel.class,
                            waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser));
            panel.searchAndSelectItem(attributeName);
            Optional<WebElement> attributeValueInput =
                    getRoot().findElements(ATTRIBUTE_VALUES_TEXT_BOX_LOCATOR).stream()
                            .filter((WebElement input) -> input.getAttribute("value").contains("_wildcard"))
                            .findFirst();
            assertTrue(attributeValueInput.isPresent(), "Could not find the attribute value input!");
            attributeValueInput.get().sendKeys(attributeValue);
        }
        getRoot().click();
    }
}
