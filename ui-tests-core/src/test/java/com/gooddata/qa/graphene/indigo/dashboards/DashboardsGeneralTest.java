package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DashboardsGeneralTest extends GoodSalesAbstractTest {

    @Test(dependsOnMethods = {"createProject"})
    public void kpisLoadedCheck() {
        initIndigoDashboardsPage();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEditModeCancel() {
        Kpi selectedKpi;
        String selectedKpiDataHeadline;
        String selectedKpiDataValue;

        initIndigoDashboardsPage();

        indigoDashboardsPage.switchToEditMode();

        selectedKpi = indigoDashboardsPage.selectKpi(0);
        selectedKpiDataHeadline = selectedKpi.getHeadline();
        selectedKpiDataValue = selectedKpi.getValue();

        indigoDashboardsPage.selectMetricByIndex(0).cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
        assertEquals(selectedKpi.getValue(), selectedKpiDataValue);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkKpiTitleChange() {
        Kpi selectedKpi;
        String selectedKpiDataHeadline;

        initIndigoDashboardsPage();

        indigoDashboardsPage.switchToEditMode();

        selectedKpi = indigoDashboardsPage.selectKpi(0);
        selectedKpiDataHeadline = selectedKpi.getHeadline();

        selectedKpi.setHeadline("Test headline");

        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);

        indigoDashboardsPage.selectMetricByIndex(0);
        assertTrue(selectedKpi.getHeadline().equals("Test headline"));

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
    }

}
