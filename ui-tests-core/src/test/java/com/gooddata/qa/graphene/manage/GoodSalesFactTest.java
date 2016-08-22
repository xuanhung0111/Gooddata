package com.gooddata.qa.graphene.manage;

import static org.openqa.selenium.By.id;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

public class GoodSalesFactTest extends GoodSalesAbstractTest {

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-fact";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void factAggregationsTest() {
        FactDetailPage factDetailPage = initObject(FACT_AMOUNT);
        for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
            factDetailPage.createSimpleMetric(metricType, FACT_AMOUNT);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = { "object-tests" })
    public void changeFactFolderTest() {
        initObject(FACT_AMOUNT).changeFolder("Stage History");
    }

    @Test(dependsOnGroups = {"object-tests"})
    public void editFactBasicInfo() {
        final String editedName = FACT_AMOUNT + " -edited";
        final String description = "New description";
        final String tag = "newtag";

        FactDetailPage factDetailPage = initObject(FACT_AMOUNT);
        factDetailPage
                .changeName(editedName)
                .changeDescription(description)
                .addTag(tag);

        initObject(editedName);
        takeScreenshot(browser, "Fact-basic-info-changes", getClass());
        assertEquals(factDetailPage.getName(), editedName);
        assertEquals(factDetailPage.getDescription(), description);
        assertThat(factDetailPage.getTags(), hasItem(tag));
    }

    private FactDetailPage initObject(String factName) {
        initFactPage();
        ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser).selectObject(factName);
        return FactDetailPage.getInstance(browser);
    }
}