package com.gooddata.qa.graphene.manage;

import com.gooddata.qa.CssUtils;
import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.openqa.selenium.By;

public class ComputedAttributesTest extends GoodSalesAbstractTest {

    @FindBy(css = ".bucket-values")
    private WebElement bucketValues;

    @FindBy(css = ".buckets .row")
    private List<WebElement> bucketingRows;

    @FindBy(css = ".s-btn-create_computed_attribute")
    private WebElement btnCreateComputedAttribute;

    @FindBy(css = ".s-attributeBucketName")
    private WebElement attributeBucketName;
    
    @FindBy (css = ".modelThumbContentImage")
    private WebElement modelImage;

    @FindBy (css = ".s-btn-delete")
    private WebElement btnDelete;

    private static final String COMPUTED_ATTRIBUTE_NAME = "Sales Rep Ranking";

    private static final String EXPECTED_DELETE_DESCRIPTION = "To delete this computed attribute you must delete its data set. Go to its Data Set administration page and click Delete.";

    @Test(dependsOnMethods = { "createProject" })
    public void createComputedAttribute() {
        initAttributePage();
        attributePage.createAttribute();

        waitForElementVisible(btnCreateComputedAttribute);
        assertFalse(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        createAttributePage.selectAttribute("Sales Rep");
        assertFalse(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        createAttributePage.selectMetric("# of Won Opps.");
        assertEquals(waitForElementVisible(bucketValues).getText(), "# of Won Opps.");
        assertTrue(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        assertEquals(bucketingRows.size(), 3);
        createAttributePage.addBucket();
        assertEquals(bucketingRows.size(), 4);
        createAttributePage.setBucket(0, "Poor", "120");
        createAttributePage.setBucket(1, "Good", "200");
        createAttributePage.setBucket(2, "Great", "250");
        createAttributePage.setBucket(3, "Best");
        createAttributePage.setComputedAttributeName(COMPUTED_ATTRIBUTE_NAME);
        Screenshots.takeScreenshot(browser, "computed-attribute-creation-page", this.getClass());
        createAttributePage.submit();

        waitForElementVisible(attributeBucketName);
        Screenshots.takeScreenshot(browser, "computed-attribute-details-page", this.getClass());

        List<String> expectedBucketNames = Arrays.asList("Poor", "Good", "Great", "Best");
        List<String> expectedBucketRanges = Arrays.asList("# of Won Opps. <= 120", "120 < # of Won Opps. <= 200", "200 < # of Won Opps. <= 250", "250 < # of Won Opps.");
        createAttributePage.checkCreatedComputedAttribute(COMPUTED_ATTRIBUTE_NAME, expectedBucketNames, expectedBucketRanges);

        // check delete button is disabled right after computed attribute creation
        checkDeleteButtonAndInfo();
    }
    
    @Test(dependsOnMethods = { "createComputedAttribute" })
    public void checkOthersPageAfterComputedAttributeCreated() throws InterruptedException, ParseException, IOException, JSONException {
    	initAttributePage();
        Screenshots.takeScreenshot(browser, "attribute-list", this.getClass());
        
        initModelPage();
        String src = modelImage.getAttribute("src");
        for(int i = 0; i < 10 &&  src.equals(""); i++) {
        	Thread.sleep(1000);
        	src = modelImage.getAttribute("src");
        }
        //time for the ldm image loaded from src. 
        Thread.sleep(4000);
        Screenshots.takeScreenshot(browser, "project-model", this.getClass());
        
    	verifyLDMModelProject(185494);
    }

    @Test(dependsOnMethods = { "createComputedAttribute" })
    public void checkAttributePageAfterComputedAttributeCreated() throws InterruptedException {
        initAttributePage();
        By computedAttributeItem = By.cssSelector(".s-title-" + CssUtils.simplifyText(COMPUTED_ATTRIBUTE_NAME) + " a");
        waitForElementVisible(computedAttributeItem, browser).click();
        // check after fresh attribute load
        checkDeleteButtonAndInfo();
    }

    @Test(dependsOnMethods = { "createComputedAttribute" })
    public void createReportWithComputedAttribute() throws InterruptedException {
        List<String> expectedAttributeHeader = Arrays.asList(COMPUTED_ATTRIBUTE_NAME);
        List<String> expectedAttributeValues = Arrays.asList("Best", "Good", "Great", "Poor");
        List<Float> expectedMetricValues = Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f);
        createReport(new ReportDefinition().withName("Computed Attribute Report")
                                           .withWhats("Amount")
                                           .withHows(COMPUTED_ATTRIBUTE_NAME),
                    "Computed Attribute Report");
        reportPage.saveReport();
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, expectedAttributeHeader, "Attribute name is incorrrect");
        assertEquals(attributeValues, expectedAttributeValues, "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, expectedMetricValues, "Metric values are incorrrect");
    }

    private boolean isCreatedButtonEnabled() {
        return ! btnCreateComputedAttribute.getAttribute("class").contains("disabled");
    }

    // check that delete button is disabled and that there's expected explanation message
    private void checkDeleteButtonAndInfo() {
        assertTrue(attributeDetailPage.isDeleteButtonDisabled(), "Delete Button is Disabled");
        assertEquals(attributeDetailPage.getDeleteButtonDescription(), EXPECTED_DELETE_DESCRIPTION);
    }
}
