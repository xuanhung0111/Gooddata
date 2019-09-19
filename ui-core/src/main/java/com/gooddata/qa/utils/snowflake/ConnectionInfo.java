package com.gooddata.qa.utils.snowflake;

public class ConnectionInfo {

    String warehouse;
    String schema;
    String database;
    String userName;
    String password;
    String url;
    DatabaseType dbType;

    public DatabaseType getDbType() {
        return dbType;
    }

    public ConnectionInfo setDbType(DatabaseType dbType) {
        this.dbType = dbType;
        return this;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public ConnectionInfo setWarehouse(String warehouse) {
        this.warehouse = warehouse;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public ConnectionInfo setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public ConnectionInfo setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ConnectionInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ConnectionInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ConnectionInfo setUrl(String url) {
        this.url = url;
        return this;
    }
}
