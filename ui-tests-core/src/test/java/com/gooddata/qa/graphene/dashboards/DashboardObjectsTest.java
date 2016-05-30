package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;

import java.util.Calendar;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

@Test(groups = {"dashboardObjects"},
      description = "Tests for simple project and dashboard objects functionality in GD platform")
public class DashboardObjectsTest extends AbstractProjectTest {

    private final String variableName = "FVariable";
    private static final long expectedDashboardExportSize = 65000L;
    private static final String REPORT_NAME = "Amount Overview table";
    private static final String METRIC_NAME = "Sum of Amount";
    private static final String FACT_NAME ="Amount";
    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private static final int YEAR_OF_DATA = 2007;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "SimpleProject-test-dashboard-objects";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadDataTest() {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll", getClass());
    }

    @Test(dependsOnMethods = {"uploadDataTest"})
    public void createvariableTest() {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable(variableName)
                .withAttribute("Education")
                .withAttributeElements("Bachelors Degree", "Graduate Degree"));
    }

    @Test(dependsOnMethods = {"uploadDataTest"})
    public void changeStateLabelTest() {
        initAttributePage();
        attributePage.configureAttributeLabel("State", AttributeLabelTypes.US_STATE_NAME);
    }

    @Test(dependsOnMethods = {"changeStateLabelTest", "createvariableTest"})
    public void addDashboardObjectsTest() {;
        createMetric(METRIC_NAME, 
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(FACT_NAME))),
                DEFAULT_METRIC_FORMAT);

        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_NAME)
                .withHows("Lastname", "Firstname", "Education", "Position", "Department"),
                REPORT_NAME);

        initDashboardsPage();
        String dashboardName = "Test";
        dashboardsPage.addNewDashboard(dashboardName);
        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "County");
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, this.variableName);
        dashboardEditBar.addTimeFilterToDashboard(0, String.format("%s ago", 
                Calendar.getInstance().get(Calendar.YEAR) - YEAR_OF_DATA));
        dashboardEditBar.addReportToDashboard(REPORT_NAME);
        sleepTightInSeconds(2);
        dashboardEditBar.addTextToDashboard(TextObject.HEADLINE, "Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.SUB_HEADLINE, "Sub-Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.DESCRIPTION, "Description", "google.com");
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_NAME);
        sleepTightInSeconds(2);
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART, METRIC_NAME);
        sleepTightInSeconds(2);
        dashboardEditBar.addWebContentToDashboard("https://www.gooddata.com");
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"addDashboardObjectsTest"})
    public void printDashboardTest() {
        initDashboardsPage();
        String exportedDashboardName = dashboardsPage.printDashboardTab(0);
        verifyDashboardExport(exportedDashboardName.replace(" ", "_"), expectedDashboardExportSize);
        checkRedBar(browser);
    }
}
