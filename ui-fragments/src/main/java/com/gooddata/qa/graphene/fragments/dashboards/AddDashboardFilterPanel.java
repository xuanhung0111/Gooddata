package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.nonNull;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;

public class AddDashboardFilterPanel extends SelectItemPopupPanel {

    public void addAttributeFilter(DashAttributeFilterTypes type, String name, String... label) {
        waitForElementVisible(type.getLocator(), getRoot()).click();

        if (type == DashAttributeFilterTypes.ATTRIBUTE) {
            waitForElementVisible(By.className("dateCheckbox"), getRoot()).click();
        }

        searchAndSelectItem(name);

        if (label.length != 0) {
            selectAttributeLabel(label[0]);
        }

        submitPanel();
    }

    public void addTimeFilter(String dateDimension, DateGranularity dateGranularity, String timeLine) {
        if (nonNull(dateDimension)) {
            searchAndSelectItem(dateDimension);
            waitForElementVisible(By.className("s-btn-next"), getRoot()).click();
        }

        FilterPanel.getPanel(TimeFilterPanel.class, browser)
                .selectDateGranularity(dateGranularity)
                .selectTimeLine(timeLine)
                .submit();
    }

    private AddDashboardFilterPanel selectAttributeLabel(String label) {
        new Select(waitForElementVisible(By.cssSelector(".dfChanger select"), getRoot())).selectByVisibleText(label);
        return this;
    }

    public enum DashAttributeFilterTypes {
        ATTRIBUTE(".gdc-buttonGroup .first"),
        PROMPT(".gdc-buttonGroup .last");

        private String locator;

        private DashAttributeFilterTypes(String locator) {
            this.locator = locator;
        }

        public By getLocator() {
            return By.cssSelector(locator);
        }
    }
}
