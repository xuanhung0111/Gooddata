package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class FilterWidget extends AbstractFragment {

    public static class FilterPanel extends AbstractFragment {

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

        @FindBy(css = ".yui3-c-simpleColumn-window")
        private WebElement scroller;

        @FindBy(css = ".yui3-c-simpleColumn-window .yui3-c-control")
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

    }

    @FindBy(xpath = "//div[contains(@class,'yui3-listfilterpanel')]")
    private FilterPanel panel;

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

}
