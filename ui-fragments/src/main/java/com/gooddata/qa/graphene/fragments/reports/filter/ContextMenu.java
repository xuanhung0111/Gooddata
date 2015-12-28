package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

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
}
