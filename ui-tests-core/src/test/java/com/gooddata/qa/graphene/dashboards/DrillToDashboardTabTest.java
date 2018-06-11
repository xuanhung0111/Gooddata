package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAndContinueDialog;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.*;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DrillToDashboardTabTest extends GoodSalesAbstractTest {

    private final String DASHBOARD_HAVING_SAME_FILTER_MODE = "Dashboard having same filter mode";
    private final String DASHBOARD_HAVING_DIFF_FILTER_MODE = "Dashboard having different filter mode";
    private final String DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB = "Dashboard having no filter on source tab";
    private final String DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB = "Dashboard having no filter on target tab";
    private final String DASHBOARD_FOR_DRILL_FURTHER_TEST = "Report for drill further test";
    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";
    private final String FIRST_TAB = "First Tab";
    private final String SECOND_TAB = "Second Tab";
    private final String TAB_ANOTHER_DASHBOARD = "Tab Another Dashboard";

    private final String DASHBOARD_DRILLING_GROUP = "Dashboards";
    private final String REPORTS_DRILLING_GROUP = "Reports";
    private final String DISCOVERY = "Discovery";
    private final String CLOSED_WON = "Closed Won";
    private final String WEST_14 = "14 West";
    private final String BULBS_1000 = "1000Bulbs.com";
    private final String COMPUSCI = "CompuSci";
    private final String EXPLORER = "Explorer";
    private final String ALL = "All";

    private final List<String> attributeValuesOfAmountReports =
            asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");
    private final List<String> attributeValuesOfSalesReports =
            asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private final List<String> attributeValuesOfReportHavingFilter = asList("Excalibur Technology > CompuSci",
            "LasX Industries > CompuSci", "Premier Integrity Solutions > CompuSci", "Turner Industries > CompuSci",
            "VideoLink > Explorer");

    private FilterItemContent stageNameAsSingleFilter;
    private FilterItemContent accountAsMultipleFilter;
    private FilterItemContent stageNameAsMultipleFilter;
    private FilterItemContent accountAsSingleFilter;
    private FilterItemContent productAsSingleFilter;

    private String amountByProductReportUri;
    private String salesSeasonalityReportUri;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        amountByProductReportUri = getReportCreator().createAmountByProductReport();
        salesSeasonalityReportUri = getReportCreator().createSalesSeasonalityReport();
        getReportCreator().createTop5OpenByCashReport();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());

        //init Dashboard Filter Objects
        stageNameAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        accountAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_ACCOUNT));
        stageNameAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        accountAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_ACCOUNT));
        productAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_PRODUCT));
    }

    @DataProvider
    public Object[][] drillToDashboardData() {
        List<Pair<String, List<String>>> changeValuesInSameFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> expectedValuesInSameFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> changeValuesInDiffFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> expectedValuesInDiffFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, singletonList(BULBS_1000)),
                Pair.of(ATTR_STAGE_NAME, singletonList(ALL)),
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)));

        List<Pair<String, List<String>>> changeValuesInNoFilterInSourceDash = new ArrayList<>();

        List<Pair<String, List<String>>> expectedValuesInNoFilterInSourceDash = singletonList(
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)));

        List<Pair<String, List<String>>> changeValuesInNoFilterInTargetDash = asList(
                Pair.of(ATTR_STAGE_NAME, singletonList(DISCOVERY)),
                Pair.of(ATTR_PRODUCT, singletonList(EXPLORER)));

        List<Pair<String, List<String>>> expectedValuesInNoFilterInTargetDash = new ArrayList<>();

        return new Object[][]{
                {DASHBOARD_HAVING_SAME_FILTER_MODE, changeValuesInSameFilterModeDash,
                        expectedValuesInSameFilterModeDash, 1},
                {DASHBOARD_HAVING_DIFF_FILTER_MODE, changeValuesInDiffFilterModeDash,
                        expectedValuesInDiffFilterModeDash, 0},
                {DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB, changeValuesInNoFilterInSourceDash,
                        expectedValuesInNoFilterInSourceDash, 1},
                {DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB, changeValuesInNoFilterInTargetDash,
                        expectedValuesInNoFilterInTargetDash, 6}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "drillToDashboardData")
    public void testDrillToDashboardTab(String dashboard, List<Pair<String, List<String>>> filterValuesToChange,
                                        List<Pair<String, List<String>>> expectedFilterValues,
                                        int expectedReportSize) throws IOException, JSONException {

        String dashboardUri = dashboardRequest.createDashboard(getDashboardByName(dashboard).getMdObject());

        try {
            initDashboardsPage().selectDashboard(dashboard);

            dashboardsPage.getTabs().getTab(TARGET_TAB).open();
            int reportRowCountBeforeDrilling = dashboardsPage.getContent()
                    .getLatestReport(TableReport.class)
                    .getAttributeValues()
                    .size();

            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport reportOnSourceTab = dashboardsPage.getContent().getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);

            reportOnSourceTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), TARGET_TAB), "Dashboards");
            dashboardsPage.saveDashboard();


            if (!filterValuesToChange.isEmpty()) {
                for (Pair<String, List<String>> entry : filterValuesToChange) {
                    changeFilterValues(entry.getLeft(), entry.getRight());
                }
            }

            // att to drill to is CompuSci
            reportOnSourceTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE).waitForLoaded();

            final Function<WebDriver, Boolean> isTargetTabLoaded = browser -> dashboardsPage.getTabs()
                    .getTab(TARGET_TAB).isSelected();
            Graphene.waitGui().until(isTargetTabLoaded);

            TableReport reportOnTargetTab = dashboardsPage.getContent()
                    .getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class).waitForLoaded();
            Screenshots.takeScreenshot(browser, "testDrillToDashboardTab-" + dashboard, getClass());
            if (!expectedFilterValues.isEmpty()) {
                for (Pair<String, List<String>> expectedFilterValue : expectedFilterValues) {
                    checkSelectedFilterValues(expectedFilterValue.getLeft(), expectedFilterValue.getRight());
                }
            }

            assertEquals(reportOnTargetTab.getAttributeValues().size(), expectedReportSize);

            BrowserUtils.refreshCurrentPage(browser);
            WaitUtils.waitForDashboardPageLoaded(browser);
            assertEquals(
                    dashboardsPage.getContent().getLatestReport(TableReport.class)
                            .waitForLoaded().getAttributeValues().size(),
                    reportRowCountBeforeDrilling, "Target tab items are not back to original values");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDrillToCurrentTab() throws JSONException, IOException {
        Dashboard dashboard = initDashboardHavingNoFilterInTabs();
        String dashboardUri = dashboardRequest.createDashboard(dashboard.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dashboard.getName());
            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
            tableReport.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), SOURCE_TAB), DASHBOARD_DRILLING_GROUP);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE).waitForLoaded();

            assertTrue(dashboardsPage.getTabs().getTab(SOURCE_TAB).isSelected(),
                    SOURCE_TAB + " is not selected after drill action");

            TableReport tableReportAfterDrilling = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertEquals(tableReportAfterDrilling.getAttributeValues(), attributeValuesOfAmountReports,
                    "Report aren't rendered correctly");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDrillToReportBeforeToTab() throws JSONException, IOException {
        Dashboard dashboard = initDashboardHavingNoFilterInTabs();
        String dashboardUri = dashboardRequest.createDashboard(dashboard.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dashboard.getName());
            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
            addDrillingHavingInnerDrill(tableReport, TARGET_TAB);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            TableReport reportAfterDrillingToReport = drillDialog.getReport(TableReport.class);
            assertEquals(reportAfterDrillingToReport.getAttributeValues(), attributeValuesOfReportHavingFilter,
                    "Report aren't rendered correctly");
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(REPORT_AMOUNT_BY_PRODUCT, "CompuSci"), ">>"));

            reportAfterDrillingToReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            final Function<WebDriver, Boolean> isTargetTabLoaded = browser -> dashboardsPage.getTabs()
                    .getTab(TARGET_TAB).isSelected();
            Graphene.waitGui().until(isTargetTabLoaded);
            waitForDashboardPageLoaded(browser);

            TableReport reportAfterDrillingToTab = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertEquals(reportAfterDrillingToTab.getAttributeValues(), attributeValuesOfSalesReports,
                    "Report aren't rendered correctly");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDrillToAnotherDashboardTab() throws JSONException, IOException {
        Dashboard dashboard = initDashboardHavingNoFilterInSourceTab();
        String dashboardUri = dashboardRequest.createDashboard(dashboard.getMdObject());
        String anotherDashboardUri = dashboardRequest.createDashboard(initDashboardHavingOneTab().getMdObject());
        try {
            initDashboardsPage().selectDashboard(dashboard.getName());
            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
            tableReport.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), TAB_ANOTHER_DASHBOARD), DASHBOARD_DRILLING_GROUP);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            SaveAndContinueDialog popupDialog = SaveAndContinueDialog.getInstance(browser);
            assertTrue(popupDialog.getRoot().isDisplayed(), "Save and Continue dialog isn't displayed");

            popupDialog.cancel();
            assertTrue(dashboardsPage.getTabs().getTab(SOURCE_TAB).isSelected(), "Tab is changed");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit mode");

            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            waitForFragmentVisible(popupDialog);
            popupDialog.saveAndContinue();
            dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(TAB_ANOTHER_DASHBOARD).isSelected(),
                    TAB_ANOTHER_DASHBOARD + " is not selected after drill action");
            assertFalse(dashboardsPage.isEditDashboardVisible(), "Current mode isn't view mode");

            browser.navigate().back();
            dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(SOURCE_TAB).isSelected(), "Back to incorrect tab");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit mode");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri, anotherDashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDrillToReportBeforeToAnotherDashboardTab() throws JSONException, IOException {
        Dashboard dashboard = initDashboardHavingNoFilterInSourceTab();
    String dashboardUri = dashboardRequest.createDashboard(dashboard.getMdObject());

        String anotherDashboardUri = dashboardRequest.createDashboard(initDashboardHavingOneTab().getMdObject());
        try {
            initDashboardsPage().selectDashboard(dashboard.getName());
            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
            addDrillingHavingInnerDrill(tableReport, TAB_ANOTHER_DASHBOARD);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                    waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
            TableReport reportAfterDrillingToReport = drillDialog.getReport(TableReport.class);
            assertEquals(reportAfterDrillingToReport.getAttributeValues(), attributeValuesOfReportHavingFilter,
                    "Report aren't rendered correctly");
            assertEquals(drillDialog.getBreadcrumbsString(),
                    StringUtils.join(Arrays.asList(REPORT_AMOUNT_BY_PRODUCT, "CompuSci"), ">>"));

            reportAfterDrillingToReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            SaveAndContinueDialog popupDialog = SaveAndContinueDialog.getInstance(browser);
            assertTrue(popupDialog.getRoot().isDisplayed(), "Save and Continue dialog isn't displayed");

            popupDialog.cancel();
            assertTrue(dashboardsPage.getTabs().getTab(SOURCE_TAB).isSelected(), "Tab is changed");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit mode");

            reportAfterDrillingToReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            waitForFragmentVisible(popupDialog);
            popupDialog.saveAndContinue();
            waitForDashboardPageLoaded(browser);
            dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(TAB_ANOTHER_DASHBOARD).isSelected(),
                    TAB_ANOTHER_DASHBOARD + " is not selected after drill action");
            assertFalse(dashboardsPage.isEditDashboardVisible(), "Current mode is not view mode");

            browser.navigate().back();
            dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(SOURCE_TAB).isSelected(), "Back to incorrect tab");
            assertTrue(dashboardsPage.isEditDashboardVisible(), "Current mode isn't edit mode");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashboardUri, anotherDashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSecondDrillFurtherToTab() throws IOException, JSONException {
        String dashUri = dashboardRequest.createDashboard(initDashForDrillFurtherTest().getMdObject());
        try {
            initDashboardsPage().selectDashboard(DASHBOARD_FOR_DRILL_FURTHER_TEST).editDashboard();
            dashboardsPage.getTabs().openTab(0);
            TableReport tableOnFirstTab = dashboardsPage.getContent().getLatestReport(TableReport.class);

            WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                    .openConfigurationPanelFor(tableOnFirstTab.getRoot(), browser);
            widgetConfigPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                    .addDrilling(Pair.of(singletonList(METRIC_AMOUNT), REPORT_TOP_5_OPEN_BY_CASH), REPORTS_DRILLING_GROUP)
                    .addInnerDrillToLastItemPanel(Pair.of(singletonList(ATTR_OPPORTUNITY), FIRST_TAB))
                    .addDrilling(Pair.of(singletonList(ATTR_PRODUCT), REPORT_TOP_5_OPEN_BY_CASH), REPORTS_DRILLING_GROUP)
                    .addInnerDrillToLastItemPanel(Pair.of(singletonList(METRIC_TOP_5_OF_BEST_CASE), SECOND_TAB));
            widgetConfigPanel.saveConfiguration();
            dashboardsPage.saveDashboard();
            tableOnFirstTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            TableReport reportAfterDrillAction = DashboardDrillDialog.getInstance(browser)
                    .getReport(TableReport.class).waitForLoaded();

            assertEquals(reportAfterDrillAction.getAttributeHeaders(), singletonList(ATTR_OPPORTUNITY),
                    REPORT_TOP_5_OPEN_BY_CASH + "report is not displayed after drill action");
            reportAfterDrillAction.drillOnFirstValue(CellType.METRIC_VALUE);
            assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded().getReportTiTle(),
                    REPORT_TOP_5_OPEN_BY_CASH, REPORT_TOP_5_OPEN_BY_CASH + " and " + SECOND_TAB + " are not displayed");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }

    private Dashboard getDashboardByName(String name) {
        List<Dashboard> dashboards = asList(initDashboardHavingDiffFilterMode(), initDashboardHavingSameFilterMode(),
                initDashboardHavingNoFilterInSourceTab(), initDashboardHavingNoFilterInTargetTab());

        return dashboards.stream()
                .filter(dashboard -> dashboard.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find given dashboard"));
    }

    private void changeFilterValues(String filterName, List<String> values) {
        dashboardsPage.getContent().getFilterWidgetByName(filterName)
                .changeAttributeFilterValues(values.toArray(new String[values.size()]));
    }

    private void checkSelectedFilterValues(String filterName, List<String> expectedValues) {
        assertEquals(dashboardsPage.getContent().getFilterWidgetByName(filterName).getCurrentValue(),
                expectedValues.stream().collect(Collectors.joining(", ")));
    }

    private Dashboard initDashboardHavingDiffFilterMode() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(productAsSingleFilter, RIGHT),
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(accountAsSingleFilter, TOP_RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_DIFF_FILTER_MODE);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsSingleFilter);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(accountAsSingleFilter);
            dash.addFilter(accountAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Tab initTab(String name, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream().map(pair -> Builder.of(FilterItem::new)
                .with(item -> item.setContentId(pair.getLeft().getId()))
                .with(item -> item.setPosition(pair.getRight())).build()).collect(Collectors.toList());

        ReportItem reportItem = createReportItem(amountByProductReportUri,
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));

        return initDashboardTab(name,
                Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream).collect
                        (Collectors.toList()));
    }

    private Dashboard initDashboardHavingSameFilterMode() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(productAsSingleFilter, RIGHT),
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_SAME_FILTER_MODE);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsSingleFilter);
            dash.addFilter(accountAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Dashboard initDashboardHavingNoFilterInSourceTab() {
        Tab sourceTab = initDashboardTab(SOURCE_TAB, singletonList(createReportItem(amountByProductReportUri)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(productAsSingleFilter, RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Dashboard initDashboardHavingNoFilterInTargetTab() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(productAsSingleFilter, TOP_RIGHT)));

        Tab targetTab = initDashboardTab(TARGET_TAB, singletonList(createReportItem(amountByProductReportUri)));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Dashboard initDashboardHavingNoFilterInTabs() {
        Tab sourceTab = initDashboardTab(SOURCE_TAB, singletonList(createReportItem(amountByProductReportUri)));
        Tab targetTab = initDashboardTab(TARGET_TAB, singletonList(createReportItem(salesSeasonalityReportUri)));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName("Dashboard " + generateHashString());
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
        }).build();
    }

    private Dashboard initDashboardHavingOneTab() {
        Tab sourceTab = initDashboardTab(TAB_ANOTHER_DASHBOARD, singletonList(createReportItem(amountByProductReportUri)));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName("Dashboard " + generateHashString());
            dash.addTab(sourceTab);
        }).build();
    }

    private void addDrillingHavingInnerDrill(TableReport tableReport, String tabDrillTo) {
        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                .openConfigurationPanelFor(tableReport.getRoot(), browser);
        widgetConfigPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                .addDrilling(Pair.of(singletonList(ATTR_PRODUCT), REPORT_TOP_5_OPEN_BY_CASH), REPORTS_DRILLING_GROUP)
                .addInnerDrillToLastItemPanel(Pair.of(singletonList(ATTR_OPPORTUNITY), tabDrillTo));
        widgetConfigPanel.saveConfiguration();
    }

    private Dashboard initDashForDrillFurtherTest() {
        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(DASHBOARD_FOR_DRILL_FURTHER_TEST);
            dashboard.addTab(Builder.of(Tab::new).with(tab -> {
                tab.setTitle(FIRST_TAB);
                tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                    reportItem.setObjUri(getReportByTitle(REPORT_AMOUNT_BY_PRODUCT).getUri());
                    reportItem.setPosition(ItemPosition.LEFT);
                }).build());
            }).build());

            dashboard.addTab(Builder.of(Tab::new).with(tab -> {
                tab.setTitle(SECOND_TAB);
                tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                    reportItem.setObjUri(getReportByTitle(REPORT_TOP_5_OPEN_BY_CASH).getUri());
                    reportItem.setPosition(ItemPosition.RIGHT);
                }).build());
            }).build());
        }).build();
    }
}
