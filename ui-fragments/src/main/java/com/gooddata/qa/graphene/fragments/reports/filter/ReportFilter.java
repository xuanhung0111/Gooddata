package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;

import java.util.List;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.PromptFilterItem;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportFilter extends AbstractFragment {

    public static final By REPORT_FILTER_LOCATOR = id("filtersContainer");
    private static final By DELETE_FILTER_BUTTON_LOCATOR = By.className("s-btn-delete");

    @FindBy(css = ".s-attributeFilter")
    private WebElement attributeFilterLink;

    @FindBy(css = ".s-rankFilter")
    private WebElement rankFilterLink;

    @FindBy(css = ".s-rangeFilter")
    private WebElement rangeFilterLink;

    @FindBy(css = ".s-promptFilter")
    private WebElement promptFilterLink;

    @FindBy(css = ".s-btn-add_filter")
    private WebElement addFilterButton;

    @FindBy(className = "c-filterLine")
    private List<WebElement> existingFilters;

    public void addFilter(FilterItem filterItem) {
        clickAddFilter();

        if(filterItem instanceof AttributeFilterItem) {
            openAttributeFilterFragment().addFilter(filterItem);

        } else if (filterItem instanceof RankingFilterItem) {
            openRankingFilterFragment().addFilter(filterItem);

        } else if (filterItem instanceof RangeFilterItem) {
            openRangeFilterFragment().addFilter(filterItem);

        } else if (filterItem instanceof PromptFilterItem) {
            openPromptFilterFragment().addFilter(filterItem);

        } else {
            throw new IllegalArgumentException("Unknow filter item: " + filterItem);
        }
    }

    public ReportFilter clickAddFilter() {
        Optional.of(waitForElementVisible(addFilterButton))
                .filter(e -> !e.getAttribute("class").contains("disabled"))
                .ifPresent(WebElement::click);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractFilterFragment> T openExistingFilter(String filterName,
            FilterFragment returnFragment) {

        getFilterElement(filterName).click();

        return (T) Graphene.createPageFragment(returnFragment.getFragmentClass(),
                waitForElementVisible(returnFragment.getLocator(), browser));
    }

    public boolean hoverMouseToFilter(String filterName) {
        WebElement existingFilter = getFilterElement(filterName);

        new Actions(browser).moveToElement(existingFilter).perform();
        return isElementPresent(By.xpath("./ancestor::div[contains(@class,'filterLine_hover')]"), existingFilter);
    }

    public void deleteFilter(String filterName) {
        hoverMouseToFilter(filterName);
        waitForElementVisible(DELETE_FILTER_BUTTON_LOCATOR, browser).click();
    }

    public AttributeFilterFragment openAttributeFilterFragment() {
        return openFilterFragment(attributeFilterLink, FilterFragment.ATTRIBUTE_FILTER);
    }

    public PromptFilterFragment openPromptFilterFragment() {
        return openFilterFragment(promptFilterLink, FilterFragment.PROMPT_FILTER);
    }

    public RankingFilterFragment openRankingFilterFragment() {
        return openFilterFragment(rankFilterLink, FilterFragment.RANKING_FILTER);
    }

    private RangeFilterFragment openRangeFilterFragment() {
        return openFilterFragment(rangeFilterLink, FilterFragment.RANGE_FILTER);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractFilterFragment> T openFilterFragment(WebElement link,
            FilterFragment returnFragment) {

        waitForElementVisible(link).click();
        return (T) Graphene.createPageFragment(returnFragment.getFragmentClass(),
                waitForElementVisible(returnFragment.getLocator(), browser));
    }

    public WebElement getFilterElement(final String filterName) {
        return existingFilters.stream()
                .map(e -> e.findElement(By.className("text")))
                .filter(e -> filterName.equals(e.getText()))
                .findFirst()
                .get();
    }

    public enum FilterFragment {
        ATTRIBUTE_FILTER(AttributeFilterFragment.class, ".c-attributeFilterLineEditor"),
        RANKING_FILTER(RankingFilterFragment.class, ".c-rankFilterLineEditor"),
        RANGE_FILTER(RangeFilterFragment.class, ".c-rangeFilterLineEditor"),
        PROMPT_FILTER(PromptFilterFragment.class, ".c-promptFilterLineEditor");

        private Class<? extends AbstractFilterFragment> fragmentClass;
        private String locator;

        private FilterFragment(Class<? extends AbstractFilterFragment> fragmentClass, String locator) {
            this.fragmentClass = fragmentClass;
            this.locator = locator;
        }

        public Class<? extends AbstractFilterFragment> getFragmentClass() {
            return fragmentClass;
        }

        public By getLocator() {
            return By.cssSelector(locator);
        }
    }
}
