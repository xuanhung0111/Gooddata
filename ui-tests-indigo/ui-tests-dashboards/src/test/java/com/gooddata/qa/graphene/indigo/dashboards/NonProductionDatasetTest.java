package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class NonProductionDatasetTest extends AbstractProjectTest {

    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";
    private static final String PAYROLL_DATASET = "Payroll";

    private static final String FACT_AMOUNT = "Amount";

    private static final String METRIC_AMOUNT_SUM = "Amount Sum";
    private static final String METRIC_DEPARTMENT_COUNT = "Department Count";

    private static final String ATTRIBUTE_DEPARTMENT = "Department";
    private static final String ATTRIBUTE_RECORDS_OF_PAYROLL = "Records of Payroll";

    private static final String DATASET_PAYDATE = "Paydate";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "Non-Production-Dataset-Test";
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void setupFeatureFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void uploadCsvFile() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + PAYROLL_DATASET + "-dataset", getClass());
    }

    @Test(dependsOnMethods = { "uploadCsvFile" }, groups = { "precondition" })
    public void createTestMetrics() {
        final String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));

        getMdService().createObj(getProject(),
                new Metric(METRIC_AMOUNT_SUM,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        Formatter.DEFAULT.toString()));

        final String departmentUri = getMdService()
                .getObjUri(getProject(), Attribute.class, title(ATTRIBUTE_DEPARTMENT));
        final String recordsOfPayrollUri = getMdService()
                .getObjUri(getProject(), Attribute.class, title(ATTRIBUTE_RECORDS_OF_PAYROLL));

        getMdService().createObj(getProject(),
                new Metric(METRIC_DEPARTMENT_COUNT,
                        MetricTypes.COUNT.getMaql()
                                .replaceFirst("__attr__", format("[%s]", departmentUri))
                                .replaceFirst("__attr__", format("[%s]", recordsOfPayrollUri)),
                        Formatter.DEFAULT.toString()));
    }

    @Test(dependsOnGroups = { "precondition" }, groups = { "basic-test" })
    public void testMeasureOptions() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        assertEquals(indigoDashboardsPage.dragAddKpiPlaceholder().getConfigurationPanel().getMetricSelect().getValues(),
                asList(METRIC_AMOUNT_SUM, METRIC_DEPARTMENT_COUNT), "The measure options are not correct");
    }

    @Test(dependsOnGroups = { "precondition" }, groups = { "basic-test" })
    public void testDatasetOptions() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        assertEquals(indigoDashboardsPage.dragAddKpiPlaceholder().getConfigurationPanel()
                .selectMetricByName(METRIC_AMOUNT_SUM).getDataSets(), singletonList(DATASET_PAYDATE));
    }

    @Test(dependsOnGroups = { "basic-test" })
    public void saveKpiUsingNonProductionData() throws JSONException, IOException {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        indigoDashboardsPage.addWidget(new KpiConfiguration.Builder().metric(METRIC_AMOUNT_SUM)
                .dataSet(DATASET_PAYDATE).comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build());

        final String expectedKpiValue = indigoDashboardsPage.selectDateFilterByName("All time")
                .getKpiByHeadline(METRIC_AMOUNT_SUM).getValue();
        indigoDashboardsPage.saveEditModeWithWidgets();
        try {
            takeScreenshot(browser, "Test-Save-Kpi-Using-Non-Production-Data", getClass());
            assertEquals(indigoDashboardsPage.getKpiByHeadline(METRIC_AMOUNT_SUM).getValue(), expectedKpiValue,
                    "The saved kpi value is not correct");
        } finally {
            IndigoRestUtils.deleteAnalyticalDashboard(getRestApiClient(),
                    getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0));
        }
    }
}
