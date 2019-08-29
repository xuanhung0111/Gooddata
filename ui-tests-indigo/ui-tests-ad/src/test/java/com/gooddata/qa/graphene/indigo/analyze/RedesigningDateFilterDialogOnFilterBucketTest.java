package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.enums.DateRange.ALL_TIME;
import static com.gooddata.qa.graphene.enums.DateRange.STATIC_PERIOD;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_7_DAYS;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_30_DAYS;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_90_DAYS;
import static com.gooddata.qa.graphene.enums.DateRange.THIS_MONTH;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_MONTH;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_12_MONTHS;
import static com.gooddata.qa.graphene.enums.DateRange.THIS_QUARTER;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_QUARTER;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_4_QUARTERS;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_YEAR;
import static com.gooddata.qa.graphene.enums.DateRange.THIS_YEAR;


import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_TIMELINE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class RedesigningDateFilterDialogOnFilterBucketTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Redesign date filter dropdown to dialog";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createTimelineBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redesignTheDateFilterDialog() {
        List<String> datePeriods = asList(ALL_TIME.toString(), STATIC_PERIOD.toString(), LAST_7_DAYS.toString(),
                LAST_30_DAYS.toString(), LAST_90_DAYS.toString(), THIS_MONTH.toString(), LAST_MONTH.toString(),
                LAST_12_MONTHS.toString(), THIS_QUARTER.toString(), LAST_QUARTER.toString(), LAST_4_QUARTERS.toString(),
                THIS_YEAR.toString(), LAST_YEAR.toString());
        DateFilterPickerPanel dateFilterPickerPanel = initAnalysePage()
                .addDateFilter()
                .getFilterBuckets()
                .openDateFilterPickerPanel();
        DateDimensionSelect dateDimensionSelect = dateFilterPickerPanel.getDateDatasetSelect();
        assertTrue(isEqualCollection(dateDimensionSelect.getDateDimensionGroup("DEFAULT").getDateDimensions(),
                asList(DATE_DATASET_ACTIVITY, DATE_DATASET_CLOSED, DATE_DATASET_CREATED,
                        DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE)),
                        "The date dimensions which belong to no group are not correct");

        dateDimensionSelect.ensureDropdownClosed();
        assertEquals(dateFilterPickerPanel.getDatePresetSelect().getValues(), datePeriods);
        assertTrue(dateFilterPickerPanel.isApplyButtonVisible(), "The Apply button should be visible");
        assertTrue(dateFilterPickerPanel.isCancelButtonVisible(), "The Cancel button should be visible");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void chooseStaticPeriodOption() {
        DateFilterPickerPanel dateFilterPickerPanel = initAnalysePage()
                .addDateFilter()
                .getFilterBuckets()
                .openDateFilterPickerPanel()
                .selectStaticPeriod();
        assertTrue(dateFilterPickerPanel.isFromDateVisible(), "The From Date should be visible");
        assertTrue(dateFilterPickerPanel.isToDateVisible(), "The To Date should be visible");

        dateFilterPickerPanel.changePeriod(LAST_7_DAYS.toString());
        assertFalse(dateFilterPickerPanel.isFromDateVisible(), "The From Date should be disappeared");
        assertFalse(dateFilterPickerPanel.isToDateVisible(), "The To Date should be disappeared");
        assertTrue(dateFilterPickerPanel.isApplyButtonEnabled(), "The Apply button should be enabled");
        assertTrue(dateFilterPickerPanel.isCancelButtonEnabled(), "The Cancel button should be enabled");

        dateFilterPickerPanel.selectStaticPeriod()
                .configTimeFilterByRangeHelper("00/00/0000", "aa/bb/cccc")
                .changePeriod(LAST_7_DAYS.toString());
        assertFalse(dateFilterPickerPanel.isFromDateVisible(), "The From Date should be disappeared");
        assertFalse(dateFilterPickerPanel.isToDateVisible(), "The To Date should be disappeared");
        assertTrue(dateFilterPickerPanel.isApplyButtonEnabled(), "The Apply button should be enabled");
        assertTrue(dateFilterPickerPanel.isCancelButtonEnabled(), "The Cancel button should be enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyDateFilterDialog() {
        FiltersBucket filterBucket = initAnalysePage()
                .addMetric(METRIC_TIMELINE_BOP)
                .addDateFilter()
                .getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket
                .openDateFilterPickerPanel()
                .changeDateDimension(DATE_DATASET_CREATED)
                .changePeriod(LAST_YEAR.toString());
        dateFilterPickerPanel.apply();

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0, 0), hasItems(asList(METRIC_TIMELINE_BOP, "36,525")));
        assertEquals(parseFilterText(filterBucket.getDateFilterText()),
                asList(DATE_DATASET_CREATED, LAST_YEAR.toString()));

        filterBucket.openDateFilterPickerPanel().cancel();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0, 0), hasItems(asList(METRIC_TIMELINE_BOP, "36,525")));

        dateFilterPickerPanel = filterBucket.openDateFilterPickerPanel();
        assertEquals(dateFilterPickerPanel.getDateDatasetSelect().getSelection(), DATE_DATASET_CREATED);
        assertEquals(dateFilterPickerPanel.getDatePresetSelect().getSelection(), LAST_YEAR.toString());

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_TIMELINE_BOP)
                .expandConfiguration();
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0, 0), hasItems(asList(METRIC_TIMELINE_BOP, "36,525")));
        dateFilterPickerPanel = filterBucket.openDateFilterPickerPanel();
        assertEquals(dateFilterPickerPanel.getDateDatasetSelect().getSelection(), DATE_DATASET_CREATED);
        assertEquals(dateFilterPickerPanel.getDatePresetSelect().getSelection(), LAST_YEAR.toString());
    }
}
