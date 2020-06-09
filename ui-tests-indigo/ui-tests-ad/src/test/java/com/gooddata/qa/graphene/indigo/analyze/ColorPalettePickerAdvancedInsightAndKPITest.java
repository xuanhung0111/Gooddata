package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import com.gooddata.qa.utils.http.ColorPaletteRequestData;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static java.util.Collections.singletonList;

public class ColorPalettePickerAdvancedInsightAndKPITest extends AbstractAnalyseTest {

    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private final String DATE_FILTER_ALL_TIME = "All time";
    private final String KPI_DASHBOARD = "KPI_Dashboard" + generateHashString();
    private final String DEFAULT_INSIGHT = "Default Insight" + generateHashString();
    private final String NEW_INSIGHT = "New Insight" + generateHashString();
    private final String INSIGHT_APPLY_CUSTOM_COLOR_PICKER = "Insight Apply Custom Color Picker" + generateHashString();
    private final String INSIGHT_APPLY_COLOR_PICKER_MAPPING = "Insight Apply Color Picker Mapping" + generateHashString();
    private final String INSIGHT_TEST = "Insight Test" + generateHashString();
    private final String INSIGHT_TEST_SAVE_AS = "Insight Test Save As" + generateHashString();
    private final String IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT = "Import Export Partial Project With Insight" + generateHashString();
    private final String IMPORT_EXPORT_PROJECT_WITH_INSIGHT = "Import Export Project With Insight" + generateHashString();
    private final String INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES = "Insight Switch between insight types" + generateHashString();
    private static List<Pair<String, ColorPaletteRequestData.ColorPalette>> listColorPalettes = asList(
            Pair.of("guid1", ColorPaletteRequestData.ColorPalette.RED),
            Pair.of("guid2", ColorPaletteRequestData.ColorPalette.GREEN),
            Pair.of("guid3", ColorPaletteRequestData.ColorPalette.BLUE),
            Pair.of("guid4", ColorPaletteRequestData.ColorPalette.YELLOW));

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Color-Palette-Picker-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        metrics.createSnapshotBOPMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        setCustomColorPickerFlag(false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchBetweenColumnChartAndPieChartOnInsight() {
        setCustomColorPickerFlag(true);
        try {
            createInsightHasAttributeOnViewBy(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES,
                    METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ReportType.COLUMN_CHART);
            initAnalysePage().openInsight(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .selectColor(ColorPalette.PURPLE.toReportFormatString());
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 2), ColorPalette.PURPLE.toString());
            analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 2), ColorPalette.PURPLE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"testSwitchBetweenColumnChartAndPieChartOnInsight"})
    public void testSwitchBetweenTreeMapChartAndHeadMapChartOnInsight() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(INSIGHT_SWITCH_BETWEEN_INSIGHT_TYPES).waitForReportComputing()
                    .changeReportType(ReportType.TREE_MAP).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.DARK_CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .selectColor(ColorPalette.PURE_ORANGE.toReportFormatString());
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 1), ColorPalette.PURE_ORANGE.toString());
            analysisPage.changeReportType(ReportType.TREE_MAP).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(analysisPage.getChartReport().getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString(),
                    ColorPalette.BRIGHT_RED.toString(), ColorPalette.PURE_ORANGE.toString()));
            analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 1), ColorPalette.PURE_ORANGE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    /*@Test(dependsOnGroups = {"createProject"})
    public void testInsightApplyPaletteColorPickerOnAttribute() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURE_ORANGE.toReportFormatString());
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURE_ORANGE.toString());
        analysisPage.getStackConfigurationPanelBucket();
        analysisPage.addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing().removeStack().waitForReportComputing();
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURE_ORANGE.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInsightApplyPaletteColorPickerOnMeasure() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        analysisPage.getStackConfigurationPanelBucket();
        analysisPage.removeStack().waitForReportComputing();
        analysisPage.addStack(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSwitchBetweenXAxisMetricAndYAxisMetricOnInsight() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
            analysisPage.changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().getColor(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.getMeasureConfigurationPanelBucket();
            analysisPage.reorderSecondaryMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT)
                    .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .selectColor(ColorPalette.PURE_ORANGE.toReportFormatString());
            assertEquals(analysisPage.getChartReport().getColor(0, 0), ColorPalette.PURE_ORANGE.toString());
            analysisPage.getMeasureConfigurationPanelBucket();
            analysisPage.reorderSecondaryMetric(METRIC_OPP_FIRST_SNAPSHOT, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().getColor(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.reorderSecondaryMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
            assertEquals(analysisPage.getChartReport().getColor(0, 0), ColorPalette.PURE_ORANGE.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOff() {
        setCustomColorPickerFlag(false);
        try {
            createInsightHasAttributeOnStackByAndViewBy(DEFAULT_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            ChartReport chartReport = initAnalysePage().openInsight(DEFAULT_INSIGHT)
                    .waitForReportComputing().getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            createInsightHasAttributeOnStackByAndViewBy(INSIGHT_APPLY_COLOR_PICKER_MAPPING, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            initAnalysePage().openInsight(INSIGHT_APPLY_COLOR_PICKER_MAPPING).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .selectColor(ColorPalette.PURPLE.toReportFormatString());
            analysisPage.saveInsight();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
            setCustomColorPickerFlag(true);
            createInsightHasAttributeOnStackByAndViewBy(INSIGHT_APPLY_CUSTOM_COLOR_PICKER, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            initAnalysePage().openInsight(INSIGHT_APPLY_CUSTOM_COLOR_PICKER).waitForReportComputing();
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            analysisPage.saveInsight();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOff"})
    public void testDisableCustomColorPickerFlagWithPaletteColorOnGreyPageTurnOn() {
        try {
            createInsightHasAttributeOnStackByAndViewBy(NEW_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                    ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
            applyColorPaletteOnGrayPage();
            browser.navigate().refresh();
            initAnalysePage().openInsight(NEW_INSIGHT).waitForReportComputing();
            ChartReport chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
            initAnalysePage().openInsight(INSIGHT_APPLY_COLOR_PICKER_MAPPING).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.RED.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.RED.toString(), ColorPalette.GREEN.toString()));
            initAnalysePage().openInsight(INSIGHT_APPLY_CUSTOM_COLOR_PICKER).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.YELLOW.toString(), ColorPalette.GREEN.toString()));
        } finally {
            deleteColorPaletteOnGrayPage();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOpenAsReportButtonDisabledWhenAppliedPaletteColorOnInsight() {
        createInsightHasAttributeOnViewBy(INSIGHT_TEST, METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ReportType.COLUMN_CHART);
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        assertTrue(analysisPage.isDisableOpenAsReport(), "The insight is not compatible with custom colors");
    }

    @Test(dependsOnMethods = {"testOpenAsReportButtonDisabledWhenAppliedPaletteColorOnInsight"})
    public void testSaveAsInsightWhenAppliedPaletteColorOnInsight() {
        initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        analysisPage.saveInsightAs(INSIGHT_TEST_SAVE_AS);
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnMethods = {"testSaveAsInsightWhenAppliedPaletteColorOnInsight"})
    public void testActionsUndoAndRedoWhenAppliedPaletteColorOnInsight() {
        initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        analysisPage.undo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        analysisPage.redo();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
    }

    @Test(dependsOnMethods = {"testSaveAsInsightWhenAppliedPaletteColorOnInsight"})
    public void testActionReorderingWhenAppliedPaletteColorOnInsight() {
        initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing();
        analysisPage.addMetric(METRIC_OPP_FIRST_SNAPSHOT).waitForReportComputing();
        analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        analysisPage.getMeasureConfigurationPanelBucket();
        ChartReport chartReport = analysisPage
                .reorderMetric(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT).getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.getLegendColors(), asList(
                ColorPalette.CYAN.toString(), ColorPalette.PURPLE.toString()));
        analysisPage.getMeasureConfigurationPanelBucket();
        analysisPage.reorderMetric(METRIC_OPP_FIRST_SNAPSHOT, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(chartReport.getLegendColors(), asList(
                ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
    }

    @Test(dependsOnMethods = {"testActionReorderingWhenAppliedPaletteColorOnInsight"})
    public void testActionClearWhenAppliedPaletteColorOnInsight() {
        initAnalysePage().openInsight(INSIGHT_TEST).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        analysisPage.getPageHeader().getResetButton();
        assertTrue(analysisPage.getPageHeader().isResetButtonEnabled(),
            "Insight has been cleared ,So can not apply Color Palette");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testApplyColorPalettePickerOnKpi() {
        createInsightHasAttributeOnStackByAndViewBy(IMPORT_EXPORT_PROJECT_WITH_INSIGHT, METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(IMPORT_EXPORT_PROJECT_WITH_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        analysisPage.saveInsight();
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(IMPORT_EXPORT_PROJECT_WITH_INSIGHT).waitForWidgetsLoading()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME).clickDashboardBody()
                .changeDashboardTitle(KPI_DASHBOARD)
                .saveEditModeWithWidgets();
        assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
        assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(
                ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
    }

    @Test(dependsOnMethods = {"testApplyColorPalettePickerOnKpi"})
    public void testExportAndImportProjectWithInsight() {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
                true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets()
                    .selectKpiDashboard(KPI_DASHBOARD).waitForWidgetsLoading();
            Screenshots.takeScreenshot(browser, "export import project apply color picker in "
                    + KPI_DASHBOARD, getClass());
            assertEquals(indigoDashboardsPage.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            assertEquals(indigoDashboardsPage.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(indigoDashboardsPage.getKpiLegendColors(), asList(
                    ColorPalette.PURPLE.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testExportAndImportProjectWithInsight"})
    public void testPartialExportAndImportWithInsight() {
        String exportToken = exportPartialProject();
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            ChartReport chartReport = initAnalysePage().openInsight(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT)
                    .waitForReportComputing().getChartReport();
            takeScreenshot(browser, "testPartialExportAndImportWithInsight", getClass());
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getLegendColors(), asList(
                    ColorPalette.CYAN.toString(), ColorPalette.LIME_GREEN.toString()));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }*/

    private void createInsightHasAttributeOnViewBy(String title, String metric, String attribute, ReportType reportType) {
            indigoRestRequest.createInsight(
                    new InsightMDConfiguration(title, reportType)
                            .setMeasureBucket(
                                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                            .setCategoryBucket(asList(
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                            CategoryBucket.Type.ATTRIBUTE))));
    }

    private String createInsightHasAttributeOnStackByAndViewBy(String title, String metric,
                                                               String attribute, String stack) {
            return indigoRestRequest.createInsight(
                    new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                            .setMeasureBucket(
                                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                            .setCategoryBucket(asList(
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                            CategoryBucket.Type.ATTRIBUTE),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                            CategoryBucket.Type.STACK))));
    }

    private void setCustomColorPickerFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }

    private void applyColorPaletteOnGrayPage() {
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));
    }

    private void deleteColorPaletteOnGrayPage() {
        indigoRestRequest.deleteColorsPalette();
    }

    private String exportPartialProject() {
        String insight = createInsightHasAttributeOnStackByAndViewBy(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(IMPORT_EXPORT_PARTIAL_PROJECT_WITH_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        return exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
    }
}
