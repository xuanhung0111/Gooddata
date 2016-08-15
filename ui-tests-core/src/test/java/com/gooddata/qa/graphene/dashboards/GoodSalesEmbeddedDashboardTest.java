package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.GOODSALES_TEMPLATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;
import static java.nio.file.Files.deleteIfExists;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.google.common.collect.Lists;

public class GoodSalesEmbeddedDashboardTest extends GoodSalesAbstractTest {

    private static final int OTHER_WIDGETS_TAB_INDEX = 3;
    private static final int HEADLINE_REPORT_TAB_INDEX = 2;
    private static final int CHART_REPORT_TAB_INDEX = 1;
    private static final int TABULAR_REPORT_TAB_INDEX = 0;
    private static final int NUMBER_OF_TABS = 4;

    private static final String ADDITIONAL_PROJECT_TITLE = "GoodSales-project-to-share-dashboard";
    private static final String EMBEDDED_DASHBOARD_NAME = "Embedded Dashboard";

    private static final long EXPECTED_EXPORT_DASHBOARD_SIZE = 1200000L;
    private static final long EXPECTED_EXPORT_CHART_SIZE = 28000L;

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
                new UiReportDefinition().withName("tabular_report").withWhats(new WhatItem(METRIC_AMOUNT, ATTR_STATUS))
                        .withHows(ATTR_YEAR_CREATED);
        chartReportDef =
                new UiReportDefinition().withName("chart_report").withWhats(METRIC_AMOUNT).withHows(ATTR_YEAR_CREATED)
                        .withType(ReportTypes.BAR);
        headlineReportDef =
                new UiReportDefinition().withName("headline_report").withWhats(METRIC_AMOUNT)
                        .withType(ReportTypes.HEADLINE);
    }

    @Test(dependsOnGroups = "createProject")
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
        dashboardsPage.addNewDashboard("Embedded Dashboard")
                .addReportToDashboard(tabularReportDef.getName())
                .addNewTab("chart_report")
                .addReportToDashboard(chartReportDef.getName())
                .addNewTab("headline_report")
                .addReportToDashboard(headlineReportDef.getName())
                .addNewTab("other_widgets")
                .addLineToDashboard()
                .addWebContentToDashboard("https://www.gooddata.com")
                .addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_AMOUNT)
                .addWidgetToDashboard(WidgetTypes.GEO_CHART, METRIC_AMOUNT)
                .saveDashboard();

        sharedDashboardUrl = browser.getCurrentUrl();

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();

        embedUri = embedDashboardDialog.getPreviewURI();
        htmlEmbedCode = embedDashboardDialog.getEmbedCode();
    }

    @Test(dependsOnMethods = "createDashboardToShare")
    public void createAdditionalProject() throws JSONException {
        additionalProjectId = ProjectRestUtils
                .createProject(getGoodDataClient(), ADDITIONAL_PROJECT_TITLE, GOODSALES_TEMPLATE,
                        testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
    }

    @DataProvider(name = "embeddedDashboard")
    public Object[][] getEmbeddedDashboardProvider() {
        return new Object[][] {
            {true},
            {false}
        };
    }

    @Test(dependsOnMethods = "createAdditionalProject", dataProvider = "embeddedDashboard")
    public void embedDashboard(boolean withIframe) throws IOException {
        EmbeddedDashboard embeddedDashboard = null;

        if (withIframe) {
            embeddedDashboard =
                    embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embedded Dashboard");
        } else {
            embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);
        }

        assertEquals(embeddedDashboard.getTabs().getNumberOfTabs(), NUMBER_OF_TABS,
                "Incorrect tab number on embedded dashboard!");

        TableReport tableReport = embeddedDashboard.openTab(TABULAR_REPORT_TAB_INDEX)
                .getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeElements(), is(attributeValues));
        assertThat(tableReport.getMetricElements(), is(metricValues));

        embeddedDashboard
                .openTab(CHART_REPORT_TAB_INDEX)
                .getReport(chartReportDef.getName(), ChartReport.class)
                .openReportInfoViewPanel()
                .downloadReportAsFormat(ExportFormat.PDF);

        try {
            verifyReportExport(ExportFormat.PDF, chartReportDef.getName(), EXPECTED_EXPORT_CHART_SIZE);

        } finally {
            deleteIfExists(Paths.get(getExportFilePath(chartReportDef.getName(), ExportFormat.PDF)));
        }

        OneNumberReport oneNumberReport =  embeddedDashboard.openTab(HEADLINE_REPORT_TAB_INDEX)
                .getReport(headlineReportDef.getName(), OneNumberReport.class);

        assertEquals(oneNumberReport.getDescription(), headlineReportDescription);
        assertEquals(oneNumberReport.getValue(), headlineReportValue);

        String exportedDashboardName = embeddedDashboard.printDashboardTab(OTHER_WIDGETS_TAB_INDEX);

        try {
            verifyDashboardExport(exportedDashboardName, "other_widgets", EXPECTED_EXPORT_DASHBOARD_SIZE);

        } finally {
            deleteIfExists(Paths.get(getExportFilePath(exportedDashboardName, ExportFormat.PDF)));
        }

        checkRedBar(browser);
    }

    @Test(dependsOnMethods = "createAdditionalProject", dataProvider = "embeddedDashboard")
    public void configReportTitleVisibility(boolean withIframe) {
        String dashboardName = "Config Report Title On Embedded Dashboard";
        EmbeddedDashboard embeddedDashboard = null;

        if (withIframe) {
            embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, dashboardName);
        } else {
            embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);
        }

        TableReport tableReport = embeddedDashboard
                .openTab(TABULAR_REPORT_TAB_INDEX)
                .getReport(tabularReportDef.getName(), TableReport.class);

        takeScreenshot(browser,
                "embedded-dashboard-with-iframe-mode-: " + valueOf(withIframe) + "-report-title-is-visible", getClass());
        assertTrue(tableReport.isReportTitleVisible(), "Report title is not visible");

        embeddedDashboard.editDashboard();

        setReportTitleVisibility(tableReport, false);

        embeddedDashboard.saveDashboard();

        try {
            tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

            takeScreenshot(browser,
                    "embedded-dashboard-with-iframe-mode-: " + valueOf(withIframe) + "-report-title-is-invisible",
                    getClass());
            assertFalse(tableReport.isReportTitleVisible(), "Report title is visible");

        } finally {
            embeddedDashboard.editDashboard();

            setReportTitleVisibility(tableReport, true);

            embeddedDashboard.saveDashboard();
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

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Drill On Embedded Dashboard");

        embeddedDashboard
                .getReport(tabularReportDef.getName(), TableReport.class)
                .drillOnAttributeValue(attributeValueToDrill);

        DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        TableReport drilledTableReport = drillDialog.getReport(TableReport.class);

        assertThat(drilledTableReport.getAttributeElements(), is(drilledDownReportAttributeValues));
        assertThat(drilledTableReport.getMetricElements(), is(drilledDownReportMetricValues));

        drillDialog.closeDialog();

        embeddedDashboard
                .getReport(tabularReportDef.getName(), TableReport.class)
                .drillOnMetricValue(metricValueToDrill);

        drillDialog = Graphene
                .createPageFragment(DashboardDrillDialog.class, waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        drilledTableReport = drillDialog.getReport(TableReport.class);

        assertThat(drilledTableReport.getAttributeElements(), is(drilledInReportAttributeValues));
        assertThat(drilledTableReport.getMetricElements(), is(drilledInReportMetricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void filterEmbeddedDashboardWithUrlParameter() {
        browser.get(sharedDashboardUrl);

        EmbedDashboardDialog embedDialog = dashboardsPage.openEmbedDashboardDialog();
        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);
        embedDialog.selectFilterAttribute(ATTR_YEAR_CREATED, filteredAttributeValues);
        String htmlEmbedCode = embedDialog.getEmbedCode();
        String embedUri = embedDialog.getPreviewURI();

        EmbeddedDashboard embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode,
                additionalProjectId, "Embedded Dashboard With Url Parameter");

        TableReport tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeElements(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricElements(), is(filteredMetricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeElements(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricElements(), is(filteredMetricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embedOneTabDashboard() {
        initDashboardsPage()
                .addNewDashboard("One Tab Dashboard")
                .addReportToDashboard(tabularReportDef.getName())
                .saveDashboard();

        EmbedDashboardDialog dashboardEmbedDialog = dashboardsPage.openEmbedDashboardDialog();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");

        assertFalse(embeddedDashboard.isTabBarVisible(), "Tab bar still presents on embedded dashboard with iframe!");

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        assertFalse(embeddedDashboard.isTabBarVisible(), "Tab bar still presents on embedded dashboard without iframe!");

        embeddedDashboard = embedDashboardToOtherProjectDashboard(
                htmlEmbedCode.replace("dashboard.html#", "dashboard.html#nochrome=true&"), additionalProjectId,
                "Embed One Tab Dashboard without Print & Edit button");

        assertFalse(embeddedDashboard.isPrintButtonVisible(), "Print button is still visible!");
        assertFalse(embeddedDashboard.isEditButtonVisible(), "Edit button is still visible!");
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embeddedDashboardWithFilter() {
        initDashboardsPage()
                .addNewDashboard("Dashboard with filter")
                .addReportToDashboard(tabularReportDef.getName())
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_CREATED)
                .saveDashboard();

        EmbedDashboardDialog dashboardEmbedDialog = dashboardsPage.openEmbedDashboardDialog();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();

        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");

        embeddedDashboard.getFirstFilter().changeAttributeFilterValue(filteredAttributeValues);

        TableReport tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeElements(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricElements(), is(filteredMetricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        embeddedDashboard.getFirstFilter().changeAttributeFilterValue(filteredAttributeValues);
        tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeElements(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricElements(), is(filteredMetricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void viewEmbeddedDashboardWithNotAuthorizedAccount() throws ParseException, IOException, JSONException {
        try {
            signInAtGreyPages(testParams.getViewerUser(), testParams.getViewerPassword());
            browser.get(embedUri);
            String notAuthorizedMessage = LoginFragment.getInstance(browser).getNotAuthorizedMessage();
            log.info("Not authorized message: " + notAuthorizedMessage);
            assertTrue(notAuthorizedMessage.contains("ACCESS DENIED"));
            assertTrue(notAuthorizedMessage
                    .contains("You are not authorized to access this area. Please contact your administrator."));
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void openReportOnEmbeddedDashboard() {
        EmbeddedDashboard embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode,
                additionalProjectId, "Open report on embedded dashboard");

        embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class)
                .openReportInfoViewPanel()
                .clickViewReportButton();

        TableReport tableReport = Graphene.createPageFragment(ReportPage.class,
                waitForElementVisible(ReportPage.LOCATOR, browser))
                .getTableReport();

        assertThat(tableReport.getAttributeElements(), is(attributeValues));
        assertThat(tableReport.getMetricElements(), is(metricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class)
                .openReportInfoViewPanel()
                .clickViewReportButton();

        tableReport = Graphene.createPageFragment(ReportPage.class, waitForElementVisible(ReportPage.LOCATOR, browser))
                .getTableReport();

        assertThat(tableReport.getAttributeElements(), is(attributeValues));
        assertThat(tableReport.getMetricElements(), is(metricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void createScheduleOfEmbeddedDashboard() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, true);

        String defaultScheduleSubject = EMBEDDED_DASHBOARD_NAME + " Dashboard";

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Schedule on Embedded Dashboard");

        DashboardScheduleDialog scheduleDialog = embeddedDashboard.openTab(CHART_REPORT_TAB_INDEX)
                .showDashboardScheduleDialog();

        assertEquals(scheduleDialog.getSelectedFrequency(), "Daily", "Incorrect default frequency!");
        assertEquals(scheduleDialog.getSelectedTab(), "Chart_report", "Incorrect default tab!");

        String scheduleSubject = scheduleDialog.showCustomForm().getCustomEmailSubject();
        assertEquals(scheduleSubject, defaultScheduleSubject + " - chart_report", "Incorrect default subject!");

        String customScheduleSubject = defaultScheduleSubject + System.currentTimeMillis();

        scheduleDialog.setCustomEmailSubject(customScheduleSubject)
                .setCustomRecipients(Lists.newArrayList(testParams.getViewerUser()))
                .schedule();

        assertTrue(initEmailSchedulesPage().isPrivateSchedulePresent(customScheduleSubject));
        WebElement schedule1 = EmailSchedulePage.getInstance(browser).getPrivateSchedule(customScheduleSubject);
        assertEquals(EmailSchedulePage.getInstance(browser).getBccEmailsOfPrivateSchedule(schedule1), testParams.getViewerUser());

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        scheduleDialog = embeddedDashboard.openTab(HEADLINE_REPORT_TAB_INDEX)
                .showDashboardScheduleDialog();

        assertEquals(scheduleDialog.getSelectedFrequency(), "Daily", "Incorrect default frequency!");
        assertEquals(scheduleDialog.getSelectedTab(), "Headline_report", "Incorrect default tab!");

        scheduleSubject = scheduleDialog.showCustomForm().getCustomEmailSubject();
        assertEquals(scheduleSubject, defaultScheduleSubject + " - headline_report", "Incorrect default subject!");

        customScheduleSubject = defaultScheduleSubject + System.currentTimeMillis();

        scheduleDialog.setCustomEmailSubject(customScheduleSubject)
                .setCustomRecipients(Lists.newArrayList(testParams.getViewerUser()))
                .schedule();

        assertTrue(initEmailSchedulesPage().isPrivateSchedulePresent(customScheduleSubject));
        WebElement schedule2 = EmailSchedulePage.getInstance(browser).getPrivateSchedule(customScheduleSubject);
        assertEquals(EmailSchedulePage.getInstance(browser).getBccEmailsOfPrivateSchedule(schedule2), testParams.getViewerUser());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws JSONException {
        ProjectRestUtils.deleteProject(getGoodDataClient(), additionalProjectId);
    }

    private EmbeddedDashboard embedDashboardToOtherProjectDashboard(String embedCode, String projectToShare,
            String dashboardName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectToShare + DASHBOARD_PAGE_SUFFIX);
        waitForDashboardPageLoaded(browser);

        EmbeddedWidget embeddedWidget = dashboardsPage
                .addNewDashboard(dashboardName)
                .addWebContentToDashboard(embedCode)
                .getLastEmbeddedWidget()
                .resizeFromTopLeftButton(-300, 0)
                .resizeFromBottomRightButton(200, 600);

        dashboardsPage.saveDashboard();
        return embeddedWidget.getEmbeddedDashboard();
    }

    private EmbeddedDashboard initEmbeddedDashboardWithUri(String uri) {
        browser.get(uri);
        return Graphene.createPageFragment(EmbeddedDashboard.class, waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
    }

    private void setReportTitleVisibility(TableReport report, boolean visible) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
        StyleConfigPanel stylePanel = configPanel.getTab(Tab.STYLE, StyleConfigPanel.class);

        if (visible) {
            stylePanel.setTitleVisible();

        } else {
            stylePanel.setTitleHidden();
        }

        configPanel.saveConfiguration();
    }

    private String getExportFilePath(String name, ExportFormat format) {
        return testParams.getDownloadFolder() + testParams.getFolderSeparator() + name + "." + format.getName();
    }
}
