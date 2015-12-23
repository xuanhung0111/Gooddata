package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class EmptyCatalogueTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Empty-Catalogue-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_finish_loading_when_there_are_no_metrics_attributes_or_facts() {
        visitEditor();

        expectFind(".s-catalogue-loaded");
    }
}
