package com.gooddata.qa.graphene.fragments.dashboards.widget;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.SelectionConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;

public class FilterWidget extends AbstractFragment {

    @FindBy(tagName = "button")
    private WebElement button;

    @FindBy(className = "titleContainer")
    private WebElement titleContainer;

    private static final By BY_TITLE_LABEL = By.cssSelector(".titleLabel span");
    private static final By BY_INPUT_LABEL = By.cssSelector("input");

    public FilterWidget openPanel() {
        if (!isOpen()) {
            waitForElementVisible(button).click();
        }
        return this;
    }

    public void closePanel() {
        if (isOpen()) {
            FilterPanel.getInstance(browser).close();
        }
    }

    public boolean isOpen() {
        return button.getAttribute("class").contains("active");
    }

    public <T extends FilterPanel> T getPanel(Class<T> clazz) {
        if (isOpen()) {
            return FilterPanel.getPanel(clazz, browser);
        }

        return null;
    }

    public List<String> getAllAttributeValues() {
        openPanel();
        return getPanel(AttributeFilterPanel.class).getAllAtributeValues();
    }

    public void changeTimeFilterValueByClickInTimeLine(String dataRange) {
        openPanel();
        getPanel(TimeFilterPanel.class).changeValueByClickInTimeLine(dataRange);
    }

    public void changeTimeFilterByEnterFromAndToDate(String startTime, String endTime) {
        openPanel();
        getPanel(TimeFilterPanel.class).changeValueByEnterFromDateAndToDate(startTime, endTime);
    }

    public void changeAttributeFilterValue(String... values) {
        openPanel();
        getPanel(AttributeFilterPanel.class).changeValues(values);
    }

    public void changeAttributeFilterValueInSingleMode(String value) {
        openPanel();
        getPanel(AttributeFilterPanel.class).changeValueInSingleMode(value);
    }

    public String getCurrentValue() {
        return getRoot().getText().split("\n")[1];
    }

    public void changeSelectionToOneValue() {
        WidgetConfigPanel configPanel = WidgetConfigPanel.
                openConfigurationPanelFor(this.getRoot(), browser);

        configPanel.getTab(WidgetConfigPanel.Tab.SELECTION,
                SelectionConfigPanel.class).changeSelectionToOneValue();

        configPanel.saveConfiguration();
    }

    public String getTitle() {
        return waitForElementVisible(titleContainer).findElement(BY_TITLE_LABEL).getText();
    }

    public void changeTitle(String title) {
        if (!getRoot().getAttribute("class").contains("yui3-c-filterdashboardwidget-selected")) {
            getRoot().click();
        }

        waitForElementVisible(titleContainer).findElement(BY_TITLE_LABEL).click();
        sleepTightInSeconds(2);
        WebElement inputElement = titleContainer.findElement(BY_INPUT_LABEL);
        inputElement.clear();
        inputElement.sendKeys(title);
        inputElement.sendKeys(Keys.ENTER);
    }
}
