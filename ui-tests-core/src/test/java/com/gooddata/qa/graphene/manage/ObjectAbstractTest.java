package com.gooddata.qa.graphene.manage;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

public abstract class ObjectAbstractTest extends GoodSalesAbstractTest {

    protected static String name = "";
    protected static String description = "";
    protected static String tagName = "";

    @Test(dependsOnGroups = { "object-tests" }, groups = { "property-object-tests" })
    public void changeNameTest() throws InterruptedException {
	initObject(name);
	String newName = name + "changed";
	name = objectDetailPage.changeObjectName(newName);
    }

    @Test(dependsOnGroups = { "object-tests" }, groups = { "property-object-tests" })
    public void addDescriptionTest() throws InterruptedException {
	initObject(name);
	objectDetailPage.addDescription(description);
    }

    @Test(dependsOnGroups = { "object-tests" }, groups = { "property-object-tests" })
    public void addTagTest() throws InterruptedException {
	initObject(name);
	objectDetailPage.addTag(tagName);
	String tagNameWithSpaces = "graphene test adding tag";
	objectDetailPage.addTag(tagNameWithSpaces);
    }

    @Test(dependsOnGroups = { "property-object-tests" }, groups = { "final-tests" })
    public void verifyAllPropertiesTest() {
	initObject(name);
	objectDetailPage.verifyAllPropertiesAtOnce(name, description, tagName);
    }
}
