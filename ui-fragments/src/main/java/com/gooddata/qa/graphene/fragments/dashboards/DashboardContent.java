package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.AbstractReport;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardContent extends AbstractFragment {

    @FindBy(css = ".c-projectdashboard-items .yui3-c-reportdashboardwidget")
    private List<WebElement> reports;

    @FindBy(css = ".geo-content-wrapper")
    private List<DashboardGeoChart> geoCharts;

    @FindBy(className = "yui3-c-filterdashboardwidget")
    private List<FilterWidget> filters;

    private static final By REPORT_TITLE_LOCATOR = By.cssSelector(".yui3-c-reportdashboardwidget-reportTitle a");

    public int getNumberOfReports() {
        return reports.size();
    }

    private static String REPORT_IMAGE_LOCATOR = "//div[contains(@class, '${reportName}')]//img";

    public <T extends AbstractReport> T getReport(int reportIndex, Class<T> clazz) {
        return Graphene.createPageFragment(clazz, reports.get(reportIndex));
    }

    public <T extends AbstractReport> T getReport(final String name, Class<T> clazz) {
        return Graphene.createPageFragment(clazz, Iterables.find(reports, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return name.equals(waitForElementVisible(REPORT_TITLE_LOCATOR, input).getAttribute("title"));
            }
        }));
    }

    public <T extends AbstractReport> T getLatestReport(Class<T> clazz) {
        return getReport(reports.size() - 1, clazz);
    }

    public List<DashboardGeoChart> getGeoCharts() {
        return geoCharts;
    }

    public DashboardGeoChart getGeoChart(int geoChartIndex) {
        return getGeoCharts().get(geoChartIndex);
    }

    public WebElement getImageFromReport(String reportName){
        By eleBy =  By.xpath(REPORT_IMAGE_LOCATOR.replace("${reportName}", CssUtils.simplifyText(reportName)));
        return waitForElementVisible(eleBy, browser);
     }

    public List<FilterWidget> getFilters() {
        return filters;
    }

    public FilterWidget getFirstFilter() {
        return filters.get(0);
    }

    public FilterWidget getFilterWidget(final String condition) {
        // need to refresh page so filter widget can load its root element when accessing
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);

        return Iterables.find(filters, new Predicate<FilterWidget>() {
            @Override
            public boolean apply(FilterWidget input) {
                return input.getRoot().getAttribute("class").contains("s-" + condition);
            }
        }, null);
    }
}
