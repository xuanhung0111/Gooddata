package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;

public class ConnectionConfiguration extends AbstractFragment {
    public static final String CONNECTION_CONFIGURATION_CLASS = "create-or-edit-inner-connection-container";
    private static final String KEY_PARAMETER = "generic-parameter-key-field";
    private static final String KEY_SECURE_PARAMETER = "generic-parameter-secure-key-field";
    private static final By ADDING_COMPONENT = By.className("generic-parameter-adding");
    private static final By PARAMETER_INPUT = By.className("generic-parameter-input");

    @FindBy(className = "gd-input-field")
    protected List<WebElement> input;

    @FindBy(className = "s-test_connection")
    private WebElement validateButton;

    @FindBy(className = "has-error")
    private List<WebElement> requiredMessage;

    @FindBy(className = "gd-message-text")
    private WebElement validateMessage;

    @FindBy(css = ".s-redshift-basic-radio .input-radio")
    private WebElement basic;

    @FindBy(css = ".s-redshift-iam-radio .input-radio")
    private WebElement iam;

    @FindBy(className = "output-stage-section")
    private WebElement outputStageSection;

    @FindBy(className = "provider-label-value")
    private WebElement datasourceType;

    @FindBy(className = "s-add_parameter")
    private WebElement addParameterBtn;

    @FindBy(className = "s-add_secure_parameter")
    private WebElement addSecureParameterBtn;

    @FindBy(className = "icon-trash")
    private WebElement trashIcon;

    @FindBy(className = "generic-parameter-input")
    private List<WebElement> parameterInput;

    @FindBy(className = KEY_PARAMETER)
    private WebElement parameterKeyField;

    @FindBy(className = "generic-parameter-value-field")
    private WebElement parameterValueField;

    @FindBy(className = KEY_SECURE_PARAMETER)
    private WebElement parameterSecureKeyField;

    @FindBy(className = "generic-parameter-secure-value-field")
    private WebElement parameterSecureValueField;

    @FindBy(className = "generic-parameters-empty-container")
    private WebElement emptyParameters;

    public static final ConnectionConfiguration getInstance(SearchContext context) {
        return Graphene.createPageFragment(ConnectionConfiguration.class, waitForElementVisible(className(CONNECTION_CONFIGURATION_CLASS), context));
    }

