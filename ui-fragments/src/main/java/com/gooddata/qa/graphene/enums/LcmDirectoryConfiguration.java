package com.gooddata.qa.graphene.enums;

public enum LcmDirectoryConfiguration {

    GD_ENDCODE_PARAM("gd_encoded_params", "gd_encoded_params"),
    CLIENT_GDC_HOSTNAME("CLIENT_GDC_HOSTNAME", "staging-lcm-prod.intgdc.com"),
    GDC_PASSWORD("GDC_PASSWORD", "GDC_PASSWORD"),
    ORGANIZATION("organization", "staging-lcm-prod"),
    ADS_PASSWORD("ads_password", "ads_password"),
    GDC_USERNAME("GDC_USERNAME", "GDC_USERNAME"),
    CLIENT_GDC_PROTOCOL("CLIENT_GDC_PROTOCOL", "https"),
    GDC_LOG_LEVEL("GDC_LOG_LEVEL", "DEBUG"),
    GD_ENDCODE_HIDEN_PARAMS("gd_encoded_hidden_params", "gd_encoded_hidden_params"),
    EXCLUDE_FACT_RULE("exclude_fact_rule", "true"),
    TRANSFER_ALL("transfer_all", "true"),
    SEGMENT_ID("SEGMENT_ID", "segement_id"),
    JDBC_URL("JDBC_URL", "jbbc_url"),
    DEVELOPMENT_PID("DEVELOPMENT_PID", "development_pid"),
    MASTER_PROJECT_NAME("mstpn", "master_project_name");

    private String paramName;
    private String paramValue;

    private LcmDirectoryConfiguration(String paramName, String paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public String getParamName() {
        return paramName;
    }
    public String getParamValue() {
        return paramValue;
    }

}
