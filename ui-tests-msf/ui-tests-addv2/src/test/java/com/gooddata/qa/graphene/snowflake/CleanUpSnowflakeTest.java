package com.gooddata.qa.graphene.snowflake;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.cloudresources.ConnectionInfo;
import com.gooddata.qa.utils.cloudresources.SnowflakeUtils;
import net.bytebuddy.utility.RandomString;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.hamcrest.MatcherAssert.assertThat;

public class CleanUpSnowflakeTest extends AbstractTest {

    private SnowflakeUtils snowflakeUtils;
    private final String WAREHOUSE_NAME = "ATT_WAREHOUSE";
    private final String SCHEMA_NAME = "PUBLIC";
    private int databaseRetentionDays;

    @Test
    public void cleanUp() throws SQLException {
        // load properties and create connection to Snowflake
        initTestProperties();

        // get all Snowflake database created by Graphene
        ArrayList<SnowflakeUtils.DatabaseInfo> allSnowflakeOtherDB = snowflakeUtils.getOldSnowflakeDatabase("ATT_OTHER");
        ArrayList<SnowflakeUtils.DatabaseInfo> allSnowflakeDB = snowflakeUtils.getOldSnowflakeDatabase("ATT_DATABASE");
        allSnowflakeDB.addAll(allSnowflakeOtherDB);
        // Filter old databases
        ArrayList<SnowflakeUtils.DatabaseInfo> filteredResult = filterOldDatabases(allSnowflakeDB);
        if (allSnowflakeDB.size() == 0 || filteredResult.size() == 0) {
            log.info("There is no database need to drop");
            return;
        }

        // Drop old databases
        dropManyDatabases(filteredResult);
        sleepTightInSeconds(30);

        // Assert dropped databases to make this test pass or fail.
        ArrayList<String> allOtherDatabaseName = this.convertToProjectIdCollection(snowflakeUtils.getOldSnowflakeDatabase("ATT_OTHER"));
        ArrayList<String> allDatabaseName = this.convertToProjectIdCollection(snowflakeUtils.getOldSnowflakeDatabase("ATT_DATABASE"));
        allDatabaseName.addAll(allOtherDatabaseName);
        log.info("All database names: " + allDatabaseName.toString());

        ArrayList<String> droppedDatabaseName = this.convertToProjectIdCollection(filteredResult);
        log.info("Dropped database is older than " + databaseRetentionDays + ": " + droppedDatabaseName.toString());

        assertThat("Fail to drop one or some databases", !(allDatabaseName.containsAll(droppedDatabaseName)));
    }

    private void initTestProperties() throws SQLException {
        snowflakeUtils = new SnowflakeUtils(createSnowflakeConnectionInfo());
        databaseRetentionDays = testParams.getDatabaseRetentionDays();
    }

    /**
     * Filter old databases
     *
     * @listOfDB list of database need to filter
     */
    private ArrayList<SnowflakeUtils.DatabaseInfo> filterOldDatabases(ArrayList<SnowflakeUtils.DatabaseInfo> listOfDB) {
        return listOfDB
                .stream()
                .filter(this::isOldDatabases)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Is database old.
     */
    private boolean isOldDatabases(SnowflakeUtils.DatabaseInfo db) {
        Days tmp = Days.daysBetween(db.getCreated_on(), DateTime.now());
        return tmp.getDays() >= databaseRetentionDays;
    }

    /**
     * Drop many databases.
     *
     * @listOfDB arrayList of DatabaseInfo
     */
    private void dropManyDatabases(ArrayList<SnowflakeUtils.DatabaseInfo> listOfDB) {
        listOfDB.forEach(
                db -> {
                    try {
                        snowflakeUtils.dropDatabaseIfExists(db.getName());
                    } catch (SQLException exp) {
                        log.warning("Fail to drop database with name: " + db.getName() + "\n" + exp.toString());
                        return;
                    }
                }
        );
    }

    /**
     * Convert to list of database name.
     */
    private ArrayList<String> convertToProjectIdCollection(ArrayList<SnowflakeUtils.DatabaseInfo> databaseArrayList) {
        return databaseArrayList
                .stream()
                .map(SnowflakeUtils.DatabaseInfo::getName)
                .collect(Collectors.toCollection(ArrayList<String>::new));
    }

    // create connection to Snowflake
    private ConnectionInfo createSnowflakeConnectionInfo() {
        return new ConnectionInfo()
                .setUserName(testParams.getSnowflakeUserName())
                .setPassword(testParams.getSnowflakePassword())
                .setUrl(testParams.getSnowflakeJdbcUrl())
                .setWarehouse(WAREHOUSE_NAME)
                .setSchema(SCHEMA_NAME)
                .setDatabase(RandomString.make(5));
    }
}
