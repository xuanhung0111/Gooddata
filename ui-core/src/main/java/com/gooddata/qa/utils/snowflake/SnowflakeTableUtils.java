package com.gooddata.qa.utils.snowflake;

public class SnowflakeTableUtils {
    public static final String PK_CUSKEY = "custkey";
    public static final String ATTR_NAME = "name";
    public static final String FACT_AGE = "age";
    public static final String DATASET_CUSTOMERS = "customers";
    public static final String DATASET_CUSTOMERS_NO_SYSTEM = "customers2";
    public static final String DATASET_CUSTOMERS_ONLY_CLIENTID = "customers3";
    public static final String DATASET_CUSTOMERS_ONLY_TIMESTAMP = "customers4";
    public static final String DATASET_CUSTOMERS_ONLY_DELETED = "customers5";
    public static final String DATASET_CUSTOMERS_TIMESTAMP_CLIENTID = "customers6";
    public static final String DATASET_CUSTOMERS_DELETED_CLIENTID = "customers7";
    public static final String DATASET_CUSTOMERS_TIMESTAMP_DELETED = "customers8";
    public static final String OPTIONAL_PREFIX = "PRE_";
    public static final String TABLE_CUSTOMERS = OPTIONAL_PREFIX + "CUSTOMERS";
    public static final String TABLE_CUSTOMERS_NO_SYSTEM = OPTIONAL_PREFIX + "CUSTOMERS2";
    public static final String TABLE_CUSTOMERS_ONLY_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS3";
    public static final String TABLE_CUSTOMERS_ONLY_TIMESTAMP = OPTIONAL_PREFIX + "CUSTOMERS4";
    public static final String TABLE_CUSTOMERS_ONLY_DELETED = OPTIONAL_PREFIX + "CUSTOMERS5";
    public static final String TABLE_CUSTOMERS_TIMESTAMP_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS6";
    public static final String TABLE_CUSTOMERS_DELETED_CLIENTID = OPTIONAL_PREFIX + "CUSTOMERS7";
    public static final String TABLE_CUSTOMERS_TIMESTAMP_DELETED = OPTIONAL_PREFIX + "CUSTOMERS8";
    public static final String PKCOLUMN_CUSKEY = "CP__CUSTKEY";
    public static final String COLUMN_NAME = "A__NAME";
    public static final String COLUMN_AGE = "F__AGE";
    public static final String COLUMN_X_TIMESTAMP = "X__TIMESTAMP";
    public static final String COLUMN_X_DELETED = "X__DELETED";
    public static final String COLUMN_X_CLIENT_ID = "X__CLIENT_ID";
    public static final String VARCHAR_TYPE = "varchar";
    public static final String NUMERIC_TYPE = "NUMERIC(12,2)";
    public static final String TIMESTAMP_TYPE = "TIMESTAMP";
    public static final String BOOLEAN_TYPE = "BOOLEAN";
    public static final String PRIMARY_KEY = "PRIMARY KEY";
}
