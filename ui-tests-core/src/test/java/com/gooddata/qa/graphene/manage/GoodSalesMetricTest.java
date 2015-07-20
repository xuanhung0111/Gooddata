package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.entity.metric.CustomMetricUI.buildAttribute;
import static com.gooddata.qa.graphene.entity.metric.CustomMetricUI.buildAttributeValue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.*;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.common.CheckUtils;
import com.gooddata.qa.graphene.common.Sleeper;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.reports.ReportVisualizer;
import com.gooddata.qa.graphene.fragments.reports.TableReport;

@Test(groups = {"GoodSalesMetrics"},
        description = "Tests for GoodSales project (metric creation functionality) in GD platform")
public class GoodSalesMetricTest extends GoodSalesAbstractTest {

    private static final String DATE_DIMENSION_SNAPSHOT = "Date dimension (Snapshot)";
    private static final String YEAR_SNAPSHOT = "Year (Snapshot)";
    private static final String QUARTER_YEAR_SNAPSHOT = "Quarter/Year (Snapshot)";
    private static final String QUARTER_YEAR_CREATED = "Quarter/Year (Created)";
    private static final String QUARTER_YEAR_CLOSED = "Quarter/Year (Closed)";
    private static final String DATE_DIMENSION_CLOSE = "Date dimension (Closed)";
    private static final String YEAR_CLOSE = "Year (Closed)";
    private static final String MONTH_YEAR_SNAPSHOT = "Month/Year (Snapshot)";
    private static final String YEAR_2010 = "2010";
    private static final String YEAR_2011 = "2011";
    private static final String YEAR_2012 = "2012";

    private static final String PRODUCT = "Product";
    private static final String STAGE_NAME = "Stage Name";
    private static final String AMOUNT = "Amount";
    private static final String STAGE = "Stage";
    private static final String STAGE_HISTORY = "Stage History";
    private static final String VELOCITY = "Velocity";
    private static final String DURATION = "Duration";
    private static final String PROBABILITY = "Probability";
    private static final String DAYS_TO_CLOSE = "Days to Close";
    private static final String WIN_RATE = "Win Rate";
    private static final String REMAINING_QUOTA = "Remaining Quota";
    private static final String BEST_CASE = "Best Case";
    private static final String NUMBER_OF_OPEN_OPPS = "# of Open Opps.";
    private static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    private static final String LOST = "Lost";
    private static final String WON = "Won";
    private static final String STATUS = "Status";
    private static final String DEPARTMENT = "Department";
    private static final String AVG_AMOUNT = "Avg. Amount";

    private static final String AGGREGATION = "Aggregation";
    private static final String NUMERIC = "Numeric";
    private static final String GRANULARITY = "Granularity";
    private static final String LOGICAL = "Logical";
    private static final String FILTER = "Filters";

    private static final String NO_DATA_MATCH_REPORT_MESSAGE = "No data match the filtering criteria";

