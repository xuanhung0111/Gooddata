package com.gooddata.qa.graphene.indigo.analyze;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CategoriesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;

public class GoodSalesReplaceMetricAttributeTest extends AnalyticalDesignerAbstractTest {

    private static final String EXPECTED = "Expected";
    private static final String REMAINING_QUOTA = "Remaining Quota";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Replace-Metric-Attribute-Test";
    }

    @Test(dependsOnGroups = {"init"}, groups = {"sanity"})
    public void replaceMetricByNewOne() {
        initAnalysePage();
        final MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(AMOUNT)));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(NUMBER_OF_ACTIVITIES, AMOUNT)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, EXPECTED);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(), asList(EXPECTED, AMOUNT)));

        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, REMAINING_QUOTA);
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));

        analysisPage.undo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.redo();
        assertTrue(isEqualCollection(metricsBucket.getItemNames(),
                asList(REMAINING_QUOTA, AMOUNT, EXPECTED)));
        checkingOpenAsReport("replaceMetricByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeByNewOne() {
        initAnalysePage();
        final CategoriesBucket categoriesBucket = analysisPage.getCategoriesBucket();
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
        assertEquals(stacksBucket.getAddedStackByName(), STAGE_NAME);
        assertTrue(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));

        analysisPage.replaceStack(DEPARTMENT);
        assertEquals(stacksBucket.getAddedStackByName(), DEPARTMENT);
        assertFalse(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.undo();
        assertEquals(stacksBucket.getAddedStackByName(), STAGE_NAME);
        assertTrue(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertFalse(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.redo();
        assertEquals(stacksBucket.getAddedStackByName(), DEPARTMENT);
        assertFalse(filtersBucket.isFilterVisible(STAGE_NAME));
        assertTrue(filtersBucket.isFilterVisible(PRODUCT));
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchAttributesBetweenAxisAndStackBy() {
        initAnalysePage();
        final CategoriesBucket categoriesBucket = analysisPage.getCategoriesBucket();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.addAttribute(STAGE_NAME).addStack(DEPARTMENT);
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(STAGE_NAME)));
        assertEquals(stacksBucket.getAddedStackByName(), DEPARTMENT);

        analysisPage.drag(categoriesBucket.getFirst(), stacksBucket.get());
        assertTrue(isEqualCollection(categoriesBucket.getItemNames(), asList(DEPARTMENT)));
        assertEquals(stacksBucket.getAddedStackByName(), STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
