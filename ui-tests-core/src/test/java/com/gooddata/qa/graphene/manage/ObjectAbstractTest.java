package com.gooddata.qa.graphene.manage;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

public abstract class ObjectAbstractTest extends GoodSalesAbstractTest {

    protected static String name = "";
    protected static String description = "";
    protected static String tagName = "";

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void changeNameTest() throws InterruptedException {
        initObject(name);
        name = objectDetailPage.changeObjectName(name + "changed");
    }

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void addDescriptionTest() throws InterruptedException {
        initObject(name);
        objectDetailPage.addDescription(description);
    }

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void addTagTest() throws InterruptedException {
        initObject(name);
        objectDetailPage.addTag(tagName);
        objectDetailPage.addTag("graphene test adding tag");
    }

    @Test(dependsOnGroups = {"property-object-tests"}, groups = {"final-tests"})
    public void verifyAllPropertiesTest() {
        initObject(name);
        objectDetailPage.verifyAllPropertiesAtOnce(name, description, tagName);
    }

    @Test(dependsOnGroups = {"final-tests"}, groups = {"tests"})
    public void finalTest() {
        successfulTest = true;
    }

    public void initObject(String variableName) {

    }
}