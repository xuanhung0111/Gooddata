package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DeploySDDProcessDialog extends AbstractFragment {

    @FindBy(className = "deploy-sdd-process-create-datasource-help")
    private WebElement manageDataSourceButton;

    @FindBy(className = "ait-component-selection-dropdown-button")
    private DataSourceDropdown dataSourceDropDown;

    @FindBy(className = "input-text")
    private WebElement clientIdentifierInput;

    @FindBy(className = "button-text")
    private WebElement dataSourceTitle;

    @FindBy(css = ".deploy-sdd-process-distribute-data-scope .ait-dataset-selection-radio-all")
    private List<WebElement> scopes;

    @FindBy(css = ".deploy-sdd-process-distribute-data-segment .ait-dataset-selection-radio-all")
    private List<WebElement> segments;

    public static DeploySDDProcessDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DeploySDDProcessDialog.class,
                waitForElementVisible(className("deploy-process-dialog-area"), context));
    }

    public void manageDataSource() {
        waitForElementVisible(manageDataSourceButton).click();
    }

    public String getRedirectedPageFromManageDataSource() {
        return waitForElementVisible(manageDataSourceButton.findElement(By.className("action-important-link")))
                .getAttribute("href");
    }

    public String getSelectedDataSourceName() {
        return waitForElementVisible(dataSourceTitle).getText();
    }

    public String getSegment() {
        return waitForElementVisible(className("deploy-sdd-process-distribute-data-segment"), getRoot()).getText();
    }

    public DeploySDDProcessDialog selectSegment(String title) {
        segments.stream()
                .filter(dataProduct -> dataProduct.getAttribute("value").contains(title))
                .findFirst()
                .get()
                .click();
        return this;
    }

    public DeploySDDProcessDialog selectScope(Scope scope) {
        scopes.stream()
                .filter(dataProduct -> dataProduct.getAttribute("value").contains(scope.getTitle()))
                .findFirst()
                .get()
                .click();
        return this;
    }

    public DeploySDDProcessDialog selectDataSource(String dataSourceTitle) {
        getDataSourceDropdown().expand().selectDataSource(dataSourceTitle);
        return this;
    }

    private DataSourceDropdown getDataSourceDropdown() {
        return waitForFragmentVisible(dataSourceDropDown);
    }

    public enum Scope {

        CURRENT_PROJECT("currentproject"),
        SEGMENT("segment");

        private String title;

        private Scope(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
