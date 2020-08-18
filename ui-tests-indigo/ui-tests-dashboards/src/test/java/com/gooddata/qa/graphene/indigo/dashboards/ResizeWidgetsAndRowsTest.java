package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class ResizeWidgetsAndRowsTest extends AbstractDashboardTest {

    private final String INSIGHT_COLUMN_HAS_SOME_METRICS = "Column Metrics";
    private final String INSIGHT_TABLE_HAS_SOME_METRICS = "Table Metrics";
    private final String INSIGHT_TABLE_DELETED = "Delete Insight" + generateHashString();
    private final String INSIGHT_TEST_EXPORTED = "Exported Insight" + generateHashString();

    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private KpiConfiguration kpi;
    private String sourceProjectId;
    private String targetProjectId;
    private String insightJsonObject;

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        kpi = new KpiConfiguration.Builder()
            .metric(METRIC_AMOUNT)
            .dataSet(DATE_DATASET_CLOSED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
            .build();

        createInsightHasOnlyMetric(INSIGHT_COLUMN_HAS_SOME_METRICS, ReportType.COLUMN_CHART,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        createInsightHasOnlyMetric(INSIGHT_TABLE_HAS_SOME_METRICS, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        insightJsonObject = createInsightHasOnlyMetric(INSIGHT_TEST_EXPORTED, ReportType.COLUMN_CHART,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addResizeToWidget() {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_COLUMN_HAS_SOME_METRICS).waitForWidgetsLoading()
            .hoverOnResizerWidget();
        assertTrue(indigoDashboardsPage.isWidthResizerColorVisible(), "Should be have a blue resizer");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void resizeWidget() {
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_COLUMN_HAS_SOME_METRICS).waitForWidgetsLoading()
            .selectWidgetByHeadline(Insight.class, INSIGHT_COLUMN_HAS_SOME_METRICS);

        indigoDashboardsPage.resizeWidthOfWidget(ResizeBullet.TEN).saveEditModeWithWidgets();
        assertTrue(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_COLUMN_HAS_SOME_METRICS)
            .getAttribute("class").contains("s-fluid-layout-column-width-" + ResizeBullet.TEN.getNumber()),
            "The width widget should be resized");

        indigoDashboardsPage.switchToEditMode().resizeWidthOfWidget(ResizeBullet.SEVEN).saveEditModeWithWidgets();
        assertTrue(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_COLUMN_HAS_SOME_METRICS)
                .getAttribute("class").contains("s-fluid-layout-column-width-" + ResizeBullet.SEVEN.getNumber()),
            "The width widget should be resized");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void resizeLimitMinAndMaxWidget() {
        //scenario 3 and 5
        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_COLUMN_HAS_SOME_METRICS).waitForWidgetsLoading();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_COLUMN_HAS_SOME_METRICS).getAttribute("class"),
            containsString("s-fluid-layout-column-width-" + ResizeBullet.SIX.getNumber()));

        indigoDashboardsPage.resizeMinimumWidget();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_COLUMN_HAS_SOME_METRICS).getAttribute("class"),
                containsString("s-fluid-layout-column-width-" + ResizeBullet.FOUR.getNumber()));

        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_TABLE_HAS_SOME_METRICS).waitForWidgetsLoading();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_TABLE_HAS_SOME_METRICS).getAttribute("class"),
            containsString("s-fluid-layout-column-width-" + ResizeBullet.TWELVE.getNumber()));

        indigoDashboardsPage.resizeMinimumWidget();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(INSIGHT_TABLE_HAS_SOME_METRICS).getAttribute("class"),
                containsString("s-fluid-layout-column-width-" + ResizeBullet.THREE.getNumber()));

        initIndigoDashboardsPage().addDashboard().addKpi(kpi).waitForWidgetsLoading().clickDashboardBody();

        assertThat(indigoDashboardsPage.getWidgetFluidLayout(METRIC_AMOUNT).getAttribute("class"),
            containsString("s-fluid-layout-column-width-" + ResizeBullet.TWO.getNumber()));
        indigoDashboardsPage.resizeMinimumWidget();

        assertThat(indigoDashboardsPage.getWidgetFluidLayout(METRIC_AMOUNT).getAttribute("class"),
                containsString("s-fluid-layout-column-width-" + ResizeBullet.TWO.getNumber()));

        indigoDashboardsPage.resizeMaximumWidget();
        assertThat(indigoDashboardsPage.getWidgetFluidLayout(METRIC_AMOUNT).getAttribute("class"),
            containsString("s-fluid-layout-column-width-" + ResizeBullet.TWELVE.getNumber()));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void showBulletsAndWidthResizerWhenMoving() {
        initIndigoDashboardsPage().addDashboard()
            .addInsight(INSIGHT_COLUMN_HAS_SOME_METRICS).addInsightNext(INSIGHT_TABLE_HAS_SOME_METRICS)
            .waitForWidgetsLoading();

        indigoDashboardsPage.dragToResizeMinimumWidget();

        expectResizeBulletsPresent();

        indigoDashboardsPage.dragToResizeMaximumWidget();

        assertEquals(indigoDashboardsPage.getSizeWidthResizer(), 2);
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void notShowErrorMessageAfterDeletingDataset() {
        String widgetUri = createInsightHasOnlyMetric(INSIGHT_TABLE_DELETED, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_TABLE_DELETED).selectDateFilterByName("All time")
            .waitForWidgetsLoading();
        indigoRestRequest.deleteObjectsUsingCascade(widgetUri);
        indigoDashboardsPage.selectDateFilterByName("This month").selectDateFilterByName("All time")
            .waitForWidgetsLoading();

        assertThat(indigoDashboardsPage.getDashboardBodyText(),
            containsString("SORRY, WE CAN'T DISPLAY THIS INSIGHT"));
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList())));
    }

    private void expectResizeBulletsPresent() {
        IntStream.rangeClosed(0, 12).forEach(i -> assertTrue(isElementVisible(cssSelector(
            String.format(".s-resize-bullet-%d", i)), browser)));
    }
}
