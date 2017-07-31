package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_STAGE_VELOCITY;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesSaveInsightTest extends GoodSalesAbstractAnalyseTest {

    private static final String INSIGHT_TEST = "Insight-Test";
    private static final String INSIGHT_TEST_LONG = "British scientists were crucial to the success of the Manhattan Project, "
            + "British scientists were crucial to the success of the Manhattan Project "
            + "British scientists were crucial to the success of the Manhattan Project "
            + "British scientists were crucial to the success of the Manhattan Project "
            + "British scientists were crucial to the success of the Manhattan Project ";
    private static final String INSIGHT_TEST_SPECIAL = "@#$%^&*()";
    private static final String INSIGHT_TEST_DUPLICATE = "Insight-Test";
    private static final String INSIGHT_TEST_NULL = "";
    private static final String CLOSED = "Closed";
    private static final String CREATED = "Created";
    private static final String DATE_CLOSED_DIMENSION_INSIGHT = "Save-Insight-Containing-Date-Closed-Dimension";
    private static final String DATE_CREATED_DIMENSION_INSIGHT = "Save-Insight-Containing-Date-Created-Dimension";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Save-Insight-Test";
    }

    @DataProvider(name = "chartTypeDataProvider")
    public Object[][] inSightNameDataProvider() {
        return new Object[][]{
                {INSIGHT_TEST},
                {INSIGHT_TEST_LONG},
                {INSIGHT_TEST_SPECIAL},
                {INSIGHT_TEST_DUPLICATE}
        };
    }
    @Test(dependsOnGroups = {"init"}, dataProvider = "chartTypeDataProvider")
    public void testSaveInsight(String insightName) throws JSONException, IOException {
        final int expectedTrackerCount = analysisPage
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        analysisPage.setInsightTitle(insightName).saveInsight();
        assertFalse(isElementVisible(className(SaveInsightDialog.ROOT_CLASS), browser),
                "Save dialog exists");
        //make sure data is cleared before open insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(analysisPage.openInsight(insightName).getChartReport().getTrackersCount(),
                expectedTrackerCount);
    }

    @Test(dependsOnGroups = {"init"})
    public void testSaveInsightWithBlankName () throws JSONException, IOException {
        analysisPage.setInsightTitle(INSIGHT_TEST_NULL);
        assertFalse(analysisPage.isSaveInsightEnabled());
    }

    @Test(dependsOnMethods = {"testSaveInsight"})
    public void testSaveAsUsingExistingInsightWithoutChange() {
        final String insight = "Save-As-Using-Existing-Insight-Without-Change";
        final int expectedTrackersCount = analysisPage
                .openInsight(INSIGHT_TEST)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        //make sure the workspace is blank before opening insight
        assertTrue(analysisPage.saveInsightAs(insight).resetToBlankState().isBlankState(),
                "The workspace is not blank");
        assertEquals(analysisPage.openInsight(insight).getChartReport().getTrackersCount(), expectedTrackersCount);
    }

    @Test(dependsOnMethods = {"testSaveInsight"})
    public void testSaveAsUsingExistingInsightWithChange() {
        final String insight = "Save-As-Using-Existing-Insight-With-Change";
        analysisPage.openInsight(INSIGHT_TEST)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting");
        final int expectedTrackersCount = analysisPage
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        //make sure the workspace is blank before opening insight
        assertTrue(analysisPage.saveInsightAs(insight).resetToBlankState().isBlankState(),
                "The workspace is not blank");
        assertEquals(
                analysisPage.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getTrackersCount(),
                expectedTrackersCount);

        //check original insight is not affected by save as
        assertEquals(analysisPage.openInsight(INSIGHT_TEST).waitForReportComputing().getChartReport()
                .getTrackersCount(), 4);
    }

    @Test(dependsOnGroups = {"init"})
    public void testSaveUntitledInsight() throws JSONException, IOException {
        final String insight = "Untitled-Insight-Test";
        final List<String> expectedLabels = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport()
                .getDataLabels();
        analysisPage.saveInsight(insight);

        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " does not exist in Saved Insight list");
        assertEquals(
                analysisPage.openInsight(insight)
                        .waitForReportComputing()
                        .getChartReport()
                        .getDataLabels(),
                expectedLabels);
    }

    @Test(dependsOnGroups = {"init"})
    public void testInsightNameInSaveDialog() throws JSONException, IOException {
        final String insight = "Untitled-Insight-Test-2";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();

        AnalysisPageHeader pageheader =  analysisPage.getPageHeader();
        pageheader.saveWithoutSubmitting(insight).cancel();
        pageheader.saveInsight();
        //After cancel and save again, the Save Dialog is not cached the old information.
        assertTrue(SaveInsightDialog.getInstance(browser).isSubmitButtonDisabled());
        assertEquals(SaveInsightDialog.getInstance(browser).getName(), "");
    }

    @Test(dependsOnGroups = {"init"})
    public void trySavingUntitledInsightButCancel() throws JSONException, IOException {
        final String insight = "No-Saved-Insight-After-Canceling-Test";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getPageHeader()
                .saveWithoutSubmitting(insight)
                .cancel();

        assertFalse(isElementPresent(className(SaveInsightDialog.ROOT_CLASS), browser),
                "Save dialog does not exist");
        assertFalse(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " exists in Saved Insight list");
    }

    @Test(dependsOnGroups = {"init"})
    public void testEditSavedInsight() throws JSONException, IOException {
        final String insight = "Editing-Saved-Insight-Test";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .saveInsight(insight);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                "The Insight is not created");

        final List<String> expectedLabels = analysisPage
                .removeAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getDataLabels();
        analysisPage.saveInsight();
        takeScreenshot(browser, "Edit-Insight-Test", getClass());

        //make sure data is cleared before open insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        assertEquals(analysisPage.openInsight(insight).waitForReportComputing().getChartReport()
                .getDataLabels(), expectedLabels);
    }

    @Test(dependsOnGroups = {"init"})
    public void testSaveAsButtonNotPresentInBlankState() {
        assertFalse(analysisPage.getPageHeader().isSaveAsPresent(),
                "Save As button is displayed at start state");
    }

    @Test(dependsOnGroups = {"init"})
    public void cannotSaveAsInsightCreatedFromBlankState() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        assertFalse(analysisPage.getPageHeader().isSaveAsPresent(),
                "Save As button is displayed after adding a few changes to blank state");
    }

    @Test(dependsOnGroups = {"init"}, groups = {"save-insight-containing-date-dimension"})
    public void testSaveInsightContainingDateClosedDimension() {
        final int expectedTrackers = analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "The selected dimension was not displayed");

        analysisPage.saveInsight(DATE_CLOSED_DIMENSION_INSIGHT);
        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState(), "Working workspace was not been cleared");
        analysisPage.openInsight(DATE_CLOSED_DIMENSION_INSIGHT).waitForReportComputing();

        takeScreenshot(browser, "Save-Insight-Containing-Date-Closed-Dimension", getClass());
        assertEquals(analysisPage.getChartReport().getTrackersCount(), expectedTrackers, "Chart render was not " +
                "correct");
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "The selected dimension was not displayed");
    }

    @Test(dependsOnGroups = {"init"}, groups = {"save-insight-containing-date-dimension"})
    public void testSaveInsightContainingDateCreatedDimension() {
        final int expectedTrackers = analysisPage.addMetric(METRIC_STAGE_VELOCITY)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount();

        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "The selected dimension was not displayed");

        analysisPage.saveInsight(DATE_CREATED_DIMENSION_INSIGHT);
        //make sure data is cleared before opening insight
        assertTrue(analysisPage.resetToBlankState().isBlankState(), "Working workspace was not been cleared");
        analysisPage.openInsight(DATE_CREATED_DIMENSION_INSIGHT).waitForReportComputing();

        takeScreenshot(browser, "Save-Insight-Containing-Date-Created-Dimension", getClass());
        assertEquals(analysisPage.getChartReport().getTrackersCount(), expectedTrackers, "Chart render was not " +
                "correct");
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "The selected dimension was not displayed");
    }

    @Test(dependsOnGroups = {"save-insight-containing-date-dimension"},
            description = "CL-9969: Date is remembered from previous viz")
    public void testDateDimensionOnSavedInsights() {
        //make sure data is cleared before opening first insight
        assertTrue(analysisPage.resetToBlankState().isBlankState());
        analysisPage.openInsight(DATE_CLOSED_DIMENSION_INSIGHT).waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CLOSED,
                "Selected date dimension of " + DATE_CLOSED_DIMENSION_INSIGHT + "was not correct");

        //make sure the first insight is loaded
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), DATE_CLOSED_DIMENSION_INSIGHT);
        //the second insight should be opened right after the first one
        assertEquals(analysisPage.openInsight(DATE_CREATED_DIMENSION_INSIGHT).waitForReportComputing()
                .getPageHeader().getInsightTitle(), DATE_CREATED_DIMENSION_INSIGHT);
        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), CREATED,
                "Selected date dimension of " + DATE_CREATED_DIMENSION_INSIGHT + "was not correct");
    }

    @Test(dependsOnGroups = {"init"})
    public void saveInsightAfterOpenAsReport() {
        String insight = "Save-Insight-After-Open-As-Report";
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).changeReportType(ReportType.TABLE).waitForReportComputing();

        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            reportPage.getTableReport().waitForReportLoading();
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }

        analysisPage.saveInsight(insight).resetToBlankState().openInsight(insight).waitForReportComputing();

        takeScreenshot(browser, "saveInsightAfterOpenAsReport", getClass());
        assertEquals(analysisPage.getTableReport().getContent(),
                asList(asList("Email", "33,920"), asList("In Person Meeting", "35,975"),
                        asList("Phone Call", "50,780"), asList("Web Meeting", "33,596")));
    }
}
