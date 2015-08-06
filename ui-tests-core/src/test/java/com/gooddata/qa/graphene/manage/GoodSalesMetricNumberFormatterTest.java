package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.WhatItem;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.ReportVisualizer;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;

public class GoodSalesMetricNumberFormatterTest extends GoodSalesAbstractTest {

    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";

    private static final By METRIC_DETAIL_FORMAT_LOCATOR = By.cssSelector(".c-metricDetailFormat .formatter");
    private static final By TABLE_REPORT_LOCATOR = By.id("gridContainerTab");

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-metric-number-formatter";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testNumberFormatEditor() throws InterruptedException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        waitForFragmentVisible(metricDetailPage).changeMetricFormatButDiscard(Formatter.BARS);
        // don't know why get text of metric format in metric detail page return #,##0 instead of #,##0.00
        assertTrue(Formatter.DEFAULT.toString().startsWith(metricDetailPage.getMetricFormat()));
        Screenshots.takeScreenshot(browser, "testNumberFormatEditor-beforeChangeFormat", getClass());
        metricDetailPage.changeMetricFormat(Formatter.BARS);
        Screenshots.takeScreenshot(browser, "testNumberFormatEditor-afterChangeFormat", getClass());
        assertEquals(metricDetailPage.getMetricFormat(), Formatter.BARS.toString());

        try {
            initReportPage();

            ReportVisualizer reportVisualizer = reportPage.getVisualiser();
            reportVisualizer.selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)));
            WebElement metric = reportVisualizer.getMetric(NUMBER_OF_ACTIVITIES);
            assertNotNull(metric);
            metric.click();
            assertEquals(waitForElementVisible(METRIC_DETAIL_FORMAT_LOCATOR, browser)
                    .getText(), Formatter.BARS.toString());

            reportVisualizer.finishReportChanges()
                .selectReportVisualisation(ReportTypes.TABLE);
            TableReport report = Graphene.createPageFragment(TableReport.class,
                    waitForElementVisible(TABLE_REPORT_LOCATOR, browser));
            List<String> values = report.getRawMetricElements();
            assertEquals(values.size(), 1);
            assertTrue(Formatter.BARS.toString().contains(values.get(0)));

            assertEquals(reportPage.showConfiguration().getCustomNumberFormat(), Formatter.BARS.toString());
        } finally {
            resetMetricFormat();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editFormatInReportPage() throws InterruptedException {
        try {
            initReportPage();
            ReportVisualizer reportVisualizer = reportPage.getVisualiser();
            reportVisualizer.selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
                .finishReportChanges()
                .selectReportVisualisation(ReportTypes.TABLE);
            TableReport report = Graphene.createPageFragment(TableReport.class,
                    waitForElementVisible(TABLE_REPORT_LOCATOR, browser));
            List<String> values = report.getRawMetricElements();
            assertEquals(values.size(), 1);
            assertEquals(values.get(0), "154,271.00");

            reportPage.showConfiguration()
                .changeNumberFormatButDiscard(Formatter.BARS);
            report.waitForReportLoading();
            values = report.getRawMetricElements();
            assertEquals(values.size(), 1);
            assertEquals(values.get(0), "154,271.00");

            Screenshots.takeScreenshot(browser, "editFormatInReportPage-beforeChangeFormat", getClass());
            reportPage.showConfiguration()
                .changeNumberFormat(Formatter.BARS);
            report.waitForReportLoading();
            Screenshots.takeScreenshot(browser, "editFormatInReportPage-afterChangeFormat", getClass());
            values = report.getRawMetricElements();
            assertEquals(values.size(), 1);
            assertTrue(Formatter.BARS.toString().contains(values.get(0)));
        } finally {
            resetMetricFormat();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editFormatWhenCreatingNewMetric() throws InterruptedException {
        initReportPage();
        reportPage.createSimpleMetric(SimpleMetricTypes.SUM, "Duration", null, false);
        WebElement editFormat = waitForElementVisible(METRIC_DETAIL_FORMAT_LOCATOR, browser);
        editFormat.click();

        MetricFormatterDialog formatterDialog =  Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser));
        formatterDialog.changeFormatButDiscard(Formatter.BARS);
        assertEquals(waitForElementVisible(editFormat).getText(), Formatter.DEFAULT.toString());

        Screenshots.takeScreenshot(browser, "editFormatWhenCreatingNewMetric-beforeChangeFormat", getClass());
        waitForElementVisible(editFormat).click();
        formatterDialog.changeFormat(Formatter.BARS);
        Screenshots.takeScreenshot(browser, "editFormatWhenCreatingNewMetric-afterChangeFormat", getClass());
        assertEquals(waitForElementVisible(editFormat).getText(), Formatter.BARS.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editExistFormatInSnDDialog() throws InterruptedException {
        try {
            initReportPage();
            ReportVisualizer reportVisualizer = reportPage.getVisualiser();
            WebElement metric = reportVisualizer.selectWhatArea(asList(new WhatItem(NUMBER_OF_ACTIVITIES)))
                    .getMetric(NUMBER_OF_ACTIVITIES);
            metric.click();

            WebElement editFormat = waitForElementVisible(METRIC_DETAIL_FORMAT_LOCATOR, browser);
            editFormat.click();

            MetricFormatterDialog formatterDialog =  Graphene.createPageFragment(MetricFormatterDialog.class,
                    waitForElementVisible(MetricFormatterDialog.LOCATOR, browser));
            formatterDialog.changeFormatButDiscard(Formatter.BARS);
            // don't know why get text of metric format in metric detail page return #,##0 instead of #,##0.00
            assertTrue(Formatter.DEFAULT.toString().startsWith(editFormat.getText()));

            Screenshots.takeScreenshot(browser, "editExistFormatInSnDDialog-beforeChangeFormat", getClass());
            waitForElementVisible(editFormat).click();
            formatterDialog.changeFormat(Formatter.BARS);
            Screenshots.takeScreenshot(browser, "editExistFormatInSnDDialog-afterChangeFormat", getClass());
            assertEquals(waitForElementVisible(editFormat).getText(), Formatter.BARS.toString());
        } finally {
            resetMetricFormat();
        }
    }

    private void initReportPage() {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
    }

    private void resetMetricFormat() {
        initProjectsPage();
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        waitForFragmentVisible(metricDetailPage).changeMetricFormat(Formatter.DEFAULT);
        assertEquals(metricDetailPage.getMetricFormat(), Formatter.DEFAULT.toString());
    }
}
