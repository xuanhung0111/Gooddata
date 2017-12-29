package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

public class GoodSalesManageObjectsTest extends ManageObjectsAbstractTest {

    private static final List<String> METRICSLIST = Arrays.asList( 
            METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_AMOUNT, METRIC_AMOUNT_BOP, 
            METRIC_BEST_CASE_BOP, METRIC_EXPECTED_PERCENT_OF_GOAL, METRIC_PROBABILITY_BOP, METRIC_WON, METRIC_CLOSE_EOP, 
            METRIC_SNAPSHOT_BOP, METRIC_SNAPSHOT_EOP, METRIC_TIMELINE_BOP, METRIC_TIMELINE_EOP);
    private static final List<String> VARIABLESLIST = Arrays.asList(VARIABLE_QUOTA, VARIABLE_STATUS );

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-manage-objects";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountBOPMetric();
        metricCreator.createBestCaseBOPMetric();
        metricCreator.createProbabilityBOPMetric();
        metricCreator.createCloseEOPMetric();
        metricCreator.createExpectedPercentOfGoalMetric();
        metricCreator.createNumberOfOpenOppsMetric();

        getVariableCreator().createQuoteVariable();
        getVariableCreator().createStatusVariable();
        initMetricPage().getObjectFolder().addFolder("metrics", "_System", null);
    }
    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "fact"})
    public void viewFactsTable() {
        viewSortObjectsTable(ObjectTypes.FACT, factsList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("Days to Close", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Duration", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ObjectTypes.FACT, taggedObjects, tagsMap, Arrays.asList("Days to Close"));
        checkObjectLinkInTable(ObjectTypes.FACT, "Opp. Close (Date)");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjects", "attribute"})
    public void viewAttributesTable() {
        viewSortObjectsTable(ObjectTypes.ATTRIBUTE, attributesList);
        List<String> filteredObjectsList = Arrays.asList("Day of Week (Mon-Sun) (Activity)",
                "Day of Week (Mon-Sun) (Closed)", "Day of Week (Mon-Sun) (Created)",
                "Day of Week (Mon-Sun) (Snapshot)", "Day of Week (Mon-Sun) (Timeline)");
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("day", Arrays.asList("2", "17px"));
        tagsMap.put("eu", Arrays.asList("3", "15px"));
        this.filterObjectByTagList(ObjectTypes.ATTRIBUTE, filteredObjectsList, tagsMap);
        List<String> filteredObjectsCloud = Arrays.asList("Week (Mon-Sun) (Activity)",
                "Week (Mon-Sun) (Closed)", "Week (Mon-Sun) (Created)", "Week (Mon-Sun) (Snapshot)",
                "Week (Mon-Sun) (Timeline)");
        tagsMap.put("year", Arrays.asList("7", "19px"));
        this.filterObjectByTagCloud(ObjectTypes.ATTRIBUTE, filteredObjectsCloud, tagsMap);
        checkObjectLinkInTable(ObjectTypes.ATTRIBUTE, "Day of Week (Mon-Sun) (Activity)");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "metric"})
    public void viewMetricsTable() {
        viewSortObjectsTable(ObjectTypes.METRIC, METRICSLIST);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put(METRIC_NUMBER_OF_OPEN_OPPS, Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put(METRIC_EXPECTED_PERCENT_OF_GOAL, Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ObjectTypes.METRIC, taggedObjects, tagsMap, Arrays.asList(METRIC_NUMBER_OF_OPEN_OPPS));
        checkObjectLinkInTable(ObjectTypes.METRIC, METRIC_NUMBER_OF_OPPORTUNITIES);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"viewObjetcs", "variable"})
    public void viewVariablesTable() {
        openObjectsTable(ObjectTypes.VARIABLE)
            .assertTableHeader()
            .sortObjectsTable(ObjectsTable.SORT_DESC, VARIABLESLIST)
            .sortObjectsTable(ObjectsTable.SORT_ASC, VARIABLESLIST);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put(VARIABLE_QUOTA, Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put(VARIABLE_STATUS, Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ObjectTypes.VARIABLE, taggedObjects, tagsMap, Arrays.asList(VARIABLE_QUOTA));
        checkObjectLinkInTable(ObjectTypes.VARIABLE, VARIABLE_STATUS);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "fact"})
    public void moveFactsBetweenFolders() {
        List<String> movedObjects = Arrays.asList(FACT_AMOUNT, FACT_DAYS_TO_CLOSE, FACT_DURATION, FACT_PROBABILITY, 
                FACT_VELOCITY, FACT_TIMELINE_DATE);
        moveObjectsBetweenFolders(ObjectTypes.FACT, movedObjects, "Stage History");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "attribute"})
    public void moveAttributesBetweenFolders() {
        List<String> movedObjects = Arrays.asList(ATTR_ACTIVITY, ATTR_PRIORITY, ATTR_PRODUCT, ATTR_REGION);
        moveObjectsBetweenFolders(ObjectTypes.ATTRIBUTE, movedObjects, "Activity");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1, groups = {"moveObjects", "metric"})
    public void moveMetricsBetweenFolders() {
        List<String> movedObjects = Arrays.asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE_BOP, METRIC_PROBABILITY_BOP, 
                METRIC_CLOSE_EOP);
        moveObjectsBetweenFolders(ObjectTypes.METRIC, movedObjects, "_System");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "fact"})
    public void deleteFacts() {
        List<String> deletedObjects = Arrays.asList(FACT_AMOUNT, FACT_DAYS_TO_CLOSE, FACT_DURATION, FACT_PROBABILITY, 
                FACT_VELOCITY, FACT_TIMELINE_DATE);
        deleteObjectsTable(deletedObjects, factsList, ObjectTypes.FACT);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "attribute"})
    public void deleteAttributes() {
        List<String> deletedObjects = Arrays.asList(ATTR_ACTIVITY, ATTR_DEPARTMENT,
                ATTR_MONTH_OF_QUARTER_CREATED, ATTR_PRIORITY, ATTR_PRODUCT, ATTR_REGION);
        deleteObjectsTable(deletedObjects, attributesList, ObjectTypes.ATTRIBUTE);
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2, groups = {"deleteObjects", "metric"})
    public void deleteMetrics() {
        List<String> deletedObjects = Arrays.asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE_BOP, METRIC_PROBABILITY_BOP, 
                METRIC_CLOSE_EOP);
        deleteObjectsTable(deletedObjects, METRICSLIST, ObjectTypes.METRIC);
    }
}