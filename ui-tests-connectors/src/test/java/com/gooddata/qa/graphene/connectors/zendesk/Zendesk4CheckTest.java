/*
 * Copyright (C) 2007-2013, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.connectors.zendesk;

import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.OneNumberReportDefinitionContent;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.connectors.ZendeskHelper;
import com.gooddata.qa.graphene.enums.Connectors;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.report.ReportExportFormat;
import com.gooddata.report.ReportService;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

@SuppressWarnings("serial")
public class Zendesk4CheckTest extends AbstractZendeskCheckTest {

    private String zendeskAPIUser;
    private String zendeskAPIPassword;

    private ZendeskHelper zendeskHelper;
    private boolean useApiProxy;

    private static final String JSON_USER_CREATE = getResourceAsString("/zendesk-api/user-create.json");

    private static final String JSON_ORGANIZATION_CREATE = getResourceAsString("/zendesk-api/organization-create.json");

    private static final String USERS_REPORT_NAME = "Users count";
    private static final String ORGANIZATIONS_REPORT_NAME = "Organizations count";

    private int createdZendeskUserId;
    private int createdZendeskOrganizationId;
    private Map<String, Integer> reportMetricsResults;

    private MetadataService mdService = null;
    private Project project = null;

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = testParams.loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = testParams.loadProperty("connectors.zendesk4.uploadUser");
        zendeskUploadUserPassword = testParams.loadProperty("connectors.zendesk4.uploadUserPassword");

        connectorType = Connectors.ZENDESK4;
        expectedDashboardsAndTabs = new HashMap<>();
        expectedDashboardsAndTabs.put("Insights - View Only", new String[]{
                "Overview", "Tickets", "Satisfaction", "Efficiency", "Agent Activity", "SLAs", "Learn More"
        });
        expectedDashboardsAndTabs.put("My dashboard", new String[]{"First Tab"});
        zendeskAPIUser = testParams.loadProperty("connectors.zendesk.apiUser");
        zendeskAPIPassword = testParams.loadProperty("connectors.zendesk.apiUserPassword");
        useApiProxy = Boolean.parseBoolean(testParams.loadProperty("http.client.useApiProxy"));

        reportMetricsResults = new HashMap<>();
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void initializeGoodDataSDK() {
        goodDataClient = getGoodDataClient();
        mdService = goodDataClient.getMetadataService();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskUsersReport() throws IOException {
        createOneNumberReportDefinition(USERS_REPORT_NAME, "# Users");
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void createZendeskOrganizationsReport() throws IOException {
        createOneNumberReportDefinition(ORGANIZATIONS_REPORT_NAME, "# Organizations");
    }

    @Test(dependsOnMethods = {"testZendeskIntegration"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void initZendeskApiClient() {
        if (zendeskApiUrl.contains("staging") && !zendeskAPIUser.isEmpty() && !zendeskAPIPassword.isEmpty()) {
            zendeskHelper = new ZendeskHelper(new RestApiClient(zendeskApiUrl.replace("https://", ""),
                    zendeskAPIUser, zendeskAPIPassword, false, useApiProxy));
        } else {
            fail("Zendesk staging API is not used, tests for adding new objects will be skipped");
        }
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskUsersReport"}, groups = {"zendeskApiTests",
            "connectorWalkthrough"})
    public void testUsersCount() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        reportMetricsResults.put(USERS_REPORT_NAME, gdUsersCount);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(), ZendeskHelper.ZendeskObject.USER);
    }

    @Test(dependsOnMethods = {"initZendeskApiClient", "createZendeskOrganizationsReport"},
            groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testOrganizationsCount() throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        reportMetricsResults.put(ORGANIZATIONS_REPORT_NAME, gdOrganizationsCount);
        // Zendesk 4 project contains one dummy organization - ATP-2954
        compareObjectsCount(gdOrganizationsCount, zendeskHelper.getNumberOfOrganizations() + 1,
                ZendeskHelper.ZendeskObject.ORGANIZATION);
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewUser() throws IOException, JSONException {
        createdZendeskUserId = zendeskHelper.createNewUser(
                format(JSON_USER_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnMethods = {"testUsersCount"}, groups = {"zendeskApiTests", "newZendeskObjects",
            "connectorWalkthrough"})
    public void testAddNewOrganization() throws IOException, JSONException {
        createdZendeskOrganizationId = zendeskHelper.createNewOrganization(
                format(JSON_ORGANIZATION_CREATE, ZendeskHelper.getCurrentTimeIdentifier()));
    }

    @Test(dependsOnGroups = {"newZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronization() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testUsersCountAfterIncrementalSync() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(),
                ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME) + 1,
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronization"}, groups = {"zendeskApiTests",
            "zendeskAfterCreateTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterIncrementalSync()
            throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        compareObjectsCount(gdOrganizationsCount,
                zendeskHelper.getNumberOfOrganizations() + 1, // + 1 dummy organization - ATP-2954
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME) + 1,
                "Organizations count doesn't match after incremental sync");
    }

    @Test(dependsOnGroups = {"updateZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsUpdate() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testUsersCountAfterIncrementalSync"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskUser() throws IOException {
        zendeskHelper.deleteUser(createdZendeskUserId);
    }

    @Test(dependsOnMethods = {"testOrganizationsCountAfterIncrementalSync"}, groups = {"zendeskApiTests",
            "connectorWalkthrough", "deleteZendeskObjects"})
    public void deleteZendeskOrganization() throws IOException {
        zendeskHelper.deleteOrganization(createdZendeskOrganizationId);
    }

    @Test(dependsOnGroups = {"deleteZendeskObjects"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testIncrementalSynchronizationAfterObjectsDeletion() throws JSONException {
        scheduleIntegrationProcess(integrationProcessCheckLimit);
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testUsersCountAfterDeletion() throws IOException, JSONException {
        int gdUsersCount = getNumberFromGDReport(USERS_REPORT_NAME);
        compareObjectsCount(gdUsersCount, zendeskHelper.getNumberOfUsers(), ZendeskHelper.ZendeskObject.USER);
        assertEquals(gdUsersCount, reportMetricsResults.get(USERS_REPORT_NAME).intValue(),
                "Users count doesn't match after incremental sync");
    }

    @Test(dependsOnMethods = {"testIncrementalSynchronizationAfterObjectsDeletion"}, groups = {"zendeskApiTests",
            "zendeskAfterDeletionTests", "connectorWalkthrough"})
    public void testOrganizationsCountAfterDeletion() throws IOException, JSONException {
        int gdOrganizationsCount = getNumberFromGDReport(ORGANIZATIONS_REPORT_NAME);
        compareObjectsCount(gdOrganizationsCount,
                zendeskHelper.getNumberOfOrganizations() + 1, // + 1 dummy organization - ATP-2954
                ZendeskHelper.ZendeskObject.ORGANIZATION);
        assertEquals(gdOrganizationsCount, reportMetricsResults.get(ORGANIZATIONS_REPORT_NAME).intValue(),
                "Organizations count doesn't match after incremental sync");
    }

    @Test(dependsOnGroups = {"zendeskAfterDeletionTests"}, groups = {"zendeskApiTests", "connectorWalkthrough"})
    public void testFullLoad() throws IOException, JSONException {
        runConnectorProjectFullLoad();
    }

    private void createOneNumberReportDefinition(String reportName, String metricTitle) {
        Metric metric = mdService.getObj(project, Metric.class, Restriction.title(metricTitle));
        createOneNumberReportDefinition(reportName, metric);
    }

    private void createOneNumberReportDefinition(String reportName, Metric metric) {
        if (mdService.find(project, ReportDefinition.class, Restriction.title(reportName)).isEmpty()) {
            ReportDefinition definition = 
                    OneNumberReportDefinitionContent.create(reportName, asList(METRIC_GROUP),
                            Collections.emptyList(),
                            singletonList(new MetricElement(metric)));
            mdService.createObj(project, definition);
        } else {
            System.out.println("Required report definition already exists: " + reportName);
        }
    }

    private int getNumberFromGDReport(String reportName) throws IOException {
        final ByteArrayOutputStream output = exportReport(reportName);

        ICsvListReader listReader = null;
        try {
            listReader = getCsvListReader(output);

            List<String> reportResult = listReader.read();
            return Integer.valueOf(reportResult.get(1));
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }

    private ByteArrayOutputStream exportReport(String reportName) {
        return exportReport(mdService.getObj(project, ReportDefinition.class, Restriction.title(reportName)));
    }

    private ByteArrayOutputStream exportReport(ReportDefinition rd) {
        ReportService reportService = goodDataClient.getReportService();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportReport(rd, ReportExportFormat.CSV, output).get();

        System.out.println("Going to read a csv file with following content: \n" + output.toString());

        return output;
    }

    private void compareObjectsCount(int actual, int expected, ZendeskHelper.ZendeskObject objectName) {
        assertEquals(actual, expected, objectName.getPluralName() + " count don't match (against zendesk API)");
    }

    @Override
    public String openZendeskSettingsUrl() {
        openUrl(getIntegrationUri());
        Graphene.guardHttp(waitForElementVisible(BY_GP_SETTINGS_LINK, browser)).click();
        return browser.getCurrentUrl();
    }

    private static CsvListReader getCsvListReader(ByteArrayOutputStream output) throws IOException {
        final CsvListReader reader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(output.toByteArray())),
                CsvPreference.STANDARD_PREFERENCE);
        reader.getHeader(true);
        return reader;
    }

    private static class FieldChange {
        private final String fieldAlias;
        private final String newValue;
        private final String oldValue;
        private final boolean toBeChecked;
        private final boolean toBeSend;

        FieldChange(String fieldAlias, String newValue, String oldValue) {
            this(fieldAlias, newValue, oldValue, true, true);
        }

        FieldChange(String fieldAlias, String newValue, String oldValue, boolean toBeChecked, boolean toBeSend) {
            this.fieldAlias = fieldAlias;
            this.newValue = newValue;
            this.oldValue = oldValue;
            this.toBeChecked = toBeChecked;
            this.toBeSend = toBeSend;
        }
    }
}
