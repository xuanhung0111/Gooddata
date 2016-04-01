package com.gooddata.qa.graphene.indigo.analyze.e2e.common;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import com.gooddata.qa.graphene.common.StartPageContext;
import org.testng.annotations.BeforeClass;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.indigo.analyze.AnalyticalDesignerAbstractTest;
import org.testng.annotations.Test;

public abstract class AbstractAdE2ETest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void speedUpTestRun() {
        validateAfterClass = false;
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void initStartPage() {
        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                waitForFragmentVisible(analysisPage);
            }

            @Override
            public String getStartPage() {
                return PAGE_UI_ANALYSE_PREFIX.replace("${analyze.resource}", testParams.getIndigoAnalyzeResource()) +
                        testParams.getProjectId() + "/reportId/edit";
            }
        };
    }

    protected String getAttributeDisplayFormIdentifier(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title))
                .getDefaultDisplayForm()
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }

    protected String getAttributeDisplayFormIdentifier(String title, final String partialSummary) {
        return getMdService().getObj(getProject(), Attribute.class, title(title))
                .getDisplayForms()
                .stream()
                .filter(form -> form.getSummary().contains(partialSummary))
                .findFirst()
                .get()
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }
}
