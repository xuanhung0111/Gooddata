package com.gooddata.qa.graphene.fragments.dashboards.menu;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.openqa.selenium.support.FindBy;

public class DashboardMenu extends SimpleMenu {

    private static final By BY_DASHBOARD_SELECTOR_TITLE = By.xpath("a/span");

    @FindBy(css = ".s-dashboards-menu-item")
    protected List<WebElement> items;

    public Collection<String> getAllItemNames() {
        waitForAllItemsVisible();
        return Collections2.transform(items, new Function<WebElement, String>() {
            @Override 
            public String apply(WebElement elem) {
                return elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title");
            }
        });
    }

    public boolean selectDashboardByIndex(int index) throws InterruptedException {
        waitForAllItemsVisible();
        for (WebElement elem : items) {
            if (Integer.valueOf(elem.getAttribute("gdc:index")) != index) 
                continue;

            elem.findElement(BY_LINK).click();
            Thread.sleep(3000);
            waitForDashboardPageLoaded(browser);
            return true;
        }

        System.out.println("Dashboard not selected because it's not present!!!!");
        return false;
    }

    public boolean selectDashboardByName(String name) throws InterruptedException {
        waitForAllItemsVisible();
        for (WebElement elem : items) {
            if (!name.equals(elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title")))
                continue;

            elem.findElement(BY_LINK).click();
            Thread.sleep(3000);
            waitForDashboardPageLoaded(browser);
            return true;
        }

        System.out.println("Dashboard not selected because it's not present!!!!");
        return false;
    }
}
