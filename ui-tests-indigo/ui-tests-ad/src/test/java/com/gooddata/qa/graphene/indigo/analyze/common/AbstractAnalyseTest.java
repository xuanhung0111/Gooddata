package com.gooddata.qa.graphene.indigo.analyze.common;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.testng.annotations.Test;

public abstract class AbstractAnalyseTest extends GoodSalesAbstractTest {

    protected static final String DATE = "Date";

    @FindBy(className = AnalysisPage.MAIN_CLASS)
    protected AnalysisPage analysisPage;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties(); // GS fixture is used by default
    }

    @Override
    protected void customizeProject() throws Throwable {
        // set analyse page as start page context
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
