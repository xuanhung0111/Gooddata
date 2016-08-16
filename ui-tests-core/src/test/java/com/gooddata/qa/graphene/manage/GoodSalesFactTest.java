package com.gooddata.qa.graphene.manage;

import static org.openqa.selenium.By.id;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

public class GoodSalesFactTest extends ObjectAbstractTest {

    private String factFolder;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-fact";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void initialize() throws JSONException {
        name = "Amount";
        this.factFolder = "Stage History";
        description = "Graphene test on view and modify Fact";
        tagName = "Graphene-test";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = { "object-tests" })
    public void factAggregationsTest() {
        initObject(name);
        for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
            factDetailPage.createSimpleMetric(metricType, name);
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = { "object-tests" })
    public void changeFactFolderTest() {
        initObject(name).changeObjectFolder(factFolder);
    }

    @Override
    public ObjectPropertiesPage initObject(String factName) {
        initFactPage();
        ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser).selectObject(factName);
        return FactDetailPage.getInstance(browser);
    }
}