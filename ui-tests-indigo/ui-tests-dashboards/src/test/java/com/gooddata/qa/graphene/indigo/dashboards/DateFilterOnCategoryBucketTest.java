package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class DateFilterOnCategoryBucketTest extends GoodSalesAbstractDashboardTest {

    private static final String INSIGHT_HAVING_ATTRIBUTE_CONFIGURATION = "Insight-Having-Attribute-Configuration";
    private static final String INSIGHT_HAVING_TREND_BY_CONFIGURATION = "Insight-Having-Trend-By-Configuration";
    private static final String INSIGHT_HAVING_VIEW_BY_CONFIGURATION = "Insight-Having-View-By-Configuration";

    @DataProvider
    public Object[][] categoryTypes() {
        return new Object[][] {
            {INSIGHT_HAVING_ATTRIBUTE_CONFIGURATION, CategoryType.ATTRIBUTE},
            {INSIGHT_HAVING_TREND_BY_CONFIGURATION, CategoryType.TREND_BY},
            {INSIGHT_HAVING_VIEW_BY_CONFIGURATION, CategoryType.VIEW_BY}
        };
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, dataProvider = "categoryTypes")
    public void createInsight(String insightName, CategoryType type) {
        AnalysisPage page =
                initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();
        page.getAttributesBucket().changeDateDimension(DATE_CREATED);
        page.waitForReportComputing().changeReportType(type.getReportType()).waitForReportComputing()
                .saveInsight(insightName);
    }

    @DataProvider
    public Object[][] InsightNames() {
        return new Object[][] {
            {INSIGHT_HAVING_ATTRIBUTE_CONFIGURATION},
            {INSIGHT_HAVING_TREND_BY_CONFIGURATION},
            {INSIGHT_HAVING_VIEW_BY_CONFIGURATION}
        };
    }

    @Test(dependsOnMethods = {"createInsight"}, dataProvider = "InsightNames")
    public void selectDateDatasetOnInsight(String insightName) {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(insightName)
                .waitForWidgetsLoading();
        checkSelectedDateDataset(DATE_CREATED, "Selected-Date-Dataset-On-" + insightName);
    }

    @Test(dependsOnMethods = {"createInsight"}, dataProvider = "InsightNames")
    public void testDateDatasetGroups(String insightName) {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(insightName)
                .waitForWidgetsLoading();
        checkDateDatasetGroups(singletonList(DATE_CREATED), singletonList(DATE_ACTIVITY),
                "Date-Dataset-Groups-On-" + insightName);
    }

    private void checkSelectedDateDataset(String expectedValue, String screenshotName) {
        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        takeScreenshot(browser, screenshotName, getClass());
        assertEquals(panel.getSelectedDataSet(), expectedValue, "Selected dataset is not correct");
        assertTrue(panel.isDateDataSetSelectCollapsed(), "Date dataset is not selected automatically");
    }

    private void checkDateDatasetGroups(List<String> expectedRecommenedValues, List<String> expectedOtherValues,
            String screenshotName) {
        DateDimensionSelect dropDown = indigoDashboardsPage.getConfigurationPanel().openDateDataSet();
        takeScreenshot(browser, screenshotName, getClass());
        assertEquals(dropDown.getDateDimensionGroup("RECOMMENDED").getDateDimensions(), expectedRecommenedValues);
        assertEquals(dropDown.getDateDimensionGroup("OTHER").getDateDimensions(), expectedOtherValues);
    }

    // map category type into report type in order to increase test readability
    private enum CategoryType {
        ATTRIBUTE(ReportType.TABLE),
        VIEW_BY(ReportType.COLUMN_CHART),
        TREND_BY(ReportType.LINE_CHART);

        private ReportType chartType;

        private CategoryType(ReportType chartType) {
            this.chartType = chartType;
        }

        public ReportType getReportType() {
            return this.chartType;
        }
    }
}
