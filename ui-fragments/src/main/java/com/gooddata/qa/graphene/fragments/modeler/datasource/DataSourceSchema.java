package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataSourceSchema extends AbstractFragment {
    private static final String DATASOURCE_SCHEMA = "datasource-schema";

    @FindBy(className = "datasource-schema-no-tables")
    private WebElement schemaNoTable;

    @FindBy(className = "schema-name")
    private WebElement schemaName;

    @FindBy(className = "refresh-schema")
    private WebElement refeshSchema;

    public static DataSourceSchema getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceSchema.class, waitForElementVisible(className(DATASOURCE_SCHEMA), searchContext));
    }

    public String getTextNoTableInSchema() {
        return schemaNoTable.getText();
    }

    public DataSourceSchemaContent getSchemaContent() {
        return DataSourceSchemaContent.getInstance(browser);
    }

    public String getTextSchemaName() {
        return schemaName.getText();
    }

    public void clickRefeshSchema() {
        Actions action = new Actions(browser);
        action.moveToElement(schemaName).click().build().perform();
        waitForElementVisible(refeshSchema);
        action.moveToElement(refeshSchema).click().build().perform();
    }
}
