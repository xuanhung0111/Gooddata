package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;

public class CreateAttributePage extends AbstractFragment {

    @FindBy(css = ".computed-attribute-name input")
    private WebElement computedAttributeName;

    @FindBy(css = ".s-name")
    private WebElement computedAttributeNameInViewMode;

    @FindBy(css = ".computed-attribute-submit")
    private WebElement submitButton;

    @FindBy(css = "div#p-objectPage h1 a span")
    private WebElement backDataPageLink;

    @FindBy(css = ".computed-attribute-attribute")
    private WebElement attributeButton;

    @FindBy(css = ".computed-attribute-metric")
    private WebElement metricButton;

    @FindBy(css = ".dropdown-attribute")
    private DropDown attributeDropDown;

    @FindBy(css = ".dropdown-metric")
    private DropDown metricDropDown;

    @FindBy(css = ".bucketing")
    private WebElement bucketing;

    @FindBy(css = ".s-attributeBucketRow")
    private List<WebElement> bucketTableRows;

    @FindBy(css = ".s-btn-add_bucket")
    private WebElement addBucketButton;

    @FindBy(css = ".buckets .row")
    private List<WebElement> bucketingRows;

    private static final By bubleContentLocator = By.cssSelector(".bubble-content .content");

    private static final By bucketNameLocator = By.cssSelector(".row-item .bucket-name input");

    private static final By bucketToLocator = By.cssSelector(".row-item .bucket-range input");

    private static final By attributeBucketNameLocator = By.cssSelector(".s-attributeBucketName");

    private static final By attributeBucketRangeLocator = By.cssSelector(".s-attributeBucketRange");

    public void selectAttribute(String name) {
        waitForElementVisible(attributeButton).click();
        attributeDropDown.searchAndSelectItem(name);
    }

    public void selectMetric(String name) {
        waitForElementVisible(metricButton).click();
        metricDropDown.searchAndSelectItem(name);
    }

    public void addBucket() {
        addBucketButton.click();
    }

    public void setBucket(int index, String name, String value) {
        setBucketName(index, name);
        setBucketTo(index, value);
    }

    public void setBucket(int index, String name) {
        setBucketName(index, name);
    }
    
    public String getBubleText() {
        return waitForElementVisible(bubleContentLocator, browser).getText();
    }

    public void setComputedAttributeName(String name) {
        waitForElementVisible(computedAttributeName).sendKeys(name);
    }

    public void submit() {
        waitForElementVisible(submitButton).click();
    }

    public void cancel() {
        waitForElementVisible(backDataPageLink).click();
    }

    private WebElement getBucketElement(int index) {
        return bucketingRows.get(index);
    }

    private WebElement getBucketNameElement(int index) {
        return getBucketElement(index).findElement(bucketNameLocator);
    }

    private WebElement getBucketToElement(int index) {
        return getBucketElement(index).findElement(bucketToLocator);
    }

    private void setBucketName(int index, String name) {
        WebElement bucketNameElement = getBucketNameElement(index);
        bucketNameElement.clear();
        bucketNameElement.sendKeys(name);
    }

    private void setBucketTo(int index, String value) {
        WebElement bucketToElement = getBucketToElement(index);
        bucketToElement.clear();
        bucketToElement.sendKeys(value);
    }

    public void checkCreatedComputedAttribute(String attributeName, List<String> expectedBucketNames, List<String> expectedBucketRanges) {
        waitForElementVisible(attributeBucketNameLocator, browser);
        assertEquals(computedAttributeNameInViewMode.getText(), attributeName);
        for (int i = 0; i < expectedBucketNames.size(); i++) {
            assertEquals(bucketTableRows.get(i).findElement(attributeBucketNameLocator).getText(), expectedBucketNames.get(i), "Wrong bucket name");
            assertEquals(bucketTableRows.get(i).findElement(attributeBucketRangeLocator).getText(), expectedBucketRanges.get(i), "Wrong bucket range");
        }
    }
}
