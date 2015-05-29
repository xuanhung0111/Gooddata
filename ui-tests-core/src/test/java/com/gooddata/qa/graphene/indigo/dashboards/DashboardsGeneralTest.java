package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardsGeneralTest extends GoodSalesAbstractTest {
    @Test(dependsOnMethods = {"createProject"}, groups = {"loading"})
    public void kpisLoadedCheck() {
        initIndigoDashboardsPage();
        waitForElementNotPresent(Kpi.IS_LOADING);
    }
}
