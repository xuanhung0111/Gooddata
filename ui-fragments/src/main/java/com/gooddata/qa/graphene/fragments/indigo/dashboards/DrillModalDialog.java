package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class DrillModalDialog extends AbstractFragment {

    @FindBy(className = "s-drill-close-button")
    private WebElement closeButton;

    @FindBy(className = "s-drill-title")
    private WebElement titleInsight;

    private static final By ROOT = className("s-drill-modal-dialog");
    private static final By BY_CHART_REPORT = className("highcharts-container");
    private static final By BY_HIGHCHARTS_TOOLTIP = cssSelector(".highcharts-tooltip-container div.highcharts-tooltip");
    private static final String TOOLTIP_ITEM = ".gd-viz-tooltip-item";
    private static final String TOOLTIP_TITLE = ".gd-viz-tooltip-title";
    private static final String TOOLTIP_VALUE = ".gd-viz-tooltip-value";

    public static DrillModalDialog getInstance(final SearchContext searchContext) {
        return Graphene.createPageFragment(DrillModalDialog.class,
            waitForElementVisible(ROOT, searchContext));
    }

    public String getTitleInsight() {
        return waitForElementVisible(titleInsight).getText();
    }

    public void close() {
        waitForElementVisible(closeButton).click();
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class, waitForElementVisible(BY_CHART_REPORT, getRoot()));
    }

    public List<List<String>> getTooltipTextOnTrackerByIndex(int groupIndex, int index) {
        displayTooltipOnTrackerByIndex(groupIndex, index);
        return getCurrentHighChartsTooltip();
    }

    private void displayTooltipOnTrackerByIndex(int groupIndex, int index) {
        WebElement tracker = getTracker(groupIndex, index);
        getActions().moveToElement(tracker).moveByOffset(1, 1).click().perform();
    }

    private WebElement getTracker(int groupIndex, int index) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
            .findElements(cssSelector(String.format(".highcharts-series-%s.highcharts-tracker rect," +
                ".highcharts-series-%s.highcharts-tracker path," +
                ".highcharts-series-%s.highcharts-tracker circle", groupIndex, groupIndex, groupIndex))));
        return list.get(index);
    }

    private List<List<String>> getCurrentHighChartsTooltip() {
        WebElement currentTooltip =  waitForCollectionIsNotEmpty(browser.findElements(BY_HIGHCHARTS_TOOLTIP))
            .stream()
            .filter(e -> e.getAttribute("style")
            .contains("opacity: 1"))
            .findFirst()
            .get();

        return waitForCollectionIsNotEmpty(currentTooltip.findElements(cssSelector(TOOLTIP_ITEM))).stream()
            .map(item -> asList(item.findElement(cssSelector(TOOLTIP_TITLE)).getText(),
                item.findElement(cssSelector(TOOLTIP_VALUE)).getText()))
            .collect(Collectors.toList());
    }
}
