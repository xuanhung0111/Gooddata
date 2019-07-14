package com.gooddata.qa.utils.snowflake;

import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_FIRST_GRAIN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_SECOND_GRAIN;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_DELETED_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_MUlTILABELS_NORMAL;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_DELETE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_DELETE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_DELETE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_NO_SYSTEM;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY_LABEL;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY_LINK;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;

public class DatasetUtils {
    public static CsvFile datasetNormal() {
        return new CsvFile(DATASET_CUSTOMERS).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_DELETED),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetDelete() {
        return new CsvFile(DATASET_DELETE_CUSTOMERS).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetDeleteTimeStampClientId() {
        return new CsvFile(DATASET_DELETE_CUSTOMERS_TIMESTAMP_CLIENTID).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetDeleteTimeStampDeleted() {
        return new CsvFile(DATASET_DELETE_CUSTOMERS_TIMESTAMP_DELETED).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(COLUMN_X_TIMESTAMP));
    }

    public static CsvFile datasetFactTableGrain() {
        return new CsvFile(DATASET_CUSTOMERS).columns(new CsvFile.Column(ATTR_FIRST_GRAIN), new CsvFile.Column(ATTR_SECOND_GRAIN),
                new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP),
                new CsvFile.Column(COLUMN_X_DELETED), new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetNormalHasMainLabel() {
        return new CsvFile(DATASET_CUSTOMERS_MUlTILABELS_NORMAL).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(PK_CUSKEY_LABEL), new CsvFile.Column(PK_CUSKEY_LINK), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_DELETED),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetNormalHasDefaultLabel() {
        return new CsvFile(DATASET_CUSTOMERS_MUlTILABELS_HASDEFAULT).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(PK_CUSKEY_LINK), new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE),
                new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_DELETED),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetNormalChangeDefaultLabel() {
        return new CsvFile(DATASET_CUSTOMERS).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(PK_CUSKEY_LABEL),
                new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP),
                new CsvFile.Column(COLUMN_X_DELETED), new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetNormalNoDefaultLabel() {
        return new CsvFile(DATASET_CUSTOMERS_MUlTILABELS_NODEFAULT).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(PK_CUSKEY_LINK), new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE),
                new CsvFile.Column(COLUMN_X_TIMESTAMP), new CsvFile.Column(COLUMN_X_DELETED),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetNoSystem() {
        return new CsvFile(DATASET_CUSTOMERS_NO_SYSTEM).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE));
    }

    public static CsvFile datasetOnlyClientId() {
        return new CsvFile(DATASET_CUSTOMERS_ONLY_CLIENTID).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetOnlyTimeStamp() {
        return new CsvFile(DATASET_CUSTOMERS_ONLY_TIMESTAMP).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP));
    }

    public static CsvFile datasetOnlyDeleted() {
        return new CsvFile(DATASET_CUSTOMERS_ONLY_DELETED).columns(new CsvFile.Column(PK_CUSKEY), new CsvFile.Column(ATTR_NAME),
                new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_DELETED));
    }

    public static CsvFile datasetTimeStampClientId() {
        return new CsvFile(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetDeletedClientId() {
        return new CsvFile(DATASET_CUSTOMERS_DELETED_CLIENTID).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_DELETED),
                new CsvFile.Column(COLUMN_X_CLIENT_ID));
    }

    public static CsvFile datasetTimeStampDeleted() {
        return new CsvFile(DATASET_CUSTOMERS_TIMESTAMP_DELETED).columns(new CsvFile.Column(PK_CUSKEY),
                new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE), new CsvFile.Column(COLUMN_X_TIMESTAMP),
                new CsvFile.Column(COLUMN_X_DELETED));
    }
}
