package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.variable.AbstractVariable;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;

public class VariablesPage extends DataPage {

    @FindBy(css = ".s-btn-create_variable")
    private WebElement createVariableButton;

    @FindBy(id = "variablesTable")
    private ObjectsTable variablesTable;

    public static VariablesPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(VariablesPage.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public String createVariable(AbstractVariable variable) {
        clickCreateVariableButton();

        if (variable instanceof AttributeVariable) 
            return getVariableDetailPage().createFilterVariable((AttributeVariable) variable);
        return getVariableDetailPage().createNumericVariable((NumericVariable) variable);
    }

    public VariableDetailPage openVariableFromList(String variableName) {
        waitForFragmentVisible(variablesTable).selectObject(variableName);
        return getVariableDetailPage();
    }

    public boolean hasVariable(final String variableName) {
        return waitForFragmentVisible(variablesTable)
                .getRows()
                .stream()
                .map(r -> r.findElement(By.className("title")))
                .filter(e -> variableName.equals(e.getText()))
                .findFirst()
                .isPresent();
    }

    public VariablesPage clickCreateVariableButton() {
        waitForElementVisible(createVariableButton).click();
        return this;
    }

    public VariableDetailPage getVariableDetailPage() {
        return VariableDetailPage.getInstance(browser);
    }
}
