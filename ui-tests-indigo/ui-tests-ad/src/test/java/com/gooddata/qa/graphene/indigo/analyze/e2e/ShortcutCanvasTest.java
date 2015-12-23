package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ShortcutCanvasTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Shortcut-Canvas-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_replace_attribute_when_user_drops_it_on_canvas_shortcut() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        drag(departmentAttr, ".s-shortcut-metric-attribute");

        expectFind(".adi-components .s-id-metricvalues");
        expectMissing(".adi-components " + activityTypeAttrLabel);
        expectFind(".adi-components " + departmentAttrLabel);
    }
}
