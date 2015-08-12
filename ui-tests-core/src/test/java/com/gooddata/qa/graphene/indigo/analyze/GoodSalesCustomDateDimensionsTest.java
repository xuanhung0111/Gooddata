package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
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

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.google.common.collect.Sets;

public class GoodSalesCustomDateDimensionsTest extends AnalyticalDesignerAbstractTest {

    private static final String NUMBER = "Number";
    private static final String RETAIL_DATE = "Retaildate";

    @Test(dependsOnGroups = {"init"})
    public void datePresetsAppliedInReport() {
        initAnalysePage();
        analysisPage.addMetricFromFact(NUMBER).addCategory(DATE);

        assertTrue(analysisPage.isFilterVisible(RETAIL_DATE));
        assertEquals(analysisPage.getFilterText(RETAIL_DATE), RETAIL_DATE + ": All time");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        for (String period : Sets.newHashSet(analysisPage.getAllTimeFilterOptions())) {
            System.out.println(format("Try with time period [%s]", period));
            analysisPage.configTimeFilter(period).waitForReportComputing();
            if (analysisPage.isExplorerMessageVisible()) {
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
        analysisPage.addMetricFromFact(NUMBER)
            .addCategory(DATE)
            .configTimeFilterByRange(RETAIL_DATE, "07/13/2014", "08/11/2014")
            .waitForReportComputing();
        checkingOpenAsReport("dateRangeAppliedInReport");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyRecommendation() {
        initAnalysePage();
        analysisPage.addMetricFromFact(NUMBER);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertEquals(analysisPage.getFilterText(RETAIL_DATE), RETAIL_DATE + ": Last 4 quarters");
        assertThat(analysisPage.getAllAddedCategoryNames(), contains(DATE));
        assertThat(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), equalTo(4));
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("applyRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void testGranularityOfDate() {
        initAnalysePage();
        analysisPage.addMetricFromFact(NUMBER)
            .addCategory(DATE)
            .changeGranularity("Month")
            .waitForReportComputing();
        assertThat(analysisPage.getChartReport().getTrackersCount(), greaterThanOrEqualTo(1));

        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders();
        assertThat(headers, equalTo(asList("Short (Jan 2010) (Retaildate)".toUpperCase(),
                "Sum of Number".toUpperCase())));
    }

    @Test(dependsOnGroups = {"init"})
    public void testPopAndPercentOnCustomDate() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetricFromFact(NUMBER)
            .addCategory(DATE)
            .expandMetricConfiguration("Sum of " + NUMBER)
            .turnOnShowInPercents()
            .compareToSamePeriodOfYearBefore()
            .waitForReportComputing()
            .getChartReport();

        assertThat(report.getLegends(), equalTo(asList("% Sum of Number - previous year", "% Sum of Number")));
        checkingOpenAsReport("testPopAndPercentOnCustomDate");
    }
}
