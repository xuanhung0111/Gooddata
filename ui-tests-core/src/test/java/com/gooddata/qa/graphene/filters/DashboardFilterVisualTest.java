package com.gooddata.qa.graphene.filters;

import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;

public class DashboardFilterVisualTest extends GoodSalesAbstractTest {

    private static final By BY_SELECT_ONLY_LINK = By.className("selectOnly");

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-dashboard-filter-visual";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDoesNotDisplayOnlyAnchor() {
        List<WebElement> attributeElements = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel()
                .getItemElements();

        assertTrue(attributeElements.stream().allMatch(item -> !isSelectOnlyLinkDisplayedOn(item)),
                "'Select only' link is displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDisplaysOnlyAnchorOnHover() {
        List<WebElement> attributeElements = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel()
                .getItemElements();

        assertTrue(attributeElements.stream()
                .allMatch(item -> {
                    new Actions(browser).moveToElement(item).perform();
                    return isSelectOnlyLinkDisplayedOn(item);
                }),
                "'Select only' link is not displayed on hover");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectOneValueOnSelectOnlyClick() {
        List<WebElement> attributeElements = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel()
                .getItemElements();

        WebElement valueSelectOnly = attributeElements.get(0);
        clickSelectOnlyLinkOn(valueSelectOnly);
        assertTrue(isSelected(valueSelectOnly), "Value is not selected after click on 'Select only' link");

        assertTrue(attributeElements.subList(1, attributeElements.size())
                .stream()
                .allMatch(e -> !isSelected(e)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectAllFiltered() {
        AttributeFilterPanel panel = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel();

        panel.clearAllItems()
                .searchItem("on")
                .selectAllItems()
                .clearSearchInput();

        List<WebElement> attributeElements = waitForCollectionIsNotEmpty(panel.getItemElements());

        assertTrue(attributeElements.stream()
                .filter(e -> e.getText().toLowerCase().contains("on"))
                .allMatch(this::isSelected));

        assertTrue(attributeElements.stream()
                .filter(e -> !e.getText().toLowerCase().contains("on"))
                .allMatch(e -> !isSelected(e)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAllValuesAreSelectedByDefault() {
        List<WebElement> attributeElements = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel()
                .getItemElements();

        assertTrue(attributeElements.stream().allMatch(this::isSelected), "All items are not selected by default");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDeselectAllValues() {
        AttributeFilterPanel panel = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel();

        panel.clearAllItems();

        assertTrue(panel.getItemElements()
                .stream()
                .allMatch(e -> !isSelected(e)),
                "Some items are still selected after de-select all values");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectAllValues() {
        AttributeFilterPanel panel = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel();

        panel.selectAllItems();

        assertTrue(panel.getItemElements()
                .stream()
                .allMatch(this::isSelected),
                "All items are not selected");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testValuesAreFileteredCorrectly() {
        AttributeFilterPanel panel = getProductFilterInFirstTab()
                .openPanel()
                .getAttributeFilterPanel();

        panel.searchItem("on");

        assertTrue(panel.getItemElements()
                .stream()
                .allMatch(e -> e.getText().contains("on") && isSelected(e)),
                "Items are not displayed properly with search criteria");
    }

    private FilterWidget getProductFilterInFirstTab() {
        initDashboardsPage();
        dashboardsPage.getTabs().openTab(0);

        return dashboardsPage.getContent().getFilterWidget("product").openPanel();
    }

    private boolean isSelectOnlyLinkDisplayedOn(WebElement element) {
        return isElementVisible(BY_SELECT_ONLY_LINK, element);
    }

    private void clickSelectOnlyLinkOn(WebElement element) {
        new Actions(browser).moveToElement(element).perform();
        waitForElementVisible(BY_SELECT_ONLY_LINK, element).click();
    }

    private boolean isSelected(WebElement element) {
        return waitForElementVisible(By.tagName("input"), element).isSelected();
    }
}
