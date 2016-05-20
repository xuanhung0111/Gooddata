package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.beust.jcommander.internal.Lists;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class ReportInfoViewPanel extends AbstractFragment {

    @FindBy(css = ".reportTitle")
    private WebElement reportTitle;

    @FindBy(css = ".reportFiltersAndMetricsHandle")
    private WebElement metricsAndFiltersLink;

    @FindBy(css = ".s-report-metrics li strong")
    private List<WebElement> metrics;

    @FindBy(css = ".s-report-filters li strong")
    private List<WebElement> filters;

    @FindBy(css = ".s-btn-view_this_report")
    private WebElement viewReportButton;

    @FindBy(css = ".s-btn-download_as___")
    private WebElement downloadAsButton;

    private static final By REPORT_TITLE_LOCATOR = By.cssSelector(".reportTitle");

    public String getReportTitle() {
        waitForElementVisible(this.getRoot());
        List<WebElement> reportTitle = browser.findElements(REPORT_TITLE_LOCATOR);

        if (reportTitle.isEmpty()) {
            return "";
        }

        return reportTitle.get(0).getText().trim();
    }

    public List<String> getAllMetricNames() {
        return getAllFilterOrMetricNames(metrics);
    }

    public List<String> getAllFilterNames() {
        return getAllFilterOrMetricNames(filters);
    }

    public void clickViewReportButton() {
        waitForElementVisible(viewReportButton).click();
    }

    public void downloadReportAsFormat(ExportFormat format) {
        waitForElementVisible(downloadAsButton).click();

        Graphene.createPageFragment(SimpleMenu.class, waitForElementVisible(SimpleMenu.LOCATOR, browser))
                .select(format.getLabel());
    }

    private List<String> getAllFilterOrMetricNames(List<WebElement> filtersOrMetrics) {
        WebElement reportFiltersAndMetricsAdditionalInfo = browser.findElement(
                By.cssSelector(".reportFiltersAndMetricsAdditionalInfo"));

        if (!reportFiltersAndMetricsAdditionalInfo.isDisplayed()) {
            waitForElementVisible(metricsAndFiltersLink).click();
            waitForElementVisible(reportFiltersAndMetricsAdditionalInfo);
        }

        return Lists.newArrayList(Collections2.transform(filtersOrMetrics, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }));
    }
}
