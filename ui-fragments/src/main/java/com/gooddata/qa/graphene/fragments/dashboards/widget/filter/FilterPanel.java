package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FilterPanel extends AbstractFragment {

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    private static final By ATTRIBUTE_PANEL_LOCATOR = By.className("yui3-listfilterpanel");
    private static final By TIME_PANEL_LOCATOR = By.className("yui3-c-tabtimefilterpanel");

    public static FilterPanel getInstance(WebDriver browser) {
        if (!browser.findElements(ATTRIBUTE_PANEL_LOCATOR).isEmpty()) {
            return Graphene.createPageFragment(FilterPanel.class,
                    waitForElementVisible(ATTRIBUTE_PANEL_LOCATOR, browser));
        }

        return Graphene.createPageFragment(FilterPanel.class,
                waitForElementVisible(TIME_PANEL_LOCATOR, browser));
    }

    public static <T extends FilterPanel> T getPanel(Class<T> clazz, WebDriver browser) {
        return Graphene.createPageFragment(clazz, waitForElementVisible(
                clazz == AttributeFilterPanel.class ? ATTRIBUTE_PANEL_LOCATOR : TIME_PANEL_LOCATOR, browser));
    }

    public void close() {
        waitForElementPresent(cancelButton);
        if (cancelButton.getAttribute("class").contains("gdc-hidden")) {
            return;
        }
        cancelButton.click();
    }

    public void submit() {
        waitForElementPresent(applyButton);
        if (applyButton.getAttribute("class").contains("gdc-hidden")) {
            return;
        }
        waitForElementVisible(applyButton).click();
    }
}
