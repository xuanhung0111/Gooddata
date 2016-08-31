package com.gooddata.qa.graphene.manage;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.entity.metric.CustomMetricUI.buildAttributeValue;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SNAPSHOT_EOP1;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SNAPSHOT_EOP2;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_HISTORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CLOSE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_DAYS_TO_CLOSE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_DURATION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PROBABILITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_VELOCITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WIN_RATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.report.ReportExportFormat;
import com.gooddata.report.ReportService;

public class GoodSalesMetricTest extends GoodSalesAbstractTest {

    private static final String YEAR_2010 = "2010";
    private static final String YEAR_2011 = "2011";
    private static final String YEAR_2012 = "2012";

    private static final String NEGATIVE = "negative";
    private static final String NULL_METRIC = "null-metric";

    private static final String AGGREGATION = "Aggregation";
    private static final String NUMERIC = "Numeric";
    private static final String GRANULARITY = "Granularity";
    private static final String LOGICAL = "Logical";
    private static final String FILTER = "Filters";

    private static final String NO_DATA_MATCH_REPORT_MESSAGE = "No data match the filtering criteria";

    private static final Object REPORT_NOT_COMPUTABLE_MESSAGE =
            "Report not computable due to improper metric definition";

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
        String metricName = "Share % " + getCurrentDateString();
        String expectedMaql =
                "SELECT " + METRIC_AMOUNT + " / (SELECT " + METRIC_AMOUNT + " BY " + ATTR_YEAR_SNAPSHOT + ", ALL OTHER WITHOUT PF)";
        assertTrue(initMetricPage().createShareMetric(metricName, METRIC_AMOUNT, ATTR_YEAR_SNAPSHOT)
                .isMetricCreatedSuccessfully(metricName, expectedMaql, DEFAULT_METRIC_NUMBER_FORMAT));

