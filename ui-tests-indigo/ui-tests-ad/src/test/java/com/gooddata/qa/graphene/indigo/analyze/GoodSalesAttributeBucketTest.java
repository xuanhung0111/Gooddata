package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesAttributeBucketTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Bucket-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeByNewOne() {
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();
        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();

        analysisPageReact.addAttribute(ATTR_STAGE_NAME);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_STAGE_NAME)));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));

        analysisPageReact.replaceAttribute(ATTR_STAGE_NAME, ATTR_PRODUCT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_PRODUCT)));
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));

        analysisPageReact.changeReportType(ReportType.LINE_CHART);
        analysisPageReact.addStack(ATTR_STAGE_NAME);
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));

        analysisPageReact.replaceStack(ATTR_DEPARTMENT);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchAttributesBetweenAxisAndStackBy() {
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();
        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();

        analysisPageReact.addAttribute(ATTR_STAGE_NAME).addStack(ATTR_DEPARTMENT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_STAGE_NAME)));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPageReact.drag(categoriesBucket.getFirst(), stacksBucket.get());
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_DEPARTMENT)));
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
