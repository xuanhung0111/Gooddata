package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;

public class SubFilterContainer extends AbstractFragment {

    private static final String ATTRIBUTE_VALUES_SUB_FILTER = "Attribute Values";
    private static final String DATE_RANGE_SUB_FILTER = "Date Range";
    private static final String VARIABLE_SUB_FILTER = "Variable";

    @FindBy(className = "s-btn-filter_metric_by")
    private WebElement filterMetricByButton;

    @FindBy(className = "yui3-c-subfiltereditor")
    private List<WebElement> subFilters;

    public void addSubFilterByAttributeValues(String attribute, String...values) {
        selectType(ATTRIBUTE_VALUES_SUB_FILTER);

        SelectItemPopupPanel panel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser));

        panel.searchAndSelectItem(attribute).submitPanel();

        // After selecting attribute, the pop-up panel is still displayed.
        // In this case, we could continue to wait for it visible then select more attribute values.
        waitForFragmentVisible(panel).searchAndSelectItems(Arrays.asList(values))
                .submitPanel();
    }

    public void addSubFilterByDateRange(String attrDate, Time time) {
        selectType(DATE_RANGE_SUB_FILTER);
        searchAndSelectItem(attrDate);

        Graphene.createPageFragment(FloatingRangePanel.class,
                waitForElementVisible(By.className("t-floatingPanel"), browser))
                .selectTime(time);
        waitForElementVisible(By.cssSelector(".s-btn-select:not(.gdc-hidden):not(.disabled)"), browser).click();
    }

    public void addSubFilterByVariable(String variableName) {
        selectType(VARIABLE_SUB_FILTER);
        searchAndSelectItem(variableName);
    }

    public SubFilter getLatestSubFilter() {
        return getSubFilterByIndex(subFilters.size() - 1);
    }

    private SubFilter getSubFilterByIndex(int index) {
        return Graphene.createPageFragment(SubFilter.class,
                waitForElementVisible(waitForCollectionIsNotEmpty(subFilters).get(index)));
    }

    private void selectType(String menu) {
        waitForElementVisible(filterMetricByButton).click();
        SimpleMenu.getInstance(browser).select(menu);
    }

    private void searchAndSelectItem(String item) {
        Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser))
                .searchAndSelectItem(item)
                .submitPanel();
    }
}
