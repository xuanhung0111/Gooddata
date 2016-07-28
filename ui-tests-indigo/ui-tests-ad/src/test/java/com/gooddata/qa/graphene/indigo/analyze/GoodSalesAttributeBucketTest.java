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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesAttributeBucketTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Bucket-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeByNewOne() {
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(ATTR_STAGE_NAME);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_STAGE_NAME)));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));

        analysisPage.replaceAttribute(ATTR_STAGE_NAME, ATTR_PRODUCT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_PRODUCT)));
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));

        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.addStack(ATTR_STAGE_NAME);
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));

        analysisPage.replaceStack(ATTR_DEPARTMENT);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_STAGE_NAME));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_PRODUCT));
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchAttributesBetweenAxisAndStackBy() {
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(ATTR_STAGE_NAME).addStack(ATTR_DEPARTMENT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_STAGE_NAME)));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.drag(categoriesBucket.getFirst(), stacksBucket.get());
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(ATTR_DEPARTMENT)));
        assertEquals(stacksBucket.getAttributeName(), ATTR_STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
