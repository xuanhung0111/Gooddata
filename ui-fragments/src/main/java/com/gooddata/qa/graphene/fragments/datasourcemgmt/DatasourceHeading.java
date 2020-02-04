package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DatasourceHeading extends AbstractFragment {
    public static final String DATASOURCE_HEADING_CLASS = "heading";

    @FindBy(className = "datasource-heading-name")
    private WebElement datasourceName;

    @FindBy(className =  "s-edit-datasource-button")
    private WebElement btnEdit;

    @FindBy(className =  "delete-button")
    private WebElement btnDelete;

    public static final DatasourceHeading getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DatasourceHeading.class, waitForElementVisible(className(DATASOURCE_HEADING_CLASS), searchContext));
    }

    public String getName() {
        waitForElementVisible(datasourceName);
        return datasourceName.getText();
    }

    public void clickEditButton() {
        waitForElementVisible(btnEdit);
        btnEdit.click();
    }

    public DeleteDatasourceDialog clickDeleteButton() {
        waitForElementVisible(btnDelete);
        btnDelete.click();
        return DeleteDatasourceDialog.getInstance(browser);
    }
}
