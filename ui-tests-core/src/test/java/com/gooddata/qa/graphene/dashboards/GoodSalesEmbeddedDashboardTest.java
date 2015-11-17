package com.gooddata.qa.graphene.dashboards;

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

import static org.openqa.selenium.By.className;
import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboardWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.utils.frame.InFrameAction;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public class GoodSalesEmbeddedDashboardTest extends GoodSalesAbstractTest {

    private static final int OTHER_WIDGETS_TAB_INDEX = 3;
    private static final int HEADLINE_REPORT_TAB_INDEX = 2;
    private static final int CHART_REPORT_TAB_INDEX = 1;
    private static final int TABULAR_REPORT_TAB_INDEX = 0;
    private static final int NUMBER_OF_TABS = 4;

    private static final String ADDITIONAL_PROJECT_TITLE = "GoodSales-project-to-share-dashboard";
    private static final String EMBEDDED_DASHBOARD_NAME = "Embedded Dashboard";

    private static final long EXPECTED_EXPORT_DASHBOARD_SIZE = 1300000L;
    private static final long EXPECTED_EXPORT_CHART_SIZE = 28000L;

    @FindBy(css = "iframe.yui3-c-iframewidget-content")
    private EmbeddedDashboardWidget embeddedDashboardWidget;

    @FindBy(id = "root")
    private EmbeddedDashboardWidget embeddedDashboarWidgetWithoutIframe;

    private UiReportDefinition tabularReportDef;
    private List<String> attributeValues;
    private List<Float> metricValues;
    private UiReportDefinition chartReportDef;
    private UiReportDefinition headlineReportDef;
    private String headlineReportDescription;
    private String headlineReportValue;
    private String sharedDashboardUrl;

    private String embedUri;
    private String htmlEmbedCode;
    private String additionalProjectId = "";

    @BeforeClass
    public void setUpData() {
        projectTitle = "GoodSales-embedded-dashboard-test";
        tabularReportDef =
                new UiReportDefinition().withName("tabular_report").withWhats(new WhatItem("Amount", "Status"))
                        .withHows("Year (Created)");
        chartReportDef =
                new UiReportDefinition().withName("chart_report").withWhats("Amount").withHows("Year (Created)")
                        .withType(ReportTypes.BAR);
        headlineReportDef =
                new UiReportDefinition().withName("headline_report").withWhats("Amount")
                        .withType(ReportTypes.HEADLINE);
    }

    @Test(dependsOnMethods = "createProject")
    public void prepareReportsForEmbeddedDashboard() {
        createReport(tabularReportDef, tabularReportDef.getName());
        TableReport tabularReport = reportPage.getTableReport();
        attributeValues = tabularReport.getAttributeElements();
        metricValues = tabularReport.getMetricElements();
        createReport(chartReportDef, chartReportDef.getName());
        createReport(headlineReportDef, headlineReportDef.getName());
        OneNumberReport headlineReport = reportPage.getHeadlineReport();
        headlineReportDescription = headlineReport.getDescription();
        headlineReportValue = headlineReport.getValue();
    }

    @Test(dependsOnMethods = "prepareReportsForEmbeddedDashboard")
    public void createDashboardToShare() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Embedded Dashboard");
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(tabularReportDef.getName());
        dashboardsPage.addNewTab("chart_report");
        dashboardEditBar.addReportToDashboard(chartReportDef.getName());
        dashboardsPage.addNewTab("headline_report");
        dashboardEditBar.addReportToDashboard(headlineReportDef.getName());
        dashboardsPage.addNewTab("other_widgets");
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.addWebContentToDashboard("https://www.gooddata.com");
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, "Amount");
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART, "Amount");
        dashboardEditBar.saveDashboard();
        sharedDashboardUrl = browser.getCurrentUrl();
        DashboardEmbedDialog dashboardEmbedDialog = dashboardsPage.embedDashboard();
        Sleeper.sleepTightInSeconds(3);
        embedUri = dashboardEmbedDialog.getPreviewURI();
        htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();
    }

    @Test(dependsOnMethods = "createDashboardToShare")
    public void createAdditionalProject() throws JSONException {
        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());
        additionalProjectId =
                gpProject.createProject(ADDITIONAL_PROJECT_TITLE, ADDITIONAL_PROJECT_TITLE, GOODSALES_TEMPLATE,
                        testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations);
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void shareDashboardWithIframe() {
        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embedded Dashboard");
        assertEquals(embeddedDashboardWidget.getNumberOfTabsInFrame(), NUMBER_OF_TABS,
                "Incorrect tab number on embedded dashboard!");

        embeddedDashboardWidget.openTabInFrame(TABULAR_REPORT_TAB_INDEX);
        assertTabularReportInFrame(tabularReportDef.getName(), attributeValues, metricValues);

        embeddedDashboardWidget.openTabInFrame(CHART_REPORT_TAB_INDEX).exportChartReportInFrame(
                chartReportDef.getName());
        verifyReportExport(ExportFormat.PDF, chartReportDef.getName(), EXPECTED_EXPORT_CHART_SIZE);

        embeddedDashboardWidget.openTabInFrame(HEADLINE_REPORT_TAB_INDEX);
        assertHeadlineReportInFrame(headlineReportDef.getName(), headlineReportDescription, headlineReportValue);

        String exportedDashboardName =
                embeddedDashboardWidget.downloadEmbeddedDashboardInFrame(OTHER_WIDGETS_TAB_INDEX);
        verifyDashboardExport(exportedDashboardName.replace(" ", "_"), EXPECTED_EXPORT_DASHBOARD_SIZE);
        embeddedDashboardWidget.checkRedBarInFrame();
    }

    @Test(dependsOnMethods = "createDashboardToShare")
    public void shareDashboardWithoutIframe() {
        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), NUMBER_OF_TABS,
                "Incorrect tab number on embedded dashboard!");

        embeddedDashboarWidgetWithoutIframe.openTab(TABULAR_REPORT_TAB_INDEX);
        assertTabularReport(tabularReportDef.getName(), attributeValues, metricValues);

        embeddedDashboarWidgetWithoutIframe.openTab(CHART_REPORT_TAB_INDEX).exportChartReport(
                chartReportDef.getName());
        verifyReportExport(ExportFormat.PDF, chartReportDef.getName(), EXPECTED_EXPORT_CHART_SIZE);

        embeddedDashboarWidgetWithoutIframe.openTab(HEADLINE_REPORT_TAB_INDEX);
        assertHeadlineReport(headlineReportDef.getName(), headlineReportDescription, headlineReportValue);

        String exportedDashboardName =
                embeddedDashboarWidgetWithoutIframe.downloadEmbeddedDashboard(OTHER_WIDGETS_TAB_INDEX);
        verifyDashboardExport(exportedDashboardName.replace(" ", "_"), EXPECTED_EXPORT_DASHBOARD_SIZE);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void configReportTitleVisibilityWithIframe() {
        String dashboardName = "Config Report Title On Embedded Dashboard";
        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, dashboardName);

        final TableReport report = embeddedDashboardWidget.openTabInFrame(TABULAR_REPORT_TAB_INDEX)
            .editDashboardInFrame()
            .getReportInFrame(tabularReportDef.getName(), TableReport.class);

        InFrameAction.Utils.doActionInFrame(embeddedDashboardWidget.getRoot(), (InFrameAction<?>) (() -> {
            takeScreenshot(browser, "configReportTitleVisibilityWithIframe - report title is visible", getClass());
            assertTrue(report.isReportTitleVisible());

            WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
            configPanel.getTab(Tab.STYLE, StyleConfigPanel.class).setTitleHidden();
            sleepTightInSeconds(2);
            configPanel.saveConfiguration();
            takeScreenshot(browser, "configReportTitleVisibilityWithIframe - report title is invisible",
                    getClass());

            WebElement saveButton = waitForElementVisible(className("s-btn-save"), browser);
            saveButton.click();
            waitForElementNotVisible(saveButton);

            return 0;
        }), browser);

        final TableReport report2 = embeddedDashboardWidget.
                getReportInFrame(tabularReportDef.getName(), TableReport.class);

        InFrameAction.Utils.doActionInFrame(embeddedDashboardWidget.getRoot(), (InFrameAction<?>) (() -> {
            assertFalse(report2.isReportTitleVisible());
            return 0;
        }), browser);
    }

    @Test(dependsOnMethods = "createDashboardToShare")
    public void configReportTitleVisibilityWithoutIframe() {
        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();

        TableReport report = embeddedDashboarWidgetWithoutIframe.openTab(TABULAR_REPORT_TAB_INDEX)
            .editDashboard()
            .getReport(tabularReportDef.getName(), TableReport.class);
        takeScreenshot(browser, "configReportTitleVisibilityWithoutIframe - report title is visible", getClass());
        assertTrue(report.isReportTitleVisible());

        setReportTitleVisibility(report, true);

        try {
            report = embeddedDashboarWidgetWithoutIframe.getReport(tabularReportDef.getName(), TableReport.class);
            takeScreenshot(browser, "configReportTitleVisibilityWithoutIframe - report title is invisible",
                    getClass());
            assertFalse(report.isReportTitleVisible());
        } finally {
            setReportTitleVisibility(embeddedDashboarWidgetWithoutIframe.editDashboard()
                .getReport(tabularReportDef.getName(), TableReport.class), false);
        }
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void drillingOnEmbeddedDashboard() {
        String attributeValueToDrill = "2009";
        List<String> drilledDownReportAttributeValues =
                Lists.newArrayList("Q1/2009", "Q2/2009", "Q3/2009", "Q4/2009");
        List<Float> drilledDownReportMetricValues =
                Lists.newArrayList(1279125.6F, 1881130.8F, 2381755.0F, 3114457.0F, 8656468.0F);

        String metricValueToDrill = "$2,773,426.95";
        List<String> drilledInReportAttributeValues = Lists.newArrayList("Lost", "Open", "Won");
        List<Float> drilledInReportMetricValues =
                Lists.newArrayList(1980676.11F, 326592.22F, 466158.62F, 2773426.95F);

        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Drill On Embedded Dashboard");
        embeddedDashboardWidget.drillOnAttributeInFrame(tabularReportDef.getName(), attributeValueToDrill);

        assertTrue(
                CollectionUtils.isEqualCollection(drilledDownReportAttributeValues,
                        embeddedDashboardWidget.getDrilledReportAttributesInFrame()),
                "Incorrect drilled-down report attributes!");
        assertTrue(
                CollectionUtils.isEqualCollection(drilledDownReportMetricValues,
                        embeddedDashboardWidget.getDrilledReportMetricsInFrame()),
                "Incorrect drilled-down report metrics!");

        embeddedDashboardWidget.closeDrillDialogInFrame();

        embeddedDashboardWidget.drillOnMetricInFrame(tabularReportDef.getName(), metricValueToDrill);
        assertTrue(
                CollectionUtils.isEqualCollection(drilledInReportAttributeValues,
                        embeddedDashboardWidget.getDrilledReportAttributesInFrame()),
                "Incorrect drilled-in report attributes!");
        assertTrue(
                CollectionUtils.isEqualCollection(drilledInReportMetricValues,
                        embeddedDashboardWidget.getDrilledReportMetricsInFrame()),
                "Incorrect drilled-in report metrics!");
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void filterEmbeddedDashboardWithUrlParameter() {
        browser.get(sharedDashboardUrl);

        DashboardEmbedDialog embedDialog = dashboardsPage.embedDashboard();
        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);
        embedDialog.selectFilterAttribute("Year (Created)", filteredAttributeValues);
        String htmlEmbedCode = embedDialog.getEmbedCode();
        String embedUri = embedDialog.getPreviewURI();

        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId,
                "Embedded Dashboard With Url Parameter");
        assertTabularReportInFrame(tabularReportDef.getName(), Lists.newArrayList(filteredAttributeValues),
                filteredMetricValues);

        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        assertTabularReport(tabularReportDef.getName(), Lists.newArrayList(filteredAttributeValues),
                filteredMetricValues);
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embedOneTabDashboard() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("One Tab Dashboard");
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(tabularReportDef.getName());
        dashboardEditBar.saveDashboard();

        DashboardEmbedDialog dashboardEmbedDialog = dashboardsPage.embedDashboard();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();

        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");
        assertFalse(embeddedDashboardWidget.isTabBarVisibleInFrame(),
                "Tab bar still presents on embedded dashboard with iframe!");

        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        assertFalse(embeddedDashboarWidgetWithoutIframe.isTabBarVisible(),
                "Tab bar still presents on embedded dashboard without iframe!");

        embedDashboardToOtherProjectDashboard(
                htmlEmbedCode.replace("dashboard.html#", "dashboard.html#nochrome=true&"), additionalProjectId,
                "Embed One Tab Dashboard without Print & Edit button");
        assertFalse(embeddedDashboardWidget.isPrintButtonVisibleInFrame(), "Print button is still visible!");
        assertFalse(embeddedDashboardWidget.isEditButtonVisibleInFrame(), "Edit button is still visible!");
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embeddedDashboardWithFilter() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Dashboard with filter");
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(tabularReportDef.getName());
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Year (Created)");
        dashboardEditBar.saveDashboard();

        DashboardEmbedDialog dashboardEmbedDialog = dashboardsPage.embedDashboard();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();


        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);

        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");
        embeddedDashboardWidget.modifyFirstAttributeFilterInFrame(filteredAttributeValues);
        assertTabularReportInFrame(tabularReportDef.getName(), Lists.newArrayList(filteredAttributeValues),
                filteredMetricValues);

        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        embeddedDashboarWidgetWithoutIframe.modifyFirstAttributeFilter(filteredAttributeValues);
        assertTabularReport(tabularReportDef.getName(), Lists.newArrayList(filteredAttributeValues),
                filteredMetricValues);
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void viewEmbeddedDashboardWithNotAuthorizedAccount() throws ParseException, IOException, JSONException {
        try {
            signInAtGreyPages(testParams.getViewerUser(), testParams.getViewerPassword());
            browser.get(embedUri);
            waitForFragmentVisible(loginFragment);
            String notAuthorizedMessage = loginFragment.getNotAuthorizedMessage();
            System.out.println("Not authorized message: " + notAuthorizedMessage);
            assertTrue(notAuthorizedMessage.contains("Access denied"));
            assertTrue(notAuthorizedMessage
                    .contains("You are not authorized to access this area. Please contact your administrator."));
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void openReportOnEmbeddedDashboard() {
        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId,
                "Open report on embedded dashboard");
        embeddedDashboardWidget.openTabularReportInFrame(tabularReportDef.getName())
                .getReportPageOnEmbeddedDashboardInFrame();
        assertTrue(
                CollectionUtils.isEqualCollection(attributeValues,
                        embeddedDashboardWidget.getAttributesOnReportPageInFrame()),
                "Incorrect attribute values in embedded report with iframe!");
        assertTrue(
                CollectionUtils.isEqualCollection(metricValues,
                        embeddedDashboardWidget.getMetricsOnReportPageInFrame()),
                "Incorrect attribute values in embedded report with iframe!");

        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        embeddedDashboarWidgetWithoutIframe.openTabularReport(tabularReportDef.getName())
                .getReportPageOnEmbeddedDashboard();
        assertTrue(
                CollectionUtils.isEqualCollection(attributeValues,
                        embeddedDashboarWidgetWithoutIframe.getAttributesOnReportPage()),
                "Incorrect attribute values in embedded report without iframe!");
        assertTrue(
                CollectionUtils.isEqualCollection(metricValues,
                        embeddedDashboarWidgetWithoutIframe.getMetricsOnReportPage()),
                "Incorrect attribute values in embedded report without iframe!");

    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void createScheduleOfEmbeddedDashboard() throws JSONException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS);

        String defaultScheduleSubject = EMBEDDED_DASHBOARD_NAME + " Dashboard";

        embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Schedule on Embedded Dashboard");
        embeddedDashboardWidget.openTabInFrame(CHART_REPORT_TAB_INDEX).openDashboardScheduleDialogInFrame();
        assertEquals(embeddedDashboardWidget.getSelectedFrequencyInFrame(), "Daily",
                "Incorrect default frequency!");
        assertEquals(embeddedDashboardWidget.getSelectedTabToScheduleInFrame(), "Chart_report",
                "Incorrect default tab!");
        embeddedDashboardWidget.showCustomScheduleFormInFrame();
        assertEquals(embeddedDashboardWidget.getCustomScheduleSubjectInFrame(), defaultScheduleSubject
                + " - chart_report", "Incorrect default subject!");
        String customScheduleSubject1 = defaultScheduleSubject + System.currentTimeMillis();
        embeddedDashboardWidget.setCustomScheduleSubjectInFrame(customScheduleSubject1)
                .setCustomRecipientsInFrame(Lists.newArrayList(testParams.getViewerUser()))
                .saveDashboardScheduleInFrame();

        initEmailSchedulesPage();
        assertTrue(emailSchedulesPage.isPrivateSchedulePresent(customScheduleSubject1));
        WebElement schedule1 = emailSchedulesPage.getPrivateSchedule(customScheduleSubject1);
        assertEquals(emailSchedulesPage.getBccEmailsOfPrivateSchedule(schedule1), testParams.getViewerUser());

        browser.get(embedUri);
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe).waitForEmbeddedDashboardLoaded();
        embeddedDashboarWidgetWithoutIframe.openTab(HEADLINE_REPORT_TAB_INDEX).openDashboardScheduleDialog();
        assertEquals(embeddedDashboarWidgetWithoutIframe.getSelectedFrequency(), "Daily",
                "Incorrect default frequency!");
        assertEquals(embeddedDashboarWidgetWithoutIframe.getSelectedTabToSchedule(), "Headline_report",
                "Incorrect default tab!");
        embeddedDashboarWidgetWithoutIframe.showCustomScheduleForm();
        assertEquals(embeddedDashboarWidgetWithoutIframe.getCustomScheduleSubject(), defaultScheduleSubject
                + " - headline_report", "Incorrect default subject!");
        String customScheduleSubject2 = defaultScheduleSubject + System.currentTimeMillis();
        embeddedDashboarWidgetWithoutIframe.setCustomScheduleSubject(customScheduleSubject2)
                .setCustomRecipients(Lists.newArrayList(testParams.getViewerUser())).saveDashboardSchedule();

        initEmailSchedulesPage();
        assertTrue(emailSchedulesPage.isPrivateSchedulePresent(customScheduleSubject2));
        WebElement schedule2 = emailSchedulesPage.getPrivateSchedule(customScheduleSubject2);
        assertEquals(emailSchedulesPage.getBccEmailsOfPrivateSchedule(schedule2), testParams.getViewerUser());
    }

    @AfterClass
    public void cleanUp() throws JSONException {
        deleteProject(additionalProjectId);
    }

    private void assertHeadlineReportInFrame(String reportTitle, String expectedDescription, String expectedValue) {
        assertEquals(embeddedDashboardWidget.getHeadlineReportDescriptionInFrame(reportTitle),
                expectedDescription, "Incorrect headline description on embedded report!");
        assertEquals(embeddedDashboardWidget.getHeadlineReportValueInFrame(reportTitle), expectedValue,
                "Incorrect headline value on embedded report!");
    }

    private void assertHeadlineReport(String reportTitle, String expectedDescription, String expectedValue) {
        assertEquals(embeddedDashboarWidgetWithoutIframe.getHeadlineReportDescription(reportTitle),
                expectedDescription, "Incorrect headline description on embedded report!");
        assertEquals(embeddedDashboarWidgetWithoutIframe.getHeadlineReportValue(reportTitle), expectedValue,
                "Incorrect headline value on embedded report!");
    }

    private void assertTabularReportInFrame(String reportTitle, List<String> expectedAttributeValues,
            List<Float> expectedMetricValues) {
        assertTrue(
                CollectionUtils.isEqualCollection(expectedAttributeValues,
                        embeddedDashboardWidget.getTableReportAttributesInFrame(reportTitle)),
                "Incorrect attribute values in embedded report without iframe!");
        assertTrue(
                CollectionUtils.isEqualCollection(expectedMetricValues,
                        embeddedDashboardWidget.getTableReportMetricsInFrame(reportTitle)),
                "Incorrect metric values in embedded report without iframe!");
    }

    private void assertTabularReport(String reportTitle, List<String> expectedAttributeValues,
            List<Float> expectedMetricValues) {
        waitForFragmentVisible(embeddedDashboarWidgetWithoutIframe);
        assertTrue(
                CollectionUtils.isEqualCollection(expectedAttributeValues,
                        embeddedDashboarWidgetWithoutIframe.getTableReportAttributes(reportTitle)),
                "Incorrect attribute values in embedded report without iframe!");
        assertTrue(
                CollectionUtils.isEqualCollection(expectedMetricValues,
                        embeddedDashboarWidgetWithoutIframe.getTableReportMetrics(reportTitle)),
                "Incorrect metric values in embedded report without iframe!");
    }

    private void embedDashboardToOtherProjectDashboard(String embedCode, String projectToShare,
            String dashboardName) {
        initProjectsPage();
        projectsPage.goToProject(projectToShare);
        waitForFragmentVisible(dashboardsPage);
        dashboardsPage.addNewDashboard(dashboardName);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addWebContentToDashboard(embedCode);
        dashboardsPage.getContent().resizeWidgetTopLeft(-300, 0);
        dashboardsPage.getContent().resizeWidgetBottomRight(200, 500);
        dashboardEditBar.saveDashboard();
        Sleeper.sleepTightInSeconds(3);
        browser.navigate().refresh();
        waitForFragmentVisible(dashboardsPage);
        waitForFragmentVisible(embeddedDashboardWidget);
    }

    private void setReportTitleVisibility(TableReport report, boolean isHidden) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
        StyleConfigPanel stylePanel = configPanel.getTab(Tab.STYLE, StyleConfigPanel.class);
        if (isHidden) {
            stylePanel.setTitleHidden();
        } else {
            stylePanel.setTitleVisible();
        }
        sleepTightInSeconds(2);
        configPanel.saveConfiguration();
        embeddedDashboarWidgetWithoutIframe.getDashboardsPage()
            .getDashboardEditBar()
            .saveDashboard();
    }
}
