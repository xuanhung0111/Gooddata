package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiAlertInMobileTest extends AbstractDashboardTest {

    private static final String KPI_ALERT_RESOURCE = "/kpi-alert-mobile/";
    private static final String UNIQUE_NAME = "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    private static final String DATASET_ID = "dataset.user";
    private Kpi kpi;
    private static final String DATASET_NAME = "User";
    private static final String PADDINGS = "30px";

    private List<String> kpiNames = new ArrayList<>();

    @Override
    public void initProperties() {
        projectTitle = "Kpi-alert-mobile-test"; // use empty project
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        setupMaql(LdmModel.loadFromFile(KPI_ALERT_RESOURCE + "user.maql"));

        // Grey page cannot be accessed on mobile, should use Rest to setup dataset for project here
        setupDataViaRest(DATASET_ID, new FileInputStream(getResourceAsFile(KPI_ALERT_RESOURCE + "user.csv")));

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("number"));
        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class, identifier("user_date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s])", numberFactUri);

        String sumOfNumberMetricUri = getMdService()
                .createObj(getProject(), new Metric(UNIQUE_NAME, maqlExpression, "#,##0"))
                .getUri();

        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        String kpiUri = indigoRestRequest.createKpiWidget(
                new KpiMDConfiguration.Builder()
                        .title(UNIQUE_NAME)
                        .metric(sumOfNumberMetricUri)
                        .dateDataSet(dateDatasetUri)
                        .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                        .comparisonDirection(ComparisonDirection.GOOD)
                        .build());

        indigoRestRequest.createAnalyticalDashboard(singletonList(kpiUri));
    }

    @Test(dependsOnGroups = "createProject", groups = "mobile")
    public void checkAlert() throws JSONException, IOException, URISyntaxException {
        initIndigoDashboardsPageWithWidgets()
                .getWidgetByHeadline(Kpi.class, UNIQUE_NAME)
                .openAlertDialog()
                .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold("5")
                .setAlert();

        // Grey page cannot be accessed on mobile, should use Rest to setup dataset for project here
        setupDataViaRest(DATASET_ID, new FileInputStream(getResourceAsFile(KPI_ALERT_RESOURCE + "user.csv")));

        kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, UNIQUE_NAME);

        takeScreenshot(browser, "Kpi-" + UNIQUE_NAME + "-alert-triggered", getClass());
        assertTrue(kpi.isAlertTriggered(), "Kpi " + UNIQUE_NAME + " alert is not triggered");
    }

    @Test(dependsOnMethods = "checkAlert", groups = "mobile")
    public void checkUIDialogContentTest() {
        initIndigoDashboardsPageWithWidgets();
        KpiAlertDialog kpiAlertDialog = indigoDashboardsPage.selectKpiDashboard("title")
                .getWidgetByHeadline(Kpi.class, UNIQUE_NAME).openAlertDialog();
        takeScreenshot(browser, "Test Dialog Content", KpiAlertInMobileTest.class);
        assertEquals(kpiAlertDialog.getCalculateWidthKPIAlertDialogContent(indigoDashboardsPage.getWidthDashboardSectionKPIs(), PADDINGS), kpiAlertDialog.getAlertDialogContentWidth());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void checkUIDialogContentHasManyWidgetsOnKPITest() throws JSONException, IOException {
        String csvFilePath = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"), new CsvFile.Column("Date"))
                .rows("ABC", "15000", "2015-09-01")
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        Attribute firstNameAttribute = getMdService().getObj(getProject(), Attribute.class, title("Firstname"));
        String firstNameValueUri = getMdService().getAttributeElements(firstNameAttribute)
                .stream().filter(e -> "ABC".equals(e.getTitle())).findFirst().get().getUri();

        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttribute.getUri(), firstNameValueUri);

        List<Metric> metrics = Arrays.asList(
                new Metric(generateUniqueMetricName(), maqlExpression, "[<15000]#,##0.00 $"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[=10000]#,##0 USD"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[>5000]#,##0.0 VND"));

        List<String> kpiUris = new ArrayList<>();
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());

        for (Metric metric : metrics) {
            kpiNames.add(metric.getTitle());
            kpiUris.add(indigoRestRequest.createKpiWidget(
                    new KpiMDConfiguration.Builder()
                            .title(metric.getTitle())
                            .metric(getMdService().createObj(getProject(), metric).getUri())
                            .dateDataSet(dateDatasetUri)
                            .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                            .comparisonDirection(ComparisonDirection.GOOD)
                            .build()));
        }

        indigoRestRequest.createAnalyticalDashboard(kpiUris, "Many Widgets on KPIs");

        for (String kpiName : kpiNames) {
            initIndigoDashboardsPageWithWidgets();
            KpiAlertDialog kpiAlertDialog = indigoDashboardsPage.selectKpiDashboard("Many Widgets on KPIs")
                    .getWidgetByHeadline(Kpi.class, kpiName)
                    .openAlertDialog();
            assertEquals(kpiAlertDialog.getCalculateWidthKPIAlertDialogContent(indigoDashboardsPage.getWidthDashboardSectionKPIs(), PADDINGS), kpiAlertDialog.getAlertDialogContentWidth());
            takeScreenshot(browser, "checkUIDialogContentHasManyWidgetsOnKPI" + generateUniqueMetricName(), KpiAlertInMobileTest.class);
        }
    }

    private String generateUniqueMetricName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
