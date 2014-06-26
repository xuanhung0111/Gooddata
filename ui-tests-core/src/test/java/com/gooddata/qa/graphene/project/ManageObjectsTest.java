package com.gooddata.qa.graphene.project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

@Test(groups = {"ManageObjects"}, description = "Tests for list, filter and sort objects in GD platform")
public class ManageObjectsTest extends ManageObjectsAbstractTest {

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-manage-objects-tests";
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"viewObjetcs", "fact"})
    public void viewFactsTable() throws InterruptedException {
        viewSortObjectsTable(ui.factsTable, ObjectTypes.FACT, factsList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("Days to Close", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Duration", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ui.factsTable, taggedObjects, tagsMap, Arrays.asList("Days to Close"));
        checkObjectLinkInTable(ui.factsTable, "Opp. Close (Date)");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"viewObjects",
            "attribute"})
    public void viewAttributesTable() throws InterruptedException {
        viewSortObjectsTable(ui.attributesTable, ObjectTypes.ATTRIBUTE, attributesList);
        List<String> filteredObjectsList = Arrays.asList("Day of Week (Mon-Sun) (Activity)",
                "Day of Week (Mon-Sun) (Closed)", "Day of Week (Mon-Sun) (Created)",
                "Day of Week (Mon-Sun) (Snapshot)", "Day of Week (Mon-Sun) (Timeline)");
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("day", Arrays.asList("2", "17px"));
        tagsMap.put("eu", Arrays.asList("3", "15px"));
        this.filterObjectByTagList(ui.attributesTable, filteredObjectsList, tagsMap);
        List<String> filteredObjectsCloud = Arrays.asList("Week (Mon-Sun) (Activity)",
                "Week (Mon-Sun) (Closed)", "Week (Mon-Sun) (Created)", "Week (Mon-Sun) (Snapshot)",
                "Week (Mon-Sun) (Timeline)");
        tagsMap.put("year", Arrays.asList("7", "19px"));
        this.filterObjectByTagCloud(ui.attributesTable, filteredObjectsCloud, tagsMap);
        checkObjectLinkInTable(ui.attributesTable, "Day of Week (Mon-Sun) (Activity)");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"viewObjetcs", "metric"})
    public void viewMetricsTable() throws InterruptedException {
        viewSortObjectsTable(ui.metricsTable, ObjectTypes.METRIC, metricsList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("# of Open Opps.", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Expected % of Goal", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ui.metricsTable, taggedObjects, tagsMap, Arrays.asList("# of Open Opps."));
        checkObjectLinkInTable(ui.metricsTable, "# of Opportunities [BOP]");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"viewObjetcs",
            "variable"})
    public void viewVariablesTable() throws InterruptedException {
        openObjectsTable(ui.variablesTable, ObjectTypes.VARIABLE);
        ui.variablesTable.assertTableHeader();
        ui.variablesTable.sortObjectsTable(ObjectsTable.SORT_DESC, variablesList);
        ui.variablesTable.sortObjectsTable(ObjectsTable.SORT_ASC, variablesList);
        Map<String, List<String>> taggedObjects = new HashMap<String, List<String>>();
        taggedObjects.put("Quota", Arrays.asList("newtag1", "newtag2"));
        taggedObjects.put("Status", Arrays.asList("newtag1"));
        Map<String, List<String>> tagsMap = new HashMap<String, List<String>>();
        tagsMap.put("newtag1", Arrays.asList("1", "40px"));
        tagsMap.put("newtag2", Arrays.asList("2", "15px"));
        this.checkTagObjects(ui.variablesTable, taggedObjects, tagsMap, Arrays.asList("Quota"));
        checkObjectLinkInTable(ui.variablesTable, "Status");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"moveObjects", "fact"})
    public void moveFactsBetweenFolders() throws InterruptedException {
        List<String> movedObjects = Arrays.asList("Duration", "Velocity");
        moveObjectsBetweenFolders(ObjectTypes.FACT, ui.factsTable, movedObjects, "2", "Stage History");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"moveObjects",
            "attribute"})
    public void moveAttributesBetweenFolders() throws InterruptedException {
        List<String> movedObjects = Arrays.asList("Activity", "Priority");
        moveObjectsBetweenFolders(ObjectTypes.ATTRIBUTE, ui.attributesTable, movedObjects, "2", "Activity");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 1, groups = {"moveObjects", "metric"})
    public void moveMetricsBetweenFolders() throws InterruptedException {
        List<String> movedObjects = Arrays.asList("Amount [BOP]", "_Close [EOP]");
        moveObjectsBetweenFolders(ObjectTypes.METRIC, ui.metricsTable, movedObjects, "2", "_System");
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 2, groups = {"deleteObjects", "fact"})
    public void deleteFacts() throws InterruptedException {
        List<String> deletedObjects = Arrays.asList("Duration", "Velocity");
        deleteObjectsTable(ui.factsTable, deletedObjects, factsList, ObjectTypes.FACT);
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 2, groups = {"deleteObjects",
            "attribute"})
    public void deleteAttributes() throws InterruptedException {
        List<String> deletedObjects = Arrays.asList("Activity", "Department",
                "Month of Quarter (Created)");
        deleteObjectsTable(ui.attributesTable, deletedObjects, attributesList, ObjectTypes.ATTRIBUTE);
    }

    @Test(dependsOnMethods = {"createProject"}, priority = 2, groups = {"deleteObjects",
            "metric"})
    public void deleteMetrics() throws InterruptedException {
        List<String> deletedObjects = Arrays.asList("Amount [BOP]", "_Close [EOP]");
        deleteObjectsTable(ui.metricsTable, deletedObjects, metricsList, ObjectTypes.METRIC);
    }

    @Test(dependsOnGroups = {"viewObjects", "moveObjects", "deleteObjects", "attribute", "fact",
            "metric", "variable"})
    public void endOfTests() throws InterruptedException {
        successfulTest = true;
    }
}