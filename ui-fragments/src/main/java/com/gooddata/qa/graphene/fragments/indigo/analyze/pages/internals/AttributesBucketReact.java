package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class AttributesBucketReact extends AbstractBucket {

    @FindBy(className = "s-date-granularity-switch")
    private Select granularity;

    // s-date-dimension-switch renamed to s-date-dataset-switch
    @FindBy(className = "s-date-dataset-switch")
    private Select dimensionSwitch;

    public List<String> getItemNames() {
        return getElementTexts(items, e -> e.findElement(BY_HEADER));
    }

    public void changeGranularity(String time) {
        waitForElementVisible(granularity).selectByVisibleText(time);
    }

    public String getSelectedGranularity() {
        return waitForElementVisible(granularity).getFirstSelectedOption().getText();
    }

    public List<String> getAllGranularities() {
        return getElementTexts(waitForElementVisible(granularity).getOptions());
    }

    public String getSelectedDimensionSwitch() {
        waitForElementVisible(dimensionSwitch);
        return dimensionSwitch.getFirstSelectedOption().getText();
    }

    public void changeDateDimension(String switchDimension) {
        waitForElementVisible(this.dimensionSwitch);
        this.dimensionSwitch.selectByVisibleText(switchDimension);
    }

    public WebElement getFirst() {
        return items.get(0);
    }

    public WebElement get(final String name) {
        return items.stream()
                .map(e -> e.findElement(BY_HEADER))
                .filter(e -> name.equals(e.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find attribute: " + name));
    }

    @Override
    public String getWarningMessage() {
        throw new UnsupportedOperationException();
    }
}
