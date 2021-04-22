package com.gooddata.qa.graphene.fragments.disc.process;

import org.jboss.arquillian.graphene.Graphene;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;

public class DataSourceDialog extends AbstractFragment {

    private static final String DATASOURCE_AUTH_DETAIL = "ait-data-source-fragment";

    @FindBy(className = "data-source-detail-item")
    private List<WebElement> itemDatasource;

    @FindBy(className = "data-source-connection-validation-button")
    private WebElement btnDataSourceValidation;

    @FindBy(className = "datasource-provider-dropdown-button")
    private WebElement dataSourceProviderButton;

    @FindBy(className = "datasource-provider-selection-dropdown")
    private WebElement dataSourceProviderDropDown;

    @FindBy(className = "fail")
    private WebElement errorMessage;

    @FindBy(className = "success")
    private WebElement successMessage;

    @FindBy(className = "has-error")
    private List<WebElement> requiredFieldList;

    @FindBy(css = "input[value='basic']")
    private WebElement basic;

    @FindBy(css = "input[value='IAM']")
    private WebElement iam;

    @FindBy(css = ".bigquery-private-key-textarea-editor-expand > textarea")
    private WebElement privateKey;

    @FindBy(className = "datasource-provider-dropdown-button")
    private DatasourceProviderDropDown datasourceProviderDropdown;

    @FindBy(xpath = "//h3[contains(text(),'Output Stage Prefix')]/following-sibling::div[1]/input")
    private WebElement outputStagePrefixInput;

    @FindBy(xpath = "//h3[contains(text(),'Data Source Name')]/following-sibling::div[1]/input")
    private WebElement dataSourceNameInput;

    @FindBy(className = "ait-data-source-confirm-btn")
    private WebElement btnConfirm;

    public static final DataSourceDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceDialog.class, waitForElementVisible(className(DATASOURCE_AUTH_DETAIL), searchContext));
    }

    protected DataSourceDialog addText(String inputName, String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(itemDatasource.stream()
                .map(item -> item.findElement(By.xpath(format("//label[text()='%s']/following-sibling::div[1]/input",inputName))))
                .findFirst()
                .get()).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE).sendKeys(value).build().perform();
        return this;
    }

    protected DataSourceDialog addSecretkey(String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(privateKey).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .sendKeys(Keys.DELETE).sendKeys(value).build().perform();
        return this;
    }

    protected String getValue(String inputName) {
        return  itemDatasource.stream()
                .map(item -> item.findElement(By.xpath(format("//label[contains(text(),'%s')]/following-sibling::div[1]/input", inputName))))
                .findFirst()
                .get().getAttribute("value");
    }

    public DataSourceDialog clickValidateButton() {
        waitForElementVisible(btnDataSourceValidation);
        btnDataSourceValidation.click();
        return this;
    }

    public DataSourceDialog clickConfirmButton() {
        waitForElementVisible(btnConfirm);
        btnConfirm.click();
        return this;
    }

    public DataSourceDialog waitForDropDownProviderDisable() {
        waitForElementNotVisible(dataSourceProviderButton);
        return this;
    }

    public DataSourceDialog addDatasourceName(String name) {
        waitForElementVisible(dataSourceNameInput).clear();
        dataSourceNameInput.sendKeys(name);
        return this;
    }

    public DataSourceDialog addOutputStagePrefix(String name) {
        waitForElementVisible(outputStagePrefixInput).clear();
        outputStagePrefixInput.sendKeys(name);
        return this;
    }

    public String getErrorMessage() {
        waitForElementVisible(errorMessage);
        return errorMessage.getText();
    }

    public String getSuccessMessage() {
        waitForElementVisible(successMessage);
        return successMessage.getText();
    }

    public int getNumberOfRequiredMessage() {
        waitForCollectionIsNotEmpty(requiredFieldList);
        return requiredFieldList.size();
    }

    public DataSourceDialog selectDatasourceProvider(DataSourceDialog.DatasourceProvider dataSourceType) {
        getDatasourceDropdown()
                .expand()
                .selectDatasource(dataSourceType.getValue());
        return this;
    }

    private DatasourceProviderDropDown getDatasourceDropdown() {
        return waitForFragmentVisible(datasourceProviderDropdown);
    }

    public DataSourceDialog addSnowflakeInfo(String connectionUrl, String username, String password, String database,
                                                 String schema, String warehouse) {
        waitForElementVisible(this.getRoot().findElement(By.className("snowflake-data-source-detail-area")));
        addText("Connection URL", connectionUrl);
        addText("Username", username);
        addText("Password", password);
        addText("Database", database);
        addText("Schema", schema);
        addText("Warehouse", warehouse);
        return this;
    }

    public DataSourceDialog addBigquerryInfo(String clientEmail, String privateKey, String project, String dataset) {
        waitForElementVisible(this.getRoot().findElement(By.className("bigquery-data-source-detail-area")));
        addText("Client E-mail", clientEmail);
        addSecretkey(privateKey);
        addText("Project", project);
        addText("Dataset", dataset);
        return this;
    }

    public DataSourceDialog addBasicRedshiftInfo(String connnectionUrl, String username, String password,
                                                     String database, String schema) {
        waitForElementVisible(this.getRoot().findElement(By.className("redshift-data-source-basic-auth-detail-area")));
        addText("Connection URL", connnectionUrl);
        addText("Username", username);
        addText("Password", password);
        addText("Database", database);
        addText("Schema", schema);
        return this;
    }

    public DataSourceDialog addIAMRedshiftInfo(String connnectionUrl, String dbuser, String accesskey,
                                                   String secretkey, String database, String schema) {
        // default redshift information is basic auth
        waitForElementVisible(this.getRoot().findElement(By.className("redshift-data-source-basic-auth-detail-area")));
        //Change to IAM auth
        waitForElementVisible(iam).click();
        waitForElementVisible(this.getRoot().findElement(By.className("redshift-data-source-iam-auth-detail-area")));
        addText("Connection URL", connnectionUrl);
        addText("Database user", dbuser);
        addText("Access key ID", accesskey);
        addText("Secret access key", secretkey);
        addText("Database", database);
        addText("Schema", schema);
        return this;
    }

    public String getTextPrefix() {
        waitForElementVisible(outputStagePrefixInput);
        return outputStagePrefixInput.getAttribute("value");
    }

    public String getTextDataSourceName() {
        waitForElementVisible(dataSourceNameInput);
        return dataSourceNameInput.getAttribute("value");
    }

    //these function use for Bigquery
    public String getTextClientEmail() {
        return getValue("Service account email");
    }

    public String getTextProject() {
        return getValue("Google project ID");
    }

    public String getTextDataset() {
        return getValue("Dataset");
    }

    //these function use for Redshift
    public String getTextUserName() {
        return getValue("Username");
    }

    public String getTextDatabaseUser() {
        return getValue("Database user");
    }

    public String getTextAccessKey() {
        return getValue("Access key ID");
    }

    //these function use for Snowflake
    public String getTextWarehouse() {
        return getValue("Warehouse");
    }

    //these function use for both Snowflake and Redshift
    public String getTextUrl() {
        return getValue("Connection URL");
    }

    public String getTextDatabase() {
        return getValue("Database");
    }

    public String getTextSchema() {
        return getValue("Schema");
    }

    public enum DatasourceProvider {
        SNOWFLAKE("snowflake"),
        REDSHIFT("amazon_redshift"),
        BIGQUERRY("google_bigquery");

        private String value;

        private DatasourceProvider(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
}

