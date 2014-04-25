package com.gooddata.qa.graphene.metric;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.AggregationMetricTypes;
import com.gooddata.qa.graphene.enums.FilterMetricTypes;
import com.gooddata.qa.graphene.enums.GranularityMetricTypes;
import com.gooddata.qa.graphene.enums.LogicalMetricTypes;
import com.gooddata.qa.graphene.enums.NumericMetricTypes;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;

@Test(groups = { "GoodSalesMetrics" }, description = "Tests for GoodSales project (metric creation functionality) in GD platform")
public class SimpleMetricTest extends GoodSalesAbstractTest {

    private String metric;
    private String attrFolder;
    private String attr;
    private String attrValue;
    private String fact;
    private String ratioMetric1;
    private String ratioMetric2;
    Map<String, String> data;

    private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

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
	data = new HashMap<String, String>();
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createShareMetric() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Share % " + getCurrentDateString();
	metricEditorPage
		.createShareMetric(metricName, metric, attrFolder, attr);
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
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
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Difference " + getCurrentDateString();
	metricEditorPage.createDifferentMetric(metricName, metric, attrFolder,
		attr, attrValue);
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
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
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Ratio " + getCurrentDateString();
	metricEditorPage.createRatioMetric(metricName, ratioMetric1,
		ratioMetric2);
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	waitForElementVisible(metricsTable.getRoot());
	waitForDataPageLoaded();
	metricsTable.selectObject(metricName);
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
    public void createAggreationMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	data.put("attrFolder0", this.attrFolder);
	data.put("attrFolder1", this.attrFolder);
	data.put("fact0", this.fact);
	data.put("fact1", this.fact);
	data.put("attribute0", this.attr);
	data.put("attribute1", this.attr);
	ArrayList<AggregationMetricTypes> metricType = new ArrayList<AggregationMetricTypes>();
	metricType.add(AggregationMetricTypes.AVG);
	metricType.add(AggregationMetricTypes.COUNT);
	metricType.add(AggregationMetricTypes.CORREL);
	for (AggregationMetricTypes type : metricType) {
	    String metricName = type.getlabel() + " " + getCurrentDateString();
	    metricEditorPage.createAggregationMetric(type, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createNumericMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	data.put("fact0", this.fact);
	ArrayList<NumericMetricTypes> metricType = new ArrayList<NumericMetricTypes>();
	metricType.add(NumericMetricTypes.ABS);
	metricType.add(NumericMetricTypes.EXP);
	metricType.add(NumericMetricTypes.FLOOR);
	for (NumericMetricTypes type : metricType) {
	    String metricName = type.getlabel() + " " + getCurrentDateString();
	    metricEditorPage.createNumericMetric(type, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createGranularityMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	data.put("attrFolder0", this.attrFolder);
	data.put("fact0", this.fact);
	data.put("metric0", this.metric);
	data.put("metric1", this.metric);
	data.put("attribute0", this.attr);
	ArrayList<GranularityMetricTypes> metricType = new ArrayList<GranularityMetricTypes>();
	metricType.add(GranularityMetricTypes.BY_ALL_ATTRIBUTE);
	metricType.add(GranularityMetricTypes.FOR_NEXT);
	metricType.add(GranularityMetricTypes.BY_ALL);
	metricType.add(GranularityMetricTypes.WITHIN);
	for (GranularityMetricTypes type : metricType) {
	    String metricName = type.getlabel() + " " + getCurrentDateString();
	    metricEditorPage.createGranularityMetric(type, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createLogicalMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	data.put("attrFolder0", this.attrFolder);
	data.put("attrFolder1", this.attrFolder);
	data.put("metric0", this.metric);
	data.put("metric1", this.metric);
	data.put("metric2", this.metric);
	data.put("metric3", this.metric);
	data.put("attribute0", this.attr);
	data.put("attribute1", this.attr);
	data.put("attrValue0", this.attrValue);
	data.put("attrValue1", this.attrValue);
	ArrayList<LogicalMetricTypes> metricType = new ArrayList<LogicalMetricTypes>();
	metricType.add(LogicalMetricTypes.AND);
	metricType.add(LogicalMetricTypes.NOT);
	metricType.add(LogicalMetricTypes.CASE);
	metricType.add(LogicalMetricTypes.IF);
	for (LogicalMetricTypes type : metricType) {
	    String metricName = type.getlabel() + " " + getCurrentDateString();
	    metricEditorPage.createLogicalMetric(type, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createFilterMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	data.put("attrFolder0", this.attrFolder);
	data.put("attrFolder1", this.attrFolder);
	data.put("fact0", this.fact);
	data.put("fact1", this.fact);
	data.put("metric0", this.metric);
	data.put("metric1", this.metric);
	data.put("attribute0", this.attr);
	data.put("attribute1", this.attr);
	data.put("attrValue0", this.attrValue);
	data.put("attrValue1", this.attrValue);
	ArrayList<FilterMetricTypes> metricType = new ArrayList<FilterMetricTypes>();
	metricType.add(FilterMetricTypes.EQUAL);
	metricType.add(FilterMetricTypes.NOT_IN);
	metricType.add(FilterMetricTypes.BOTTOM);
	metricType.add(FilterMetricTypes.WITHOUT_PF);
	for (FilterMetricTypes type : metricType) {
	    String metricName = type.getlabel() + " " + getCurrentDateString();
	    metricEditorPage.createFilterMetric(type, metricName, data);
	}
    }

    public String getCurrentDateString() {
	DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
	Date date = new Date();
	return dateFormat.format(date);
    }
}
