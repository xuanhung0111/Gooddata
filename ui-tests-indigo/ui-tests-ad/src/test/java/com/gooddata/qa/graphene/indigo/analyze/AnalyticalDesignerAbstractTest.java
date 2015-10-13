package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.IOException;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

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

    protected boolean isWalkmeTurnOff = false;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectCreateCheckIterations = 60; // 5 minutes
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"enableAnalyticalDesigner"})
    public void enableAnalyticalDesigner() throws IOException, JSONException {
        RestUtils.setFeatureFlags(getRestApiClient(), FeatureFlagOption.createFeatureClassOption(
                ProjectFeatureFlags.ANALYTICAL_DESIGNER.getFlagName(), true));
    }

    @Test(dependsOnGroups = {"enableAnalyticalDesigner"}, groups = {"turnOffWalkme"}, priority = 1)
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

    @Test(dependsOnGroups = {"turnOffWalkme"}, alwaysRun = true, groups = {"init"})
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
