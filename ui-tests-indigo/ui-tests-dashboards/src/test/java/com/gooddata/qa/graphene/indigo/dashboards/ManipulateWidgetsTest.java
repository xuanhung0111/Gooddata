package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class ManipulateWidgetsTest extends AbstractDashboardTest {

    private static final String TEST_HEADLINE = "Test headline";
    private static final String HINT_FOR_EDIT_NAME_BORDER_COLOR = "rgba\\(177, 193, 209, 0\\.(498\\d+|5)\\)";
    private static final String LONG_NAME_METRIC = "# test metric with longer name is shortened";
    private static final String PATTERN_OF_METRIC_NAME = "is shortened";

    @Override
    public void initProperties() {
        super.initProperties();
        validateAfterClass = true;
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        final List<String> kpiUris = asList(createAmountKpi(), createLostKpi(), createNumOfActivitiesKpi());
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), kpiUris);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkEditModeCancelNoChanges() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .selectDateFilterByName(DATE_FILTER_ALL_TIME)
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String kpiHeadline = selectedKpi.getHeadline();
        String kpiValue = selectedKpi.getValue();

        waitForFragmentVisible(indigoDashboardsPage).cancelEditModeWithoutChange();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
        assertEquals(selectedKpi.getValue(), kpiValue);

        takeScreenshot(browser, "checkEditModeCancelNoChanges", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndDiscard() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        waitForFragmentVisible(indigoDashboardsPage).cancelEditModeWithChanges();

        assertEquals(selectedKpi.getHeadline(), kpiHeadline);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndAbortCancel() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String kpiHeadline = selectedKpi.getHeadline();
        String modifiedHeadline = generateUniqueHeadlineTitle();

        selectedKpi.setHeadline(modifiedHeadline);

        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);

        waitForFragmentVisible(indigoDashboardsPage).tryCancelingEditModeWithoutApplying();

        assertEquals(selectedKpi.getHeadline(), modifiedHeadline);
        assertNotEquals(selectedKpi.getHeadline(), kpiHeadline);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiTitleChangeAndSave() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String uniqueHeadline = generateUniqueHeadlineTitle();
        selectedKpi.setHeadline(uniqueHeadline);

        waitForFragmentVisible(indigoDashboardsPage).saveEditModeWithWidgets();

        assertEquals(selectedKpi.getHeadline(), uniqueHeadline);

        takeScreenshot(browser, "checkKpiTitleChangeAndSave-" + uniqueHeadline, getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiTitleChangeWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        selectedKpi.setHeadline("");

        waitForFragmentVisible(indigoDashboardsPage).getConfigurationPanel()
            .selectMetricByName(METRIC_AMOUNT)
            .selectDateDataSetByName(DATE_DATASET_CREATED);

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, METRIC_AMOUNT);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(METRIC_LOST)
            .selectDateDataSetByName(DATE_DATASET_CREATED);
        assertNotEquals(selectedKpi.getHeadline(), metricHeadline);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        Kpi selectedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        waitForFragmentVisible(indigoDashboardsPage).getConfigurationPanel()
            .selectMetricByName(METRIC_AMOUNT)
            .selectDateDataSetByName(DATE_DATASET_CREATED);

        selectedKpi.setHeadline(TEST_HEADLINE);
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, TEST_HEADLINE);

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(METRIC_AMOUNT)
            .selectDateDataSetByName(DATE_DATASET_CREATED);

        assertEquals(selectedKpi.getHeadline(), TEST_HEADLINE);

        takeScreenshot(browser, "checkKpiTitlePersistenceWhenMetricChange-" + TEST_HEADLINE, getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndSave() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        int kpisCountAfterAdd = kpisCount + 1;

        waitForFragmentVisible(indigoDashboardsPage)
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_DATASET_CREATED)
                .build())
            .saveEditModeWithWidgets();

        assertEquals(kpisCountAfterAdd, indigoDashboardsPage.getKpisCount());
        assertEquals(kpisCountAfterAdd, initIndigoDashboardsPageWithWidgets().getKpisCount());

        indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).delete();
        indigoDashboardsPage.saveEditModeWithWidgets();

        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkDeleteKpiConfirmAndDiscard() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().getFirstWidget(Kpi.class).delete();

        assertEquals(kpisCount, indigoDashboardsPage.cancelEditModeWithChanges().getKpisCount());
        assertEquals(kpisCount, initIndigoDashboardsPageWithWidgets().getKpisCount());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void testCancelAddingWidget() {
        int kpisCount = initIndigoDashboardsPageWithWidgets().getKpisCount();

        waitForFragmentVisible(indigoDashboardsPage)
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_DATASET_CREATED)
                .build())
            .cancelEditModeWithChanges();

        assertEquals(indigoDashboardsPage.getKpisCount(), kpisCount);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiShowHintForEditableName() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .selectFirstWidget(Kpi.class);

        takeScreenshot(browser, "Kpi does not show hint before hover to headline", this.getClass());
        assertFalse(kpi.hasHintForEditName(), "Kpi shows hint although headline is not hovered");

        String hintColor = kpi.hoverToHeadline();
        takeScreenshot(browser, "Kpi shows hint for editable name when hover to headline", this.getClass());
        assertTrue(hintColor.matches(HINT_FOR_EDIT_NAME_BORDER_COLOR),
                "Hint color not matches! Expected: " + HINT_FOR_EDIT_NAME_BORDER_COLOR + " but actual: " + hintColor);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkMetricWithLongerNameWillBeShortened() {
        createMetric(LONG_NAME_METRIC, "SELECT 1", "#,##0");

        MetricSelect metricSelect = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .dragAddKpiPlaceholder()
                .getConfigurationPanel()
                .getMetricSelect();
        metricSelect.searchForText(LONG_NAME_METRIC);
        takeScreenshot(browser, "Metric with longer name", getClass());
        assertEquals(metricSelect.getValues().size(), 1);

        metricSelect.searchForText(PATTERN_OF_METRIC_NAME);
        assertTrue(metricSelect.isNameShortened(LONG_NAME_METRIC), "The metric still displays full name");

        String metricTooltip = metricSelect.getTooltip(LONG_NAME_METRIC);
        takeScreenshot(browser, "Metric tooltip when partial searching", getClass());
        assertEquals(metricTooltip, LONG_NAME_METRIC);

        metricSelect.searchForText(LONG_NAME_METRIC);
        assertTrue(metricSelect.isNameShortened(LONG_NAME_METRIC), "The metric still displays full name");

        metricTooltip = metricSelect.getTooltip(LONG_NAME_METRIC);
        takeScreenshot(browser, "Metric tooltip when searching", getClass());
        assertEquals(metricTooltip, LONG_NAME_METRIC);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void deleteMetricUsingInKpi() {
        final String deletedMetric = "DELETED_METRIC";
        createMetric(deletedMetric, "SELECT 1", "#,##0");

        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(deletedMetric)
                .dataSet(DATE_DATASET_ACTIVITY)
                .build())
            .saveEditModeWithWidgets();

        initMetricPage().openMetricDetailPage(deletedMetric).deleteObject();

        initIndigoDashboardsPageWithWidgets();
        takeScreenshot(browser, "Dashboards after deleting metric using in Kpi", getClass());

        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().selectLastWidget(Kpi.class);
        takeScreenshot(browser, "Unlisted measure in metric selection", getClass());

        indigoDashboardsPage.getConfigurationPanel().waitForSelectedMetricIsUnlisted();

        indigoDashboardsPage.getLastWidget(Kpi.class).delete();
        indigoDashboardsPage.saveEditModeWithWidgets();
        takeScreenshot(browser, "Dashboards after deleting bad Kpi", getClass());
    }

    private String generateUniqueHeadlineTitle() {
        // create unique headline title which fits into headline title (has limited size)
        return UUID.randomUUID().toString().substring(0, 13);
    }
}
