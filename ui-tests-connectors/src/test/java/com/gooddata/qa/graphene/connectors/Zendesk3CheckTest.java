package com.gooddata.qa.graphene.connectors;

import com.gooddata.qa.graphene.enums.Connectors;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;

@Test(groups = {"connectors", "zendesk3"}, description = "Checklist tests for Zendesk3 connector in GD platform")
public class Zendesk3CheckTest extends AbstractZendeskCheckTest {

    @BeforeClass
    public void loadRequiredProperties() {
        zendeskApiUrl = testParams.loadProperty("connectors.zendesk.apiUrl");
        zendeskUploadUser = testParams.loadProperty("connectors.zendesk.uploadUser");
        zendeskUploadUserPassword = testParams.loadProperty("connectors.zendesk.uploadUserPassword");

        connectorType = Connectors.ZENDESK3;
        expectedDashboardsAndTabs = new HashMap<String, String[]>();
        expectedDashboardsAndTabs.put("Advanced Metrics", new String[]{
                "Overview", "Ticket Creation", "Ticket Distribution", "Performance", "Backlog", "Open Issues",
                "Customer Satisfaction"
        });
        expectedDashboardsAndTabs.put("GoodData for Zendesk", new String[]{
                "Ticket Creation", "Ticket Resolution", "Agent Performance", "Watch List"
        });
        expectedDashboardsAndTabs.put("Release 3 New Dashboards", new String[]{
                "Overview w/ Business Hours", "Staffing and Volume Heat Maps"
        });
    }

    @Test(groups = {"connectorWalkthrough", "connectorIntegration"},
            dependsOnMethods = {"testZendeskIntegration"})
    public void testIncrementalSynchronization() throws JSONException, InterruptedException {
        scheduleIntegrationProcess(integrationProcessCheckLimit, 1);
    }

    @Override
    public String openZendeskSettingsUrl() {
        return gotoIntegrationSettings();
    }
}
