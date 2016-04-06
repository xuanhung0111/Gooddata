package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ErrorStatesTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Error-States-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_too_many_data_points_when_result_is_too_large__413() {
        analysisPageReact.addAttribute(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));

        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_error_even_when_there_are_too_many_data_points() {
        analysisPageReact.addStack(ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_too_many_data_points_when_chart_cannot_be_rendered_because_of_it() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addStack(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());

        waitForElementVisible(cssSelector(".s-error-too-many-data-points .s-switch-to-table"), browser).click();
        analysisPageReact.waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-picker .vis-type-table.is-selected"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_no_data_when_result_is_empty() {
        if (!testParams.isReuseProject()) {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
        }
        analysisPageReact.addMetric("__EMPTY__")
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-empty-result"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_when_configuration_does_not_contain_one() {
        analysisPageReact.addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_invalid_configuration_error_when_execution_fails() {
        analysisPageReact.addStack(ACCOUNT)
            .addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".s-error-invalid-configuration"), browser));
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_blank_message_after_reset_from_error_state() {
        analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addStack(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));

        analysisPageReact.resetToBlankState();
    }
}
