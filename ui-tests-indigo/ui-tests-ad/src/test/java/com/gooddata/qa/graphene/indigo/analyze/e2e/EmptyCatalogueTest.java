package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.openqa.selenium.By.cssSelector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class EmptyCatalogueTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        // create empty project using default name
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Empty-Catalogue-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_finish_loading_when_there_are_no_metrics_attributes_or_facts() {
        waitForElementPresent(cssSelector(".s-catalogue-loaded"), browser);
    }
}
