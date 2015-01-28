package com.gooddata.qa.graphene.manage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = {"GoodSalesMetrics"}, description = "Tests for GoodSales project (metric creation functionality) in GD platform")
public class GoodSalesMetricTest extends GoodSalesAbstractTest {
    private String attrFolder;
    private String attr;
    private String attrValue;
    private String ratioMetric1;
    private String ratioMetric2;
    private String productAttr;
    private String stageNameAttr;
    private String quarterYearAttr;
    private String quarterYearCreatedAttr;
    private String statusAttr;
    private String expectedMetricFormat;
    private Map<String, String> data;
    List<String> yearSnapshotValues;
    List<String> productValues;
    List<String> stageNameValues;
    List<String> quarterYearValues;
    List<String> quarterYearCreatedValues;
    List<String> statusValues;

    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-metric";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() throws InterruptedException, JSONException {
        expectedMetricFormat = "#,##0.00";
        attrFolder = "Date dimension (Snapshot)";
        attr = "Year (Snapshot)";
        attrValue = "2010";
        ratioMetric1 = "# of Won Opps.";
        ratioMetric2 = "# of Open Opps.";
        productAttr = "Product";
        stageNameAttr = "Stage Name";
        quarterYearAttr = "Quarter/Year (Snapshot)";
        quarterYearCreatedAttr = "Quarter/Year (Created)";
        statusAttr = "Status";
        quarterYearValues = Arrays.asList("Q2/2010", "Q3/2010", "Q4/2010",
                "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012",
                "Q2/2012");
        yearSnapshotValues = Arrays.asList("2010", "2011", "2012");
        productValues = Arrays.asList("CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid");
        stageNameValues = Arrays.asList("Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "Negotiation", "Closed Won",
                "Closed Lost");
        quarterYearCreatedValues = Arrays.asList("Q1/2008", "Q2/2008",
                "Q3/2008", "Q4/2008", "Q1/2009", "Q2/2009", "Q3/2009",
                "Q4/2009", "Q1/2010", "Q2/2010", "Q3/2010", "Q4/2010",
                "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012");
        statusValues = Arrays.asList("Lost", "Open", "Won");
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createShareMetric() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metricName = "Share % " + getCurrentDateString();
        String metric = "Amount";
        metricEditorPage.createShareMetric(metricName, metric, attrFolder, attr);
        List<Float> metricValues = Arrays.asList(0.23f, 0.2f, 0.33f, 0.07f,
                0.08f, 0.09f);
        checkMetricValuesInReport(metricName, productAttr, metricValues,
                productValues);
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createDifferentMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metric = "Amount";
        String metricName = "Difference " + getCurrentDateString();
        metricEditorPage.createDifferentMetric(metricName, metric, attrFolder, attr, attrValue);
        List<Float> metricValues = Arrays.asList(20171686.25f, 16271278.69f,
                32627524.67f, 6273858.91f, 7532925.39f, 6825820.63f);
        checkMetricValuesInReport(metricName, productAttr, metricValues,
                productValues);
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createRatioMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metricName = "Ratio " + getCurrentDateString();
        metricEditorPage.createRatioMetric(metricName, ratioMetric1, ratioMetric2);
        List<Float> metricValues = Arrays.asList(3.61f);
        checkMetricValuesInReport(metricName, null, metricValues, null);
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createAggregationMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String fact0 = "Amount";
        String fact1 = "Probability";
        String fact2 = "Velocity";
        String fact3 = "Duration";
        String fact4 = "Days to Close";
        String attrFolder0 = "Stage";
        String attrFolder1 = "Stage History";
        String attr0 = "Stage Name";
        String attr1 = "Stage History";
        data = new HashMap<String, String>();
        data.put("attrFolder0", attrFolder0);
        data.put("attrFolder1", attrFolder1);
        data.put("attribute0", attr0);
        data.put("attribute1", attr1);
        for (MetricList metric : MetricList.values()) {
            if (metric.getType().equalsIgnoreCase("Aggregation")) {
                String metricName = metric + " " + getCurrentDateString();
                switch (metric) {
                case COVAR:
                case COVARP:
                case RSQ:
                    data.put("fact0", fact2);
                    data.put("fact1", fact3);
                    break;
                case MAX:
                case RUNMAX:
                    data.put("fact0", fact4);
                    break;
                case MIN:
                case RUNMIN:
                    data.put("fact0", fact3);
                    break;
                default:
                    data.put("fact0", fact0);
                    data.put("fact1", fact1);
                    break;
                }
                System.out.println(String.format(
                        "Creating %s metric, name: %s, data: %s", metric, metricName, data.toString()));
                metricEditorPage.createAggregationMetric(metric.getMetric(), metricName, data);
                String expectedMaql = metric.getMaql()
                        .replace("${fact0}", data.get("fact0"))
                        .replace("${fact1}", data.get("fact1"))
                        .replace("${attr0}", data.get("attribute0"))
                        .replace("${attr1}", data.get("attribute1"));
                metricEditorPage.verifyMetric(metricName, expectedMaql, expectedMetricFormat);
                switch (metric) {
                case AVG:
                case COUNT:
                case MEDIAN:
                    checkMetricValuesInReport(metricName, stageNameAttr,
                            metric.getMetricValues(), stageNameValues);
                    break;
                case CORREL:
                    checkMetricValuesInReport(metricName, null,
                            metric.getMetricValues(), null);
                    break;
                case COVAR:
                case COVARP:
                case PERCENTILE:
                case RSQ:
                    checkMetricValuesInReport(metricName, productAttr,
                            metric.getMetricValues(), productValues);
                    break;
                case MIN:
                case RUNMIN:
                    checkMetricValuesInReport(metricName,
                            quarterYearCreatedAttr, metric.getMetricValues(),
                            quarterYearCreatedValues);
                    break;
                default:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            metric.getMetricValues(), quarterYearValues);
                    break;
                }
                openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
            }
        }
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createNumericMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metric0 = "Best Case";
        String metric1 = "Amount";
        String metric2 = "Win Rate";
        String metric3 = "Remaining Quota";
        data = new HashMap<String, String>();
        data.put("metric1", metric1);
        for (MetricList metric: MetricList.values() ) {
            if (metric.getType().equalsIgnoreCase("Numeric")) {
                String metricName = metric + " " + getCurrentDateString();
                switch (metric) {
                case EXP:
                    data.put("metric0", metric2);
                    break;
                case SIGN:
                    data.put("metric0", metric3);
                    break;
                default:
                    data.put("metric0", metric0);
                    break;
                }
                System.out.println(String.format(
                        "Creating %s metric, name: %s, data: %s", metric, metricName, data.toString()));
                metricEditorPage.createNumericMetric(metric.getMetric(), metricName, data);
                String expectedMaql = metric.getMaql()
                        .replace("${metric}", data.get("metric0"))
                        .replace("${metric1}", data.get("metric1"));
                metricEditorPage.verifyMetric(metricName, expectedMaql, expectedMetricFormat);
                switch (metric) {
                case IFNULL:
                    checkMetricValuesInReport(metricName, statusAttr, metric.getMetricValues(), statusValues);
                    break;
                case SIGN:
                    checkMetricValuesInReport(metricName, quarterYearAttr, metric.getMetricValues(), quarterYearValues);
                    break;
                default:
                    checkMetricValuesInReport(metricName, productAttr, metric.getMetricValues(), productValues);
                    break;
                }
                openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
            }
        }
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createGranularityMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String fact0 = "Probability";
        String attrFolder0 = "Date dimension (Snapshot)";
        String attrFolder1 = "Date dimension (Closed)";
        String attr0 = "Year (Snapshot)";
        String attr1 = "Year (Closed)";
        String metric0 = "Amount";
        String metric1 = "# of Won Opps.";
        String metric2 = "# of Open Opps.";
        data = new HashMap<String, String>();
        List<String> forPreviousAttrValues = Arrays.asList("Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q2/2014", "Q3/2014", "Q4/2014", "Q1/2015", "Q2/2015", "Q4/2015");
        List<String> forNextPeriodAttrValues = Arrays.asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q3/2014");
        List<String> forPreviousPeriodAttrValues = Arrays.asList("Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q2/2014", "Q3/2014", "Q1/2015");
        for (MetricList metric: MetricList.values() ) {
            if (metric.getType().equalsIgnoreCase("Granularity")) {
                String metricName = metric + " " + getCurrentDateString();
                switch (metric) {
                case FOR_PREVIOUS:
                case FOR_NEXT_PERIOD:
                case FOR_PREVIOUS_PERIOD:
                    data.put("attrFolder0", attrFolder1);
                    data.put("metric0", metric2);
                    data.put("attribute0", attr1);
                    break;
                default:
                    data.put("attrFolder0", attrFolder0);
                    data.put("fact0", fact0);
                    data.put("metric0", metric0);
                    data.put("metric1", metric1);
                    data.put("attribute0", attr0);
                    break;
                }
                System.out.println(String.format("Creating %s metric, name: %s, data: %s", metric, metricName, data.toString()));
                metricEditorPage.createGranularityMetric(metric.getMetric(), metricName, data);
                String expectedMaql = metric.getMaql().replace("${metric0}", data.get("metric0")).replace("${metric1}", data.get("metric1")).replace("${attr0}", data.get("attribute0"));
                metricEditorPage.verifyMetric(metricName, expectedMaql , expectedMetricFormat);
                switch (metric) {
                case BY:
                case BY_ALL_EXCEPT:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            metric.getMetricValues(), quarterYearValues);
                    break;
                case FOR_PREVIOUS:
                    checkMetricValuesInReport(metricName, "Quarter/Year (Closed)",
                            metric.getMetricValues(), forPreviousAttrValues);
                    break;
                case FOR_NEXT_PERIOD:
                    checkMetricValuesInReport(metricName, "Quarter/Year (Closed)",
                            metric.getMetricValues(), forNextPeriodAttrValues);
                    break;
                case FOR_PREVIOUS_PERIOD:
                    checkMetricValuesInReport(metricName, "Quarter/Year (Closed)",
                            metric.getMetricValues(), forPreviousPeriodAttrValues);
                    break;
                default:
                    checkMetricValuesInReport(metricName, productAttr,
                            metric.getMetricValues(), productValues);
                    break;
                }
                openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
            }
        }
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createLogicalMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String attrFolder0 = "Date dimension (Snapshot)";
        String attr0 = "Year (Snapshot)";
        String attrValue0 = "2011";
        String attr1 = "Month/Year (Snapshot)";
        String attrValue1 = "May 2011";
        String metric0 = "# of Open Opps.";
        String metric2 = "Lost";
        String metric3 = "Won";
        String metric4 = "Probability";
        String metric5 = "Amount";
        data = new HashMap<String, String>();
        data.put("attrFolder0", attrFolder0);
        data.put("attrFolder1", attrFolder0);
        data.put("attribute0", attr0);
        data.put("attribute1", attr1);
        data.put("attrValue0", attrValue0);
        data.put("attrValue1", attrValue1);
        data.put("metric3", metric3);
        for (MetricList metric: MetricList.values() ) {
            if (metric.getType().equalsIgnoreCase("Logical")) {
                String metricName = metric + " " + getCurrentDateString();
                if (metric.equals(MetricList.CASE)) {
                    data.put("metric0", metric2);
                    data.put("metric1", metric3);
                    data.put("metric2", metric2);
                } else if (metric.equals(MetricList.IF)) {
                    data.put("metric0", metric4);
                    data.put("metric1", metric5);
                    data.put("metric2", metric5);
                } else {
                    data.put("metric0", metric0);
                    data.put("metric1", metric3);
                    data.put("metric2", metric2);
                }
                System.out.println(String.format(
                        "Creating %s metric, name: %s, data: %s", metric,
                        metricName, data.toString()));
                metricEditorPage.createLogicalMetric(metric.getMetric(), metricName, data);
                String expectedMaql = metric.getMaql()
                        .replace("${metric0}", data.get("metric0"))
                        .replace("${metric1}", data.get("metric1"))
                        .replace("${metric2}", data.get("metric2"))
                        .replace("${metric3}", data.get("metric3"))
                        .replace("${attr0}", data.get("attribute0"))
                        .replace("${attrValue0}", data.get("attrValue0"))
                        .replace("${attr1}", data.get("attribute1"))
                        .replace("${attrValue1}", data.get("attrValue1"));
                System.out.println("expectedMaql" + expectedMaql);
                metricEditorPage.verifyMetric(metricName, expectedMaql, expectedMetricFormat);
                switch (metric) {
                case CASE:
                    List<String> attrValues = Arrays.asList("CompuSci",
                            "Educationly", "Explorer", "Grammar Plus",
                            "PhoenixSoft", "TouchAll", "WonderKid");
                    checkMetricValuesInReport(metricName, productAttr,
                            metric.getMetricValues(), attrValues);
                    break;
                case IF:
                    checkMetricValuesInReport(metricName, stageNameAttr,
                            metric.getMetricValues(), stageNameValues);
                    break;
                default:
                    checkMetricValuesInReport(metricName, productAttr,
                            metric.getMetricValues(), productValues);
                    break;
                }
                openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
            }
        }
    }

    @Test(dependsOnMethods = {"initialize"})
    public void createFilterMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String attrFolder = "Date dimension (Snapshot)";
        String attr = "Year (Snapshot)";
        String attrValue0 = "2010";
        String attrValue1 = "2011";
        String attrValue2 = "2012";
        String metric0 = "# of Open Opps.";
        String metric1 = "Amount";
        data = new HashMap<String, String>();
        data.put("attrFolder0", attrFolder);
        data.put("attrFolder1", attrFolder);
        data.put("metric0", metric0);
        data.put("metric1", metric1);
        data.put("attribute0", attr);
        data.put("attribute1", attr);
        List<String> quartersIn2010 = Arrays.asList("Q2/2010", "Q3/2010", "Q4/2010");
        List<String> quartersIn2012 = Arrays.asList("Q1/2012", "Q2/2012");
        List<String> withoutPfAttributeValues = Arrays.asList("Q2/2012");
        List<String> quartersIn2011and2012 = Arrays.asList("Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012");
        List<String> quartersIn2010and2011 = Arrays.asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011");
        for (MetricList metric : MetricList.values()) {
            if (metric.getType().equalsIgnoreCase("Filter")) {
                String metricName = metric + " " + getCurrentDateString();
                switch (metric) {
                case LESS:
                case LESS_OR_EQUAL:
                    data.put("attrValue0", attrValue1);
                    break;
                case BETWEEN:
                    data.put("attrValue0", attrValue0);
                    data.put("attrValue1", attrValue2);
                    break;
                default:
                    data.put("attrValue0", attrValue0);
                    data.put("attrValue1", attrValue1);
                    break;
                }
                System.out.println(String.format(
                        "Creating %s metric, name: %s, data: %s", metric, metricName, data.toString()));
                metricEditorPage.createFilterMetric(metric.getMetric(), metricName, data);
                String expectedMaql = metric.maql.replace("${metric0}", data.get("metric0"))
                        .replace("${metric1}", data.get("metric1"))
                        .replace("${attr}", data.get("attribute0"))
                        .replace("${attrValue0}", data.get("attrValue0"))
                        .replace("${attrValue1}", data.get("attrValue1"));
                metricEditorPage.verifyMetric(metricName, expectedMaql, expectedMetricFormat);
                switch (metric) {
                case EQUAL:
                case LESS:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.EQUAL.getMetricValues(), quartersIn2010);
                    break;
                case DOES_NOT_EQUAL:
                case GREATER:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.GREATER.getMetricValues(), quartersIn2011and2012);
                    break;
                case GREATER_OR_EQUAL:
                case BETWEEN:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.BETWEEN.getMetricValues(), quarterYearValues);
                    break;
                case LESS_OR_EQUAL:
                case IN:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.IN.getMetricValues(), quartersIn2010and2011);
                    break;
                case WITHOUT_PF:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.WITHOUT_PF.getMetricValues(), withoutPfAttributeValues);
                    break;
                default:
                    checkMetricValuesInReport(metricName, quarterYearAttr,
                            MetricList.NOT_IN.getMetricValues(), quartersIn2012);
                    break;
                }
                openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
            }
        }
    }

    private void checkMetricValuesInReport(String metricName,
            String attributeName, List<Float> metricValues,
            List<String> attributeValues) throws InterruptedException {
        System.out.println("Verifying metric values of " + metricName +" in report");

        ReportDefinition reportDefinition = new ReportDefinition();
        if (attributeName != null) {
            reportDefinition.withHows(attributeName);
        }
        reportDefinition.withWhats(metricName);
        if (metricName.contains("IFNULL")) {
            reportDefinition.withWhats("Amount");
        }
        reportDefinition.withName("report_" + metricName);

        createReport(reportDefinition, "screenshot");

        if (metricName.contains("WITHOUT_PF")) {
            reportPage.addFilter(FilterItem.Factory.createListValuesFilter("Month/Year (Snapshot)", "Apr 2012"));
            reportPage.saveReport();
        }
        List<Float> metricValuesinGrid = reportPage.getTableReport()
                .getMetricElements();
        Screenshots.takeScreenshot(browser, "check-metric" + "-" + metricName,
                this.getClass());
        Assert.assertEquals(metricValuesinGrid, metricValues,
                "Metric values list is incorrrect");
        System.out.println(metricValuesinGrid + "--" + metricValues);
        if (attributeValues != null) {
            List<String> attributeValuesinGrid = reportPage.getTableReport()
                    .getAttributeElements();
            Assert.assertEquals(attributeValuesinGrid, attributeValues);
        }
        reportPage.saveReport();
    }

    public String getCurrentDateString() {
        return DATE_FORMAT.format(new Date()).replaceAll("\\W", "-");
    }

    public enum MetricList {
        // Numeric
        ABS("ABS", "SELECT ABS(${metric})", "5319697.32f, 4363972.93f, 20255220.38f, 1670164.74f, 1198603.33f, 3036473.23f", "Numeric"),
        EXP("EXP", "SELECT EXP(${metric})", "1.76f, 1.74f, 1.7f, 1.77f, 1.79f, 1.69f", "Numeric"),
        IFNULL("IFNULL", "SELECT IFNULL(${metric},0)", "0f, 35844131.93f, 0f, 42470571.16f, 35844131.93f, 38310753.45f", "Numeric"),
        LOG("LOG", "SELECT LOG(${metric})", "6.73f, 6.64f, 7.31f, 6.22f, 6.08f, 6.48f", "Numeric"),
        LN("LN", "SELECT LN(${metric})", "15.49f, 15.29f, 16.82f, 14.33f, 14f, 14.93f", "Numeric"),
        POWER("POWER", "SELECT POWER(${metric},1)", "5319697.32f, 4363972.93f, 20255220.38f, 1670164.74f, 1198603.33f, 3036473.23f", "Numeric"),
        RANK("RANK", "SELECT RANK(${metric})", "5f, 4f, 6f, 2f, 1f, 3f", "Numeric"),
        ROUND("ROUND", "SELECT ROUND(${metric})", "5319697f, 4363973f, 20255220f, 1670165f, 1198603f, 3036473f", "Numeric"),
        FLOOR("FLOOR", "SELECT FLOOR(${metric})", "5319697f, 4363972f, 20255220f, 1670164f, 1198603f, 3036473f", "Numeric"),
        CEILING("CEILING", "SELECT CEILING(${metric})", "5319698f, 4363973f, 20255221f, 1670165f, 1198604f, 3036474f", "Numeric"),
        TRUNC("TRUNC", "SELECT TRUNC(${metric})", "5319697f, 4363972f, 20255220f, 1670164f, 1198603f, 3036473f", "Numeric"),
        SIGN("SIGN", "SELECT SIGN(${metric})", "1f, -1f, -1f, -1f, -1f, -1f, -1f, -1f, -1f", "Numeric"),
        SQRT("SQRT", "SELECT SQRT(${metric})", "2306.45f, 2089.01f, 4500.58f, 1292.35f, 1094.81f, 1742.55f", "Numeric"),
        SUBTRACTION("+, -, *, /", "SELECT ${metric} - ${metric1}", "-21903202.32f, -18582922.54f, -18340974.48f, -6371867.18f, -8327254.58f, -7255103.51f", "Numeric"),
        // Aggregation
        AVG("AVG", "SELECT AVG(${fact0})", "91950.44f, 24518.32f, 30384.53f, 59651.14f, 37137.94f, 28180f, 11747.9f, 24006.22f", "Aggregation"),
        RUNAVG("RUNAVG", "SELECT RUNAVG(${fact0})", "28781.53f, 22976.64f, 20236.14f, 18799.05f, 18254.70f, 18797.26f, 19476.70f, 19760.85f, 19923.45f", "Aggregation"),
        MAX("MAX", "SELECT MAX(${fact0})", "679f, 710f, 1460f, 1446f, 1355f, 1264f, 1173f, 1844f, 1795f", "Aggregation"),
        RUNMAX("RUNMAX", "SELECT RUNMAX(${fact0})", "679f, 710f, 1460f, 1460f, 1460f, 1460f, 1460f, 1844f, 1844f", "Aggregation"),
        MIN("MIN", "SELECT MIN(${fact0})", "0f, 9f, 2f, 6f, 7f, 5f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 3f", "Aggregation"),
        RUNMIN("RUNMIN", "SELECT RUNMIN(${fact0})", "0f, 9f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f", "Aggregation"),
        SUM("SUM", "SELECT SUM(${fact0})", "26680476.45f, 173032507.14f, 286622094.62f, 403085507.4f, 549068386.54f, 784155771.33f, 1026730772.12f, 1245801949.39f, 1122736243.73f", "Aggregation"),
        RUNSUM("RUNSUM", "SELECT RUNSUM(${fact0})", "26680476.45f, 199712983.59f, 486335078.21f, 889420585.61f, 1438488972.15f, 2222644743.48f, 3249375515.6f, 4495177464.99f, 5617913708.72f", "Aggregation"),
        MEDIAN("MEDIAN", "SELECT MEDIAN(${fact0})", "5240.16f, 6362.40f, 12000f, 12000f, 16998.12f, 13187.33f, 2980.46f, 4800f", "Aggregation"),
        CORREL("CORREL", "SELECT CORREL(${fact0},${fact1})", "-0.05f", "Aggregation"),
        COUNT("COUNT", "SELECT COUNT(${attr0},${attr1})", "1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f", "Aggregation"),
        COVAR("COVAR", "SELECT COVAR(${fact0},${fact1})", "2.26f, 8.17f, 38.61f, 59.28f, 14.13f, 52.22f", "Aggregation"),
        COVARP("COVARP", "SELECT COVARP(${fact0},${fact1})", "2.26f, 8.15f, 38.5f, 58.74f, 14.02f, 51.8f", "Aggregation"),
        PERCENTILE("PERCENTILE", "SELECT PERCENTILE(${fact0},0.25)", "832.80f, 769.52f, 940.80f, 1348.12f, 1036.80f, 900f", "Aggregation"),
        RSQ("RSQ", "SELECT RSQ(${fact0},${fact1})", "0f, 0f, 0.02f, 0.05f, 0f, 0.03f", "Aggregation"),
        STDEV("STDEV", "SELECT STDEV(${fact0})", "74188.03f, 66021.22f, 52967.24f, 48908.74f, 47954.34f, 169291.7f, 220273.73f, 204454.97f, 192490.02f", "Aggregation"),
        RUNSTDEV("RUNSTDEV", "SELECT RUNSTDEV(${fact0})", "74188.03f, 66965.11f, 58453.34f, 53987.64f, 51665.31f, 106485.98f, 148893.7f, 165539.55f, 171079.39f", "Aggregation"),
        VAR("VAR", "SELECT VAR(${fact0})", "5503864027f, 4358800996.88f, 2805528940.9f, 2392065115.96f, 2299618561.68f, 28659680430.94f, 48520516045.51f, 41801835719.68f, 37052408241.94f", "Aggregation"),
        RUNVAR("RUNVAR", "SELECT RUNVAR(${fact0})", "5503864027f, 4484325735.48f, 3416792441.66f, 2914665138.04f, 2669304494.43f, 11339263468.53f, 22169333880.03f, 27403342437.44f, 29268158189.74f", "Aggregation"),
        // Granularity
        BY("BY", "SELECT ${metric0} / (SELECT ${metric1} BY ${attr0})", "10780.66f, 23031.85f, 34560.16f, 14490.03f, 19202.08f, 29123.39f, 34651.63f, 31400.59f, 35223.64f", "Granularity"),
        BY_ALL_ATTRIBUTE("BY ALL attributes", "SELECT ${metric0} / (SELECT ${metric1} BY ALL ${attr0})", "31728.32f, 29046.7f, 49673.35f, 27827.10f, 29310.33f, 37836.68f", "Granularity"),
        BY_ALL("BY ALL IN ALL OTHER DIMENSIONS", "SELECT ${metric0} / (SELECT ${metric1} BY ALL IN ALL OTHER DIMENSIONS)", "8221.96f, 6930.5f, 11656.96f, 2428.88f, 2877.03f, 3108.3f", "Granularity"),
        BY_ATTR_ALL_OTHER("BY Attr, ALL OTHER", "SELECT ${metric0} / (SELECT ${metric1} BY ${attr0}, ALL IN ALL OTHER DIMENSIONS)", "8221.96f, 6930.5f, 11656.96f, 2428.88f, 2877.03f, 3108.3f", "Granularity"),
        FOR_NEXT("FOR Next", "SELECT ${metric0} FOR Next(${attr0})", "27222899.64f, 22946895.47f, 38596194.86f, 8042031.92f, 9525857.91f, 10291576.74f", "Granularity"),
        FOR_PREVIOUS("FOR Previous", "SELECT ${metric0} FOR Previous(${attr0})", "13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f", "Granularity"),
        FOR_NEXT_PERIOD("FOR NextPeriod", "SELECT ${metric0} FOR NextPeriod(${attr0})", "13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f", "Granularity"),
        FOR_PREVIOUS_PERIOD("FOR PreviousPeriod", "SELECT ${metric0} FOR PreviousPeriod(${attr0})", "13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f", "Granularity"),
        BY_ALL_EXCEPT("BY ALL IN ALL OTHER DIMENSIONS EXCEPT (FOR)", "SELECT ${metric0} BY ALL IN ALL OTHER DIMENSIONS EXCEPT ${attr0}", "8398134.81f, 17941808.52f, 26922362f, 36036707.95f, 47755578.7f, 72429870.62f, 86178611.71f, 103967358.32f, 116625456.54f", "Granularity"),
        // Logical
        AND("AND", "SELECT ${metric0} WHERE ${attr0} = ${attrValue0} AND ${attr1} = ${attrValue1}", "74f, 78f, 69f, 31f, 28f, 34f", "Logical"),
        OR("OR", "SELECT ${metric0} WHERE ${attr0} = ${attrValue0} OR ${attr1} = ${attrValue1}", "119f, 137f, 128f, 39f, 48f, 63f", "Logical"),
        CASE("CASE", "SELECT CASE WHEN ${metric0} > ${metric1} THEN 1, WHEN ${metric2} < ${metric3} THEN 2 ELSE 3 END", "1f, 1f, 2f, 2f, 1f, 3f, 1f", "Logical"),
        IF("IF", "SELECT IF ${metric0} > 0.5 THEN ${metric1} * 10 ELSE ${metric2} / 10 END", "1844726.61f, 424902.79f, 561206.26f, 260629.35f, 30674661.20f, 18620157.30f, 383107534.50f, 4247057.12f", "Logical"),
        NOT("NOT", "SELECT ${metric0} WHERE NOT (${attr0} = ${attrValue0})", "246f, 224f, 214f, 70f, 72f, 92f", "Logical"),
        // Filter   
        EQUAL("= (equals)", "SELECT ${metric0} WHERE ${attr} = ${attrValue0}", "149f, 210f, 232f", "Filter"),
        DOES_NOT_EQUAL("<> (does not equal)", "SELECT ${metric0} WHERE ${attr} <> ${attrValue0}", "272f, 338f, 437f, 534f, 730f, 918f", "Filter"),
        GREATER("> (greater)", "SELECT ${metric0} WHERE ${attr} > ${attrValue0}", "272f, 338f, 437f, 534f, 730f, 918f", "Filter"),
        LESS("< (less)", "SELECT ${metric0} WHERE ${attr} < ${attrValue0}", "149f, 210f, 232f", "Filter"),
        GREATER_OR_EQUAL(">= (greater or equal)", "SELECT ${metric0} WHERE ${attr} >= ${attrValue0}", "149f, 210f, 232f, 272f, 338f, 437f, 534f, 730f, 918f", "Filter"),
        LESS_OR_EQUAL("<= (less or equal)", "SELECT ${metric0} WHERE ${attr} <= ${attrValue1}", "149f, 210f, 232f, 272f, 338f, 437f, 534f", "Filter"),
        BETWEEN ("BETWEEN", "SELECT ${metric0} WHERE ${attr} BETWEEN ${attrValue0} AND ${attrValue1}", "149f, 210f, 232f, 272f, 338f, 437f, 534f, 730f, 918f", "Filter"),
        NOT_BETWEEN("NOT BETWEEN", "SELECT ${metric0} WHERE ${attr} NOT BETWEEN ${attrValue0} AND ${attrValue1}", "730f, 918f", "Filter"),
        IN("IN", "SELECT ${metric0} WHERE ${attr} IN (${attrValue0}, ${attrValue1})", "149f, 210f, 232f, 272f, 338f, 437f, 534f", "Filter"),
        NOT_IN("NOT IN", "SELECT ${metric0} WHERE ${attr} NOT IN (${attrValue0}, ${attrValue1})", "730f, 918f", "Filter"),
        WITHOUT_PF("WITHOUT PARENT FILTER", "SELECT ${metric0} - (SELECT ${metric1} BY ALL ${attr} WITHOUT PARENT FILTER)", "-116624637.54f", "Filter");

        private final String name;
        private final String maql;
        private final String values;
        private final String type;

        private MetricList(String name, String maql, String values, String type) {
            this.name = name;
            this.maql = maql;
            this.values = values;
            this.type = type;
        }

        private String getMaql() {
            return maql;
        }

        private String getType() {
            return type;
        }

        private MetricTypes getMetric() {
            MetricTypes numericMetric = null;
            for (MetricTypes metric: MetricTypes.values()) {
                if (metric.getLabel().equals(this.name))
                    numericMetric = metric;
            }
            return numericMetric;
        }

        private List<Float> getMetricValues() {
            List<Float> metricValues = new ArrayList<Float>();
            for (String el: Arrays.asList(values.split(", "))){
                metricValues.add(Float.valueOf(el));
            }
            return metricValues;
        }
    }
}