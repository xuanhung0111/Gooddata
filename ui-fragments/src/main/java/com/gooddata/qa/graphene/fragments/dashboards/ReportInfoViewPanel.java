package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.beust.jcommander.internal.Lists;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class ReportInfoViewPanel extends AbstractFragment {

    @FindBy(css = ".reportTitle")
    private WebElement reportTitle;

    @FindBy(css = ".reportFiltersAndMetricsHandle")
    private WebElement metricsAndFiltersLink;

    @FindBy(css = ".s-report-metrics li")
    private List<WebElement> metrics;

    @FindBy(css = ".s-btn-view_this_report")
    private WebElement viewReportButton;

    @FindBy(css = ".s-btn-download_as___")
    private WebElement downloadAsButton;

    private static final By REPORT_TITLE_LOCATOR = By.cssSelector(".reportTitle");

    public String getReportTitle() {
        waitForElementVisible(this.getRoot());
        try {
            return browser.findElement(REPORT_TITLE_LOCATOR).getText().trim();
        } catch (NoSuchElementException e) {
            // Report info view panel in drill report does not
            // contain report title
            return "";
        }
    }

    public List<String> getAllMetricNames() {
        WebElement reportFiltersAndMetricsAdditionalInfo = browser.findElement(
                By.cssSelector(".reportFiltersAndMetricsAdditionalInfo"));
        if (!reportFiltersAndMetricsAdditionalInfo.isDisplayed()) {
            waitForElementVisible(metricsAndFiltersLink).click();
            waitForElementVisible(reportFiltersAndMetricsAdditionalInfo);
        }

        final By strongElement = By.cssSelector("strong");
        return Lists.newArrayList(Collections2.transform(metrics,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(strongElement).getText().trim();
            }
        }));
    }

    public void clickViewReportButton() {
        waitForElementVisible(viewReportButton).click();
    }

    public void downloadReportAsFormat(ExportFormat format) {
        waitForElementVisible(downloadAsButton).click();
        waitForElementVisible(By.xpath(String.format("//a[text()='%s']", format.getLabel())), browser).click();
    }
}
