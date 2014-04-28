package com.gooddata.qa.graphene.metric;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
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
    private String ratioMetric1;
    private String ratioMetric2;
    private Map<String, String> data;

    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
	    DATE_FORMAT_PATTERN);

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
	ratioMetric1 = "# of Won Opps.";
	ratioMetric2 = "# of Open Opps.";
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createShareMetric() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Share % " + getCurrentDateString();
	metricEditorPage
		.createShareMetric(metricName, metric, attrFolder, attr);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createDifferentMetricTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Difference " + getCurrentDateString();
	metricEditorPage.createDifferentMetric(metricName, metric, attrFolder,
		attr, attrValue);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createRatioMetricTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metricName = "Ratio " + getCurrentDateString();
	metricEditorPage.createRatioMetric(metricName, ratioMetric1,
		ratioMetric2);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createAggreationMetricTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String fact0 = "Probability";
	String fact1 = "Velocity";
	String attrFolder0 = "Stage";
	String attrFolder1 = "Stage History";
	String attr0 = "Stage Name";
	String attr1 = "Stage History";
	data = new HashMap<String, String>();
	data.put("attrFolder0", attrFolder0);
	data.put("attrFolder1", attrFolder1);
	data.put("fact0", fact0);
	data.put("fact1", fact1);
	data.put("attribute0", attr0);
	data.put("attribute1", attr1);
	ArrayList<AggregationMetricTypes> metricType = new ArrayList<AggregationMetricTypes>();
	metricType.add(AggregationMetricTypes.AVG);
	metricType.add(AggregationMetricTypes.COUNT);
	metricType.add(AggregationMetricTypes.CORREL);
	ArrayList<String> expectedMaql = new ArrayList<String>();
	String expectedFormat = "#,##0.00";
	String expectedMaqlAvg = "SELECT AVG(" + fact0 + ")";
	String expectedMaqlCorrel = "SELECT CORREL(" + fact0 + "," + fact1
		+ ")";
	String expectedMaqlCount = "SELECT COUNT(" + attr0 + "," + attr1 + ")";
	expectedMaql.add(expectedMaqlAvg);
	expectedMaql.add(expectedMaqlCount);
	expectedMaql.add(expectedMaqlCorrel);
	int i = 0;
	for (AggregationMetricTypes type : metricType) {
	    String metricName = type.getLabel() + " " + getCurrentDateString();
	    System.out.println(String.format(
		    "Creating %s metric, name: %s, data: %s", type, metricName,
		    data.toString()));
	    metricEditorPage.createAggregationMetric(type, metricName, data);
	    metricEditorPage.verifyMetric(metricName, expectedMaql.get(i),
		    expectedFormat);
	    i++;
	    openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createNumericMetricTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	String metric = "Best Case";
	data = new HashMap<String, String>();
	data.put("metric0", metric);
	ArrayList<NumericMetricTypes> metricTypes = new ArrayList<NumericMetricTypes>();
	metricTypes.add(NumericMetricTypes.ABS);
	metricTypes.add(NumericMetricTypes.EXP);
	metricTypes.add(NumericMetricTypes.FLOOR);
	String expectedFormat = "#,##0.00";
	String expectedMaql;
	for (NumericMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " "
		    + getCurrentDateString();
	    System.out.println(String.format(
		    "Creating %s metric, name: %s, data: %s", metricType,
		    metricName, data.toString()));
	    metricEditorPage.createNumericMetric(metricType, metricName, data);
	    expectedMaql = "SELECT " + metricType + "(" + metric + ")";
	    metricEditorPage.verifyMetric(metricName, expectedMaql,
		    expectedFormat);
	    openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");

	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createGranularityMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	String fact0 = "Probability";
	String attrFolder0 = "Stage";
	String attr0 = "Status";
	String metric0 = "# of Won Opps.";
	data = new HashMap<String, String>();
	data.put("attrFolder0", attrFolder0);
	data.put("fact0", fact0);
	data.put("metric0", metric0);
	data.put("metric1", metric0);
	data.put("attribute0", attr0);
	ArrayList<GranularityMetricTypes> metricTypes = new ArrayList<GranularityMetricTypes>();
	metricTypes.add(GranularityMetricTypes.BY_ALL_ATTRIBUTE);
	metricTypes.add(GranularityMetricTypes.FOR_NEXT);
	metricTypes.add(GranularityMetricTypes.BY_ALL);
	metricTypes.add(GranularityMetricTypes.WITHIN);
	ArrayList<String> expectedMaql = new ArrayList<String>();
	String expectedFormat = "#,##0.00";
	String expectedMaqlByAllAttr = "SELECT " + metric0 + " / (SELECT "
		+ metric0 + " BY ALL " + attr0 + ")";
	String expectedMaqlForNext = "SELECT " + metric0 + " FOR Next(" + attr0
		+ ")";
	String expectedMaqlByAll = "SELECT " + metric0 + " / (SELECT "
		+ metric0 + " BY ALL IN ALL OTHER DIMENSIONS)";
	String expectedMaqlWithin = "SELECT RANK(" + metric0 + ") WITHIN ("
		+ fact0 + ")";
	expectedMaql.add(expectedMaqlByAllAttr);
	expectedMaql.add(expectedMaqlForNext);
	expectedMaql.add(expectedMaqlByAll);
	expectedMaql.add(expectedMaqlWithin);
	int i = 0;
	for (GranularityMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " "
		    + getCurrentDateString();
	    System.out.println(String.format(
		    "Creating %s metric, name: %s, data: %s", metricType,
		    metricName, data.toString()));
	    metricEditorPage.createGranularityMetric(metricType, metricName,
		    data);
	    metricEditorPage.verifyMetric(metricName, expectedMaql.get(i),
		    expectedFormat);
	    i++;
	    openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createLogicalMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	String attrFolder0 = "Date dimension (Snapshot)";
	String attr0 = "Year (Snapshot)";
	String attrValue0 = "2009";
	String attr1 = "Month/Year (Snapshot)";
	String attrValue1 = "May 2009";
	String metric0 = "# of Open Opps.";
	String metric1 = "# of Won Opps.";
	data = new HashMap<String, String>();
	data.put("attrFolder0", attrFolder0);
	data.put("attrFolder1", attrFolder0);
	data.put("metric0", metric0);
	data.put("metric1", metric1);
	data.put("metric2", metric0);
	data.put("metric3", metric1);
	data.put("attribute0", attr0);
	data.put("attribute1", attr1);
	data.put("attrValue0", attrValue0);
	data.put("attrValue1", attrValue1);
	ArrayList<LogicalMetricTypes> metricTypes = new ArrayList<LogicalMetricTypes>();
	metricTypes.add(LogicalMetricTypes.AND);
	metricTypes.add(LogicalMetricTypes.NOT);
	metricTypes.add(LogicalMetricTypes.CASE);
	metricTypes.add(LogicalMetricTypes.IF);
	ArrayList<String> expectedMaql = new ArrayList<String>();
	String expectedFormat = "#,##0.00";
	String expectedMaqlAnd = "SELECT " + metric0 + " WHERE " + attr0
		+ " = " + attrValue0 + " AND " + attr1 + " = " + attrValue1;
	String expectedMaqlNot = "SELECT " + metric0 + " WHERE NOT (" + attr0
		+ " = " + attrValue0 + ")";
	String expectedMaqlCase = "SELECT CASE WHEN " + metric0 + " > "
		+ metric1 + " THEN 1, WHEN " + metric0 + " < " + metric1
		+ " THEN 2 ELSE 3 END";
	String expectedMaqlIf = "SELECT IF " + metric0 + " > 0.5 THEN "
		+ metric1 + " * 10 ELSE " + metric0 + " / 10 END";
	expectedMaql.add(expectedMaqlAnd);
	expectedMaql.add(expectedMaqlNot);
	expectedMaql.add(expectedMaqlCase);
	expectedMaql.add(expectedMaqlIf);
	int i = 0;
	for (LogicalMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " "
		    + getCurrentDateString();
	    System.out.println(String.format(
		    "Creating %s metric, name: %s, data: %s", metricType,
		    metricName, data.toString()));
	    metricEditorPage.createLogicalMetric(metricType, metricName, data);
	    metricEditorPage.verifyMetric(metricName, expectedMaql.get(i),
		    expectedFormat);
	    i++;
	    openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	}
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "tests" })
    public void createFilterMetricTest() throws InterruptedException {
	browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId
		+ "|dataPage|metrics");
	String attrFolder = "Date dimension (Snapshot)";
	String attr = "Year (Snapshot)";
	String attrValue0 = "2009";
	String attrValue1 = "2011";
	String metric0 = "# of Open Opps.";
	String fact = "Amount";
	data = new HashMap<String, String>();
	data.put("attrFolder0", attrFolder);
	data.put("attrFolder1", attrFolder);
	data.put("fact0", fact);
	data.put("fact1", fact);
	data.put("metric0", metric0);
	data.put("metric1", metric0);
	data.put("attribute0", attr);
	data.put("attribute1", attr);
	data.put("attrValue0", attrValue0);
	data.put("attrValue1", attrValue1);
	ArrayList<FilterMetricTypes> metricTypes = new ArrayList<FilterMetricTypes>();
	metricTypes.add(FilterMetricTypes.EQUAL);
	metricTypes.add(FilterMetricTypes.NOT_IN);
	metricTypes.add(FilterMetricTypes.BOTTOM);
	metricTypes.add(FilterMetricTypes.WITHOUT_PF);
	ArrayList<String> expectedMaql = new ArrayList<String>();
	String expectedFormat = "#,##0.00";
	String expectedMaqlEqual = "SELECT " + metric0 + " WHERE " + attr
		+ " = " + attrValue0;
	String expectedMaqlNotIn = "SELECT " + metric0 + " WHERE " + attr
		+ " NOT IN (" + attrValue0 + ", " + attrValue1 + ")";
	String expectedMaqlBottom = "SELECT " + fact
		+ " WHERE BOTTOM(25%) IN (SELECT " + fact + " BY " + attr + ")";
	String expectedMaqlWithoutPf = "SELECT " + metric0 + " - (SELECT "
		+ metric0 + " BY ALL " + attr + " WITHOUT PARENT FILTER)";
	expectedMaql.add(expectedMaqlEqual);
	expectedMaql.add(expectedMaqlNotIn);
	expectedMaql.add(expectedMaqlBottom);
	expectedMaql.add(expectedMaqlWithoutPf);
	int i = 0;
	for (FilterMetricTypes metricType : metricTypes) {
	    String metricName = metricType.getLabel() + " "
		    + getCurrentDateString();
	    System.out.println(String.format(
		    "Creating %s metric, name: %s, data: %s", metricType,
		    metricName, data.toString()));
	    metricEditorPage.createFilterMetric(metricType, metricName, data);
	    metricEditorPage.verifyMetric(metricName, expectedMaql.get(i),
		    expectedFormat);
	    i++;
	    openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|metrics");
	}
    }

    public String getCurrentDateString() {
	return DATE_FORMAT.format(new Date());
    }

}
