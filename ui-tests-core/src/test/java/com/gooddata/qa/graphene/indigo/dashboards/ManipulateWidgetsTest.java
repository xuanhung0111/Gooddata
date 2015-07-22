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
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String kpiValue = selectedKpi.getValue();

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
        assertEquals(selectedKpi.getValue(), kpiValue);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndDiscard() {
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

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
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

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
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

        String uniqueHeadline = generateUniqueHeadlineTitle();
        selectedKpi.setHeadline(uniqueHeadline);

        indigoDashboardsPage.saveEditMode();

        assertEquals(selectedKpi.getHeadline(), uniqueHeadline);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitleChangeWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

        selectedKpi.setHeadline("");

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, AMOUNT);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(LOST)
            .selectDateDimensionByName(DATE_CREATED);
        assertNotEquals(selectedKpi.getHeadline(), metricHeadline);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        selectedKpi.setHeadline(TEST_HEADLINE);
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, TEST_HEADLINE);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(AMOUNT)
            .selectDateDimensionByName(DATE_CREATED);

        assertEquals(selectedKpi.getHeadline(), TEST_HEADLINE);
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkDeleteKpiConfirmAndSave() {
        int kpisCount = initIndigoDashboardsPage().getKpisCount();

        int kpisCountAfterAdd = kpisCount + 1;

        indigoDashboardsPage
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .saveEditMode();

        assertEquals(kpisCountAfterAdd, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCountAfterAdd, initIndigoDashboardsPage().getKpisCount());

        indigoDashboardsPage
            .switchToEditMode()
            .deleteLastKpi()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage.saveEditMode();

        assertEquals(kpisCount, initIndigoDashboardsPage().getKpisCount());
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkDeleteKpiConfirmAndDiscard() {
        int kpisCount = initIndigoDashboardsPage().getKpisCount();

        indigoDashboardsPage
            .switchToEditMode()
            .deleteKpi(0)
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .cancelEditMode()
            .waitForDialog()
            .submitClick();

        assertEquals(kpisCount, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCount, initIndigoDashboardsPage().getKpisCount());
    }

    private String generateUniqueHeadlineTitle() {
        // create unique headline title which fits into headline title (has limited size)
        return UUID.randomUUID().toString().substring(0, 18);
    }
}
