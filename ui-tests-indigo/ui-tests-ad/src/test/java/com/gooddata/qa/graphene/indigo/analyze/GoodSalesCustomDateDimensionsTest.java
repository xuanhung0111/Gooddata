package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.google.common.collect.Sets;

public class GoodSalesCustomDateDimensionsTest extends AnalyticalDesignerAbstractTest {

    private static final String NUMBER = "Number";
    private static final String RETAIL_DATE = "Retaildate";

    @Test(dependsOnGroups = {"init"})
    public void datePresetsAppliedInReport() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER, FieldType.FACT).addDate();

        assertTrue(filtersBucket.isFilterVisible(RETAIL_DATE));
        assertEquals(filtersBucket.getFilterText(RETAIL_DATE), RETAIL_DATE + ": All time");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        for (String period : Sets.newHashSet(filtersBucket.getAllTimeFilterOptions())) {
            System.out.println(format("Try with time period [%s]", period));
            filtersBucket.configTimeFilter(period);
            if (analysisPage.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPage.getExplorerMessage()));
            } else {
                System.out.println(format("Time period [%s] is ok", period));
            }
            checkingOpenAsReport("datePresetsAppliedInReport - " + period);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void dateRangeAppliedInReport() throws ParseException {
        initAnalysePage();
        analysisPage.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .getFilterBuckets()
            .configTimeFilterByRange(RETAIL_DATE, "07/13/2014", "08/11/2014");
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("dateRangeAppliedInReport");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyRecommendation() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER, FieldType.FACT);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertEquals(analysisPage.getFilterBuckets().getFilterText(RETAIL_DATE), RETAIL_DATE + ": Last 4 quarters");
        assertThat(analysisPage.getCategoriesBucket().getItemNames(), contains(DATE));
        assertThat(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), equalTo(4));
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("applyRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void testGranularityOfDate() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .getCategoriesBucket()
            .changeGranularity("Month");
        assertThat(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), greaterThanOrEqualTo(1));

        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertThat(headers, equalTo(asList("month/year (retaildate)", "sum of number")));
    }

    @Test(dependsOnGroups = {"init"})
    public void testPopAndPercentOnCustomDate() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .waitForReportComputing()
            .getChartReport();

        analysisPage.getMetricsBucket()
            .getMetricConfiguration("Sum of " + NUMBER)
            .expandConfiguration()
            .showPercents()
            .showPop();

        analysisPage.waitForReportComputing();
        assertThat(report.getLegends(), equalTo(asList("% Sum of Number - previous year", "% Sum of Number")));
        checkingOpenAsReport("testPopAndPercentOnCustomDate");
    }
}
