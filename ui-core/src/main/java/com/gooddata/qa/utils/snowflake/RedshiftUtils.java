package com.gooddata.qa.utils.snowflake;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class RedshiftUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedshiftUtils.class);
    private ConnectionInfo redshiftConnectionInfo;
    private Connection connection;

    public RedshiftUtils(ConnectionInfo connectionInfo) throws SQLException {
        redshiftConnectionInfo = connectionInfo;
        this.connection = buildConnection(redshiftConnectionInfo);
    }

    /**
     * Create schema witch specific name on the Redshift.
     *
     */
    public RedshiftUtils createSchema() throws SQLException {
        // create table
        executeSql("CREATE SCHEMA IF NOT EXISTS " + redshiftConnectionInfo.getSchema());
        logger.info("Created table with name: " + redshiftConnectionInfo.getSchema());
        return this;
    }

    /**
     * Drop schema on the redshift.
     *
     */
    public void dropSchemaIfExists() throws SQLException {
        executeSql("DROP SCHEMA IF EXISTS " + redshiftConnectionInfo.getSchema());
        logger.info("Dropped the schema with name is: " + redshiftConnectionInfo.getSchema());
    }

    /**
     * Copy CSV data from S3 to table.
     *
     * @param tableName
     * @param s3Path
     * @param s3AccessKey
     * @param s3SecretKey
     */
    public void loadDataFromS3ToDatabase(String tableName, String s3Path, String s3AccessKey, String s3SecretKey) throws SQLException {
        executeSql(String.format(
                "copy %s from '%s' credentials 'aws_access_key_id=%s;aws_secret_access_key=%s' delimiter ',' ignoreheader 1 region 'us-east-1';",
                tableName, s3Path, s3AccessKey, s3SecretKey));
        logger.info("Loaded CSV data from S3 " + s3Path + "to table " + tableName);
    }

    /**
     * Create or replace table if exists on Redshift database.
     *
     * @param tableName     name of table.
     * @param listOfColumns need at least one column to create table.
     */
    public RedshiftUtils createTable(String tableName, List<DatabaseColumn> listOfColumns) throws SQLException {
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
     * Drop tables on Redshift.
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
     * Execute SQL statement on the redshift via given connection info.
     *
     * @param sqlStr SQL command or a script (multiple colon-separated commands) to execute.
     */
    public void executeSql(String sqlStr) throws SQLException {
        // create statements
        Statement statement = connection.createStatement();
        // execute SQL commands
        logger.info("Executing the SQL statements");
        logger.info(sqlStr);
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
    public ArrayList<String> getArrayResult(String sqlStr, String column) throws SQLException {
        // create statements
        Statement statementArray = connection.createStatement();
        ArrayList<String> resultArray = new ArrayList<String>();
        try {
            // execute SQL commands
            logger.info("Executing the SQL statements");
            ResultSet result = statementArray.executeQuery(sqlStr);
            logger.info("Done executing the SQL statements");
            while (result.next()) {
                resultArray.add(result.getString(column));
            }
            return resultArray;
        } finally {
            // close statement
            statementArray.close();
        }
    }

    /**
     * Build JDBC connection to redshift.
     *
     * @param connectionInfo redshift connection information.
     * @return a connection point to redshift database schema.
     */
    private static Connection buildConnection(ConnectionInfo connectionInfo) throws SQLException {
        try {
            Class.forName("com.amazon.redshift.jdbc42.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Redshift driver not found" + ex);
        }

        // build connection properties
        Properties properties = new Properties();
        properties.put("user", connectionInfo.getUserName());
        properties.put("password", connectionInfo.getPassword());
        properties.put("db", connectionInfo.getDatabase());
        properties.put("schema", connectionInfo.getSchema());

        String connectStr = connectionInfo.getUrl();
        return DriverManager.getConnection(connectStr, properties);
    }

    public void updateColumn(String tableName, String column, String value, boolean isSchemaCreation) {
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

    public void truncateTable(String tableName) {
        try {
            executeSql("TRUNCATE TABLE " + tableName);
        } catch (SQLException e) {
            logger.error("Truncate table failed !!!");
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> getRecords(String tableName, String column, int limit) {
        try {
            String sqlStr = "SELECT " + column + " FROM " + tableName + " LIMIT " + limit;
            return getArrayResult(sqlStr, column);
        } catch (SQLException e) {
            logger.error("get Records error" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get result by Redshift condition .
     *
     * @param tableName     redshift Table name.
     * @param column        redshift column name.
     * @param andConditions condition after where clause. (example: column = "Column 1" , condition = " = 1 ")
     * @param limit         : limit records returns
     * @return ResultSet apply querry
     */
    public ArrayList<String> getRecordsByCondition(String tableName, String column, List<Pair<String, String>> andConditions,
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
            return getArrayResult(sqlStr, column);
        } catch (SQLException e) {
            logger.error("get Records By Condition error" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getCondition(Pair<String, String> condition) {
        return String.format("(%s %s)", condition.getLeft(), condition.getRight());
    }

    /**
     * Force to close JDBC connection to Redshift immediately, don't need to wait Java close it automatically.
     * This will release all Statement, ResultSet, other database resources.
     */
    public void closeRedshiftConnection() {
        if (connection == null) {
            logger.info("There is no established connection so nothing to close.");
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.info("Close Redshift Connection failed!!");
        }
    }

    public void executeCommandsForSpecificWarehouse(){
        try {
            executeSql(String.format("set search_path = \"%s\";", redshiftConnectionInfo.getSchema()));

        } catch (SQLException e) {
            logger.error("Set search_path failed : " + e.getMessage());
            throw new RuntimeException(e);
        }
}
}
