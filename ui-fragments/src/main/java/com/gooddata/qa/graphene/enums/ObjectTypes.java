package com.gooddata.qa.graphene.enums;

import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;

public enum ObjectTypes {

    FACT("fact", "factsTable", "factsMenuItem", FactDetailPage.class),
    ATTRIBUTE("attribute", "attributesTable", "attributesMenuItem", AttributeDetailPage.class),
    METRIC("metric", "metricsTable", "metricsMenuItem", MetricDetailsPage.class),
    VARIABLE("variable", "variablesTable", "variablesMenuItem", VariableDetailPage.class),
    MODEL("model", "ldmModelMenuItem"),
    DATA_SETS("data sets", "uploadsTable", "dataSetsMenuItem", DatasetDetailPage.class);

    private final String name;
    private final String objectsTableID;
    private final String menuItem;
    private final Class<? extends ObjectPropertiesPage> detailPage;

    private ObjectTypes(String name, String id, String menuItem, Class<? extends ObjectPropertiesPage> detailPage) {
        this.name = name;
        this.objectsTableID = id;
        this.menuItem = menuItem;
        this.detailPage = detailPage;
    }

    private ObjectTypes(String name, String menuItem) {
        this(name, null, menuItem, null);
    }

    public String getName() {
        return name;
    }

    public String getObjectsTableID() {
        return objectsTableID;
    }

    public String getMenuItemXpath() {
        return "//li[@id='${menuItem}']".replace("${menuItem}", menuItem);
    }

    public Class<? extends ObjectPropertiesPage> getDetailPage() {
        return detailPage;
    }
}
