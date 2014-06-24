package com.gooddata.qa.graphene.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.VariableTypes;

@Test(groups = {"GoodSalesVariables"}, description = "Tests for GoodSales project (create/view and edit variable functionality) in GD platform")
public class SimpleVariableTest extends ObjectAbstractTest {

    private String attrName;
    private String defaultNumber;
    private String userNumber;
    private Map<String, String> data;
    ArrayList<String> lsAttrElements;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "Simple-variable-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void initialize() throws InterruptedException, JSONException {
        this.attrName = "Stage Name";
        description = "Graphene test on view and modify Variable";
        tagName = "Graphene-test";
        this.defaultNumber = "1234";
        this.userNumber = "5678";
        data = new HashMap<String, String>();
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"object-tests"})
    public void createNumericVariableTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|variables");
        name = "Test variable" + System.currentTimeMillis();
        data.put("variableName", name);
        data.put("defaultNumber", this.defaultNumber);
        data.put("userNumber", this.userNumber);
        variablePage.createVariable(VariableTypes.NUMERIC, data);
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"object-tests"})
    public void createAttributeVariableDefaultValueTest()
            throws InterruptedException {
        initAttributeVariable();
        data.put("userValueFlag", "false");
        variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"object-tests"})
    public void createAttributeVariableUserValueTest()
            throws InterruptedException {
        initAttributeVariable();
        data.put("userValueFlag", "true");
        variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    private void initAttributeVariable() {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|variables");
        name = "Test variable" + System.currentTimeMillis();
        data.put("variableName", name);
        data.put("attribute", this.attrName);
        lsAttrElements = new ArrayList<String>();
        lsAttrElements.add("Interest");
        lsAttrElements.add("Discovery");
        String str = StringUtils.join(lsAttrElements, ", ");
        data.put("attrElements", str);
    }

    @Override
    public void initObject(String variableName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|variables");
        variablePage.openVariableFromList(variableName);
    }
}