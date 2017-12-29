package com.gooddata.qa.graphene.reports;

import com.gooddata.GoodData;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportContainer;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.ReportEmbedDialog;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.google.common.collect.Lists;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.GOODSALES_TEMPLATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesEmbeddedReportTest extends GoodSalesAbstractTest {

    private static final long MINIMUM_EMBEDDED_REPORT_PDF_SIZE = 20000L;
    private static final long MINIMUM_EMBEDDED_REPORT_CSV_SIZE = 60L;
    private static final long MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE = 26000L;
    private static final long MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE = 100L;
    private final static String EMBEDDED_REPORT_TITLE = "Embedded Report";
    private final static String ADDITIONAL_PROJECT_TITLE = "GoodSales-project-to-share-report";

    private GoodData editorGoodDataClient;

    private String additionalProjectId = "";
    private String reportUrl;
    private String htmlEmbedCode;
    private String embedUri;
    private List<String> attributeValues;
    private List<Float> metricValues;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-embedded-report-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createLostMetric();
        getVariableCreator().createStatusVariable();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportToShare() {
        createReport(GridReportDefinitionContent.create(EMBEDDED_REPORT_TITLE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByIdentifier("attr.stage.status"))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
        initReportsPage().openReport(EMBEDDED_REPORT_TITLE);

        takeScreenshot(browser, "Report-To-Share" + "-" + EMBEDDED_REPORT_TITLE + "-" +
                METRIC_GROUP, this.getClass());
        reportPage.setReportVisible();
        reportUrl = browser.getCurrentUrl();

        TableReport tableReport = reportPage.getTableReport();
        attributeValues = tableReport.getAttributeValues();
        metricValues = tableReport.getMetricValues();

        System.out.println("attributeValues: " + attributeValues);
        System.out.println("metricValues: " + metricValues);
    }

    @Test(dependsOnMethods = {"createReportToShare"})
    public void editorGetEmbedCode() throws JSONException {
        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
        openReportByUrl(reportUrl);
        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        htmlEmbedCode = embedDialog.getHtmlCode();
        embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        System.out.println("htmlEmbedCode: " + htmlEmbedCode);
        System.out.println("embedUri: " + embedUri);
    }

    @Test(dependsOnMethods = {"editorGetEmbedCode"})
    public void createAdditionalProject() throws JSONException, InterruptedException {
        additionalProjectId = ProjectRestUtils.createProject(getEditorGoodDataClient(),
                ADDITIONAL_PROJECT_TITLE, GOODSALES_TEMPLATE, testParams.getAuthorizationToken(),
                testParams.getProjectDriver(), testParams.getProjectEnvironment());
    }

    @DataProvider(name = "embeddedReport")
    public Object[][] getEmbeddedReportProvider() {
        return new Object[][] {
            {true},
            {false}
        };
    }

    @Test(dependsOnMethods = {"createAdditionalProject"}, dataProvider = "embeddedReport")
    public void shareTableReportToOtherProjectDashboard(boolean withIframe) {
        EmbeddedReportContainer embeddedReportContainer = null;

        if (withIframe) {
            embeddedReportContainer =
                    embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Share table report");
        } else {
            embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        }

        assertEquals(embeddedReportContainer.getInfo(), EMBEDDED_REPORT_TITLE);

        TableReport tableReport = embeddedReportContainer.getTableReport();

        assertThat(tableReport.getAttributeHeaders(), is(newArrayList(ATTR_STATUS)));
        assertThat(tableReport.getAttributeValues(), is(attributeValues));

        assertThat(tableReport.getMetricHeaders(), is(newHashSet(METRIC_AMOUNT)));
        assertThat(tableReport.getMetricValues(), is(metricValues));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"}, dataProvider = "embeddedReport")
    public void viewEmbeddedReportOnReportPage(boolean withIframe) {
        EmbeddedReportContainer embeddedReportContainer = null;

        if (withIframe) {
            embeddedReportContainer = embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId,
                    "View embedded report on report page");
        } else {
            embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        }

        embeddedReportContainer.openReportInfoViewPanel().clickViewReportButton();

        switchToPopUpWindow(EMBEDDED_REPORT_TITLE);
        waitForFragmentVisible(reportPage);
        assertEquals(reportPage.getReportName(), EMBEDDED_REPORT_TITLE, "Incorrect report title!");

        TableReport tableReport = reportPage.getTableReport();
        assertThat(tableReport.getAttributeValues(), is(attributeValues));
        assertThat(tableReport.getMetricValues(), is(metricValues));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"}, dataProvider = "embeddedReport")
    public void downloadEmbeddedReport(boolean withIframe) {
        EmbeddedReportContainer embeddedReportContainer = null;

        if (withIframe) {
            embeddedReportContainer =
                    embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Download embedded report");
        } else {
            embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        }

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, EMBEDDED_REPORT_TITLE, MINIMUM_EMBEDDED_REPORT_CSV_SIZE);

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.PDF_LANDSCAPE);
        verifyReportExport(ExportFormat.PDF_LANDSCAPE, EMBEDDED_REPORT_TITLE, MINIMUM_EMBEDDED_REPORT_PDF_SIZE);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareHeadlineReport() {
        String headlineReportTitle = "Embedded Headline Report";
        UiReportDefinition headlineReportDef =
                new UiReportDefinition().withName(headlineReportTitle).withWhats("Amount")
                        .withType(ReportTypes.HEADLINE);
        createReport(headlineReportDef, "Headline-Report");
        OneNumberReport headlineReport = reportPage.getHeadlineReport();
        String headlineReportDescription = headlineReport.getDescription();
        String headlineReportNumber = headlineReport.getValue();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        // Check headline report with iframe
        OneNumberReport embeddedHeadlineReport = 
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, headlineReportTitle)
                .getHeadlineReport();

        assertEquals(embeddedHeadlineReport.getDescription(), headlineReportDescription);
        assertEquals(embeddedHeadlineReport.getValue(), headlineReportNumber);

        // Check headline report without iframe
        embeddedHeadlineReport = initEmbeddedReportWithUri(embedUri).getHeadlineReport();

        assertEquals(embeddedHeadlineReport.getDescription(), headlineReportDescription);
        assertEquals(embeddedHeadlineReport.getValue(), headlineReportNumber);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void updateReportAfterSharing() {
        String reportTitle = "Update embedded report";
        UiReportDefinition reportDefinition =
                new UiReportDefinition().withName(reportTitle).withHows("Stage Name").withWhats("Amount");
        createReport(reportDefinition, "Update-Report-After-Sharing");
        String reportUrl = browser.getCurrentUrl();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, reportTitle);

        openReportByUrl(reportUrl);
        String[] filteredValues =
                {"Interest", "Discovery", "Short List", "Negotiation", "Closed Won", "Closed Lost"};
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Stage Name", filteredValues));
        reportPage.saveReport();

        TableReport embeddedTableReport = initDashboardsPage(additionalProjectId)
                .getLastEmbeddedWidget()
                .getEmbeddedReportContainer()
                .getTableReport();
        assertThat(embeddedTableReport.getAttributeValues(), is(newArrayList(filteredValues)));

        embeddedTableReport = initEmbeddedReportWithUri(embedUri).getTableReport();
        assertThat(embeddedTableReport.getAttributeValues(), is(newArrayList(filteredValues)));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareUnsavedReport() {
        String reportTitle = "Embed unsaved report";
        List<String> attributeElement;
        List<Float> metricElement;

        initReportCreation().setReportName(reportTitle).openWhatPanel().selectItem(METRIC_AMOUNT);
        reportPage.openHowPanel().selectItem(ATTR_IS_ACTIVE).done();

        attributeElement = reportPage.getTableReport().getAttributeValues();
        metricElement = reportPage.getTableReport().getMetricValues();
        WebElement unsavedReportWarning = reportPage.embedUnsavedReport();
        assertEquals(unsavedReportWarning.getText(), "Please first save the report before embeding. Close");
        reportPage.closeEmbedUnsavedWarning();

        reportPage.embedUnsavedReport();
        reportPage.createReportFromUnsavedWarningEmbed();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        EmbeddedReportContainer embeddedReportContainer =
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, reportTitle + " to dashaboard");
        assertEquals(embeddedReportContainer.getInfo(), reportTitle);

        TableReport tableReport = embeddedReportContainer.getTableReport();
        assertThat(tableReport.getAttributeValues(), is(attributeElement));
        assertThat(tableReport.getMetricValues(), is(metricElement));

        embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        assertEquals(embeddedReportContainer.getInfo(), reportTitle);

        tableReport = embeddedReportContainer.getTableReport();
        assertThat(tableReport.getAttributeValues(), is(attributeElement));
        assertThat(tableReport.getMetricValues(), is(metricElement));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareEmptyReport() {
        String reportTitle = "Empty report";

        createReportViaRest(getEditorGoodDataClient(), GridReportDefinitionContent.create(reportTitle,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByIdentifier("attr.stage.status"))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_LOST)))));

        initReportsPage().openReport(reportTitle);
        takeScreenshot(browser, "Update-Report-After-Sharing" + "-" + reportTitle + "-" +
                METRIC_GROUP, this.getClass());

        reportPage.addFilter(FilterItem.Factory.createPromptFilter("Status"));
        reportPage.saveReport();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        EmbeddedReportContainer embeddedReportContainer =
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed empty report");
        assertTrue(embeddedReportContainer.getTableReport().hasNoData(), "Embedded Empty Report is not empty!");

        embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        assertTrue(embeddedReportContainer.getTableReport().hasNoData(), "Embedded Empty Report is not empty!");
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareChartReport() {
        String embeddedChartReport = "Embedded Chart Report";
        createReportViaRest(getEditorGoodDataClient(), GridReportDefinitionContent.create(embeddedChartReport,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByIdentifier("attr.stage.status"))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
        initReportsPage().openReport(embeddedChartReport);
        reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress().saveReport();

        takeScreenshot(browser, "Embedded-chart-report" + "-" + embeddedChartReport + "-" +
                METRIC_GROUP, this.getClass());

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        EmbeddedReportContainer embeddedReportContainer =
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embedded Chart Report on dashboard");
        checkRedBar(browser);

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, embeddedChartReport, MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE);

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.PDF);
        verifyReportExport(ExportFormat.PDF, embeddedChartReport, MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE);

        embeddedReportContainer = initEmbeddedReportWithUri(embedUri);

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, embeddedChartReport, MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE);

        embeddedReportContainer.openReportInfoViewPanel().downloadReportAsFormat(ExportFormat.PDF);
        verifyReportExport(ExportFormat.PDF, embeddedChartReport, MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void filterEmbeddedReportWithUrlParameter() {
        String reportTitle = "Embedded report with url parameter";
        UiReportDefinition reportDefinition =
                new UiReportDefinition().withName(reportTitle).withHows("Stage Name").withWhats("Amount");
        createReport(reportDefinition, "Update-Report-After-Sharing");

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String[] filteredAttributeValues = {"Interest", "Closed Won"};
        embedDialog.selectFilterAttribute("Stage Name", filteredAttributeValues);
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();

        EmbeddedReportContainer embeddedReportContainer =
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Share report with url parameter");
        assertThat(embeddedReportContainer.getTableReport().getAttributeValues(),
                is(newArrayList(filteredAttributeValues)));

        embeddedReportContainer = initEmbeddedReportWithUri(embedUri);
        assertThat(embeddedReportContainer.getTableReport().getAttributeValues(),
                is(newArrayList(filteredAttributeValues)));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void drillEmbeddedReport() {
        UiReportDefinition reportDefinition =
                new UiReportDefinition().withName("Drill embeded report").withHows("Year (Created)")
                        .withWhats(new WhatItem("Amount", "Status"));
        createReport(reportDefinition, "Drill-embedded-report");

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        String attributeValueToDrill = "2009";
        List<String> drilledDownReportAttributeValues =
                Lists.newArrayList("Q1/2009", "Q2/2009", "Q3/2009", "Q4/2009");
        List<Float> drilledDownReportMetricValues =
                Lists.newArrayList(1279125.6F, 1881130.8F, 2381755.0F, 3114457.0F);

        String metricValueToDrill = "$2,773,426.95";
        List<String> drilledInReportAttributeValues = Lists.newArrayList("Lost", "Open", "Won");
        List<Float> drilledInReportMetricValues =
                Lists.newArrayList(1980676.1F, 326592.22F, 466158.62F);

        EmbeddedReportContainer embeddedReportContainer =
                embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Drill embedded report with iframe");

        TableReport tableReport = embeddedReportContainer.getTableReport()
                .drillOn(attributeValueToDrill, CellType.ATTRIBUTE_VALUE);

        assertThat(tableReport.getAttributeValues(), is(drilledDownReportAttributeValues));
        assertThat(tableReport.getMetricValues(), is(drilledDownReportMetricValues));

        tableReport = refreshDashboardPage()
                .getLastEmbeddedWidget()
                .getEmbeddedReportContainer()
                .getTableReport()
                .drillOn(metricValueToDrill, CellType.METRIC_VALUE);

        assertThat(tableReport.getAttributeValues(), is(drilledInReportAttributeValues));
        assertThat(tableReport.getMetricValues(), is(drilledInReportMetricValues));

        embeddedReportContainer = initEmbeddedReportWithUri(embedUri);

        tableReport = embeddedReportContainer.getTableReport().drillOn(attributeValueToDrill, CellType.ATTRIBUTE_VALUE);

        assertThat(tableReport.getAttributeValues(), is(drilledDownReportAttributeValues));
        assertThat(tableReport.getMetricValues(), is(drilledDownReportMetricValues));

        browser.navigate().refresh();
        tableReport = waitForFragmentVisible(embeddedReportContainer)
                .getTableReport()
                .drillOn(metricValueToDrill, CellType.METRIC_VALUE);

        assertThat(tableReport.getAttributeValues(), is(drilledInReportAttributeValues));
        assertThat(tableReport.getMetricValues(), is(drilledInReportMetricValues));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws JSONException {
        ProjectRestUtils.deleteProject(getEditorGoodDataClient(), additionalProjectId);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void openReportByUrl(String reportUrl) {
        browser.get(reportUrl);
        waitForFragmentVisible(reportPage).initPage();
    }

    private void switchToPopUpWindow(String reportTitle) {
        for (String window : browser.getWindowHandles()) {
            String windowTitle = browser.switchTo().window(window).getTitle();
            if (windowTitle.contains(reportTitle))
                return;
        }
    }

    private EmbeddedReportContainer embedReportToOtherProjectDashboard(String embedCode,
            String projectToShare, String dashboardName) {
        EmbeddedWidget embeddedWidget = initDashboardsPage(projectToShare)
                .addNewDashboard(dashboardName)
                .addWebContentToDashboard(embedCode)
                .getLastEmbeddedWidget()
                .resizeFromTopLeftButton(-300, 0)
                .resizeFromBottomRightButton(200, 600);

        dashboardsPage.saveDashboard();
        return embeddedWidget.getEmbeddedReportContainer();
    }

    private EmbeddedReportContainer initEmbeddedReportWithUri(String embedUri) {
        browser.get(embedUri);

        //the previous page is using the same widget so should sleep in two seconds before getting widget
        //otherwise, it could get the old widget
        sleepTightInSeconds(2);

        return Graphene.createPageFragment(EmbeddedReportContainer.class,
                waitForElementVisible(EmbeddedReportContainer.LOCATOR, browser));
    }

    private DashboardsPage initDashboardsPage(String projectId) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + DASHBOARD_PAGE_SUFFIX);
        waitForDashboardPageLoaded(browser);

        return dashboardsPage;
    }

    private DashboardsPage refreshDashboardPage() {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);

        return dashboardsPage;
    }

    private GoodData getEditorGoodDataClient() {
        if (isNull(editorGoodDataClient)) {
            editorGoodDataClient = getGoodDataClient(testParams.getEditorUser(), testParams.getPassword());
        }

        return editorGoodDataClient;
    }
}
