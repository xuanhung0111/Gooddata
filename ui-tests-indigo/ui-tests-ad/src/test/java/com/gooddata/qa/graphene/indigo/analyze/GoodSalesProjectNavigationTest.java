package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.utils.http.RestUtils;

public class GoodSalesProjectNavigationTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Project-Navigation-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void openAnalysePageAfterDeleteAnotherProject() throws ParseException, JSONException, IOException {
        String projectId = testParams.getProjectId();
        String otherProjectId = RestUtils.createProject(getRestApiClient(), projectTitle, projectTitle,
                GOODSALES_TEMPLATE, testParams.getAuthorizationToken(),
                testParams.getDwhDriver(), testParams.getProjectEnvironment());

        try {
            testParams.setProjectId(otherProjectId);

            initDashboardsPage();
            assertThat(browser.getCurrentUrl(), containsString(otherProjectId));

            createReport(new UiReportDefinition()
                    .withName("Report")
                    .withWhats(AMOUNT)
                    .withHows(STAGE_NAME),
                    "Create Report");

            initProjectsAndUsersPage();
            projectAndUsersPage.deteleProject();
            waitForProjectsPageLoaded(browser);

        } finally {
            testParams.setProjectId(projectId);
        }

        initAnalysePageByUrl();
        takeScreenshot(browser, "Analyse page loaded", getClass());
        assertThat(browser.getCurrentUrl(), containsString(projectId));
    }
}
