package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.collections.Sets;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Fragment represents table report in
 *  - Dashboard page
 *  - Report page
 *  - Drill dialog
 */
public class TableReport extends AbstractReport {

    @FindBy(css = ".containerBody .gridTabPlate .gridTile .element span.captionWrapper")
    private List<WebElement> attributeElementInGrid;

    @FindBy(css = ".containerBody .gridTabPlate .gridTile .metric span")
    private List<WebElement> metricElementInGrid;

    @FindBy(css = ".containerBody .gridTabPlate .gridTile .data")
    private List<WebElement> metricValuesInGrid;

    @FindBy(css = ".drillable")
    private List<WebElement> drillableElements;

    @FindBy(css = ".gridTab.mainHeaderTab .captionWrapper")
    private List<WebElement> attributesHeader;

    @FindBy(css = ".containerBody .gridTabPlate .gridTile .metric span")
    private List<WebElement> metricsHeader;

    @FindBy(xpath = "//div[contains(@class,'c-report-message')]")
    private WebElement reportMessage;

    private static final String NO_DATA = "No data";

    private static final String REPORT_NOT_COMPUTABLE = "Report not computable due to improper metric definition.";

    public List<String> getAttributesHeader() {
        waitForReportLoading();
        return Lists.newArrayList(Collections2.transform(attributesHeader,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    public Set<String> getMetricsHeader() {
        waitForReportLoading();
        return Sets.newHashSet(Collections2.transform(metricsHeader,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    public List<String> getAttributeElements() {
        waitForReportLoading();
        return Lists.newArrayList(Collections2.transform(attributeElementInGrid,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }

    public List<List<String>> getAttributeElementsByRow() {
        waitForReportLoading();
        List<List<String>> result = Lists.newArrayList();
        List<String> row = null;
        List<String> lastRow = null;
        Pair<Integer, Integer> possition = null;
        int lastRowIndex = 0;

        for (WebElement element: attributeElementInGrid) {
            possition = getPossitionFromRegion(element.findElement(BY_PARENT).getAttribute("gdc:region"));
            if (possition.getLeft() == lastRowIndex + 1) {
                if (row != null) {
                    result.add(row);
                    lastRow = row;
                }
                lastRowIndex++;
                row = Lists.newArrayList();
                if (possition.getRight() > 0) {
                    for (int i = 0; i < possition.getRight(); i++) {
                        row.add(lastRow.get(i));
                    }
                }
            }
            row.add(element.getText().trim());
        }
        if (row != null) {
            result.add(row);
        }

        return result;
    }

    public List<Float> getMetricElements() {
        waitForReportLoading();
        return Lists.newArrayList(Collections2.transform(metricValuesInGrid,
                new Function<WebElement, Float>() {
            @Override
            public Float apply(WebElement input) {
                return ReportPage.getNumber(input.getAttribute("title"));
            }
        }));
    }

    public List<String> getRawMetricElements() {
        waitForReportLoading();
        return Lists.newArrayList(Collections2.transform(metricValuesInGrid,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getAttribute("title");
            }
        }));
    }

    public void verifyAttributeIsHyperlinkInReport() {
        String drillToHyperLinkElement = "Open external link in a new window";
        assertTrue(drillableElements.size() > 0, "Attribute is NOT drillable");
        for (WebElement element : drillableElements) {
            assertTrue(waitForElementVisible(element).getAttribute("title").indexOf(drillToHyperLinkElement) > -1,
                       "Some of elements are NOT drillable to external link!");
        }
    }

    public void clickOnAttributeToOpenDrillReport(String attributeName) {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!attributeName.equals(e.getText().trim()))
                continue;
            e.click();
            break;
        }
    }

    public boolean isRollupTotalVisible() {
        waitForReportLoading();

        try {
            return browser.findElement(By.cssSelector(".totalHeader")).isDisplayed();
        } catch(NoSuchElementException e) {
            return false;
        }
    }

    public void drillOnMetricValue() throws IllegalArgumentException {
        waitForReportLoading();
        String cssClass = null;
        for (WebElement e : drillableElements) {
            cssClass = e.getAttribute("class");

            if (!cssClass.contains("even") && !cssClass.contains("odd"))
                continue;

            e.findElement(By.cssSelector("span")).click();
            return;
        }
        throw new IllegalArgumentException("No metric value to drill on");
    }

    public void drillOnMetricValue(String value) throws IllegalArgumentException {
        waitForReportLoading();
        String cssClass = null;
        WebElement spanElement = null;
        for (WebElement e : drillableElements) {
            cssClass = e.getAttribute("class");

            if (!cssClass.contains("even") && !cssClass.contains("odd"))
                continue;

            spanElement = e.findElement(By.cssSelector("span"));
            if (!value.equals(spanElement.getText()))
                continue;
            spanElement.click();
            return;
        }
        throw new IllegalArgumentException(String.format("No metric value %s to drill on", value));
    }

    public void drillOnAttributeValue() throws IllegalArgumentException {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!e.findElement(BY_PARENT).getAttribute("class").contains("rows"))
                continue;

            e.click();
            return;
        }
        throw new IllegalArgumentException("No attribute value to drill on");
    }

    public void drillOnAttributeValue(String value) throws IllegalArgumentException {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!e.findElement(BY_PARENT).getAttribute("class").contains("rows"))
                continue;

            if (!value.equals(e.getText()))
                continue;
            e.click();
            return;
        }
        throw new IllegalArgumentException(String.format("No attribute value %s to drill on", value));
    }

    public void waitForReportLoading() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // wait for report idle when drill on metric values
                try { Thread.sleep(1000); } catch(InterruptedException e) {}

                try {
                    return !TableReport.this.getRoot().findElement(By.cssSelector(".c-report"))
                            .getAttribute("class").contains("reloading");
                } catch(NoSuchElementException e) {
                    // in Report Page
                    return !metricElementInGrid.isEmpty() || !attributeElementInGrid.isEmpty();
                }
            }
        });
    }

    public boolean isNoData() {
        return waitForElementVisible(reportMessage).getText().contains(NO_DATA);
    }

    public boolean isNotComputed() {
        return waitForElementVisible(reportMessage.findElement(By.tagName("p"))).getText()
                                                               .contains(REPORT_NOT_COMPUTABLE);
    }

    private Pair<Integer, Integer> getPossitionFromRegion(String region) {
        String[] parts = region.split(",");
        return Pair.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }
}
