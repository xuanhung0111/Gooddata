package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
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

import java.io.IOException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.base.Predicate;

public class GoodSalesGridModificationTest extends GoodSalesAbstractTest {
    
    private final static String AMOUNT_METRIC = "Amount-Metric";
    private final static String PROBABILITY_METRIC = "Probability-Metric";
    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String REPORT_WITHOUT_METRIC = "Report-Without-Metric";
    private final static String AMOUNT = "Amount";
    private final static String PROBABILITY = "Probability";
    private final static String STATUS = "Status";
    private final static String INTEREST = "Interest";
    private final static String STAGE_NAME = "Stage Name";
    private final static String STAGE = "Stage";
    private final static String ADD_NEW_ATTRIBUTE = "Add New Attribute";
    private final static String OPPORTUNITY = "Opportunity";
    private final static String ACCOUNT = "Account";
    private final static String SFDC_URL = "SFDC URL";
    private final static String METRIC_FORMAT = "#,##0.00";
    private final static String REGION = "Region";

    private final static String REPORT_NOT_COMPUTABLE_MESSAGE = "Report not computable due to improper metric definition";
    private final static String DISPLAY_LABEL = "http://www.google.com";

    @Test(dependsOnMethods = {"createProject"})
    public void setupProject() {
        final String stageNameUri = getMdService()
                .getObj(getProject(), Attribute.class, title(STAGE_NAME))
                .getDefaultDisplayForm()
                .getUri();

        final String amountUri = getMdService()
                .getObj(getProject(), Fact.class, title(AMOUNT))
                .getUri();

        final String probabilityUri = getMdService()
                .getObj(getProject(), Fact.class, title(PROBABILITY))
                .getUri();

        final String amountMetricUri = getMdService()
                .createObj(getProject(), new Metric(AMOUNT_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        METRIC_FORMAT))
                .getUri();

        final Metric probabilityMetric = getMdService().createObj(getProject(),
                new Metric(PROBABILITY_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", probabilityUri)),
                        METRIC_FORMAT));

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT, singletonList("metricGroup"),
                singletonList(new AttributeInGrid(stageNameUri)),
                asList(new GridElement(amountMetricUri, AMOUNT),
                        new GridElement(probabilityMetric.getUri(), PROBABILITY)));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    @Test(dependsOnMethods = {"setupProject"})
    public void breakDownMetricValue() {
        openReport(SIMPLE_REPORT).getTableReport()
                .openContextMenuFromCellValue("770,636,605.83")
                .selectItem("Break Down This Number");

        reportPage.selectAttribute("Year (Snapshot)").doneSndPanel().waitForReportExecutionProgress();

        takeScreenshot(browser, "break-down-metric-value", getClass());
        assertTrue(isEqualCollection(reportPage.getTableReport().getAttributeElements(),
                asList("2010", "2011", "2012", INTEREST)), "The expected attribute elements are not displayed");

        //use List.equals() to test that metric elements are computed correctly and have correct order
        assertTrue(reportPage.getTableReport().getMetricElements()
                .equals(asList(8094721.67f, 43.30f, 366082936.76f, 459.60f, 396458947.40f, 483.90f)),
                "The expected metric elements are not displayed");
    }

    @Test(dependsOnMethods = {"setupProject"})
    public void drillOnGridMenu() {
        openReport(SIMPLE_REPORT)
                .getTableReport()
                .openContextMenuFromCellValue(INTEREST)
                .selectItem("Break Down \"" + INTEREST + "\"");

        reportPage.selectAttribute(STATUS).doneSndPanel();

        takeScreenshot(browser, "drill-on-grid-menu", getClass());
        assertTrue(reportPage.getTableReport().getAttributesHeader().contains(STATUS),
                "Status attribute has not been added");
        assertTrue(reportPage.openFilterPanel().getFilterElement(STAGE_NAME + " is " + INTEREST).isDisplayed(),
                "The filter has not been added");
    }

    @Test(dependsOnMethods = {"setupProject"})
    public void filterOnGridMenu() {
        assertTrue(
                openReport(SIMPLE_REPORT).getTableReport()
                        .showOnly(INTEREST)
                        .getAttributeElements().equals(singletonList(INTEREST)),
                INTEREST + "is not the only attribute or not displayed");

        takeScreenshot(browser, "filter-on-grid-menu", getClass());
        //use List.equals() to test that metric elements are computed correctly and have correct order
        assertTrue(reportPage.getTableReport().getMetricElements().equals(asList(770636605.83f, 986.80f)),
                "The metrics elements are not as expected");
    }

