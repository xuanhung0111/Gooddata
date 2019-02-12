package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class ExportLimitationsTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Export-Limitations-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_disable_export_button_if_stacking_and_slicing_by_the_same_attribute() {
        assertFalse(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            // Drag exactly the same attribute to the category and stack
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getPageHeader()
            .isExportButtonEnabled(), "Export button should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_disable_export_if_the_same_metric_is_in_metric_bucket_twice() {
        assertFalse(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getPageHeader()
            .isExportButtonEnabled(), "Export button should be disabled");
    }
}
