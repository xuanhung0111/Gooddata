package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class MetricConfigPanel extends AbstractFragment {

    @FindBy(xpath = "//*[contains(@class,'metricRow')]/button")
    private WebElement metricSelect;

    @FindBy(xpath = "//*[contains(@class,'dateDimensionContainer')]/button")
    private WebElement dateDimensionSelect;

    @FindBy(css = ".whenFilter button")
    private WebElement whenFilter;

    @FindBy(css = ".periodSelect button")
    private WebElement periodSelect;

    @FindBy(css = ".linkExternalFilter input")
    private WebElement linkExternalFilter;

    public void selectMetric(String metric, String... dateDimension) {
        waitForElementVisible(metricSelect).click();
        Function<WebDriver, Boolean> popupDisplayed = browser -> browser.findElements(SelectItemPopupPanel.LOCATOR).size() > 1;
        Graphene.waitGui().until(popupDisplayed);

        Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1))
                .searchAndSelectItem(metric);

        if (dateDimension.length == 0) return;
        waitForElementVisible(dateDimensionSelect).click();
        Graphene.waitGui().until(popupDisplayed);
        Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1))
                .searchAndSelectItem(dateDimension[0]);
    }

    public boolean isWhenDropdownVisibled() {
        return waitForElementPresent(whenFilter).isDisplayed() && waitForElementPresent(periodSelect).isDisplayed();
    }

    public boolean isWhenDropdownEnabled() {
        return isElementEnabled(waitForElementPresent(whenFilter)) &&
                isElementEnabled(waitForElementPresent(periodSelect));
    }

    public boolean isLinkExternalFilterVisible() {
        return waitForElementPresent(linkExternalFilter).isDisplayed();
    }

    public boolean isLinkExternalFilterSelected() {
        return waitForElementPresent(linkExternalFilter).isSelected();
    }

    private boolean isElementEnabled(WebElement element) {
        return !element.getAttribute("class").contains("disabled");
    }
}
