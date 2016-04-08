package com.gooddata.qa.graphene.common;

import com.gooddata.project.Environment;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.enums.project.DeleteMode;

import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import static com.gooddata.qa.utils.EnumUtils.lookup;

public class TestParameters {

    private Properties testVariables;

    private String host;
    private String projectId;
    private String user;
    private String password;
    private String editorUser;
    private String editorPassword;
    private String viewerUser;
    private String viewerPassword;
    private String adminUser;
    private String adminPassword;
    private String authorizationToken;
    private String authorizationToken2;
    private ProjectDriver projectDriver = ProjectDriver.POSTGRES;
    private DeleteMode deleteMode = DeleteMode.DELETE_NEVER;
    private Environment projectEnvironment = Environment.TESTING;
    private String testIdentification;
    private String downloadFolder;
    private String csvFolder;
    private int defaultTimeout;
    private int extendedTimeoutMultiple;
    private String folderSeparator;
    private boolean reuseProject = false;
    // set this to what `host` is proxied to
    // (e.g. set hostProxy=staging3.intgdc.com if host is localhost:8443 and proxied to staging3)
    private String hostProxy;

    public TestParameters(Properties testVariables) {
        this.testVariables = testVariables;
        host = loadProperty("host");
        user = loadProperty("user");
        password = loadProperty("password");
        editorUser = loadProperty("editorUser");
        editorPassword = loadProperty("editorPassword");
        viewerUser = loadProperty("viewerUser");
        viewerPassword = loadProperty("viewerPassword");
        adminUser = loadProperty("adminUser");
        adminPassword = loadProperty("adminPassword");
        projectDriver = lookup(loadProperty("project.dwhDriver"), ProjectDriver.class, ProjectDriver.POSTGRES, "getValue");
        authorizationToken = loadProperty("project.authorizationToken");
        authorizationToken2 = loadProperty("project.authorizationToken2");
        deleteMode = lookup(loadProperty("deleteMode"), DeleteMode.class, DeleteMode.DELETE_NEVER);
        projectEnvironment = lookup(loadProperty("project.environment"), Environment.class, Environment.TESTING);
        downloadFolder = loadProperty("browserDownloadFolder");
        csvFolder = loadProperty("csvFolder");
        defaultTimeout = Integer.parseInt(loadProperty("timeout"));
        extendedTimeoutMultiple = Integer.parseInt(loadProperty("extendedTimeoutMultiple"));
        testIdentification = loadProperty("testIdentification");
        if (testIdentification == null) {
            testIdentification = UUID.randomUUID().toString();
        }
        folderSeparator = loadProperty("file.separator");
        reuseProject = Boolean.valueOf(loadProperty("project.reuse"));
        if (reuseProject) projectId = loadProperty("projectId");
        hostProxy = loadProperty("hostProxy");
    }

    /**
     * Method to return property value from:
     * - System.getProperty(key) if present (good for CI integration)
     * - properties file defined on path "propertiesPath" (default properties are in variables-env-test.properties)
     *
     * @param propertyKey
     * @return value of required property
     */
    public String loadProperty(String propertyKey) {
        String property = System.getProperty(propertyKey);
        if (property != null && property.length() > 0) {
            return property;
        }
        return testVariables.getProperty(propertyKey);
    }

    public String getHost() {
        return host;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEditorUser() {
        return editorUser;
    }

    public String getEditorPassword() {
        return editorPassword;
    }

    public String getViewerUser() {
        return viewerUser;
    }

    public String getViewerPassword() {
        return viewerPassword;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public String getAuthorizationToken2() {
        return authorizationToken2;
    }

    public ProjectDriver getProjectDriver() {
        return projectDriver;
    }

    public Environment getProjectEnvironment() {
        return projectEnvironment;
    }

    public DeleteMode getDeleteMode() {
        return deleteMode;
    }

    public String getTestIdentification() {
        return testIdentification;
    }

    public String getDownloadFolder() {
        return downloadFolder;
    }

    public String getCsvFolder() {
        return csvFolder;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    public int getExtendedTimeout() {
        return defaultTimeout * extendedTimeoutMultiple;
    }

    public String getFolderSeparator() {
        return folderSeparator;
    }

    public boolean isReuseProject() {
        return reuseProject;
    }

    public void setReuseProject(boolean reuseProject) {
        this.reuseProject = reuseProject;
    }

    public boolean isHostProxy() {
        return this.hostProxy != null && !this.hostProxy.isEmpty();
    }

    public String getHostProxy() {
        return this.hostProxy;
    }

    /**
     * Some test cases should be executed only on cluster.
     * E.g.
     * * Kpi alert evaluation via Sendrid (3rd party) - dependency on 3rd party system
     */
    public boolean isClusterEnvironment() {
        return Stream.of(".*secure.*\\.gooddata\\.com", "staging.*\\.intgdc\\.com", "perf\\.getgooddata\\.com", ".*\\.eu\\.gooddata\\.com")
                .anyMatch(this.host::matches);
    }
}
