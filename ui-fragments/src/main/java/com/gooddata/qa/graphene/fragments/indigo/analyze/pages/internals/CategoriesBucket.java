package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class CategoriesBucket extends AbstractBucket {

    @FindBy(className = "s-date-granularity-switch")
    private Select granularity;

    @FindBy(className = "s-date-dimension-switch")
    private Select dimensionSwitch;

    public List<String> getItemNames() {
        return items.stream()
            .map(e -> e.findElement(BY_HEADER))
            .map(WebElement::getText)
            .collect(toList());
    }

    public void changeGranularity(String time) {
        waitForElementVisible(granularity).selectByVisibleText(time);
    }

    public String getSelectedGranularity() {
        return waitForElementVisible(granularity).getFirstSelectedOption().getText();
    }

    public List<String> getAllGranularities() {
        return waitForElementVisible(granularity).getOptions()
            .stream()
            .map(WebElement::getText)
            .collect(toList());
    }

    public String getSelectedDimensionSwitch() {
        waitForElementVisible(dimensionSwitch);
        return dimensionSwitch.getFirstSelectedOption().getText();
    }

    public void changeDimensionSwitchInBucket(String dimensionSwitch) {
        waitForElementVisible(this.dimensionSwitch);
        this.dimensionSwitch.selectByVisibleText(dimensionSwitch);
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
}
