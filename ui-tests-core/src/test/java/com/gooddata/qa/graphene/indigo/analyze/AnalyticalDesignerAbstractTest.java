package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;

public abstract class AnalyticalDesignerAbstractTest extends AbstractProjectTest {

    protected static final String DATE = "Date";
    protected boolean isWalkmeTurnOff = false;

    @BeforeClass
    public void initProperties() {
        projectCreateCheckIterations = 60; // 5 minutes
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"turnOfWalkme"}, priority = 1)
    public void turnOffWalkme() {
        if (isWalkmeTurnOff) {
            return;
        }

        initAnalysePage();

        try {
            WebElement walkmeCloseElement = waitForElementVisible(By.className("walkme-action-close"), browser);
            walkmeCloseElement.click();
            waitForElementNotPresent(walkmeCloseElement);
        } catch (TimeoutException e) {
            System.out.println("Walkme dialog is not appeared!");
        }
    }

    @Test(dependsOnGroups = {"turnOfWalkme"}, alwaysRun = true, groups = {"init"})
    public void prepareToTestAdAfterTurnOffWalkme() {
    }

    protected void checkingOpenAsReport(String screenShot) {
        takeScreenshot(browser, screenShot + "-AD-page", getClass());
        if (!analysisPage.isExportToReportButtonEnabled()) {
            System.out.println("[Open as Report] button is disabled. Skip export report!");
            return;
        }

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, screenShot, getClass());
        checkRedBar(browser);
        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }
}
