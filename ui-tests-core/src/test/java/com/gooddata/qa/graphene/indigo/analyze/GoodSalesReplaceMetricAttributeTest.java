package com.gooddata.qa.graphene.indigo.analyze;

import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class GoodSalesReplaceMetricAttributeTest extends AnalyticalDesignerAbstractTest {

    private static final String EXPECTED = "Expected";
    private static final String QUOTA = "Quota";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Replace-Metric-Attribute-Test";
    }

    @Test(dependsOnGroups = {"init"}, groups = {"sanity"})
    public void replaceMetricByNewOne() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(NUMBER_OF_ACTIVITIES)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, AMOUNT);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(AMOUNT)));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(NUMBER_OF_ACTIVITIES, AMOUNT)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, EXPECTED);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(EXPECTED, AMOUNT)));

        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.replaceMetric(NUMBER_OF_ACTIVITIES, QUOTA);
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(QUOTA, AMOUNT, EXPECTED)));

        analysisPage.undo();
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(),
                asList(NUMBER_OF_ACTIVITIES, AMOUNT, EXPECTED)));

        analysisPage.redo();
        assertTrue(isEqualCollection(analysisPage.getAllAddedMetricNames(), asList(QUOTA, AMOUNT, EXPECTED)));
        checkingOpenAsReport("replaceMetricByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void replaceAttributeByNewOne() {
        initAnalysePage();
        analysisPage.addCategory(STAGE_NAME);
        assertTrue(isEqualCollection(analysisPage.getAllAddedCategoryNames(), asList(STAGE_NAME)));
        assertTrue(analysisPage.isFilterVisible(STAGE_NAME));

        analysisPage.replaceCategory(ACTIVITY_TYPE);
        assertTrue(isEqualCollection(analysisPage.getAllAddedCategoryNames(), asList(ACTIVITY_TYPE)));
        assertFalse(analysisPage.isFilterVisible(STAGE_NAME));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));

        analysisPage.changeReportType(ReportType.LINE_CHART);
        analysisPage.addStackBy(STAGE_NAME);
        assertEquals(analysisPage.getAddedStackByName(), STAGE_NAME);
        assertTrue(analysisPage.isFilterVisible(STAGE_NAME));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));

        analysisPage.replaceStackBy(DEPARTMENT);
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);
        assertFalse(analysisPage.isFilterVisible(STAGE_NAME));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));
        assertTrue(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.undo();
        assertEquals(analysisPage.getAddedStackByName(), STAGE_NAME);
        assertTrue(analysisPage.isFilterVisible(STAGE_NAME));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));
        assertFalse(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.redo();
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);
        assertFalse(analysisPage.isFilterVisible(STAGE_NAME));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));
        assertTrue(analysisPage.isFilterVisible(DEPARTMENT));
        checkingOpenAsReport("replaceAttributeByNewOne");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchAttributesBetweenAxisAndStackBy() {
        initAnalysePage();
        analysisPage.addCategory(STAGE_NAME).addStackBy(DEPARTMENT);
        assertTrue(isEqualCollection(analysisPage.getAllAddedCategoryNames(), asList(STAGE_NAME)));
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);

        analysisPage.switchAxisAndStackBy();
        assertTrue(isEqualCollection(analysisPage.getAllAddedCategoryNames(), asList(DEPARTMENT)));
        assertEquals(analysisPage.getAddedStackByName(), STAGE_NAME);
        checkingOpenAsReport("switchAttributesBetweenAxisAndStackBy");
    }
}
