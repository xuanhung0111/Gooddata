package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.DateRange.ALL_TIME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

import java.util.List;

public class ManualResizingColumnTest extends AbstractAnalyseTest {

    private static final String INSIGHT = "Insight";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Resizing Column Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_TABLE_COLUMN_AUTO_RESIZING, true);
        getMetricCreator().createNumberOfOpportunitiesMetric();
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void manualResizeOneColumn() {
        initAnalysePage().changeReportType(ReportType.TABLE)
                .addMetric(METRIC_AMOUNT).addMetric(METRIC_NUMBER_OF_OPPORTUNITIES).waitForReportComputing()
                .addAttribute(ATTR_IS_WON).waitForReportComputing()
                .addColumnsAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        PivotTableReport tableReport = analysisPage.getPivotTableReport();
        int previousSize = tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth();
        //Resize & revert size one column
        tableReport.resizeColumn(METRIC_AMOUNT, 0, 30);
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should be resized");
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth(), previousSize);

        tableReport.revertSizeColumn(METRIC_AMOUNT, 0);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize); //Sometimes width + 9
        previousSize = tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth();
        //Resize & revert size same type columns
        tableReport.resizeColumn(METRIC_AMOUNT, 1, 30, Keys.ALT);
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should be resized");
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth());

        tableReport.revertSizeColumn(METRIC_AMOUNT, 0, Keys.ALT);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth(), previousSize);
        previousSize = tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth();
        int previousSizeTarget = tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,0).getSize().getWidth();
        //Resize & revert size all columns
        tableReport.resizeColumn(METRIC_AMOUNT, 1, 10, Keys.COMMAND);
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should be resized");
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(),
                tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,1).getSize().getWidth());

        tableReport.revertSizeColumn(METRIC_AMOUNT, 0, Keys.COMMAND);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize);
        assertEquals(tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,1).getSize().getWidth(), previousSizeTarget);

        tableReport = analysisPage.saveInsight(INSIGHT).waitForReportComputing().getPivotTableReport();
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize);
        assertEquals(tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,1).getSize().getWidth(), previousSizeTarget);
    }

    @Test(dependsOnMethods = {"manualResizeOneColumn"})
    public void manualResizeColumnOnEmbeddedMode() {
        initEmbeddedAnalysisPage().openInsight(INSIGHT);
        PivotTableReport tableReport = analysisPage.waitForReportComputing().getPivotTableReport();

        int previousSize = tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth();
        int previousSizeTarget = tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,0).getSize().getWidth();
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize);
        assertEquals(tableReport.getHeaderElement(METRIC_NUMBER_OF_OPPORTUNITIES,1).getSize().getWidth(), previousSizeTarget);

        tableReport.resizeColumn(METRIC_AMOUNT, 0, 30);
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should be resized");
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,1).getSize().getWidth(), previousSize);

        analysisPage.reorderMetric(METRIC_AMOUNT, METRIC_NUMBER_OF_OPPORTUNITIES).waitForReportComputing();
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should keep sized with re-oder");

        tableReport.revertSizeColumn(METRIC_AMOUNT, 0);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize); //Sometimes width + 9

        analysisPage.undo().waitForReportComputing();
        assertNotEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize, "Should resize to previous step");

        analysisPage.removeMetric(METRIC_AMOUNT).waitForReportComputing().addMetric(METRIC_AMOUNT).waitForReportComputing();
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize, "Shouldn't keep manual resize of metric");
    }

    @Test(dependsOnMethods = {"manualResizeOneColumn"})
    public void manualResizeColumnOnDashboard() {
        String dashboardTitle = "Dashboard";
        IndigoDashboardsPage indigoDashboardsPage =
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboardTitle).addInsight(INSIGHT).waitForWidgetsLoading();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(ALL_TIME).apply();
        PivotTableReport tableReport = indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport();
        assertEquals(tableReport.getHeaderElement("Direct Sales",0).getSize().getWidth(),
                tableReport.getHeaderElement("Inside Sales",0).getSize().getWidth());

        int previousSize = tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth();
        tableReport.resizeColumn(METRIC_AMOUNT, 0, 30);
        assertTrue(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth() > previousSize, "Column should be resized");

        tableReport.revertSizeColumn(METRIC_NUMBER_OF_OPPORTUNITIES, 0, Keys.COMMAND);
        assertEquals(tableReport.getHeaderElement(METRIC_AMOUNT,0).getSize().getWidth(), previousSize); //Sometimes width + 9

        tableReport.resizeColumn(METRIC_AMOUNT, 0, 50, Keys.ALT);
        indigoDashboardsPage.saveEditModeWithWidgets().exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));
        takeScreenshot(browser, dashboardTitle, getClass());
        assertThat(contents, hasItems("Amount # of Opportunities", "false $53,901,464.88 1,752", "true $26,504,860.08 2,184"));
    }
}
