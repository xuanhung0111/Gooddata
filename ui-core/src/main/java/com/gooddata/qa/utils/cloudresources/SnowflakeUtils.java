package com.gooddata.qa.utils.cloudresources;

import net.snowflake.client.jdbc.SnowflakeConnectionV1;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class SnowflakeUtils {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeUtils.class);
    ConnectionInfo snowflakeConnectionInfo;
    Connection connection;

    public SnowflakeUtils(ConnectionInfo connectionInfo) throws SQLException {
        snowflakeConnectionInfo = connectionInfo;
        this.connection = buildConnection(snowflakeConnectionInfo);
    }

    /**
     * Create database witch specific name on the Snowflake.
     *
     * @param databaseName
     */
    public void createDatabase(String databaseName) throws SQLException {
        // create or replace new database
        executeSql("CREATE OR REPLACE DATABASE " + databaseName + " comment='This database is using by ATT team'");
        logger.info("Created database with specific name is: " + databaseName);
    }

    /**
     * Drop database on the snowflake.
     *
     * @param databaseName name of database will be dropped.
     */
    public void dropDatabaseIfExists(String databaseName) throws SQLException {
        executeSql("DROP DATABASE IF EXISTS " + databaseName);
        logger.info("Dropped the database with name is: " + databaseName);
    }

    /**
     * Get old databases which name start with ATT_DATABASE and created by Graphene.
     */
    public ArrayList<DatabaseInfo> getOldSnowflakeDatabase() throws SQLException {
        ArrayList<DatabaseInfo> resultArray = new ArrayList<DatabaseInfo>();
        ResultSet resultSet = getSqlResult("SHOW TERSE DATABASES STARTS WITH 'ATT_DATABASE'");
        while (resultSet.next()) {
            DatabaseInfo dbinfo = new DatabaseInfo();
            dbinfo.setName(resultSet.getString("name"));
            DateTime date = new DateTime(resultSet.getTimestamp("created_on").getTime());
            dbinfo.setCreated_on(date);
            resultArray.add(dbinfo);
        }
        return resultArray;
    }

    /**
     * Upload CSV to Snowflake stage.
     *
     * @param stageName
     * @param tableName
     * @param csvFullPath
     */
    public void uploadCsv2Snowflake(String stageName, String prefix, String tableName, String csvFullPath)
            throws SQLException, FileNotFoundException {
        createSnowflakeLocalStage(stageName);
        uploadCsvToSnowflakeStage(stageName, prefix, tableName, csvFullPath);
        loadCsvDataFromStage(stageName, tableName);
    }

    /**
     * Create a local stage with specific name on Snowflake.
     *
     * @param stageName
     */
    public void createSnowflakeLocalStage(String stageName) throws SQLException {
        executeSql("CREATE OR REPLACE STAGE " + stageName);
        logger.info("Created Snowflake stage with name is: " + stageName);
    }

    /**
     * Upload a CSV file to stage on Snowflake.
     *
     * @param stageName
     * @param csvPath
     */
    public void uploadCsvToSnowflakeStage(String stageName, String prefix, String desFileName, String csvPath)
            throws SQLException, FileNotFoundException {
        // Put local csv to InputStream
        File file = new File(csvPath);
        InputStream fileInputStream = new FileInputStream(file);

        // Upload CSV files as an InputStream
        Connection connection = buildConnection(snowflakeConnectionInfo);
        ((SnowflakeConnectionV1) connection).compressAndUploadStream(stageName, prefix, fileInputStream, desFileName);
        logger.info("Uploaded CSV files to stage: " + stageName);
    }

    /**
     * Copy CSV data from a stage to table.
     *
     * @param stageName
     * @param tableName
     */
    public void loadCsvDataFromStage(String stageName, String tableName) throws SQLException {
        executeSql(String.format(
                "COPY INTO %s FROM @%s file_format=(type=csv error_on_column_count_mismatch=false skip_header = 1)",
                tableName, stageName));
        logger.info("Loaded CSV data from stage " + stageName + "to table " + tableName);
    }

    /**
     * Create or replace table if exists on Snowflake database.
     *
     * @param tableName     name of table.
     * @param listOfColumns need at least one column to create table.
     */
    public SnowflakeUtils createTable(String tableName, List<DatabaseColumn> listOfColumns) throws SQLException {
        // setup columns
        String columnsOfTable = setupColumnsOfTable(listOfColumns);

        // create table
        executeSql("CREATE TABLE " + tableName + "(" + columnsOfTable + ")");
        logger.info("Created table with name: " + tableName);
        return this;
    }

    /**
     * Setup columns for table structure.
     *
     * @return columns will be created along with table.
     */
    public String setupColumnsOfTable(List<DatabaseColumn> listOfColumns) {
        // add comma between columns to applicable to SQL format
        return listOfColumns
                .stream()
                .map(DatabaseColumn::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Drop tables on Snowflake.
     *
     * @param tableNames
     */
    public void dropTables(String... tableNames) throws SQLException {
        for (String willDeleteTableName : tableNames) {
            executeSql("DROP TABLE IF EXISTS " + willDeleteTableName + " CASCADE;");
            logger.info("Dropped table with name: " + willDeleteTableName);
        }
    }

    /**
     * Execute SQL statement on the snowflake via given connection info.
     *
     * @param sqlStr SQL command or a script (multiple colon-separated commands) to execute.
     */
    public void executeSql(String sqlStr) throws SQLException {
        // create statements
        Statement statement = connection.createStatement();

        // execute SQL commands
        logger.info("Executing the SQL statements");
        System.out.println(sqlStr);
        statement.executeUpdate(sqlStr);
        logger.info("Done executing the SQL statements");

        // close statement
        statement.close();
    }

    /**
     * Execute SQL statement then return SQL result.
     *
     * @param sqlStr SQL command or a script (multiple colon-separated commands) to execute.
     * @return SQL result
     */
    public ResultSet getSqlResult(String sqlStr) throws SQLException {
        // create statements
        Statement statement = connection.createStatement();

        // execute SQL commands
        logger.info("Executing the SQL statements");
        ResultSet result = statement.executeQuery(sqlStr);
        logger.info("Done executing the SQL statements");

        // close statement
        statement.close();
        return result;
    }

    /**
     * Build JDBC connection to snowflake.
     *
     * @param connectionInfo snowflake connection information.
     * @return a connection point to Snowflake database schema.
     */
    private static Connection buildConnection(ConnectionInfo connectionInfo) throws SQLException {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Snowflake driver not found" + ex);
        }

        // build connection properties
        Properties properties = new Properties();
        properties.put("user", connectionInfo.getUserName());
        properties.put("password", connectionInfo.getPassword());
        properties.put("warehouse", connectionInfo.getWarehouse());
        properties.put("db", connectionInfo.getDatabase());
        properties.put("schema", connectionInfo.getSchema());

        String connectStr = connectionInfo.getUrl();
        return DriverManager.getConnection(connectStr, properties);
    }

    public void updateColumn(String tableName, String column, String value) {
        try {
            executeSql("UPDATE " + tableName + " SET " + column + "= " + value);
        } catch (SQLException e) {
            logger.error("Update column" + column + "with value" + value + "failed");
            throw new RuntimeException(e);
        }
    }

    public void dropColumn(String tableName, String column) {
        try {
            executeSql("ALTER TABLE " + tableName + " DROP COLUMN " + column);
        } catch (SQLException e) {
            logger.error("Drop column" + column + "failed");
            throw new RuntimeException(e);
        }
    }

    public void addColumn(String tableName, String column, String datatype) {
        try {
            executeSql("ALTER TABLE " + tableName + " ADD COLUMN " + column + " " + datatype);
        } catch (SQLException e) {
            logger.error("Add column" + column + "with datatype " + datatype + "failed");
            throw new RuntimeException(e);
        }
    }

    public ResultSet getRecords(String tableName, String column, int limit) {
        try {
            String sqlStr = "SELECT " + column + " FROM " + tableName + " LIMIT " + limit;
            return getSqlResult(sqlStr);
        } catch (SQLException e) {
            logger.error("get Records error" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get result by Snowflake condition .
     *
     * @param tableName     snowflake Table name.
     * @param column        snowflake column name.
     * @param andConditions condition after where clause. (example: column = "Column 1" , condition = " = 1 ")
     * @param limit         : limit records returns
     * @return ResultSet apply querry
     */
    public ResultSet getRecordsByCondition(String tableName, String column, List<Pair<String, String>> andConditions,
                                           List<Pair<String, String>> orConditions, int limit) {

        String andConditionString = andConditions != null
                ? " ( " + andConditions.stream().map(this::getCondition).collect(joining(" AND ")) + " ) "
                : "";
        String orConditionString = orConditions != null
                ? " ( " + orConditions.stream().map(this::getCondition).collect(joining(" OR ")) + " ) "
                : "";
        StringBuilder builder = new StringBuilder();
        try {
            String sqlStr = builder.append("SELECT ${column} FROM ${tableName} WHERE ")
                    .append(andConditionString)
                    .append((andConditions != null && orConditions != null) ? " AND " : "")
                    .append(orConditionString)
                    .append(" LIMIT " + limit)
                    .toString()
                    .replace("${tableName}", tableName)
                    .replace("${column}", column);
            logger.info("SQL Record in range:" + sqlStr);
            return getSqlResult(sqlStr);
        } catch (SQLException e) {
            logger.error("get Records By Condition error" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getCondition(Pair<String, String> condition) {
        return String.format("(%s %s)", condition.getLeft(), condition.getRight());
    }

    /**
     * Force to close JDBC connection to snowflake immediately, don't need to wait Java close it automatically.
     * This will release all Statement, ResultSet, other database resources.
     */
    public void closeSnowflakeConnection() {
        if (connection == null) {
            logger.info("There is no established connection so nothing to close.");
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            // Ignore because not sense.
        }
    }

    public class DatabaseInfo {
        String name;
        DateTime created_on;

        public DatabaseInfo() {
            this.name = name;
            this.created_on = created_on;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DateTime getCreated_on() {
            return created_on;
        }

        public void setCreated_on(DateTime created_on) {
            this.created_on = created_on;
        }
    }
}
