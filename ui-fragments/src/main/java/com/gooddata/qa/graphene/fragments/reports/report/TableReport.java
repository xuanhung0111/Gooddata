package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.testng.collections.Sets;

import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu;
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
public class TableReport extends AbstractDashboardReport {

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

    private static final String NO_DATA = "No data";

    private static final String REPORT_NOT_COMPUTABLE = "Report not computable due to improper metric definition.";

    private static final By BY_SORT_LOCATOR = className("sort");
    private static final By REPORT_MESSAGE_LOCATOR = className("c-report-message");

    public TableReport sortByHeader(final String header, final Sort howToSort) {
        getActions().moveToElement(getHeaderElement(header)).perform();

        //graphene finds wrong sort arrows in some cases, so we need to find parent node again 
        List<WebElement> sortElements = browser.findElement(cssSelector("div.hover"))
                .findElements(By.className("sortArrow"));
        for(WebElement e : sortElements) {
            if(e.getAttribute("class").contains(howToSort.toString())) {
                //have tried to use WebElement.click() but it's unstable
                getActions().moveToElement(e).click().perform();
                break;
            }
        }

        waitForReportLoading();
        return this;
    }

    public boolean isSortAvailable(final String header) {
        final WebElement headerElement = waitForElementVisible(getHeaderElement(header));
        getActions().moveToElement(headerElement).perform();
        return !headerElement.findElements(BY_SORT_LOCATOR).isEmpty();
    }

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

