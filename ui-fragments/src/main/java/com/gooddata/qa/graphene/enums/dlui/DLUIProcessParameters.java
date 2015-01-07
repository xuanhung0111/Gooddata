package com.gooddata.qa.graphene.enums.dlui;

public enum DLUIProcessParameters {

    EXECUTABLE("executable"),
    ADSUSER("ADS_USER"),
    ADSPASSWORD("ADS_PASSWORD"),
    ADSURL("ADS_URL"),
    CREATE_TABLE_SQL("CREATE_TABLE"),
    COPY_TABLE_SQL("COPY_TABLE");
    
    private String jsonObjectKey;
    
    private DLUIProcessParameters(String jsonObjectKey) {
        this.jsonObjectKey = jsonObjectKey;
    }

    public String getJsonObjectKey() {
        return jsonObjectKey;
    }
}
