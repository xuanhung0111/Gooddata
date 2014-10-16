package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

/**
 * Fragment represents a dashboard filter in view mode.
 * Support: Attribute Filter, Time Filter
 *
 */
public class FilterWidget extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'yui3-listfilterpanel')]")
    private FilterPanel panel;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-tabtimefilterpanel')]")
    private TimeFilterPanel timePanel;

    @FindBy(css = "button")
    private WebElement button;

    public void openPanel() {
        if (!isOpen()) {
            button.click();
        }
    }

    public void closePanel() {
        if (isOpen()) {
            panel.cancel.click();
        }
    }

    public boolean isOpen() {
        return button.getAttribute("class").contains("active");
    }

    public FilterPanel getPanel() {
        if (isOpen()) {
            return panel;
        } else {
            return null;
        }
    }

    public TimeFilterPanel getTimePanel() {
        if (isOpen()) return timePanel;
        return null;
    }

    public void changeTimeFilterValueByClickInTimeLine(String dataRange) {
        openPanel();
        getTimePanel().changeValueByClickInTimeLine(dataRange);
    }

    public void changeAttributeFilterValue(String... values) {
        openPanel();
        getPanel().changeValues(values);
    }

    public String getCurrentValue() {
        return getRoot().getText().split("\n")[1];
    }

    /**
     * Fragment represents a attribute filter panel.
     * Should use openPanel() before getting that panel.
     *
     */
    public static class FilterPanel extends AbstractFragment {

        @FindBy(css = ".yui3-c-simpleColumn-window")
        private WebElement scroller;

        @FindBy(xpath = "//div[contains(@class,'c-checkboxSelectOnly') and not(contains(@class,'gdc-hidden'))]")
        private List<FilterPanelRow> rows;

        @FindBy(css = ".clearVisible")
        private WebElement deselectAll;

        @FindBy(css = ".selectVisible")
        private WebElement selectAll;

        @FindBy(css = ".s-btn-cancel")
        private WebElement cancel;

        @FindBy(css = ".s-btn-apply")
        private WebElement apply;

        @FindBy(css = ".s-afp-input")
        private WebElement search;

        public List<FilterPanelRow> getRows() {
            return rows;
        }

        public WebElement getScroller() {
            return scroller;
        }

        public void waitForValuesToLoad() {
            waitForElementPresent(By.cssSelector(".yui3-c-simpleColumn-window.loaded"), browser);
        }

        public WebElement getSelectAll() {
            return selectAll;
        }

        public WebElement getDeselectAll() {
            return deselectAll;
        }

        public WebElement getCancel() {
            return cancel;
        }

        public WebElement getApply() {
            return apply;
        }

        public WebElement getSearch() {
            return search;
        }

        /**
         * support change some values of attribute filter
         * 
         * @param values
         */
        public void changeValues(String... values) {
            waitForValuesToLoad();
            waitForElementVisible(deselectAll).click();
            for (String value : values) {
                selectOneValue(value);
            }
            waitForElementVisible(apply).click();
        }

        private void selectOneValue(String value) {
            waitForElementVisible(search).clear();
            waitForElementVisible(search).sendKeys(value);
            waitForValuesToLoad();
            for (FilterPanelRow row : rows) {
                if (!value.equals(row.label.getText())) continue;
                row.checkbox.click();
                break;
            }
        }

        public static class FilterPanelRow extends AbstractFragment {

            @FindBy(css = ".selectOnly")
            private WebElement selectOnly;

            @FindBy(css = "label")
            private WebElement label;

            @FindBy(css = "input[type=checkbox]")
            private WebElement checkbox;

            public WebElement getSelectOnly() {
                return selectOnly;
            }

            public WebElement getCheckbox() {
                return checkbox;
            }

            public WebElement getLabel() {
                return label;
            }

            public boolean isSelected() {
                String checked = getCheckbox().getAttribute("checked");
                return checked != null && checked.contains("true");
            }
        }
    }

    /**
     * Fragment represents a time filter panel.
     * Should use openPanel() before getting that panel.
     *
     */
    public static class TimeFilterPanel extends AbstractFragment {

        private String timeLineLocator = "//div[text()='${time}']";

        @FindBy(css = ".s-btn-apply")
        private WebElement applyButton;

        /**
         * Support change value of time filter by clicking on a value time line
         * 
         * @param dataRange
         */
        public void changeValueByClickInTimeLine(String dataRange) {
            waitForElementVisible(By.xpath(timeLineLocator.replace("${time}", dataRange)), browser).click();
            waitForElementVisible(applyButton).click();
            waitForElementNotVisible(this.getRoot());
        }
    }
}