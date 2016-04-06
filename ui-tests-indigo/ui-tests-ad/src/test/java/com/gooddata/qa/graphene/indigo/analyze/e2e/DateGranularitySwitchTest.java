package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class DateGranularitySwitchTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Date-Granularity-Switch-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_change_date_granularity_appropriately() {
        beforeEach();

        analysisPageReact.getAttributesBucket()
            .changeGranularity("Month");
        analysisPageReact.waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-x.s-id-" +
                getAttributeDisplayFormIdentifier("Month/Year (Activity)", "Short")), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_use_default_date_granularity_after_switching_and_resetting() {
        beforeEach();

        // switch granularity to non-default value
        analysisPageReact.getAttributesBucket()
            .changeGranularity("Month");
        analysisPageReact.resetToBlankState()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-x.s-id-" +
                getAttributeDisplayFormIdentifier("Year (Activity)")), browser));
    }

    private void beforeEach() {
        // D&D the first metric/attribute to configuration
        analysisPageReact.changeReportType(ReportType.COLUMN_CHART)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addDate();
    }
}
