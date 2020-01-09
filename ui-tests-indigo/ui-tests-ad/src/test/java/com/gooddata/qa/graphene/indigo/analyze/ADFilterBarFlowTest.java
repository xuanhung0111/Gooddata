package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.report.Attribute;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.*;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FilterBarPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.manage.AttributePage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class ADFilterBarFlowTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TEST_SWITCHING = "Switching Insight";
    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Updated AD filter bar flow";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createTimelineEOPMetric();
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());

        // TODO: BB-1675 enableNewADFilterBar FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_MEASURE_VALUE_FILTERS, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addAttributesFromCatalogIntoBucket() {
        addAttributesIntoBucket();
        addTheSameAttributeIntoBucket();
    }

    public void addAttributesIntoBucket() {
        FiltersBucket filtersBucket = initAnalysePage().addAttribute(ATTR_DEPARTMENT).getFilterBuckets();
        assertFalse(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "Attributes should be not automatically added to filter bar");

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertEquals(filterBarPicker.getValuesText(), asList(ATTR_DEPARTMENT));
        filterBarPicker.checkItem(ATTR_DEPARTMENT).apply();

        assertTrue(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "Attributes should be automatically added to filter bar");
        analysisPage.openFilterBarPicker().uncheckItem(ATTR_DEPARTMENT).apply();
        assertFalse(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "Attributes should be automatically added to filter bar");
    }

    public void addTheSameAttributeIntoBucket() {
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE)
            .addStack(ATTR_DEPARTMENT);
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();

        assertEquals(Collections.frequency(filterBarPicker.getValuesText(), ATTR_DEPARTMENT), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addAndRemoveAttributesFromCatalogIntoBucket() {
        addAndRenameAttributeIntoBucket();
        removeAttributeByUnTickingCheckboxAndDroppingAttribute();
    }

    public void addAndRenameAttributeIntoBucket() {
        AttributesBucket attributesBucket = initAnalysePage()
            .addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT).getAttributesBucket();
        attributesBucket.setTitleItemBucket(ATTR_DEPARTMENT, "Renamed Department");

        assertEquals(attributesBucket.getItemNames(), asList("Renamed Department\n" + ATTR_DEPARTMENT));
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();

        assertThat(filterBarPicker.getValuesText(), hasItem(ATTR_DEPARTMENT));

        filterBarPicker.checkItem(ATTR_DEPARTMENT).apply();

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertEquals(parseFilterText(filtersBucket.getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));

        filtersBucket.configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("$80,406,324.96"));
    }

    public void removeAttributeByUnTickingCheckboxAndDroppingAttribute() {
        analysisPage.openFilterBarPicker().uncheckItem(ATTR_DEPARTMENT).apply();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertFalse(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "The attribute should be removed from filter bar");

        analysisPage.openFilterBarPicker().checkItem(ATTR_DEPARTMENT).apply();
        analysisPage.removeFilter(ATTR_DEPARTMENT);
        assertFalse(analysisPage.openFilterBarPicker().isItemCheck(ATTR_DEPARTMENT),
            "State should be not ticked");

        FilterBarPicker filterBarPicker = initAnalysePage().addAttribute(ATTR_DEPARTMENT).addFilter(ATTR_DEPARTMENT)
            .openFilterBarPicker();
        assertTrue(filterBarPicker.isItemCheck(ATTR_DEPARTMENT), "Default state should be ticked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addOrRemoveDateAddedFromCatalogIntoBucket() {
        addDateIntoBucket();
        renameDateIntoBucket();
        removeDateByUnTickingCheckboxAndDropping();
    }

    public void addDateIntoBucket() {
        FiltersBucket filtersBucket = initAnalysePage().addDate().getFilterBuckets();
        assertFalse(filtersBucket.isDateFilterVisible(), "Date should be not automatically added to filter bar");

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemCheck(DATE), "Default state Date should be un ticked");
    }

    public void renameDateIntoBucket() {
        analysisPage.getAttributesBucket().setTitleItemBucket("Date", "Renamed Date");
        analysisPage.openFilterBarPicker().checkItem(DATE).apply();
        assertThat(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()), hasItem("All time"));
    }

    public void removeDateByUnTickingCheckboxAndDropping() {
        analysisPage.openFilterBarPicker().uncheckItem(DATE).apply();
        analysisPage.addDateFilter();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertThat(parseFilterText(filtersBucket.getDateFilterText()), hasItem("All time"));

        analysisPage.openFilterBarPicker().uncheckItem(DATE).apply();
        assertFalse(analysisPage.openFilterBarPicker().isItemCheck(DATE),
            "Date should be not checked");
        assertFalse(filtersBucket.isDateFilterVisible(), "Date should be removed to filter bar");

        analysisPage.openFilterBarPicker().checkItem(DATE).apply();
        analysisPage.removeDateFilter();
        assertFalse(analysisPage.openFilterBarPicker().isItemCheck("Date"),
            "Date should be not checked");
        assertFalse(filtersBucket.isDateFilterVisible(), "Date should be removed to filter bar");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dragAndDropAttributeFromCatalogToFilterBar() {
        dragAndDropDateAndAttributeToFilterBar();
        removeDateAndAttributeFromFilterBarToCatalog();
        dragAndDropTwentyAttributeToFilterBar();
    }

    public void dragAndDropDateAndAttributeToFilterBar() {
        initAnalysePage().addDateFilter().addFilter(ATTR_DEPARTMENT);
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertTrue(filterBarPicker.isItemCheck("Date"), "Attribute should be checked");
        assertEquals(filterBarPicker.getValuesText(), asList("Date", ATTR_DEPARTMENT));
    }

    public void removeDateAndAttributeFromFilterBarToCatalog() {
        analysisPage.removeDateFilter().removeFilter(ATTR_DEPARTMENT);
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertFalse(filtersBucket.isDateFilterVisible(), "Date should be removed to filter bar");
        assertFalse(filtersBucket.isFilterVisible(ATTR_DEPARTMENT), "Attribute should be removed to filter bar");
        assertFalse(analysisPage.isFilterBarButtonEnabled(), "Filter Bar Button should be not enabled");
    }

    public void dragAndDropTwentyAttributeToFilterBar() {
        String firstComputedAttribute = createComputedAttributeUsing(ATTR_DEPARTMENT);
        String secondComputedAttribute = createComputedAttributeUsing(ATTR_DEPARTMENT);

        initAnalysePage();
        Stream.of(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT,
            ATTR_FORECAST_CATEGORY, ATTR_IS_ACTIVE, ATTR_IS_CLOSED, ATTR_IS_TASK, ATTR_IS_WON,
            ATTR_OPP_SNAPSHOT, ATTR_OPPORTUNITY, ATTR_PRIORITY, ATTR_PRODUCT, ATTR_REGION, ATTR_SALES_REP,
            ATTR_STAGE_HISTORY, ATTR_STAGE_NAME, ATTR_STATUS, firstComputedAttribute, secondComputedAttribute)
        .forEach(attribute -> analysisPage.addFilter(attribute));
        analysisPage.addDate().openFilterBarPicker().checkItem(DATE).apply();

        assertEquals(analysisPage.waitForReportComputing().getFilterBuckets()
            .getFiltersCount(), 21);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addAttributeViaRecommendedStepsPanel() {
        dragAndDropAttributeIntoCanvas();
        applyCompareBetweenEachAttributePanel();
        applySeeTrendQuarterPanel();
        applyCompareToTheSamePeriodInPreviousYear();
    }

    public void dragAndDropAttributeIntoCanvas() {
        initAnalysePage().drag(analysisPage.getCatalogPanel().searchAndGet(ATTR_DEPARTMENT, FieldType.ATTRIBUTE),
            () -> waitForElementVisible(cssSelector(".s-recommendation-attribute-canvas"), browser))
            .waitForReportComputing();

        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertEquals(attributesBucket.getItemNames(), Collections.singletonList(ATTR_DEPARTMENT));
        assertFalse(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "Should be not auto-created them into filter bar.\n ");

    }

    public void applyCompareBetweenEachAttributePanel() {
        initAnalysePage().addMetric(METRIC_AMOUNT);

        RecommendationContainer recommendationContainer =
            Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
            recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACCOUNT).apply();

        analysisPage.waitForReportComputing();

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), Collections.singletonList(ATTR_ACCOUNT));
        assertFalse(analysisPage.getFilterBuckets().isFilterVisible(ATTR_ACCOUNT),
            "Should be not auto-created them into filter bar.\n ");
    }

    public void applySeeTrendQuarterPanel() {
        initAnalysePage().addMetric(METRIC_TIMELINE_EOP);
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<TrendingRecommendation>getRecommendation(RecommendationStep.SEE_TREND)
            .select(DateGranularity.QUARTER.toString()).apply();

        analysisPage.waitForReportComputing();

        assertThat(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()),
            hasItem("Last 4 quarters"));
        assertTrue(analysisPage.openFilterBarPicker().isItemCheck(DATE),
            "Date should be ticked on filter bar dropdown");
        ChartReport chartReport = analysisPage.getChartReport();

        assertEquals(chartReport.getDataLabels(), asList("44,195", "44,195", "44,195", "44,195"));
    }

    public void applyCompareToTheSamePeriodInPreviousYear() {
        Graphene.createPageFragment(RecommendationContainer.class,
            waitForElementVisible(RecommendationContainer.LOCATOR, browser))
            .<ComparisonRecommendation>getRecommendation(RecommendationStep.COMPARE).apply();

        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("44,195", "44,195", "44,195", "44,195",
            "44,195", "44,195", "44,195", "44,195"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dragMetricIntoTrendingOverTimePanel() {
        WebElement metric = initAnalysePage().getCatalogPanel().searchAndGet(METRIC_TIMELINE_EOP, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        ChartReport report = analysisPage.drag(metric, trendRecommendation)
            .waitForReportComputing().getChartReport();
        assertThat(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()),
            hasItem("Last 4 quarters"));
        assertTrue(analysisPage.openFilterBarPicker().isItemCheck(DATE),
            "Date should be ticked on filter bar dropdown");

        assertEquals(report.getDataLabels(), asList("44,195", "44,195", "44,195", "44,195"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyCompareToPreviousPeriodTestViaRecommended() {
        initAnalysePage().addMetric(METRIC_TIMELINE_EOP).addAttribute(ATTR_DEPARTMENT);

        RecommendationContainer recommendationContainer =
            Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
            recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(DateRange.THIS_QUARTER.toString()).apply();

        analysisPage.waitForReportComputing();

        assertThat(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()).toString(),
            containsString(DateRange.THIS_QUARTER.toString() + "\n" +
                "Compare (all) to, Same period (SP) previous year"));

        assertTrue(analysisPage.openFilterBarPicker().isItemCheck(DATE),
            "Date should be ticked on filter bar dropdown");
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("44,195", "44,195", "44,195", "44,195"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceAttributeOrDateFromBucketNotOnFilterBar() {
        initAnalysePage().addAttribute(ATTR_FORECAST_CATEGORY).addDate();
        analysisPage.replaceAttribute(DATE, ATTR_IS_CLOSED);

        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_IS_CLOSED));
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemCheck(ATTR_IS_CLOSED),
            "New attribute should be created on filter bar dropdown with state is unchecked");
        assertEquals(filterBarPicker.getValuesText(), asList(ATTR_FORECAST_CATEGORY, ATTR_IS_CLOSED));
        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 0);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceAttributeOrDateFromBucketOnFilterBar() {
        analysisPage.openFilterBarPicker().checkItem(ATTR_IS_CLOSED);
        analysisPage.replaceAttribute(ATTR_IS_CLOSED, ATTR_DEPARTMENT);

        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemCheck(ATTR_DEPARTMENT),
            "New attribute should be created on filter bar dropdown with state is unchecked");
        assertEquals(filterBarPicker.getValuesText(), asList(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));

        assertFalse(analysisPage.getFilterBuckets().isFilterVisible(ATTR_IS_CLOSED),
            "Previous attribute should be still kept on filter bar");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceAttributeFromBucket() {
        replaceNewAttributeOnViewByAndFilterBar();
        replaceNewAttributeOnViewByAndNotFilterBar();
    }

    public void replaceNewAttributeOnViewByAndFilterBar() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration("Column chart", ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_STAGE_NAME),
                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight("Column chart").replaceStack(ATTR_FORECAST_CATEGORY);

        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        assertThat(analysisPage.openFilterBarPicker().getValuesText(), hasItems(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 0);
    }

    public void replaceNewAttributeOnViewByAndNotFilterBar() {
        FilterBarPicker filterBarPicker = initAnalysePage().openInsight("Column chart").openFilterBarPicker();
        filterBarPicker.checkItem(ATTR_FORECAST_CATEGORY).checkItem(ATTR_STAGE_NAME).apply();
        analysisPage.replaceStack(ATTR_FORECAST_CATEGORY);

        assertEquals(analysisPage.getStacksBucket().getItemNames(), asList(ATTR_FORECAST_CATEGORY));
        assertThat(analysisPage.openFilterBarPicker().getValuesText(), hasItems(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT, ATTR_STAGE_NAME));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible(ATTR_STAGE_NAME));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void reorderAttributeFromBucket() {
        reorderAttributeOrDateInABucket();
        reorderAttributeOrDateBetweenBuckets();
        disappearDateWhenReorderAttribute();
    }

    public void reorderAttributeOrDateInABucket() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration("Reorder Insight", ReportType.COLUMN_CHART)
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_STAGE_NAME),
                        CategoryBucket.Type.STACK))));


        initAnalysePage().openInsight("Reorder Insight").reorderAttribute(ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT);
        assertEquals(analysisPage.openFilterBarPicker().getValuesText(),
            asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_STAGE_NAME));

        analysisPage.replaceAttributeWithDate(ATTR_DEPARTMENT);
        assertEquals(analysisPage.openFilterBarPicker().getValuesText(),
            asList(DATE, ATTR_FORECAST_CATEGORY, ATTR_STAGE_NAME));
    }

    public void reorderAttributeOrDateBetweenBuckets() {
        analysisPage.reorderRowAndColumn(ATTR_STAGE_NAME, DATE);
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_STAGE_NAME, ATTR_FORECAST_CATEGORY));
        assertThat(analysisPage.openFilterBarPicker().getValuesText(), not(hasItem(DATE)));
    }

    public void disappearDateWhenReorderAttribute() {
        analysisPage.undo().openFilterBarPicker().checkItem(DATE).apply();
        analysisPage.reorderRowAndColumn(ATTR_STAGE_NAME, DATE);
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_STAGE_NAME, ATTR_FORECAST_CATEGORY));
        assertTrue(analysisPage.getFilterBuckets().isDateFilterVisible(), "Date should be still kept on filter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchingBetweenTypesOfInsight () {
        switchInsightsNotConfiguringAttributeFilter();
        switchInsightsConfiguringAttributeFilter();
    }

    public void switchInsightsNotConfiguringAttributeFilter() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_TEST_SWITCHING, ReportType.COLUMN_CHART)
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_STAGE_NAME),
                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_TEST_SWITCHING).changeReportType(ReportType.HEAD_LINE);

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        assertEquals(metricsBucket.getItemNames(), emptyList());

        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
        assertEquals(metricsSecondaryBucket.getItemNames(), emptyList());
        assertFalse(analysisPage.isFilterBarButtonEnabled(), "Filter Bar Button should be disabled");
    }

    public void switchInsightsConfiguringAttributeFilter() {
        analysisPage.changeReportType(ReportType.COLUMN_CHART).openFilterBarPicker().checkItem(ATTR_FORECAST_CATEGORY)
            .checkItem(ATTR_DEPARTMENT).apply();

        analysisPage.changeReportType(ReportType.HEAD_LINE);
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertTrue(filtersBucket.isFilterVisible(ATTR_FORECAST_CATEGORY),
            "Attribute still will be remained on filter bar");
        assertTrue(filtersBucket.isFilterVisible(ATTR_DEPARTMENT),
            "Attribute still will be remained on filter bar");

        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertTrue(filterBarPicker.isItemCheck(ATTR_FORECAST_CATEGORY),
            "Attribute still be listed in the dropdown with ticked checkbox");
        assertTrue(filterBarPicker.isItemCheck(ATTR_DEPARTMENT),
            "Attribute still be listed in the dropdown with ticked checkbox");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoOrRedoViaFilterBarDropdown() {
        initAnalysePage().addAttribute(ATTR_DEPARTMENT).openFilterBarPicker().checkItem(ATTR_DEPARTMENT).apply();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        analysisPage.undo();
        assertEquals(filtersBucket.getFiltersCount(), 0);
        analysisPage.redo();
        assertEquals(filtersBucket.getFiltersCount(), 1);
        analysisPage.clear();
        assertFalse(analysisPage.isFilterBarButtonEnabled(), "Filter button should be disabled with tooltip");
    }

    private String createComputedAttributeUsing(String attributeTitle) {
        String name = "CA " + System.currentTimeMillis();
        initAttributePage()
            .moveToCreateAttributePage()
            .createComputedAttribute(new ComputedAttributeDefinition().withAttribute(attributeTitle)
                .withMetric(METRIC_AMOUNT)
                .withName(name));
        return name;
    }
}
