package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WIN_RATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KPIDashboardCRUDAndMovingTest extends AbstractDashboardTest {

    private final String INSIGHT_HAS_SOME_METRICS = "Some Metrics";
    private final String DASHBOARD_MANY_KPIS = "Many KPIs";
    private final String DASHBOARD_HAS_KPI_AND_INSIGHT = "Kpi and Insight";
    private final String DASHBOARD_ON_TOP_WITH_NO_SCROLLBAR = "On Top No Scroll Bar";
    private final String DASHBOARD_ON_BOTTOM_WITH_NO_SCROLLBAR = "On Bottom No Scroll Bar";
    private final String DASHBOARD_BETWEEN_WITH_NO_SCROLLBAR = "Between No Scroll Bar";
    private final String DASHBOARD_TEST_REMOVE_WIDGET = "Remove Widget";
    private final String DASHBOARD_HAS_NO_WIDGET = "No Widget";
    private final String DASHBOARD_TEST_FIRST_POSITION = "First Position";
    private final String DASHBOARD_TEST_LAST_POSITION = "Last Position";
    private final String firstInsight = "First Insight";
    private final String secondaryInsight = "Secondary Insight";
    private final String thirdInsight = "Third Insight";

    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private KpiConfiguration kpi;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();
        getMetricCreator().createWonMetric();
        getMetricCreator().createBestCaseMetric();
        getMetricCreator().createWinRateMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_LAYOUTS_DASHBOARD, true);
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        kpi = new KpiConfiguration.Builder()
            .metric(METRIC_AMOUNT)
            .dataSet(DATE_DATASET_CLOSED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
            .build();

        createInsightHasOnlyMetric(INSIGHT_HAS_SOME_METRICS, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        createInsightHasOnlyMetric(firstInsight, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        createInsightHasOnlyMetric(secondaryInsight, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));

        createInsightHasOnlyMetric(thirdInsight, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addOneWidgetToEmptyDashboard() {
        initIndigoDashboardsPage().addDashboard().dragInsightToDashboard(INSIGHT_HAS_SOME_METRICS);
        assertTrue(indigoDashboardsPage.getMinHeightLayoutPlaceHolder().contains("min-height: 450px"));
        assertTrue(indigoDashboardsPage.getAttributeClassFluidLayout().contains("s-fluid-layout-column-width-12"));
        assertEquals(indigoDashboardsPage.getDashboardBodyText(), "Drop insight");
        new Actions(browser).release().perform();

        initIndigoDashboardsPage().addDashboard().dragAddKpiToDashboard();
        assertTrue(indigoDashboardsPage.getMinHeightLayoutPlaceHolder().contains("min-height: 300px"));
        assertTrue(indigoDashboardsPage.getAttributeClassFluidLayout().contains("s-fluid-layout-column-width-2"));
        assertEquals(indigoDashboardsPage.getDashboardBodyText(), "Drop insight");
        new Actions(browser).release().perform();
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addManyWidgetsIntoSameRow() throws IOException {
        initIndigoDashboardsPage().addDashboard();
        IntStream.range(0, 6).forEach(e -> indigoDashboardsPage.addKpiToBeginningOfRow(kpi));
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_MANY_KPIS).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_MANY_KPIS);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            asList(asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"),
                Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"),
                Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"))));

        indigoDashboardsPage.switchToEditMode().addKpiToBeginningOfRow(kpi).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT)));

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri), asList(asList(
            Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"),
            Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"),
            Pair.of(METRIC_AMOUNT, "2"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addAKpiAndAWidgetIntoSameRow() throws IOException {
        initIndigoDashboardsPage().addDashboard().addKpiToBeginningOfRow(kpi)
            .addInsightToCreateSameRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.DropZone.NEXT)
            .changeDashboardTitle(DASHBOARD_HAS_KPI_AND_INSIGHT).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, INSIGHT_HAS_SOME_METRICS)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_HAS_KPI_AND_INSIGHT);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            singletonList(asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void doSomeActionsWithDragAndDropWidget() {
        initIndigoDashboardsPage().addDashboard().tryToDragAndDropKPI(indigoDashboardsPage.getSaveButton());
        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), singletonList(EMPTY_LIST));

        indigoDashboardsPage.addInsight(INSIGHT_HAS_SOME_METRICS).saveEditModeWithWidgets().addDashboard()
            .addInsight(INSIGHT_HAS_SOME_METRICS).cancelEditModeWithChanges().switchToEditMode();
        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(),
            singletonList(singletonList(INSIGHT_HAS_SOME_METRICS)));

        initIndigoDashboardsPage().addDashboard().addKpiToBeginningOfRow(kpi).getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).delete();
        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), singletonList(EMPTY_LIST));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void removeSingleWidgetInARow() throws IOException {
        initIndigoDashboardsPage().addDashboard();
        asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_WON, METRIC_BEST_CASE, METRIC_WIN_RATE, METRIC_AMOUNT_BOP)
            .forEach(metric -> indigoDashboardsPage.addKpiToBeginningOfRow(new KpiConfiguration.Builder()
                .metric(metric)
                .dataSet(DATE_DATASET_CLOSED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .build()));

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), singletonList(asList(
            METRIC_AMOUNT_BOP, METRIC_WIN_RATE, METRIC_BEST_CASE, METRIC_WON, METRIC_AVG_AMOUNT, METRIC_AMOUNT)));

        indigoDashboardsPage.selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT).delete();
        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), singletonList(asList(
            METRIC_AMOUNT_BOP, METRIC_WIN_RATE, METRIC_BEST_CASE, METRIC_WON, METRIC_AVG_AMOUNT)));

        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TEST_REMOVE_WIDGET).saveEditModeWithWidgets();
        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_TEST_REMOVE_WIDGET);

        assertThat(getListWidthAndUriMetadata(indigoDashboardUri).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), not(hasItem(Pair.of(METRIC_AMOUNT, 2))));

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT_BOP).delete();
        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), singletonList(asList(
            METRIC_WIN_RATE, METRIC_BEST_CASE, METRIC_WON, METRIC_AVG_AMOUNT)));

        indigoDashboardsPage.saveEditModeWithWidgets();
        assertThat(getListWidthAndUriMetadata(indigoDashboardUri).stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), not(hasItem(Pair.of(METRIC_AMOUNT_BOP, 2))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void removeLastWidgetInRow() throws IOException {
        initIndigoDashboardsPage().addDashboard().addKpiToBeginningOfRow(kpi)
            .addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .saveEditModeWithWidgets().switchToEditMode().selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT).delete();

        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_HAS_NO_WIDGET).saveEditModeWithWidgets();
        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_HAS_NO_WIDGET);
        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            singletonList(singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addWidgetNewRow_OnTopDashboard_WithNoScrollBar() throws IOException {
        initIndigoDashboardsPage().addDashboard();
        IntStream.range(0, 3).forEach(e -> indigoDashboardsPage.addKpiToBeginningOfRow(kpi));

        indigoDashboardsPage.addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.TOP)
            .changeDashboardTitle(DASHBOARD_ON_TOP_WITH_NO_SCROLLBAR).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            singletonList(INSIGHT_HAS_SOME_METRICS), asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_ON_TOP_WITH_NO_SCROLLBAR);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri), asList(
            singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12")),
            asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addWidgetNewRow_OnBottomDashboard_WithNoScrollBar() throws IOException {
        initIndigoDashboardsPage().addDashboard();
        IntStream.range(0, 3).forEach(e -> indigoDashboardsPage.addKpiToBeginningOfRow(kpi));

        indigoDashboardsPage.addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .changeDashboardTitle(DASHBOARD_ON_BOTTOM_WITH_NO_SCROLLBAR).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT), singletonList(INSIGHT_HAS_SOME_METRICS)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_ON_BOTTOM_WITH_NO_SCROLLBAR);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri), asList(
            asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2")),
            singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addWidgetNewRow_BetweenExistingRows_WithNoScrollBar() throws IOException {
        initIndigoDashboardsPage().addDashboard();
        IntStream.range(0, 3).forEach(e -> indigoDashboardsPage.addKpiToBeginningOfRow(kpi));

        indigoDashboardsPage.addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .addInsightToCreateANewRow(firstInsight, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .changeDashboardTitle(DASHBOARD_BETWEEN_WITH_NO_SCROLLBAR).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT), singletonList(firstInsight),
            singletonList(INSIGHT_HAS_SOME_METRICS)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_BETWEEN_WITH_NO_SCROLLBAR);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri), asList(
            asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AMOUNT, "2")),
            singletonList(Pair.of(firstInsight, "12")),
            singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addWidgetIntoSameRowAtFirstPosition() throws IOException {
        initIndigoDashboardsPage().addDashboard().addKpiToBeginningOfRow(kpi)
            .addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .addInsightToCreateSameRow(firstInsight, METRIC_AMOUNT, Widget.DropZone.PREV)
            .changeDashboardTitle(DASHBOARD_TEST_FIRST_POSITION).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(firstInsight, METRIC_AMOUNT), singletonList(INSIGHT_HAS_SOME_METRICS)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_TEST_FIRST_POSITION);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            asList(asList(Pair.of(firstInsight, "12"), Pair.of(METRIC_AMOUNT, "2")),
                singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addWidgetIntoSameRowAtLastPosition() throws IOException {
        initIndigoDashboardsPage().addDashboard().addKpiToBeginningOfRow(kpi)
            .addInsightToCreateANewRow(INSIGHT_HAS_SOME_METRICS, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
            .addInsightToCreateSameRow(firstInsight, METRIC_AMOUNT, Widget.DropZone.NEXT)
            .changeDashboardTitle(DASHBOARD_TEST_LAST_POSITION).saveEditModeWithWidgets();

        assertEquals(indigoDashboardsPage.getHeaderWidgetFluidLayout(), asList(
            asList(METRIC_AMOUNT, firstInsight), singletonList(INSIGHT_HAS_SOME_METRICS)));

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(DASHBOARD_TEST_LAST_POSITION);

        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            asList(asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(firstInsight, "12")),
                singletonList(Pair.of(INSIGHT_HAS_SOME_METRICS, "12"))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void addNewWidgetAfterExistingWidgetInSameRow() throws IOException {
        String dashboard = "Dashboard" + generateHashString();
        KpiConfiguration kpiAmount = new KpiConfiguration.Builder()
            .metric(METRIC_AMOUNT)
            .dataSet(DATE_DATASET_CLOSED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
            .build();

        KpiConfiguration kpiAvgAmount = new KpiConfiguration.Builder()
            .metric(METRIC_AVG_AMOUNT)
            .dataSet(DATE_DATASET_CLOSED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
            .build();

        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).addKpi(kpiAvgAmount).changeDashboardTitle(dashboard)
            .saveEditModeWithWidgets();

        String indigoDashboardUri = indigoRestRequest.getAnalyticalDashboardUri(dashboard);
        assertEquals(getListWidthAndUriMetadata(indigoDashboardUri),
            asList(asList(Pair.of(METRIC_AMOUNT, "2"), Pair.of(METRIC_AVG_AMOUNT, "2"))));
    }

    private List<List<Pair<String, String>>> getListWidthAndUriMetadata(String dashboardUri)
        throws JSONException, IOException {
        List<List<Pair<String, String>>> listAllWidget = new ArrayList<>();
        JSONArray rowsArray;

        try {
            rowsArray = indigoRestRequest.getJsonObject(dashboardUri).getJSONObject("analyticalDashboard")
                .getJSONObject("content").getJSONObject("layout").getJSONObject("fluidLayout").getJSONArray("rows");
        } catch (JSONException e) {
            rowsArray = new JSONArray();
        }

        for (int i = 0; i < rowsArray.length(); i++) {
            List<Pair<String, String>> listWidgetInOneRow = new ArrayList<>();
            for (int j = 0; j < rowsArray.getJSONObject(i).getJSONArray("columns").length(); j++) {
                JSONObject columnsObject = rowsArray.getJSONObject(i).getJSONArray("columns").getJSONObject(j);
                listWidgetInOneRow.add(Pair.of(getTitleUriOfColumn(columnsObject), getWidthOfColumn(columnsObject)));
            }
            listAllWidget.add(listWidgetInOneRow);
        }
        return listAllWidget;
    }

    private String getTitleUriOfColumn(JSONObject columnObject) throws IOException {
        JSONObject uriWidget = indigoRestRequest.getJsonObject(columnObject.getJSONObject("content")
            .getJSONObject("widget").getJSONObject("qualifier").getString("uri"));

        if (uriWidget.has("kpi")) {
            return uriWidget.getJSONObject("kpi").getJSONObject("meta").getString("title");
        } else if (uriWidget.has("visualizationWidget")) {
            return uriWidget.getJSONObject("visualizationWidget").getJSONObject("meta").getString("title");
        }
        return StringUtils.EMPTY;
    }

    private String getWidthOfColumn(JSONObject columnObject) {
        return columnObject.getJSONObject("size").getJSONObject("xl").get("width").toString();
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList())));
    }
}
