package com.gooddata.qa.graphene.common;

import com.gooddata.project.Environment;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

import static com.gooddata.qa.utils.EnumUtils.lookup;

public class TestParameters {

    private Properties testVariables;
    private long createProjectTimeout;
    private String host;
    private String isolatedDomainSalesForce;
    private String projectId;
    private String domainUser = null;
    private String user;
    private String password;
    private String snowflakePassword;
    private String snowflakeUserName;
    private String snowflakeJdbcUrl;
    private String redshiftPassword;
    private String redshiftUserName;
    private String redshiftJdbcUrl;
    private String redshiftIAMDbUser;
    private String redshiftIAMAccessKey;
    private String redshiftIAMSecretKey;
    private String redshiftIAMShortUrl;
    private String redshiftIAMLongUrl;
    private String bigqueryClientEmail;
    private String bigqueryPrivateKey;
    private String editorUser;
    private String editorInvitationsUser;
    private String editorAdminUser;
    private String explorerUser;
    private String viewerUser;
    private String viewerDisabledExport;
    private String dashboardOnlyUser;
    private String authorizationToken;
    private ProjectDriver projectDriver = ProjectDriver.POSTGRES;
    private DeleteMode deleteMode = DeleteMode.DELETE_NEVER;
    private Environment projectEnvironment = Environment.TESTING;
    private int retentionDays;
    private int databaseRetentionDays;
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
    // variables of SDK
    private String reactFolder;
    private String reactProjectTitle;
    private String uisdkVersion;
    private String localhostSDK;
    private String brickAppstore;
    private String lcmDataloadProcessComponentVersion;

    public static TestParameters getInstance() {
        String propertiesPath = System.getProperty("propertiesPath", System.getProperty("user.dir") +
                "/ui-tests-core/src/test/resources/variables-env-test.properties".replace("/",
                        System.getProperty("file.separator")));
        System.out.println("User properties: " + propertiesPath);

        Properties testVariables = new Properties();
        try {
            FileInputStream in = new FileInputStream(propertiesPath);
            testVariables.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties weren't loaded from path: " + propertiesPath);
        }

        return new TestParameters(testVariables);
    }

