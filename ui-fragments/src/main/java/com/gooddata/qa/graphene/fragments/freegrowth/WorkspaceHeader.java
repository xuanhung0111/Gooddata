package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.indigo.Header;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.Optional;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class WorkspaceHeader extends Header {

    public static final WorkspaceHeader getWorkspaceHeaderInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(WorkspaceHeader.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public void logout() {
        Graphene.waitGui()
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".gd-header-account")))
                .click();
    }

    public void verifyWorkspaceHeader() {
        List<WebElement> items = this.getMeasureMenuItems();
        assertThat("Number of header menu item must be 5", items.size() == 4);
        Optional<WebElement> dashboardMenu = this.getDashboardMenuItem();
        assertFalse(dashboardMenu.isPresent(), "Header menu should not contains dashboard item");
        Optional<WebElement> reportsMenu = this.getReportMenuItem();
        assertFalse(reportsMenu.isPresent(), "Header menu should not contains report item");
    }

    public void verifyLoadMenuActive() {
        Optional<WebElement> activeItem = this.getActiveMenuItem();
        assertTrue(activeItem.get().findElement(By.tagName("span")).getAttribute("class").contains("s-menu-load"),
                "Load is not default active menu");
    }

    public void verifyKpiDashboardMenuActive() {
        Optional<WebElement> activeItem = this.getActiveMenuItem();
        assertTrue(activeItem.get().findElement(By.tagName("span")).getAttribute("class").contains("s-menu-kpis"),
                "Kpi db is not default active menu");
    }
}
