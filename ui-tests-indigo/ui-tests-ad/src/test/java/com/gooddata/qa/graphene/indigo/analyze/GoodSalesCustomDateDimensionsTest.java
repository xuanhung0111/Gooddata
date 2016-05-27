package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;
import com.google.common.collect.Sets;

public class GoodSalesCustomDateDimensionsTest extends AbstractAnalyseTest {

    private static final String NUMBER = "Number";
    private static final String RETAIL_DATE = "Retaildate";

    private static final String FISCAL_CSV_PATH = "/" + UPLOAD_CSV + "/fiscal_dimension_sample_test.csv";
    private static final String FISCAL_DATASET = "Fiscal Dimension Sample Test";
    private static final String FISCAL_DATASET_ID = "dataset.csv_fiscal_dimension_sample_test";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Custom-Date-Dimension-Test";
    }

    @Override
    public void initStartPage() {
        startPageContext = new StartPageContext() {
            @Override
            public void waitForStartPageLoaded() {
                waitForFragmentVisible(analysisPageReact);
            }

            @Override
            public String getStartPage() {
                //load fiscal data set as default 
                //because FF is only refreshed when url is different from previous one
                return PAGE_UI_ANALYSE_PREFIX.replace("analyze", "analyze-new") + testParams.getProjectId()
                        + "/reportId/edit?dataset=" + FISCAL_DATASET_ID;
            }
        };
    }

    @Override
    public void prepareSetupProject() {
        uploadCSV(getFilePathFromResource(FISCAL_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + FISCAL_DATASET +"-dataset", getClass());
    }

    @Test(dependsOnGroups = {"init"})
    public void datePresetsAppliedInReport() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(NUMBER, FieldType.FACT).addDate();

        assertTrue(filtersBucketReact.isDateFilterVisible());
        assertEquals(filtersBucketReact.getDateFilterText(), RETAIL_DATE + ": All time");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        for (String period : Sets.newHashSet(filtersBucketReact.getDateFilterOptions())) {
            System.out.println(format("Try with time period [%s]", period));
            filtersBucketReact.configDateFilter(period);
            if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPageReact.getExplorerMessage()));
            } else {
                System.out.println(format("Time period [%s] is ok", period));
            }
            checkingOpenAsReport("datePresetsAppliedInReport - " + period);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void dateRangeAppliedInReport() throws ParseException {
        analysisPageReact.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .getFilterBuckets()
            .configDateFilter("07/13/2014", "08/11/2014");
        analysisPageReact.waitForReportComputing();
        checkingOpenAsReport("dateRangeAppliedInReport");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyRecommendation() {
        analysisPageReact.addMetric(NUMBER, FieldType.FACT);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertEquals(analysisPageReact.getFilterBuckets().getDateFilterText(), RETAIL_DATE + ": Last 4 quarters");
        assertThat(analysisPageReact.getAttributesBucket().getItemNames(), contains(DATE));
        assertThat(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount(), equalTo(4));
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("applyRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void testGranularityOfDate() {
        analysisPageReact.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .getAttributesBucket()
            .changeGranularity("Month");
        assertThat(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount(), greaterThanOrEqualTo(1));

        List<String> headers = analysisPageReact.changeReportType(ReportType.TABLE)
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
        ChartReportReact report = analysisPageReact.addMetric(NUMBER, FieldType.FACT)
            .addDate()
            .waitForReportComputing()
            .getChartReport();

        analysisPageReact.getMetricsBucket()
            .getMetricConfiguration("Sum of " + NUMBER)
            .expandConfiguration()
            .showPercents()
            .showPop();

        analysisPageReact.waitForReportComputing();
        assertThat(report.getLegends(), equalTo(asList("% Sum of Number - previous year", "% Sum of Number")));
        checkingOpenAsReport("testPopAndPercentOnCustomDate");
    }
}
