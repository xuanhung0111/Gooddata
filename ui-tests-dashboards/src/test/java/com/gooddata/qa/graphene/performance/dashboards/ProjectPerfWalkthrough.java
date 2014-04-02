package com.gooddata.qa.graphene.performance.dashboards;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Test(groups = {"projectDashboardPerf"}, description = "Tests for performance of rendering dashboards of given project")
public class ProjectPerfWalkthrough extends AbstractTest {

    @BeforeClass
    public void initStartPage() {
        startPage = "gdc";

        projectId = loadProperty("projectId");
    }

    @Test(groups = {"perfInit"})
    public void init() throws JSONException {
        signInAtGreyPages(user, password);
    }

    @Test(dependsOnGroups = {"perfInit"})
    public void dashboardsWalkthrough() throws InterruptedException, JSONException {
        browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX.replace("#s", "#_keepLogs=1&s") + projectId + "|projectDashboardPage");
        verifyProjectDashboardsAndTabs(false, null, false);
        String output = (String) ((JavascriptExecutor) browser).executeScript("return GDC.perf.logger.getCsEvents()");
        createPerfOutputFile(output);
        successfulTest = true;
    }

    private void createPerfOutputFile(String csvContent) throws JSONException {
        File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
        File outputFile = new File(mavenProjectBuildDirectory, "perf.csv");
        try {
            FileUtils.writeStringToFile(outputFile, csvContent);
            System.out.println("Created performance statistics at ./target/perf.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