    protected void addInput(String nameElement, String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(input.stream()
                .filter(input -> input.getAttribute("name").equals(nameElement))
                .findFirst()
                .get()).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE).sendKeys(value).build().perform();

    }

    // these function use for both Snowflake and Redshift
    public void addUrl( String value) {
        addInput("connectionURL", value);

    }

    public void addDatabase(String value) {
        addInput("database", value);
    }

    // these function use for Snowflake
    public void addWarehouse(String value) {
        addInput("warehouse", value);
    }

    public void addUsername(String value) {
        addInput("username", value);
    }

    public void addPassword(String value){
        addInput("password", value);
    }

    // these function use for Bigquery
    public void addClientEmail(String value) {
        addInput("clientEmail", value);
    }

    public void addPrivateKey(String value) { addInput("privateKey", value);}

    public void addProject(String value) {
        addInput("project", value);
    }

    public void addDataset(String value) {
        addInput("dataset", value);
    }

    //these function use for Redshift
    public void addBasicInfo(String uname, String pwd) {
        basic.click();
        addInput("username", uname);
        addInput("password", pwd);
    }

    public void addIAMInfo(String dbuser , String access, String secret) {
        iam.click();
        addInput("databaseUser", dbuser);
        addInput("accessKeyId", access);
        addInput("secretAccessKey", secret);
    }

    //these function use for all cloud resources
    public void addSchemaOutputStage(String value) {
        addInput("schema", value);
    }

    public void addPrefix(String value) {
        outputStageSection.click();
        addInput("outputStagePrefix", value);
    }

    public void clickValidateButton() {
        Graphene.waitGui()
                .until(ExpectedConditions.elementToBeClickable(validateButton))
                .click();
    }

    public int getNumberOfRequiredMessage() {
        waitForCollectionIsNotEmpty(requiredMessage);
        return requiredMessage.size();
    }

    public String getValidateMessage() {
        waitForElementVisible(validateMessage);
        return validateMessage.getText();
    }

    public void addBigqueryInfo(String clientemail, String privatekey, String project,
                                String dataset, String prefix) {
        addClientEmail(clientemail);
        addPrivateKey(privatekey);
        addProject(project);
        addDataset(dataset);
        addPrefix(prefix);
    }

    public void addSnowflakeInfo(String url, String warehouse, String username, String password,
                                  String database, String prefix, String schema) {
        addUrl(url);
        addUsername(username);
        addPassword(password);
        addDatabase(database);
        addWarehouse(warehouse);
        addPrefix(prefix);
        addSchemaOutputStage(schema);
    }

    public void addRedshiftBasicInfo(String url, String username, String password, String database
            , String prefix, String schema) {
        addUrl(url);
        addBasicInfo(username, password);
        addDatabase(database);
        addPrefix(prefix);
        addSchemaOutputStage(schema);
    }

    public void addRedshiftIAMInfo(String url, String dbuser, String accesskey, String secretkey,
                                   String database, String prefix, String schema) {
        addUrl(url);
        addIAMInfo(dbuser, accesskey, secretkey);
        addDatabase(database);
        addPrefix(prefix);
        addSchemaOutputStage(schema);
    }

    // ========= These function use for Generic Datasource ============

    public String getDatasourceType() {
        return waitForElementVisible(datasourceType).getText();
    }

    public ConnectionConfiguration clickAddParameterButton() {
        waitForElementVisible(addParameterBtn).click();
        return this;
    }

    public ConnectionConfiguration clickAddSecureParameterButton() {
        waitForElementVisible(addSecureParameterBtn).click();
        return this;
    }

    public boolean isAddingComponentDisplayed() {
        return isElementVisible(ADDING_COMPONENT, this.getRoot()) && addParameterBtn.isDisplayed()
                && addSecureParameterBtn.isDisplayed();
    }

    public boolean isNewLineParameterDisplayed() {
        return isElementVisible(PARAMETER_INPUT, this.getRoot()) && isElementVisible(parameterKeyField) &&
            isElementVisible(parameterValueField);
    }

    public boolean isNewLineSecureParameterDisplayed() {
        return isElementVisible(PARAMETER_INPUT, this.getRoot()) && isElementVisible(parameterSecureKeyField) &&
                isElementVisible(parameterSecureValueField);
    }

    public ConnectionConfiguration clickOnTrash() {
        waitForElementVisible(trashIcon).click();
        return this;
    }

    public ConnectionConfiguration inputOnlyAddParameter(String key) {
        waitForElementVisible(parameterKeyField).clear();
        parameterKeyField.sendKeys(key);
        return this;
    }

    public ConnectionConfiguration inputAddParameter(String key, String value) {
        inputOnlyAddParameter(key);
        waitForElementVisible(parameterValueField).clear();
        parameterValueField.sendKeys(value);
        return this;
    }

    public ConnectionConfiguration inputOnlyAddSecureParameter(String key) {
        waitForElementVisible(parameterSecureKeyField).clear();
        parameterSecureKeyField.sendKeys(key);
        return this;
    }

    public ConnectionConfiguration inputAddSecureParameter(String key, String value) {
        inputOnlyAddSecureParameter(key);
        waitForElementVisible(parameterSecureValueField).clear();
        parameterSecureValueField.sendKeys(value);
        return this;
    }

    public List<String> getParameterValue(String inputText) {
        String key = waitForElementVisible(parameterKeyField).getAttribute(inputText);
        String value = waitForElementVisible(parameterValueField).getAttribute(inputText);
        return asList(key, value);
    }

    public List<String> getPlaceHolderParameterInput() {
        return getParameterValue("placeholder");
    }

    public List<String> getCurrentParameterValue() {
        return getParameterValue("value");
    }

    public List<String> getSecureParameterValue(String inputText) {
        String key = waitForElementVisible(parameterSecureKeyField).getAttribute(inputText);
        String value = waitForElementVisible(parameterSecureValueField).getAttribute(inputText);
        return asList(key, value);
    }

    public List<String> getPlaceHolderSecureParameterInput() {
        return getSecureParameterValue("placeholder");
    }

    public List<String> getCurrentSecureParameterInput() {
        return getSecureParameterValue("value");
    }

    public int countTrashIcon() {
        return browser.findElements(By.className("icon-trash")).size();
    }

    public WebElement getParamInfo(String parameterName, String keyField) {
        return parameterInput.stream().filter(el -> el.findElement(By.className(keyField))
                .getAttribute("value").equals(parameterName)).findFirst().get();
    }

    public void deleteParameterValue(String parameterName) {
        getParamInfo(parameterName, KEY_PARAMETER).findElement(By.className("icon-trash")).click();
    }

    public void deleteSecureParameter(String parameterName) {
        getParamInfo(parameterName, KEY_SECURE_PARAMETER).findElement(By.className("icon-trash")).click();
    }

    public boolean isEmptyParameter() {
        return isElementVisible(emptyParameters);
    }

    public String getErrorMessageOnParamLine(int rowLine) {
        getActions().moveToElement(parameterInput.get(rowLine)).build().perform();
        return waitForElementVisible(By.className("required-message"), browser).getText();
    }
}
