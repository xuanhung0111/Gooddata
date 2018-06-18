package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.google.common.collect.Sets;

public class CustomDateDimensionsTest extends AbstractAnalyseTest {

    private static final String NUMBER = "Number";
    private static final String RETAIL_DATE = "Retaildate";

    @Override
    public void initProperties() {
        // create empty project
        projectTitle = "Custom-Date-Dimension-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        URL maqlResource = getClass().getResource("/retail-date/maql.txt");
        setupMaql(IOUtils.toString(maqlResource, StandardCharsets.UTF_8));

        URL retailDateResouce = getClass().getResource("/retail-date/upload.zip");
        String webdavURL = uploadFileToWebDav(retailDateResouce, null);
        getFileFromWebDav(webdavURL, retailDateResouce);

        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void datePresetsAppliedInReport() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER, FieldType.FACT).addDate().waitForReportComputing();

        assertTrue(filtersBucketReact.isDateFilterVisible());
        assertEquals(filtersBucketReact.getDateFilterText(), RETAIL_DATE + ":\nAll time");

        for (String period : Sets.newHashSet(filtersBucketReact.getDateFilterOptions())) {
            System.out.println(format("Try with time period [%s]", period));
            filtersBucketReact.configDateFilter(period);
            if (analysisPage.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPage.getExplorerMessage()));
            } else {
                System.out.println(format("Time period [%s] is ok", period));
            }
            checkingOpenAsReport("datePresetsAppliedInReport - " + period);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dateRangeAppliedInReport() throws ParseException {
        analysisPage.addMetric(NUMBER, FieldType.FACT)
                .addDate()
                .getFilterBuckets()
                .configDateFilter("07/13/2014", "08/11/2014");
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("dateRangeAppliedInReport");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyRecommendation() {
        analysisPage.addMetric(NUMBER, FieldType.FACT).waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertEquals(analysisPage.getFilterBuckets().getDateFilterText(), RETAIL_DATE + ":\nLast 4 quarters");
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertThat(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), lessThanOrEqualTo(4));
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        checkingOpenAsReport("applyRecommendation");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testGranularityOfDate() {
        analysisPage.addMetric(NUMBER, FieldType.FACT)
                .addDate()
                .getAttributesBucket()
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

    @Test(dependsOnGroups = {"createProject"})
    public void testPopAndPercentOnCustomDate() {
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
