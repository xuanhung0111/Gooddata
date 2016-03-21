package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.RangeFilterItem.RangeType;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.Ranking;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ContextMenu extends AbstractFragment {

    @FindBy(xpath = "//*[@id='ctxMenu']/div/ul/li")
    private List<WebElement> items;

    @FindBy(css = "#ctxMenu .yui3-menu-header")
    private List<WebElement> headers;

    public ContextMenu hoverToItem(String item) {
        new Actions(browser).moveToElement(getItemElement(item)).perform();
        return this;
    }

    public void selectItem(String item) {
        getItemElement(item).click();
    }

    public void addRankingFilter(Ranking ranking, int size) {
        addFilter(ranking, size);
    }

    public void addRangeFilter(RangeType rangeType, int rangeNumber) {
        addFilter(rangeType, rangeNumber);
    }

    public List<String> getGroupNames() {
        return headers.stream().map(e -> e.getText()).collect(Collectors.toList());
    }

    public void aggregateTableData(final AggregationType type, final String subItem) {
        aggregateTableData(type, subItem, true);
    }

    public void nonAggregateTableDate(final AggregationType type, final String subItem) {
        aggregateTableData(type, subItem, false);
    }

    private WebElement getItemElement(final String item) {
        return items.stream()
                .filter(e -> item.equals(e.getText().trim()))
                .findFirst()
                .get();
    }

    private SubItem getSubItem(final String subItem) {
        return Graphene.createPageFragment(SubItem.class,
                waitForElementVisible(By.cssSelector("#undefined:not(.yui3-menu-hidden)"), browser)
                .findElements(By.className("yui3-menuitem"))
                .stream()
                .filter(e -> subItem.equals(e.getText().toLowerCase().trim()))
                .findFirst()
                .get());
    }

    private void addFilter(Enum<?> filterType, int size) {
        if(!(filterType instanceof Ranking) && !(filterType instanceof RangeType)) {
            throw new IllegalArgumentException("Unknown filter type");
        }

        getSubItem(filterType.toString())
                .hover()
                .enterValue(size)
                .apply();
    }

    public static class SubItem extends AbstractFragment {

        @FindBy(tagName = "input")
        private WebElement input;

        @FindBy(tagName = "button")
        private WebElement okButton;

        public SubItem hover() {
            new Actions(browser).moveToElement(this.getRoot()).perform();
            return this;
        }

        public SubItem enterValue(int value) {
            waitForElementVisible(input).sendKeys(String.valueOf(value));
            return this;
        }

        public void apply() {
            waitForElementVisible(okButton).click();
        }
    }

    private void aggregateTableData(final AggregationType type, final String subItem, final boolean subItemState) {
        final WebElement subItemElement = hoverToItem(type.getType()).getSubItem(subItem.toLowerCase()).hover().getRoot();
        final boolean isChecked = subItemElement.getAttribute("class").contains("yui3-menuitem-checked");

        if(subItemState ^ isChecked)
            subItemElement.click();
    }

    public static enum AggregationType {
        SUM("Sum"),
        AVERAGE("Average"),
        MINIMUM("Minimum"),
        MAXIMUM("Maximum"),
        ROLLUP("Rollup"),
        MEDIAN("Median"),
        RUNNING("Running (sum, avg…)");

        private final String type;

        private AggregationType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
