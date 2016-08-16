package com.gooddata.qa.graphene.enums;

public enum ObjectTypes {

    FACT("fact", "factsTable", "factsMenuItem"),
    ATTRIBUTE("attribute", "attributesTable", "attributesMenuItem"),
    METRIC("metric", "metricsTable", "metricsMenuItem"),
    VARIABLE("variable", "variablesTable", "variablesMenuItem"),
    MODEL("model", "ldmModelMenuItem"),
    DATA_SETS("data sets", "uploadsTable", "dataSetsMenuItem");

    private final String name;

    private final String objectsTableID;

    private final String menuItem;

    private ObjectTypes(String name, String id, String menuItem) {
        this.name = name;
        this.objectsTableID = id;
        this.menuItem = menuItem;
    }

    private ObjectTypes(String name, String menuItem) {
        this(name, null, menuItem);
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
}
