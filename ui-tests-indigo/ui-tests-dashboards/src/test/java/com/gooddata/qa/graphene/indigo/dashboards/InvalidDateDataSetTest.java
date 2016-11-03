package com.gooddata.qa.graphene.indigo.dashboards;

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
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class InvalidDateDataSetTest extends GoodSalesAbstractDashboardTest {

    private static final String INSIGHT_USING_DATE_FILTER = "Insight-Using-Date-Filter";
    private static final String DATE_DIMENSION_ERROR_MESSAGE =
            " can no longer be applied to the insight. Select a different dimension or edit the insight.";

    @Test(dependsOnGroups = {"dashboardsInit"})
    public void testPreviouslySelectedDateDataSet() {
        AnalysisPage page =
                initAnalysePage().addMetric(METRIC_OPP_FIRST_SNAPSHOT).addDateFilter().waitForReportComputing();
        page.getFilterBuckets().changeDateDimension(DATE_TIMELINE);
        page.waitForReportComputing().saveInsight(INSIGHT_USING_DATE_FILTER);

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_USING_DATE_FILTER);

        assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_TIMELINE,
                DATE_TIMELINE + " is not selected");

        indigoDashboardsPage.saveEditModeWithWidgets();

        initAnalysePage().openInsight(INSIGHT_USING_DATE_FILTER).waitForReportComputing()
                .replaceMetric(METRIC_OPP_FIRST_SNAPSHOT, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing()
                .saveInsight();

        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        takeScreenshot(browser, "Unrelated-Date-DataSet-Error-Message", getClass());
        assertEquals(panel.getSelectedDataSetColor(), "rgba(229, 77, 66, 1)", "Selected data set color is not red");
        assertEquals(panel.getDateDataSetError(), "\"" + DATE_TIMELINE + "\"" + DATE_DIMENSION_ERROR_MESSAGE,
                "Error msg is not correct");

        DateDimensionSelect dropdown = panel.openDateDataSet();
        takeScreenshot(browser, "Date-DataSet-Dropdown-Contains-Unrelated-Group", getClass());
        assertEquals(dropdown.getDateDimensionGroup("UNRELATED").getDateDimensions(),
                singletonList(DATE_TIMELINE));
        assertEquals(dropdown.getDateDimensionGroup("OTHER").getDateDimensions(), singletonList(DATE_CREATED));
        assertEquals(dropdown.getDateDimensionGroup("RECOMMENDED").getDateDimensions(),
                singletonList(DATE_ACTIVITY));
    }

    @Test(dependsOnMethods = {"testPreviouslySelectedDateDataSet"})
    public void editDateDimensionOnDashboardToRemoveWarning() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                INSIGHT_USING_DATE_FILTER);
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not appear");

        indigoDashboardsPage.getConfigurationPanel().openDateDataSet().selectByName(DATE_ACTIVITY);
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
        page.getFilterBuckets().changeDateDimension(DATE_CREATED);
        page.waitForReportComputing().saveInsight();

        initIndigoDashboardsPageWithWidgets().switchToEditMode().selectWidgetByHeadline(Insight.class,
                INSIGHT_USING_DATE_FILTER);
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetErrorPresent(),
                "Error does not disappear");
    }
}
