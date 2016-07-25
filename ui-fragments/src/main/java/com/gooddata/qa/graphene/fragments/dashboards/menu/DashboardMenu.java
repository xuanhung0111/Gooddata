package com.gooddata.qa.graphene.fragments.dashboards.menu;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.function.Predicate;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.SimpleMenu;

public class DashboardMenu extends SimpleMenu {

    private static final By BY_DASHBOARD_SELECTOR_TITLE = By.xpath("a/span");

    public static DashboardMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DashboardMenu.class, waitForElementVisible(LOCATOR, searchContext));
    }

    @Override
    public int getItemsCount() {
        // Do not count the first item. It is a label named "SELECT DASHBOARD"
        return super.getItemsCount() - 1;
    }

    public Collection<String> getAllItemNames() {
        waitForAllItemsVisible();

        return items.stream()
            .skip(1)
            .map(e -> e.findElement(BY_DASHBOARD_SELECTOR_TITLE))
            .map(e -> e.getAttribute("title"))
            .collect(toList());
    }

    public void selectDashboardByIndex(final int index) {
        final Predicate<WebElement> indexPredicate =
                e -> Integer.valueOf(e.getAttribute("gdc:index")) == index;
        selectDashboardByPredicate(indexPredicate);
    }

    public void selectDashboardByName(final String name) {
        final Predicate<WebElement> namePredicate =
                e -> name.equals(e.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title"));
        selectDashboardByPredicate(namePredicate);
    }

    private void selectDashboardByPredicate(final Predicate<WebElement> predicate) {
        waitForAllItemsVisible();

        items.stream()
            .filter(predicate)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Dashboard not selected because it's not present!!!!"))
            .findElement(BY_LINK)
            .click();

        sleepTightInSeconds(3);
        waitForDashboardPageLoaded(browser);
    }
}
