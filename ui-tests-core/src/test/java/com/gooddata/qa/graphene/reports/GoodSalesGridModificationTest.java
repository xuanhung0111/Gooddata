package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.base.Predicate;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PROBABILITY;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GoodSalesGridModificationTest extends GoodSalesAbstractTest {
    
    private final static String AMOUNT_METRIC = "Amount-Metric";
    private final static String PROBABILITY_METRIC = "Probability-Metric";
    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String REPORT_WITHOUT_METRIC = "Report-Without-Metric";
    private final static String INTEREST = "Interest";
    private final static String STAGE = "Stage";
    private final static String ADD_NEW_ATTRIBUTE = "Add New Attribute";
    private final static String SFDC_URL = "SFDC URL";
    private final static String METRIC_FORMAT = "#,##0.00";

    private final static String REPORT_NOT_COMPUTABLE_MESSAGE = "Report not computable due to improper metric definition";
    private final static String DISPLAY_LABEL = "http://www.google.com";

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();
        createProbabilityMetric();
        final Attribute stageName = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));

        final String amountUri = getMdService()
                .getObj(getProject(), Fact.class, title(FACT_AMOUNT))
                .getUri();

        final String probabilityUri = getMdService()
                .getObj(getProject(), Fact.class, title(METRIC_PROBABILITY))
                .getUri();

        final Metric amountMetric = getMdService()
                .createObj(getProject(), new Metric(AMOUNT_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        METRIC_FORMAT));

        final Metric probabilityMetric = getMdService().createObj(getProject(),
                new Metric(PROBABILITY_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", probabilityUri)),
                        METRIC_FORMAT));

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri(), stageName.getTitle())),
                asList(new MetricElement(amountMetric),
                        new MetricElement(probabilityMetric)));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void breakDownMetricValue() {
        initReportsPage().openReport(SIMPLE_REPORT).getTableReport()
                .openContextMenuFrom("770,636,605.83", CellType.METRIC_VALUE)
                .selectItem("Break Down This Number");

        reportPage.selectAttribute("Year (Snapshot)").doneSndPanel().waitForReportExecutionProgress();

        takeScreenshot(browser, "break-down-metric-value", getClass());
        assertTrue(isEqualCollection(reportPage.getTableReport().getAttributeValues(),
                asList("2010", "2011", "2012", INTEREST)), "The expected attribute elements are not displayed");

        //use List.equals() to test that metric elements are computed correctly and have correct order
        assertTrue(reportPage.getTableReport().getMetricValues()
                .equals(asList(8094721.67f, 43.30f, 366082936.76f, 459.60f, 396458947.40f, 483.90f)),
                "The expected metric elements are not displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnGridMenu() {
        initReportsPage().openReport(SIMPLE_REPORT)
                .getTableReport()
                .openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
                .selectItem("Break Down \"" + INTEREST + "\"");

        reportPage.selectAttribute(ATTR_STATUS).doneSndPanel();

        takeScreenshot(browser, "drill-on-grid-menu", getClass());
        assertTrue(reportPage.getTableReport().getAttributeHeaders().contains(ATTR_STATUS),
                "Status attribute has not been added");
        assertTrue(reportPage.openFilterPanel().getFilterElement(ATTR_STAGE_NAME + " is " + INTEREST).isDisplayed(),
                "The filter has not been added");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterOnGridMenu() {
        TableReport report = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        report.openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
                .selectItem(format("Show only \"%s\"", INTEREST));
        assertTrue(report.getAttributeValues().equals(singletonList(INTEREST)),
                INTEREST + "is not the only attribute or not displayed");

        takeScreenshot(browser, "filter-on-grid-menu", getClass());
        //use List.equals() to test that metric elements are computed correctly and have correct order
        assertTrue(report.getMetricValues().equals(asList(770636605.83f, 986.80f)),
                "The metrics elements are not as expected");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addAttributeUsingGridMenu() {
        initReportsPage().openReport(SIMPLE_REPORT).getTableReport()
                .openContextMenuFrom(INTEREST, CellType.ATTRIBUTE_VALUE)
                .selectItem(ADD_NEW_ATTRIBUTE);

        takeScreenshot(browser, "adding-attribute", getClass());
        reportPage.selectFolderLocation(STAGE).selectAttribute(ATTR_STATUS).doneSndPanel().waitForReportExecutionProgress();
        assertTrue(reportPage.getTableReport().getAttributeHeaders().contains(ATTR_STATUS),
                "Status attribute has not been added");

    }

    @Test(dependsOnGroups = {"createProject"})
    public void manipulateWithOnlyAttributeReport() {
        initReportCreation().createReport(new UiReportDefinition()
                .withName(REPORT_WITHOUT_METRIC)
                .withHows(ATTR_STAGE_NAME));

        assertFalse(
                initReportsPage().openReport(REPORT_WITHOUT_METRIC).getTableReport()
                        .openContextMenuFrom(ATTR_STAGE_NAME, CellType.ATTRIBUTE_HEADER)
                        .getGroupNames().contains("Totals"),
                "Totals group is displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillAttributeToHyperlinkLabel() {
        final String hyperlinkReport = "Hyperlink-report";
        assertTrue(initAttributePage().initAttribute(ATTR_OPPORTUNITY).isDrillToExternalPage(),
                "The attribute is not linked to external page");

        AttributeDetailPage.getInstance(browser).clearDrillingSetting().setDrillToExternalPage();

        initReportCreation().createReport(new UiReportDefinition()
                .withName(hyperlinkReport)
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_OPPORTUNITY)
                .withFilters(FilterItem.Factory.createAttributeFilter(ATTR_OPPORTUNITY,
                        "1000Bulbs.com > Educationly", "1000Bulbs.com > PhoenixSoft",
                        "101 Financial > Educationly")));

        final TableReport table = reportPage.waitForReportExecutionProgress().getTableReport();

        assertTrue(table.isDrillableToExternalPage(CellType.ATTRIBUTE_VALUE), "cannot drill report to external page");

        final String currentWindow = browser.getWindowHandle();

        table.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
        checkExternalPageUrl(currentWindow);

        reportPage.openHowPanel()
                .selectAttribute(ATTR_OPPORTUNITY)
                .changeDisplayLabel(SFDC_URL)
                .doneSndPanel()
                .waitForReportExecutionProgress();

        assertTrue(isEqualCollection(table.getAttributeValues(), asList(DISPLAY_LABEL, DISPLAY_LABEL, DISPLAY_LABEL)),
                "The label has not been applied");

        table.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
        checkExternalPageUrl(currentWindow);
        reportPage.saveReport();

        initAttributePage().initAttribute(ATTR_OPPORTUNITY).clearDrillingSetting().setDrillToAttribute(ATTR_ACCOUNT);

        initReportsPage().openReport(hyperlinkReport);
        table.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
        reportPage.waitForReportExecutionProgress();

        assertTrue(table.getAttributeHeaders().contains(ATTR_ACCOUNT), "The expected attribute is not displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editHeadlineReport() throws ParseException, JSONException, IOException {
        final String headlineReport = "Headline-Report";
        final String metricName = "Simple-Metric";
        final String amountUri = getMdService().getObj(getProject(), Fact.class, title(FACT_AMOUNT)).getUri();

        final String simpleMetricUri = getMdService()
                .createObj(getProject(),
                        new Metric(metricName, "SELECT SUM([" + amountUri + "])", METRIC_FORMAT))
                .getUri();

        initReportCreation().createReport(new UiReportDefinition()
                .withName(headlineReport)
                .withWhats(new WhatItem(metricName, ATTR_REGION))
                .withType(ReportTypes.HEADLINE));

        reportPage.getHeadlineReport().focus();
        assertTrue(
                isEqualCollection(
                        reportPage.waitForReportExecutionProgress()
                                .getTableReport()
                                .getAttributeHeaders(),
                        singletonList(ATTR_REGION)),
                "The expected attribute is not displayed");
        browser.navigate().back();
        reportPage.waitForReportExecutionProgress();

        assertNotNull(
                initReportsPage().openReport(headlineReport).openHowPanel()
                        .selectAttribute(ATTR_REGION)
                        .doneSndPanel()
                        .waitForReportExecutionProgress()
                        .getTableReport(),
                "The grid report is not displayed");
        browser.navigate().back();
        reportPage.waitForReportExecutionProgress();

        final String durationUri = getMdService().getObj(getProject(), Fact.class, title("Duration")).getUri();
        DashboardsRestUtils.changeMetricExpression(getRestApiClient(),
                simpleMetricUri,
                "SELECT SUM([" + amountUri + "]+[" + durationUri + "])");

        assertThat(initReportsPage().openReport(headlineReport).getInvalidDataReportMessage(),
                equalTo(REPORT_NOT_COMPUTABLE_MESSAGE));
    }

    private void checkExternalPageUrl(final String currentWindow) {
        try {
            final Predicate<WebDriver> predicate = browser -> browser.getWindowHandles().size() == 2;
            Graphene.waitGui().until(predicate);

            BrowserUtils.switchToLastTab(browser);
            WaitUtils.waitForStringMissingInUrl("about:blank");
            assertTrue(browser.getCurrentUrl().contains("www.google.com"), "The external link is not correct");
            browser.close();

        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

}