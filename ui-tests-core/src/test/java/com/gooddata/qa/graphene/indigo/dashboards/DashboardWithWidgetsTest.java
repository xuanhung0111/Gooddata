package com.gooddata.qa.graphene.indigo.dashboards;

import org.testng.annotations.Test;

public abstract class DashboardWithWidgetsTest extends DashboardsGeneralTest {

    public static final String AMOUNT = "Amount";
    public static final String LOST = "Lost";
    public static final String NUMBER_OF_ACTIVITIES = "# of Activities";

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void initIndigoDashboardWithWidgets() throws InterruptedException {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT)
            .addWidget(LOST)
            .addWidget(NUMBER_OF_ACTIVITIES)
            .saveEditMode();
    }

}
