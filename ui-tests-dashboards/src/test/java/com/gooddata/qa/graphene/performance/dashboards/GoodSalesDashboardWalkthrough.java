package com.gooddata.qa.graphene.performance.dashboards;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "dashboardPerf" }, description = "Tests for performance od rendering dashboards in GoodSales project")
public class GoodSalesDashboardWalkthrough extends AbstractTest { 

	private static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "gdc";

        expectedGoodSalesDashboardsAndTabs = new HashMap<String, String[]>();
        expectedGoodSalesDashboardsAndTabs.put("Pipeline Analysis", new String[]{
                "Outlook", "What's Changed", "Waterfall Analysis", "Leaderboards", "Activities",
                "Sales Velocity", "Quarterly Trends", "Seasonality", "...and more"
        });
	}
	
	@Test(groups = { "GoodSalesPerfInit" } )
	public void init() throws JSONException {
		signInAtGreyPages(user, password);
	}
	
	@Test(dependsOnGroups = { "GoodSalesPerfInit" })
	public void createProject() throws JSONException, InterruptedException {
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject("GoodSales-perf-test", "", GOODSALES_TEMPLATE, authorizationToken, 240);
		Screenshots.takeScreenshot(browser, "GoodSales-perf-project-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createProject" })
	public void dashboardsWalkthrough() throws InterruptedException, JSONException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX.replace("#s", "#_keepLogs=1&s") + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		for (int i = 1; i <= 10; i++) {
			System.out.println("Iteration:" + i);
			verifyProjectDashboardsAndTabs(true, expectedGoodSalesDashboardsAndTabs, false);
		}
		String output = (String) ((JavascriptExecutor) browser).executeScript("return GDC.perf.logger.getCsEvents()");
		createPerfOutputFile(output);
		successfulTest = true;
	}
	
	@Test(dependsOnMethods = { "dashboardsWalkthrough" })
	public void deleteProject() {
		deleteProjectByDeleteMode(successfulTest);
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
		createPerfOutputFileWithMetadata(outputFile);
	}
	
	private void createPerfOutputFileWithMetadata(File file) throws JSONException {
		File outputFile = new File(file.getParentFile(), "perf_log.csv");
		StringBuilder result = new StringBuilder();
		String metaHeaders = "time,branch,build,builduri,gitcommit,browser";
		String branch = System.getProperty("GD_PERFTIME_BRANCH_LABEL", "branch-not-set");
		String buildNumber = System.getenv("BUILD_NUMBER");
		String buildUrl = System.getenv("BUILD_URL");
		String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
		String metaValues = time + "," + branch + "," + buildNumber + "," + buildUrl + "," + getClientVersion() + "," + System.getProperty("browser", "firefox") + ",";
		result.append(metaHeaders + ",sessionid,start,duration,action" + "\n");
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(file);
			while (it.hasNext()) {
				String line = it.nextLine();
				result.append(metaValues).append(line).append("\n");
			}
			FileUtils.writeStringToFile(outputFile, result.toString());
			System.out.println("Created performance statistics with metadata to be uploaded at GD platform (./target/perf_log.csv)");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			it.close();
		}
	}
	
	private String getClientVersion() throws JSONException {
		browser.get(getRootUrl() + "/gdc/releaseInfo");
		JSONObject json = loadJSON();
		JSONArray components = json.getJSONObject("release").getJSONArray("components");
		for (int i = 0; i < components.length(); i++) {
			JSONObject component = components.getJSONObject(i);
			if (component.getJSONObject("component").getString("name").equals("client")) {
				return component.getJSONObject("component").getString("release");
			}
		}
		return "client-version-not-found";
	}
	
}
