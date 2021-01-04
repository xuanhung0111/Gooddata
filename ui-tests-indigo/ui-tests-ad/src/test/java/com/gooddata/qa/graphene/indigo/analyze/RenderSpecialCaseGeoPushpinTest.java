package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.SkipException;
import org.testng.annotations.Test;


import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.XAE_VERSION;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForMainPageLoading;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class RenderSpecialCaseGeoPushpinTest extends AbstractProjectTest {

    private static final String ATTR_LATLONG = "Latlong";
    private static final String ATTR_CHART_TYPE = "Geo pushpin";
    private static final String ATTR_POPULATION = "Population";
    private static final String CSV_GEO_25k1_POINTS = "/Geo25k1Points.csv";
    private static final String CSV_GEO_25k_POINTS = "/Geo25kPoints.csv";
    private static final String CSV_GEO_POINTS = "/GeoPoints.csv";
    private static final String ATTR_STATE = "State";
    private static String AD_DATASET_LINK = "https://%s/analyze/#/%s/reportId/edit?dataset=%s";
    private AnalysisPage analysisPage;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_RENDER_GEO_CHART_TEST";
    }

    @Override
    public void customizeProject() throws Throwable {
        if (BrowserUtils.isFirefox()) {
            throw new SkipException("Skip test case on Firefox Browser due to disabled weblg ");
        }
        projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.updateProjectConfiguration(XAE_VERSION.getFlagName(), "3");
    }

    @Test(dependsOnGroups= {"createProject"}, groups = {"rendergeoChart"})
    public void renderNoLocationGeoPushpinChart() {
        final String MESSAGE_SYSTEM = "NO LOCATION IN YOUR INSIGHT" +
                "\nAdd a geo attribute to your insight, or switch to other visualization." +
                "\nOnce done, you'll be able to save it.";
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + CSV_GEO_POINTS));
        initAttributePage().initAttribute(ATTR_LATLONG).selectLabelType(ATTR_CHART_TYPE);
        String adDatasetUrl = format(AD_DATASET_LINK, testParams.getHost(), testParams.getProjectId(), "" +
                "dataset.csv_geopoints");
        openUrl(adDatasetUrl);
        waitForMainPageLoading(browser);
        analysisPage = AnalysisPage.getInstance(browser);
        analysisPage.changeReportType(ReportType.GEO_CHART).addAttributeToMeasureSize(ATTR_POPULATION, FieldType.FACT).
                waitForReportComputing();

        assertThat(analysisPage.getMainEditor().getCanvasMessage(), containsString(MESSAGE_SYSTEM));
    }

    @Test(dependsOnMethods = {"renderNoLocationGeoPushpinChart"}, groups = {"rendergeoChart"})
    public void renderGeoPushpinChartWithAXEVersion3() {
        final String MESSAGE_SYSTEM = "SORRY, WE CAN'T DISPLAY THIS INSIGHT" +
                "\nTry applying different filters, or using different measures or attributes." +
                "\nIf this did not help, contact your administrator.";
        analysisPage.clear();
        analysisPage.changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_LATLONG, FieldType.GEO).waitForReportComputing()
                .addAttributeToMeasureColor(ATTR_STATE, FieldType.ATTRIBUTE).waitForReportComputing();

        assertFalse(analysisPage.getMainEditor().isExplorerMessageVisible(),
                "Geo chart cannot render on canvas with AXE version 3");
    }

    @Test(dependsOnMethods = {"renderGeoPushpinChartWithAXEVersion3"}, groups = {"rendergeoChart"})
    public void renderNoDataGeoPushpinChart() {
        analysisPage.clear();
        DateFilterPickerPanel dateFilter = analysisPage.changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_LATLONG, FieldType.GEO).waitForReportComputing()
                .addAttributeToMeasureColor(ATTR_POPULATION, FieldType.FACT).waitForReportComputing()
                .addDateFilter().getFilterBuckets().openDateFilterPickerPanel();

        dateFilter.configTimeFilterByRangeHelper("01/01/2020", "01/02/2020");
        assertEquals(dateFilter.getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");
        assertFalse(dateFilter.isCompareTimePeriodPresent(), "Not support compare with the period time");
        dateFilter.apply();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO DATA FOR YOUR FILTER SELECTION\n" +
                "Try adjusting or removing some of the filters.");
    }

    @Test(dependsOnMethods = {"renderNoDataGeoPushpinChart"}, groups = {"rendergeoChart"})
    public void render25kPointsOnGeoPushpinChart() {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + CSV_GEO_25k_POINTS));
        initAttributePage().initAttribute(ATTR_LATLONG).selectLabelType(ATTR_CHART_TYPE);
        String adDatasetUrl = format(AD_DATASET_LINK, testParams.getHost(), testParams.getProjectId(),
                "dataset.csv_geo25kpoints");
        openUrl(adDatasetUrl);
        waitForMainPageLoading(browser);
        analysisPage.changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_LATLONG, FieldType.GEO).waitForReportComputing();
        sleepTightInSeconds(2); // need to sleep because chart will be render after selected chart type
        assertFalse(analysisPage.getMainEditor().isExplorerMessageVisible(),
                "Geo chart can load with 25k points on canvas");
    }

    @Test(dependsOnMethods = {"render25kPointsOnGeoPushpinChart"}, groups = {"rendergeoChart"})
    public void renderOver25kPointsGeoPushpinChart() {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + CSV_GEO_25k1_POINTS));
        initAttributePage().initAttribute(ATTR_LATLONG).selectLabelType(ATTR_CHART_TYPE);
        String adDatasetUrl = format(AD_DATASET_LINK, testParams.getHost(), testParams.getProjectId(),
                "dataset.csv_geo25k1points");
        openUrl(adDatasetUrl);
        waitForMainPageLoading(browser);
        analysisPage.changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_LATLONG, FieldType.GEO).waitForReportComputing();

        assertThat(analysisPage.getMainEditor().getCanvasMessage(), containsString(
                "TOO MANY DATA POINTS TO DISPLAY\n" + "Add a filter, or switch to table view."));

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertFalse(analysisPage.getMainEditor().isExplorerMessageVisible(), "Geo should be show correctly");
    }
}
