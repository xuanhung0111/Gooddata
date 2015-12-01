package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ErrorStatesTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Error-States-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_too_many_data_points_when_result_is_too_large__413() {
        visitEditor();

        dragFromCatalogue(accountAttr, CATEGORIES_BUCKET);
        expectError(".s-error-missing-metric");
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        expectError(".s-error-too-many-data-points");
        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_error_even_when_there_are_too_many_data_points() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, STACKS_BUCKET);
        expectError(".s-error-missing-metric");
        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_too_many_data_points_when_chart_cannot_be_rendered_because_of_it() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        expectError(".s-error-too-many-data-points");
        expectExportDisabled();

        click(".s-error-too-many-data-points .s-switch-to-table");
        expectFind(".s-visualization-picker .vis-type-table.is-selected");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_no_data_when_result_is_empty() {
        String identifier;
        if (testParams.isReuseProject()) {
            identifier = getMdService().getObj(getProject(), Metric.class, title("__EMPTY__")).getIdentifier();
        } else {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            identifier = createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0")
                    .getIdentifier();
        }

        visitEditor();

        dragFromCatalogue(".s-id-" + identifier.toLowerCase(), METRICS_BUCKET);
        expectError(".s-error-empty-result");
        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_when_configuration_does_not_contain_one() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        expectError(".s-error-missing-metric");
        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_invalid_configuration_error_when_execution_fails() {
        visitEditor();

        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        dragFromCatalogue(activityTypeAttr, METRICS_BUCKET);

        expectError(".s-error-invalid-configuration");
        expectExportDisabled();
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_blank_message_after_reset_from_error_state() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);
        expectError(".s-error-too-many-data-points");

        resetReport();
        expectClean();
    }
}
