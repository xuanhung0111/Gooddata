package com.gooddata.qa.utils.cloudresources;

public class ConnectionInfo {

    String warehouse;
    String schema;
    String database;
    String userName;
    String password;
    String url;
    DatabaseType dbType;
    String clientEmail;
    String privateKey;
    String project;

    public String getClientEmail() {
        return clientEmail;
    }

    public ConnectionInfo setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public ConnectionInfo setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public String getProject() {
        return project;
    }

    public ConnectionInfo setProject(String project) {
        this.project = project;
        return this;
    }

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
