package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesAttributeBucketTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Bucket-Test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceAttributeByNewOne() {
        final AttributesBucket categoriesBucket = initAnalysePage().getAttributesBucket();
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(ATTR_STAGE_NAME);
        assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter should display");

        analysisPage.replaceAttribute(ATTR_STAGE_NAME, ATTR_PRODUCT);
        assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_PRODUCT));
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter shouldn't display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT), ATTR_PRODUCT + " filter should display");

        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.addStack(ATTR_STAGE_NAME);
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter should display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT), ATTR_PRODUCT + " filter should display");

        analysisPage.replaceStack(ATTR_DEPARTMENT);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter shouldn't display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT), ATTR_PRODUCT + " filter should display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter should display");

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter should display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT), ATTR_PRODUCT + " filter should display");
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter shouldn't display");

        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME), ATTR_STAGE_NAME + " filter shouldn't display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT), ATTR_PRODUCT + " filter should display");
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter should display");
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchAttributesBetweenAxisAndStackBy() {
        final AttributesBucket categoriesBucket = initAnalysePage().getAttributesBucket();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(ATTR_STAGE_NAME).addStack(ATTR_DEPARTMENT);
        assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_STAGE_NAME));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.drag(categoriesBucket.getFirst(), stacksBucket.get());
        assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_DEPARTMENT));
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
