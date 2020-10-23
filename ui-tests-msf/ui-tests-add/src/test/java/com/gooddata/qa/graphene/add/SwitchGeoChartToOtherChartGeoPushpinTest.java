package com.gooddata.qa.graphene.add;

import com.gooddata.qa.graphene.AbstractGeoPushpinTest;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class SwitchGeoChartToOtherChartGeoPushpinTest extends AbstractGeoPushpinTest {

    private final String GEO_PUSHPIN_INSIGHT = "GeoChartInsight";
    private final String ATTR_POPULATION = "population";
    private final String ATTR_COUNTRY = "state";
    private final String ATTR_GEO_PUSHPIN = "city";
    private final String ATTR_DISTRICT = "district";
    private final String ATTR_TIMEZONE = "timezone";
    private static final String UNSUPPORTED_ITEM_MESSAGE = "Unsupported item is hidden";
    private static final String UNSUPPORTED_ITEMS_MESSAGE = "Unsupported items are hidden";
    private AnalysisPage analysisPage;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_SWITCH_GEO_PUSHPIN_CHART_";
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"geoPushpinProject"},
            dataProvider = "columnBarAreaLineChart")
    public void switchGeoChartToColumnBarAreLineChart(ReportType type) {
        addGeoPushpinChart();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMeasureConfigurationPanelBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_COUNTRY);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
        analysisPage.changeReportType(ReportType.GEO_CHART).waitForReportComputing();
        analysisPage.removeMeasureColorBucket("M2\n" + "Count of " + ATTR_DISTRICT);
        analysisPage.changeReportType(type).waitForReportComputing();
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        assertEquals(analysisPage.getMeasureConfigurationPanelBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION));
        analysisPage.changeReportType(ReportType.GEO_CHART).waitForReportComputing();
        analysisPage.removeMeasureSizeBucket("M1\n" + "Sum of " + ATTR_POPULATION)
                .addAttributeToMeasureColor(ATTR_DISTRICT, FieldType.ATTRIBUTE);
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMeasureConfigurationPanelBucket().getItemNames(),
                asList("Count of " + ATTR_DISTRICT));
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchGeoChartToComboChart() {
        openGeoPushpinChart();
        analysisPage.changeReportType(ReportType.COMBO_CHART);
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureAsColumnBucketBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION, "Count of " + ATTR_DISTRICT));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList());
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchGeoChartToHeadlineChart() {
        openGeoPushpinChart();
        analysisPage.changeReportType(ReportType.HEAD_LINE);
        assertEquals(analysisPage.getMeasureConfigurationPanelBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList("Count of " + ATTR_DISTRICT));
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEMS_MESSAGE);
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"},
            dataProvider = "scatterPlotBubbleChart")
    public void switchGeoChartToScatterPlotChart(ReportType type) {
        openGeoPushpinChart();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList("Sum of " + ATTR_POPULATION));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList("Count of " + ATTR_DISTRICT));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
        if (type.equals(ReportType.BUBBLE_CHART)) {
            assertEquals(analysisPage.getMetricsTertiaryBucket().getItemNames(), asList());
        }
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"},
            dataProvider = "pieDonutChart")
    public void switchGeoChartToPieDonutChart(ReportType type) {
        openGeoPushpinChart();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMeasureAsColumnBucketBucket().getItemNames(), asList("Sum of " + ATTR_POPULATION));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEMS_MESSAGE);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        analysisPage.removeGeoPushpin(ATTR_GEO_PUSHPIN).waitForReportComputing();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_COUNTRY);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
        analysisPage.changeReportType(ReportType.GEO_CHART).waitForReportComputing();
        analysisPage.removeStack();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION, "Count of " + ATTR_DISTRICT));
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"},
            dataProvider = "treeMapHeatMap")
    public void switchGeoChartToTreeMapAndHeatMapChart(ReportType type) {
        openGeoPushpinChart();
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMeasureAsColumnBucketBucket().getItemNames(), asList("Sum of " + ATTR_POPULATION));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_COUNTRY);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        analysisPage.removeMeasureSizeBucket("M1\n" + "Sum of " + ATTR_POPULATION);
        analysisPage.changeReportType(type).waitForReportComputing();
        assertEquals(analysisPage.getMeasureAsColumnBucketBucket().getItemNames(), asList("Count of " + ATTR_DISTRICT));
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchGeoChartToTableChart() {
        openGeoPushpinChart();
        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getMeasureAsColumnBucketBucket().getItemNames(),
                asList("Sum of " + ATTR_POPULATION, "Count of " + ATTR_DISTRICT));
        assertEquals(analysisPage.getAttributesBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
            assertEquals(analysisPage.getAttributesColumnsBucket().getAttributeName(), ATTR_COUNTRY);
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"},
            dataProvider = "columnBarChart")
    public void switchColumnBarChartToGeoChart(ReportType type) {
        analysisPage = initAnalysePage();
        analysisPage.changeReportType(type).waitForReportComputing();
        analysisPage.addStack(ATTR_DISTRICT).addAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_COUNTRY)
                .addMetric(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        assertEquals(analysisPage.getMainEditor().getWarningUnsupportedMessage(), UNSUPPORTED_ITEM_MESSAGE);
        analysisPage.changeReportType(type).waitForReportComputing();
        analysisPage.removeStack();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_COUNTRY);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        analysisPage.changeReportType(type).waitForReportComputing();
        analysisPage.addMetric(ATTR_DISTRICT, FieldType.ATTRIBUTE).removeAttribute(ATTR_GEO_PUSHPIN)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  "");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchAreaChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.STACKS_AREA_CHART).waitForReportComputing();
        analysisPage.addAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_COUNTRY)
                .addMetric(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_COUNTRY);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");

        analysisPage.changeReportType(ReportType.STACKS_AREA_CHART).waitForReportComputing();
        analysisPage.removeAttribute(ATTR_COUNTRY).addStack(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");

        analysisPage.changeReportType(ReportType.STACKS_AREA_CHART).waitForReportComputing();
        analysisPage.removeStack().removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_COUNTRY)
                .addMetric(ATTR_DISTRICT, FieldType.ATTRIBUTE).waitForReportComputing();

        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  "");
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchLineChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.LINE_CHART);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT).addMetric(ATTR_GEO_PUSHPIN, FieldType.ATTRIBUTE)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_GEO_PUSHPIN);
        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.removeMetric("Count of " + ATTR_GEO_PUSHPIN).addStack(ATTR_DISTRICT).addAttribute(ATTR_GEO_PUSHPIN);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        analysisPage.clear();
        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.addAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchComboChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART);
        analysisPage.addAttribute(ATTR_GEO_PUSHPIN).addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetricToSecondaryBucket(ATTR_DISTRICT, FieldType.ATTRIBUTE).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  "");
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        analysisPage.changeReportType(ReportType.COMBO_CHART);
        analysisPage.addMetric(ATTR_TIMEZONE, FieldType.ATTRIBUTE).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        analysisPage.changeReportType(ReportType.COMBO_CHART);
        analysisPage.removeMetricsSecondaryBucket("Count of " + ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_TIMEZONE);
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchHeadlineChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.HEAD_LINE);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetricToSecondaryBucket(ATTR_DISTRICT, FieldType.ATTRIBUTE).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertFalse(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be hidden");
        analysisPage.changeReportType(ReportType.HEAD_LINE);
        analysisPage.removeMetric("Sum of " + ATTR_POPULATION)
                .removeMetricsSecondaryBucket("Count of " + ATTR_DISTRICT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchScatterPlotChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.SCATTER_PLOT);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetricToSecondaryBucket(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_GEO_PUSHPIN).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.SCATTER_PLOT);
        analysisPage.removeMetric("Sum of " + ATTR_POPULATION).removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_COUNTRY);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "M1\n" + "Count of " + ATTR_DISTRICT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchBubbleChartToGeoChart() {
        initAnalysePage().changeReportType(ReportType.BUBBLE_CHART);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetricToSecondaryBucket(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addMetricToTertiaryBucket(ATTR_COUNTRY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_GEO_PUSHPIN)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n"+ "Count of " + ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.BUBBLE_CHART);
        analysisPage.removeMetric("Sum of " + ATTR_POPULATION)
                .removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_TIMEZONE);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n"+ "Count of " + ATTR_COUNTRY);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n"+ "Count of " + ATTR_DISTRICT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"},
            dataProvider = "pieDonutChart")
    public void switchPieDonutChartToGeoChart(ReportType type) {
        initAnalysePage().changeReportType(type).waitForReportComputing();
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetric(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addMetric(ATTR_COUNTRY, FieldType.ATTRIBUTE)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        analysisPage.clear();
        analysisPage.changeReportType(type).waitForReportComputing();
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT).addAttribute(ATTR_GEO_PUSHPIN);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        analysisPage.changeReportType(type).waitForReportComputing();
        analysisPage.removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchTreeMapToGeoChart() {
        initAnalysePage().changeReportType(ReportType.TREE_MAP);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetric(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addMetric(ATTR_COUNTRY, FieldType.ATTRIBUTE)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        analysisPage.clear();
        analysisPage.changeReportType(ReportType.TREE_MAP);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT).addAttribute(ATTR_GEO_PUSHPIN).addStack(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.TREE_MAP);
        analysisPage.removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchHeatmapToGeoChart() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addAttribute(ATTR_GEO_PUSHPIN)
                .addStack(ATTR_DISTRICT).waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.HEAT_MAP);
        analysisPage.removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
    }

    @Test(dependsOnMethods = {"switchGeoChartToColumnBarAreLineChart"}, groups = {"geoPushpinProject"})
    public void switchTableToGeoChart() {
        initAnalysePage().changeReportType(ReportType.TABLE);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT)
                .addMetric(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addMetric(ATTR_COUNTRY, FieldType.ATTRIBUTE)
                .waitForReportComputing();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Count of " + ATTR_DISTRICT);
        assertTrue(analysisPage.getMainEditor().isWarningUnsupportedMessageVisible(),
                "The warning unsupported message should be displayed");
        analysisPage.clear();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(ATTR_POPULATION, FieldType.FACT).addAttribute(ATTR_GEO_PUSHPIN).
                addColumnsAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_PUSHPIN);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(),  ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.removeAttribute(ATTR_GEO_PUSHPIN).addAttribute(ATTR_DISTRICT);
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
    }

    @DataProvider(name = "columnBarAreaLineChart")
    public Object[][] columnBarAreaLineChartTypeDataProvider() {
        return new Object[][]{
                {ReportType.COLUMN_CHART},
                {ReportType.BAR_CHART},
                {ReportType.STACKS_AREA_CHART},
                {ReportType.LINE_CHART}
        };
    }

