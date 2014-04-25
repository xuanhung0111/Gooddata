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
import com.gooddata.qa.graphene.enums.metrics.AggregationMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.FilterMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.GranularityMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.LogicalMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.NumericMetricTypes;
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
    private Map<String, String> data;

    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

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
        data = new HashMap<String, String>();
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
	    String metricName = type.getLabel() + " " + getCurrentDateString();
	    metricEditorPage.createAggregationMetric(type, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createNumericMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
        data = new HashMap<String, String>();
	data.put("fact0", this.fact);
	ArrayList<NumericMetricTypes> metricTypes = new ArrayList<NumericMetricTypes>();
	metricTypes.add(NumericMetricTypes.ABS);
	metricTypes.add(NumericMetricTypes.EXP);
	metricTypes.add(NumericMetricTypes.FLOOR);
	for (NumericMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " " + getCurrentDateString();
	    metricEditorPage.createNumericMetric(metricType, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createGranularityMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
        data = new HashMap<String, String>();
	data.put("attrFolder0", this.attrFolder);
	data.put("fact0", this.fact);
	data.put("metric0", this.metric);
	data.put("metric1", this.metric);
	data.put("attribute0", this.attr);
	ArrayList<GranularityMetricTypes> metricTypes = new ArrayList<GranularityMetricTypes>();
	metricTypes.add(GranularityMetricTypes.BY_ALL_ATTRIBUTE);
	metricTypes.add(GranularityMetricTypes.FOR_NEXT);
	metricTypes.add(GranularityMetricTypes.BY_ALL);
	metricTypes.add(GranularityMetricTypes.WITHIN);
	for (GranularityMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " " + getCurrentDateString();
	    metricEditorPage.createGranularityMetric(metricType, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createLogicalMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
        data = new HashMap<String, String>();
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
	ArrayList<LogicalMetricTypes> metricTypes = new ArrayList<LogicalMetricTypes>();
	metricTypes.add(LogicalMetricTypes.AND);
	metricTypes.add(LogicalMetricTypes.NOT);
	metricTypes.add(LogicalMetricTypes.CASE);
	metricTypes.add(LogicalMetricTypes.IF);
	for (LogicalMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " " + getCurrentDateString();
	    metricEditorPage.createLogicalMetric(metricType, metricName, data);
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createFilterMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
        data = new HashMap<String, String>();
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
	ArrayList<FilterMetricTypes> metricTypes = new ArrayList<FilterMetricTypes>();
	metricTypes.add(FilterMetricTypes.EQUAL);
	metricTypes.add(FilterMetricTypes.NOT_IN);
	metricTypes.add(FilterMetricTypes.BOTTOM);
	metricTypes.add(FilterMetricTypes.WITHOUT_PF);
	for (FilterMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " " + getCurrentDateString();
	    metricEditorPage.createFilterMetric(metricType, metricName, data);
	}
    }

    public String getCurrentDateString() {
        return DATE_FORMAT.format(new Date());
    }
}
