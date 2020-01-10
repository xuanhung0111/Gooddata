package com.gooddata.qa.graphene.redshift;

import com.gooddata.qa.graphene.AbstractTest;

import com.gooddata.qa.utils.cloudresources.*;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CleanUpRedShiftTest extends AbstractTest {
    private final String DATABASE_NAME = "dev";
    private final String SCHEMA_NAME = "autoschema"; //dummy schema
    private RedshiftUtils redshiftUtils;
    private int databaseRetentionDays;

    @Test
    public void cleanUp() throws SQLException {
        databaseRetentionDays = testParams.getDatabaseRetentionDays();
        List<String> oldSchemas = new ArrayList<String>();
        initTestProperties();
        oldSchemas = getOldRedshiftSchema();
        int result = dropOldRedshiftSchema(oldSchemas);
        assertTrue(result == 0, "Schema in list is not clear");
    }

    private void initTestProperties() throws SQLException {
        ConnectionInfo connectionInfo = createRedshiftConnectionInfo(DATABASE_NAME, DatabaseType.REDSHIFT,
                SCHEMA_NAME);
        redshiftUtils = new RedshiftUtils(connectionInfo);
    }

    private List<String> getOldRedshiftSchema() throws SQLException {
        List<String> listSchemas = new ArrayList<String>();
        List<String> oldSchemas = new ArrayList<String>();
        // Get all schema created by Auto test
        listSchemas = redshiftUtils.getListCreatedSchema();
        log.info("List Schemas created by Auto : " + listSchemas.toString());
        // Filter old schemas from list of schema created by Auto test
        for (String schema : listSchemas) {
            String time = schema.substring(schema.lastIndexOf("__") + 2);
            String pattern = "YYYY_MM_dd_HH_mm_ss";
            DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
            DateTime date = dtf.parseDateTime(time);
            Days tmp = Days.daysBetween(date.toLocalDate(), DateTime.now().toLocalDate());
            if (tmp.getDays() >= databaseRetentionDays) {
                oldSchemas.add(schema);
            }
        }
        log.info("List chosen Schemas created by Auto : " + oldSchemas.toString());
        return oldSchemas;
    }

    private int dropOldRedshiftSchema(List<String> oldSchemas) throws SQLException {
        int countSchema = oldSchemas.size();
        if (countSchema != 0) {
            for (String chosenSchema : oldSchemas) {
                redshiftUtils.deleteTablesInSelectedSchema(chosenSchema);
                redshiftUtils.dropSchemaIfExists(chosenSchema);
                countSchema--;
            }
        }
        return countSchema;
    }

    private ConnectionInfo createRedshiftConnectionInfo(String database, DatabaseType dbType, String schema) {
        return new ConnectionInfo()
                .setDbType(dbType)
                .setDatabase(database)
                .setSchema(schema)
                .setUserName(testParams.getRedshiftUserName())
                .setPassword(testParams.getRedshiftPassword())
                .setUrl(testParams.getRedshiftJdbcUrl());
    }
}
