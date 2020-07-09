package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;

import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP1;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP2;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.CANVAS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.COLORS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.LEGEND;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.X_AXIS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ColorPalettePickerUILayoutInsightAndKPITest extends AbstractAnalyseTest {
    private final String TEST_MULTI_MEASURE_ON_INSIGHT = "Test Multi Measure On Insight" + generateHashString();
    private final String INSIGHT_FILTER_DATE_BY_SPPY = "Insight Filter Date By SPPY" + generateHashString();
    private final String INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY = "Insight has attribute on view by" + generateHashString();
    private final String INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY = "Insight has attribute on stack by" + generateHashString();
    private ProjectRestRequest projectRestRequest;
    private List<String> listItemsConfigurationPanel =
            asList(COLORS.toString(), X_AXIS.toString(), Y_AXIS.toString(), LEGEND.toString(), CANVAS.toString());

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "TEST UI LAYOUT ON INSIGHT";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        metrics.createSnapshotBOPMetric();
        metrics.createSnapshotEOP1Metric();
        metrics.createSnapshotEOP2Metric();
        metrics.createSnapshotEOPMetric();
        metrics.createTimelineBOPMetric();
        metrics.createTimelineEOPMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    protected void prepareInsightsApplyColorsPalette() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter().waitForReportComputing().saveInsight(INSIGHT_FILTER_DATE_BY_SPPY);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_SNAPSHOT_EOP1)
                .addMetric(METRIC_SNAPSHOT_EOP2).addMetric(METRIC_SNAPSHOT_EOP)
                .addMetric(METRIC_TIMELINE_BOP).addMetric(METRIC_TIMELINE_EOP)
                .waitForReportComputing().saveInsight(TEST_MULTI_MEASURE_ON_INSIGHT);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter().waitForReportComputing().saveInsight(INSIGHT_FILTER_DATE_BY_SPPY);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY);

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addStack(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY);
    }


    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorItemAtFirstPositionOnConfigurationPanel() {
        assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().getItemNames(), listItemsConfigurationPanel);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testSearchTextBoxVisibleWhenThereAreManyMeasureOnInsight() {
        assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT)
                .waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().isSearchTextBoxOnColourConfigurationVisible(),
        "The Search TextBox should be shown on Colour Configuration");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorSnippetListIsListedMeasuresElementsList() {
        assertTrue(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY)
                .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                .isColorSnippetListVisible(), "Color Snippet list should be shown");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorSnippetListIsListedAttributeElementsList() {
        assertTrue(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY)
                .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                .isColorSnippetListVisible(), "Color Snippet list should be shown");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testDerivedMeasuresInheritColorFromItsParent() {
        initAnalysePage().openInsight(INSIGHT_FILTER_DATE_BY_SPPY).getFilterBuckets().openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);
        analysisPage.waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                .selectColor(ColorPalette.PURPLE.toReportFormatString());
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.LIGHT_GRAYISH_PURPLE.toString());
        assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.PURPLE.toString());
        assertEquals(chartReport.getLegendColors(),asList(
                ColorPalette.LIGHT_GRAYISH_PURPLE.toString(), ColorPalette.PURPLE.toString()));
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorSnippetOnColorMeasureConfigurationCorrespondingToColorOnInsight() {
        assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY)
                .waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().getColor(ColorPalette.CYAN.toCssFormatString()),
                analysisPage.getChartReport().checkColorColumn(0, 0));
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorSnippetOnColorAttributeConfigurationCorrespondingToColorOnInsight() {
        assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration().getColor(ColorPalette.CYAN.toCssFormatString()),
                analysisPage.getChartReport().checkColorColumn(0, 0));
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testScrollBarVisibleWhenThereAreManyMeasureOnInsight() {
        assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT)
                .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                .isScrollBarOnColourConfigurationVisible(),"The ScrollBar should be visible");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testAllColorsIsListedInPaletteColorPicker() {
        assertTrue(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY).waitForReportComputing()
                .openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().isListColorItemsVisible(), "A color should be visible");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testBoxShadowIsShowWhenColorSnippetSelected() {
        assertTrue(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY).waitForReportComputing()
                .openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().isBoxShadowVisible(),"A Box-Shadow should be visible");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testPaletteColorPickerIsClosedAfterSnippetColorSelected() {
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY).waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPalette.PURPLE.toReportFormatString());
        assertFalse(analysisPage.isDialogDropdownBoxVisible(),"A palette color picker should be closed");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testNewColorKeptAndMapToInsightChartAfterAppliedColorSnippetInColorPalettePickerDialog() {
        initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPalette.MODERATE_RED.toReportFormatString());
        assertTrue(analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.MODERATE_RED.toCssFormatString())
                .getColorsPaletteDialog().isActiveColor(ColorPalette.MODERATE_RED.toReportFormatString()),
                "A new color should be kept on color snippet");
        assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.MODERATE_RED.toString());
        analysisPage.saveInsightAs("Test multi measure on insight save as");
    }

        @Test(dependsOnMethods = {"testNewColorKeptAndMapToInsightChartAfterAppliedColorSnippetInColorPalettePickerDialog"})
        public void testNewColorKeptAfterReopenColorPalettePickerDialog() {
            assertEquals(initAnalysePage().openInsight("Test multi measure on insight save as").waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.PURPLE.toCssFormatString())
                    .getColorsPaletteDialog().getColor(), ColorPalette.PURPLE.toString());
        }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testResetButtonIsDisabledWhenSnippetColorNothingChanged() {
        initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString());
        assertFalse(analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .isResetButtonVisibled(),"A Reset Button should be disabled When snippet color nothing changed");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testResetButtonVisibleWhenSnippetColorChanged() {
        initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing().openConfigurationPanelBucket()
                .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPalette.PURPLE.toReportFormatString());
        assertTrue(analysisPage.openConfigurationPanelBucket()
                .openColorConfiguration().isResetButtonVisibled(),"A Reset Button should be visible");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testColorOnConfigurationPanel() {
        assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().getColor(), ColorPalette.CYAN.toString());
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testSearchNoDataInColorConfig() {
        assertEquals(initAnalysePage()
                .openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration()
                .searchItem("123456").getNoDataOnResultSearchText(), "No matching data");
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testResetButtonLabel() {
        setCustomColorPickerFlag(true);
        try {
            initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().selectColor(ColorPalette.PURPLE.toReportFormatString());
            assertEquals(analysisPage.openConfigurationPanelBucket().openColorConfiguration().getResetButtonText(), "Reset Colors");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testCustomColorButtonLabel() {
        setCustomColorPickerFlag(true);
        try {
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().getCustomColorButtonText(), "Custom color");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testSearchHasDataInColorConfig() {
        assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                .openConfigurationPanelBucket().openColorConfiguration().searchItem("# of Activities")
                .getResultSearchText(), "# of Activities");
    }

    private void setCustomColorPickerFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }
}
