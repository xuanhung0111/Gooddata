package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.common.StartPageContext;

public abstract class AnalyticalDesignerAbstractTest extends AbstractProjectTest {

    protected static final String DATE = "Date";
    protected static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    protected static final String NUMBER_OF_LOST_OPPS = "# of Lost Opps.";
    protected static final String NUMBER_OF_OPEN_OPPS = "# of Open Opps.";
    protected static final String NUMBER_OF_OPPORTUNITIES = "# of Opportunities";
    protected static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    protected static final String SNAPSHOT_BOP = "_Snapshot [BOP]";
    protected static final String PERCENT_OF_GOAL = "% of Goal";
    protected static final String IS_WON = "Is Won?";
    protected static final String QUOTA = "Quota";
    protected static final String PRODUCT = "Product";
    protected static final String ACTIVITY_TYPE = "Activity Type";
    protected static final String AMOUNT = "Amount";
    protected static final String STAGE_NAME = "Stage Name";
    protected static final String ACCOUNT = "Account";
    protected static final String DEPARTMENT = "Department";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectCreateCheckIterations = 60; // 5 minutes
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "[Indigo Analyse] ";
    }

    /* A hook for setup test project */
    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void prepareSetupProject() throws Throwable {
    }

    @Test(dependsOnMethods = {"prepareSetupProject"}, groups = {"init"})
    public void initStartPage() {
        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                waitForFragmentVisible(analysisPage);
            }

            @Override
            public String getStartPage() {
                return PAGE_UI_ANALYSE_PREFIX + testParams.getProjectId() + "/reportId/edit";
            }
        };
    }

    protected void checkingOpenAsReport(String screenShot) {
        takeScreenshot(browser, screenShot + "-AD-page", getClass());
        if (!analysisPage.getPageHeader().isExportButtonEnabled()) {
            log.info("[Open as Report] button is disabled. Skip export report!");
            return;
        }

        analysisPage.exportReport();

        BrowserUtils.switchToLastTab(browser);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, screenShot, getClass());
        checkRedBar(browser);

        browser.close();
        BrowserUtils.switchToFirstTab(browser);
    }
}
