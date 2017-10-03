package com.gooddata.qa.graphene.indigo.analyze.e2e.common;

import static com.gooddata.md.Restriction.title;

import org.testng.annotations.BeforeClass;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public abstract class AbstractAdE2ETest extends AbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void speedUpTestRun() {
        validateAfterClass = false;
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
