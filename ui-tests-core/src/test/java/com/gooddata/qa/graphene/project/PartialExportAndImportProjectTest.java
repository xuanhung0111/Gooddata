package com.gooddata.qa.graphene.project;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.UrlParserUtils.getObjdUri;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class PartialExportAndImportProjectTest extends AbstractProjectTest {

    private final static String PROJECT_TEMPLATE = "/projectTemplates/OnboardingWalkMe/3";

    private final static String TARGET_PROJECT_TITLE = "Target-Project";
    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String SIMPLE_METRIC = "Simple-Metric";
    private final static String NEW_DASHBOARD = "New-Dashboard";
    private final static String CONSUMER_INSIGHTS = "Consumer Insights";
    private static final String SIMPLE_FILTERED_VARIABLE = "Simple-filtered-variable";
    private static final String REGION = "Region";
    private static final String SOUTH = "South";
    private static final String WEST = "West";
    private static final String MIDWEST = "Midwest";
    private static final String NORTHEAST = "Northeast";
    private static final String SALES = "Sales";
    private static final String SPEND = "Spend";
    private static final String BAR_CHART_REPORT = "Bar Chart: Email Open Rate by Month";

    private String sourceProjectId;
    private String targetProjectId;

    @BeforeClass
    public void initProjectTemplate() {
        projectTemplate = PROJECT_TEMPLATE;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void setUpProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = ProjectRestUtils.createProject(getGoodDataClient(),
                TARGET_PROJECT_TITLE, PROJECT_TEMPLATE, testParams.getAuthorizationToken(),
                testParams.getProjectDriver(), testParams.getProjectEnvironment());
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void partialExportAndImportReport() throws JSONException {
        final String reportUri = createSimpleReport();
        final String exportToken = exportPartialProject(reportUri, DEFAULT_PROJECT_CHECK_LIMIT);

        try {
            testParams.setProjectId(targetProjectId);
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            initReportsPage().openReport(SIMPLE_REPORT);
            //use List.equals due to checking attribute & metric order
            assertTrue(reportPage.getTableReport().getAttributeElements().equals(asList(MIDWEST, NORTHEAST, SOUTH, WEST)),
                    "There is difference between actual and expected attributes");
            assertTrue(reportPage.getTableReport().getMetricElements().equals(asList(3.8f, 2.8f, 7.3f, 3.7f)),
                    "There is difference between actual and expected metric elelemts");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"partialExportAndImportReport"})
    public void partialExportAndImportChartReport() throws JSONException {
        initReportsPage().openReport(SIMPLE_REPORT);
        reportPage.waitForReportExecutionProgress();
        final String simpleReportUri = getObjdUri(browser.getCurrentUrl());
        reportPage.selectReportVisualisation(ReportTypes.BAR).saveReport();

        final String exportToken = exportPartialProject(simpleReportUri, DEFAULT_PROJECT_CHECK_LIMIT);

        try {
            testParams.setProjectId(targetProjectId);
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            initReportsPage().openReport(SIMPLE_REPORT);
            reportPage.waitForReportExecutionProgress();
            assertTrue(isElementVisible(id("chartContainer"), browser), "The bar chart is not displayed");
            takeScreenshot(browser, "Simple-report-in-bar-chart-type", getClass());
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void partialExportAndImportDashboard() throws JSONException {
        initDashboardsPage().selectDashboard(CONSUMER_INSIGHTS);
        final String existingDashobardUri = getObjdUri(browser.getCurrentUrl());

        dashboardsPage.addNewDashboard(NEW_DASHBOARD).editDashboard().addReportToDashboard(BAR_CHART_REPORT).saveDashboard();
        takeScreenshot(browser, "HL-Sales-report-on-dashboard", getClass());

        dashboardsPage.selectDashboard(NEW_DASHBOARD);
        final String newDashobardUri = getObjdUri(browser.getCurrentUrl());
        final String exportToken = exportPartialProject(existingDashobardUri + "\n" + newDashobardUri,
                DEFAULT_PROJECT_CHECK_LIMIT);

        try {
            testParams.setProjectId(targetProjectId);
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            initDashboardsPage().selectDashboard(CONSUMER_INSIGHTS);

            //use List.equals due to checking tab order
            assertTrue(asList("Executive Overview", "Marketing Contribution", "Sales Forecast", "Create Insights")
                    .equals(dashboardsPage.getTabs().getAllTabNames()), "The imported dashboard is incorrect ");

            dashboardsPage.selectDashboard(NEW_DASHBOARD);
            assertTrue(dashboardsPage.getContent().getReport(BAR_CHART_REPORT, OneNumberReport.class).getRoot().isDisplayed(),
                    "The imported dashboard is not correct");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void partialExportAndImportMetric() throws JSONException {
        final String simpleMetricUri = createSimpleMetric().getUri();
        final String spendMetricUri = getMdService().getObjUri(getProject(), Metric.class, title(SPEND));

        final String exportToken = exportPartialProject(simpleMetricUri + "\n" + spendMetricUri,
                DEFAULT_PROJECT_CHECK_LIMIT);

        try {
            testParams.setProjectId(targetProjectId);
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            assertTrue(initMetricPage().openMetricDetailPage(SPEND).getMAQL()
                    .equals("SELECT SUM(ifnull(Spend,0))"), "The imported metric is not correct");

            assertTrue(initMetricPage().openMetricDetailPage(SIMPLE_METRIC).getMAQL()
                    .equals("SELECT SUM(Sales)"), "The imported metric is not correct");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void partialExportAndImportVariable() throws JSONException {
        final AttributeVariable simpleVariable = new AttributeVariable(SIMPLE_FILTERED_VARIABLE)
                .withAttribute(REGION)
                .withAttributeValues(asList(SOUTH, WEST));

        final String simpleVariableUri = initVariablePage().createVariable(simpleVariable);
        final String exportToken = exportPartialProject(simpleVariableUri, DEFAULT_PROJECT_CHECK_LIMIT);

        try {
            testParams.setProjectId(targetProjectId);
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

            assertTrue(initVariablePage().hasVariable(SIMPLE_FILTERED_VARIABLE),
                    "There has an issue after import partial project");
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @AfterClass
    public void tearDown() {
        ProjectRestUtils.deleteProject(getGoodDataClient(), sourceProjectId);
        testParams.setProjectId(targetProjectId);
    }

    private String createSimpleReport() {
        final String salesMetricUri = getMdService().getObjUri(getProject(), Metric.class, title(SALES));
        final Attribute region = getMdService().getObj(getProject(), Attribute.class, title(REGION));

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT,
                singletonList("metricGroup"),
                singletonList(new AttributeInGrid(region.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(salesMetricUri, SALES)));

        definition = getMdService().createObj(getProject(), definition);
        return getMdService().createObj(getProject(), new Report(definition.getTitle(), definition)).getUri();
    }

    private Metric createSimpleMetric() {
        final String salesFactUri = getMdService().getObjUri(getProject(), Fact.class, title(SALES));
        return getMdService().createObj(getProject(), new Metric(SIMPLE_METRIC, MetricTypes.SUM.getMaql()
                .replaceFirst("__fact__", format("[%s]", salesFactUri)), "#,##0.00"));
    }

}
