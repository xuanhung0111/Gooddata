package com.gooddata.qa.graphene.project;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Test(groups = {"projectSimple"}, description = "Tests for basic project functionality in GD platform")
public class SimpleProjectTest extends AbstractProjectTest {

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void dummyTestToSetStatus() throws InterruptedException {
        successfulTest = true;
    }

}