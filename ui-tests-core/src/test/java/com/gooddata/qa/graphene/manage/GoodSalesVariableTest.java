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
    public void createNumericVariableTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        variablePage.createVariable(new NumericVariable("Test variable" + System.currentTimeMillis())
                .withDefaultNumber(1234)
                .withUserNumber(5678));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariableDefaultValueTest()
            throws InterruptedException {
        variablePage.createVariable(initAttributeVariable());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "object-tests" })
    public void createAttributeVariableUserValueTest()
            throws InterruptedException {
        variablePage.createVariable(initAttributeVariable()
                .withUserSpecificValues());
    }

    private AttributeVariable initAttributeVariable() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        return new AttributeVariable("Test variable" + System.currentTimeMillis())
                        .withAttribute("Stage Name")
                        .withAttributeElements("Interest", "Discovery");
    }

    @Override
    public void initObject(String variableName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        variablePage.openVariableFromList(variableName);
    }
}