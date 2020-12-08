package com.gooddata.qa.graphene.add;

import com.gooddata.qa.graphene.AbstractGeoPushpinTest;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel.LogicalOperator.EQUAL_TO;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForIndigoMessageDisappear;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.*;

public class RenderGeoPushpinTest extends AbstractGeoPushpinTest {

    private static final String ATTR_POPULATION = "population";
    private static final String ATTR_COUNTRY = "state";
    private static final String ATTR_GEO_CHART = "city";
    private static final String SEGMENT_FILTER = "Washington";
    private static final String GEO_INSIGHT = "GeoPushpinInsight";
    private static final String VIEWPORT_INSIGHT = "ViewportInsight";
    private static final String EMBEDDED_URI = "analyze/embedded/#/%s/reportId/edit";
    private AnalysisPage analysisPage;
    private GeoPushpinChartPicker geoChart;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_Render_ATTR_GEO_CHART_Test";
        useDynamicUser = false;
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"geopushpinproject"})
    public void checkUiForGeoPushpin() {
        analysisPage = initAnalysePage();
        List<String> attributeList = analysisPage.changeReportType(ReportType.GEO_CHART).getListAttributeBucketTitle();
        assertEquals(attributeList, asList("LOCATION", "MEASURE (SIZE)", "MEASURE (COLOR)", "SEGMENT BY"));

        List<String> bucketInvitationList = analysisPage.getListAttributeBucketInvitation();
        assertEquals(bucketInvitationList, asList("DRAG HERE", "DRAG OR OR HERE", "DRAG OR OR HERE", "DRAG OR HERE",
                "DRAG OR OR HERE"));
    }

    @Test(dependsOnMethods = {"checkUiForGeoPushpin"}, groups = {"geopushpinproject"})
    public void getTooltipGeoChart() {
        addGeoPushpinChart();
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
        assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
        assertTrue(geoChart.isZoomInAndZoomOutDisplayed(), "Zoom button should be show");
        geoChart.doubleClickOnZoomInBtn();
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertFalse(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be hidden");
        geoChart.doubleClickOnZoomOutBtn();
        sleepTightInSeconds(2);
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
        geoChart.clickFilterSegmentOnPushpinLegend(SEGMENT_FILTER);
        assertEquals(geoChart.getColorFilterSegmentOnPushpinLegend(SEGMENT_FILTER), "rgb(204, 204, 204)");
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertFalse(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be hidden");
        geoChart.clickFilterSegmentOnPushpinLegend(SEGMENT_FILTER);
        assertEquals(geoChart.getColorFilterSegmentOnPushpinLegend(SEGMENT_FILTER), "rgb(229, 77, 66)");
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be display");
    }

    @Test(dependsOnMethods = {"getTooltipGeoChart"}, groups = {"geopushpinproject"})
    public void renderGeoPushpinChart() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        log.info("Case 1:  Drag and drop non-geo attribute into Location bucket");
        analysisPage.tryToDragAttributeToLocationPushpin(ATTR_POPULATION, FieldType.FACT);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");

        log.info("Case 2:  Drag and drop geo attribute into Location bucket");
        analysisPage.addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO).waitForReportComputing();
        geoChart = GeoPushpinChartPicker.getInstance(browser);
        assertTrue(geoChart.isGeoRenderChartDisplayed(), "Geo render chart is not displayed");
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_CHART);

        log.info("Case 3:  GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (size)");
        analysisPage.addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        assertTrue(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend is not visible");
        assertThat(geoChart.getListIconPushpinSizeLegend(),
                hasItems("circle-min-icon", "circle-average-icon", "circle-max-icon"));
        analysisPage.removeMeasureSizeBucket("M1\n" + "Sum of " + ATTR_POPULATION);

        log.info("Case 4:  : GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (color) ");
        analysisPage.addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        assertFalse(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend is displayed");
        assertTrue(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend is not visible");
        assertEquals(geoChart.getListBackgroundColorPushpinLegend(),
                asList("(215, 242, 250, 0.7)", "(176, 229, 245, 0.7)", "(137, 216, 240, 0.7)", "(98, 203, 235, 0.7)",
                        "(59, 190, 230, 0.7)", "(20, 178, 226, 0.7)"));
        assertEquals(geoChart.getListLablesColorPushpinLegend(),
                asList("0", "882k", "1.8M", "2.6M", "3.5M", "4.4M", "5.3M"));

        log.info("Case 5:  : GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (size)" +
                "bucket, 1 measure on Measure (color) bucket");
        analysisPage.addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        assertTrue(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend is not visible");
        assertTrue(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend is not visible");
        assertEquals(geoChart.getListBackgroundColorPushpinLegend(),
                asList("(215, 242, 250, 0.7)", "(176, 229, 245, 0.7)", "(137, 216, 240, 0.7)", "(98, 203, 235, 0.7)",
                        "(59, 190, 230, 0.7)", "(20, 178, 226, 0.7)"));
        assertThat(geoChart.getListIconPushpinSizeLegend(),
                hasItems("circle-min-icon", "circle-average-icon", "circle-max-icon"));

        log.info("Case 6:  : GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (size)" +
                "bucket, 1 measure on Measure (color) bucket, 1 attribute on Segment By bucket");
        analysisPage.addStack(ATTR_COUNTRY).waitForReportComputing();
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend should be hidden");
        assertTrue(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend is not visible");
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category legend isn't displayed");
        analysisPage.addFilter(ATTR_COUNTRY);
        analysisPage.getFilterBuckets().getFilter(ATTR_COUNTRY).click();
        assertEquals(AttributeFilterPickerPanel.getInstance(browser).getItemNames(),
                geoChart.getAttrFilterOnPushpinLegend());
        analysisPage.getFilterBuckets().getFilter(ATTR_COUNTRY).click();
        analysisPage.removeFilter(ATTR_COUNTRY);

        log.info("Case 7: GEO pushpin with 1 geo attribute on Location bucket, 1 attribute on Segment By bucket");
        analysisPage.removeMeasureSizeBucket("M1\n" + "Sum of " + ATTR_POPULATION).waitForReportComputing();
        analysisPage.removeMeasureColorBucket("M1\n" + "Sum of " + ATTR_POPULATION).waitForReportComputing();
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend should be hidden");
        assertFalse(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend should be hidden");
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category legend isn't displayed");

        log.info("Case 8: GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (size)" +
                ", 1 attribute on Segment By bucket");
        analysisPage.addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend should be hidden");
        assertTrue(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend is not displayed");
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category legend isn't displayed");

        log.info("Case 9: GEO pushpin with 1 geo attribute on Location bucket, 1 measure on Measure (color) " + "" +
                "bucket, 1 attribute on Segment By bucket");
        analysisPage.removeMeasureSizeBucket("M1\n" + "Sum of " + ATTR_POPULATION).waitForReportComputing();
        analysisPage.addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT).waitForReportComputing();
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend should be hidden");
        assertFalse(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend should be hidden");
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category legend isn't displayed");

        log.info("Case 11: Hide the legend");
        analysisPage.removeStack();
        analysisPage.openFilterBarPicker().checkItem("Sum of " + ATTR_POPULATION, 1).apply();
        analysisPage.getFilterBuckets().getFilter("Sum of " + ATTR_POPULATION + " (M1)").click();
        MeasureValueFilterPanel.getInstance(browser).addMeasureValueFilter(EQUAL_TO, "726,666");
        assertFalse(geoChart.isPushpinColorLegendVisible(), "Geo pushpin color legend should be hidden");
        assertFalse(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size legend should be hidden");
        assertFalse(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category legend should be hidden");
    }

    @Test(dependsOnMethods = {"renderGeoPushpinChart"}, groups = {"geopushpinproject"})
    public void undoRedoClearActionOnChart() {
        addGeoPushpinChart();
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), ATTR_GEO_CHART);
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(),
                "M2\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                "M1\n" + "Sum of " + ATTR_POPULATION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_COUNTRY);
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertTrue(analysisPageHeader.isOpenButtonEnabled(), "Open button should be enabled");
        assertTrue(analysisPageHeader.isResetButtonEnabled(), "Clear button should be enabled");
        assertTrue(analysisPageHeader.isSaveButtonEnabled(), "Save button should be enabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
        analysisPage.undo();
        assertNotEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_COUNTRY);
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertTrue(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be enabled");
        analysisPage.redo();
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_COUNTRY);
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
        analysisPage.clear();
        analysisPage.changeReportType(ReportType.GEO_CHART);
        assertEquals(analysisPage.getLocationBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureColorBucket().getAttributeName(), "");
        assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(), "");
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), "");
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertTrue(analysisPageHeader.isOpenButtonEnabled(), "Open button should be enabled");
        assertTrue(analysisPageHeader.isResetButtonEnabled(), "Clear button should be enabled");
        assertFalse(analysisPageHeader.isSaveButtonEnabled(), "Save button should be disabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Redo button should be disabled");
    }

    @Test(dependsOnMethods = {"undoRedoClearActionOnChart"}, groups = {"geopushpinproject"})
    public void applyFilterIntoGeoChart() {
        addGeoPushpinChart();
        analysisPage.addFilter(ATTR_COUNTRY);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_COUNTRY,"Delaware", "Washington");
        assertEquals(geoChart.getAttrFilterOnPushpinLegend(), asList("Delaware", "Washington"));
        analysisPage.removeFilter(ATTR_COUNTRY).waitForReportComputing();
    }

    @Test(dependsOnMethods = {"applyFilterIntoGeoChart"}, groups = {"geopushpinproject"})
    public void testGeoPushpinInEmbeddedMode() throws IOException {
        try {
            openUrl(getEmbeddedAdUrl());
            analysisPage = getEmbeddedAnalysisPage().changeReportType(ReportType.GEO_CHART)
                    .addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO)
                    .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                    .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT)
                    .addStack(ATTR_COUNTRY).waitForReportComputing();
            sleepTightInSeconds(2);
            geoChart = GeoPushpinChartPicker.getInstance(browser);
            geoChart.hoverOnGeoPushpin(-445, -120);
            assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
            assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
            assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category is not visibled");
            assertTrue(geoChart.isPushpinSizeLegendVisible(), "Geo pushpin size is not visibled");
            geoChart.doubleClickOnZoomInBtn();
            geoChart.hoverOnGeoPushpin(-445, -120);
            assertFalse(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be hidden");
            geoChart.doubleClickOnZoomOutBtn();
            sleepTightInSeconds(2);
            geoChart.hoverOnGeoPushpin(-445, -120);
            assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");

            analysisPage.exportTo(OptionalExportMenu.File.CSV);
            final File exportCsvFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + "Untitled insight" + "." + ExportFormat.CSV.getName());
            waitForExporting(exportCsvFile);
            assertEquals(exportCsvFile.getName(),  "Untitled insight.csv");
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath("Untitled insight.csv")));
        }
    }

    @Test(dependsOnMethods = {"testGeoPushpinInEmbeddedMode"}, groups = {"renderGeoChart"})
    public void renderProtectedAttributeGeoPushpinChart() throws IOException {
        AttributeRestRequest attributeRestRequest = new AttributeRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        String attributeUri = attributeRestRequest.getAttributeByTitle(ATTR_COUNTRY).getUri();
        try {
            attributeRestRequest.setAttributeProtected(attributeUri);
            analysisPage =initAnalysePage().changeReportType(ReportType.GEO_CHART)
                    .addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO)
                    .addAttributeToMeasureSize(ATTR_COUNTRY, FieldType.ATTRIBUTE).waitForReportComputing()
                    .saveInsight(GEO_INSIGHT);
            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);
            initAnalysePage().openInsight(GEO_INSIGHT).waitForReportComputing();
            assertEquals(analysisPage.getMeasureSizeBucket().getAttributeName(),
                    "M1\n" + "Count of " + ATTR_COUNTRY);
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" +
                    "Contact your administrator.");
            Screenshots.takeScreenshot(browser, "protectedAttributeWithGeoPushpinChart", getClass());
        } finally {
            attributeRestRequest.unsetAttributeProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"renderProtectedAttributeGeoPushpinChart"}, groups = {"renderGeoChart"})
    public void renderRestrictFactGeoPushpinChart() throws IOException {
        FactRestRequest factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        String factUri = factRestRequest.getFactByTitle(ATTR_POPULATION).getUri();
        try {
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.GEO_CHART)
                    .addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO)
                    .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                    .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT)
                    .addStack(ATTR_COUNTRY).waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).confirmExport();
            assertEquals(ElementUtils.getErrorMessage(browser),
                    "You cannot export this insight because it contains restricted data.");
        } finally {
            factRestRequest.unsetFactRestricted(factUri);
        }
    }

    @Test(dependsOnMethods = {"renderRestrictFactGeoPushpinChart"}, groups = {"geopushpinproject"})
    public void exportGeoPushpinInsightOnAD() throws IOException {
        addGeoPushpinChart();
        exportGeoPushpinChartToCsvFile("Untitled insight", "/geoPushpinChart.csv");
        waitForIndigoMessageDisappear(browser);
        exportGeoPushpinChartToXlsxFile("Untitled insight", "/geoPushpinChart.xlsx");
    }

    @Test(dependsOnMethods = {"exportGeoPushpinInsightOnAD"}, groups = {"geopushpinproject"})
    public void exportGeoPushpinInsightWithAttributeFilterOnAD() throws IOException {
        analysisPage.addFilter(ATTR_COUNTRY).waitForReportComputing();
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_COUNTRY,"Delaware");
        analysisPage.waitForReportComputing();
        exportGeoPushpinChartToXlsxFile("Untitled insight", "/filterGeoPushpin.xlsx");
        waitForIndigoMessageDisappear(browser);
        exportGeoPushpinChartToCsvFile("Untitled insight", "/filterGeoPushpin.csv");
    }

    @Test(dependsOnMethods = {"checkUiForGeoPushpin"}, groups = {"geopushpinproject"})
    public void verifyViewportAfterReopenInsight() {
        addGeoPushpinChart();
        analysisPage.saveInsight(VIEWPORT_INSIGHT).waitForReportComputing();
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
        assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
        Map<String, String> expectedTooltip = geoChart.getPopupTooltipDetails();
        analysisPage.openInsight(VIEWPORT_INSIGHT).waitForReportComputing();
        sleepTightInSeconds(2);
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed - Bug SD-1070");
        assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
        Map<String, String> currentTooltip = geoChart.getPopupTooltipDetails();
        assertThat(expectedTooltip, equalTo(currentTooltip));
    }

    public void exportGeoPushpinChartToXlsxFile(String insightName, String fileCompare) throws IOException {
        try {
            analysisPage.exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).uncheckOption(ExportXLSXDialog.OptionalExport.CELL_MERGED).confirmExport();
            final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + insightName + "." + ExportFormat.EXCEL_XLSX.getName());
            waitForExporting(exportFile);
            assertEquals(exportFile.getName(), insightName + ".xlsx");
            assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), XlsxUtils.excelFileToRead(
                    getFilePathFromResource("/" + PAYROLL_CSV + fileCompare), 0));
        }
        finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(
                    insightName + "." + ExportFormat.EXCEL_XLSX.getName())));
        }
    }

    public void exportGeoPushpinChartToCsvFile(String insightName, String fileCompare) throws IOException {
        try {
            analysisPage.exportTo(OptionalExportMenu.File.CSV);
            final File exportCsvFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + insightName + "." + ExportFormat.CSV.getName());
            waitForExporting(exportCsvFile);
            assertEquals(exportCsvFile.getName(), insightName + ".csv");
            assertEquals(CSVUtils.readCsvFile(exportCsvFile),
                    CSVUtils.readCsvFile(getResourceAsFile("/" + PAYROLL_CSV + fileCompare)));
        } finally {
            deleteIfExists(Paths.get(testParams.getExportFilePath(
                    insightName + "." + ExportFormat.CSV.getName())));
        }
    }

    public void addGeoPushpinChart() {
        analysisPage = initAnalysePage().clear().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_CHART, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT)
                .addStack(ATTR_COUNTRY).waitForReportComputing();
        sleepTightInSeconds(3);
        geoChart = GeoPushpinChartPicker.getInstance(browser);
    }

    private String getEmbeddedAdUrl() {
        return getRootUrl() + format(EMBEDDED_URI, testParams.getProjectId());
    }

    private EmbeddedAnalysisPage getEmbeddedAnalysisPage() {
        return EmbeddedAnalysisPage.getInstance(browser);
    }
}
