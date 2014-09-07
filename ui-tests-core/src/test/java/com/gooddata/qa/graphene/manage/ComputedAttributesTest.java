package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.ReportTypes;

public class ComputedAttributesTest extends GoodSalesAbstractTest {

    @FindBy(css = ".bucket-values")
    private WebElement bucketValues;

    @FindBy(css = ".buckets .row")
    private List<WebElement> bucketingRows;

    @FindBy(css = ".s-btn-create_computed_attribute")
    private WebElement btnCreateComputedAttribute;

    @FindBy(css = ".s-btn-select_drill_attribute")
    private WebElement btnSelectDrillAttribute;

    @Test(dependsOnMethods = { "createProject" }, groups = { "computedAttributeTest" })
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
        createAttributePage.setComputedAttributeName("Sales Rep Ranking");
        createAttributePage.submit();

        waitForElementVisible(btnSelectDrillAttribute);
        List<String> expectedBucketNames = Arrays.asList("Poor", "Good", "Great", "Best");
        List<String> expectedBucketRanges = Arrays.asList("# of Won Opps. <= 120", "120 < # of Won Opps. <= 200", "200 < # of Won Opps. <= 250", "250 < # of Won Opps.");
        createAttributePage.checkCreatedComputedAttribute("Sales Rep Ranking", expectedBucketNames, expectedBucketRanges);
    }

    @Test(dependsOnMethods = { "createComputedAttribute" }, groups = { "computedAttributeTest" })
    public void createReportWithComputedAttribute() throws InterruptedException {
        List<String> expectedAttributeHeader = Arrays.asList("Sales Rep Ranking");
        List<String> expectedAttributeValues = Arrays.asList("Best", "Good", "Great", "Poor");
        List<Float> expectedMetricValues = Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f);
        createReport("Computed Attribute Report", ReportTypes.TABLE,  Arrays.asList("Amount"),  Arrays.asList("Sales Rep Ranking"), "Computed Attribute Report");
        reportPage.saveReport();
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
}
