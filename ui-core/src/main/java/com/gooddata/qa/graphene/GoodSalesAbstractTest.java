package com.gooddata.qa.graphene;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.fixture.Fixture;
import org.testng.annotations.BeforeClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.util.Collections.singletonList;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    protected final String REPORT_TOP_SALES_REPS_BY_WON_AND_LOST = "Top Reps. by Won and Lost";
    protected final String REPORT_AMOUNT_BY_PRODUCT = "Sum of all deals by Product";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "GoodSales-test";
        appliedFixture = Fixture.GOODSALES;

        // going to be removed when https://jira.intgdc.com/browse/QA-6503 is done
        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[]{
                DASH_TAB_OUTLOOK, DASH_TAB_WHATS_CHANGED, DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_LEADERBOARDS, DASH_TAB_ACTIVITIES, DASH_TAB_SALES_VELOCITY,
                DASH_TAB_QUARTERLY_TRENDS, DASH_TAB_SEASONALITY, DASH_TAB_AND_MORE
        });
    }

    // md objects for dashboard
    protected String createTopSalesRepsByWonAndLostReport() {
        return createTableReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_SALES_REPS_BY_WON_AND_LOST,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_SALES_REP))),
                        Arrays.asList(
                                new MetricElement(getMetricByTitle(METRIC_WON)),
                                new MetricElement(getMetricByTitle(METRIC_LOST)))));
    }

    protected String createAmountByProductReport() {
        return createTableReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_PRODUCT, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
    }

}
