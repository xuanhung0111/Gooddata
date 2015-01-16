package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
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

import com.gooddata.qa.graphene.fragments.common.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
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
            if (cssClass.contains("rows") || cssClass.contains("columns"))
                continue;

            if (!cssClass.contains("even") && !cssClass.contains("odd"))
                continue;

            e.findElement(By.cssSelector("span")).click();
            break;
        }
    }

    public void drillOnAttributeValue() {
        waitForReportLoading();
        for (WebElement e : attributeElementInGrid) {
            if (!e.findElement(BY_PARENT).getAttribute("class").contains("rows"))
                continue;

            e.click();
            break;
        }
    }

    public void waitForReportLoading() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                // wait for report idle when drill on metric values
                try { Thread.sleep(100); } catch(InterruptedException e) {}

                try {
                    return !TableReport.this.getRoot().findElement(By.cssSelector(".c-report"))
                            .getAttribute("class").contains("reloading");
                } catch(NoSuchElementException e) {
                    // in Report Page
                    return !metricElementInGrid.isEmpty();
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

    public void addDrilling(Pair<List<String>, String> pairs, String group) {
        waitForElementVisible(this.getRoot()).click();
        DashboardEditWidgetToolbarPanel toolbar = Graphene.createPageFragment(DashboardEditWidgetToolbarPanel.class,
                waitForElementVisible(DashboardEditWidgetToolbarPanel.LOCATOR, browser));
        toolbar.openConfigurationPanel();
        
        if (isAddDrillingButtonVisible()) {
            waitForElementVisible(By.cssSelector(".s-btn-add_drilling"), browser).click();
        } else {
            waitForElementVisible(By.cssSelector(".s-btn-add_more___"), browser).click();
        }
        waitForElementVisible(By.cssSelector(".s-btn-select_metric___attribute___"), browser).click();

        waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser);
        SelectItemPopupPanel popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                browser.findElements(SelectItemPopupPanel.LOCATOR).get(1));

        for (String item : pairs.getLeft()) {
            popupPanel.searchAndSelectItem(item);
        }
        waitForElementVisible(By.cssSelector(".s-btn-select_attribute___report"), browser).click();
        waitForElementVisible(popupPanel.getRoot());
        popupPanel.changeGroup(group);
        popupPanel.searchAndSelectItem(pairs.getRight());
        waitForElementVisible(By.cssSelector(".s-btn-apply"), browser).click();
    }

    public void addDrilling(Pair<List<String>, String> pairs) {
        addDrilling(pairs, "Attributes");
    }

    private boolean isAddDrillingButtonVisible() {
        try {
            return browser.findElement(By.cssSelector(".s-btn-add_drilling")).isDisplayed();
        } catch(NoSuchElementException e) {
            return false;
        }
    }
}
