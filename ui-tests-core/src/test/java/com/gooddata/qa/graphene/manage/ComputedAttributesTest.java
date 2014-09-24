package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

public class ComputedAttributesTest extends GoodSalesAbstractTest {

    @FindBy(css = ".bucket-values")
    private WebElement bucketValues;

    @FindBy(css = ".buckets .row")
    private List<WebElement> bucketingRows;

    @FindBy(css = ".s-btn-create_computed_attribute")
    private WebElement btnCreateComputedAttribute;

    @FindBy(css = ".s-attributeBucketName")
    private WebElement attributeBucketName;
    
    @FindBy (css = ".param strong a")
    private WebElement modelHref;	
    
    @FindBy (css = ".modelThumbContentImage")
    private WebElement modelImage;

    @Test(dependsOnMethods = { "createProject" }, groups = { "computedAttributeTest" })
    public void createComputedAttribute() throws InterruptedException {
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
        createAttributePage.setComputedAttributeName("Sales Rep Ranking");
        createAttributePage.submit();

        waitForElementVisible(attributeBucketName);
        Screenshots.takeScreenshot(browser, "computed-attribute-details-page", this.getClass());
        
        List<String> expectedBucketNames = Arrays.asList("Poor", "Good", "Great", "Best");
        List<String> expectedBucketRanges = Arrays.asList("# of Won Opps. <= 120", "120 < # of Won Opps. <= 200", "200 < # of Won Opps. <= 250", "250 < # of Won Opps.");
        createAttributePage.checkCreatedComputedAttribute("Sales Rep Ranking", expectedBucketNames, expectedBucketRanges);
        
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
        
        openUrl(PAGE_GDC_PROJECTS + "/" + testParams.getProjectId() +  "/ldm");
        String href = modelHref.getAttribute("href");
        int indexPNG = href.indexOf(".png");
        String imageFileName = href.substring(0, indexPNG+4);
        imageFileName = imageFileName.substring(imageFileName.lastIndexOf("/")+1);
        try{
        	downloadImageFile(href, imageFileName);
        }catch (IOException e) {
        	System.out.println(e);
        }
        verifyLDMModelProject(imageFileName, 185494);
    }

    @Test(dependsOnMethods = { "createComputedAttribute" }, groups = { "computedAttributeTest" })
    public void createReportWithComputedAttribute() throws InterruptedException {
        List<String> expectedAttributeHeader = Arrays.asList("Sales Rep Ranking");
        List<String> expectedAttributeValues = Arrays.asList("Best", "Good", "Great", "Poor");
        List<Float> expectedMetricValues = Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f);
        createReport("Computed Attribute Report", ReportTypes.TABLE,  Arrays.asList("Amount"),  Arrays.asList("Sales Rep Ranking"), "Computed Attribute Report");
        reportPage.saveReport();
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, expectedAttributeHeader, "Attribute name is incorrrect");
        assertEquals(attributeValues, expectedAttributeValues, "Attribute values are incorrrect");
        assertEquals(metricValues, expectedMetricValues, "Metric values are incorrrect");
    }

    @Test(dependsOnGroups = { "computedAttributeTest" }, groups = { "tests" })
    public void endOfTests() {
        successfulTest = true;
    }

    private boolean isCreatedButtonEnabled() {
        return ! btnCreateComputedAttribute.getAttribute("class").contains("disabled");
    }   
    
    private void verifyLDMModelProject(String fileName, long minimalSize) {
        File imageExport = new File(testParams.getDownloadFolder() + fileName);
        System.out.println("imageExport = " + imageExport);
        long fileSize = imageExport.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize >= minimalSize, "Export is probably invalid, check the LDM image manually! Current size is " + fileSize + ", but minimum " + minimalSize + " was expected");
    }
    
    private void downloadImageFile(String href, String filename) throws IOException {
    	URL url = new URL(href);
    	InputStream in = new BufferedInputStream(url.openStream());
    	OutputStream out = new BufferedOutputStream(new FileOutputStream(testParams.getDownloadFolder() + filename));

    	for ( int i; (i = in.read()) != -1; ) {
    	    out.write(i);
    	}
    	
    	out.close();
    	in.close();
    }
}
