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

import com.gooddata.qa.graphene.enums.FilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.AggregationMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.FilterMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.GranularityMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.LogicalMetricTypes;
import com.gooddata.qa.graphene.enums.metrics.NumericMetricTypes;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = {"GoodSalesMetrics"}, description = "Tests for GoodSales project (metric creation functionality) in GD platform")
public class SimpleMetricTest extends GoodSalesAbstractTest {

    private String attrFolder;
    private String attr;
    private String attrValue;
    private String ratioMetric1;
    private String ratioMetric2;
    private String productAttr;
    private String stageNameAttr;
    private String quarterYearAttr;
    private Map<String, String> data;
    List<String> yearSnapshotValues;
    List<String> productValues;
    List<String> stageNameValues;

    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd HH:mm:ss";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "Simple-metric-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void initialize() throws InterruptedException, JSONException {

        attrFolder = "Date dimension (Snapshot)";
        attr = "Year (Snapshot)";
        attrValue = "2010";
        ratioMetric1 = "# of Won Opps.";
        ratioMetric2 = "# of Open Opps.";
        productAttr = "Product";
        stageNameAttr = "Stage Name";
        quarterYearAttr = "Quarter/Year (Snapshot)";
        yearSnapshotValues = Arrays.asList("2010", "2011", "2012");
        productValues = Arrays.asList("CompuSci", "Educationly", "Explorer",
                "Grammar Plus", "PhoenixSoft", "WonderKid");
        stageNameValues = Arrays.asList("Interest", "Discovery", "Short List",
                "Risk Assessment", "Conviction", "Negotiation", "Closed Won",
                "Closed Lost");
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
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

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
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

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
    public void createRatioMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metricName = "Ratio " + getCurrentDateString();
        metricEditorPage.createRatioMetric(metricName, ratioMetric1, ratioMetric2);
        List<Float> metricValues = Arrays.asList(3.61f);
        checkMetricValuesInReport(metricName, null, metricValues, null);
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
    public void createAggreationMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String fact0 = "Probability";
        String fact1 = "Amount";
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
        String expectedMaqlCorrel = "SELECT CORREL(" + fact0 + "," + fact1 + ")";
        String expectedMaqlCount = "SELECT COUNT(" + attr0 + "," + attr1 + ")";
        expectedMaql.add(expectedMaqlAvg);
        expectedMaql.add(expectedMaqlCount);
        expectedMaql.add(expectedMaqlCorrel);
        List<Float> avgMetricValues = Arrays.asList(0.1f, 0.2f, 0.3f, 0.5f,
                0.6f, 0.8f, 1.0f, 0.0f);
        List<Float> correlMetricValues = Arrays.asList(-0.05f);
        List<Float> countMetricValues = Arrays.asList(1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f);
        int i = 0;
        for (AggregationMetricTypes type : metricType) {
            String metricName = type.getLabel() + " " + getCurrentDateString();
            System.out.println(String.format(
                    "Creating %s metric, name: %s, data: %s", type, metricName,
                    data.toString()));
            metricEditorPage.createAggregationMetric(type, metricName, data);
            metricEditorPage.verifyMetric(metricName, expectedMaql.get(i), expectedFormat);
            switch (type) {
            case AVG:
                checkMetricValuesInReport(metricName, stageNameAttr,
                        avgMetricValues, stageNameValues);
                break;
            case COUNT:
                checkMetricValuesInReport(metricName, stageNameAttr,
                        countMetricValues, stageNameValues);
                break;
            case CORREL:
                checkMetricValuesInReport(metricName, null, correlMetricValues,
                        null);
                break;
            default:
                break;
            }
            i++;
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
    public void createNumericMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String metric = "Best Case";
        data = new HashMap<String, String>();
        data.put("metric0", metric);
        ArrayList<NumericMetricTypes> metricTypes = new ArrayList<NumericMetricTypes>();
        metricTypes.add(NumericMetricTypes.ABS);
        metricTypes.add(NumericMetricTypes.EXP);
        metricTypes.add(NumericMetricTypes.FLOOR);
        String expectedFormat = "#,##0.00";
        String expectedMaql;
        List<Float> ABSMetricValues = Arrays.asList(5319697.32f, 4363972.93f,
                20255220.38f, 1670164.74f, 1198603.33f, 3036473.23f);
        List<Float> EXPMetricValues = Arrays.asList(1.76f, 1.74f, 1.7f, 1.77f,
                1.79f, 1.69f);
        List<Float> FLOORMetricValues = Arrays.asList(5319697f, 4363972f,
                20255220f, 1670164f, 1198603f, 3036473f);
        for (NumericMetricTypes metricType : metricTypes) {
            String metricName = metricType.getLabel() + " " + getCurrentDateString();
            if (metricType.equals(NumericMetricTypes.EXP))
                metric = "Win Rate";
            else
                metric = "Best Case";
            data.put("metric0", metric);
            System.out.println(String.format(
                    "Creating %s metric, name: %s, data: %s", metricType,
                    metricName, data.toString()));
            metricEditorPage.createNumericMetric(metricType, metricName, data);
            expectedMaql = "SELECT " + metricType + "(" + metric + ")";
            metricEditorPage.verifyMetric(metricName, expectedMaql, expectedFormat);
            switch (metricType) {
            case ABS:
                checkMetricValuesInReport(metricName, productAttr,
                        ABSMetricValues, productValues);
                break;
            case EXP:
                checkMetricValuesInReport(metricName, productAttr,
                        EXPMetricValues, productValues);
                break;
            case FLOOR:
                checkMetricValuesInReport(metricName, productAttr,
                        FLOORMetricValues, productValues);
                break;
            default:
                break;
            }
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");

        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
    public void createGranularityMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String fact0 = "Probability";
        String attrFolder0 = "Date dimension (Snapshot)";
        String attr0 = "Year (Snapshot)";
        String metric0 = "Amount";
        String metric1 = "# of Won Opps.";
        data = new HashMap<String, String>();
        data.put("attrFolder0", attrFolder0);
        data.put("fact0", fact0);
        data.put("metric0", metric0);
        data.put("metric1", metric1);
        data.put("attribute0", attr0);
        ArrayList<GranularityMetricTypes> metricTypes = new ArrayList<GranularityMetricTypes>();
        metricTypes.add(GranularityMetricTypes.BY_ALL_ATTRIBUTE);
        metricTypes.add(GranularityMetricTypes.FOR_NEXT);
        metricTypes.add(GranularityMetricTypes.BY_ALL);
        metricTypes.add(GranularityMetricTypes.WITHIN);
        ArrayList<String> expectedMaql = new ArrayList<String>();
        String expectedFormat = "#,##0.00";
        String expectedMaqlByAllAttr = "SELECT " + metric0 + " / (SELECT "
                + metric1 + " BY ALL " + attr0 + ")";
        String expectedMaqlForNext = "SELECT " + metric0 + " FOR Next(" + attr0 + ")";
        String expectedMaqlByAll = "SELECT " + metric0 + " / (SELECT "
                + metric1 + " BY ALL IN ALL OTHER DIMENSIONS)";
        String expectedMaqlWithin = "SELECT RANK(" + metric0 + ") WITHIN (" + fact0 + ")";
        expectedMaql.add(expectedMaqlByAllAttr);
        expectedMaql.add(expectedMaqlForNext);
        expectedMaql.add(expectedMaqlByAll);
        expectedMaql.add(expectedMaqlWithin);
        List<Float> byAllAttributesMetricValues = Arrays.asList(31728.32f,
                29046.7f, 49673.35f, 27827.10f, 29310.33f, 37836.68f);
        List<Float> forNextMetricValues = Arrays.asList(27222899.64f,
                22946895.47f, 38596194.86f, 8042031.92f, 9525857.91f,
                10291576.74f);
        List<Float> byAllMetricValues = Arrays.asList(8221.96f, 6930.5f,
                11656.96f, 2428.88f, 2877.03f, 3108.3f);
        int i = 0;
        for (GranularityMetricTypes metricType : metricTypes) {
            String metricName = metricType.getLabel() + " " + getCurrentDateString();
            System.out.println(String.format(
                    "Creating %s metric, name: %s, data: %s", metricType,
                    metricName, data.toString()));
            metricEditorPage.createGranularityMetric(metricType, metricName, data);
            metricEditorPage.verifyMetric(metricName, expectedMaql.get(i), expectedFormat);
            switch (metricType) {
            case BY_ALL_ATTRIBUTE:
                checkMetricValuesInReport(metricName, productAttr,
                        byAllAttributesMetricValues, productValues);
                break;
            case FOR_NEXT:
                checkMetricValuesInReport(metricName, productAttr,
                        forNextMetricValues, productValues);
                break;
            case BY_ALL:
                checkMetricValuesInReport(metricName, productAttr,
                        byAllMetricValues, productValues);
                break;
            default:
                break;
            }
            i++;
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
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
        String expectedMaqlCase = "SELECT CASE WHEN " + metric2 + " > "
                + metric3 + " THEN 1, WHEN " + metric2 + " < " + metric3
                + " THEN 2 ELSE 3 END";
        String expectedMaqlIf = "SELECT IF " + metric4 + " > 0.5 THEN "
                + metric5 + " * 10 ELSE " + metric5 + " / 10 END";
        expectedMaql.add(expectedMaqlAnd);
        expectedMaql.add(expectedMaqlNot);
        expectedMaql.add(expectedMaqlCase);
        expectedMaql.add(expectedMaqlIf);
        List<Float> andMetricValues = Arrays.asList(74f, 78f, 69f, 31f, 28f,
                34f);
        List<Float> notMetricValues = Arrays.asList(246f, 224f, 214f, 70f, 72f,
                92f);
        List<Float> caseMetricValues = Arrays
                .asList(1f, 1f, 2f, 2f, 1f, 3f, 1f);
        List<Float> ifMetricValues = Arrays.asList(1844726.61f, 424902.79f,
                561206.26f, 260629.35f, 30674661.20f, 18620157.30f,
                383107534.50f, 4247057.12f);
        int i = 0;
        for (LogicalMetricTypes metricType : metricTypes) {
            String metricName = metricType.getLabel() + " " + getCurrentDateString();
            if (metricType.equals(LogicalMetricTypes.CASE)) {
                data.put("metric0", metric2);
                data.put("metric1", metric3);
                data.put("metric2", metric2);
                data.put("metric3", metric3);
            } else if (metricType.equals(LogicalMetricTypes.IF)) {
                data.put("metric0", metric4);
                data.put("metric1", metric5);
                data.put("metric2", metric5);
            } else {
                data.put("metric0", metric0);
            }
            System.out.println(String.format(
                    "Creating %s metric, name: %s, data: %s", metricType,
                    metricName, data.toString()));
            metricEditorPage.createLogicalMetric(metricType, metricName, data);
            metricEditorPage.verifyMetric(metricName, expectedMaql.get(i), expectedFormat);
            switch (metricType) {
            case AND:
                checkMetricValuesInReport(metricName, productAttr,
                        andMetricValues, productValues);
                break;
            case NOT:
                checkMetricValuesInReport(metricName, productAttr,
                        notMetricValues, productValues);
                break;
            case CASE:
                List<String> productValues = Arrays.asList("CompuSci",
                        "Educationly", "Explorer", "Grammar Plus",
                        "PhoenixSoft", "TouchAll", "WonderKid");
                checkMetricValuesInReport(metricName, productAttr,
                        caseMetricValues, productValues);
                break;
            case IF:
                checkMetricValuesInReport(metricName, stageNameAttr,
                        ifMetricValues, stageNameValues);
                break;
            default:
                break;
            }
            i++;
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"metric-tests"})
    public void createFilterMetricTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        String attrFolder = "Date dimension (Snapshot)";
        String attr = "Year (Snapshot)";
        String attrValue0 = "2010";
        String attrValue1 = "2011";
        String metric0 = "# of Open Opps.";
        String metric1 = "Amount";
        String fact = "Amount";
        data = new HashMap<String, String>();
        data.put("attrFolder0", attrFolder);
        data.put("attrFolder1", attrFolder);
        data.put("fact0", fact);
        data.put("fact1", fact);
        data.put("metric0", metric0);
        data.put("metric1", metric1);
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
                + metric1 + " BY ALL " + attr + " WITHOUT PARENT FILTER)";
        expectedMaql.add(expectedMaqlEqual);
        expectedMaql.add(expectedMaqlNotIn);
        expectedMaql.add(expectedMaqlBottom);
        expectedMaql.add(expectedMaqlWithoutPf);
        List<Float> equalMetricValues = Arrays.asList(149f, 210f, 232f);
        List<Float> notInMetricValues = Arrays.asList(730f, 918f);
        List<Float> withoutPfMetricValues = Arrays.asList(-116624637.54f);
        List<String> equalAttributeValues = Arrays.asList("Q2/2010", "Q3/2010",
                "Q4/2010");
        List<String> notInAttributeValues = Arrays.asList("Q1/2012", "Q2/2012");
        List<String> withoutPfAttributeValues = Arrays.asList("Q2/2012");
        int i = 0;
        for (FilterMetricTypes metricType : metricTypes) {
            String metricName = metricType.getLabel() + " " + getCurrentDateString();
            System.out.println(String.format(
                    "Creating %s metric, name: %s, data: %s", metricType,
                    metricName, data.toString()));
            metricEditorPage.createFilterMetric(metricType, metricName, data);
            metricEditorPage.verifyMetric(metricName, expectedMaql.get(i), expectedFormat);
            switch (metricType) {
            case EQUAL:
                checkMetricValuesInReport(metricName, quarterYearAttr,
                        equalMetricValues, equalAttributeValues);
                break;
            case NOT_IN:
                checkMetricValuesInReport(metricName, quarterYearAttr,
                        notInMetricValues, notInAttributeValues);
                break;
            case WITHOUT_PF:
                checkMetricValuesInReport(metricName, quarterYearAttr,
                        withoutPfMetricValues, withoutPfAttributeValues);
                break;
            default:
                break;
            }
            i++;
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|metrics");
        }
    }

    private void checkMetricValuesInReport(String metricName,
            String attributeName, List<Float> metricValues,
            List<String> attributeValues) throws InterruptedException {
        List<String> what = Arrays.asList(metricName);
        List<String> how = null;
        if (attributeName != null) {
            how = Arrays.asList(attributeName);
        }
        createReport("report_" + metricName, ReportTypes.TABLE, what, how,
                "screenshot");
        if (metricName.contains("WITHOUT PARENT FILTER")) {
            data = new HashMap<String, String>();
            data.put("attribute", "Month/Year (Snapshot)");
            data.put("attributeElements", "Apr 2012");
            reportPage.addFilter(FilterTypes.ATTRIBUTE, data);
            reportPage.saveReport();
        }
        List<Float> metricValuesinGrid = reportPage.getTableReport()
                .getMetricElements();
        Screenshots.takeScreenshot(browser, "check-metric" + "-" + metricName,
                this.getClass());
        Assert.assertEquals(metricValuesinGrid, metricValues,
                "Metric values list is incorrrect");
        if (attributeValues != null) {
            List<String> attributeValuesinGrid = reportPage.getTableReport()
                    .getAttributeElements();
            int i = 0;
            for (String attributeValue : attributeValues) {
                Assert.assertEquals(attributeValuesinGrid.get(i),
                        attributeValue);
                i++;
            }
        }
        reportPage.saveReport();
    }

    public String getCurrentDateString() {
        return DATE_FORMAT.format(new Date()).replaceAll("\\W", "-");
    }

    @Test(dependsOnGroups = {"metric-tests"}, groups = {"tests"})
    public void finalTest() {
        successfulTest = true;
    }
}