//    @DataProvider(name = "columnBarAreaChart")
//    public Object[][] columnBarAreaChartTypeDataProvider() {
//        return new Object[][]{
//                {ReportType.COLUMN_CHART},
//                {ReportType.BAR_CHART},
//                {ReportType.STACKS_AREA_CHART},
//        };
//    }

    @DataProvider(name = "columnBarChart")
    public Object[][] columnBarChartTypeDataProvider() {
        return new Object[][]{
                {ReportType.COLUMN_CHART},
                {ReportType.BAR_CHART},
        };
    }

    @DataProvider(name = "scatterPlotBubbleChart")
    public Object[][] scatterPlotBubbleChartTypeDataProvider() {
        return new Object[][]{
                {ReportType.SCATTER_PLOT},
                {ReportType.BUBBLE_CHART},
        };
    }

    @DataProvider(name = "pieDonutChart")
    public Object[][] pieDonutChartTypeDataProvider() {
        return new Object[][]{
                {ReportType.PIE_CHART},
                {ReportType.DONUT_CHART},
        };
    }

    @DataProvider(name = "treeMapHeatMap")
    public Object[][] treeMapHeatMapChartTypeDataProvider() {
        return new Object[][]{
                {ReportType.TREE_MAP},
                {ReportType.HEAT_MAP},
        };
    }

    public void addGeoPushpinChart() {
        analysisPage = initAnalysePage().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_PUSHPIN, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_DISTRICT, FieldType.ATTRIBUTE)
                .addStack(ATTR_COUNTRY).waitForReportComputing()
                .saveInsight(GEO_PUSHPIN_INSIGHT);
    }

    public void openGeoPushpinChart() {
        initAnalysePage().openInsight(GEO_PUSHPIN_INSIGHT).waitForReportComputing();
    }

}
