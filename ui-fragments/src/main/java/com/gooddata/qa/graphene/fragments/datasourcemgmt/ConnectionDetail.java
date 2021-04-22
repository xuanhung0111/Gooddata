package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ConnectionDetail extends AbstractFragment {
    private static final By CONNECTION_DETAIL_CLASS = By.className("datasource-wrapper");
    private static final int TIMEOUT_WAIT_CONTENT_LOADED = 5 * 60;

    @FindBy(className = "provider-icon")
    private WebElement provider;

    @FindBy(xpath = "//label[contains(text(),'Schema')]/following-sibling::div")
    private WebElement schema ;

    @FindBy(xpath = "//label[contains(text(),'Table prefix')]/following-sibling::div")
    private WebElement prefix ;

    @FindBy(className = "labeled-input")
    private List<WebElement> labeledInput;

    @FindBy(className = "generate-button")
    private WebElement btnGenerate;

    @FindBy(className = "s-open-publish-button")
    private WebElement openPublishIntoWorkspace;

    @FindBy(className = "s-open-in-disc-link")
    private WebElement openDiscLink;

    @FindBy(className = "publish-loading-modal-container")
    private static WebElement loadingModal;

    @FindBy(className = "user-heading")
    private UserHeading userHeading;

    @FindBy(className = "user-field")
    private UserField userField;

    public static final ConnectionDetail getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ConnectionDetail.class, waitForElementVisible(CONNECTION_DETAIL_CLASS, searchContext));
    }

    protected String getInput(String text) {
        return labeledInput.stream()
                .filter(labeledInput -> labeledInput.findElement(By.tagName("label")).getText().equals(text))
                .map(labeledInput -> labeledInput.findElement(By.tagName("div")))
                .findFirst()
                .get()
                .getText();
    }

    public PublishModeDialog getPublishModeDialog() {
        return PublishModeDialog.getInstance(browser);
    }

    public PublishResult getPublishResultDialog() {
        return PublishResult.getInstance(browser);
    }

    public UserHeading getUserHeading() {
        return UserHeading.getInstance(browser);
    }

    public UserField getUserField() {
        return UserField.getInstance(browser);
    }

    public ConnectionDetail waitLoadingModelPage() {
        waitForElementNotVisible(loadingModal, TIMEOUT_WAIT_CONTENT_LOADED);
        return this;
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
        return getInput("Service account email");
    }

    public String getTextProject() {
        return getInput("Google project ID");
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
