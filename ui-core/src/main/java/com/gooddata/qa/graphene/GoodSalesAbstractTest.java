package com.gooddata.qa.graphene;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeClass;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "GoodSales-test";
        projectTemplate = GOODSALES_TEMPLATE;
        projectCreateCheckIterations = 60; // 5 minutes

        expectedGoodSalesDashboardsAndTabs = new HashMap<String, String[]>();
        expectedGoodSalesDashboardsAndTabs.put("Pipeline Analysis", new String[]{
                "Outlook", "What's Changed", "Waterfall Analysis", "Leaderboards", "Activities",
                "Sales Velocity", "Quarterly Trends", "Seasonality", "...and more"
        });
    }
}
