package com.gooddata.qa.graphene.indigo.analyze.common;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.common.StartPageContext;

public abstract class AbstractAnalyseTest extends AbstractProjectTest {

    protected static final String DATE = "Date";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "[Indigo Analyse] ";
    }

    /* A hook for setup test project */
    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public abstract void prepareSetupProject() throws Throwable;

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
