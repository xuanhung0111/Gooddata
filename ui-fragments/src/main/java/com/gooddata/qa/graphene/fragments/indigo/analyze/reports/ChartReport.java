package com.gooddata.qa.graphene.fragments.indigo.analyze.reports;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ChartReport extends AbstractFragment {

    @FindBy(css = ".highcharts-series *")
    private List<WebElement> trackers;

    @FindBy(className = "highcharts-legend-item")
    private List<WebElement> legends;

    @FindBy(css = "div.highcharts-tooltip")
    private WebElement tooltip;

    @FindBy(css = ".highcharts-data-labels tspan")
    private List<WebElement> dataLabels;

    @FindBy(css = ".highcharts-axis-labels text[text-anchor = 'middle']")
    private List<WebElement> axisLabels;

    private static final By BY_Y_AXIS_TITLE = By.className("highcharts-yaxis-title");

    public String getYaxisTitle() {
        List<WebElement> yAxisTitle = getRoot().findElements(BY_Y_AXIS_TITLE);
        if (yAxisTitle.isEmpty()) {
            return "";
        }
        return yAxisTitle.get(0).getText();
    }

    private List<String> getLabels(Collection<WebElement> labels) {
        return getElementTexts(waitForCollectionIsNotEmpty(labels));
    }

    public int getTrackersCount() {
        if (isLineChart()) {
            return waitForCollectionIsNotEmpty(trackers).size();
        }

        return (int) waitForCollectionIsNotEmpty(trackers).stream()
            .map(e -> e.getAttribute("height"))
            .map(Integer::parseInt)
            .filter(i -> i > 0)
            .count();
    }

    public List<List<String>> getTooltipTextOnTrackerByIndex(int index) {
        waitForCollectionIsNotEmpty(trackers);
        checkIndex(index);
        getActions().moveToElement(trackers.get(index)).perform();

        waitForElementVisible(tooltip);
        return getTooltipText();
    }

    public boolean isLegendVisible() {
        return !legends.isEmpty();
    }

    public boolean areLegendsHorizontal() {
        List<String[]> values = getTransformValueFormLegend();
        final String y = values.get(0)[1];

        return values.stream().allMatch(input -> y.equals(input[1]));
    }

    public boolean areLegendsVertical() {
        List<String[]> values = getTransformValueFormLegend();
        final String x = values.get(0)[0];

        return values.stream().allMatch(input -> x.equals(input[0]));
    }

    public List<String> getLegends() {
        return getElementTexts(waitForCollectionIsNotEmpty(legends), e -> e.findElement(By.cssSelector("tspan")));
    }

    public List<String> getLegendColors() {
        return waitForCollectionIsNotEmpty(legends).stream()
            .map(e -> e.findElement(By.cssSelector("path")))
            .map(e -> e.getCssValue("fill"))
            .collect(toList());
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

    private List<String[]> getTransformValueFormLegend() {
        return waitForCollectionIsNotEmpty(legends).stream()
            .map(e -> e.getAttribute("transform"))
            .map(e -> e.replace("translate(", ""))
            .map(e -> e.replace(")", ""))
            .map(e -> e.split(","))
            .collect(toList());
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
