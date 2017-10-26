package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog.ElementType;

public class MetricEditorTest extends AbstractProjectTest {

    private static final String PAYROLL_CSV_PATH = "/" + PAYROLL_CSV + "/payroll.csv";
    private static final String PAYROLL_DATASET = "Payroll";
    private static final String RECORD_OF_PAYROLL = "Records of Payroll";

    @Override
    protected void initProperties() {
        // use empty project
        projectTitle = "Metric-Editor-Improvement-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + PAYROLL_DATASET + "-dataset", getClass());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRecordOfDatasetOnAttributeValues() {
        assertTrue(
                initMetricPage().openMetricEditor().clickCustomMetricLink()
                        .selectElementType(ElementType.ATTRIBUTE_VALUES).waitForElementsLoading()
                        .selectElement(RECORD_OF_PAYROLL).isEmptyMessagePresent(),
                "The empty message is not displayed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRecordOfDatasetOnAttributeLabels() {
        assertEquals(
                initMetricPage().openMetricEditor().clickCustomMetricLink()
                        .selectElementType(ElementType.ATTRIBUTE_LABELS).waitForElementsLoading()
                        .selectElement(RECORD_OF_PAYROLL).getNoDataMessage(),
                "There are no attribute labels", "The no label message is not displayed");
    }
}
