package com.gooddata.qa.graphene;

import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractProjectTest;

import java.util.HashMap;
import java.util.Map;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    protected static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    protected static final int expectedGoodSalesReportsCount = 103;
    protected static final int expectedGoodSalesReportsCustomFoldersCount = 9;

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
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
