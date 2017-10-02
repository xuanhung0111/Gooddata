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
    private String domainUser = null;
    private String user;
    private String password;
    private String editorUser;
    private String viewerUser;
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
    private String userDomain;
    // set this to what `host` is proxied to
    // (e.g. set hostProxy=staging3.intgdc.com if host is localhost:8443 and proxied to staging3)
    private String hostProxy;
    private String languageCode;

    public TestParameters(Properties testVariables) {
        this.testVariables = testVariables;
        host = loadProperty("host");
        user = loadProperty("user");
        password = loadProperty("password");
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
        languageCode = loadProperty("language");
        userDomain = loadProperty("user.domain");
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

    public void setDomainUser(String domainUser) {
        this.domainUser = domainUser;
    }

    public String getDomainUser() {
        return domainUser;
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

    public void setEditorUser(String editorUser) {
        this.editorUser = editorUser;
    }

    public String getEditorUser() {
        return editorUser;
    }

    public void setViewerUser(String viewerUser) {
        this.viewerUser = viewerUser;
    }

    public String getViewerUser() {
        return viewerUser;
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

    public String getLanguageCode() {
        return languageCode;
    }

    public String getUserDomain() {
        return userDomain;
    }
    /**
     * Some test cases should be executed only on cluster.
     * E.g.
     * * Kpi alert evaluation via Sendrid (3rd party) - dependency on 3rd party system
     */
    public boolean isClusterEnvironment() {
        return isTestingEnvironment() || isProductionEnvironment() || isPerformanceEnvironment();
    }

    public boolean isTestingEnvironment() {
        return Stream.of("ponies\\.intgdc\\.com", "zebroids\\.intgdc\\.com", "donkeys\\.intgdc\\.com",
                "staging.*\\.intgdc\\.com")
                .anyMatch(this.host::matches);
    }

    public boolean isProductionEnvironment() {
        return Stream.of("whitelabeled\\.intgdc\\.com", ".*secure.*\\.gooddata\\.com",
                ".*\\.eu\\.gooddata\\.com", "gdcwltesteu\\.getgooddata\\.com",
                ".*\\.na\\.prodgdc\\.com", ".*\\.ca\\.gooddata\\.com")
                .anyMatch(this.host::matches);
    }

    public boolean isPerformanceEnvironment() {
        return Stream.of("perf\\.getgooddata\\.com")
                .anyMatch(this.host::matches);
    }

    public boolean isPIEnvironment() {
        return !isClusterEnvironment() && !isHostProxy();
    }

    public boolean isClientDemoEnvironment() {
        return Stream.of("client-demo\\.na\\.intgdc\\.com.*")
                .anyMatch(this.host::matches);
    }
}
