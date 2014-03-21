package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.FilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.ReportsList;
import com.gooddata.qa.graphene.project.GoodSalesAbstractTest;

@Test(groups = { "GoodSalesReports" }, description = "Tests for GoodSales project (reports functionality) in GD platform")
public class FilterReportTest extends GoodSalesAbstractTest {

	private String reportName;
	private List<String> how;
	private List<String> what;
	private String attributeFilter;
	private String metricFilter;
	private String variableFilter;
	private String rankType;
	private String rankSize;
	Map<String, String> data;
	private ReportsList reportsList;

	@BeforeClass
	public void setProjectTitle() {
		projectTitle = "GoodSales-filter-test";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
	public void initialize() throws InterruptedException, JSONException {

		reportName = "Test Filter";
		what = new ArrayList<String>();
		what.add("Amount");
		how = new ArrayList<String>();
		how.add("Stage Name");
		attributeFilter = "Stage Name";
		metricFilter = "Amount";
		variableFilter = "Status";
		rankType = "Top"; // (valid elements: "Top", "Bottom")
		rankSize = "3"; // (valid elements: "1", "3", "5" , "10")

		reportsList = Graphene.createPageFragment(ReportsList.class,
				browser.findElement(BY_PANEL_ROOT));
		Assert.assertNotNull(reportsList, "Reports page not initialized!");

	}

	@Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
	public void createReportTest() throws InterruptedException {
		System.out.println("CREATING tabular report......");
		initReportsPage();
		reportsPage.startCreateReport();
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
		Assert.assertNotNull(reportPage, "Report page not initialized!");
		reportPage.createReport(this.reportName, ReportTypes.TABLE, this.what,
				this.how);
		Thread.sleep(5000);
		checkRedBar();
		System.out.println("FINISHED creating report ......");

	}

	@Test(dependsOnMethods = { "createReportTest" }, groups = { "tests" })
	public void filterReportTest() throws InterruptedException {

		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|domainPage|");
		waitForReportsPageLoaded();
		reportsList.openReport(this.reportName);
		data = new HashMap<String, String>();
		data.put("attribute", this.attributeFilter);
		data.put("metric", this.metricFilter);
		data.put("type", this.rankType);
		data.put("size", this.rankSize);
		data.put("number", "100000");
		data.put("variable", this.variableFilter);
		reportPage.addFilter(FilterTypes.ATTRIBUTE, data);
		reportPage.addFilter(FilterTypes.RANK, data);
		reportPage.addFilter(FilterTypes.RANGE, data);
		reportPage.addFilter(FilterTypes.PROMPT, data);
		reportPage.saveReport();
		checkRedBar();
		successfulTest = true;

	}

}
