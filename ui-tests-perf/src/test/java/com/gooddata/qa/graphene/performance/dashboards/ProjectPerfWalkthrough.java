package com.gooddata.qa.graphene.performance.dashboards;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.greypages.projects.ClearCaches;
import com.gooddata.qa.utils.graphene.Screenshots;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;

@Test(groups = {"projectDashboardPerf"}, description = "Tests for performance of rendering dashboards of given project")
public class ProjectPerfWalkthrough extends AbstractUITest {

    private int startDashboardIndex = 1;
    private boolean singleDashboardComputation = false;
    private boolean clearCaches = false;

    public static final Pattern REPORT_PATTERN =
            Pattern.compile("pdp_D(\\d+)_T(.+)_EC(\\d+)_DWRI(\\d+)_RC(\\d+)_rr(\\d+)");
    public static final String SEPARATOR = ",";

    @BeforeClass
    public void initProperties() {
        startPageContext = new StartPageContext() {
            
            @Override
            public void waitForStartPageLoaded() {
                CheckUtils.waitForElementVisible(By.className("param"), browser);
            }
            
            @Override
            public String getStartPage() {
                return "gdc";
            }
        };

        testParams.setProjectId(testParams.loadProperty("projectId"));
        startDashboardIndex = Integer.valueOf(testParams.loadProperty("startDashboardIndex"));
        clearCaches = Boolean.valueOf(testParams.loadProperty("clearCaches"));
        singleDashboardComputation = Boolean.valueOf(testParams.loadProperty("singleDashboardComputation"));
    }

    @Test(groups = {"perfInit"})
    public void init() throws JSONException {
        signInAtGreyPages(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnMethods = {"init"}, groups = {"perfInit"})
    public void clearCaches() throws JSONException {
        if (clearCaches) {
            System.out.println("Going to clear caches in project: " + testParams.getProjectId());
            openUrl(PAGE_GDC_PROJECTS + "/" + testParams.getProjectId() + "/clearCaches");
            ClearCaches clearCaches = Graphene.createPageFragment(ClearCaches.class, browser.findElement(BY_GP_FORM));
            clearCaches.clearCaches(0);
        } else {
            System.out.println("Caches clear wasn't required");
        }
    }

    @Test(dependsOnGroups = {"perfInit"})
    public void dashboardsWalkthrough() throws JSONException {
        openUrl(PAGE_UI_PROJECT_PREFIX.replace("#s", "#_keepLogs=1&s") + testParams.getProjectId() + "|projectDashboardPage");
        verifyProjectDashboardsAndTabs(startDashboardIndex);
    }

    protected void verifyProjectDashboardsAndTabs(int dashboardStartIndex) throws JSONException {
        waitForDashboardPageLoaded(browser);
        sleepTight(5000);
        waitForElementVisible(dashboardsPage.getRoot());
        int dashboardsCount = dashboardsPage.getDashboardsCount();
        for (int i = dashboardStartIndex; i <= dashboardsCount; i++) {
            dashboardsPage.selectDashboard(i);
            sleepTight(5000);
            System.out.println("Current dashboard index: " + i);
            singleDashboardWalkthrough(i, dashboardsPage.getDashboardName());
            if (singleDashboardComputation) {
                System.out.println("Single dashboard computation was required, following dashboards won't be computed");
                return;
            }
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
            waitForDashboardPageLoaded(browser);
            Screenshots.takeScreenshot(browser, dashboardName + "-tab-" + i + "-" + tabLabels.get(i), this.getClass());
            Assert.assertTrue(tabs.isTabSelected(i), "Tab isn't selected");
            checkRedBar(browser);
        }
        String output = (String) ((JavascriptExecutor) browser).executeScript("return GDC.perf.logger.getCsEvents()");
        createPerfOutputFile(output, dashboardIndex, dashboardName);
        // logger cleanup
        ((JavascriptExecutor) browser).executeScript("GDC.perf.logger._lines = [];");
    }

    private void createPerfOutputFile(String csvContent, int dashboardIndex, String dashboardName)
            throws JSONException {
        File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
        String fileSuffix = dashboardIndex + dashboardName;
        String fileName = "perf-" + fileSuffix.replace(" ", "_") + ".csv";
        File outputFile = new File(mavenProjectBuildDirectory, fileName);
        try {
            FileUtils.writeStringToFile(outputFile, csvContent);
            System.out.println("Created performance statistics at " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        transformLoggerData(outputFile, dashboardIndex);
    }

    private void transformLoggerData(File rawFile, int dashboardIndex) {
        String line = null;
        File outputFile = new File(rawFile.getParent(), "output-" + dashboardIndex + ".csv");
        String header = new StringBuilder().append("dashboardIndex").append(SEPARATOR).append("dashboardId").
                append(SEPARATOR).append("tabId").append(SEPARATOR).append("reportId").append(SEPARATOR).
                append("time").append("\n").toString();
        System.out.println("Going to transform perf file to " + outputFile.getAbsolutePath());
        try {
            FileUtils.writeStringToFile(outputFile, header);
            BufferedReader reader = new BufferedReader(new FileReader(rawFile));
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(SEPARATOR);
                String description = values[3];
                Matcher matcher = REPORT_PATTERN.matcher(description);
                if (matcher.matches()) {
                    String dashboardId = matcher.group(1);
                    String tabId = matcher.group(2);
                    String reportId = matcher.group(5);
                    String output = new StringBuilder().append(dashboardIndex).append(SEPARATOR).append(dashboardId).
                            append(SEPARATOR).append(tabId).append(SEPARATOR).append(reportId).append(SEPARATOR).
                            append(values[2]).append("\n").toString();
                    FileUtils.writeStringToFile(outputFile, output, true);
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found...");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Error appeared during file transformation");
            throw new RuntimeException(e);
        }
    }

}
