package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.testng.annotations.Test;

@Test(groups = {"projectSimple"}, description = "Tests for basic project creation in GD platform")
public class SimpleProjectTest extends AbstractProjectTest {

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void dummyTestToSetStatus() throws InterruptedException {
        successfulTest = true;
    }

}