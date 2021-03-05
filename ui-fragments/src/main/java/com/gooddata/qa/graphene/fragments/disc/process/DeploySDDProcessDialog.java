package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DeploySDDProcessDialog extends AbstractFragment {

    @FindBy(className = "ait-component-selection-dropdown-button")
    private DataSourceDropdown dataSourceDropDown;

    @FindBy(className = "input-text")
    private WebElement clientIdentifierInput;

    @FindBy(className = "datasource-selection-button")
    private WebElement dataSourceTitle;

    @FindBy(css = ".deploy-sdd-process-distribute-data-scope .ait-dataset-selection-radio-all")
    private List<WebElement> scopes;

    @FindBy(css = ".deploy-sdd-process-distribute-data-segment .ait-dataset-selection-radio-all")
    private List<WebElement> segments;

    @FindBy(className = "datasource-selection-button")
    private DataSourceDropdown selectionDataSourceDropDown;

    @FindBy(css = ".csv-datadistribution-path-input .input-text")
    private WebElement inputDataPath;

    public static DeploySDDProcessDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DeploySDDProcessDialog.class,
                waitForElementVisible(className("deploy-process-dialog-area"), context));
    }

    public String getSelectedDataSourceName() {
        return waitForElementVisible(dataSourceTitle).getText();
    }

    public String getSegment() {
        return waitForElementVisible(className("deploy-sdd-process-distribute-data-segment"), getRoot()).getText();
    }

    public DeploySDDProcessDialog selectSegment(String title) {
        By selector = By.cssSelector(".s-" + simplifyText(title));
        ElementUtils.scrollElementIntoView(
                By.cssSelector(".deploy-sdd-process-distribute-data-segment .ember-view.ember-list-view"), selector, browser, 50);
        waitForElementVisible(selector, browser).click();
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

    public DeploySDDProcessDialog selectDataSourceType(String dataSourceTitle) {
        getSelectionDataSourceDropdown().expand().selectDataSource(dataSourceTitle);
        return this;
    }

    public Boolean checkExistingDataSource(String dataSourceTitle) {
        return getDataSourceDropdown().expand().IsDataSourceExist(dataSourceTitle);
    }

    public boolean isSelectedSegment(String title) {
        return waitForElementVisible(By.cssSelector(".s-" + simplifyText(title) + " input"), browser).isSelected();
    }

    private DataSourceDropdown getDataSourceDropdown() {
        return waitForFragmentVisible(dataSourceDropDown);
    }

    public DataSourceDropdown getSelectionDataSourceDropdown() {
        return waitForFragmentVisible(selectionDataSourceDropDown);
    }

    public boolean isS3DatasourceDisabled() {
        return selectionDataSourceDropDown .getRoot().getAttribute("class").contains("disabled");
    }

    public String getTextErrorDatasource() {
        return selectionDataSourceDropDown.getRoot().getText();
    }

    public DeploySDDProcessDialog inputDatasourcePath(String path) {
        waitForElementVisible(inputDataPath).clear();
        inputDataPath.sendKeys(path);
        return this;
    }

    public String getDatasourcePath() {
        return waitForElementVisible(inputDataPath).getAttribute("value");
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
