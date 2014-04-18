package com.gooddata.qa.graphene.metric;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

@Test(groups = { "GoodSalesMetrics" }, description = "Tests for GoodSales project (reports functionality) in GD platform")
public class SimpleMetricTest extends GoodSalesAbstractTest {

    private String metric;
    private String attrFolder;
    private String attr;
    private String attrValue;
    private String fact;
    private String ratioMetric1;
    private String ratioMetric2;

    @BeforeClass
    public void setProjectTitle() {
	projectTitle = "Simple-metric-test";
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
    public void initialize() throws InterruptedException, JSONException {

	metric = "# of Activities";
	attrFolder = "Date dimension (Snapshot)";
	attr = "Year (Snapshot)";
	attrValue = "2009";
	fact = "Amount";
	ratioMetric1 = "# of Won Opps.";
	ratioMetric2 = "# of Open Opps.";
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createMetric() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String currentDateTime = dateFormat.format(date);
	String metricName = "Share % " + currentDateTime;
	metricEditorPage
		.createShareMetric(metricName, metric, attrFolder, attr);
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectMetric(metricName);
	waitForElementVisible(metricDetailPage.getRoot());
	waitForObjectPageLoaded();
	String maqlValue = metricDetailPage.getMAQL(metricName);
	String expectedMAQL = "SELECT " + metric + " / (SELECT " + metric
		+ " BY " + attr + ", ALL OTHER WITHOUT PF)";
	Assert.assertEquals(maqlValue, expectedMAQL,
		"Metric is not created properly");
	String format = metricDetailPage.getMetricFormat(metricName);
	String expectedFormat = "#,##0.00";
	Assert.assertEquals(format, expectedFormat,
		"Metric format is not set properly");
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createDifferentMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String currentDateTime = dateFormat.format(date);
	String metricName = "Difference " + currentDateTime;
	metricEditorPage.createDifferentMetric(metricName, metric, attrFolder,
		attr, attrValue);
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectMetric(metricName);
	waitForElementVisible(metricDetailPage.getRoot());
	waitForObjectPageLoaded();
	String maqlValue = metricDetailPage.getMAQL(metricName);
	String expectedMAQL = "SELECT " + metric + " - (SELECT " + metric
		+ " BY ALL " + attr + " WHERE " + attr + " IN (" + attrValue
		+ ") WITHOUT PF)";
	Assert.assertEquals(maqlValue, expectedMAQL,
		"Metric is not created properly");
	String format = metricDetailPage.getMetricFormat(metricName);
	String expectedFormat = "#,##0.00";
	Assert.assertEquals(format, expectedFormat,
		"Metric format is not set properly");
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createRatioMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String currentDateTime = dateFormat.format(date);
	String metricName = "Ratio " + currentDateTime;
	metricEditorPage.createRatioMetric(metricName, ratioMetric1,
		ratioMetric2);
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectMetric(metricName);
	waitForElementVisible(metricDetailPage.getRoot());
	waitForObjectPageLoaded();
	String maqlValue = metricDetailPage.getMAQL(metricName);
	String expectedMAQL = "SELECT " + ratioMetric1 + " / " + ratioMetric2;
	Assert.assertEquals(maqlValue, expectedMAQL,
		"Metric is not created properly");
	String format = metricDetailPage.getMetricFormat(metricName);
	String expectedFormat = "#,##0.00";
	Assert.assertEquals(format, expectedFormat,
		"Metric format is not set properly");
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createCustomAVGMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	String currentDateTime = dateFormat.format(date);
	String metricName = "AVG " + currentDateTime;
	metricEditorPage.createCustomAVGMetric(metricName, fact);
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectMetric(metricName);
	waitForElementVisible(metricDetailPage.getRoot());
	waitForObjectPageLoaded();
	String maqlValue = metricDetailPage.getMAQL(metricName);
	String expectedMAQL = "SELECT AVG" + "(" + fact + ")";
	Assert.assertEquals(maqlValue, expectedMAQL,
		"Metric is not created properly");
	String format = metricDetailPage.getMetricFormat(metricName);
	String expectedFormat = "#,##0.00";
	Assert.assertEquals(format, expectedFormat,
		"Metric format is not set properly");
    }
}
