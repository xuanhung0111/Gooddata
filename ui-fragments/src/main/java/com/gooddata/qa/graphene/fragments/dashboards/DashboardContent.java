package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.fragments.reports.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.TableReport;

import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardContent extends AbstractFragment {
    @FindBy(css = ".c-projectdashboard-items .yui3-c-reportdashboardwidget")
    private List<WebElement> reports;

    @FindBy(css = ".geo-content-wrapper")
    private List<DashboardGeoChart> geoCharts;

    public int getNumberOfReports() {
        return reports.size();
    }

    private static String REPORT_IMAGE_LOCATOR = "//div[contains(@class, '${reportName}')]//img";

    public <T extends AbstractReport> T getReport(int reportIndex, Class<T> clazz) {
        return Graphene.createPageFragment(clazz, reports.get(reportIndex));
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
    
    public TableReport getTableReport(String reportName) {
        return Graphene.createPageFragment(TableReport.class,
                waitForElementVisible(this.getRoot().findElement(
                        By.cssSelector(".s-" + CssUtils.simplifyText(reportName)))));
    }
}
