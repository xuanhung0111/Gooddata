package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.AbstractGeoPushpinTest;
import com.gooddata.qa.graphene.enums.GeoPointSize;
import com.gooddata.qa.graphene.enums.LegendPosition;
import com.gooddata.qa.graphene.enums.MapArea;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.utils.http.ColorPaletteRequestData;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.enums.LegendPosition.AUTO_DEFAULT;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

public class ConfigurationOnGeoPushpinTest extends AbstractGeoPushpinTest {

    private final String ATTR_POPULATION = "population";
    private final String ATTR_COUNTRY = "state";
    private final String ATTR_GEO_PUSHPIN = "city";
    private AnalysisPage analysisPage;
    private GeoPushpinChartPicker geoChart;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_CONFIG_ATTR_GEO_PUSHPIN_TEST";
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"geoPushpinProject"})
    public void configColorOnlyLocationBucket() {
        addGeoPushpinChartOnlyLocationBucket();
        analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .openColorsPaletteDialog(ColorPaletteRequestData.ColorPalette.CYAN.toCssFormatString())
                .getColorsPaletteDialog().selectColor(ColorPaletteRequestData.ColorPalette.PURPLE
                .toReportFormatString());
        assertTrue(analysisPage.openConfigurationPanelBucket().openColorConfiguration().isResetButtonVisibled(),
                "A Reset Button should be visible");
        assertEquals(analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .getColor(ColorPaletteRequestData.ColorPalette.PURPLE.toCssFormatString()), "rgb(171,85,163)");

        analysisPage.openConfigurationPanelBucket().openColorConfiguration().resetColor();
        assertEquals(analysisPage.openConfigurationPanelBucket().openColorConfiguration()
                .getColor(ColorPaletteRequestData.ColorPalette.CYAN.toCssFormatString()), "rgb(20,178,226)");

        assertTrue(analysisPage.openConfigurationPanelBucket().openLegendConfiguration().isToggleDisabled(),
                "Legend toggle should be turn off");
    }

    @Test(dependsOnMethods = {"configColorOnlyLocationBucket"}, groups = {"geoPushpinProject"})
    public void configMapOnlyLocationBucket() {
        ConfigurationPanelBucket.ItemConfiguration itemConfig = analysisPage.openConfigurationPanelBucket()
                .openMapConfiguration();
        assertEquals(itemConfig.getDefaultValueInDropDownList(), "Include all data");

        List<MapArea> listArea = asList(MapArea.AFRICA, MapArea.WORLD, MapArea.AMERICAN_NORTH, MapArea.AMERICAN_SOUTH,
                MapArea.ASIA, MapArea.AUSTRALIA, MapArea.EUROPE, MapArea.ALL_DATA);
        for (MapArea area : listArea) {
            itemConfig.openItemsSelectedInConfiguration().selectArea(area);
            assertEquals(itemConfig.getDefaultValueInDropDownList(), area.getCountryName());
        }
        assertEquals(itemConfig.openItemsSelectedInConfiguration().getListItems(),
                asList("Include all data", "World", "CONTINENTS", "Africa", "America (North)", "America (South)",
                        "Asia", "Australia", "Europe"));
    }

    @Test(dependsOnMethods = {"configMapOnlyLocationBucket"}, groups = {"geoPushpinProject"})
    public void configPointsOnlyLocationBucket() {
        analysisPage.openConfigurationPanelBucket().openMapConfiguration().collapseItemsSelected();
        ConfigurationPanelBucket.ItemConfiguration pointPannel = analysisPage.openConfigurationPanelBucket()
                .openPointsConfiguration();
        assertTrue(pointPannel.isPointsGroupChecked(), "Group Nearby Points should be checked");
        assertEquals(pointPannel.getDefaultSmallestSize(), "auto (default)");
        assertEquals(pointPannel.getDefaultLargestSize(), "auto (default)");
    }

    @Test(dependsOnMethods = {"configPointsOnlyLocationBucket"}, groups = {"geoPushpinProject"})
    public void configLegendWithFullBucket() {
        addGeoPushpinChartWithFullBucket();
        ConfigurationPanelBucket.ItemConfiguration legendPanel = analysisPage.openConfigurationPanelBucket()
                .openLegendConfiguration();
        geoChart = GeoPushpinChartPicker.getInstance(browser);
        assertTrue(legendPanel.isToggleTurnOn(), "Legend toggle should be turn on");
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo legend should be displayed");
        List<LegendPosition> position = asList(LegendPosition.TOP, LegendPosition.BOTTOM, LegendPosition.RIGHT,
                LegendPosition.LEFT);
        for (LegendPosition location : position) {
            legendPanel.openItemsSelectedInConfiguration().selectLegendPosition(location);
            assertEquals(legendPanel.getDefaultValueInDropDownList(), location.getPosition());
            assertTrue(geoChart.getAttributeGeoLegend().contains("position-" + location.getPosition()));
        }
        legendPanel.openItemsSelectedInConfiguration().selectLegendPosition(AUTO_DEFAULT);
        assertEquals(legendPanel.getDefaultValueInDropDownList(), AUTO_DEFAULT.getPosition());
        assertTrue(geoChart.getAttributeGeoLegend().contains("position-top"));
        legendPanel.switchOff();
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo legend should be hidden");
    }

    @Test(dependsOnMethods = {"configLegendWithFullBucket"}, groups = {"geoPushpinProject"})
    public void configPointsWithFullBucket() {
        ConfigurationPanelBucket.ItemConfiguration pointPannel = analysisPage.openConfigurationPanelBucket()
                .openPointsConfiguration();
        assertFalse(pointPannel.isPointsGroupDisabled(), "Group Nearby Points should be hidden");
        assertEquals(pointPannel.getDefaultSmallestSize(), "auto (default)");
        assertEquals(pointPannel.getDefaultLargestSize(), "auto (default)");
        List<GeoPointSize> pointSizes = asList(GeoPointSize.DEFAULT, GeoPointSize.SIZE_0_5X, GeoPointSize.SIZE_0_75X,
                GeoPointSize.NORMAL, GeoPointSize.SIZE_1_25X, GeoPointSize.SIZE_1_5X);
        for (GeoPointSize point : pointSizes) {
            pointPannel.openPointsSizeSelected(0).selectPointSize(point);
            assertEquals(pointPannel.getDefaultSmallestSize(), point.getSize());
            pointPannel.openPointsSizeSelected(1).selectPointSize(point);
            assertEquals(pointPannel.getDefaultLargestSize(), point.getSize());
        }
    }

    public void addGeoPushpinChartWithFullBucket() {
        analysisPage = initAnalysePage().clear().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_PUSHPIN, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT)
                .addStack(ATTR_COUNTRY)
                .waitForReportComputing();
    }

    public void addGeoPushpinChartOnlyLocationBucket() {
        analysisPage = initAnalysePage().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_PUSHPIN, FieldType.GEO)
                .waitForReportComputing();
    }
}
