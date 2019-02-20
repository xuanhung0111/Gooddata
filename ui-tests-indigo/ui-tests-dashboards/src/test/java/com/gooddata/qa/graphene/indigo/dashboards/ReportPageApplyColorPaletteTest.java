package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.utils.http.RestClient;

import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;

import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;
import com.gooddata.qa.browser.BrowserUtils;

import java.awt.AWTException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.initColorPalette;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class ReportPageApplyColorPaletteTest extends GoodSalesAbstractTest {

    private static final String TESTING_COLOR_PALETTE_REPORT_CHART = "Testing color palette report chart";
    private static List<Pair<String, ColorPalette>> listColorPalettes = Arrays.asList(Pair.of("guid1", ColorPalette.RED),
            Pair.of("guid2", ColorPalette.GREEN), Pair.of("guid3", ColorPalette.BLUE), Pair.of("guid4", ColorPalette.YELLOW));

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Report-Page-Apply-Color-Palette-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest.setColor(initColorPalette(listColorPalettes));
    }

    public void deleteIndigoRestRequestColorsPalette() {
        IndigoRestRequest indigoRestDeleteRequest = new IndigoRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestDeleteRequest.deleteColorsPalette();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReportChartApplyColorPalette() throws AWTException {
        initReportCreation().setReportName(TESTING_COLOR_PALETTE_REPORT_CHART)
                .openWhatPanel()
                .selectItems(METRIC_NUMBER_OF_ACTIVITIES, METRIC_OPP_FIRST_SNAPSHOT);
        reportPage.openHowPanel().selectItem(ATTR_ACTIVITY_TYPE).done();
        reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress();
        reportPage.finishCreateReport();
        BrowserUtils.zoomBrowser(browser);
        browser.navigate().refresh();
        initReportsPage().openReport(TESTING_COLOR_PALETTE_REPORT_CHART).waitForReportExecutionProgress();
        reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress();
        takeScreenshot(browser, "testReportChartApplyColorPalette", getClass());
        assertEquals(reportPage.checkColorColumn(0), ColorPalette.RED.toReportFormatString());
        assertEquals(reportPage.getReportLegendColors(), asList(ColorPalette.RED.toReportFormatString(), ColorPalette.GREEN.toReportFormatString()));
    }

    @Test(dependsOnMethods = {"testReportChartApplyColorPalette"})
    public void testReportChartNotApplyColorPalette() throws AWTException {
        deleteIndigoRestRequestColorsPalette();
        initReportsPage().openReport(TESTING_COLOR_PALETTE_REPORT_CHART).waitForReportExecutionProgress();
        reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress();
        takeScreenshot(browser, "testReportChartNotApplyColorPalette", getClass());
        assertNotEquals(reportPage.checkColorColumn(0), ColorPalette.RED.toReportFormatString());
        assertNotEquals(reportPage.getReportLegendColors(), asList(ColorPalette.RED.toReportFormatString(), ColorPalette.GREEN.toReportFormatString()));
        BrowserUtils.resetZoomBrowser(browser);
    }
}
