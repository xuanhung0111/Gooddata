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

        analysisPage.getAttributesBucket()
            .changeGranularity("Month");
        analysisPage.waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-x.s-id-" +
                getAttributeDisplayFormIdentifier("Month/Year (Activity)", "Short")), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_use_default_date_granularity_after_switching_and_resetting() {
        beforeEach();

        // switch granularity to non-default value
        analysisPage.getAttributesBucket()
            .changeGranularity("Month");
        analysisPage.resetToBlankState()
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-x.s-id-" +
                getAttributeDisplayFormIdentifier("Year (Activity)")), browser));
    }

    private void beforeEach() {
        // D&D the first metric/attribute to configuration
        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addDate();
    }
}
