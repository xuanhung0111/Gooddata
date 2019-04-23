package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PROBABILITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.ScrollType;
import org.testng.annotations.Test;

public class DragAndDropMetricsOrAttributesColumnOnReportTest extends GoodSalesAbstractTest {

    private final String REORDERING_METRICS_AND_ATTRIBUTE_PIVOT = "Reordering multi Metrics And multi Attributes Pivot";
    private final String REORDERING_METRIC_AND_ATTRIBUTES = "Reodering has single Metric And multi Attributes";
    private final String REORDERING_METRICS_AND_ATTRIBUTES = "Reordering multi Metrics And multi Attributes";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "Report with reordering metric columns";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createProbabilityMetric();
        metrics.createAvgAmountMetric();
        metrics.createQuotaMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testReorderingMetricOnReportPivot() {
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
                .withName(REORDERING_METRICS_AND_ATTRIBUTE_PIVOT)
                .withWhats(new WhatItem(METRIC_AMOUNT))
                .withWhats(new WhatItem(METRIC_PROBABILITY))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT)),
                REORDERING_METRICS_AND_ATTRIBUTE_PIVOT);
        initReportsPage().openReport(REORDERING_METRICS_AND_ATTRIBUTE_PIVOT);
        reportPage.reorderMetric(METRIC_PROBABILITY, METRIC_AMOUNT);
        // list of metrics 0: METRIC_AMOUNT 1: METRIC_PROBABILITY
        reportPage.waitForReportExecutionProgress();
        assertEquals(reportPage.getMetricTitle(0), METRIC_PROBABILITY,
                "Probability should be in left side of Amount");
    }

    @Test(dependsOnMethods = {"testReorderingMetricOnReportPivot"})
    protected void testReorderingAttributeOnReportPivot() {
        initReportsPage().openReport(REORDERING_METRICS_AND_ATTRIBUTE_PIVOT);
        // list of attributes 0: ATTR_YEAR_SNAPSHOT 1: ATTR_QUARTER_YEAR_SNAPSHOT
        // 2: ATTR_STAGE_NAME 3: ATTR_OPPORTUNITY
        reportPage.reorderAttribute(ATTR_OPPORTUNITY, ATTR_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();
        assertEquals(reportPage.getAttributeTitle(2), ATTR_OPPORTUNITY,
                "Opportunity should be in left side of Stage Name");
        reportPage.reorderAttribute(ATTR_YEAR_SNAPSHOT, ATTR_STAGE_NAME);
        reportPage.waitForReportExecutionProgress();
        assertEquals(reportPage.getAttributeTitle(2), ATTR_YEAR_SNAPSHOT,
                "Year (Snapshot) should be in left side of Stage Name");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testReorderingSingleMetricAndMultiAttributesOnReport() {
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
                .withName(REORDERING_METRIC_AND_ATTRIBUTES)
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT)),
                REORDERING_METRIC_AND_ATTRIBUTES);
        initReportsPage().openReport(REORDERING_METRIC_AND_ATTRIBUTES);
        reportPage.openHowPanel().selectItem(ATTR_ACCOUNT).done();
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Account", "1000Bulbs.com",
                "101 Financial", "123 Exteriors", "14 West", "1-800 Postcards", "1-800 We Answer", "1-888-OhioComp",
                "1 Source Consulting", "1st Choice Staffing & Consulting", "1st in Video - Music World", "2 Wheel Bikes",
                "352 Media Group", "2HB Software Designs"));
        reportPage.waitForReportExecutionProgress();
        TableReport report = reportPage.getTableReport();
        assertTrue(report.scrollIntoViewAndCheckValue("Walling Data > Educationly", ScrollType.VERT),
                "Report isn't rendered correctly without any error for firefox browser.");
        reportPage.removeAttribute(ATTR_YEAR_SNAPSHOT);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(ATTR_YEAR_SNAPSHOT), "Year (Snapshot) should be removed");
        reportPage.removeMetric(METRIC_AMOUNT);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(METRIC_AMOUNT), "Amount should be removed");
        assertTrue(report.scrollIntoViewAndCheckValue("Q4/2011", ScrollType.HORI),
                "Report is rendered correctly without any error.");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testReorderingMultiMetricsAndMultiAttributesOnReport() {
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
                .withName(REORDERING_METRICS_AND_ATTRIBUTES)
                .withWhats(new WhatItem(METRIC_AMOUNT))
                .withWhats(new WhatItem(METRIC_AVG_AMOUNT))
                .withWhats(new WhatItem(METRIC_PROBABILITY))
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP))
                .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT))
                .withHows(new HowItem(ATTR_OPPORTUNITY, Position.LEFT)),
                REORDERING_METRICS_AND_ATTRIBUTES);
        initReportsPage().openReport(REORDERING_METRICS_AND_ATTRIBUTES).waitForReportExecutionProgress();
        reportPage.openHowPanel().selectItem(ATTR_ACCOUNT).done();
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Account", "1000Bulbs.com",
                "101 Financial", "123 Exteriors", "14 West", "1-800 Postcards", "1-800 We Answer", "1-888-OhioComp",
                "1 Source Consulting", "1st Choice Staffing & Consulting", "1st in Video - Music World", "2 Wheel Bikes",
                "352 Media Group", "2HB Software Designs"));
        reportPage.waitForReportExecutionProgress();
        TableReport report = reportPage.getTableReport();
        assertTrue(report.scrollIntoViewAndCheckValue("Walling Data > Educationly", ScrollType.VERT),
                "Report isn't rendered correctly without any error for firefox browser.");
        reportPage.removeMetric(METRIC_AMOUNT);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(METRIC_AMOUNT), "Amount should be removed");
        reportPage.removeMetric(METRIC_AVG_AMOUNT);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(METRIC_AVG_AMOUNT), "Avg. Amount should be removed");
        assertTrue(report.scrollIntoViewAndCheckValue("Q4/2011", ScrollType.HORI),
                "Report is rendered correctly without any error.");
    }

    @Test(dependsOnMethods = {"testReorderingMultiMetricsAndMultiAttributesOnReport"})
    protected void testDrogAndDropWhenAddedOrRemovedMetricOnReport() {
        initReportsPage().openReport(REORDERING_METRICS_AND_ATTRIBUTES).waitForReportExecutionProgress();
        reportPage.openWhatPanel().selectItem(METRIC_QUOTA).done();
        reportPage.waitForReportExecutionProgress();
        TableReport report = reportPage.getTableReport();
        assertTrue(report.checkValue(METRIC_QUOTA), "Quota should be added");
        report.sortBy(METRIC_QUOTA, TableReport.CellType.METRIC_HEADER, Sort.ASC).waitForLoaded();
        reportPage.openHowPanel().selectItem(ATTR_ACCOUNT).done();
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Account", "1000Bulbs.com",
                "101 Financial", "123 Exteriors", "14 West", "1-800 Postcards", "1-800 We Answer", "1-888-OhioComp",
                "1 Source Consulting", "1st Choice Staffing & Consulting", "1st in Video - Music World", "2 Wheel Bikes",
                "352 Media Group", "2HB Software Designs"));
        assertTrue(report.scrollIntoViewAndCheckValue("Walling Data > Educationly", ScrollType.VERT),
                "Report isn't rendered correctly without any error for firefox browser.");
        reportPage.removeMetric(METRIC_QUOTA);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(METRIC_QUOTA), "Quota should be removed");
        assertTrue(report.scrollIntoViewAndCheckValue("Q4/2011", ScrollType.HORI),
                "Report is rendered correctly without any error.");
    }

    @Test(dependsOnMethods = {"testDrogAndDropWhenAddedOrRemovedMetricOnReport"})
    protected void testDrogAndDropWhenAddedOrRemovedAttributeOnReport() {
        initReportsPage().openReport(REORDERING_METRICS_AND_ATTRIBUTES).waitForReportExecutionProgress();
        reportPage.openHowPanel().selectItem(ATTR_ACCOUNT).done();
        reportPage.waitForReportExecutionProgress();
        TableReport report = reportPage.getTableReport();
        assertTrue(report.checkValue(ATTR_ACCOUNT), "Account should be added");
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Account", "1000Bulbs.com",
                "101 Financial", "123 Exteriors", "14 West", "1-800 Postcards", "1-800 We Answer", "1-888-OhioComp",
                "1 Source Consulting", "1st Choice Staffing & Consulting", "1st in Video - Music World", "2 Wheel Bikes",
                "352 Media Group", "2HB Software Designs"));
        report.sortBy(ATTR_STAGE_NAME, TableReport.CellType.ATTRIBUTE_HEADER, Sort.ASC).waitForLoaded();
        assertTrue(report.scrollIntoViewAndCheckValue("Walling Data > Educationly", ScrollType.VERT),
                "Report isn't rendered correctly without any error for firefox browser.");
        reportPage.removeAttribute(ATTR_ACCOUNT);
        reportPage.waitForReportExecutionProgress();
        assertFalse(report.checkValue(ATTR_ACCOUNT), "Account should be removed");
        assertTrue(report.scrollIntoViewAndCheckValue("Q4/2011", ScrollType.HORI),
                "Report is rendered correctly without any error.");
    }
}
