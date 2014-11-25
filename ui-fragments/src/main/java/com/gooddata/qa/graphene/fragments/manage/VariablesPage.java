package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForObjectPageLoaded;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.variable.AbstractVariable;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class VariablesPage extends AbstractFragment {

    @FindBy(css = ".s-btn-create_variable")
    private WebElement createVariableButton;

    @FindBy(xpath = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]")
    protected VariableDetailPage variableDetailPage;

    @FindBy(id = "variablesTable")
    private ObjectsTable variablesTable;

    public void createVariable(AbstractVariable var) throws InterruptedException {
        waitForElementVisible(createVariableButton).click();
        waitForObjectPageLoaded(browser);
        String variableDetailsWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(variableDetailsWindowHandle);

        String varName = var.getName();
        if (var instanceof AttributeVariable) {
            AttributeVariable attrVar = (AttributeVariable) var;

            variableDetailPage.createFilterVariable(attrVar);
            openVariableFromList(varName);
            variableDetailPage.verifyAttributeVariable(attrVar);
            return;
        }

        if (var instanceof NumericVariable) {
            NumericVariable numVar = (NumericVariable) var;

            variableDetailPage.createNumericVariable(numVar);
            openVariableFromList(varName);
            variableDetailPage.setUserValueNumericVariable(numVar.getUserNumber());
            openVariableFromList(varName);
            variableDetailPage.verifyNumericalVariable(numVar);
        }
    }

    public void openVariableFromList(String variableName) {
        waitForDataPageLoaded(browser);
        waitForFragmentVisible(variablesTable);
        variablesTable.selectObject(variableName);
        waitForObjectPageLoaded(browser);
    }

}
