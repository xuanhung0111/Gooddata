package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ChartReport extends AbstractFragment {

    @FindBy(css = ".highcharts-series *")
    private List<WebElement> trackers;

    @FindBy(css = ".highcharts-legend-item")
    private List<WebElement> legends;

    @FindBy(css = "div.highcharts-tooltip")
    private WebElement tooltip;

    @FindBy(css = ".highcharts-data-labels tspan")
    private List<WebElement> dataLabels;

    @FindBy(css = ".highcharts-axis-labels text[text-anchor = 'middle']")
    private List<WebElement> axisLabels;

    private static final String DESELECTED_COLOR = "rgb(216,216,216)";

    public List<String> getStackLabels() {
        return getLabels(browser.findElements(By.cssSelector(".highcharts-stack-labels tspan")));
    }

    private List<String> getLabels(Collection<WebElement> labels) {
        waitForCollectionIsNotEmpty(labels);
        return Lists.newArrayList(Collections2.transform(labels, new Function<WebElement, String>(){
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

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

    public boolean isLegendVisible() {
        return !legends.isEmpty();
    }

    public boolean areLegendsHorizontal() {
        List<String[]> values = getTransformValueFormLegend();
        final String y = values.get(0)[1];

        return Iterables.all(values, new Predicate<String[]>() {
            @Override
            public boolean apply(String[] input) {
                return y.equals(input[1]);
            }
        });
    }

    public boolean areLegendsVertical() {
        List<String[]> values = getTransformValueFormLegend();
        final String x = values.get(0)[0];

        return Iterables.all(values, new Predicate<String[]>() {
            @Override
            public boolean apply(String[] input) {
                return x.equals(input[0]);
            }
        });
    }

    private List<String[]> getTransformValueFormLegend() {
        waitForCollectionIsNotEmpty(legends);
        return Lists.newArrayList(Collections2.transform(legends, new Function<WebElement, String[]>() {
            @Override
            public String[] apply(WebElement input) {
                return input.getAttribute("transform").replace("translate(", "").replace(")", "").split(",");
            }
        }));
    }

    public List<String> getLegends() {
        waitForCollectionIsNotEmpty(legends);
        return Lists.newArrayList(Collections2.transform(legends, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(By.cssSelector("tspan")).getText();
            }
        }));
    }

    public List<String> getLegendColors() {
        waitForCollectionIsNotEmpty(legends);
        return Lists.newArrayList(Collections2.transform(legends, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(By.cssSelector("path")).getCssValue("fill");
            }
        }));
    }

    public String getLegendColorByName(String name) {
        return findLegendByName(name).findElement(By.cssSelector("span")).getCssValue("fill");
    }

    public List<String> getDataLabels() {
        return getLabels(dataLabels);
    }

    public List<String> getAxisLabels() {
        // Axis labels will be empty in case report has no attribute.
        if (axisLabels.isEmpty())
            return Collections.emptyList();

        return getLabels(axisLabels);
    }

    public ChartReport clickOnLegendByName(String name) {
        findLegendByName(name).click();
        return this;
    }

    private WebElement findLegendByName(final String name) {
        waitForCollectionIsNotEmpty(legends);

        return FluentIterable.from(legends).filter(new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return "div".equals(input.getTagName());
            }
        }).firstMatch(new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return name.equals(input.findElement(By.cssSelector("span")).getText());
            }
        }).get();
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
