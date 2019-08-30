package com.gooddata.qa.utils.snowflake;

public class SnowflakeTableUtils {
    // Dataset names in GD workspace
    public static final String PK_CUSKEY = "custkey";
    public static final String PK_CUSKEY_LABEL = "custkeylabel";
    public static final String PK_CUSKEY_LINK = "custkeylink";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_FIRST_GRAIN = "custkeygrain";
    public static final String ATTR_SECOND_GRAIN = "custkeygrain2";
    public static final String FACT_AGE = "age";
    public static final String DATASET_CUSTOMERS = "customers";
    public static final String DATASET_DELETE_CUSTOMERS = "delete_customers";
    public static final String DATASET_DELETE_CUSTOMERS_TIMESTAMP_CLIENTID = "delete_customers_timestamp_clientid";
    public static final String DATASET_DELETE_CUSTOMERS_TIMESTAMP_DELETED = "delete_customers_timestamp_deleted";
    public static final String DATASET_DELETE_CUSTOMERS_DELETED_CLIENTID = "delete_customers_deleted_clientid";
    public static final String DATASET_CUSTOMERS_MUlTILABELS_NORMAL = "customersmultinormal";
    public static final String DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT = "customersmultidefault";
    public static final String DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT = "customersmultinodefault";
    public static final String DATASET_CUSTOMERS_NO_SYSTEM = "customers2";
    public static final String DATASET_CUSTOMERS_ONLY_CLIENTID = "customers3";
    public static final String DATASET_CUSTOMERS_ONLY_TIMESTAMP = "customers4";
    public static final String DATASET_CUSTOMERS_ONLY_DELETED = "customers5";
    public static final String DATASET_CUSTOMERS_TIMESTAMP_CLIENTID = "customers6";
    public static final String DATASET_CUSTOMERS_DELETED_CLIENTID = "customers7";
    public static final String DATASET_CUSTOMERS_TIMESTAMP_DELETED = "customers8";
    public static final String OPTIONAL_PREFIX = "PRE_";
    public static final String DELETED_PREFIX = "DELETED_";
    // ADS Table names
    public static final String TABLE_CUSTOMERS = OPTIONAL_PREFIX + "CUSTOMERS";
    public static final String TABLE_CUSTOMERS_MUlTILABELS_NORMAL = OPTIONAL_PREFIX + "CUSTOMERSMULTINORMAL";
    public static final String TABLE_CUSTOMERS_MUlTILABELS_HASDEFAULT = OPTIONAL_PREFIX + "CUSTOMERSMULTIDEFAULT";
    public static final String TABLE_CUSTOMERS_MUlTILABELS_NODEFAULT = OPTIONAL_PREFIX + "CUSTOMERSMULTINODEFAULT";
    public static final String TABLE_CUSTOMERS_NO_SYSTEM = OPTIONAL_PREFIX + "CUSTOMERS2";
    public static final String TABLE_CUSTOMERS_ONLY_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS3";
    public static final String TABLE_CUSTOMERS_ONLY_TIMESTAMP = OPTIONAL_PREFIX + "CUSTOMERS4";
    public static final String TABLE_CUSTOMERS_ONLY_DELETED = OPTIONAL_PREFIX + "CUSTOMERS5";
    public static final String TABLE_CUSTOMERS_TIMESTAMP_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS6";
    public static final String TABLE_CUSTOMERS_DELETED_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS7";
    public static final String TABLE_CUSTOMERS_TIMESTAMP_DELETED = OPTIONAL_PREFIX + "CUSTOMERS8";
    public static final String DELETED_TABLE_CUSTOMERS = OPTIONAL_PREFIX + DELETED_PREFIX + "CUSTOMERS";
    public static final String DELETED_TABLE_CUSTOMERS_TIMESTAMP_CLIENTID = OPTIONAL_PREFIX + DELETED_PREFIX + "CUSTOMERS6";
    public static final String DELETED_TABLE_CUSTOMERS_DELETED_CLIENTID = OPTIONAL_PREFIX + DELETED_PREFIX + "CUSTOMERS7";
    public static final String DELETED_TABLE_CUSTOMERS_TIMESTAMP_DELETED = OPTIONAL_PREFIX + DELETED_PREFIX + "CUSTOMERS8";
    // Column names in ADS Tables
    public static final String PKCOLUMN_CUSKEY = "CP__CUSTKEY";
    public static final String GRAINCOLUMN_CUSKEY = "A__CUSTKEYGRAIN";
    public static final String GRAINCOLUMN_SECOND_CUSKEY = "A__CUSTKEYGRAIN2";
    public static final String PKCOLUMN_CUSKEY_LABEL = "L__CUSTKEY__CUSTKEYLABEL";
    public static final String PKCOLUMN_CUSKEY_LINK = "L__CUSTKEY__CUSTKEYLINK";
    public static final String COLUMN_NAME = "A__NAME";
    public static final String COLUMN_AGE = "F__AGE";
    public static final String COLUMN_X_TIMESTAMP = "X__TIMESTAMP";
    public static final String COLUMN_X_DELETED = "X__DELETED";
    public static final String COLUMN_X_CLIENT_ID = "X__CLIENT_ID";
    // Datatypes of ADS Column
    public static final String VARCHAR_TYPE = "varchar";
    public static final String NUMERIC_TYPE = "NUMERIC(12,2)";
    public static final String TIMESTAMP_TYPE = "TIMESTAMP";
    public static final String BOOLEAN_TYPE = "BOOLEAN";
    public static final String PRIMARY_KEY = "PRIMARY KEY";
    public static final int LIMIT_RECORDS = 100;
    // Dataset names in GD workspace for Custom discriminator testing
    public static final String DATASET_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN = "customersmappingprojectid";
    public static final String DATASET_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN = "customersmappingclientid";
    public static final String DATASET_MAPPING_BOTH_NO_CLIENT_ID_COLUMN = "customersmappingboth";
    public static final String DATASET_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN = "customersmappingprojectid2";
    public static final String DATASET_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN = "customersmappingclientid2";
    public static final String DATASET_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN = "customersmappingboth2";
    // ADS Table names for Custom discriminator testing
    public static final String TABLE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGPROJECTID";
    public static final String TABLE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGCLIENTID";
    public static final String TABLE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGBOTH";
    public static final String TABLE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGPROJECTID2";
    public static final String TABLE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGCLIENTID2";
    public static final String TABLE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + "CUSTOMERSMAPPINGBOTH2";
    public static final String TABLE_DELETE_MAPPING_PROJECT_ID_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGPROJECTID";
    public static final String TABLE_DELETE_MAPPING_CLIENT_ID_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGCLIENTID";
    public static final String TABLE_DELETE_MAPPING_BOTH_NO_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGBOTH";
    public static final String TABLE_DELETE_MAPPING_PROJECT_ID_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGPROJECTID2";
    public static final String TABLE_DELETE_MAPPING_CLIENT_ID_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGCLIENTID2";
    public static final String TABLE_DELETE_MAPPING_BOTH_HAS_CLIENT_ID_COLUMN = OPTIONAL_PREFIX + DELETED_PREFIX
            + "CUSTOMERSMAPPINGBOTH2";

}
