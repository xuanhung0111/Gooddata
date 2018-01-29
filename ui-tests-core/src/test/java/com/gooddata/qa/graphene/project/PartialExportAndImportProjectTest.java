package com.gooddata.qa.graphene.project;

import com.gooddata.fixture.ResourceManagement.*;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_5_LOST_BY_CASH;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_5_OPEN_BY_CASH;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PartialExportAndImportProjectTest extends GoodSalesAbstractTest {

    private final String FIRST_TAB = "First Tab";
    private final String SECOND_TAB = "Second Tab";

    private String sourceProjectId;
    private String targetProjectId;

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createProjectUsingFixture("Target-Project", ResourceTemplate.GOODSALES);
    }

    @Test(dependsOnMethods = {"createAnotherProject"})
    public void partialExportAndImportReport() throws JSONException {
        String exportToken = exportPartialProject(getReportCreator().createActivitiesByTypeReport(), DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            TableReport report = initReportsPage().openReport(REPORT_ACTIVITIES_BY_TYPE).getTableReport();
            assertEquals(report.getAttributeValues(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"),
                    "There is difference between actual and expected attributes");
            assertEquals(report.getRawMetricValues(), asList("33,920", "35,975", "50,780", "33,596"),
                    "There is difference between actual and expected metric elements");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"partialExportAndImportReport"})
    public void partialExportAndImportChartReport() throws JSONException {
        // using report will be changed from table to bar chart
        String top5OpenByCashReportUri = getReportCreator().createTop5OpenByCashReport();
        initReportsPage().openReport(REPORT_TOP_5_OPEN_BY_CASH)
                .selectReportVisualisation(ReportTypes.BAR)
                .waitForReportExecutionProgress()
                .saveReport();

        String exportToken = exportPartialProject(top5OpenByCashReportUri, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initReportsPage().openReport(REPORT_TOP_5_OPEN_BY_CASH);
            takeScreenshot(browser, "Report-in-bar-chart-type", getClass());
            assertTrue(isElementVisible(className("yui3-c-chart"), browser), "The bar chart is not rendered");
            assertEquals(reportPage.getSelectedChartType(), ReportTypes.BAR.getName(), "Displayed chart is not Bar type");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"createAnotherProject"})
    public void partialExportAndImportDashboard() throws JSONException, IOException {
        String dashboard1 = "Dashboard contains Top 5 Won";
        String dashboard2 = "Dashboard contains Top 5 Lost";

        List<String> dashUris = new ArrayList<>();
        dashUris.add(DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(),
                initDashboard(dashboard1, getReportCreator().createTop5WonByCashReport()).getMdObject()));
        dashUris.add(DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(),
                initDashboard(dashboard2, getReportCreator().createTop5LostByCashReport()).getMdObject()));

        String exportToken = exportPartialProject(
                dashUris.stream().collect(Collectors.joining("\n")), DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initDashboardsPage().selectDashboard(dashboard1);
            //use List.equals due to checking tab order
            assertTrue(asList(FIRST_TAB, SECOND_TAB).equals(dashboardsPage.getTabs().getAllTabNames()),
                    "The imported dashboard is not correct ");

            dashboardsPage.selectDashboard(dashboard2);
            assertTrue(dashboardsPage.getContent()
                            .getReport(REPORT_TOP_5_LOST_BY_CASH, TableReport.class)
                            .getAttributeValues()
                            .equals(asList("Boston Health Economics > CompuSci", "Hinshaw & Culbertson > CompuSci",
                                    "Mediatavern > Explorer", "R2integrated > PhoenixSoft", "TechSource > CompuSci")),
                    "The imported dashboard is not correct");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"createAnotherProject"})
    public void partialExportAndImportMetric() throws JSONException {
        String amountMetricUri = getMetricCreator().createAvgAmountMetric().getUri();
        String numberOfActUri = getMetricCreator().createNumberOfActivitiesMetric().getUri();
        String exportToken = exportPartialProject(
                amountMetricUri + "\n" + numberOfActUri, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_AMOUNT).getMAQL(),
                    "SELECT SUM(Amount) where Opp. Snapshot (Date)= _Snapshot [EOP]", "The imported metric is not correct");
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES).getMAQL(),
                    "SELECT COUNT(Activity)", "The imported metric is not correct");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"createAnotherProject"})
    public void partialExportAndImportVariable() throws JSONException {
        String exportToken = exportPartialProject(getVariableCreator().createStatusVariable(), DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            assertTrue(initVariablePage().hasVariable(VARIABLE_STATUS), "Variable is not imported");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    private Dashboard initDashboard(String name, String reportUri) {
        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(name);
            dashboard.addTab(Builder.of(Tab::new)
                    .with(tab -> {
                        tab.setTitle(FIRST_TAB);
                        tab.addItem(Builder.of(ReportItem::new)
                                .with(reportItem -> {
                                    reportItem.setObjUri(reportUri);
                                    reportItem.setPosition(TabItem.ItemPosition.LEFT);
                                }).build());
                    }).build());

            dashboard.addTab(Builder.of(Tab::new).with(tab -> tab.setTitle(SECOND_TAB)).build()); // empty tab
        }).build();
    }

    @AfterClass
    public void tearDown() {
        ProjectRestUtils.deleteProject(getGoodDataClient(), targetProjectId);
        testParams.setProjectId(sourceProjectId);
    }
}
