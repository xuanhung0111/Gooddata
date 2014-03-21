package com.gooddata.qa.graphene.project;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

public class BasicScenariosTest extends SimpleProjectTest {
	protected static final String ATTRIBUTE_NAME = "State";
	protected static final String ATTRIBUTE_LABEL_TYPE = "US States (Name)";

	protected static final String ATTRIBUTE_FOR_VARIABLE = "Education";

	protected static final String PROMPT_NAME = "FVariable";
	protected static final String FILTER_ATTRIBUTE_NAME = "County";
	protected static final String REPORT_NAME = "Amount Overview table";
	protected static final String METRIC_NAME = "Avg of Amount";

	private long expectedDashboardExportSize = 65000L;

	@BeforeClass
	public void initStartPage() {
		projectTitle = "Basic-scenario-test";
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
	public void uploadDataTest() throws InterruptedException {
		String csvFilePath = loadProperty("csvFilePath");
		uploadSimpleCSV(csvFilePath + "payroll-Tutorial.csv", "simple-ws");

	}

	@Test(dependsOnMethods = { "uploadDataTest" }, groups = { "tests" })
	public void createvariableTest() throws InterruptedException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
				+ "|dataPage|variables");
		variableDetailPage.createFilterVariable(ATTRIBUTE_FOR_VARIABLE,
				PROMPT_NAME);
	}

	@Test(dependsOnMethods = { "createvariableTest" }, groups = { "tests" })
	public void changeStateLabelTest() throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
		waitForElementVisible(attributesTable.getRoot());
		waitForDataPageLoaded();
		attributesTable.selectAttribute(ATTRIBUTE_NAME);
		waitForElementVisible(attributeDetailPage.getRoot());
		waitForObjectPageLoaded();
		Assert.assertEquals(attributeDetailPage.getAttributeName(),
				ATTRIBUTE_NAME, "Invalid attribute name on detail page");
		attributeDetailPage.selectLabelType(ATTRIBUTE_LABEL_TYPE);
		Thread.sleep(2000);
		Assert.assertEquals(attributeDetailPage.getAttributeLabelType(),
				ATTRIBUTE_LABEL_TYPE, "Label type not set properly");

	}

	// add widgets into Dashboard
	@Test(dependsOnMethods = { "changeStateLabelTest" }, groups = { "tests" })
	public void addDashboardObjectsTest() throws InterruptedException {
		initDashboardsPage();
		DashboardEditBar dashboardEditBar = dashboardsPage
				.getDashboardEditBar();
		String dashboardName = "Test";
		dashboardsPage.addNewDashboard(dashboardName);
		dashboardsPage.editDashboard();
		dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE,
				FILTER_ATTRIBUTE_NAME);
		dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT,
				PROMPT_NAME);
		dashboardEditBar.addTimeFilterToDashboard(0);
		dashboardEditBar.addReportToDashboard(REPORT_NAME);
		Thread.sleep(2000);
		dashboardEditBar.addTextToDashboard(TextObject.HEADLINE, "Headline",
				"google.com");
		dashboardEditBar.addTextToDashboard(TextObject.SUB_HEADLINE,
				"Sub-Headline", "google.com");
		dashboardEditBar.addTextToDashboard(TextObject.DESCRIPTION,
				"Description", "google.com");
		dashboardEditBar.addLineToDashboard();
		dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC,
				METRIC_NAME);
		Thread.sleep(2000);
		dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART,
				METRIC_NAME);
		Thread.sleep(2000);
		dashboardEditBar.addWebContentToDashboard();
		dashboardEditBar.saveDashboard();
		String exporteddashboardName = dashboardsPage.printDashboardTab(0);
		verifyDashboardExport(exporteddashboardName.replace(" ", "_"),
				expectedDashboardExportSize);
		checkRedBar();
		successfulTest = true;
	}

}