    public List<String> getTotalHeaders() {
        waitForReportLoading();
        return browser
                .findElements(By.cssSelector(".containerBody .gridTabPlate .gridTile .totalHeader span.captionWrapper"))
                .stream().map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<Float> getTotalValues() {
        waitForReportLoading();
        return browser
                .findElements(By.cssSelector(".containerBody .gridTabPlate .gridTile div.total:not(.totalHeader)"))
                .stream().map(e -> e.getAttribute("title"))
                .map(ReportPage::getNumber)
                .collect(Collectors.toList());
    }

    public TableReport changeAliasToAttribute(final String attribute, String alias) {
        WebElement header = attributesHeader.stream()
            .filter(e -> attribute.equals(e.getText().trim()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find attribute: " + attribute))
            .findElement(BY_PARENT);
        new Actions(browser).moveToElement(header).doubleClick().doubleClick().perform();
        WebElement input = waitForElementVisible(className("ipeEditor"), browser);
        input.clear();
        input.sendKeys(alias);
        input.sendKeys(Keys.ENTER);
        assertEquals(header.getText().trim(), alias);
        return this;
    }

    public TableReport changeAliasToMetric(final String metric, String alias) {
        WebElement header = metricsHeader.stream()
                .filter(e -> metric.equals(e.getText().trim()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find metric: " + metric))
                .findElement(BY_PARENT);
        new Actions(browser).doubleClick(header).perform();
        WebElement input = waitForElementVisible(className("ipeEditor"), browser);
        input.clear();
        input.sendKeys(alias);
        input.sendKeys(Keys.ENTER);
        assertEquals(header.getText().trim(), alias);
        return this;
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

    public TableReport showOnly(String attributeValue) {
        waitForReportLoading();
        WebElement cell = attributeElementInGrid.stream()
            .filter(e -> attributeValue.equals(e.getText().trim()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find attribute value: " + attributeValue));
        getActions().contextClick(cell).perform();
        waitForElementVisible(className(format("s-show_only__%s_", simplifyText(attributeValue))), browser).click();
        waitForReportLoading();
        return this;
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

    public void drillOnMetricValue() {
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

    public TableReport drillOnMetricValue(String value) {
        getMetricElement(value).click();
        return this;
    }

    public WebElement getMetricElement(String value) {
        waitForReportLoading();
        String cssClass = null;
        WebElement spanElement = null;
        for (WebElement e : metricValuesInGrid) {
            cssClass = e.getAttribute("class");

            if (!cssClass.contains("even") && !cssClass.contains("odd"))
                continue;

            spanElement = e.findElement(By.cssSelector("span"));
            if (!value.equals(spanElement.getText()))
                continue;
            return spanElement;
        }
        throw new IllegalArgumentException("Cannot find metric value " + value);
    }

    public void drillOnAttributeValue() {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!e.findElement(BY_PARENT).getAttribute("class").contains("rows"))
                continue;

            e.click();
            return;
        }
        throw new IllegalArgumentException("No attribute value to drill on");
    }

    public TableReport drillOnAttributeValue(String value) {
        getAttributeValueElement(value).click();
        return this;
    }

    public WebElement getAttributeValueElement(String value) {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!e.findElement(BY_PARENT).getAttribute("class").contains("rows"))
                continue;

            if (!value.equals(e.getText()))
                continue;
            return e;
        }
        throw new IllegalArgumentException("Cannot find attribute value " + value);
    }

    public TableReport waitForReportLoading() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // wait for report idle when drill on metric values
                sleepTightInSeconds(1);

                try {
                    return !TableReport.this.getRoot().findElement(By.cssSelector(".c-report"))
                            .getAttribute("class").contains("reloading");
                } catch(NoSuchElementException e) {
                    // in Report Page
                    return !metricElementInGrid.isEmpty() || !attributeElementInGrid.isEmpty();
                }
            }
        });
        return this;
    }

    public boolean isNoData() {
        return waitForElementVisible(REPORT_MESSAGE_LOCATOR, getRoot()).getText().contains(NO_DATA);
    }

    public boolean isNotComputed() {
        WebElement reportMessage = waitForElementPresent(REPORT_MESSAGE_LOCATOR, getRoot());
        return waitForElementVisible(reportMessage.findElement(By.tagName("p"))).getText()
                .contains(REPORT_NOT_COMPUTABLE);
    }

    public void changeAttributeDisplayLabelByRightClick(final String attribute, String label) {
        WebElement header = attributesHeader.stream()
            .filter(e -> attribute.equals(e.getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find attribute header: " + attribute));

        new Actions(browser).contextClick(header).perform();
        waitForElementVisible(cssSelector("#ctxMenu .s-" + simplifyText(label) +" > a"), browser).click();
    }

    public boolean isReportTitleVisible() {
        final By reportLabelLocator = cssSelector(".yui3-c-reportdashboardwidget-reportTitle > a");

        if (!isElementPresent(reportLabelLocator, getRoot())) {
            return false;
        }

        return !getRoot().findElement(reportLabelLocator).getCssValue("display").startsWith("none");
    }

    public ContextMenu openContextMenuFromCellValue(final String cellValue) {
        getActions()
            .contextClick(
                    browser.findElements(By.cssSelector(".containerBody .gridTabPlate .gridTile div.cell"))
                        .stream()
                        .filter(e -> e.getText().equals(cellValue)).findFirst().get())
            .perform();

        return Graphene.createPageFragment(ContextMenu.class, waitForElementVisible(By.id("ctxMenu"), browser));
    }

    public List<String> getDrillableElements() {
        waitForReportLoading();
        return drillableElements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<WebElement> getImageElements() {
        waitForReportLoading();
        final List<WebElement> images = attributeElementInGrid.stream()
                .map(e -> e.findElement(BY_PARENT))
                .filter(e -> 
                        Stream.of(e.getAttribute("class").split("\\s+"))
                                .anyMatch(input -> input.equals("image")))
                .map(e -> e.findElement(By.tagName("img")))
                .collect(Collectors.toList());

        if(images.isEmpty())
            throw new RuntimeException("Cannot find any image element");

        return images;
    }

    public WebElement getImageElement(String imageSource) {
        waitForReportLoading();

        return waitForElementVisible(attributeElementInGrid.stream()
                .map(e -> e.findElement(BY_PARENT))
                .filter(e -> e.getAttribute("class").contains("s-grid-" + simplifyText(imageSource)))
                .findAny()
                .get()
                .findElement(By.tagName("img")));
    }

    private Pair<Integer, Integer> getPossitionFromRegion(String region) {
        String[] parts = region.split(",");
        return Pair.of(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    private List<WebElement> getAllHeaderElements() {
        return browser.findElements(By.cssSelector(
                ".containerBody .gridTabPlate .gridTile .cell:not(.element):not(.data) span.captionWrapper"));
    }

    private WebElement getHeaderElement(final String header) {
        return getAllHeaderElements().stream()
                .filter(e -> header.equals(e.getText()))
                .map(e -> e.findElement(BY_PARENT))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find header named: " + header));
    }

    public static enum Sort {
        ASC("ascendingArrow"),
        DESC("descendingArrow");

        private String cssClass;

        private Sort(String css) {
            cssClass = css;
        }

        @Override
        public String toString() {
            return cssClass;
        }
    }
}
