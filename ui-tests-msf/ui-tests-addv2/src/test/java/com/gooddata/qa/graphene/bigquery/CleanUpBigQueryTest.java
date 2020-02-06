package com.gooddata.qa.graphene.bigquery;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.cloudresources.*;
import com.google.cloud.bigquery.Dataset;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class CleanUpBigQueryTest extends AbstractTest {
    private final String BIGQUERY_PROJECT = "gdc-us-dev";
    private BigQueryUtils bigQueryUtils;
    private final String SCHEMA_NAME = "autoschema"; //dummy schema

    @Test
    public void cleanUp() throws ParseException {
        initTestProperties();
        List<Dataset> oldSchemas = bigQueryUtils.getOldBigQuerySchemas(testParams.getDatabaseRetentionDays());
        log.info("Size : " + oldSchemas.size());
        int result = dropOldBigQuerySchema(oldSchemas);
        assertTrue(result == 0, "Schema in list is not clear");
    }

    private void initTestProperties() {
        ConnectionInfo connectionInfo = createBigQueryConnectionInfo(BIGQUERY_PROJECT, DatabaseType.BIGQUERY, SCHEMA_NAME);
        bigQueryUtils = new BigQueryUtils(connectionInfo);
    }

    private int dropOldBigQuerySchema(List<Dataset> oldSchemas) {
        int countSchema = oldSchemas.size();
        log.info("countSchema : " + countSchema);
        if (countSchema != 0) {
            for (Dataset chosenSchema : oldSchemas) {
                bigQueryUtils.deleteTablesInSelectedSchema(chosenSchema);
                bigQueryUtils.dropSchemaIfExists(chosenSchema.getGeneratedId());
                countSchema--;
            }
        }
        return countSchema;
    }

    private ConnectionInfo createBigQueryConnectionInfo(String project, DatabaseType dbType, String schema) {
        return new ConnectionInfo()
                .setDbType(dbType)
                .setSchema(schema)
                .setProject(project)
                .setClientEmail(testParams.getBigqueryClientEmail())
                .setPrivateKey(testParams.getBigqueryPrivateKey());
    }
}
