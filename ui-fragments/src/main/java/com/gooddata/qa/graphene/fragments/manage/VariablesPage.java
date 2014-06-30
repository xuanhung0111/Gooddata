package com.gooddata.qa.graphene.fragments.manage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.VariableTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class VariablesPage extends AbstractFragment {

    @FindBy(css = ".s-btn-create_variable")
    private WebElement createVariableButton;

    @FindBy(xpath = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]")
    protected VariableDetailPage variableDetailPage;

    @FindBy(id = "variablesTable")
    private ObjectsTable variablesTable;

    public void createVariable(VariableTypes type, Map<String, String> data)
	    throws InterruptedException {
	String variableName = data.get("variableName");
	waitForElementVisible(createVariableButton).click();
	waitForObjectPageLoaded(browser);
	String variableDetailsWindowHandle = browser.getWindowHandle();
	browser.switchTo().window(variableDetailsWindowHandle);
	switch (type) {
	case ATTRIBUTE:
	    String attribute = data.get("attribute");
	    String attrElements = data.get("attrElements");
	    String userValueSetFlag = data.get("userValueFlag");
	    List<String> lsAttrElements = Arrays.asList(attrElements
		    .split(", "));
	    variableDetailPage.createFilterVariable(attribute, variableName,
		    lsAttrElements, Boolean.valueOf(userValueSetFlag));
	    openVariableFromList(variableName);
	    variableDetailPage.verifyAttributeVariable(lsAttrElements,
		    Boolean.valueOf(userValueSetFlag));
	    break;
	case NUMERIC:
	    String defaultNumber = data.get("defaultNumber");
	    String userNumber = data.get("userNumber");
	    variableDetailPage.createNumericVariable(variableName,
		    Integer.parseInt(defaultNumber));
	    openVariableFromList(variableName);
	    variableDetailPage.setUserValueNumericVariable(Integer
		    .parseInt(userNumber));
	    openVariableFromList(variableName);
	    variableDetailPage.verifyNumericalVariable(defaultNumber,
		    userNumber);
	    break;
	default:
	    break;
	}
    }

    public void openVariableFromList(String variableName) {
	waitForDataPageLoaded(browser);
	waitForElementVisible(variablesTable.getRoot());
	variablesTable.selectObject(variableName);
	waitForObjectPageLoaded(browser);
    }

}
