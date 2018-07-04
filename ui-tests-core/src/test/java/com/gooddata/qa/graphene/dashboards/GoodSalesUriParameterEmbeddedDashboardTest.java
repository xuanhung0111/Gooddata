package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

public class GoodSalesUriParameterEmbeddedDashboardTest extends AbstractEmbeddedModeTest {

    private static final String REPORT_AMOUNT_BY_YEAR_SNAP_SHOT = "report amount by year snapshot";
    private static final String FIRST_TAB = "First Tab";
    private static final String INTEREST = "Interest";

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.CONTROL_EXECUTION_CONTEXT_ENABLED, true);
        getMetricCreator().createAmountMetric();
        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_YEAR_SNAP_SHOT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_STAGE_NAME,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
    }

    @Test(dependsOnGroups = "createProject")
    public void applyMacroToEmbeddedDashboard() throws IOException {
        String dashboard = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        initDashboardsPage().selectDashboard(dashboard)
                .addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(attr.stage.name)%")
                .saveDashboard();
        Screenshots.takeScreenshot(browser, "apply_macro", getClass());
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, INTEREST);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "apply_macro", getClass());
        assertEquals(embeddedDashboard.getFirstFilter().getCurrentValue(), INTEREST);
        assertEquals(embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class).waitForLoaded()
                .getAttributeValues(), singletonList(INTEREST));
        assertEquals(embeddedDashboard.getContent().getLastTextWidgetValue(), INTEREST);
    }

    @Test(dependsOnGroups = "createProject")
    public void notApplyDisconnectFilterToEmbeddedDashboard() throws IOException {
        String dashboard = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        initDashboardsPage().selectDashboard(dashboard);
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_ACTIVITY, "Email with 1000Bulbs.com on Apr-21-08");

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "not_apply_disconnect_filter", getClass());
        assertEquals(embeddedDashboard.getFirstFilter().getCurrentValue(), "All");
        assertEquals(embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class).waitForLoaded()
                .getAttributeValues(), asList(INTEREST, "Discovery", "Short List", "Risk Assessment", "Conviction",
                "Negotiation", "Closed Won", "Closed Lost"));
    }

    @Test(dependsOnGroups = "createProject")
    public void preserveParentChildFilter() throws IOException {
        String dashboard = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_YEAR_SNAP_SHOT,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)), RIGHT));
        initDashboardsPage().selectDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_QUARTER_YEAR_SNAPSHOT,
                        DashboardWidgetDirection.DOWN)
                .setParentsForFilter(ATTR_YEAR_SNAPSHOT, ATTR_QUARTER_YEAR_SNAPSHOT);
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.saveDashboard().openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_QUARTER_YEAR_SNAPSHOT, "Q2/2010");

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "preserve_parent_child_filter", getClass());
        assertEquals(embeddedDashboard.getReport(REPORT_AMOUNT_BY_YEAR_SNAP_SHOT, TableReport.class)
                .waitForLoaded().getAttributeValues(), singletonList("2010"));
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_YEAR_SNAPSHOT).openPanel().getAllAttributeValues(),
                singletonList("2010"));
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_QUARTER_YEAR_SNAPSHOT).getCurrentValue(), "Q2/2010");
    }

    @Test(dependsOnGroups = "createProject")
    public void exportEmbeddedDashboard() throws IOException {
        final String discovery = "Discovery";
        String dashboard = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        initDashboardsPage().selectDashboard(dashboard);
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, INTEREST);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class).waitForLoaded();

        if (testParams.isClientDemoEnvironment()) {
            log.info("Client-demo does not support dashboard export");
            return;
        }
        embeddedDashboard.printDashboardTab(0);
        verifyTabExport(FIRST_TAB, INTEREST);

        embeddedDashboard.getFirstFilter().changeAttributeFilterValues(discovery);
        Screenshots.takeScreenshot(browser, "export_dashboard", getClass());
        embeddedDashboard.printDashboardTab(0);
        verifyTabExport(FIRST_TAB, discovery);
    }

    @Test(dependsOnGroups = "createProject")
    public void applySavedViewToEmbeddedDashboard() throws IOException {
        String dashboard = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        initDashboardsPage().selectDashboard(dashboard).turnSavedViewOption(true).saveDashboard();
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, INTEREST);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        Screenshots.takeScreenshot(browser, "apply_saved_view", getClass());
        assertEquals(embeddedDashboard.getFirstFilter().getCurrentValue(), INTEREST);
        assertEquals(embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class).waitForLoaded()
                .getAttributeValues(), singletonList(INTEREST));
        assertEquals(embeddedDashboard.getSavedViewWidget().getCurrentSavedView(), "* Unsaved View");
    }

    private String createDashboardWithReportAndFilter(String report, Pair<FilterItemContent, TabItem.ItemPosition> filters)
            throws IOException {
        String dashboardName = "dashboard " + generateHashString();
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(dashboardName);
            dash.addTab(initTab(FIRST_TAB, report, singletonList(filters)));
            dash.addFilter(filters.getLeft());
        }).build();

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId()).createDashboard(dashboard.getMdObject());
        return dashboardName;
    }

    private Tab initTab(String name, String report, List<Pair<FilterItemContent, TabItem.ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream().map(pair -> Builder.of(FilterItem::new)
                .with(item -> item.setContentId(pair.getLeft().getId()))
                .with(item -> item.setPosition(pair.getRight())).build()).collect(Collectors.toList());

        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));

        return initDashboardTab(name,
                Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream).collect(Collectors.toList()));
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }

    private void verifyTabExport(String tabName, String expectedValue) throws IOException {
        try {
            verifyDashboardExport(tabName, tabName, 3000L);
            assertThat(getContentFrom(tabName), containsString(expectedValue));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() + tabName + ".pdf"));
        }
    }
}
