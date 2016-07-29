package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.openqa.selenium.By.className;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.VariableStatusWidget;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DashboardContent extends AbstractFragment {

    @FindBy(css = ".geo-content-wrapper")
    private List<DashboardGeoChart> geoCharts;

    @FindBy(css = ".c-collectionWidget:not(.gdc-hidden) .yui3-c-filterdashboardwidget")
    private List<WebElement> filters;

    @FindBy(css = ".c-collectionWidget:not(.gdc-hidden) .yui3-c-dashboardcollectionwidget-content .yui3-c-dashboardwidget")
    private List<WebElement> widgets;

    private static final By REPORT_TITLE_LOCATOR = By.cssSelector(".yui3-c-reportdashboardwidget-reportTitle");
    private static final By REPORT_LOCATOR =
            By.cssSelector(".c-collectionWidget:not(.gdc-hidden) .yui3-c-reportdashboardwidget");

    private static final By BY_EMBEDDED_WIDGET = 
            By.cssSelector(".c-collectionWidget:not(.gdc-hidden) .yui3-c-iframedashboardwidget");

    private static String REPORT_IMAGE_LOCATOR = "div.s-${reportName} img";
    private static String REPORT_IMAGE_LOADED_LOCATOR =
            "div.s-${reportName} span.c-report-loading-message[style ^='display: none']";

    public int getNumberOfReports() {
        return getReports().size();
    }

    public <T extends AbstractReport> T getReport(int reportIndex, Class<T> clazz) {
        return createPageFragment(clazz, getReports().get(reportIndex));
    }

    public <T extends AbstractReport> T getReport(final String name, Class<T> clazz) {
        java.util.function.Predicate<? super WebElement> neededReport = report ->
            name.equals(report.findElement(REPORT_TITLE_LOCATOR).findElement(BY_LINK).getAttribute("title"));

        Predicate<WebDriver> reportAppeared = driver -> getReports().stream().anyMatch(neededReport);
        Graphene.waitGui().until(reportAppeared);

        return createPageFragment(clazz, getReports()
            .stream()
            .filter(neededReport)
            .findFirst()
            .get());
    }

    public <T extends AbstractReport> T getLatestReport(Class<T> clazz) {
        return getReport(getNumberOfReports() - 1, clazz);
    }

    public List<DashboardGeoChart> getGeoCharts() {
        return geoCharts;
    }

    public DashboardGeoChart getGeoChart(int geoChartIndex) {
        return getGeoCharts().get(geoChartIndex);
    }

    public WebElement getImageFromReport(String reportName){
        reportName = simplifyText(reportName);
        final By reportLoaded = By.cssSelector(REPORT_IMAGE_LOADED_LOCATOR.replace("${reportName}", reportName));
        final By reportImg =  By.cssSelector(REPORT_IMAGE_LOCATOR.replace("${reportName}", reportName));
        waitForElementPresent(reportLoaded, browser);
        return waitForElementVisible(reportImg, browser);
     }

    public List<FilterWidget> getFilters() {
        return Lists.newArrayList(Collections2.transform(filters, new Function<WebElement, FilterWidget>() {
            @Override
            public FilterWidget apply(WebElement input) {
                return createPageFragment(FilterWidget.class, input);
            }
        }));
    }

    public FilterWidget getFirstFilter() {
        return createPageFragment(FilterWidget.class, filters.get(0));
    }

    public FilterWidget getFilterWidget(final String condition) {
        waitForDashboardPageLoaded(browser);
        WebElement filter = Iterables.find(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains("s-" + condition);
            }
        }, null);

        if (filter == null) {
            return null;
        }

        return createPageFragment(FilterWidget.class, filter);
    }

    public FilterWidget getFilterWidgetByName(final String name) {
        return getFilters()
                .stream()
                .filter(e -> name.equalsIgnoreCase(e.getTitle()))
                .findFirst()
                .get();
    }

    public boolean isEmpty() {
        return widgets.size() == 0;
    }

    public VariableStatusWidget getVariableStatus(final String variable) {
        return browser.findElements(className(TextObject.VARIABLE_STATUS.getLabel()))
            .stream()
            .map(e -> Graphene.createPageFragment(VariableStatusWidget.class, e))
            .filter(widget -> variable.equals(widget.getLabel()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find variable status: " + variable));
    }

    public EmbeddedWidget getLastEmbeddedWidget() {
        return Iterables.getLast(getEmbeddedWidgets());
    }

    private List<EmbeddedWidget> getEmbeddedWidgets() {
        return browser.findElements(BY_EMBEDDED_WIDGET)
                .stream()
                .map(e -> waitForElementVisible(e))
                .map(e -> Graphene.createPageFragment(EmbeddedWidget.class, e))
                .collect(toList());
    }

    private List<WebElement> getReports() {
        return getRoot().findElements(REPORT_LOCATOR);
    }
}
