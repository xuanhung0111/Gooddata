package com.gooddata.qa.graphene.indigo.dashboards;

import org.testng.annotations.Test;

public abstract class DashboardWithWidgetsTest extends DashboardsGeneralTest {

    public static final String AMOUNT = "Amount";
    public static final String LOST = "Lost";
    public static final String NUMBER_OF_ACTIVITIES = "# of Activities";

    public static final String DATE_CREATED = "Date dimension (Created)";
    public static final String DATE_CLOSED = "Date dimension (Closed)";
    public static final String DATE_ACTIVITY = "Date dimension (Activity)";
    public static final String DATE_SNAPSHOT = "Date dimension (Snapshot)";

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void initIndigoDashboardWithWidgets() throws InterruptedException {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .addWidget(LOST, DATE_CREATED)
            .addWidget(NUMBER_OF_ACTIVITIES, DATE_CREATED)
            .saveEditMode();
    }

}
