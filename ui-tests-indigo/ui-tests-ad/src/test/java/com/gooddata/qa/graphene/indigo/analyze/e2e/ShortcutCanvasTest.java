package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ShortcutCanvasTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Shortcut-Canvas-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_replace_attribute_when_user_drops_it_on_canvas_shortcut() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .drag(analysisPage.getCataloguePanel().searchAndGet(DEPARTMENT, FieldType.ATTRIBUTE),
                    () -> waitForElementPresent(cssSelector(".s-shortcut-metric-attribute"), browser))
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".adi-components .s-id-metricvalues"), browser));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(DEPARTMENT));
    }
}
