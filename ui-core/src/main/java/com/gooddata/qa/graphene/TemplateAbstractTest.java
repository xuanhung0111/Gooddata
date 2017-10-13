package com.gooddata.qa.graphene;


import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import java.util.HashMap;
import java.util.Map;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_AND_MORE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_LEADERBOARDS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_QUARTERLY_TRENDS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_SALES_VELOCITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_SEASONALITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WATERFALL_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_WHATS_CHANGED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.GOODSALES_TEMPLATE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class TemplateAbstractTest extends AbstractProjectTest{

    protected String projectTemplate;
    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    /**
     * This is temporarily used to handle some special cases which require design changes.
     * DO NOT EXTEND THIS ABSTRACT FOR NEW TEST
     */
    @Override
    protected void initProperties() {
        // GS template is used by default
        projectTitle = "GoodSales-test";
        projectTemplate = GOODSALES_TEMPLATE;

        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[]{
                DASH_TAB_OUTLOOK, DASH_TAB_WHATS_CHANGED, DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_LEADERBOARDS,
                DASH_TAB_ACTIVITIES, DASH_TAB_SALES_VELOCITY,
                DASH_TAB_QUARTERLY_TRENDS, DASH_TAB_SEASONALITY, DASH_TAB_AND_MORE});
    }

    @Override
    protected void createNewProject() throws Throwable {
        if (!canAccessGreyPage(browser)) {
            System.out.println("Use REST api to create project.");
            testParams.setProjectId(ProjectRestUtils.createProject(getGoodDataClient(), projectTitle,
                    projectTemplate, testParams.getAuthorizationToken(), ProjectDriver.POSTGRES,
                    testParams.getProjectEnvironment()));

        } else {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());

            projectTitle += "-" + testParams.getProjectDriver().name();
            if (projectTemplate.isEmpty()) {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                        testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations));
            } else {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, projectTemplate,
                        testParams.getAuthorizationToken(), ProjectDriver.POSTGRES, testParams.getProjectEnvironment(),
                        projectCreateCheckIterations));

                if (testParams.getProjectDriver().equals(ProjectDriver.VERTICA)) {
                    String exportToken = exportProject(true, true, false, projectCreateCheckIterations * 5);
                    deleteProject(testParams.getProjectId());

                    openUrl(PAGE_GDC_PROJECTS);
                    waitForElementVisible(gpProject.getRoot());
                    testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                            testParams.getAuthorizationToken2(), testParams.getProjectDriver(),
                            testParams.getProjectEnvironment(), projectCreateCheckIterations));
                    importProject(exportToken, projectCreateCheckIterations * 5);
                }
            }
            Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
        }
    }
}
