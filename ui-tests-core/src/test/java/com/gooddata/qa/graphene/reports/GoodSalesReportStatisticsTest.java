package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForSchedulesPageLoaded;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;

public class GoodSalesReportStatisticsTest extends GoodSalesAbstractTest {

    final static String SALES_REP = "Sales Rep";
    final static String SIMPLE_REPORT = "Simple-Report";

    final static By SIDE_BAR_SELECTOR = By.className("s-sidebar-open");

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpenOppsMetric();
        metricCreator.createNumberOfOpportunitiesBOPMetric();

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(SALES_REP).getDefaultDisplayForm().getUri(), SALES_REP)),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReportStatistics() {
        assertTrue(initReportsPage().openReport(SIMPLE_REPORT).showConfiguration().getReportStatistic().contains("0 Filters"));
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(SALES_REP,
                "Adam Bradley", "Alejandro Vabiano", "Alexsandr Fyodr"));
        assertTrue(reportPage.getReportStatistic().indexOf("3 Lines") > 0
                && reportPage.getReportStatistic().indexOf("1 Filters") > 0, "Report statistics is not updated");

        reportPage.showMoreReportInfo();
        checkUsedDataLink(METRIC_AMOUNT, DataType.METRIC);
        checkUsedDataLink(SALES_REP, DataType.ATTRIBUTE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReportUsage() {
        final String simpleDashboard = "Simple-Dashboard";
        final String scheduleEmailSubject = "Report-Usage-Test";
        initDashboardsPage().addNewDashboard(simpleDashboard)
                .editDashboard()
                .addReportToDashboard(SIMPLE_REPORT)
                .saveDashboard();

        initEmailSchedulesPage().scheduleNewReportEmail(testParams.getUser(), scheduleEmailSubject,
                "report usage test", SIMPLE_REPORT, ExportFormat.PDF);

        initReportsPage().openReport(SIMPLE_REPORT).showConfiguration().showMoreReportInfo();
        checkReportUsage(new ReportUsage(simpleDashboard, UsageLocation.DASHBOARD));
        checkReportUsage(new ReportUsage(scheduleEmailSubject, UsageLocation.SCHEDULE_EMAIL));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchWideAndNarrowWorkspace() {
        final String largeReport = "Large-Report";

        ReportDefinition definition = GridReportDefinitionContent.create(largeReport, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY).getDefaultDisplayForm().getUri(), 
                        ATTR_OPPORTUNITY)),
                createGridElements());
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initReportsPage().openReport(largeReport);
        assertTrue(browser.findElements(By.className("horizScrollbar")).isEmpty(), "Horizon scroll bar exists");
        reportPage.showConfiguration();
        waitForElementVisible(SIDE_BAR_SELECTOR, browser);
        assertFalse(browser.findElements(By.className("horizScrollbar")).isEmpty(),
                "Horizon scroll bar does not exists");
    }

    private List<MetricElement> createGridElements() {
        //different resolution could make the test failed
        //because report width which depends on number of metrics
        //is not enough to display horizon scroll bar
        //or the scroll bar is displayed at the beginning
        //recommended resolution is 1920x1080
        List<String> metricTitles = asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
                METRIC_NUMBER_OF_OPPORTUNITIES_BOP, METRIC_NUMBER_OF_OPPORTUNITIES);

        return metricTitles.stream()
                .map(metricTitle -> getMdService().getObj(getProject(), Metric.class, title(metricTitle)))
                .map(MetricElement::new)
                .collect(toList());
    }

    private void checkUsedDataLink(final String data, DataType type) {
        try {
            reportPage.openUsedData(data);
            BrowserUtils.switchToLastTab(browser);

            final String objectName;
            if (type.toString().equals(DataType.ATTRIBUTE.toString())) {
                objectName = AttributeDetailPage.getInstance(browser).getName();
            } else {
                objectName = MetricDetailsPage.getInstance(browser).getName();
            }
            assertEquals(objectName, data);
            browser.close();
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    private void checkReportUsage(final ReportUsage reportUsage) {
        try {
            reportPage.getReportUsageLinks().stream()
                    .filter(e -> e.getText().equals(reportUsage.getName()))
                    .findFirst()
                    .get()
                    .click();

            BrowserUtils.switchToLastTab(browser);
            final String objectName;
            if (reportUsage.getLocation().toString().equals(UsageLocation.DASHBOARD.toString())) {
                waitForDashboardPageLoaded(browser);
                objectName = dashboardsPage.getDashboardName();
            } else {
                waitForSchedulesPageLoaded(browser);
                objectName = EmailSchedulePage.getInstance(browser).getSubjectFromInput();
            }
            assertEquals(objectName, reportUsage.getName());
            browser.close();
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    private class ReportUsage {
        private String name;
        private UsageLocation location;

        public ReportUsage(final String name, UsageLocation location) {
            this.name = name;
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public UsageLocation getLocation() {
            return location;
        }

    }

    private enum UsageLocation {
        DASHBOARD("dashboard"),
        SCHEDULE_EMAIL("Schedule email");

        private final String location;

        private UsageLocation(final String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return location;
        }
    }

    private enum DataType {
        ATTRIBUTE("Attribute"),
        METRIC("Metric");

        private String type;

        private DataType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
