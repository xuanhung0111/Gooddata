package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

/**
 * s-date-dimension-switch renamed to s-date-dataset-switch
 */
public class AttributesBucket extends AbstractBucket {

    private static final By BY_DATE_DATASET_SELECT = By.className("adi-date-dataset-switch");
    private static final By BY_DATE_GRANULARITY_SELECT = By.className("adi-date-granularity-switch");

    public List<String> getItemNames() {
        return getElementTexts(items, e -> e.findElement(BY_HEADER));
    }

    public void changeGranularity(String time) {
        getDateGranularitySelect().selectByName(time);
    }

    public String getSelectedGranularity() {
        return getDateGranularitySelect().getRoot().getText();
    }

    public Collection<String> getAllGranularities() {
        return getDateGranularitySelect().getValues();
    }

    public String getSelectedDimensionSwitch() {
        return getDateDatasetSelect().getRoot().getText();
    }

    public void changeDateDimension(String switchDimension) {
        getDateDatasetSelect().selectByName(switchDimension);
    }

    public WebElement getFirst() {
        return items.get(0);
    }

    public WebElement get(final String name) {
        return items.stream()
                .filter(e -> name.equals(e.findElement(BY_HEADER).getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find attribute: " + name));
    }

    @Override
    public String getWarningMessage() {
        throw new UnsupportedOperationException();
    }

    public DateDimensionSelect getDateDatasetSelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(BY_DATE_DATASET_SELECT, browser));
    }

    private DateDimensionSelect getDateGranularitySelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(BY_DATE_GRANULARITY_SELECT, browser));
    }
}
