package com.gooddata.qa.graphene.indigo.dashboards;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class DashboardsGeneralTest extends GoodSalesAbstractTest {

    @BeforeClass
    public void before() throws InterruptedException {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initDashboardTests() {
        initDashboardsPage();
    }
}
