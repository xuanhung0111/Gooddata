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
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;

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
            waitForElementVisible(button).click();
        }
    }

    public boolean isOpen() {
        return button.getAttribute("class").contains("active");
    }

    /**
     * Open and get all attribute values in Attribute filter panel.
     * This action always close the panel in the end.
     * @return List of attribute values
     */
    public List<String> getAllAttributeValues() {
        try {
            return openPanel().getAttributeFilterPanel().getItems();

        } finally {
            closePanel();
        }
    }

    public FilterWidget changeTimeFilterValueByClickInTimeLine(String timeLine) {
        openPanel()
            .getTimeFilterPanel()
            .selectTimeLine(timeLine)
            .submit();
        return this;
    }

    public FilterWidget changeTimeFilterByEnterFromAndToDate(String startTime, String endTime) {
        openPanel()
            .getTimeFilterPanel()
            .changeValueByEnterFromDateAndToDate(startTime, endTime);
        return this;
    }

    /**
     * Change values permanently by click pencil icon to open and
     * Select attribute values in default Attribute filter panel.
     * Can use for both single and multiple mode.
     * @param values
     * @return
     */
    public FilterWidget editAttributeFilterValues(String... values) {
        DashboardEditWidgetToolbarPanel.openEditPanelFor(this.getRoot(), browser);
        getAttributeFilterPanel().changeValues(values);

        closePanel();
        return this;
    }

    /**
     * change values temporarily by open and select attribute values in review Attribute filter panel.
     * Can use for both single and multiple mode.
     * @param values
     * @return
     */
    public FilterWidget changeAttributeFilterValues(String... values) {
        openPanel().getAttributeFilterPanel().changeValues(values);

        closePanel();
        return this;
    }

    public String getCurrentValue() {
        return getRoot().getText().split("\n")[1];
    }

    public FilterWidget changeSelectionToOneValue() {
        return switchToOneOrMultipleValuesMode(false);
    }

    public FilterWidget changeSelectionToMultipleValues() {
        return switchToOneOrMultipleValuesMode(true);
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

    public AttributeFilterPanel getAttributeFilterPanel() {
        return AttributeFilterPanel.getInstance(browser);
    }

    public TimeFilterPanel getTimeFilterPanel() {
        return TimeFilterPanel.getInstance(browser);
    }

    private FilterWidget switchToOneOrMultipleValuesMode(boolean isMultiple) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(this.getRoot(), browser);

        if (isMultiple) {
            configPanel.getTab(WidgetConfigPanel.Tab.SELECTION, SelectionConfigPanel.class)
                    .changeSelectionToMultipleValues();
        } else {
            configPanel.getTab(WidgetConfigPanel.Tab.SELECTION, SelectionConfigPanel.class)
                    .changeSelectionToOneValue();
        }

        configPanel.saveConfiguration();
        return this;
    }
}
