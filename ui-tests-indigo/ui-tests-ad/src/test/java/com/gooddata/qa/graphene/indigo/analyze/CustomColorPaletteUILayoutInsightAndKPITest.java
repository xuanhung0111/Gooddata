package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP1;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP2;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CustomColorPaletteUILayoutInsightAndKPITest extends AbstractAnalyseTest {
    private final String TEST_MULTI_MEASURE_ON_INSIGHT = "Test Multi Measure On Insight" + generateHashString();
    private final String INSIGHT_FILTER_DATE_BY_SPPY = "Insight Filter Date By SPPY" + generateHashString();
    private final String INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY = "Insight has attribute on view by" + generateHashString();
    private final String INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY = "Insight has attribute on stack by" + generateHashString();
    private ProjectRestRequest projectRestRequest;

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
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter().waitForReportComputing().saveInsight(INSIGHT_FILTER_DATE_BY_SPPY);

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_OPP_FIRST_SNAPSHOT)
                .addMetric(METRIC_SNAPSHOT_BOP).addMetric(METRIC_SNAPSHOT_EOP1)
                .addMetric(METRIC_SNAPSHOT_EOP2).addMetric(METRIC_SNAPSHOT_EOP)
                .addMetric(METRIC_TIMELINE_BOP).addMetric(METRIC_TIMELINE_EOP)
                .waitForReportComputing().saveInsight(TEST_MULTI_MEASURE_ON_INSIGHT);

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter().waitForReportComputing().saveInsight(INSIGHT_FILTER_DATE_BY_SPPY);

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY);

        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addStack(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testRGBPickerIsShowed() {
        try {
            setCustomColorPickerFlag(true);
            assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().isRGBPickerVisible(), "A RGB Picker should be visible ");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testCurrentColorIsMappedToCurrentOption() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY)
                    .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor())
                    .getCurrentOption(), ColorPalette.CYAN_DEVIANT.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testNewColorIsMappedToNewOptionWhenAppliedHexColor() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY)
                    .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor())
                    .getNewOption(), ColorPalette.YELLOW.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testTextBoxInputHexColorIsShowed() {
        try {
            setCustomColorPickerFlag(true);
            assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().isInputHexColorVisible(), "A Text Box input Hex color should be visible");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testCancelButtonIsShowed() {
        try {
            setCustomColorPickerFlag(true);
            assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().isCancelButtonVisible(), "A Cancel button should be visible");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testOkButtonIsShowed() {
        try {
            setCustomColorPickerFlag(true);
            assertTrue(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().isOkButtonVisible(), "An Ok button should be visible");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testActionWithOkButton() {
        try {
            setCustomColorPickerFlag(true);
            initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT)
                    .waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertFalse(analysisPage.isCustomColorPaletteDialogVisible(), "A Dialog Dropdown Color Palette Box should be closed ");
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testHexColorTextFieldInputData() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor())
                    .getNewOption(), ColorPalette.YELLOW.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testActionWithCancelButton() {
        try {
            setCustomColorPickerFlag(true);
            initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT)
                    .waitForReportComputing().openConfigurationPanelBucket()
                    .openColorConfiguration().openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).cancel();
            assertFalse(analysisPage.isCustomColorPaletteDialogVisible(), "A Dialog Dropdown Color Palette Box should be closed ");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testHexColorTextBoxLabel() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT)
                    .waitForReportComputing().openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().getHexText(), "HEX");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testCurrentLabel() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().getCurrentText(), "CURRENT");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testNewLabel() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().getNewText(), "NEW");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testOkLabel() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().getCancelButtonText(), "Cancel");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testCancelLabel() {
        try {
            setCustomColorPickerFlag(true);
            assertEquals(initAnalysePage().openInsight(TEST_MULTI_MEASURE_ON_INSIGHT).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().getOkButtonText(), "OK");
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testChangeColorPalettePickerToCustomPalettePickerAndVerseViceByMeasure() {
        try {
            setCustomColorPickerFlag(true);
            initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_VIEW_BY).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString()).getColorsPaletteDialog()
                    .openCustomColorPalette().getCustomColorsPaletteDialog()
                    .setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.YELLOW.toCssFormatString())
                    .getColorsPaletteDialog().selectColor(ColorPalette.PURPLE.toReportFormatString());
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.PURPLE.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            assertEquals(analysisPage.getChartReport().checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyColorsPalette"})
    public void testChangeColorPalettePickerToCustomPalettePickerAndVerseViceByAttribute() {
        try {
            setCustomColorPickerFlag(true);
            initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_ON_STACK_BY).waitForReportComputing()
                    .openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.CYAN.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            ChartReport chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.YELLOW.toCssFormatString())
                    .getColorsPaletteDialog().selectColor(ColorPalette.PURPLE.toReportFormatString());
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.PURPLE.toString());
            analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                    .openColorsPaletteDialog(ColorPalette.PURPLE.toCssFormatString())
                    .getColorsPaletteDialog().openCustomColorPalette()
                    .getCustomColorsPaletteDialog().setColorCustomPicker(ColorPalette.YELLOW.getHexColor()).apply();
            chartReport = analysisPage.getChartReport();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.YELLOW.toString());
        } finally {
            setCustomColorPickerFlag(false);
        }
    }

    private void setCustomColorPickerFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_CUSTOM_COLOR_PICKER, status);
    }
}