    public TestParameters(Properties testVariables) {
        this.testVariables = testVariables;
        this.createProjectTimeout = Long.parseLong(loadProperty("createProjectTimeout"));
        host = loadProperty("host");
        isolatedDomainSalesForce = loadProperty("isolatedDomainSalesForce");
        user = loadProperty("user");
        password = loadProperty("password");
        snowflakePassword = loadProperty("snowflakePassword");
        snowflakeUserName = loadProperty("snowflakeUserName");
        snowflakeJdbcUrl = loadProperty("snowflakeJdbcUrl");
        redshiftPassword = loadProperty("redshiftPassword");
        redshiftUserName = loadProperty("redshiftUserName");
        redshiftJdbcUrl = loadProperty("redshiftJdbcUrl");
        redshiftIAMDbUser = loadProperty("redshiftIAMDbUser");
        redshiftIAMAccessKey = loadProperty("redshiftIAMAccessKey");
        redshiftIAMSecretKey = loadProperty("redshiftIAMSecretKey");
        redshiftIAMLongUrl = loadProperty("redshiftIAMLongUrl");
        redshiftIAMShortUrl = loadProperty("redshiftIAMShortUrl");
        bigqueryClientEmail = loadProperty("bigqueryClientEmail");
        bigqueryPrivateKey = loadProperty("bigqueryPrivateKey");
        projectDriver = lookup(loadProperty("project.dwhDriver"), ProjectDriver.class, ProjectDriver.POSTGRES, "getValue");
        authorizationToken = loadProperty("project.authorizationToken");
        deleteMode = lookup(loadProperty("deleteMode"), DeleteMode.class, DeleteMode.DELETE_NEVER);
        projectEnvironment = lookup(loadProperty("project.environment"), Environment.class, Environment.TESTING);
        retentionDays = Integer.parseInt(loadProperty("project.retentionDayNumber"));
        databaseRetentionDays = Integer.parseInt(loadProperty("databaseRetentionDayNumber"));
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
        brickAppstore = loadProperty("brickAppstore");
        // For staging env, brickAppstore is PRODUCTION_APPSTORE
        // TODO: remove this workaround once testing on NA3 completed, this param should be manipulated at ci-infra
        if ("gdctest-na3".equals(userDomain)) {
            brickAppstore = "PUBLIC_APPSTORE";
        }
        lcmDataloadProcessComponentVersion = loadProperty("lcmDataloadProcessComponentVersion");
        reactFolder = loadProperty("reactFolder");
        reactProjectTitle = loadProperty("reactProjectTitle");
        uisdkVersion = loadProperty("uisdkVersion");
        localhostSDK = loadProperty("localhostSDK");
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

    public long getCreateProjectTimeout() {
        return createProjectTimeout;
    }

    public void setCreateProjectTimeout(long createNewProjectTimeout) {
        this.createProjectTimeout = createNewProjectTimeout;
    }

    public String getHost() {
        return host;
    }

    public String getIsolatedDomainSalesForce() {
        return isolatedDomainSalesForce;
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

    public String getSnowflakePassword() {
        return snowflakePassword;
    }

    public String getSnowflakeUserName() {
        return snowflakeUserName;
    }

    public String getSnowflakeJdbcUrl() {
        return snowflakeJdbcUrl;
    }

    public void setSnowflakePassword(String snowflakePassword) {
        this.snowflakePassword = snowflakePassword;
    }

    public String getRedshiftPassword() {
        return redshiftPassword;
    }

    public String getRedshiftUserName() {
        return redshiftUserName;
    }

    public String getRedshiftJdbcUrl() {
        return redshiftJdbcUrl;
    }

    public String getRedshiftIAMDbUser() {return redshiftIAMDbUser; }

    public String getRedshiftIAMAccessKey() {return redshiftIAMAccessKey; }

    public String getRedshiftIAMSecretKey() {return redshiftIAMSecretKey; }

    public String getRedshiftIAMShortUrl() {return redshiftIAMShortUrl; }

    public String getRedshiftIAMLongUrl() {return redshiftIAMLongUrl; }

    public String getBigqueryClientEmail() { return bigqueryClientEmail; }

    public void setBigqueryClientEmail(String bigqueryClientEmail) { this.bigqueryClientEmail = bigqueryClientEmail; }

    public String getBigqueryPrivateKey() { return bigqueryPrivateKey; }

    public void setBigqueryPrivateKey(String bigqueryPrivateKey) {
        this.bigqueryPrivateKey = bigqueryPrivateKey;
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

    public void setEditorInvitationsUser(String editorInvitationsUser) {
        this.editorInvitationsUser = editorInvitationsUser;
    }

    public String getEditorInvitationsUser() {
        return editorInvitationsUser;
    }

    public void setEditorAdminUser(String editorAdminUser) {
        this.editorAdminUser = editorAdminUser;
    }

    public String getEditorAdminUser() {
        return editorAdminUser;
    }

    public void setExplorerUser(String explorerUser) {
        this.explorerUser = explorerUser;
    }

    public String getExplorerUser() {
        return explorerUser;
    }

    public void setViewerUser(String viewerUser) {
        this.viewerUser = viewerUser;
    }

    public String getViewerUser() {
        return viewerUser;
    }

    public void setViewerDisabledExport(String viewerDisabledExport) {
        this.viewerDisabledExport = viewerDisabledExport;
    }

    public String getViewerDisabledExport() {
        return viewerDisabledExport;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public ProjectDriver getProjectDriver() {
        return projectDriver;
    }

    public Environment getProjectEnvironment() {
        return projectEnvironment;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public int getDatabaseRetentionDays() {
        return databaseRetentionDays;
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

    public String getExportFilePath(String fileNameExtension) {
        return getDownloadFolder() + getFolderSeparator() + fileNameExtension;
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
     * If test run under proxy the backend url is proxy url
     */
    public String getBackendUrl() {
        return isHostProxy() ? this.hostProxy : this.host;
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

    public boolean isWhitelabeledEnvironment() {
        return Stream.of("whitelabeled\\.intgdc\\.com","ponies\\.intgdc\\.com",
                "zebroids\\.intgdc\\.com","donkeys\\.intgdc\\.com","gdcwltest\\.eu\\.gooddata\\.com",
                "secure\\.gooddata\\.com","gdcwltest\\.ca\\.gooddata\\.com","gdctestwl-na3\\.na\\.gooddata\\.com")
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

    public String getDashboardOnlyUser() {
        return dashboardOnlyUser;
    }

    public void setDashboardOnlyUser(String dashboardOnlyUser) {
        this.dashboardOnlyUser = dashboardOnlyUser;
    }

    public String getReactFolder() {
        return reactFolder;
    }

    public String getReactProjectTitle() {
        return reactProjectTitle;
    }

    public String getUIsdkVersion() {
        return uisdkVersion;
    }

    public String getLocalhostSDK() {
        return localhostSDK;
    }

    public String getBrickAppstore() {
        return brickAppstore;
    }

    public String getLcmDataloadProcessComponentVersion() {
        return lcmDataloadProcessComponentVersion;
    }

    public Pair<String, String> getInfoUser(UserRoles userRole) {
        String user;
        String password;
        switch (userRole) {
            case ADMIN:
                user = getUser();
                password = getPassword();
                break;
            case EDITOR:
                user = getEditorUser();
                password = getPassword();
                break;
            case EDITOR_AND_INVITATIONS:
                user = getEditorInvitationsUser();
                password = getPassword();
                break;
            case EDITOR_AND_USER_ADMIN:
                user = getEditorAdminUser();
                password = getPassword();
                break;
            case EXPLORER:
                user = getExplorerUser();
                password = getPassword();
                break;
            case VIEWER:
                user = getViewerUser();
                password = getPassword();
                break;
            case VIEWER_DISABLED_EXPORT:
                user = getViewerDisabledExport();
                password = getPassword();
                break;
            case DASHBOARD_ONLY:
                user = getDashboardOnlyUser();
                password = getPassword();
                break;
            default:
                throw new IllegalArgumentException("Unknown user role " + userRole);
        }
        return Pair.of(user, password);
    }
}
