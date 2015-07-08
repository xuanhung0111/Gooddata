package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DashboardsGeneralTest extends GoodSalesAbstractTest {

    private Kpi selectedKpi;
    private String selectedKpiDataHeadline;
    private String selectedKpiDataValue;

    @BeforeClass
    public void before() throws InterruptedException {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initDashboardTests() {
        initDashboardsPage();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void kpisLoadedCheck() {
        initIndigoDashboardsPage();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkEditModeCancelNoChanges() {
        processKpiSelection(0);

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
        assertEquals(selectedKpi.getValue(), selectedKpiDataValue);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndDiscard() {
        processKpiSelection(0);

        selectedKpi.setHeadline("Test headline");

        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndAbortCancel() {
        processKpiSelection(0);

        selectedKpi.setHeadline("Test headline");

        assertTrue(selectedKpi.getHeadline().equals("Test headline"));

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .cancelClick();

        assertTrue(selectedKpi.getHeadline().equals("Test headline"));
        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndSave() {
        processKpiSelection(0);

        selectedKpi.setHeadline("Test headline");

        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);

        indigoDashboardsPage.saveEditMode();

        assertEquals(selectedKpi.getHeadline(), "Test headline");
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeWhenMetricChange() {
        processKpiSelection(1);

        indigoDashboardsPage.selectMetricByName("Amount");

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, "Amount");

        indigoDashboardsPage.selectMetricByName("Lost");

        assertEquals(selectedKpi.getHeadline(), "Lost");
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        processKpiSelection(0);

        indigoDashboardsPage.selectMetricByName("Amount");

        selectedKpi.setHeadline("abc");
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, "abc");

        indigoDashboardsPage.selectMetricByName("Amount");

        assertEquals(selectedKpi.getHeadline(), "abc");
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void checkViewerCannotEditDashboard() throws JSONException, InterruptedException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.VIEWER);

            initDashboardsPage();
            initIndigoDashboardsPage();

            assertEquals(indigoDashboardsPage.checkIfEditButtonIsPresent(), false);
        } finally {
            logout();
            signIn(false, UserRoles.VIEWER);
        }
    }

    private Kpi processKpiSelection(int index) {
        selectedKpi = initIndigoDashboardsPage()
                .switchToEditMode()
                .selectKpi(index);

        selectedKpiDataHeadline = selectedKpi.getHeadline();
        selectedKpiDataValue = selectedKpi.getValue();

        return selectedKpi;
    }
}
