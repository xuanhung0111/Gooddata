package com.gooddata.qa.graphene.fragments.dashboards.menu;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DashboardMenu extends SimpleMenu {

    private static final By BY_DASHBOARD_SELECTOR_TITLE = By.xpath("a/span");

    @Override
    public int getItemsCount() {
        // Do not count the first item. It is a label named "SELECT DASHBOARD"
        return super.getItemsCount() - 1;
    }

    public Collection<String> getAllItemNames() {
        waitForAllItemsVisible();
        return Lists.newArrayList(Iterables.transform(Iterables.skip(items, 1), new Function<WebElement, String>() {
            @Override 
            public String apply(WebElement elem) {
                return elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title");
            }
        }));
    }

    public boolean selectDashboardByIndex(final int index) throws InterruptedException {
        return selectDashboardByPredicate(new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement e) {
                return Integer.valueOf(e.getAttribute("gdc:index")) == index;
            }
        });
    }

    public boolean selectDashboardByName(final String name) throws InterruptedException {
        return selectDashboardByPredicate(new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement e) {
                return name.equals(e.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title"));
            }
        });
    }

    private boolean selectDashboardByPredicate(Predicate<WebElement> predicate) throws InterruptedException {
        waitForAllItemsVisible();
        WebElement dashboard = Iterables.find(items, predicate, null);

        if (dashboard == null) {
            System.out.println("Dashboard not selected because it's not present!!!!");
            return false;
        }

        dashboard.findElement(BY_LINK).click();
        Thread.sleep(3000);
        waitForDashboardPageLoaded(browser);
        return true;
    }
}
