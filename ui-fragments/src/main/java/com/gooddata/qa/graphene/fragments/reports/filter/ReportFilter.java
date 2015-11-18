package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.PromptFilterItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportFilter extends AbstractFragment {

    private static final By ATTRIBUTE_FILTER_FRAGMENT_LOCATOR = By.cssSelector(".c-attributeFilterLineEditor");
    private static final By RANK_FILTER_FRAGMENT_LOCATOR = By.cssSelector(".c-rankFilterLineEditor");
    private static final By RANGE_FILTER_FRAGMENT_LOCATOR = By.cssSelector(".c-rangeFilterLineEditor");
    private static final By PROMPT_FILTER_FRAGMENT_LOCATOR = By.cssSelector(".c-promptFilterLineEditor");

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

    private ReportFilter clickAddFilter() {
        Optional.of(waitForElementVisible(addFilterButton))
                .filter(e -> !e.getAttribute("class").contains("disabled"))
                .ifPresent(WebElement::click);
        return this;
    }

    private AttributeFilterFragment openAttributeFilterFragment() {
        return openFilterFragment(attributeFilterLink, ATTRIBUTE_FILTER_FRAGMENT_LOCATOR,
                AttributeFilterFragment.class);
    }

    private RankingFilterFragment openRankingFilterFragment() {
        return openFilterFragment(rankFilterLink, RANK_FILTER_FRAGMENT_LOCATOR, RankingFilterFragment.class);
    }

    private RangeFilterFragment openRangeFilterFragment() {
        return openFilterFragment(rangeFilterLink, RANGE_FILTER_FRAGMENT_LOCATOR, RangeFilterFragment.class);
    }

    private PromptFilterFragment openPromptFilterFragment() {
        return openFilterFragment(promptFilterLink, PROMPT_FILTER_FRAGMENT_LOCATOR, PromptFilterFragment.class);
    }

    private <T extends AbstractFilterFragment> T openFilterFragment(WebElement link, By locator, Class<T> clazz) {
        waitForElementVisible(link).click();
        return Graphene.createPageFragment(clazz, waitForElementVisible(locator, browser));
    }
}