    @Test(dependsOnMethods = {"setupProject"})
    public void addAttributeUsingGridMenu() {
        openReport(SIMPLE_REPORT).getTableReport()
                .openContextMenuFromCellValue(INTEREST)
                .selectItem(ADD_NEW_ATTRIBUTE);

        takeScreenshot(browser, "adding-attribute", getClass());
        reportPage.selectFolderLocation(STAGE).selectAttribute(STATUS).doneSndPanel().waitForReportExecutionProgress();
        assertTrue(reportPage.getTableReport().getAttributesHeader().contains(STATUS),
                "Status attribute has not been added");
        
    }

    @Test(dependsOnMethods = {"createProject"})
    public void manipulateWithOnlyAttributeReport() {
        initReportCreation().createReport(new UiReportDefinition()
                .withName(REPORT_WITHOUT_METRIC)
                .withHows(STAGE_NAME));

        assertFalse(
                openReport(REPORT_WITHOUT_METRIC).getTableReport()
                        .openContextMenuFromCellValue(STAGE_NAME)
                        .getGroupNames().contains("Totals"),
                "Totals group is displayed");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void drillAttributeToHyperlinkLabel() {
        final String hyperlinkReport = "Hyperlink-report";
        assertTrue(initAttributePage().initAttribute(OPPORTUNITY).isDrillToExternalPage(),
                "The attribute is not linked to external page");

        attributeDetailPage.clearDrillingSetting().setDrillToExternalPage();

        initReportCreation().createReport(new UiReportDefinition()
                .withName(hyperlinkReport)
                .withWhats(AMOUNT)
                .withHows(OPPORTUNITY)
                .withFilters(FilterItem.Factory.createAttributeFilter(OPPORTUNITY,
                        "1000Bulbs.com > Educationly", "1000Bulbs.com > PhoenixSoft",
                        "101 Financial > Educationly")));

        final TableReport table = reportPage.waitForReportExecutionProgress().getTableReport();

        table.verifyAttributeIsHyperlinkInReport();

        final String currentWindow = browser.getWindowHandle();

        table.drillOnMetricValue();
        checkExternalPageUrl(currentWindow);

        reportPage.openHowPanel()
                .selectAttribute(OPPORTUNITY)
                .changeDisplayLabel(SFDC_URL)
                .doneSndPanel()
                .waitForReportExecutionProgress();

        assertTrue(isEqualCollection(table.getAttributeElements(), asList(DISPLAY_LABEL, DISPLAY_LABEL, DISPLAY_LABEL)),
                "The label has not been applied");

        table.drillOnMetricValue();
        checkExternalPageUrl(currentWindow);
        reportPage.saveReport();

        initAttributePage().initAttribute(OPPORTUNITY).clearDrillingSetting().setDrillToAttribute(ACCOUNT);

        openReport(hyperlinkReport);
        table.drillOnAttributeValue();
        reportPage.waitForReportExecutionProgress();

        assertTrue(table.getAttributesHeader().contains(ACCOUNT), "The expected attribute is not displayed");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editHeadlineReport() throws ParseException, JSONException, IOException {
        final String headlineReport = "Headline-Report";
        final String metricName = "Simple-Metric";
        final String amountUri = getMdService().getObj(getProject(), Fact.class, title(AMOUNT)).getUri();

        final String simpleMetricUri = getMdService()
                .createObj(getProject(),
                        new Metric(metricName, "SELECT SUM([" + amountUri + "])", METRIC_FORMAT))
                .getUri();

        initReportCreation().createReport(new UiReportDefinition()
                .withName(headlineReport)
                .withWhats(new WhatItem(metricName, REGION))
                .withType(ReportTypes.HEADLINE));

        reportPage.getHeadlineReport().focus();
        assertTrue(
                isEqualCollection(
                        reportPage.waitForReportExecutionProgress()
                                .getTableReport()
                                .getAttributesHeader(),
                        singletonList(REGION)),
                "The expected attribute is not displayed");
        browser.navigate().back();
        reportPage.waitForReportExecutionProgress();

        assertNotNull(
                openReport(headlineReport).openHowPanel()
                        .selectAttribute(REGION)
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

        assertThat(openReport(headlineReport).getInvalidDataReportMessage(),
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