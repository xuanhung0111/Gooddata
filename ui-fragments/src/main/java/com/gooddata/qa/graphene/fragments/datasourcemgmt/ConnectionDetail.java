package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ConnectionDetail extends AbstractFragment {
    private static final By CONNECTION_DETAIL_CLASS = By.className("datasource-wrapper");

    @FindBy(className = "provider-icon")
    private WebElement provider;

    @FindBy(xpath = "//label[contains(text(),'Schema')]/following-sibling::strong")
    private WebElement schema ;

    @FindBy(xpath = "//label[contains(text(),'Table prefix')]/following-sibling::strong")
    private WebElement prefix ;

    @FindBy(className = "labeled-input")
    private List<WebElement> labeledInput;

    @FindBy(className = "generate-button")
    private WebElement btnGenerate;

    @FindBy(className =  "delete-button")
    private WebElement btnPublish;

    public static final ConnectionDetail getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ConnectionDetail.class, waitForElementVisible(CONNECTION_DETAIL_CLASS, searchContext));
    }

    protected String getInput(String text) {
        return labeledInput.stream()
                .filter(labeledInput -> labeledInput.findElement(By.tagName("label")).getText().equals(text))
                .map(labeledInput -> labeledInput.findElement(By.tagName("strong")))
                .findFirst()
                .get()
                .getText();
    }

    public GenerateOutputStageDialog getGenerateDialog () {
        return GenerateOutputStageDialog.getInstance(browser);
    }

    public void clickGenerateButton() {
        waitForElementVisible(btnGenerate);
        btnGenerate.click();
    }

    public PublishWorkspaceDialog clickPublishButton() {
        waitForElementVisible(btnPublish);
        btnGenerate.click();
        return PublishWorkspaceDialog.getInstance(browser);
    }

    public PublishWorkspaceDialog getPublishWorkspaceDialog() {
        return PublishWorkspaceDialog.getInstance(browser);
    }

    public PublishModeDialog getPublishModeDialog() {
        return PublishModeDialog.getInstance(browser);
    }

    public String getTextSchema() {
        waitForElementVisible(schema);
        return schema.getText();
    }

    public String getTextPrefix() {
        waitForElementVisible(prefix);
        return prefix.getText();
    }

    //these function use for Bigquery
    public String getTextClientEmail() {
        return getInput("Client e-mail");
    }

    public String getTextProject() {
        return getInput("Project");
    }

    public String getTextDataset() {
        return getInput("Dataset");
    }

    //these function use for Redshift
    public String getTextUserName() {
        return getInput("Username");
    }

    public String getTextDatabaseUser() {
        return getInput("Database user");
    }

    //these function use for Snowflake
    public String getTextWarehouse() {
        return getInput("Warehouse");
    }

    public String getTextUsername() {
        return getInput("Username");
    }

    //these function use for both Snowflake and Redshift
    public String getTextUrl() {
        return getInput("Connection URL");
    }

    public String getTextDatabase() {
        return getInput("Database");
    }
}
