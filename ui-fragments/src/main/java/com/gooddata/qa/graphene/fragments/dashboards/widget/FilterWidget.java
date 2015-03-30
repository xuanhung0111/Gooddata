package com.gooddata.qa.graphene.fragments.dashboards.widget;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;

public class FilterWidget extends AbstractFragment {

    @FindBy(tagName = "button")
    private WebElement button;

    public void openPanel() {
        if (!isOpen()) {
            button.click();
        }
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

    public String getCurrentValue() {
        return getRoot().getText().split("\n")[1];
    }
}