    private static final List<String> PRODUCT_VALUES = asList("CompuSci", "Educationly", "Explorer",
            "Grammar Plus", "PhoenixSoft", "WonderKid");
    private static final List<String> STAGE_NAME_VALUES = asList("Interest", "Discovery", "Short List",
            "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost");
    private static final List<String> QUARTER_YEAR_VALUES = asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011",
            "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012");

    private static final String DEFAULT_METRIC_NUMBER_FORMAT = "#,##0.00";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-metric";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"filter-share-ratio-metric"})
    public void createShareMetric() {
        initMetricPage();
        String metricName = "Share % " + getCurrentDateString();
        waitForFragmentVisible(metricPage).createShareMetric(metricName, AMOUNT, DATE_DIMENSION_SNAPSHOT,
                YEAR_SNAPSHOT);

        String expectedMaql =
                "SELECT " + AMOUNT + " / (SELECT " + AMOUNT + " BY " + YEAR_SNAPSHOT + ", ALL OTHER WITHOUT PF)";
        assertTrue(metricPage.isMetricCreatedSuccessfully(metricName, expectedMaql,
                DEFAULT_METRIC_NUMBER_FORMAT));

        List<Float> metricValues = asList(0.23f, 0.2f, 0.33f, 0.07f, 0.08f, 0.09f);
        checkMetricValuesInReport(metricName, PRODUCT, metricValues, PRODUCT_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"different-granularity-logical-metric"})
    public void createDifferentMetricTest() {
        initMetricPage();
        String metricName = "Difference " + getCurrentDateString();
        waitForFragmentVisible(metricPage).createDifferentMetric(metricName, AMOUNT,
                DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2010);

        String expectedMaql =
                "SELECT " + AMOUNT + " - (SELECT " + AMOUNT + " BY ALL " + YEAR_SNAPSHOT + " WHERE "
                        + YEAR_SNAPSHOT + " IN (" + YEAR_2010 + ") WITHOUT PF)";
        assertTrue(metricPage.isMetricCreatedSuccessfully(metricName, expectedMaql,
                DEFAULT_METRIC_NUMBER_FORMAT));

        List<Float> metricValues =
                asList(20171686.25f, 16271278.69f, 32627524.67f, 6273858.91f, 7532925.39f, 6825820.63f);
        checkMetricValuesInReport(metricName, PRODUCT, metricValues, PRODUCT_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"filter-share-ratio-metric"})
    public void createRatioMetricTest() {
        initMetricPage();
        String metricName = "Ratio " + getCurrentDateString();
        waitForFragmentVisible(metricPage)
                .createRatioMetric(metricName, NUMBER_OF_WON_OPPS, NUMBER_OF_OPEN_OPPS);

        String expectedMaql = "SELECT " + NUMBER_OF_WON_OPPS + " / " + NUMBER_OF_OPEN_OPPS;
        assertTrue(metricPage.isMetricCreatedSuccessfully(metricName, expectedMaql,
                DEFAULT_METRIC_NUMBER_FORMAT));

        checkMetricValuesInReport(metricName, asList(3.61f));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void createAggregationMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI()
            .withAttributes(
                buildAttribute(STAGE, STAGE_NAME),
                buildAttribute(STAGE_HISTORY, STAGE_HISTORY));

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(AGGREGATION)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString());

            if (metric.in(asList(MetricTypes.COVAR, MetricTypes.COVARP, MetricTypes.RSQ))) {
                customMetricInfo.withFacts(VELOCITY, DURATION);

            } else if (metric.in(asList(MetricTypes.MAX, MetricTypes.RUNMAX))) {
                customMetricInfo.withFacts(DAYS_TO_CLOSE);

            } else if (metric.in(asList(MetricTypes.MIN, MetricTypes.RUNMIN))) {
                customMetricInfo.withFacts(DURATION);

            } else if (metric== MetricTypes.FORECAST) {
                customMetricInfo.withMetrics(AMOUNT);

            } else {
                customMetricInfo.withFacts(AMOUNT);
                if (metric == MetricTypes.CORREL) {
                    customMetricInfo.addMoreFacts(PROBABILITY);
                }
            }

            createCustomMetric(customMetricInfo, metric, AGGREGATION);

            if (metric.in(asList(MetricTypes.FORECAST, MetricTypes.AVG, MetricTypes.COUNT, MetricTypes.MEDIAN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), STAGE_NAME, getMetricValues(metric),
                      STAGE_NAME_VALUES);
                continue;
            }

            if (metric == MetricTypes.CORREL) {
                checkMetricValuesInReport(customMetricInfo.getName(), getMetricValues(metric));
                continue;
            }

            if (metric.in(asList(MetricTypes.COVAR, MetricTypes.COVARP, MetricTypes.PERCENTILE,
                    MetricTypes.RSQ))) {
                checkMetricValuesInReport(customMetricInfo.getName(), PRODUCT, getMetricValues(metric),
                        PRODUCT_VALUES);
                continue;
            }

            if (metric.in(asList(MetricTypes.MIN, MetricTypes.RUNMIN))) {
                checkMetricValuesInReport( customMetricInfo.getName(), QUARTER_YEAR_CREATED,
                      getMetricValues(metric),
                      asList("Q1/2008", "Q2/2008", "Q3/2008", "Q4/2008", "Q1/2009", "Q2/2009",
                              "Q3/2009", "Q4/2009", "Q1/2010", "Q2/2010", "Q3/2010", "Q4/2010",
                              "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT, getMetricValues(metric),
                  QUARTER_YEAR_VALUES);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"numeric-metric"})
    public void createNumericMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI();

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(NUMERIC)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString());

            if (metric == MetricTypes.EXP) {
                customMetricInfo.withMetrics(WIN_RATE);

            } else if (metric == MetricTypes.SIGN) {
                customMetricInfo.withMetrics(REMAINING_QUOTA);

            } else {
                customMetricInfo.withMetrics(BEST_CASE);
                if (metric == MetricTypes.SUBTRACTION) {
                    customMetricInfo.addMoreMetrics(AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, NUMERIC);

            if (metric == MetricTypes.IFNULL) {
                checkMetricValuesInReport(customMetricInfo.getName(), STATUS, getMetricValues(metric),
                        asList(LOST, "Open", WON));
                continue;
            }

            if (metric == MetricTypes.SIGN) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), QUARTER_YEAR_VALUES);
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), PRODUCT, getMetricValues(metric),
                    PRODUCT_VALUES);
            
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"different-granularity-logical-metric"})
    public void createGranularityMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI();

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(GRANULARITY)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString());

            if (metric.in(asList(MetricTypes.FOR_PREVIOUS, MetricTypes.FOR_NEXT_PERIOD,
                    MetricTypes.FOR_PREVIOUS_PERIOD))) {
                customMetricInfo.withMetrics(NUMBER_OF_OPEN_OPPS)
                    .withAttributes(buildAttribute(DATE_DIMENSION_CLOSE, YEAR_CLOSE));

            } else if (metric == MetricTypes.WITHIN) {
                customMetricInfo.withMetrics(AVG_AMOUNT)
                .withAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT));

            } else {
                customMetricInfo.withMetrics(AMOUNT, NUMBER_OF_WON_OPPS)
                    .withAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT));
                if (metric.in(asList(MetricTypes.FOR_NEXT, MetricTypes.BY_ALL_EXCEPT))) {
                    customMetricInfo.withMetrics(AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, GRANULARITY);

            if (metric.in(asList(MetricTypes.BY, MetricTypes.BY_ALL_EXCEPT, MetricTypes.WITHIN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), QUARTER_YEAR_VALUES);
                continue;
            }

            if (metric == MetricTypes.FOR_PREVIOUS) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012",
                                "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q2/2014",
                                "Q3/2014", "Q4/2014", "Q1/2015", "Q2/2015", "Q4/2015"));
                continue;
            }

            if (metric == MetricTypes.FOR_NEXT_PERIOD) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011",
                                "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013",
                                "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q3/2014"));
                continue;
            }

            if (metric == MetricTypes.FOR_PREVIOUS_PERIOD) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011",
                                "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013",
                                "Q4/2013", "Q1/2014", "Q2/2014", "Q3/2014", "Q1/2015"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), PRODUCT, getMetricValues(metric),
                    PRODUCT_VALUES);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"different-granularity-logical-metric"})
    public void createLogicalMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI();

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(LOGICAL)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString())
                .withAttributeValues(buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT,
                        YEAR_2011))
                .withAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT));

            if (metric == MetricTypes.CASE) {
                customMetricInfo.withMetrics(LOST, WON, LOST, WON);

            } else if (metric == MetricTypes.IF) {
                customMetricInfo.withMetrics(PROBABILITY, AMOUNT, AMOUNT);

            } else {
                customMetricInfo.withMetrics(NUMBER_OF_OPEN_OPPS);
                if (metric.in(asList(MetricTypes.AND, MetricTypes.OR))) {
                    customMetricInfo.addMoreAttributeValues(
                            buildAttributeValue(DATE_DIMENSION_SNAPSHOT, MONTH_YEAR_SNAPSHOT,
                                    "May 2011"))
                        .addMoreAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, MONTH_YEAR_SNAPSHOT));
                }
            }

            createCustomMetric(customMetricInfo, metric, LOGICAL);

            if (metric == MetricTypes.CASE) {
                checkMetricValuesInReport(customMetricInfo.getName(), PRODUCT, getMetricValues(metric),
                        asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll",
                                "WonderKid"));
                continue;
            }

            if (metric == MetricTypes.IF) {
                checkMetricValuesInReport(customMetricInfo.getName(), STAGE_NAME, getMetricValues(metric),
                        STAGE_NAME_VALUES);
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), PRODUCT, getMetricValues(metric),
                    PRODUCT_VALUES);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"filter-share-ratio-metric"})
    public void createFilterMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI();

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(FILTER)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString())
                .withAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT))
                .withMetrics(NUMBER_OF_OPEN_OPPS);

            if (metric.in(asList(MetricTypes.LESS, MetricTypes.LESS_OR_EQUAL))) {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2011));

            } else if (metric.in(asList(MetricTypes.TOP, MetricTypes.BOTTOM))) {
                customMetricInfo.addMoreMetrics(AMOUNT);

            } else if (metric == MetricTypes.BETWEEN) {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2010),
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2012));

            } else {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2010));
                if (metric.in(asList(MetricTypes.NOT_BETWEEN, MetricTypes.IN, MetricTypes.NOT_IN))) {
                    customMetricInfo.addMoreAttributeValues(
                            buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2011));
                }
                if (metric == MetricTypes.WITHOUT_PF) {
                    customMetricInfo.addMoreMetrics(AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, FILTER);

            if (metric.in(asList(MetricTypes.LESS, MetricTypes.EQUAL, MetricTypes.BOTTOM))) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.EQUAL), asList("Q2/2010", "Q3/2010", "Q4/2010"));
                continue;
            }

            if (metric.in(asList(MetricTypes.DOES_NOT_EQUAL, MetricTypes.GREATER))) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.GREATER),
                        asList("Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012"));
                continue;
            }

            if (metric.in(asList(MetricTypes.GREATER_OR_EQUAL, MetricTypes.BETWEEN, MetricTypes.TOP))) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.BETWEEN), QUARTER_YEAR_VALUES);
                continue;
            }

            if (metric.in(asList(MetricTypes.LESS_OR_EQUAL, MetricTypes.IN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.IN),
                        asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011"));
                continue;
            }

            if (metric == MetricTypes.WITHOUT_PF) {
                checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), asList("Q2/2012"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), QUARTER_YEAR_SNAPSHOT,
                    getMetricValues(MetricTypes.NOT_IN), asList("Q1/2012", "Q2/2012"));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void checkGreyedOutAndNonGreyedOutAttributes() {
        CustomMetricUI customMetricInfo = new CustomMetricUI().withName("SUM-Amount")
                .withFacts(AMOUNT);
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.SUM, AGGREGATION);

        ReportDefinition reportDefinition = new ReportDefinition()
                .withName("Report-" + customMetricInfo.getName()).withWhats(customMetricInfo.getName());
        initReportCreation();
        ReportVisualizer visualiser = reportPage.getVisualiser();
        visualiser.selectWhatArea(reportDefinition.getWhats());

        List<String> greyedOutAttribues = asList("Activity Type", "Date (Activity)", "Date (Timeline)",
                STAGE_HISTORY);

        List<String> nonGreyedOutAttribues = asList("Account", "Date (Created)", "Opp. Snapshot", "Opportunity",
                DEPARTMENT, PRODUCT, STAGE_NAME, YEAR_CLOSE, MONTH_YEAR_SNAPSHOT);
        visualiser.clickOnHow();
        for(String attribute : greyedOutAttribues) {
            assertTrue(visualiser.isGreyedOutAttribute(attribute),
                    String.format("Attribue %s is not unreachable", attribute));
        }

        for(String attribute : nonGreyedOutAttribues) {
            assertFalse(visualiser.isGreyedOutAttribute(attribute),
                    String.format("Attribue %s is unreachable", attribute));
        }
        visualiser.finishReportChanges();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void checkDrillDownDefine() {
        initAttributePage();
        attributePage.initAttribute(STAGE_NAME);
        attributeDetailPage.setDrillToAttribute(DEPARTMENT);

        String metricName = "RUNSUM-Amount";
        CustomMetricUI customMetricInfo = new CustomMetricUI().withName(metricName).withFacts(AMOUNT);
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.RUNSUM, AGGREGATION);

        ReportDefinition reportDefinition = new ReportDefinition().withName("Report-" + metricName)
                .withWhats(metricName).withHows(STAGE_NAME);
        createReport(reportDefinition, "screenshot-" + "report_" + customMetricInfo.getName());
        reportPage.getTableReport().drillOnAttributeValue("Short List");
        Sleeper.sleepTight(1000); // Wait for drill report is present

        TableReport drillReport = reportPage.getTableReport();
        drillReport.waitForReportLoading();
        List<Float> drillReportMetrics = asList(1.72400224E8f, 1.16374376E8f, 2.88774592E8f);
        List<String> drillReportAttributes = asList("Direct Sales", "Inside Sales");
        System.out.println("Drill metrics: " + drillReport.getMetricElements());
        System.out.println("Drill attribues: " + drillReport.getAttributeElements());
        assertTrue(isEqualCollection(drillReport.getMetricElements(), drillReportMetrics),
                "Metric values list of drill report is incorrrect");
        assertTrue(isEqualCollection(drillReport.getAttributeElements(), drillReportAttributes),
                "Metric values list of drill report is incorrrect");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"filter-share-ratio-metric"})
    public void createInvalidMetric() {
        CustomMetricUI customMetricInfo = new CustomMetricUI().withName("WRONG METRIC WITH BETWEEN")
                .withMetrics(NUMBER_OF_OPEN_OPPS)
                .withAttributes(buildAttribute(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT))
                .withAttributeValues(
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2012),
                        buildAttributeValue(DATE_DIMENSION_SNAPSHOT, YEAR_SNAPSHOT, YEAR_2010));
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.BETWEEN, FILTER);

        ReportDefinition reportDefinition = new ReportDefinition()
                .withName("Report-" + customMetricInfo.getName()).withWhats(customMetricInfo.getName());
        createReport(reportDefinition, "screenshot-" + "report_" + customMetricInfo.getName());
        waitForFragmentVisible(reportPage);
        CheckUtils.checkRedBar(browser);
        assertEquals(reportPage.getVisualiser().getDataReportHelpMessage(), NO_DATA_MATCH_REPORT_MESSAGE,
                "Report help message is incorrect!");
        reportPage.saveReport();
    }

    private void checkMetricValuesInReport(String metricName, String attributeName, List<Float> metricValues,
            List<String> attributeValues) {
        System.out.println("Verifying metric values of [" + metricName + "] in report");

        ReportDefinition reportDefinition = new ReportDefinition();
        if (attributeName != null) {
            reportDefinition.withHows(attributeName);
        }
        reportDefinition.withWhats(metricName);
        if (metricName.contains("IFNULL")) {
            reportDefinition.withWhats(AMOUNT);
        }
        reportDefinition.withName("report_" + metricName);

        createReport(reportDefinition, "screenshot-" + "report_" + metricName);

        if (metricName.contains("WITHOUT_PF")) {
            reportPage.addFilter(FilterItem.Factory.createListValuesFilter(MONTH_YEAR_SNAPSHOT, "Apr 2012"))
                    .saveReport();
        }
        List<Float> metricValuesinGrid = reportPage.getTableReport().getMetricElements();
        takeScreenshot(browser, "check-metric" + "-" + metricName, this.getClass());
        System.out.println("Actual metric values:   " + metricValuesinGrid);
        System.out.println("Expected metric values: " + metricValues);
        assertEquals(metricValuesinGrid, metricValues, "Metric values list is incorrrect");
        if (attributeValues != null) {
            List<String> attributeValuesinGrid = reportPage.getTableReport().getAttributeElements();
            assertEquals(attributeValuesinGrid, attributeValues);
        }
        reportPage.saveReport();
    }

    private void checkMetricValuesInReport(String metricName, List<Float> metricValues) {
        checkMetricValuesInReport(metricName, null, metricValues, null);
    }

    private String getCurrentDateString() {
        return DATE_FORMAT.format(new Date()).replaceAll("\\W", "-");
    }

    private void createCustomMetric(CustomMetricUI customMetricInfo, MetricTypes metric, String type) {
        String expectedMaql = customMetricInfo.buildMaql(metric.getMaql());
        System.out.println(format("Creating %s metric [%s]: %s", metric, customMetricInfo.getName(),
                expectedMaql));

        if (AGGREGATION.equals(type)) {
            waitForFragmentVisible(metricPage).createAggregationMetric(metric, customMetricInfo);
        } else if (NUMERIC.equals(type)) {
            waitForFragmentVisible(metricPage).createNumericMetric(metric, customMetricInfo);
        } else if (GRANULARITY.equals(type)) {
            waitForFragmentVisible(metricPage).createGranularityMetric(metric, customMetricInfo);
        } else if (LOGICAL.equals(type)) {
            waitForFragmentVisible(metricPage).createLogicalMetric(metric, customMetricInfo);
        } else {
            waitForFragmentVisible(metricPage).createFilterMetric(metric, customMetricInfo);
        }

        assertTrue(metricPage.isMetricCreatedSuccessfully(customMetricInfo.getName(), expectedMaql,
                DEFAULT_METRIC_NUMBER_FORMAT));
    }

    private List<Float> getMetricValues(MetricTypes metric) {
        switch (metric) {
            case ABS:
                return asList(5319697.32f, 4363972.93f, 20255220.38f, 1670164.74f, 1198603.33f, 3036473.23f);
            case EXP:
                return asList(1.76f, 1.74f, 1.7f, 1.77f, 1.79f, 1.69f);
            case IFNULL:
                return asList(0f, 35844131.93f, 0f, 42470571.16f, 35844131.93f, 38310753.45f);
            case LOG:
                return asList(6.73f, 6.64f, 7.31f, 6.22f, 6.08f, 6.48f);
            case LN:
                return asList(15.49f, 15.29f, 16.82f, 14.33f, 14f, 14.93f);
            case POWER:
                return asList(5319697.32f, 4363972.93f, 20255220.38f, 1670164.74f, 1198603.33f, 3036473.23f);
            case RANK:
                return asList(5f, 4f, 6f, 2f, 1f, 3f);
            case ROUND:
                return asList(5319697f, 4363973f, 20255220f, 1670165f, 1198603f, 3036473f);
            case FLOOR:
                return asList(5319697f, 4363972f, 20255220f, 1670164f, 1198603f, 3036473f);
            case CEILING:
                return asList(5319698f, 4363973f, 20255221f, 1670165f, 1198604f, 3036474f);
            case TRUNC:
                return asList(5319697f, 4363972f, 20255220f, 1670164f, 1198603f, 3036473f);
            case SIGN:
                return asList(1f, -1f, -1f, -1f, -1f, -1f, -1f, -1f, -1f);
            case SQRT:
                return asList(2306.45f, 2089.01f, 4500.58f, 1292.35f, 1094.81f, 1742.55f);
            case SUBTRACTION:
                return asList(-21903202.32f, -18582922.54f, -18340974.48f, -6371867.18f, -8327254.58f,
                        -7255103.51f);
            case AVG:
                return asList(91950.44f, 24518.32f, 30384.53f, 59651.14f, 37137.94f, 28180f, 11747.9f, 24006.22f);
            case RUNAVG:
                return asList(28781.53f, 22976.64f, 20236.14f, 18799.05f, 18254.70f, 18797.26f, 19476.70f,
                        19760.85f, 19923.45f);
            case MAX:
                return asList(679f, 710f, 1460f, 1446f, 1355f, 1264f, 1173f, 1844f, 1795f);
            case RUNMAX:
                return asList(679f, 710f, 1460f, 1460f, 1460f, 1460f, 1460f, 1844f, 1844f);
            case MIN:
                return asList(0f, 9f, 2f, 6f, 7f, 5f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 3f);
            case RUNMIN:
                return asList(0f, 9f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f, 2f);
            case SUM:
                return asList(26680476.45f, 173032507.14f, 286622094.62f, 403085507.4f, 549068386.54f,
                        784155771.33f, 1026730772.12f, 1245801949.39f, 1122736243.73f);
            case RUNSUM:
                return asList(26680476.45f, 199712983.59f, 486335078.21f, 889420585.61f, 1438488972.15f,
                        2222644743.48f, 3249375515.6f, 4495177464.99f, 5617913708.72f);
            case MEDIAN:
                return asList(5240.16f, 6362.40f, 12000f, 12000f, 16998.12f, 13187.33f, 2980.46f, 4800f);
            case CORREL:
                return asList(-0.05f);
            case COUNT:
                return asList(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            case COVAR:
                return asList(2.26f, 8.17f, 38.61f, 59.28f, 14.13f, 52.22f);
            case COVARP:
                return asList(2.26f, 8.15f, 38.5f, 58.74f, 14.02f, 51.8f);
            case PERCENTILE:
                return asList(832.80f, 769.52f, 940.80f, 1348.12f, 1036.80f, 900f);
            case RSQ:
                return asList(0f, 0f, 0.02f, 0.05f, 0f, 0.03f);
            case STDEV:
                return asList(74188.03f, 66021.22f, 52967.24f, 48908.74f, 47954.34f, 169291.7f, 220273.73f,
                        204454.97f, 192490.02f);
            case RUNSTDEV:
                return asList(74188.03f, 66965.11f, 58453.34f, 53987.64f, 51665.31f, 106485.98f, 148893.7f,
                        165539.55f, 171079.39f);
            case VAR:
                return asList(5503864027f, 4358800996.88f, 2805528940.9f, 2392065115.96f, 2299618561.68f,
                        28659680430.94f, 48520516045.51f, 41801835719.68f, 37052408241.94f);
            case RUNVAR:
                return asList(5503864027f, 4484325735.48f, 3416792441.66f, 2914665138.04f, 2669304494.43f,
                        11339263468.53f, 22169333880.03f, 27403342437.44f, 29268158189.74f);
            case BY:
                return asList(10780.66f, 23031.85f, 34560.16f, 14490.03f, 19202.08f, 29123.39f, 34651.63f,
                        31400.59f, 35223.64f);
            case BY_ALL_ATTRIBUTE:
                return asList(31728.32f, 29046.7f, 49673.35f, 27827.10f, 29310.33f, 37836.68f);
            case BY_ALL:
                return asList(8221.96f, 6930.5f, 11656.96f, 2428.88f, 2877.03f, 3108.3f);
            case BY_ATTR_ALL_OTHER:
                return asList(8221.96f, 6930.5f, 11656.96f, 2428.88f, 2877.03f, 3108.3f);
            case FOR_NEXT:
                return asList(27222899.64f, 22946895.47f, 38596194.86f, 8042031.92f, 9525857.91f, 10291576.74f);
            case FOR_PREVIOUS:
                return asList(13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f);
            case FOR_NEXT_PERIOD:
                return asList(13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f);
            case FOR_PREVIOUS_PERIOD:
                return asList(13f, 17f, 16f, 24f, 20f, 14f, 17f, 122f, 452f, 144f, 38f, 30f, 5f, 1f, 1f, 2f, 1f);
            case BY_ALL_EXCEPT:
                return asList(8398134.81f, 17941808.52f, 26922362f, 36036707.95f, 47755578.7f, 72429870.62f,
                        86178611.71f, 103967358.32f, 116625456.54f);
            case AND:
                return asList(74f, 78f, 69f, 31f, 28f, 34f);
            case OR:
                return asList(119f, 137f, 128f, 39f, 48f, 63f);
            case CASE:
                return asList(1f, 1f, 2f, 2f, 1f, 3f, 1f);
            case IF:
                return asList(1844726.61f, 424902.79f, 561206.26f, 260629.35f, 30674661.20f, 18620157.30f,
                        383107534.50f, 4247057.12f);
            case NOT:
                return asList(246f, 224f, 214f, 70f, 72f, 92f);
            case EQUAL:
                return asList(149f, 210f, 232f);
            case DOES_NOT_EQUAL:
                return asList(272f, 338f, 437f, 534f, 730f, 918f);
            case GREATER:
                return asList(272f, 338f, 437f, 534f, 730f, 918f);
            case LESS:
                return asList(149f, 210f, 232f);
            case GREATER_OR_EQUAL:
                return asList(149f, 210f, 232f, 272f, 338f, 437f, 534f, 730f, 918f);
            case LESS_OR_EQUAL:
                return asList(149f, 210f, 232f, 272f, 338f, 437f, 534f);
            case BETWEEN:
                return asList(149f, 210f, 232f, 272f, 338f, 437f, 534f, 730f, 918f);
            case NOT_BETWEEN:
                return asList(730f, 918f);
            case IN:
                return asList(149f, 210f, 232f, 272f, 338f, 437f, 534f);
            case NOT_IN:
                return asList(730f, 918f);
            case WITHOUT_PF:
                return asList(-116624637.54f);
            case WITHIN:
                return asList(3.0f, 2.0f, 1.0f, 1.0f, 2.0f, 4.0f, 3.0f, 2.0f, 1.0f);
            case TOP:
                return asList(149.0f, 210.0f, 232.0f, 272.0f, 338.0f, 437.0f, 534.0f, 730.0f, 918.0f);
            case BOTTOM:
                return asList(149.0f, 210.0f, 232.0f);
            case FORECAST:
                return asList(1.8447266E7f, 4249028.0f, 5612062.5f, 2606293.5f, 3067466.0f, 1862015.8f,
                        3.8310752E7f, 4.2470572E7f);
        }
        return Collections.emptyList();
    }
}
