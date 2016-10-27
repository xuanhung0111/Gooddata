package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.joda.time.DateTime.now;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;

import java.util.List;
import java.util.UUID;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class GoodSalesAdvancedConnectingFilterTest extends GoodSalesAbstractTest {

    private static final String DASHBOARD_TAB = "New tab";
    private static final String REPORT = "Report-" + System.currentTimeMillis();

    private static final String YEAR_OF_DATA = "2012";
    private static final String YEAR_2013 = "2013";
    private static final String YEAR_2014 = "2014";
    private static final String CURRENT_YEAR = String.valueOf(now().getYear());

    private static final String THIS = "this";
    private static final String LAST = "last";
    private static final String ALL = "All";

    private static final List<String> ATTRIBUTE_VALUES = asList("Direct Sales", "Inside Sales", "CompuSci",
                "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");
    private static final List<String> COMBINATION_ATTRIBUTE_FILTER_VALUES =
            asList("Direct Sales", "Inside Sales", "CompuSci");

    private static final String PRODUCT_COMPUSCI = "CompuSci";
    private static final String PRODUCT_EDUCATIONLY = "Educationly";

    private static final String SAVED_VIEW = "New saved view";
    private static final String UN_SAVED_VIEW = "* Unsaved View";

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void createReport() {
        createReport(new UiReportDefinition()
                .withName(REPORT)
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_PRODUCT)
                .withHows(new HowItem(ATTR_DEPARTMENT, Position.TOP)),
                REPORT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDateFilterConnectBetweenDuplicatedTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);

        dashboardsPage.duplicateDashboardTab(0);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);

        dashboardsPage.openTab(0);
        assertEquals(getDateCreatedFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_2014)
                .getCurrentValue(),
                YEAR_2014);

        dashboardsPage.openTab(1);
        takeScreenshot(browser, "Date-filter-connected-between-duplicated-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDateFilterConnectBetweenSameTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .saveDashboard()
                .getFirstFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_2014);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Date-filter-connected-between-same-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithDateFilterBetweenDuplicatedTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();

        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report data is not rendered with date filter correctly");

        dashboardsPage.duplicateDashboardTab(0);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report data is not rendered with date filter correctly");

        dashboardsPage.openTab(0);
        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getReport().waitForReportLoading();

        takeScreenshot(browser,
                "checkReportRenderWithDateFilterBetweenDuplicatedTabs-Report-render-with-date-filter", getClass());
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(1);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-date-filter-in-duplicated-tab", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithDateFilterBetweenSameTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getReport().waitForReportLoading();

        takeScreenshot(browser,
                "checkReportRenderWithDateFilterBetweenSameTabs-Report-render-with-date-filter", getClass());
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-date-filter-in-same-tab", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDifferenceDateFilterNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS)
                .saveDashboard()
                .getFirstFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_2014);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Difference-date-filters-do-not-connect-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDateFilterWithDifferenceValueNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, LAST)
                .saveDashboard()
                .getFirstFilter()
                // change date filter value in new created tab to year 2013
                .changeTimeFilterValueByClickInTimeLine(YEAR_2013);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Date-filters-with-difference-value-do-not-connect-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithDifferenceDateFilterBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_SNAPSHOT, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();

        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        getDateSnapshotFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getReport().waitForReportLoading();

        takeScreenshot(browser,
                "checkReportRenderWithDifferenceDateFilterBetweenTabs-Report-render-with-date-filter", getClass());
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-difference-date-filter", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report is not render correctly");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithDifferenceDateValuesBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, LAST)
                .addReportToDashboard(REPORT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getReport().waitForReportLoading();

        takeScreenshot(browser,
                "checkReportRenderWithDifferenceDateValuesBetweenTabs-Report-render-with-date-filter", getClass());
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-difference-date-filter-value", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report is not render correctly");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDateFiltersConnectBetweenDuplicatedTab() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage.saveDashboard();
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getDateClosedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        dashboardsPage.duplicateDashboardTab(0);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getDateClosedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        dashboardsPage.openTab(0);
        assertEquals(getDateCreatedFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA)
                .getCurrentValue(),
                YEAR_OF_DATA);
        assertEquals(getDateClosedFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA)
                .getCurrentValue(),
                YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(1);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Multiple-date-filters-connected-between-duplicated-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getDateClosedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDateFiltersConnectBetweenSameTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        dashboardsPage.saveDashboard();

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getDateClosedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Multiple-date-filters-connected-between-same-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getDateClosedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDifferenceDateFiltersNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_SNAPSHOT, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage.saveDashboard();
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getDateSnapshotFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Multiple-date-filters-do-not-connected-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getDateClosedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report is not rendered correctly");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDifferenceDateFilterValuesNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, THIS);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, DateGranularity.YEAR, LAST);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        dashboardsPage.saveDashboard();

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getDateClosedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Multiple-date-filters-with-difference-value-do-not-connect-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getDateClosedFilter().getCurrentValue(), CURRENT_YEAR);
        assertTrue(getReport().isNoData(), "Report is not rendered correctly");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkFiltersCombinationConnectBetweenDuplicatedTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();

        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getProductFilter().getCurrentValue(), ALL);

        dashboardsPage.duplicateDashboardTab(0);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getProductFilter().getCurrentValue(), ALL);

        dashboardsPage.openTab(0);
        assertEquals(getDateCreatedFilter()
                .changeTimeFilterValueByClickInTimeLine(YEAR_2014)
                .getCurrentValue(),
                YEAR_2014);
        assertEquals(getProductFilter()
                .changeAttributeFilterValues(PRODUCT_COMPUSCI)
                .getCurrentValue(),
                PRODUCT_COMPUSCI);

        dashboardsPage.openTab(1);
        takeScreenshot(browser, "Attribute-filter-and-date-filter-combination-applied-between-duplicated-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_COMPUSCI);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkFiltersCombinationConnectBetweenSameTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.LEFT);
        dashboardsPage.saveDashboard();

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_2014);
        getProductFilter().changeAttributeFilterValues(PRODUCT_COMPUSCI);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Attribute-filter-and-date-filter-combination-applied-between-same-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_COMPUSCI);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithFiltersCombinationBetweenDuplicatedTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        dashboardsPage.saveDashboard();

        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getProductFilter().getCurrentValue(), ALL);
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        dashboardsPage.duplicateDashboardTab(0);
        assertEquals(getDateCreatedFilter().getCurrentValue(), CURRENT_YEAR);
        assertEquals(getProductFilter().getCurrentValue(), ALL);
        assertTrue(getReport().isNoData(), "Report data is not rendered correctly");

        dashboardsPage.openTab(0);
        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getProductFilter().changeAttributeFilterValues(PRODUCT_COMPUSCI);
        assertEquals(getReport().getAttributeElements(), COMBINATION_ATTRIBUTE_FILTER_VALUES);

        dashboardsPage.openTab(1);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-filter-combination-between-duplicated-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_COMPUSCI);
        assertEquals(getReport().getAttributeElements(), COMBINATION_ATTRIBUTE_FILTER_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkReportRenderWithFiltersCombinationBetweenSameTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        dashboardsPage.saveDashboard();

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getProductFilter().changeAttributeFilterValues(PRODUCT_COMPUSCI);
        assertEquals(getReport().getAttributeElements(), COMBINATION_ATTRIBUTE_FILTER_VALUES);

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-filter-combination-between-same-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_COMPUSCI);
        assertEquals(getReport().getAttributeElements(), COMBINATION_ATTRIBUTE_FILTER_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDifferenceFiltersCombinationNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        dashboardsPage.saveDashboard().openTab(0);

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getProductFilter().changeAttributeFilterValues(PRODUCT_COMPUSCI);
        assertEquals(getReport().getAttributeElements(), COMBINATION_ATTRIBUTE_FILTER_VALUES);

        dashboardsPage.openTab(1);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-difference-filter-combination-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getDepartmentFilter().getCurrentValue(), ALL);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDifferenceFilterValuesCombinationNotConnectBetweenTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addReportToDashboard(REPORT)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        moveElementToRightPlace(getReport().getRoot(), DashboardWidgetDirection.LEFT);
        moveElementToRightPlace(getDateCreatedFilter().getRoot(), DashboardWidgetDirection.RIGHT);
        getProductFilter().changeSelectionToOneValue();

        dashboardsPage.saveDashboard();
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_COMPUSCI);

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_OF_DATA);
        getProductFilter().changeAttributeFilterValues("Educationly");
        assertEquals(getReport().getAttributeElements(), asList("Direct Sales", "Inside Sales", "Educationly"));

        dashboardsPage.openTab(0);
        getReport().waitForReportLoading();

        takeScreenshot(browser, "Report-render-with-difference-filter-values-combination-between-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_OF_DATA);
        assertEquals(getProductFilter().getCurrentValue(), ALL);
        assertEquals(getReport().getAttributeElements(), ATTRIBUTE_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkFilterAppliedWithDashboardSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .addNewTab(DASHBOARD_TAB)
                .addTimeFilterToDashboard(DATE_DIMENSION_CREATED, DateGranularity.YEAR, THIS)
                .turnSavedViewOption(true)
                .saveDashboard()
                .openTab(0);
        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_2014);

        SavedViewWidget saveViewWidget = dashboardsPage.getSavedViewWidget();
        saveViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW);
        assertEquals(saveViewWidget.getCurrentSavedView(), SAVED_VIEW);

        dashboardsPage.openTab(1);
        takeScreenshot(browser, "New-saved-view-is-applied-for-all-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
        assertEquals(saveViewWidget.getCurrentSavedView(), SAVED_VIEW);

        getDateCreatedFilter().changeTimeFilterValueByClickInTimeLine(YEAR_2013);
        assertEquals(saveViewWidget.getCurrentSavedView(), UN_SAVED_VIEW);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Un-saved-view-is-applied-for-all-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2013);
        assertEquals(saveViewWidget.getCurrentSavedView(), UN_SAVED_VIEW);

        saveViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW);
        assertEquals(saveViewWidget.getCurrentSavedView(), SAVED_VIEW);
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);

        dashboardsPage.openTab(1);
        takeScreenshot(browser, "Selected-saved-view-is-applied-for-all-tabs", getClass());
        assertEquals(getDateCreatedFilter().getCurrentValue(), YEAR_2014);
        assertEquals(saveViewWidget.getCurrentSavedView(), SAVED_VIEW);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered for bug 'ONE-1720 Browser is"
            + " freezing when opening GD dashboard'")
    public void checkSingleFilterWorkCorrectlyWhenSwitchingTabs() {
        initDashboardsPage()
                .addNewDashboard(generateDashboard())
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .setParentsForFilter(ATTR_PRODUCT, ATTR_DEPARTMENT);

        moveElementToRightPlace(getProductFilter().getRoot(), DashboardWidgetDirection.LEFT);
        getProductFilter().changeSelectionToOneValue();
        getDepartmentFilter().changeSelectionToOneValue();

        dashboardsPage
                .addNewTab(DASHBOARD_TAB)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);

        getProductFilter().changeSelectionToOneValue();
        dashboardsPage
                .openTab(0)
                .turnSavedViewOption(true)
                .saveDashboard();

        getProductFilter().changeAttributeFilterValues(PRODUCT_EDUCATIONLY);
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget()
                .openSavedViewMenu()
                .saveCurrentView(SAVED_VIEW);

        dashboardsPage.openTab(1);
        takeScreenshot(browser, "Single-filter-works-correctly-in-new-tab", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_EDUCATIONLY);

        dashboardsPage.openTab(0);
        takeScreenshot(browser, "Single-filter-works-correctly-in-old-tab", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW);
        assertEquals(getProductFilter().getCurrentValue(), PRODUCT_EDUCATIONLY);
    }

    private String generateDashboard() {
        return "Dashboard-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void moveElementToRightPlace(WebElement element, DashboardWidgetDirection direction) {
        element.click();
        direction.moveElementToRightPlace(element);
    }

    private TableReport getReport() {
        return dashboardsPage.getReport(REPORT, TableReport.class);
    }

    private FilterWidget getDateCreatedFilter() {
        return dashboardsPage.getFilterWidgetByName(DATE_DIMENSION_CREATED);
    }

    private FilterWidget getDateClosedFilter() {
        return dashboardsPage.getFilterWidgetByName(DATE_DIMENSION_CLOSED);
    }

    private FilterWidget getDateSnapshotFilter() {
        return dashboardsPage.getFilterWidgetByName(DATE_DIMENSION_SNAPSHOT);
    }

    private FilterWidget getProductFilter() {
        return dashboardsPage.getFilterWidgetByName(ATTR_PRODUCT);
    }

    private FilterWidget getDepartmentFilter() {
        return dashboardsPage.getFilterWidgetByName(ATTR_DEPARTMENT);
    }
}