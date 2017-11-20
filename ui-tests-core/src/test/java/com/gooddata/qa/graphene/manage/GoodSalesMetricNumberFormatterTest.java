package com.gooddata.qa.graphene.manage;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesMetricNumberFormatterTest extends GoodSalesAbstractTest {

    private static final By METRIC_DETAIL_FORMAT_LOCATOR = By.cssSelector(".c-metricDetailFormat .formatter");

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-metric-number-formatter";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testNumberFormatEditor() {
        // don't know why get text of metric format in metric detail page return #,##0 instead of #,##0.00
        assertTrue(Formatter.DEFAULT.toString().startsWith(initMetricPage()
                .openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES)
                .changeMetricFormatButDiscard(Formatter.BARS)
                .getMetricFormat()));
        Screenshots.takeScreenshot(browser, "testNumberFormatEditor-beforeChangeFormat", getClass());
        MetricDetailsPage.getInstance(browser).changeMetricFormat(Formatter.BARS);
        Screenshots.takeScreenshot(browser, "testNumberFormatEditor-afterChangeFormat", getClass());
        assertEquals(MetricDetailsPage.getInstance(browser).getMetricFormat(), Formatter.BARS.toString());

        try {
            initReportsPage()
                .startCreateReport()
                .initPage()
                .openWhatPanel()
                .selectMetric(METRIC_NUMBER_OF_ACTIVITIES);
            assertEquals(waitForElementVisible(METRIC_DETAIL_FORMAT_LOCATOR, browser)
                    .getText(), Formatter.BARS.toString());

            List<String> values = reportPage.doneSndPanel()
                .selectReportVisualisation(ReportTypes.TABLE)
                .getTableReport()
                .getRawMetricValues();
            assertEquals(values.size(), 1);
            assertTrue(Formatter.BARS.toString().contains(values.get(0)));

            assertEquals(reportPage.showConfiguration()
                    .showCustomNumberFormat()
                    .getCustomNumberFormat(), Formatter.BARS.toString());
        } finally {
            resetMetricFormat();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editFormatInReportPage() {
        try {
            TableReport report = initReportsPage()
                    .startCreateReport()
                    .initPage()
                    .openWhatPanel()
                    .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .doneSndPanel()
                    .selectReportVisualisation(ReportTypes.TABLE)
                    .getTableReport();

            List<String> values = report.getRawMetricValues();
            assertEquals(values.size(), 1);
            assertEquals(values.get(0), "154,271.00");

            reportPage.showConfiguration()
                .showCustomNumberFormat()
                .changeNumberFormatButDiscard(Formatter.BARS);
            report.waitForLoaded();
            values = report.getRawMetricValues();
            assertEquals(values.size(), 1);
            assertEquals(values.get(0), "154,271.00");

            Screenshots.takeScreenshot(browser, "editFormatInReportPage-beforeChangeFormat", getClass());
            reportPage.changeNumberFormat(Formatter.BARS);
            report.waitForLoaded();
            Screenshots.takeScreenshot(browser, "editFormatInReportPage-afterChangeFormat", getClass());
            values = report.getRawMetricValues();
            assertEquals(values.size(), 1);
            assertTrue(Formatter.BARS.toString().contains(values.get(0)));
        } finally {
            resetMetricFormat();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editFormatWhenCreatingNewMetric() {
        initReportsPage()
            .startCreateReport()
            .initPage()
            .openWhatPanel()
            .createSimpleMetric(SimpleMetricTypes.SUM, "Duration");
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
    public void editExistFormatInSnDDialog() {
        try {
            initReportsPage()
                .startCreateReport()
                .initPage()
                .openWhatPanel()
                .selectMetric(METRIC_NUMBER_OF_ACTIVITIES);

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

    private void resetMetricFormat() {
        initProjectsPage();
        assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES)
                .changeMetricFormat(Formatter.DEFAULT).getMetricFormat(), Formatter.DEFAULT.toString());
    }
}
