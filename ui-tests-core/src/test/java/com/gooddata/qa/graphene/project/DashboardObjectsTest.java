package com.gooddata.qa.graphene.project;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.VariableTypes;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

@Test(groups = {"dashboardObjects"}, description = "Tests for simple project and dashboard objects functionality in GD platform")
public class DashboardObjectsTest extends AbstractProjectTest {

    private final String variableName = "FVariable";
    private static final long expectedDashboardExportSize = 65000L;

    @BeforeClass
    public void initStartPage() {
        projectTitle = "simple-dashboard-objects-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void uploadDataTest() throws InterruptedException {
        String csvFilePath = loadProperty("csvFilePath");
        uploadCSV(csvFilePath + "payroll.csv", null, "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadDataTest"}, groups = {"tests"})
    public void createvariableTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|variables");
        Map<String, String> data = new HashMap<String, String>();
        data.put("variableName", this.variableName);
        data.put("attribute", "Education");
        data.put("attrElements", "Bachelors Degree, Graduate Degree");
        data.put("userValueFlag", "false");
        variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    @Test(dependsOnMethods = {"uploadDataTest"}, groups = {"tests"})
    public void changeStateLabelTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
        attributePage.configureAttributeLabel("State", AttributeLabelTypes.US_STATE_NAME);
    }

    @Test(dependsOnMethods = {"changeStateLabelTest", "createvariableTest"}, groups = {"tests"})
    public void addDashboardObjectsTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        String dashboardName = "Test";
        dashboardsPage.addNewDashboard(dashboardName);
        dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "County");
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, this.variableName);
        dashboardEditBar.addTimeFilterToDashboard(0, "7 ago");
        dashboardEditBar.addReportToDashboard("Amount Overview table");
        Thread.sleep(2000);
        dashboardEditBar.addTextToDashboard(TextObject.HEADLINE, "Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.SUB_HEADLINE, "Sub-Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.DESCRIPTION, "Description", "google.com");
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, "Avg of Amount");
        Thread.sleep(2000);
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART, "Avg of Amount");
        Thread.sleep(2000);
        dashboardEditBar.addWebContentToDashboard();
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"addDashboardObjectsTest"}, groups = {"tests"})
    public void printDashboardTest() throws InterruptedException {
        initDashboardsPage();
        String exportedDashboardName = dashboardsPage.printDashboardTab(0);
        verifyDashboardExport(exportedDashboardName.replace(" ", "_"), expectedDashboardExportSize);
        checkRedBar();
        successfulTest = true;
    }
}