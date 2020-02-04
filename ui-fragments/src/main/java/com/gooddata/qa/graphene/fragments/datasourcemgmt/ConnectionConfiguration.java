package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ConnectionConfiguration extends AbstractFragment {
    public static final String CONNECTION_CONFIGURATION_CLASS = "create-or-edit-inner-connection-container";

    @FindBy(className = "gd-input-field")
    protected List<WebElement> input;

    @FindBy(css = ".validation-submit .gd-button-secondary")
    private WebElement validateButton;

    @FindBy(className = "required-message")
    private List<WebElement> requiredMessage;

    @FindBy(className = "gd-message-text")
    private WebElement validateMessage;

    @FindBy(css = ".s-redshift-basic-radio .input-radio")
    private WebElement basic;

    @FindBy(css = ".s-redshift-iam-radio .input-radio")
    private WebElement iam;

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
        addInput("outputStagePrefix", value);
    }

    public void clickValidateButton() {
        waitForElementVisible(validateButton).click();
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

}
