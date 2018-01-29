package com.gooddata.qa.fixture.utils.GoodSales;

import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;

import java.util.Arrays;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class Reports extends CommonRestRequest{

    private Metrics metrics = new Metrics(restClient, projectId);

    public Reports(RestClient client, String projectId) {
        super(client, projectId);
    }

    public String createTop5OpenByCashReport() {
        Metric pipelineMetric = metrics.createPercentOfPipelineMetric(
                METRIC_PERCENT_OF_PIPELINE_BEST_CASE, metrics.createBestCaseMetric());
        Metric top5Metric = metrics.createTop5Metric(
                METRIC_TOP_5_OF_BEST_CASE, getMetricByTitle(METRIC_BEST_CASE));

        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_5_OPEN_BY_CASH,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                        Arrays.asList(
                                new MetricElement(top5Metric),
                                new MetricElement(pipelineMetric))));
    }

    public String createTop5WonByCashReport() {
        Metric pipelineMetric = metrics.createPercentOfPipelineMetric(
                METRIC_PERCENT_OF_PIPELINE_WON, metrics.createWonMetric());
        Metric top5Metric = metrics.createTop5Metric(
                METRIC_TOP_5_OF_WON, getMetricByTitle(METRIC_WON));
        Metric numberOfOppsMetric = metrics.createNumberOfOppsInPeriodMetric(
                METRIC_NUMBER_OF_OPPS_WON_IN_PERIOD, getMetricByTitle(METRIC_WON));

        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_5_WON_BY_CASH,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                        Arrays.asList(
                                new MetricElement(top5Metric),
                                new MetricElement(pipelineMetric)),
                        singletonList(new Filter(
                                format("(SELECT [%s] BY [%s], ALL OTHER) >= 0",
                                        numberOfOppsMetric.getUri(),
                                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri())))));
    }

    public String createTop5LostByCashReport() {
        Metric pipelineMetric = metrics.createPercentOfPipelineMetric(
                METRIC_PERCENT_OF_PIPELINE_LOST, metrics.createLostMetric());
        Metric top5Metric = metrics.createTop5Metric(
                METRIC_TOP_5_OF_LOST, getMetricByTitle(METRIC_LOST));
        Metric numberOfOppsMetric = metrics.createNumberOfOppsInPeriodMetric(
                METRIC_NUMBER_OF_OPPS_LOST_IN_PERIOD, getMetricByTitle(METRIC_LOST));

        return createReport(
                GridReportDefinitionContent.create(REPORT_TOP_5_LOST_BY_CASH,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                        Arrays.asList(
                                new MetricElement(top5Metric),
                                new MetricElement(pipelineMetric)),
                        singletonList(new Filter(
                                format("(SELECT [%s] BY [%s], ALL OTHER) >= 0",
                                        numberOfOppsMetric.getUri(),
                                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri())))));
    }

    public String createTopSalesRepsByWonAndLostReport() {
        return createReport(
                GridReportDefinitionContent.create(REPORT_TOP_SALES_REPS_BY_WON_AND_LOST,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_SALES_REP))),
                        Arrays.asList(
                                new MetricElement(metrics.createWonMetric()),
                                new MetricElement(metrics.createLostMetric()))));
    }

    public String createAmountByProductReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_PRODUCT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
                singletonList(new MetricElement(metrics.createAmountMetric()))));
    }

    public String createAmountByDateClosedReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_DATE_CLOSED,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DATE_CLOSE))),
                singletonList(new MetricElement(metrics.createAmountMetric()))));
    }

    public String createActivitiesByTypeReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITIES_BY_TYPE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE))),
                singletonList(new MetricElement(metrics.createNumberOfActivitiesMetric()))));
    }

    public String createActiveLevelReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITY_LEVEL,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_DATE_ACTIVITY)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE))),
                singletonList(new MetricElement(metrics.createNumberOfActivitiesMetric()))));
    }

    public String createSalesSeasonalityReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_SALES_SEASONALITY,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_MONTH_SNAPSHOT))),
                Arrays.asList(
                        new MetricElement(metrics.createNumberOfWonOppsMetric()),
                        new MetricElement(metrics.createAvgWonMetric()),
                        new MetricElement(metrics.createLostMetric()),
                        new MetricElement(metrics.createWonMetric()))));
    }

    public String createEmptyReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NO_DATA,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DEPARTMENT))),
                singletonList(new MetricElement(metrics.createAmountMetric())),
                singletonList(new Filter(format("(SELECT [%s]) < 0", getMetricByTitle(METRIC_AMOUNT).getUri())))));
    }

    public String createIncomputableReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_INCOMPUTABLE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY))),
                singletonList(new MetricElement(metrics.createAmountMetric()))));
    }

    public String createTooLargeReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_TOO_LARGE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY))),
                singletonList(new MetricElement(metrics.createNumberOfActivitiesMetric()))));
    }
    public String createNewLostDrillInReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NEW_LOST_DRILL_IN,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(metrics.createLostMetric()))));
    }

    public String createNewWonDrillInReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NEW_WON_DRILL_IN,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(metrics.createWonMetric()))));
    }

    public String createAmountByStageNameReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_STAGE_NAME,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(metrics.createAmountMetric()))));
    }

    private String createReport(ReportDefinition reportDefinition) {
        ReportDefinition definition = getMdService().createObj(getProject(), reportDefinition);
        return getMdService().createObj(getProject(), new Report(definition.getTitle(), definition)).getUri();
    }
}
