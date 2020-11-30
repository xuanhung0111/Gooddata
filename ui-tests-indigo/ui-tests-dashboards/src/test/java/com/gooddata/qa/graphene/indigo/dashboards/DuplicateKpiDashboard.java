package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.md.Metric;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DuplicateKpiDashboard extends AbstractDashboardTest {
    private IndigoRestRequest indigoRestRequest;
    private static final String INSIGHT_ACTIVITIES = "Insight Activities";
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activities";
    private static final String DASHBOARD_DUPLICATE = "Duplicate Dashboard Activities";

    @Override
    protected void customizeProject() {
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        String insightWidget = createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMdService().getObj(getProject(),
                        Metric.class, title(METRIC_AMOUNT))))));
        indigoRestRequest.createAnalyticalDashboard(singletonList(insightWidget), DASHBOARD_ACTIVITIES);
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addKpi(new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED).build())
                .saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"})
    public void verifySaveAsNewButtonOnViewAndEditMode() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        Boolean saveAsNewOnViewMode = indigoDashboardsPage.openHeaderOptionsButton().isSaveAsNewItemVisible();
        assertTrue(saveAsNewOnViewMode, "Save as new is invisible on view mode");
        Boolean saveAsNewOnEditMode = indigoDashboardsPage.switchToEditMode().openHeaderOptionsButton().isSaveAsNewItemVisible();
        assertTrue(saveAsNewOnEditMode, "Save as new is invisible on edit mode");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"})
    public void saveAsDashboardDialog() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        SaveAsDialog saveAsDialog = indigoDashboardsPage.saveAsDialog();
        assertTrue(saveAsDialog.isCreateDashboardButtonDisplay(), "Create dashboard button is not exist");
        assertTrue(saveAsDialog.isCancelButtonDisplay(), "Cancel button is not exist");
        assertTrue(saveAsDialog.getTitle().equals("Save dashboard as new"), "Title is incorrect");
        assertTrue(saveAsDialog.getNameDialog().equals("Copy of Dashboard Activities"), "Name dialog is incorrect");
        assertTrue(saveAsDialog.getTextContent().equals("Alerts and email schedules will not be duplicated"), "Content text is incorrect");
        saveAsDialog.enterName(DASHBOARD_DUPLICATE).clickSubmitButton();
        String successMessage = DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText();
        assertTrue(successMessage.equals("Great. We saved your dashboard."), "Alert success message is not exist");
        assertEquals(saveAsDialog.getTitleDuplicateDashboard(), DASHBOARD_DUPLICATE, "Title of dashboard is incorrect");
    }
}
