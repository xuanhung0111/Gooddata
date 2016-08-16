package com.gooddata.qa.graphene.manage;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;

public abstract class ObjectAbstractTest extends GoodSalesAbstractTest {

    protected String name = "";
    protected String description = "";
    protected String tagName = "";

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void changeNameTest() {
        name = initObject(name).changeObjectName(name + "changed");
    }

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void addDescriptionTest() {
        initObject(name).addDescription(description);
    }

    @Test(dependsOnGroups = {"object-tests"}, groups = {"property-object-tests"})
    public void addTagTest() {
        ObjectPropertiesPage objectDetailPage = initObject(name);
        objectDetailPage.addTag(tagName);
        objectDetailPage.addTag("graphene test adding tag");
    }

    @Test(dependsOnGroups = {"property-object-tests"})
    public void verifyAllPropertiesTest() {
        initObject(name).verifyAllPropertiesAtOnce(name, description, tagName);
    }

    public abstract ObjectPropertiesPage initObject(String variableName);
}