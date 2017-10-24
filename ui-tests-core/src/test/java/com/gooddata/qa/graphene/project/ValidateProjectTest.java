package com.gooddata.qa.graphene.project;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;

import java.io.IOException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.fragments.greypages.md.validate.ValidateFragment;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;

public class ValidateProjectTest extends AbstractProjectTest {

    private static final String DASHBOARD = "New-Dashboard";
    private static final String REPORT = "Report";
    private static final String VALIDATE_STATUS = "OK";

    private String currentProjectId;
    private int validateTimeoutInSecond;

    @Override
    protected void initProperties() {
        // use empty project
        currentProjectId = testParams.getProjectId();

        validateAfterClass = false;
        validateTimeoutInSecond = testParams.getProjectDriver() == ProjectDriver.POSTGRES ?
                testParams.getDefaultTimeout() : testParams.getExtendedTimeout();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void validateBlankProject() {
        String validateStatus = initValidatePage().validate(validateTimeoutInSecond);
        takeScreenshot(browser, "Validate-blank-project", getClass());
        assertEquals(validateStatus, VALIDATE_STATUS);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void validateGoodSalesProject() {
        String projectId = createProjectUsingFixture(projectTitle, GOODSALES);
        testParams.setProjectId(projectId);

        try {
            Attribute stageNameAttribute = getAttributeByTitle(ATTR_STAGE_NAME);
            Metric amountMetric = createMetric(METRIC_NUMBER_OF_ACTIVITIES, format("SELECT COUNT([%s])",
                    getAttributeByTitle(ATTR_ACTIVITY).getUri()));

            Report report = createReportViaRest(GridReportDefinitionContent.create(REPORT,
                    singletonList(METRIC_GROUP),
                    singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
                    singletonList(new MetricElement(amountMetric))));

            initDashboardsPage()
                    .addNewDashboard(DASHBOARD)
                    .addReportToDashboard(report.getTitle())
                    .saveDashboard();
            takeScreenshot(browser, "GoodSales-dashboard-with-report", getClass());
            checkRedBar(browser);

            String validateStatus = initValidatePage().validate(validateTimeoutInSecond);
            takeScreenshot(browser, "Validate-GoodSales-project", getClass());
            assertEquals(validateStatus, VALIDATE_STATUS);

        } finally {
            testParams.setProjectId(currentProjectId);
            ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void validateProjectUsingUploadData() throws IOException {
        String projectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), projectTitle,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());
        testParams.setProjectId(projectId);

        try {
            String csvFilePath = new CsvFile("User")
                    .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"))
                    .rows("Khoa", "100")
                    .saveToDisc(testParams.getCsvFolder());
            uploadCSV(csvFilePath);

            Attribute firstNameAttribute = getMdService().getObj(getProject(), Attribute.class,
                    title("Firstname"));

            String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
            String expression = String.format("SELECT SUM([%s])", numberFactUri);

            Metric numberMetric = getMdService().createObj(getProject(),
                    new Metric("SumOfNumber", expression, "#,##0"));

            Report report = createReportViaRest(GridReportDefinitionContent.create(REPORT,
                    singletonList(METRIC_GROUP),
                    singletonList(new AttributeInGrid(firstNameAttribute.getDefaultDisplayForm().getUri(), firstNameAttribute.getTitle())),
                    singletonList(new MetricElement(numberMetric))));

            initDashboardsPage()
                    .addNewDashboard(DASHBOARD)
                    .addReportToDashboard(report.getTitle())
                    .saveDashboard();
            takeScreenshot(browser, "Dashboard-with-report", getClass());
            checkRedBar(browser);

            String validateStatus = initValidatePage().validate(validateTimeoutInSecond);
            takeScreenshot(browser, "Validate-project-using-data-upload", getClass());
            assertEquals(validateStatus, VALIDATE_STATUS);

        } finally {
            testParams.setProjectId(currentProjectId);
            ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
        }
    }

    private ValidateFragment initValidatePage() {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/validate");
        return Graphene.createPageFragment(ValidateFragment.class, waitForElementVisible(By.tagName("form"), browser));
    }
}
