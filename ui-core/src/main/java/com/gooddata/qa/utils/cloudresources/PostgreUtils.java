package com.gooddata.qa.utils.cloudresources;

import org.apache.commons.lang3.tuple.Pair;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class PostgreUtils {
    private static final Logger logger = LoggerFactory.getLogger(PostgreUtils.class);

    ConnectionInfo postgreConnectionInfo;
    Connection connection;

    public PostgreUtils(ConnectionInfo connectionInfo) throws SQLException {
        postgreConnectionInfo = connectionInfo;
        this.connection = buildConnection(postgreConnectionInfo);
    }
    /**
     * Create schema witch specific name on the Postgre.
     *
     */
    public PostgreUtils createSchema() throws SQLException {
        // create table
        executeSql("CREATE SCHEMA IF NOT EXISTS " + postgreConnectionInfo.getSchema());
        logger.info("Created table with name: " + postgreConnectionInfo.getSchema());
        return this;
    }

    /**
     * Drop schema on the postgre.
     *
     */
    public void dropSchemaIfExists() throws SQLException {
        executeSql("DROP SCHEMA IF EXISTS " + postgreConnectionInfo.getSchema());
        logger.info("Dropped the schema with name is: " + postgreConnectionInfo.getSchema());
    }

    public void dropConstrant(String tableName, String constraintName) throws SQLException {
        executeSql(String.format("ALTER TABLE %s DROP CONSTRAINT %s;", tableName, constraintName));
        logger.info("Dropped constraint successfully");
    }

    /**
     * Copy CSV data from S3 to table.
     *
     * @param tableName
     * @param reader
     */
    public void loadDataToDatabase(String tableName, FileReader reader) throws SQLException, IOException, ClassNotFoundException {
        logger.info("Connection logger:" + this.connection);
        long rowsInserted = new CopyManager(connection.unwrap(BaseConnection.class)).copyIn(
                String.format("COPY %s FROM STDIN (FORMAT csv, HEADER)", tableName),
                new BufferedReader(reader)
        );

        logger.info("%d row(s) inserted%n", rowsInserted);
    }

    /**
     * Create or replace table if exists on Postgre database.
     *
     * @param tableName     name of table.
     * @param listOfColumns need at least one column to create table.
     */
    public PostgreUtils createTable(String tableName, List<DatabaseColumn> listOfColumns) throws SQLException {
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
     * Drop tables on Postgre.
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
     * Execute SQL statement on the Postgre via given connection info.
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
     * Build JDBC connection to postgre.
     *
     * @param connectionInfo postgre connection information.
     * @return a connection point to postgre database schema.
     */
    private static Connection buildConnection(ConnectionInfo connectionInfo) throws SQLException {
        try {
            Class.forName("org.postgresql.core.BaseConnection");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Postgre driver not found" + ex);
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

    public void grantUsageSchema() {
        try {
            executeSql(String.format("grant usage on schema \"%s\" to public;", postgreConnectionInfo.getSchema()));
        } catch (SQLException e) {
            logger.error("grant usage schema failed " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void executeCommandsForSpecificWarehouse(){
        try {
            executeSql(String.format("set search_path = \"%s\";", postgreConnectionInfo.getSchema()));

        } catch (SQLException e) {
            logger.error("Set search_path failed : " + e.getMessage());
            throw new RuntimeException(e);
        }
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
     * Get result by Postgre condition .
     *
     * @param tableName     postgre Table name.
     * @param column        postgre column name.
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
     * Force to close JDBC connection to Postgre immediately, don't need to wait Java close it automatically.
     * This will release all Statement, ResultSet, other database resources.
     */
    public void closePostgreConnection() {
        if (connection == null) {
            logger.info("There is no established connection so nothing to close.");
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.info("Close PostgreSQL Connection failed!!");
        }
    }

    /**
     * @param schema selected schema
     * this function drop selected schema
     */
    public void dropSchemaIfExists(String schema) throws SQLException {
        executeSql("DROP SCHEMA IF EXISTS " + schema);
        logger.info("Dropped the schema with name is: " + schema);
    }
}
