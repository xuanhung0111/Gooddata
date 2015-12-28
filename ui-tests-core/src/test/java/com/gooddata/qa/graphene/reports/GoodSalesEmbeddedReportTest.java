package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportWidget;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.ReportEmbedDialog;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.collect.Lists;

public class GoodSalesEmbeddedReportTest extends GoodSalesAbstractTest {

    private static final long MINIMUM_EMBEDDED_REPORT_PDF_SIZE = 20000L;
    private static final long MINIMUM_EMBEDDED_REPORT_CSV_SIZE = 60L;
    private static final long MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE = 26000L;
    private static final long MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE = 100L;
    private final static String EMBEDDED_REPORT_TITLE = "Embedded Report";
    private final static String ADDITIONAL_PROJECT_TITLE = "GoodSales-project-to-share-report";
    private final static String ATTRIBUTE_NAME = "Status";
    private final static String METRIC_NAME = "Amount";

    private String additionalProjectId = "";
    private String reportUrl;
    private String htmlEmbedCode;
    private String embedUri;
    private List<String> attributeValues;
    private List<Float> metricValues;

    @FindBy(xpath = "//iframe[contains(@class, 'yui3-c-iframewidget')]")
    private EmbeddedReportWidget embeddedReportWidget;

    @FindBy(id = "content")
    private EmbeddedReportWidget embeddedReportWithoutIframe;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-embedded-report-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createReportToShare() {
        createReport(new UiReportDefinition().withName(EMBEDDED_REPORT_TITLE)
                .withHows(ATTRIBUTE_NAME).withWhats(METRIC_NAME), "Report-To-Share");
        reportPage.setReportVisible();
        reportUrl = browser.getCurrentUrl();

        TableReport tableReport = reportPage.getTableReport();
        attributeValues = tableReport.getAttributeElements();
        metricValues = tableReport.getMetricElements();

        System.out.println("attributeValues: " + attributeValues);
        System.out.println("metricValues: " + metricValues);
    }

    @Test(dependsOnMethods = "createReportToShare")
    public void addEditorAndSignInAsEditor() throws ParseException, IOException, JSONException {
        addEditorUserToProject();
        signInAtGreyPages(testParams.getEditorUser(), testParams.getEditorPassword());
    }

    @Test(dependsOnMethods = "addEditorAndSignInAsEditor")
    public void editorGetEmbedCode() {
        openReport(reportUrl);
        reportPage.getTableReport().waitForReportLoading();
        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        htmlEmbedCode = embedDialog.getHtmlCode();
        embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        System.out.println("htmlEmbedCode: " + htmlEmbedCode);
        System.out.println("embedUri: " + embedUri);
    }

