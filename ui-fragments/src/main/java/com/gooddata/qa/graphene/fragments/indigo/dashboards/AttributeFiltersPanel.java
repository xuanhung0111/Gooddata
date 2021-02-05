package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import java.util.List;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import org.openqa.selenium.By;

/**
 * Panel holding {@link AttributeFilter}s
 */
public class AttributeFiltersPanel extends AbstractFragment {

    private static final String ATTRIBUTE_FILTER_SELECTOR = ".s-attribute-filter";
    private static final String ATTRIBUTE_FILTER_LOADING_SELECTOR = ATTRIBUTE_FILTER_SELECTOR + " .s-loading";

    @FindBy(css = ATTRIBUTE_FILTER_SELECTOR)
    private List<AttributeFilter> attributeFilters;

    @FindBy(className = "dash-filters-date")
    private AttributeFilter dateAttributeFilters;

    public List<AttributeFilter> getAttributeFilters() {
        return attributeFilters;
    }

    public AttributeFilter getDateAttributeFilters() {
        return dateAttributeFilters;
    }

    public AttributeFiltersPanel waitForAttributeFiltersLoaded() {
        waitForElementNotPresent(By.cssSelector(ATTRIBUTE_FILTER_LOADING_SELECTOR));

        return this;
    }

    public AttributeFilter getAttributeFilter(String title) {
        return attributeFilters.stream().filter(s -> Objects.equals(title, s.getTitle())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Attribute filter button with title '" + title + "' not found!"));
    }

    public AttributeFilter getLastFilter() {
        return waitForAttributeFiltersLoaded().attributeFilters.get(attributeFilters.size() -1);
    }

    public AttributeFilter getIndexAttributeFilter(int index) {
        return waitForAttributeFiltersLoaded().attributeFilters.get(index);
    }

    public boolean isFilterVisible(String title) {
        return waitForAttributeFiltersLoaded().attributeFilters.stream()
                .anyMatch(e -> title.equals(e.getTitle()));
    }

    public void dragAndDropAttributeFilter(WebDriver driver, WebElement from, WebElement dropZone) {
        Actions builder = new Actions(driver);
        // Because geckodriver follows W3C and moves the mouse pointer from the centre of the screen,
        // Move the mouse pointer to the top-right corner of the fragment before moving to the specific Element
        builder.clickAndHold(from).moveByOffset(5, 5).perform();
        try {
            builder.moveToElement(dropZone).perform();
        } finally {
            builder.release().perform();
        }
    }

    public WebElement getIndexWebElementAttributeFilter(int index) {
        return getIndexAttributeFilter(index).getRoot().findElement(By.className("attribute-filter-button"));
    }

    public WebElement getLastIndexWebElementAttributeFilter() {
        return getLastFilter().getRoot().findElement(By.className("attribute-filter-button"));
    }
}
