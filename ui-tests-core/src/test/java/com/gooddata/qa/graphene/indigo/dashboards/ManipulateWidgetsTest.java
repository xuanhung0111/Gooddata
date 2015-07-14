package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import java.util.UUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class ManipulateWidgetsTest extends DashboardWithWidgetsTest {

    private static final String TEST_HEADLINE = "Test headline";

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkEditModeCancelNoChanges() {
        Kpi selectedKpi = selectKpiByIndex(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String kpiValue = selectedKpi.getValue();

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
        assertEquals(selectedKpi.getValue(), kpiValue);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndDiscard() {
        Kpi selectedKpi = selectKpiByIndex(0);
        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndAbortCancel() {
        Kpi selectedKpi = selectKpiByIndex(0);
        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .cancelClick();

        assertEquals(selectedKpi.getHeadline(), modifiedHeadline);
        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndSave() {
        Kpi selectedKpi = selectKpiByIndex(0);
        String uniqueHeadline = generateUniqueHeadlineTitle();
        selectedKpi.setHeadline(uniqueHeadline);

        indigoDashboardsPage.saveEditMode();

        assertEquals(selectedKpi.getHeadline(), uniqueHeadline);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeWhenMetricChange() {
        Kpi selectedKpi = selectKpiByIndex(0);

        indigoDashboardsPage.selectMetricByName(AMOUNT);
        selectedKpi.setHeadline("");

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, AMOUNT);

        indigoDashboardsPage.selectMetricByName(LOST);
        assertNotEquals(selectedKpi.getHeadline(), metricHeadline);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        Kpi selectedKpi = selectKpiByIndex(0);

        indigoDashboardsPage.selectMetricByName(AMOUNT);

        selectedKpi.setHeadline(TEST_HEADLINE);
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, TEST_HEADLINE);

        indigoDashboardsPage.selectMetricByName(AMOUNT);

        assertEquals(selectedKpi.getHeadline(), TEST_HEADLINE);
    }

    private String generateUniqueHeadlineTitle() {
        // create unique headline title which fits into headline title (has limited size)
        return UUID.randomUUID().toString().substring(0, 18);
    }
}
