package com.gooddata.qa.graphene.performance.dashboards;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test(groups = {"projectDashboardPerf"}, description = "Tests for performance of rendering dashboards of given project")
public class ProjectPerfWalkthrough extends AbstractTest {

    private int startDashboardIndex = 1;

    @BeforeClass
    public void initStartPage() {
        startPage = "gdc";

        projectId = loadProperty("projectId");
        startDashboardIndex = Integer.valueOf(loadProperty("startDashboardIndex"));
    }

    @Test(groups = {"perfInit"})
    public void init() throws JSONException {
        signInAtGreyPages(user, password);
    }

    @Test(dependsOnGroups = {"perfInit"})
    public void dashboardsWalkthrough() throws InterruptedException, JSONException {
        browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX.replace("#s", "#_keepLogs=1&s") + projectId + "|projectDashboardPage");
        verifyProjectDashboardsAndTabs(startDashboardIndex);
    }

    protected void verifyProjectDashboardsAndTabs(int dashboardStartIndex) throws InterruptedException, JSONException {
        waitForDashboardPageLoaded();
        Thread.sleep(5000);
        waitForElementVisible(dashboardsPage.getRoot());
        int dashboardsCount = dashboardsPage.getDashboardsCount();
        for (int i = dashboardStartIndex; i <= dashboardsCount; i++) {
            dashboardsPage.selectDashboard(i);
            Thread.sleep(5000);
            System.out.println("Current dashboard index: " + i);
            singleDashboardWalkthrough(i, dashboardsPage.getDashboardName());
        }
    }

    private void singleDashboardWalkthrough(int dashboardIndex, String dashboardName) throws JSONException {
        DashboardTabs tabs = dashboardsPage.getTabs();
        int numberOfTabs = tabs.getNumberOfTabs();
        System.out.println("Number of tabs on dashboard " + dashboardName + ": " + numberOfTabs);
        List<String> tabLabels = tabs.getAllTabNames();
        System.out.println("These tabs are available for selected project: " + tabLabels.toString());
        for (int i = 0; i < tabLabels.size(); i++) {
            tabs.openTab(i);
            System.out.println("Switched to tab with index: " + i + ", label: " + tabs.getTabLabel(i));
            waitForDashboardPageLoaded();
            Screenshots.takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            Assert.assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar();
        }
        String output = (String) ((JavascriptExecutor) browser).executeScript("return GDC.perf.logger.getCsEvents()");
        createPerfOutputFile(output, dashboardIndex + "-" + dashboardName);
        ((JavascriptExecutor) browser).executeScript("GDC.perf.logger._lines = [];");
    }

    private void createPerfOutputFile(String csvContent, String fileSuffix) throws JSONException {
        File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
        String fileName = "perf-" + fileSuffix.replace(" ", "_") + ".csv";
        File outputFile = new File(mavenProjectBuildDirectory, fileName);
        try {
            FileUtils.writeStringToFile(outputFile, csvContent);
            System.out.println("Created performance statistics at ./target/" + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
