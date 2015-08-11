package com.gooddata.qa.graphene.manage;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;

@Test(groups = {"GoodSalesVariables"}, description = "Tests for GoodSales project (create/view and edit variable functionality) in GD platform")
public class GoodSalesVariableTest extends ObjectAbstractTest {

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-variable";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void createNumericVariableTest() {
        initVariablePage();
        variablePage.createVariable(new NumericVariable("Test variable" + System.currentTimeMillis())
                .withDefaultNumber(1234)
                .withUserNumber(5678));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariableDefaultValueTest() {
        variablePage.createVariable(initAttributeVariable());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariableUserValueTest() {
        variablePage.createVariable(initAttributeVariable()
                .withUserSpecificValues());
    }

    private AttributeVariable initAttributeVariable() {
        initVariablePage();
        name = "Test variable" + System.currentTimeMillis();
        return new AttributeVariable(name)
                        .withAttribute("Stage Name")
                        .withAttributeElements("Interest", "Discovery");
    }

    @Override
    public void initObject(String variableName) {
        description = "Test description";
        tagName = "var";
        initVariablePage();
        variablePage.openVariableFromList(variableName);
    }
}