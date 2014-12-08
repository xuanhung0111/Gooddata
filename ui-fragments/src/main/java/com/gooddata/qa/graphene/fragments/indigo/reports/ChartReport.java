package com.gooddata.qa.graphene.fragments.indigo.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ChartReport extends AbstractFragment {

    @FindBy(css = ".highcharts-series *")
    private List<WebElement> trackers;

    @FindBy(css = "div.highcharts-legend-item")
    private List<WebElement> legends;

    @FindBy(css = "div.highcharts-tooltip")
    private WebElement tooltip;

    private static final String DESELECTED_COLOR = "rgb(216,216,216)";

    public ChartReport clickOnTrackerByIndex(int index) {
        waitForCollectionIsNotEmpty(trackers);
        checkIndex(index);
        trackers.get(index).click();
        return this;
    }

    public int getTrackersCount() {
        waitForCollectionIsNotEmpty(trackers);
        return trackers.size();
    }

    public boolean isTrackerInSelectedStateByIndex(int index) {
        waitForCollectionIsNotEmpty(trackers);
        checkIndex(index);
        WebElement tracker = trackers.get(index);

        if (tracker.getAttribute("stroke") == null)
            return false;

        if (isLineChart()) {
            if (!"black".equals(tracker.getAttribute("stroke")))
                return false;
            return "2".equals(tracker.getAttribute("stroke-width"));
        }

        if (DESELECTED_COLOR.equals(tracker.getAttribute("fill")))
            return false;
        if (!"#FFFFFF".equals(tracker.getAttribute("stroke")))
            return false;
        return "1".equals(tracker.getAttribute("stroke-width"));
    }

    public boolean isTrackerInNormalStateByIndex(int index) {
        if (!isLineChart())
            return isTrackerInSelectedStateByIndex(index);

        waitForCollectionIsNotEmpty(trackers);
        checkIndex(index);
        WebElement tracker = trackers.get(index);

        if (tracker.getAttribute("stroke") != null)
            return false;
        return "1".equals(tracker.getAttribute("stroke-width"));
    }

    public List<List<String>> getTooltipTextOnTrackerByIndex(int index) {
        waitForCollectionIsNotEmpty(trackers);
        checkIndex(index);
        new Actions(browser).moveToElement(trackers.get(index)).perform();

        waitForElementVisible(tooltip);
        return getTooltipText();
    }

    public List<String> getLegends() {
        waitForCollectionIsNotEmpty(legends);
        return Lists.newArrayList(Collections2.transform(legends,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(By.cssSelector("span")).getText();
            }
        }));
    }

    public List<String> getLegendColors() {
        waitForCollectionIsNotEmpty(legends);
        return Lists.newArrayList(Collections2.transform(legends,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(By.cssSelector("span")).getCssValue("fill");
            }
        }));
    }

    public String getLegendColorByName(String name) {
        return findLegendByName(name).findElement(By.cssSelector("span")).getCssValue("fill");
    }

    public ChartReport clickOnLegendByName(String name) {
        findLegendByName(name).click();
        return this;
    }

    private WebElement findLegendByName(final String name) {
        waitForCollectionIsNotEmpty(legends);
        return Iterables.find(legends, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return name.equals(input.findElement(By.cssSelector("span")).getText());
            }
        });
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= trackers.size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private List<List<String>> getTooltipText() {
        List<List<String>> result = new ArrayList<List<String>>();
        for (WebElement row : tooltip.findElements(By.cssSelector("tr"))) {
            result.add(Arrays.asList(row.findElement(By.cssSelector(".title")).getText(),
                                     row.findElement(By.cssSelector(".value")).getText()));
        }
        return result;
    }

    private boolean isLineChart() {
        return getRoot().getAttribute("class").contains("visualization-line");
    }
}
