package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.VariableTypes;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

@Test(groups = {"dashboardObjects"}, description = "Tests for simple project and dashboard objects functionality in GD platform")
public class DashboardObjectsTest extends AbstractProjectTest {

    protected static final String ATTRIBUTE_NAME = "State";
    protected static final String ATTRIBUTE_LABEL_TYPE = "US States (Name)";

    protected static final String ATTRIBUTE_FOR_VARIABLE = "Education";

    protected static final String PROMPT_NAME = "FVariable";
    protected static final String FILTER_ATTRIBUTE_NAME = "County";
    protected static final String REPORT_NAME = "Amount Overview table";
    protected static final String METRIC_NAME = "Avg of Amount";

    private static final long expectedDashboardExportSize = 65000L;
    private List<String> prompt_elements;
    private Map<String, String> data;

    @BeforeClass
    public void initStartPage() {
        projectTitle = "simple-dashboard-objects-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void uploadDataTest() throws InterruptedException {
        String csvFilePath = loadProperty("csvFilePath");
        uploadCSV(csvFilePath, null, "simple-ws");

    }

    @Test(dependsOnMethods = {"uploadDataTest"}, groups = {"tests"})
    public void createvariableTest() throws InterruptedException {
        browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|variables");
        data = new HashMap<String, String>();
        data.put("variableName", PROMPT_NAME);
	data.put("attribute", ATTRIBUTE_FOR_VARIABLE);
        prompt_elements = new ArrayList<String>();
        prompt_elements.add("Bachelors Degree");
        prompt_elements.add("Graduate Degree");
	String str = StringUtils.join(prompt_elements, ", ");
	data.put("attrElements", str);
	data.put("userValueFlag", "false");
        variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    @Test(dependsOnMethods = {"createvariableTest"}, groups = {"tests"})
    public void changeStateLabelTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
        waitForElementVisible(attributesTable.getRoot());
        waitForDataPageLoaded();
        attributesTable.selectObject(ATTRIBUTE_NAME);
        waitForElementVisible(attributeDetailPage.getRoot());
        waitForObjectPageLoaded();
        Assert.assertEquals(attributeDetailPage.getAttributeName(),
                ATTRIBUTE_NAME, "Invalid attribute name on detail page");
        attributeDetailPage.selectLabelType(ATTRIBUTE_LABEL_TYPE);
        Thread.sleep(2000);
        Assert.assertEquals(attributeDetailPage.getAttributeLabelType(),
                ATTRIBUTE_LABEL_TYPE, "Label type not set properly");

    }

    @Test(dependsOnMethods = {"changeStateLabelTest"}, groups = {"tests"})
    public void addDashboardObjectsTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        String dashboardName = "Test";
        dashboardsPage.addNewDashboard(dashboardName);
        dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, FILTER_ATTRIBUTE_NAME);
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, PROMPT_NAME);
        dashboardEditBar.addTimeFilterToDashboard(0);
        dashboardEditBar.addReportToDashboard(REPORT_NAME);
        Thread.sleep(2000);
        dashboardEditBar.addTextToDashboard(TextObject.HEADLINE, "Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.SUB_HEADLINE, "Sub-Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.DESCRIPTION, "Description", "google.com");
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_NAME);
        Thread.sleep(2000);
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART, METRIC_NAME);
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
