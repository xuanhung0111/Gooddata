package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

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

        Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, UNIQUE_NAME);

        takeScreenshot(browser, "Kpi-" + UNIQUE_NAME + "-alert-triggered", getClass());
        assertTrue(kpi.isAlertTriggered(), "Kpi " + UNIQUE_NAME + " alert is not triggered");
    }
}
