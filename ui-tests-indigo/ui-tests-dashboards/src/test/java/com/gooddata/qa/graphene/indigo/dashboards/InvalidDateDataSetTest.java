package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_TIMELINE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class InvalidDateDataSetTest extends AbstractDashboardTest {

    private static final String INSIGHT_USING_DATE_FILTER = "Insight-Using-Date-Filter";
    private static final String DATE_DIMENSION_ERROR_MESSAGE =
            " can no longer be applied to the insight. Select a different dimension or edit the insight.";

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getReportCreator().createActiveLevelReport();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testPreviouslySelectedDateDataSet() {
        AnalysisPage page =
                initAnalysePage().addMetric(METRIC_OPP_FIRST_SNAPSHOT).addDateFilter().waitForReportComputing();
        page.getFilterBuckets().changeDateDimension(DATE_DATASET_TIMELINE);
        page.waitForReportComputing().saveInsight(INSIGHT_USING_DATE_FILTER);

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_USING_DATE_FILTER);

        assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_TIMELINE,
                DATE_DATASET_TIMELINE + " is not selected");

        indigoDashboardsPage.saveEditModeWithWidgets();

        initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER).waitForReportComputing()
                .removeMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing()
                .saveInsight();

        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        takeScreenshot(browser, "Unrelated-Date-DataSet-Error-Message", getClass());
        assertEquals(panel.getDateDataSetError(), "\"" + DATE_DATASET_TIMELINE + "\"" + DATE_DIMENSION_ERROR_MESSAGE,
                "Error msg is not correct");

        DateDimensionSelect dropdown = panel.openDateDataSet();
        takeScreenshot(browser, "Date-DataSet-Dropdown-Contains-Unrelated-Group", getClass());
        assertEquals(dropdown.getDateDimensionGroup("UNRELATED").getDateDimensions(),
                singletonList(DATE_DATASET_TIMELINE));
        assertEquals(dropdown.getDateDimensionGroup("OTHER").getDateDimensions(), singletonList(DATE_DATASET_CREATED));
        assertEquals(dropdown.getDateDimensionGroup("RECOMMENDED").getDateDimensions(),
                singletonList(DATE_DATASET_ACTIVITY));
    }

    @Test(dependsOnMethods = {"testPreviouslySelectedDateDataSet"})
    public void editDateDimensionOnDashboardToRemoveWarning() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                INSIGHT_USING_DATE_FILTER);
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not appear");

        indigoDashboardsPage.getConfigurationPanel().openDateDataSet().selectByName(DATE_DATASET_ACTIVITY);
        assertFalse(
                indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not disappear");

        indigoDashboardsPage.selectDateFilterByName("All time").waitForWidgetsLoading()
                .selectWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER);
        assertEquals(indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_USING_DATE_FILTER)
                .getChartReport().getDataLabels(), singletonList("154,271"), "Chart does not render correctly");
    }

    @Test(dependsOnMethods = {"editDateDimensionOnDashboardToRemoveWarning"})
    public void editDateDimensiontOnADToRemoveWarning() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                INSIGHT_USING_DATE_FILTER);
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not appear");

        AnalysisPage page = initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER);
        page.getFilterBuckets().changeDateDimension(DATE_DATASET_CREATED);
        page.waitForReportComputing().saveInsight();

        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                INSIGHT_USING_DATE_FILTER);
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not disappear");
    }
}
