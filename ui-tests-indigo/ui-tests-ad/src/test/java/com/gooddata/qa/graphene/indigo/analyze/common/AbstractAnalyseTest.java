package com.gooddata.qa.graphene.indigo.analyze.common;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractAnalyseTest extends GoodSalesAbstractTest {

    protected static final String DATE = "Date";

    @FindBy(className = AnalysisPage.MAIN_CLASS)
    protected AnalysisPage analysisPage;

    @Override
    public void initProperties() {
        super.initProperties(); // GS fixture is used by default
    }

    protected void checkingOpenAsReport(String screenShot) {
        takeScreenshot(browser, screenShot + "-AD-page", getClass());
        if (!analysisPage.getPageHeader().isExportButtonEnabled()) {
            log.info("[Open as Report] button is disabled. Skip export report!");
            return;
        }

        analysisPage.exportReport();

        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            takeScreenshot(browser, screenShot, getClass());
            checkRedBar(browser);

        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    protected List<String> parseFilterText(String filterTitle) {
        final List<String> filterTexts = new ArrayList<>();
        final int splitIndex = filterTitle.lastIndexOf(":");

        filterTexts.add(filterTitle.substring(0, splitIndex).trim());
        filterTexts.add(filterTitle.substring(splitIndex + 1).trim());
        return filterTexts;
    }
}
