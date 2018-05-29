package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel.DrillingGroup;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.extra.YearQuarter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.enums.DateRange.ZONE_ID;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITY_LEVEL;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class DrillToDashBoardTabByDateDimensionTest extends AbstractDashboardWidgetTest {

    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";
    private static final String DATE_DIMENSION_ACTIVITY = "Date dimension (Activity)";

    private Metrics metrics;
    private String testReportUri;
    private DashboardRestRequest dashboardRequest;

    private YearQuarter currentYearQuarter = YearQuarter.now(ZONE_ID);

    @Override
    protected void customizeProject() throws Throwable {
        metrics = new Metrics(getAdminRestClient(), testParams.getProjectId());
        testReportUri = createReport();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @DataProvider
    public Object[][] drillToDashboardData() throws IOException, JSONException {
        Pair<String, String> yearFilter = Pair.of(DATE_DIMENSION_ACTIVITY, "2014");
        Pair<String, String> quarterFilter = Pair.of(DATE_DIMENSION_ACTIVITY, "Q4 2015 - Q1 2016");

        return new Object[][] {
                // same dashboard
                {createDashboardCase1(), null, TARGET_TAB + "1", yearFilter, 1},
                {createDashboardCase2(), null, TARGET_TAB + "2", yearFilter, 1},
                {createDashboardCase3(), null, TARGET_TAB + "3", quarterFilter, 2},
                // different dashboards
                {createSourceDashboardCase4(), createTargetDashboardCase4(), TARGET_TAB + "4", yearFilter, 1},
                {createSourceDashboardCase5(), createTargetDashboardCase5(), TARGET_TAB + "5", yearFilter, 1},
                {createSourceDashboardCase6(), createTargetDashboardCase6(), TARGET_TAB + "6", quarterFilter, 2},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "drillToDashboardData")
    public void drillToDashboard(String dashboard1,
                                 String dashboard2,
                                 String drillToTab,
                                 Pair<String, String> expectedFilterValue,
                                 int expectedReportRowCount) {

        int size;
        boolean isSameDashboard = StringUtils.isEmpty(dashboard2);

        initDashboardsPage().selectDashboard(dashboard1).editDashboard();

        TableReport reportOnSourceTab = dashboardsPage.getContent().getReport(REPORT_ACTIVITY_LEVEL, TableReport.class);
        reportOnSourceTab.addDrilling(Pair.of(singletonList(ATTR_YEAR_ACTIVITY), drillToTab),
                DrillingGroup.DASHBOARDS.getName());

        // wait for saving dashboard
        dashboardsPage.saveDashboard();

        // attr to drill to is 2014
        reportOnSourceTab.drillOn("2014", CellType.ATTRIBUTE_VALUE);

        if (isSameDashboard) {
            Graphene.waitGui().until(browser -> dashboardsPage.getTabs().getTab(drillToTab).isSelected());
        }

        TableReport reportOnTargetTab = dashboardsPage.getContent().getReport(REPORT_ACTIVITY_LEVEL, TableReport.class);

        if (isSameDashboard) {
            size = reportOnTargetTab.getAttributeValues().size();
        } else {
            size = reportOnTargetTab.waitForLoaded().getAttributeValues().size();
        }

        Screenshots.takeScreenshot(browser, "testDrillToDashboardTab-" +
                (isSameDashboard ? dashboard1 : dashboard2), getClass());

        assertEquals(size, expectedReportRowCount, "Report is not applied date filter");
        assertEquals(dashboardsPage.getContent().getFilterWidgetByName(expectedFilterValue.getLeft()).getCurrentValue(),
                expectedFilterValue.getRight());
    }

    /**
     * Case 1 - Drill to tab in same dashboard
     * Source tab has 'Date (Activity)' filter with 'Year' dimension
     * Target tab has 'Date (Activity)' filter with 'Year' dimension
     * Drilled value will be transferred to target tab
     *
     * @return String
     */
    private String createDashboardCase1() throws IOException, JSONException {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2011), getYearOffset(2016));
        Tab sourceTab = createTabWithFilterAndReport(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2015), getYearOffset(2016));
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "1", Pair.of(targetFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(sourceTab, targetTab), asList(sourceFilterContent, targetFilterContent));
    }

    /**
     * Case 2 - Drill to tab in same dashboard
     * Source tab does not have 'Date (Activity)' filter
     * Target tab has 'Date (Activity)' filter with 'Year' dimension
     * Drilled value will be transferred to target tab
     *
     * @return String
     */
    private String createDashboardCase2() throws IOException, JSONException {
        Tab sourceTab = createTabWithReport(SOURCE_TAB);

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2015), getYearOffset(2016));
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "2", Pair.of(targetFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(sourceTab, targetTab), asList(targetFilterContent));
    }

    /**
     * Case 3 - Drill to tab in same dashboard
     * Source tab has 'Date (Activity)' filter with 'Year' dimension
     * Target tab has 'Date (Activity)' filter with 'Quarter' dimension
     * Drilled value will not be transferred to target tab
     *
     * @return String
     */
    private String createDashboardCase3() throws IOException, JSONException {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2011), getYearOffset(2016)); // 2011-2016
        Tab sourceTab = createTabWithFilterAndReport(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.quarter"),
                getQuarterOffset("2015-Q4"), getQuarterOffset("2016-Q1"));
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "3", Pair.of(targetFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(sourceTab, targetTab), asList(sourceFilterContent, targetFilterContent));
    }

    /**
     * Case 4 - Drill to tab in same dashboard
     * Source Dashboard - Tab has 'Date (Activity)' filter with 'Year' dimension
     * Target dashboard - Tab has 'Date (Activity)' filter with 'Year' dimension
     * Drilled value will be transferred to target tab
     *
     * @return String
     */
    private String createSourceDashboardCase4() throws IOException, JSONException {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2011), getYearOffset(2016)); // 2011-2016
        Tab sourceTab = createTabWithFilterAndReport(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(sourceTab), asList(sourceFilterContent));
    }

    private String createTargetDashboardCase4() throws IOException, JSONException {
        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2015), getYearOffset(2016)); // 2015-2016
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "4", Pair.of(targetFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(targetTab), asList(targetFilterContent));
    }

    /**
     * Case 5 - Drill to tab in different dashboard
     * Source dashboard - Tab does not have 'Date (Activity)' filter
     * Target dashboard - Tab has 'Date (Activity)' filter with 'Year' dimension
     * Drilled value will be transferred to target tab
     *
     * @return String
     */
    private String createSourceDashboardCase5() throws IOException, JSONException {
        Tab sourceTab = createTabWithReport(SOURCE_TAB);
        return createDashboardOnServer(asList(sourceTab), Collections.emptyList());
    }

    private String createTargetDashboardCase5() throws IOException, JSONException {
        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2015), getYearOffset(2016));
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "5", Pair.of(targetFilterContent, TOP_RIGHT));
        return createDashboardOnServer(asList(targetTab), asList(targetFilterContent));
    }

    /**
     * Case 6 - Drill to tab in same dashboard
     * Source dashboard - Tab has 'Date (Activity)' filter with 'Year' dimension
     * Target dashboard - Tab has 'Date (Activity)' filter with 'Quarter' dimension
     * Drilled value will not be transferred to target tab
     *
     * @return String
     */
    private String createSourceDashboardCase6() throws IOException, JSONException {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("activity.year"),
                getYearOffset(2011), getYearOffset(2016));
        Tab sourceTab = createTabWithFilterAndReport(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(sourceTab), asList(sourceFilterContent));
    }

    private String createTargetDashboardCase6() throws IOException, JSONException {
        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("activity.quarter"),
                getQuarterOffset("2015-Q4"), getQuarterOffset("2016-Q1"));
        Tab targetTab = createTabWithFilterAndReport(TARGET_TAB + "6", Pair.of(targetFilterContent, TOP_RIGHT));

        return createDashboardOnServer(asList(targetTab), asList(targetFilterContent));
    }

    /*----------Util method------------*/

    private Dashboard createDashboardModel(String name, List<Tab> tabs, List<FilterItemContent> filterItemContents) {
        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(name);
            for (Tab tab : tabs) {
                dashboard.addTab(tab);
            }
            for (FilterItemContent filterItemContent : filterItemContents) {
                dashboard.addFilter(filterItemContent);
            }
        }).build();
    }

    private String createDashboardOnServer(List<Tab> tabs, List<FilterItemContent> filterItemContents)
            throws IOException, JSONException {
        String name = generateDashboardName();
        Dashboard dashboard = createDashboardModel(name, tabs, filterItemContents);
        dashboardRequest.createDashboard(dashboard.getMdObject());
        return name;
    }

    private Tab createTabWithFilterAndReport(String name, Pair<FilterItemContent, TabItem.ItemPosition> appliedFilter) {
        FilterItem filterItem = Builder.of(FilterItem::new)
                .with(item -> item.setContentId(appliedFilter.getLeft().getId()))
                .with(item -> item.setPosition(appliedFilter.getRight())).build();

        ReportItem reportItem = createReportItem(testReportUri, singletonList(filterItem.getId()));

        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(asList(reportItem, filterItem)))
                .build();
    }

    private Tab createTabWithReport(String name) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(asList(createReportItem(testReportUri))))
                .build();
    }

    private String createReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITY_LEVEL,
                singletonList(METRIC_GROUP),
                Arrays.asList(new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_ACTIVITY))),
                singletonList(new MetricElement(metrics.createNumberOfActivitiesMetric()))));
    }

    private int getYearOffset(int year) {
        return year - currentYearQuarter.getYear();
    }

    private int getQuarterOffset(String yearQuarter) {
        YearQuarter yq = YearQuarter.parse(yearQuarter);
        int yearDelta = yq.getYear() - currentYearQuarter.getYear();
        int quarterDelta = yq.getQuarterValue() - currentYearQuarter.getQuarterValue();
        return (yearDelta * 4) + quarterDelta;
    }
}