    @Test(dependsOnMethods = {"editorGetEmbedCode"})
    public void createAdditionalProject() throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());
        additionalProjectId =
                gpProject.createProject(ADDITIONAL_PROJECT_TITLE, ADDITIONAL_PROJECT_TITLE, GOODSALES_TEMPLATE,
                        testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareTableReportToOtherProjectDashboard() {
        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Share table report");

        assertEquals(embeddedReportWidget.getEmbeddedReportTitleInframe(), EMBEDDED_REPORT_TITLE);
        assertAttributeHeadersInFrame(Lists.newArrayList(ATTRIBUTE_NAME));
        assertAttributeValuesInframe(attributeValues);
        assertMetricHeadersInFrame(Lists.newArrayList(METRIC_NAME));
        assertMetricValuesInframe(metricValues);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void viewEmbeddedReportOnReportPage() {
        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId,
                "View embedded report on report page");
        embeddedReportWidget.viewEmbeddedReportInFrame();
        switchToPopUpWindow(EMBEDDED_REPORT_TITLE);
        waitForFragmentVisible(reportPage);
        reportPage.getTableReport().waitForReportLoading();
        assertEquals(reportPage.getReportName(), EMBEDDED_REPORT_TITLE, "Incorrect report title!");
        assertAttributeValuesOnReportPage(attributeValues);
        assertMetricValuesOnReportPage(metricValues);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void downloadEmbeddedReport() {
        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Download embedded report");

        embeddedReportWidget.downloadEmbeddedReportInFrame(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, EMBEDDED_REPORT_TITLE, MINIMUM_EMBEDDED_REPORT_CSV_SIZE);
        embeddedReportWidget.downloadEmbeddedReportInFrame(ExportFormat.PDF_LANDSCAPE);
        verifyReportExport(ExportFormat.PDF_LANDSCAPE, EMBEDDED_REPORT_TITLE, MINIMUM_EMBEDDED_REPORT_PDF_SIZE);
    }

    @Test(dependsOnMethods = {"editorGetEmbedCode"})
    public void shareReportWithoutIframe() {
        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        assertEquals(embeddedReportWithoutIframe.getReportTitle(), EMBEDDED_REPORT_TITLE);
        assertAttributeHeaders(Lists.newArrayList(ATTRIBUTE_NAME));
        assertAttributeValues(attributeValues);
        assertMetricHeaders(Lists.newArrayList(METRIC_NAME));
        assertMetricValues(metricValues);
    }

    @Test(dependsOnMethods = {"editorGetEmbedCode"})
    public void viewEmbeddedReportWithoutIframeOnReportPage() {
        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        embeddedReportWithoutIframe.viewEmbeddedReport();
        switchToPopUpWindow(EMBEDDED_REPORT_TITLE);
        waitForFragmentVisible(reportPage);
        reportPage.getTableReport().waitForReportLoading();
        assertEquals(reportPage.getReportName(), EMBEDDED_REPORT_TITLE, "Incorrect report title!");
        assertAttributeValuesOnReportPage(attributeValues);
        assertMetricValuesOnReportPage(metricValues);
    }

    @Test(dependsOnMethods = {"editorGetEmbedCode"})
    public void downloadEmbeddedReportWithoutIframe() {
        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);

        embeddedReportWithoutIframe.downloadEmbeddedReport(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, EMBEDDED_REPORT_TITLE, MINIMUM_EMBEDDED_REPORT_CSV_SIZE);
        embeddedReportWithoutIframe.downloadEmbeddedReport(ExportFormat.PDF_LANDSCAPE);
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
        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, headlineReportTitle);
        assertEquals(embeddedReportWidget.getHeadlineDescriptionInFrame(), headlineReportDescription,
                "Incorrect embedded headline report description!");
        assertEquals(embeddedReportWidget.getHeadlineValueInFrame(), headlineReportNumber,
                "Incorrect headline report number!");

        // Check headline report without iframe
        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        assertEquals(embeddedReportWithoutIframe.getHeadlineDescription(), headlineReportDescription,
                "Incorrect embedded headline report description!");
        assertEquals(embeddedReportWithoutIframe.getHeadlineValue(), headlineReportNumber,
                "Incorrect headline report number!");
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
        String dashboardUrl = browser.getCurrentUrl();

        openReport(reportUrl);
        String[] filteredValues =
                {"Interest", "Discovery", "Short List", "Negotiation", "Closed Won", "Closed Lost"};
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter("Stage Name", filteredValues));
        reportPage.saveReport();

        browser.get(dashboardUrl);
        waitForFragmentVisible(dashboardsPage);
        waitForFragmentVisible(embeddedReportWidget);
        assertAttributeValuesInframe(Lists.newArrayList(filteredValues));

        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        assertAttributeValues(Lists.newArrayList(filteredValues));
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareUnsavedReport() {
        String reportTitle = "Embed unsaved report";

        initReportCreation();
        reportPage.initPage()
            .setReportName(reportTitle)
            .openWhatPanel()
            .selectMetric(METRIC_NAME)
            .openHowPanel()
            .selectAttribute(ATTRIBUTE_NAME)
            .doneSndPanel();

        WebElement unsavedReportWarning = reportPage.embedUnsavedReport();
        assertEquals(unsavedReportWarning.getText(), "Please first save the report before embeding. Close");
        reportPage.closeEmbedUnsavedWarning();

        reportPage.embedUnsavedReport();
        reportPage.createReportFromUnsavedWarningEmbed();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, reportTitle + " to dashaboard");
        assertEquals(embeddedReportWidget.getEmbeddedReportTitleInframe(), reportTitle);
        assertAttributeValuesInframe(attributeValues);
        assertMetricValuesInframe(metricValues);

        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        assertEquals(embeddedReportWithoutIframe.getReportTitle(), reportTitle);
        assertAttributeValues(attributeValues);
        assertMetricValues(metricValues);
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareEmptyReport() {
        String reportTitle = "Empty report";
        UiReportDefinition updateEmbeddedReport =
                new UiReportDefinition().withName(reportTitle).withHows("Status").withWhats("Lost");
        createReport(updateEmbeddedReport, "Update-Report-After-Sharing");
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("Status"));
        reportPage.saveReport();

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed empty report");
        assertTrue(waitForFragmentVisible(embeddedReportWidget).isEmptyReportInFrame(),
                "Embedded Empty Report is not empty!");

        browser.get(embedUri);
        assertTrue(waitForFragmentVisible(embeddedReportWithoutIframe).isEmptyReport(),
                "Embedded Empty Report is not empty!");
    }

    @Test(dependsOnMethods = {"createAdditionalProject"})
    public void shareChartReport() {
        UiReportDefinition reportDefinition =
                new UiReportDefinition().withName("Embedded Chart Report").withHows("Status").withWhats("Amount")
                        .withType(ReportTypes.BAR);
        createReport(reportDefinition, "Embedded-chart-report");

        ReportEmbedDialog embedDialog = reportPage.openReportEmbedDialog();
        String htmlEmbedCode = embedDialog.getHtmlCode();
        String embedUri = embedDialog.getEmbedUri();
        embedDialog.closeEmbedDialog();

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId,
                "Embedded Chart Report on dashboard");
        checkRedBar(browser);
        embeddedReportWidget.downloadEmbeddedReportInFrame(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, reportDefinition.getName(), MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE);
        embeddedReportWidget.downloadEmbeddedReportInFrame(ExportFormat.PDF);
        verifyReportExport(ExportFormat.PDF, reportDefinition.getName(), MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE);

        browser.get(embedUri);
        embeddedReportWithoutIframe.downloadEmbeddedReport(ExportFormat.CSV);
        verifyReportExport(ExportFormat.CSV, reportDefinition.getName(), MINIMUM_EMBEDDED_CHART_REPORT_CSV_SIZE);
        embeddedReportWithoutIframe.downloadEmbeddedReport(ExportFormat.PDF);
        verifyReportExport(ExportFormat.PDF, reportDefinition.getName(), MINIMUM_EMBEDDED_CHART_REPORT_PDF_SIZE);
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

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Share report with url parameter");
        assertAttributeValuesInframe(Lists.newArrayList(filteredAttributeValues));

        browser.get(embedUri);
        waitForFragmentVisible(embeddedReportWithoutIframe);
        assertAttributeValues(Lists.newArrayList(filteredAttributeValues));
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
                Lists.newArrayList(1279125.6F, 1881130.8F, 2381755.0F, 3114457.0F, 8656468.0F);

        String metricValueToDrill = "$2,773,426.95";
        List<String> drilledInReportAttributeValues = Lists.newArrayList("Lost", "Open", "Won");
        List<Float> drilledInReportMetricValues =
                Lists.newArrayList(1980676.1F, 326592.22F, 466158.62F, 2773427.0F);

        embedReportToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Drill embedded report with iframe");
        embeddedReportWidget.drillAttributeValueInFrame(attributeValueToDrill);
        System.out.println("Drill attributes: " + embeddedReportWidget.getAttributeElementsInFrame());
        System.out.println("Drill metric: " + embeddedReportWidget.getMetricElementsInFrame());
        assertAttributeValuesInframe(drilledDownReportAttributeValues);
        assertMetricValuesInframe(drilledDownReportMetricValues);

        browser.navigate().refresh();
        waitForFragmentVisible(dashboardsPage);

        embeddedReportWidget.drillMetricValueInFrame(metricValueToDrill);
        System.out.println("Drill attributes: " + embeddedReportWidget.getAttributeElementsInFrame());
        System.out.println("Drill metric: " + embeddedReportWidget.getMetricElementsInFrame());
        assertAttributeValuesInframe(drilledInReportAttributeValues);
        assertMetricValuesInframe(drilledInReportMetricValues);

        browser.get(embedUri);
        embeddedReportWithoutIframe.drillAttributeValue(attributeValueToDrill);
        assertAttributeValues(drilledDownReportAttributeValues);
        assertMetricValues(drilledDownReportMetricValues);

        browser.navigate().refresh();
        embeddedReportWithoutIframe.drillMetricValue(metricValueToDrill);
        assertAttributeValues(drilledInReportAttributeValues);
        assertMetricValues(drilledInReportMetricValues);
    }

    @AfterClass
    public void cleanUp() throws JSONException {
        deleteProject(additionalProjectId);
        signInAtGreyPages(testParams.getUser(), testParams.getPassword());
    }

    private void openReport(String reportUrl) {
        browser.get(reportUrl);
        waitForFragmentVisible(reportPage);
    }

    private void switchToPopUpWindow(String reportTitle) {
        for (String window : browser.getWindowHandles()) {
            String windowTitle = browser.switchTo().window(window).getTitle();
            if (windowTitle.contains(reportTitle))
                return;
        }
    }

    private void embedReportToOtherProjectDashboard(String embedCode, String projectToShare, String dashboardName) {
        initProjectsPage();
        projectsPage.goToProject(projectToShare);
        waitForFragmentVisible(dashboardsPage);
        dashboardsPage.addNewDashboard(dashboardName);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addWebContentToDashboard(embedCode);
        dashboardEditBar.saveDashboard();
        Sleeper.sleepTightInSeconds(3);
        browser.navigate().refresh();
        waitForFragmentVisible(dashboardsPage);
        waitForFragmentVisible(embeddedReportWidget);
    }

    private void assertAttributeHeadersInFrame(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWidget.getAttributeHeadersInFrame()),
                "Incorrect attribute values in embedded report without iframe!");
    }

    private void assertAttributeHeaders(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWithoutIframe.getAttributeHeaders()),
                "Incorrect attribute values in embedded report without iframe!");
    }

    private void assertMetricHeadersInFrame(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWidget.getMetricHeadersInFrame()),
                "Incorrect attribute values in embedded report without iframe!");
    }

    private void assertMetricHeaders(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWithoutIframe.getMetricHeaders()),
                "Incorrect attribute values in embedded report without iframe!");
    }

    private void assertAttributeValues(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWithoutIframe.getAttributeElements()),
                "Incorrect attribute values in embedded report without iframe!");
    }

    private void assertAttributeValuesInframe(List<String> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(expectedValues,
                        embeddedReportWidget.getAttributeElementsInFrame()),
                "Incorrect attribute values in embedded report with iframe!");
    }

    private void assertMetricValues(List<Float> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(Lists.newArrayList(expectedValues),
                        embeddedReportWithoutIframe.getMetricElements()),
                "Incorrect metric values in embedded report without iframe!");
    }

    private void assertMetricValuesInframe(List<Float> expectedValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(expectedValues, embeddedReportWidget.getMetricElementsInFrame()),
                "Incorrect metric values in embedded report with iframe!");
    }

    private void assertAttributeValuesOnReportPage(List<String> expectedValues) {
        assertTrue(CollectionUtils.isEqualCollection(expectedValues, reportPage.getTableReport()
                .getAttributeElements()));
    }

    private void assertMetricValuesOnReportPage(List<Float> expectedValues) {
        assertTrue(CollectionUtils.isEqualCollection(expectedValues, reportPage.getTableReport()
                .getMetricElements()));
    }
}
