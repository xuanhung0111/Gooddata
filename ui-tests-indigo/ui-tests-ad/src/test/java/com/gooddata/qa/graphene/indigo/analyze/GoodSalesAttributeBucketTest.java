package com.gooddata.qa.graphene.indigo.analyze;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;

public class GoodSalesAttributeBucketTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Bucket-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeByNewOne() {
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(STAGE_NAME);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(STAGE_NAME)));
        assertTrue(filtersBucket.isFilterVisible(STAGE_NAME));

        analysisPage.replaceAttribute(STAGE_NAME, PRODUCT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(PRODUCT)));
        assertFalse(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));

        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.addStack(STAGE_NAME);
        assertEquals(stacksBucket.getAttributeName(), STAGE_NAME);
        assertTrue(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));

        analysisPage.replaceStack(DEPARTMENT);
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
        assertFalse(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), STAGE_NAME);
        assertTrue(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertFalse(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
        assertFalse(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchAttributesBetweenAxisAndStackBy() {
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(STAGE_NAME).addStack(DEPARTMENT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(STAGE_NAME)));
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);

        analysisPage.drag(categoriesBucket.getFirst(), stacksBucket.get());
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(DEPARTMENT)));
        assertEquals(stacksBucket.getAttributeName(), STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
