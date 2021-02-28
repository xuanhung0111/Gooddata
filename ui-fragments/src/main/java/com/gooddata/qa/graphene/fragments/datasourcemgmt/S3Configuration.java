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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;

public class S3Configuration extends AbstractFragment {
    public static final String S3_CONFIGURATION = "create-or-edit-inner-s3-connection-container";

    @FindBy(className = "gd-input-field")
    protected List<WebElement> input;

    @FindBy(className = "labeled-input")
    private List<WebElement> s3ParameterInput;

    @FindBy(className = "generic-parameter-value-field")
    private WebElement parameterValueField;

    @FindBy(className = "generic-parameter-secure-value-field")
    private WebElement parameterSecureValueField;

    @FindBy(className = "generic-parameters-empty-container")
    private WebElement emptyParameters;

    @FindBy(className = "s3-connection-form-bucket")
    private WebElement bucketField;

    @FindBy(className = "s3-connection-form-server-side-encryption")
    private WebElement formEncryption;

    @FindBy(className = "validation-submit-message")
    private WebElement validationMessage;

    @FindBy(className = "provider-label-value")
    private WebElement datasourceType;

    @FindBy(className = "s-test_connection")
    private WebElement validateButton;

    @FindBy(className = "gd-message-text")
    private WebElement validateMessage;

    public static final S3Configuration getInstance(SearchContext context) {
        return Graphene.createPageFragment(S3Configuration.class, waitForElementVisible(className(S3_CONFIGURATION), context));
    }

    protected void addInput(String nameElement, String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(input.stream().filter(input -> input.getAttribute("name")
                .equals(nameElement)).findFirst().get()).click().keyDown(Keys.CONTROL).sendKeys("a")
                .keyUp(Keys.CONTROL).sendKeys(Keys.DELETE).sendKeys(value).build().perform();
    }

    public String getDatasourceType() {
        return waitForElementVisible(datasourceType).getText();
    }

    public String getPlaceHolderBucketField() {
        return waitForElementVisible(bucketField).getAttribute("placeholder");
    }

    public void addBucket(String value) {
        addInput("bucket", value);
    }

    public void addAccessKey(String value) {
        addInput("accessKey", value);
    }

    public void addSecretKey(String value){
        addInput("secretKey", value);
    }

    public void addRegion(String value){
        addInput("region", value);
    }

    public void checkOnEncryption() {
        waitForElementVisible(formEncryption).click();
    }

    public boolean isEncryptionChecked() {
       return waitForElementVisible(formEncryption).isSelected();
    }

    public WebElement getS3ParameterInfoElement(String parameterName) {
        WebElement keyParamElement = s3ParameterInput.stream().filter(el -> el.findElement(By.tagName("label"))
                .getText().equals(parameterName)).findFirst().get();
        return keyParamElement;
    }

    public String getErrorMessageOnS3DatasourceParamLine(String parameterName) {
        return getS3ParameterInfoElement(parameterName).findElement(By.className("required-message")).getText();
    }

    public void clickValidateButton() {
        Graphene.waitGui()
                .until(ExpectedConditions.elementToBeClickable(validateButton))
                .click();
    }

    public String getValidateMessage() {
        waitForElementVisible(validateMessage);
        return validateMessage.getText();
    }
}
