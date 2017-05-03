package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_AND_MORE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_LEADERBOARDS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_QUARTERLY_TRENDS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_SALES_VELOCITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_SEASONALITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WATERFALL_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.fixture.Fixture;
import org.testng.annotations.BeforeClass;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "GoodSales-test";
        appliedFixture = Fixture.GOODSALES;

        // going to be removed when https://jira.intgdc.com/browse/QA-6503 is done
        expectedGoodSalesDashboardsAndTabs = new HashMap<String, String[]>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[]{
                DASH_TAB_OUTLOOK, DASH_TAB_WHATS_CHANGED, DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_LEADERBOARDS, DASH_TAB_ACTIVITIES, DASH_TAB_SALES_VELOCITY,
                DASH_TAB_QUARTERLY_TRENDS, DASH_TAB_SEASONALITY, DASH_TAB_AND_MORE
        });
    }
}
