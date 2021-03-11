package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class InsightConfiguration extends AbstractReactDropDown {

    private static final By BY_IFRAME = By.tagName("iframe");

    @FindBy(className = "s-options-menu-explore-insight")
    private WebElement exploreInsight;

    @FindBy(className = "s-options-menu-edit-insight")
    private WebElement editInsight;

    public static InsightConfiguration getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
            InsightConfiguration.class, waitForElementVisible(className("insight-configuration"), searchContext));
    }

    public AnalysisPage exploreInsight() {
        waitForElementVisible(exploreInsight).click();
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return AnalysisPage.getInstance(browser);
    }

    public AnalysisPage editInsight() {
        waitForElementVisible(editInsight).click();
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return AnalysisPage.getInstance(browser);
    }

    @Override
    protected String getDropdownCssSelector() {
        throw new UnsupportedOperationException("Unsupported getDropdownCssSelector() method");
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header']):not([class*='gd-list-item-separator'])";
    }
}
