package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForObjectPageLoaded;

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

    public String createVariable(AbstractVariable var) {
        waitForElementVisible(createVariableButton).click();
        waitForObjectPageLoaded(browser);
        String variableDetailsWindowHandle = browser.getWindowHandle();
        browser.switchTo().window(variableDetailsWindowHandle);
        String uri = "";

        String varName = var.getName();
        if (var instanceof AttributeVariable) {
            AttributeVariable attrVar = (AttributeVariable) var;

            variableDetailPage.createFilterVariable(attrVar);
            openVariableFromList(varName);
            uri = getUri();
            variableDetailPage.verifyAttributeVariable(attrVar);

        } else if (var instanceof NumericVariable) {
            NumericVariable numVar = (NumericVariable) var;

            variableDetailPage.createNumericVariable(numVar);
            openVariableFromList(varName);
            if (numVar.getUserNumber() != Integer.MAX_VALUE) { 
                variableDetailPage.setUserValueNumericVariable(numVar.getUserRole(), numVar.getUserNumber());
                openVariableFromList(varName);
            }
            uri = getUri();
            variableDetailPage.verifyNumericalVariable(numVar);
        }

        return uri;
    }

    public VariableDetailPage openVariableFromList(String variableName) {
        waitForDataPageLoaded(browser);
        waitForFragmentVisible(variablesTable);
        variablesTable.selectObject(variableName);
        waitForObjectPageLoaded(browser);
        return waitForFragmentVisible(variableDetailPage);
    }

    public boolean isVariableVisible(String variableName) {
        waitForDataPageLoaded(browser);
        waitForFragmentVisible(variablesTable);
        return variablesTable.getAllItems().contains(variableName);
    }

    private String getUri() {
        for (String part : browser.getCurrentUrl().split("\\|")) {
            if (part.startsWith("/gdc/md/")) {
                return part;
            }
        }
        return "";
    }
}