        List<Float> metricValues = asList(0.23f, 0.2f, 0.33f, 0.07f, 0.08f, 0.09f);
        checkMetricValuesInReport(metricName, ATTR_PRODUCT, metricValues, PRODUCT_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"different-granularity-logical-metric"})
    public void createDifferentMetricTest() {
        String metricName = "Difference " + getCurrentDateString();
        String expectedMaql =
                "SELECT " + METRIC_AMOUNT + " - (SELECT " + METRIC_AMOUNT + " BY ALL " + ATTR_YEAR_SNAPSHOT + " WHERE "
                        + ATTR_YEAR_SNAPSHOT + " IN (" + YEAR_2010 + ") WITHOUT PF)";
        assertTrue(initMetricPage().createDifferentMetric(metricName, METRIC_AMOUNT, ATTR_YEAR_SNAPSHOT, YEAR_2010)
                .isMetricCreatedSuccessfully(metricName, expectedMaql, DEFAULT_METRIC_NUMBER_FORMAT));

        List<Float> metricValues =
                asList(20171686.25f, 16271278.69f, 32627524.67f, 6273858.91f, 7532925.39f, 6825820.63f);
        checkMetricValuesInReport(metricName, ATTR_PRODUCT, metricValues, PRODUCT_VALUES);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"filter-share-ratio-metric"})
    public void createRatioMetricTest() {
        String metricName = "Ratio " + getCurrentDateString();
        String expectedMaql = "SELECT " + METRIC_NUMBER_OF_WON_OPPS + " / " + METRIC_NUMBER_OF_OPEN_OPPS;
        assertTrue(initMetricPage().createRatioMetric(metricName, METRIC_NUMBER_OF_WON_OPPS, METRIC_NUMBER_OF_OPEN_OPPS)
                .isMetricCreatedSuccessfully(metricName, expectedMaql, DEFAULT_METRIC_NUMBER_FORMAT));

        checkMetricValuesInReport(metricName, asList(3.61f));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void createAggregationMetricTest() {
        CustomMetricUI customMetricInfo = new CustomMetricUI()
            .withAttributes(ATTR_STAGE_NAME, ATTR_STAGE_HISTORY);

        for (MetricTypes metric : MetricTypes.values()) {
            if (!metric.getType().equalsIgnoreCase(AGGREGATION)) {
                continue;
            }

            initMetricPage();
            customMetricInfo.withName(metric + " " + getCurrentDateString());

            if (metric.in(asList(MetricTypes.COVAR, MetricTypes.COVARP, MetricTypes.RSQ))) {
                customMetricInfo.withFacts(METRIC_VELOCITY, METRIC_DURATION);

            } else if (metric.in(asList(MetricTypes.MAX, MetricTypes.RUNMAX))) {
                customMetricInfo.withFacts(METRIC_DAYS_TO_CLOSE);

            } else if (metric.in(asList(MetricTypes.MIN, MetricTypes.RUNMIN))) {
                customMetricInfo.withFacts(METRIC_DURATION);

            } else if (metric== MetricTypes.FORECAST) {
                customMetricInfo.withMetrics(METRIC_AMOUNT);

            } else {
                customMetricInfo.withFacts(METRIC_AMOUNT);
                if (metric == MetricTypes.CORREL) {
                    customMetricInfo.addMoreFacts(METRIC_PROBABILITY);
                }
            }

            createCustomMetric(customMetricInfo, metric, AGGREGATION);

            if (metric.in(asList(MetricTypes.FORECAST, MetricTypes.AVG, MetricTypes.COUNT, MetricTypes.MEDIAN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_STAGE_NAME, getMetricValues(metric),
                      STAGE_NAME_VALUES);
                continue;
            }

            if (metric == MetricTypes.CORREL) {
                checkMetricValuesInReport(customMetricInfo.getName(), getMetricValues(metric));
                continue;
            }

            if (metric.in(asList(MetricTypes.COVAR, MetricTypes.COVARP, MetricTypes.PERCENTILE,
                    MetricTypes.RSQ))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_PRODUCT, getMetricValues(metric),
                        PRODUCT_VALUES);
                continue;
            }

            if (metric.in(asList(MetricTypes.MIN, MetricTypes.RUNMIN))) {
                checkMetricValuesInReport( customMetricInfo.getName(), ATTR_QUARTER_YEAR_CREATED,
                      getMetricValues(metric),
                      asList("Q1/2008", "Q2/2008", "Q3/2008", "Q4/2008", "Q1/2009", "Q2/2009",
                              "Q3/2009", "Q4/2009", "Q1/2010", "Q2/2010", "Q3/2010", "Q4/2010",
                              "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT, getMetricValues(metric),
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
                customMetricInfo.withMetrics(METRIC_WIN_RATE);

            } else if (metric == MetricTypes.SIGN) {
                customMetricInfo.withMetrics(METRIC_LOST);

            } else {
                customMetricInfo.withMetrics(METRIC_BEST_CASE);
                if (metric == MetricTypes.SUBTRACTION) {
                    customMetricInfo.addMoreMetrics(METRIC_AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, NUMERIC);

            if (metric == MetricTypes.IFNULL) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_STATUS, getMetricValues(metric),
                        asList(METRIC_LOST, "Open", METRIC_WON));
                continue;
            }

            if (metric == MetricTypes.SIGN) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), QUARTER_YEAR_VALUES);
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), ATTR_PRODUCT, getMetricValues(metric),
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
                customMetricInfo.withMetrics(METRIC_NUMBER_OF_OPEN_OPPS)
                    .withAttributes(ATTR_YEAR_CLOSE);

            } else if (metric == MetricTypes.WITHIN) {
                customMetricInfo.withMetrics(METRIC_AVG_AMOUNT)
                .withAttributes(ATTR_YEAR_SNAPSHOT);

            } else {
                customMetricInfo.withMetrics(METRIC_AMOUNT, METRIC_NUMBER_OF_WON_OPPS)
                    .withAttributes(ATTR_YEAR_SNAPSHOT);
                if (metric.in(asList(MetricTypes.FOR_NEXT, MetricTypes.BY_ALL_EXCEPT))) {
                    customMetricInfo.withMetrics(METRIC_AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, GRANULARITY);

            if (metric.in(asList(MetricTypes.BY, MetricTypes.BY_ALL_EXCEPT, MetricTypes.WITHIN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), QUARTER_YEAR_VALUES);
                continue;
            }

            if (metric == MetricTypes.FOR_PREVIOUS) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012",
                                "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q2/2014",
                                "Q3/2014", "Q4/2014", "Q1/2015", "Q2/2015", "Q4/2015"));
                continue;
            }

            if (metric == MetricTypes.FOR_NEXT_PERIOD) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011",
                                "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013",
                                "Q2/2013", "Q3/2013", "Q4/2013", "Q1/2014", "Q3/2014"));
                continue;
            }

            if (metric == MetricTypes.FOR_PREVIOUS_PERIOD) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_CLOSED,
                        getMetricValues(metric), asList("Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011",
                                "Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012", "Q1/2013", "Q2/2013", "Q3/2013",
                                "Q4/2013", "Q1/2014", "Q2/2014", "Q3/2014", "Q1/2015"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), ATTR_PRODUCT, getMetricValues(metric),
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
                .withAttributeValues(buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2011))
                .withAttributes(ATTR_YEAR_SNAPSHOT);

            if (metric == MetricTypes.CASE) {
                customMetricInfo.withMetrics(METRIC_LOST, METRIC_WON, METRIC_LOST, METRIC_WON);

            } else if (metric == MetricTypes.IF) {
                customMetricInfo.withMetrics(METRIC_PROBABILITY, METRIC_AMOUNT, METRIC_AMOUNT);

            } else {
                customMetricInfo.withMetrics(METRIC_NUMBER_OF_OPEN_OPPS);
                if (metric.in(asList(MetricTypes.AND, MetricTypes.OR))) {
                    customMetricInfo.addMoreAttributeValues(
                            buildAttributeValue(ATTR_MONTH_YEAR_SNAPSHOT, "May 2011"))
                        .addMoreAttributes(ATTR_MONTH_YEAR_SNAPSHOT);
                }
            }

            createCustomMetric(customMetricInfo, metric, LOGICAL);

            if (metric == MetricTypes.CASE) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_PRODUCT, getMetricValues(metric),
                        asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll",
                                "WonderKid"));
                continue;
            }

            if (metric == MetricTypes.IF) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_STAGE_NAME, getMetricValues(metric),
                        STAGE_NAME_VALUES);
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), ATTR_PRODUCT, getMetricValues(metric),
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
                .withAttributes(ATTR_YEAR_SNAPSHOT)
                .withMetrics(METRIC_NUMBER_OF_OPEN_OPPS);

            if (metric.in(asList(MetricTypes.LESS, MetricTypes.LESS_OR_EQUAL))) {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2011));

            } else if (metric.in(asList(MetricTypes.TOP, MetricTypes.BOTTOM))) {
                customMetricInfo.addMoreMetrics(METRIC_AMOUNT);

            } else if (metric == MetricTypes.BETWEEN) {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2010),
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2012));

            } else {
                customMetricInfo.withAttributeValues(
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2010));
                if (metric.in(asList(MetricTypes.NOT_BETWEEN, MetricTypes.IN, MetricTypes.NOT_IN))) {
                    customMetricInfo.addMoreAttributeValues(
                            buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2011));
                }
                if (metric == MetricTypes.WITHOUT_PF) {
                    customMetricInfo.addMoreMetrics(METRIC_AMOUNT);
                }
            }

            createCustomMetric(customMetricInfo, metric, FILTER);

            if (metric.in(asList(MetricTypes.LESS, MetricTypes.EQUAL, MetricTypes.BOTTOM))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.EQUAL), asList("Q2/2010", "Q3/2010", "Q4/2010"));
                continue;
            }

            if (metric.in(asList(MetricTypes.DOES_NOT_EQUAL, MetricTypes.GREATER))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.GREATER),
                        asList("Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011", "Q1/2012", "Q2/2012"));
                continue;
            }

            if (metric.in(asList(MetricTypes.GREATER_OR_EQUAL, MetricTypes.BETWEEN, MetricTypes.TOP))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.BETWEEN), QUARTER_YEAR_VALUES);
                continue;
            }

            if (metric.in(asList(MetricTypes.LESS_OR_EQUAL, MetricTypes.IN))) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(MetricTypes.IN),
                        asList("Q2/2010", "Q3/2010", "Q4/2010", "Q1/2011", "Q2/2011", "Q3/2011", "Q4/2011"));
                continue;
            }

            if (metric == MetricTypes.WITHOUT_PF) {
                checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                        getMetricValues(metric), asList("Q2/2012"));
                continue;
            }

            checkMetricValuesInReport(customMetricInfo.getName(), ATTR_QUARTER_YEAR_SNAPSHOT,
                    getMetricValues(MetricTypes.NOT_IN), asList("Q1/2012", "Q2/2012"));
        }
    }

    @DataProvider(name = "likeProvider")
    public Object[][] likeProvider() {
        return new Object[][] {
            {MetricTypes.LIKE, "%m%", asList(
                    asList("Email", "33920"))},
            {MetricTypes.NOT_LIKE, "%m%", asList(
                    asList("In Person Meeting", "35975"),
                    asList("Phone Call", "50780"),
                    asList("Web Meeting", "33596"))},
            {MetricTypes.ILIKE, "%M%", asList(
                    asList("Email", "33920"),
                    asList("In Person Meeting", "35975"),
                    asList("Web Meeting", "33596"))},
            {MetricTypes.NOT_ILIKE, "%M%", asList(
                    asList("Phone Call", "50780"))}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric"}, dataProvider = "likeProvider")
    public void testLikeFunctions(MetricTypes metricType, String pattern, List<List<String>> expectedResult) 
            throws IOException {
        Metric metricObj = getMdService().getObj(getProject(), Metric.class, Restriction.title("# of Activities"));
        Attribute attrObj = getMdService().getObj(getProject(), Attribute.class, Restriction.title("Activity Type"));
        Metric metric = createLikeMetric(metricType, attrObj, metricObj, pattern);

        ReportDefinition definition = GridReportDefinitionContent.create("Report for " + metricType.getLabel(),
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(attrObj.getDefaultDisplayForm().getUri(), attrObj.getTitle())),
                singletonList(new MetricElement(metric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        final ByteArrayOutputStream output = exportReport(definition);
        List<List<String>> actualResult = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(
                new ByteArrayInputStream(output.toByteArray())), CsvPreference.STANDARD_PREFERENCE)){
            reader.getHeader(true);
            List<String> reportResult;
            while ((reportResult = reader.read()) != null) {
                actualResult.add(reportResult);
            }
        }
        assertEquals(actualResult, expectedResult);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric"})
    public void testLikeFunctionWithVariousPattern() throws IOException {
        List<List<String>> expectedResult = asList(asList("Explorer", "1465", "1465", "1465", null));
        String reportName = "LIKE_OPERATOR_with_various_pattern";
        Metric metricObj = getMdService().getObj(getProject(), Metric.class, Restriction.title("# of Opportunities"));
        Attribute attrObj = getMdService().getObj(getProject(), Attribute.class, Restriction.title("Product"));

        Metric metricWithFullStringPattern = createLikeMetric(MetricTypes.LIKE, attrObj, metricObj, "Explorer");
        Metric metricWithPercentSignPattern = createLikeMetric(MetricTypes.LIKE, attrObj, metricObj, "%lorer");
        //one underscore presents for one character, so if two characters are missing, it requires two underscores
        Metric metricWithUnderscorePattern = createLikeMetric(MetricTypes.LIKE, attrObj, metricObj, "Expl__er");
        Metric metricWithWrongUnderscorePattern = createLikeMetric(MetricTypes.LIKE, attrObj, metricObj, "Expl_r");

        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(attrObj.getDefaultDisplayForm().getUri(), attrObj.getTitle())),
                asList(new MetricElement(metricWithFullStringPattern),
                        new MetricElement(metricWithPercentSignPattern),
                        new MetricElement(metricWithUnderscorePattern),
                        new MetricElement(metricWithWrongUnderscorePattern)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        final ByteArrayOutputStream output = exportReport(definition);
        List<List<String>> actualResult = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(
                new ByteArrayInputStream(output.toByteArray())), CsvPreference.STANDARD_PREFERENCE)){
            reader.getHeader(true);
            List<String> reportResult;
            while ((reportResult = reader.read()) != null) {
                actualResult.add(reportResult);
            }
        }
        assertEquals(actualResult, expectedResult);
    }

    @DataProvider(name = "greatestLeastProvider")
    public Object[][] greatestLeastProvider() {
        return new Object[][] {
            {"GREATEST_LEAST_basic", asList(METRIC_SNAPSHOT_BOP, ATTR_SNAPSHOT_EOP1, ATTR_SNAPSHOT_EOP2),
                    getResultInGreatestLeastBasic()},
            {"GREATEST_LEAST_no_aggregation", asList(METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_OPEN_OPPS, NEGATIVE), asList(
                    asList("262", "262", "-38", "262", "-38"),
                    asList("247", "247", "-53", "247", "-53"),
                    asList("198", "198", "-102", "198", "-102"),
                    asList("46", "46", "-254", "46", "-254"),
                    asList("87", "87", "-213", "87", "-213"),
                    asList("78", "78", "-222", "78", "-222"),
                    asList("3311", null, null, null, null),
                    asList("1770", null, null, null, null))},
            {"GREATEST_LEAST_null", asList(METRIC_NUMBER_OF_OPEN_OPPS, NULL_METRIC, METRIC_SNAPSHOT_BOP), asList(
                    asList("262", "1", "40334", "40334", "1"),
                    asList("247", "1", "40334", "40334", "1"),
                    asList("198", "0", "40334", "40334", "0"),
                    asList("46", "0", "40334", "40334", "0"),
                    asList("87", "0", "40334", "40334", "0"),
                    asList("78", "0", "40334", "40334", "0"),
                    asList(null, "0", "40334", null, null),
                    asList(null, "0", "40334", null, null))}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric", "init-metrics"})
    public void createNegativeMetric() {
        String negativeMaql = "SELECT [" + getMdService().getObjUri(getProject(), Metric.class,
                Restriction.title(METRIC_NUMBER_OF_OPEN_OPPS)) + "] - 300";
        getMdService().createObj(getProject(), new Metric(NEGATIVE, negativeMaql, DEFAULT_METRIC_NUMBER_FORMAT));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric", "init-metrics"})
    public void createMetricHasNullValue() {
        String nullMaql = "SELECT CASE WHEN [" + getMdService().getObjUri(getProject(), Metric.class,
                Restriction.title(METRIC_NUMBER_OF_OPEN_OPPS)) + "] > 200 THEN 1 END";
        String ifNullMaql = "SELECT IFNULL((" + nullMaql+ "),0)";
        getMdService().createObj(getProject(), new Metric(NULL_METRIC, ifNullMaql, DEFAULT_METRIC_NUMBER_FORMAT));
    }

    @Test(dependsOnGroups = {"init-metrics"}, groups = {"non-UI-metric"},
            dataProvider = "greatestLeastProvider")
    public void testGreatestAndLeastFunction(String reportName, List<String> metrics,
            List<List<String>> expectedResult) throws IOException {
        List<Metric> greatestLeastMetrics = createGreatestAndLeastMetric(ATTR_STAGE_NAME, metrics);
        Attribute attrObj = getMdService().getObj(getProject(), Attribute.class, Restriction.title(ATTR_STAGE_NAME));

        List<MetricElement> gridElements = metrics.stream()
            .map(title -> getMdService().getObj(getProject(), Metric.class, Restriction.title(title)))
            .map(metric -> new MetricElement(metric))
            .collect(toList());

        greatestLeastMetrics.stream()
            .map(metric -> new MetricElement(metric))
            .forEach(metric -> gridElements.add(metric));

        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(attrObj.getDefaultDisplayForm().getUri(), attrObj.getTitle())), gridElements);
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        final ByteArrayOutputStream output = exportReport(definition);
        List<List<String>> actualResult = new ArrayList<>();
        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(
                new ByteArrayInputStream(output.toByteArray())), CsvPreference.STANDARD_PREFERENCE)){
            reader.getHeader(true);
            List<String> reportResult;
            while ((reportResult = reader.read()) != null) {
                actualResult.add(reportResult.subList(1, reportResult.size()));
            }
        }
        assertEquals(actualResult, expectedResult);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric"})
    public void testRollingWindowMetrics() throws ParseException, JSONException, IOException {
        Metric amount = getMdService().getObj(getProject(), Metric.class, Restriction.title(METRIC_AMOUNT));
        String yearUri = getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(ATTR_YEAR_SNAPSHOT));
        Attribute date = getMdService().getObj(getProject(), Attribute.class, Restriction.title(ATTR_DATE_SNAPSHOT));
        Attribute monthYear = getMdService().getObj(getProject(), Attribute.class, Restriction.title(ATTR_MONTH_YEAR_SNAPSHOT));

        Metric m1 = getMdService().createObj(getProject(), new Metric("M1",
                "SELECT RUNSUM( [" + amount.getUri() + "] ) ROWS BETWEEN 5 PRECEDING AND CURRENT ROW",
                DEFAULT_METRIC_NUMBER_FORMAT));

        Metric m2 = getMdService().createObj(getProject(), new Metric("M2",
                "SELECT RUNSUM( [" + amount.getUri() + "] ) ROWS BETWEEN 5 PRECEDING AND 1 PRECEDING",
                DEFAULT_METRIC_NUMBER_FORMAT));

        Metric m3 = getMdService().createObj(getProject(), new Metric("M3",
                "SELECT RUNSUM( [" + amount.getUri() + "] ) ROWS BETWEEN 5 PRECEDING AND 1 FOLLOWING",
                DEFAULT_METRIC_NUMBER_FORMAT));

        Metric m4 = getMdService().createObj(getProject(), new Metric("M4",
                "SELECT RUNSUM( [" + amount.getUri() + "] ) WITHIN ([" + yearUri + "])"
                        + " ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW",
                DEFAULT_METRIC_NUMBER_FORMAT));

        String reportName = "RollingMetric - Date";
        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(date.getDefaultDisplayForm().getUri(), date.getTitle())),
                asList(new MetricElement(amount),
                        new MetricElement(m1),
                        new MetricElement(m2),
                        new MetricElement(m3),
                        new MetricElement(m4)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
        initReportsPage().openReport(reportName);
        checkReportRenderedWell(reportName);

        reportName = "RollingMetric - Month_Year";
        definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(monthYear.getDefaultDisplayForm().getUri(), monthYear.getTitle())),
                asList(new MetricElement(amount),
                        new MetricElement(m1),
                        new MetricElement(m2),
                        new MetricElement(m3),
                        new MetricElement(m4)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
        initReportsPage().openReport(reportName);
        checkReportRenderedWell(reportName);

        List<String> firstRow;
        try (CsvListReader reader = new CsvListReader(new FileReader(new File(testParams.getDownloadFolder(),
                reportPage.exportReport(ExportFormat.CSV) + ".csv")), CsvPreference.STANDARD_PREFERENCE)) {
            reader.getHeader(true);
            firstRow = reader.read();
        }
        assertNull(firstRow.get(3));

        DashboardsRestUtils.changeMetricExpression(getRestApiClient(), m1.getUri(),
                "SELECT RUNSUM( [" + amount.getUri() + "] ) ROWS BETWEEN 5.5 PRECEDING AND CURRENT ROW");
        try {
            initReportsPage().openReport(reportName);
            takeScreenshot(browser, "checkReportRenderedWell - report not computable", getClass());
            assertThat(reportPage.getInvalidDataReportMessage(), equalTo(REPORT_NOT_COMPUTABLE_MESSAGE));
        } finally {
            DashboardsRestUtils.changeMetricExpression(getRestApiClient(), m1.getUri(),
                    "SELECT RUNSUM( [" + amount.getUri() + "] ) ROWS BETWEEN 5 PRECEDING AND CURRENT ROW");
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric"})
    public void createTimeMacrosMetrics() {
        Attribute product = getMdService().getObj(getProject(), Attribute.class, Restriction.title(ATTR_PRODUCT));

        for (MetricTypes metric : asList(MetricTypes.THIS, MetricTypes.PREVIOUS, MetricTypes.NEXT)) {
            checkMetricValuesInReport(createMetricByGoodDataClient(metric), product,
                    getMetricValues(metric), PRODUCT_VALUES);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"non-UI-metric"})
    public void createMetricsWithParentFilterExcept() {
        List<Float> withPFExceptValues = getMetricValues(MetricTypes.WITH_PF_EXCEPT);
        List<Float> withoutPFExceptValues = getMetricValues(MetricTypes.WITHOUT_PF_EXCEPT);
        List<String> attributeValues = asList("2010", "2011", "2012");
        Attribute year = getMdService().getObj(getProject(), Attribute.class, Restriction.title(ATTR_YEAR_SNAPSHOT));

        checkMetricValuesInReport(createMetricByGoodDataClient(MetricTypes.WITH_PF_EXCEPT),
                year, withPFExceptValues, attributeValues);
        addFilterAndCheckReport(attributeValues, withPFExceptValues,
                ATTR_PRODUCT, "CompuSci", "Educationly", "Explorer");
        addFilterAndCheckReport(attributeValues, withPFExceptValues, 
                ATTR_STAGE_NAME, "Interest", "Discovery", "Short List");
        addFilterAndCheckReport(attributeValues, asList(1.7967886E7f, 6.2103956E7f, 8.0406328E7f),
                ATTR_DEPARTMENT, "Direct Sales");

        checkMetricValuesInReport(createMetricByGoodDataClient(MetricTypes.WITHOUT_PF_EXCEPT),
                year, withoutPFExceptValues, attributeValues);
        addFilterAndCheckReport(attributeValues, withoutPFExceptValues, ATTR_DEPARTMENT, "Direct Sales");
        addFilterAndCheckReport(attributeValues, asList(1.3019884E7f, 4.7406964E7f, 6.5819096E7f),
                ATTR_PRODUCT, "CompuSci", "Explorer");
        addFilterAndCheckReport(attributeValues, asList(883466.2f, 1.6477721E7f, 1.7635596E7f),
                ATTR_STAGE_NAME, "Interest", "Discovery");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void checkGreyedOutAndNonGreyedOutAttributes() {
        CustomMetricUI customMetricInfo = new CustomMetricUI().withName("SUM-Amount")
                .withFacts(FACT_AMOUNT);
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.SUM, AGGREGATION);

        initReportCreation();
        reportPage.initPage()
            .openWhatPanel()
            .selectMetric(customMetricInfo.getName())
            .openHowPanel();

        asList("Activity Type", "Date (Activity)", "Date (Timeline)", ATTR_STAGE_HISTORY).stream()
            .forEach(attribute -> {
                assertTrue(reportPage.searchAttribute(attribute)
                    .isGreyedOutAttribute(attribute),
                    format("Attribue %s is not unreachable", attribute));
            });

        asList("Account", "Date (Created)", "Opp. Snapshot", "Opportunity", ATTR_DEPARTMENT, ATTR_PRODUCT, ATTR_STAGE_NAME,
                ATTR_YEAR_CLOSE, ATTR_MONTH_YEAR_SNAPSHOT).stream()
                .forEach(attribute -> {
                    assertFalse(reportPage.searchAttribute(attribute)
                            .isGreyedOutAttribute(attribute),
                            format("Attribue %s is unreachable", attribute));
                });

        reportPage.doneSndPanel();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"aggregation-metric"})
    public void checkDrillDownDefine() {
        initAttributePage().initAttribute(ATTR_STAGE_NAME)
            .setDrillToAttribute(ATTR_DEPARTMENT);

        String metricName = "RUNSUM-Amount";
        CustomMetricUI customMetricInfo = new CustomMetricUI().withName(metricName).withFacts(FACT_AMOUNT);
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.RUNSUM, AGGREGATION);

        UiReportDefinition reportDefinition = new UiReportDefinition().withName("Report-" + metricName)
                .withWhats(metricName).withHows(ATTR_STAGE_NAME);
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
                .withMetrics(METRIC_NUMBER_OF_OPEN_OPPS)
                .withAttributes(ATTR_YEAR_SNAPSHOT)
                .withAttributeValues(
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2012),
                        buildAttributeValue(ATTR_YEAR_SNAPSHOT, YEAR_2010));
        initMetricPage();
        createCustomMetric(customMetricInfo, MetricTypes.BETWEEN, FILTER);

        UiReportDefinition reportDefinition = new UiReportDefinition()
                .withName("Report-" + customMetricInfo.getName()).withWhats(customMetricInfo.getName());
        createReport(reportDefinition, "screenshot-" + "report_" + customMetricInfo.getName());
        waitForFragmentVisible(reportPage);
        checkRedBar(browser);
        assertEquals(reportPage.getDataReportHelpMessage(), NO_DATA_MATCH_REPORT_MESSAGE,
                "Report help message is incorrect!");
        reportPage.saveReport();
    }

    private ByteArrayOutputStream exportReport(ReportDefinition rd) {
        ReportService reportService = goodDataClient.getReportService();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        reportService.exportReport(rd, ReportExportFormat.CSV, output).get();
        return output;
    }

    private void checkMetricValuesInReport(Metric metric, Attribute attribute, List<Float> metricValues,
            List<String> attributeValues) {
        String reportName = "report_" + metric.getTitle();
        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(attribute.getDefaultDisplayForm().getUri(), attribute.getTitle())),
                singletonList(new MetricElement(metric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initReportsPage().openReport(reportName);
        List<Float> metricValuesinGrid = reportPage.getTableReport().getMetricElements();
        takeScreenshot(browser, "check-metric" + "-" + metric.getTitle(), this.getClass());
        System.out.println("Actual metric values:   " + metricValuesinGrid);
        System.out.println("Expected metric values: " + metricValues);
        assertEquals(metricValuesinGrid, metricValues, "Metric values list is incorrrect");
        if (attributeValues != null) {
            List<String> attributeValuesinGrid = reportPage.getTableReport().getAttributeElements();
            assertEquals(attributeValuesinGrid, attributeValues);
        }
    }

    private void checkMetricValuesInReport(String metricName, String attributeName, List<Float> metricValues,
            List<String> attributeValues) {
        System.out.println("Verifying metric values of [" + metricName + "] in report");

        UiReportDefinition reportDefinition = new UiReportDefinition();
        if (attributeName != null) {
            reportDefinition.withHows(attributeName);
        }
        reportDefinition.withWhats(metricName);
        if (metricName.contains("IFNULL")) {
            reportDefinition.withWhats(METRIC_AMOUNT);
        }
        reportDefinition.withName("report_" + metricName);

        createReport(reportDefinition, "screenshot-" + "report_" + metricName);

        if (metricName.contains("WITHOUT_PF")) {
            reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_MONTH_YEAR_SNAPSHOT, "Apr 2012"))
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

    private Metric createMetricByGoodDataClient(MetricTypes metricType) {
        String amountUri = "[" + getMdService().getObjUri(getProject(), Metric.class, Restriction.title(METRIC_AMOUNT)) + "]";
        String dateClosedUri =
                "[" + getMdService().getObjUri(getProject(), Attribute.class, Restriction.title("Date (Closed)")) + "]";
        String dateSnapshotUri = 
                "[" + getMdService().getObjUri(getProject(), Attribute.class, Restriction.title("Date (Created)"))  + "]";
        String dateCreatedUri = 
                "[" + getMdService().getObjUri(getProject(), Attribute.class, Restriction.title("Date (Snapshot)")) + "]";
        String stageNameUri =
                "[" + getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(ATTR_STAGE_NAME)) + "]";
        String productUri = "[" + getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(ATTR_PRODUCT)) + "]";

        String metric = "__metric__";
        String attribute = "__attr__";
        String maql = metricType.getMaql().replaceFirst(metric, amountUri);

        switch (metricType) {
            case THIS:
                maql = maql.replaceFirst(attribute, dateClosedUri);
                break;
            case PREVIOUS:
                maql = maql.replaceFirst(attribute, dateSnapshotUri);
                break;
            case NEXT:
                maql = maql.replaceFirst(attribute, dateCreatedUri);
                break;
            case WITH_PF_EXCEPT:
            case WITHOUT_PF_EXCEPT:
                maql = maql.replaceFirst(attribute, productUri).replaceFirst(attribute, stageNameUri);
                break;
            default:
                break;
        }

        return getMdService().createObj(getProject(), new Metric(metricType.getLabel(),
                maql, DEFAULT_METRIC_NUMBER_FORMAT));
    }

    private void addFilterAndCheckReport(Collection<String> attributeValues, Collection<Float> metricValues,
            String filter, String... filterValues) {
        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(filter, filterValues))
                .saveReport();
        TableReport report = reportPage.getTableReport();
        assertTrue(CollectionUtils.isEqualCollection(report.getMetricElements(), metricValues),
                "Metric values list is incorrrect");
        assertTrue(CollectionUtils.isEqualCollection(report.getAttributeElements(), attributeValues),
                "Attribute values list is incorrrect");
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
            MetricPage.getInstance(browser).createAggregationMetric(metric, customMetricInfo);
        } else if (NUMERIC.equals(type)) {
            MetricPage.getInstance(browser).createNumericMetric(metric, customMetricInfo);
        } else if (GRANULARITY.equals(type)) {
            MetricPage.getInstance(browser).createGranularityMetric(metric, customMetricInfo);
        } else if (LOGICAL.equals(type)) {
            MetricPage.getInstance(browser).createLogicalMetric(metric, customMetricInfo);
        } else {
            MetricPage.getInstance(browser).createFilterMetric(metric, customMetricInfo);
        }

        assertTrue(MetricPage.getInstance(browser)
                .isMetricCreatedSuccessfully(customMetricInfo.getName(), expectedMaql, DEFAULT_METRIC_NUMBER_FORMAT));
    }

    private Metric createLikeMetric(MetricTypes metricLikeType, Attribute attrObj, Metric metricObj , String pattern){
        String metricMaql = metricLikeType.getMaql()
                .replace("__metric__", format("[%s]", metricObj.getUri()))
                .replace("__attr__", format("[%s]", attrObj.getDisplayForms().iterator().next().getUri()))
                .replace("__like.pattern__", pattern);
        log.info(metricMaql);
        String likeMetricName = metricLikeType.getLabel() + System.currentTimeMillis(); 
        return getMdService().createObj(getProject(), new Metric(likeMetricName, metricMaql, DEFAULT_METRIC_NUMBER_FORMAT));
    }

    private List<Metric> createGreatestAndLeastMetric(String attribute, List<String> metrics) {
        Attribute attrObj = getMdService().getObj(getProject(), Attribute.class, Restriction.title(attribute));

        String greatestMaql = MetricTypes.GREATEST.getMaql()
                .replace("__attr__", format("[%s]", attrObj.getUri()));
        String leastMaql = MetricTypes.LEAST.getMaql()
                .replace("__attr__", format("[%s]", attrObj.getUri()));

        for (String metric : metrics) {
            String uri = getMdService().getObjUri(getProject(), Metric.class, Restriction.title(metric));
            greatestMaql = greatestMaql.replaceFirst("__metric__", format("[%s]", uri));
            leastMaql = leastMaql.replaceFirst("__metric__", format("[%s]", uri));
        };

        String greatestMetric = MetricTypes.GREATEST.getLabel() + System.currentTimeMillis();
        String leastMetric = MetricTypes.LEAST.getLabel() + System.currentTimeMillis();

        return asList(
            getMdService().createObj(getProject(), new Metric(greatestMetric, greatestMaql, DEFAULT_METRIC_NUMBER_FORMAT)),
            getMdService().createObj(getProject(), new Metric(leastMetric, leastMaql, DEFAULT_METRIC_NUMBER_FORMAT)));
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
                return asList(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f);
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
            case THIS:
                return asList(2.7219256E7f, 2.2946896E7f, 3.8596196E7f, 8042032.0f, 9525858.0f, 1.0291577E7f);
            case PREVIOUS:
                return asList(2.72229E7f, 2.2946896E7f, 3.8596196E7f, 8042032.0f, 9525858.0f, 1.0291577E7f);
            case NEXT:
                return asList(2.72229E7f, 2.2946896E7f, 3.8596196E7f, 8042032.0f, 9525858.0f, 1.0291577E7f);
            case WITH_PF_EXCEPT:
                return asList(2.6922362E7f, 8.6178608E7f, 1.16625456E8f);
            case WITHOUT_PF_EXCEPT:
                return asList(2.6922362E7f, 8.6178608E7f, 1.16625456E8f);
            default:
                return Collections.emptyList();
        }
    }

    private List<List<String>> getResultInGreatestLeastBasic() {
        List<List<String>> results = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            results.add(asList("40334", "41054", "41048", "41054", "40334"));
        }
        return results;
    }

    private void checkReportRenderedWell(String reportName) {
        takeScreenshot(browser, "checkReportRenderedWell - " + reportName, getClass());
        checkRedBar(browser);
        assertFalse(waitForFragmentVisible(reportPage).isReportTooLarge());
        assertFalse(reportPage.isInvalidDataReportMessageVisible());
    }
}
