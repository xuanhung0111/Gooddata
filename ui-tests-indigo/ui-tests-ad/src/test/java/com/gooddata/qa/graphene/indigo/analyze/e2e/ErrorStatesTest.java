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
        analysisPage.addAttribute(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_error_even_when_there_are_too_many_data_points() {
        analysisPage.addStack(ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_too_many_data_points_when_chart_cannot_be_rendered_because_of_it() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addStack(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());

        waitForElementVisible(cssSelector(".s-error-too-many-data-points .s-switch-to-table"), browser).click();
        analysisPage.waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-visualization-picker .vis-type-table.is-selected"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_no_data_when_result_is_empty() {
        if (!testParams.isReuseProject()) {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
        }
        analysisPage.addMetric("__EMPTY__")
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-empty-result"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_missing_metric_when_configuration_does_not_contain_one() {
        analysisPage.addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-missing-metric"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_invalid_configuration_error_when_execution_fails() {
        analysisPage.addStack(ACCOUNT)
            .addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".s-error-invalid-configuration"), browser));
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_blank_message_after_reset_from_error_state() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addStack(ACCOUNT)
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-error-too-many-data-points"), browser));

        analysisPage.resetToBlankState();
    }
}
