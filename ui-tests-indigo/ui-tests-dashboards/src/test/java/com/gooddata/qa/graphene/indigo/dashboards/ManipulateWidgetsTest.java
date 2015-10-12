package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.annotations.Test;

public class ManipulateWidgetsTest extends DashboardWithWidgetsTest {

    private static final String TEST_HEADLINE = "Test headline";

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkEditModeCancelNoChanges() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .selectDateFilterByName(DATE_FILTER_ALL_TIME)
            .switchToEditMode()
            .selectKpi(0);

        String kpiHeadline = selectedKpi.getHeadline();
        String kpiValue = selectedKpi.getValue();

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
        assertEquals(selectedKpi.getValue(), kpiValue);

        takeScreenshot(browser, "checkEditModeCancelNoChanges", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndDiscard() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndAbortCancel() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndSave() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        String uniqueHeadline = generateUniqueHeadlineTitle();
        selectedKpi.setHeadline(uniqueHeadline);

        indigoDashboardsPage.saveEditModeWithKpis();

        assertEquals(selectedKpi.getHeadline(), uniqueHeadline);

        takeScreenshot(browser, "checkKpiTitleChangeAndSave-" + uniqueHeadline, getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitleChangeWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
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

        takeScreenshot(browser, "checkKpiTitlePersistenceWhenMetricChange-" + TEST_HEADLINE, getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndSave() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        int kpisCountAfterAdd = kpisCount + 1;

        indigoDashboardsPage
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .build())
            .saveEditModeWithKpis();

        assertEquals(kpisCountAfterAdd, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCountAfterAdd, initIndigoDashboardsPageWithWidgets().getKpisCount());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage.saveEditModeWithKpis();

        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndDiscard() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

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
        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }
}
