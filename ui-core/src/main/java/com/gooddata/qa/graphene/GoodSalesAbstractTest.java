package com.gooddata.qa.graphene;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;

import java.util.Arrays;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    @Override
    protected void initProperties() {
        projectTitle = "GoodSales ";
        appliedFixture = GOODSALES;
    }

    //------------------------- REPORT MD OBJECTS - BEGIN  ------------------------
    protected String createTop5OpenByCashReport() {
        Metric pipelineMetric = createPercentOfPipelineMetric(createBestCaseMetric());
        Metric top5Metric = createTop5Metric(getMetricByTitle(METRIC_BEST_CASE));

        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_5_OPEN_BY_CASH,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                        Arrays.asList(
                                new MetricElement(top5Metric),
                                new MetricElement(pipelineMetric))));
    }

    protected String createTop5WonByCashReport() {
        Metric pipelineMetric = createPercentOfPipelineMetric(createWonMetric());
        Metric top5Metric = createTop5Metric(getMetricByTitle(METRIC_WON));
        Metric numberOfOppsMetric = createNumberOfOppsInPeriodMetric(
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

    protected String createTop5LostByCashReport() {
        Metric pipelineMetric = createPercentOfPipelineMetric(createLostMetric());
        Metric top5Metric = createTop5Metric(getMetricByTitle(METRIC_LOST));
        Metric numberOfOppsMetric = createNumberOfOppsInPeriodMetric(
                METRIC_NUMBER_OF_OPPS_LOST_IN_PERIOD, getMetricByTitle(METRIC_LOST));

        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_5_LOST_BY_CASH,
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

    protected String createTopSalesRepsByWonAndLostReport() {
        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_SALES_REPS_BY_WON_AND_LOST,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_SALES_REP))),
                        Arrays.asList(
                                new MetricElement(createWonMetric()),
                                new MetricElement(createLostMetric()))));
    }

    protected String createAmountByProductReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_PRODUCT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
                singletonList(new MetricElement(createAmountMetric()))));
    }

    protected String createAmountByDateClosedReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_DATE_CLOSED,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DATE_CLOSE))),
                singletonList(new MetricElement(createAmountMetric()))));
    }

    protected String createActivitiesByTypeReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITIES_BY_TYPE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE))),
                singletonList(new MetricElement(createNumberOfActivitiesMetric()))));
    }

    protected String createActiveLevelReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITY_LEVEL,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_DATE_ACTIVITY)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE))),
                singletonList(new MetricElement(createNumberOfActivitiesMetric()))));
    }

    protected String createSalesSeasonalityReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_SALES_SEASONALITY,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_MONTH_SNAPSHOT))),
                Arrays.asList(
                        new MetricElement(createNumberOfWonOppsMetric()),
                        new MetricElement(createAvgWonMetric()),
                        new MetricElement(createLostMetric()),
                        new MetricElement(createWonMetric()))));
    }

    protected String createEmptyReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NO_DATA,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DEPARTMENT))),
                singletonList(new MetricElement(createAmountMetric())),
                singletonList(new Filter(format("(SELECT [%s]) < 0", getMetricByTitle(METRIC_AMOUNT).getUri())))));
    }

    protected String createIncomputableReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_INCOMPUTABLE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY))),
                singletonList(new MetricElement(createAmountMetric()))));
    }

    protected String createTooLargeReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_TOO_LARGE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY))),
                singletonList(new MetricElement(createNumberOfActivitiesMetric()))));
    }
    protected String createNewLostDrillInReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NEW_LOST_DRILL_IN,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(createLostMetric()))));
    }

    protected String createNewWonDrillInReport() {
        return createReport(GridReportDefinitionContent.create(REPORT_NEW_WON_DRILL_IN,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(createWonMetric()))));
    }
    //------------------------- REPORT MD OBJECTS - END  ------------------------

    //------------------------- VARIABLE MD OBJECTS - BEGIN  ------------------------
    protected String createQuoteVariable() {
        return createNumericVarIfNotExit(getRestApiClient(), testParams.getProjectId(), VARIABLE_QUOTA, "3300000");
    }

    protected String createStatusVariable() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        String defaultValuesExpression = format("[%s] IN ([%s])", attributeStatus.getUri(),
                getMdService().getAttributeElements(attributeStatus).stream()
                        .filter(element -> "Open".equals(element.getTitle()))
                        .findFirst()
                        .get()
                        .getUri());

        return createFilterVarIfNotExist(getRestApiClient(), testParams.getProjectId(), VARIABLE_STATUS,
                attributeStatus.getUri(), defaultValuesExpression);
    }
    //------------------------- VARIABLE MD OBJECTS - END  ------------------------

    //------------------------- METRIC MD OBJECTS - BEGIN  ------------------------
    protected Metric createNumberOfActivitiesMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_ACTIVITIES,
                format("SELECT COUNT([%s])", getAttributeByTitle(ATTR_ACTIVITY).getUri()), DEFAULT_METRIC_FORMAT);
    }

    protected Metric createAmountMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_AMOUNT,
                format("SELECT SUM([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createAmountBOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_AMOUNT_BOP,
                format("SELECT SUM([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createBestCaseBOPMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        String elementOpenOfStatus = getMdService()
                .getAttributeElements(attributeStatus).stream()
                .filter(element -> "Open".equals(element.getTitle()))
                .findFirst()
                .get()
                .getUri();

        return createMetricIfNotExist(getGoodDataClient(), METRIC_BEST_CASE_BOP,
                format("SELECT [%s] where [%s]= [%s]",
                        createAmountBOPMetric().getUri(),
                        attributeStatus.getUri(),
                        elementOpenOfStatus),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createTimelineEOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_TIMELINE_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createSnapshotEOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_SNAPSHOT_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]<=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createSnapshotEOP1Metric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_SNAPSHOT_EOP1,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s] where [%s]<=[%s] " +
                                "and [%s]>=[%s] and [%s]<[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineEOPMetric().getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineBOPMetric().getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createSnapshotEOP2Metric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_SNAPSHOT_EOP2,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s] where [%s]<=[%s] and " +
                                "[%s]<[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineEOPMetric().getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOP1Metric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createNumberOfLostOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_LOST_OPPS,
                format("SELECT [%s] where [%s]=[%s]",
                        createNumberOfOpportunitiesMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Lost".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createNumberOfOpportunitiesMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_OPPORTUNITIES,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createTimelineBOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_TIMELINE_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createSnapshotBOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_SNAPSHOT_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]>=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineBOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createProbabilityBOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_PROBABILITY_BOP,
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                "#,##0.0%");
    }

    protected Metric createCloseEOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_CLOSE_EOP,
                format("SELECT max([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_OPP_CLOSE_DATE).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.00");
    }

    protected Metric createNumberOfOpportunitiesBOPMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_OPPORTUNITIES_BOP,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createWonMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_WON,
                format("select [%s] where [%s]=[%s]",
                        createAmountMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createPercentOfGoalMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_PERCENT_OF_GOAL,
                format("select [%s]/[%s]",
                        createWonMetric().getUri(),
                        createQuoteVariable()),
                "[=null]--;[<.3][red]#,##0.0%;[>.8][green]#,##0.0%;#,##0.0%");
    }

    protected Metric createQuotaMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_QUOTA,
                format("Select [%s] by all other", createQuoteVariable()), "$#,##0");
    }

    protected Metric createLostMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_LOST,
                format("select [%s] where [%s]=[%s]",
                        createAmountMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Lost".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createProbabilityMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_PROBABILITY,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.0%");
    }

    protected Metric createNumberOfWonOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_WON_OPPS,
                format("SELECT [%s]where [%s]=[%s]",
                        createNumberOfOpportunitiesMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createWinRateMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_WIN_RATE,
                format("select [%s]/ [%s]",
                        createNumberOfWonOppsMetric().getUri(),
                        createNumberOfOpportunitiesMetric().getUri()),
                "#,##0.0%");
    }

    protected Metric createBestCaseMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_BEST_CASE,
                format("select [%s] where [%s]=[%s]",
                        createAmountMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createAvgAmountMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_AVG_AMOUNT,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createProductiveRepsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_PRODUCTIVE_REPS,
                format("SELECT COUNT([%s],[%S]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_SALES_REP).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createStageVelocityMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        return createMetricIfNotExist(getGoodDataClient(), METRIC_STAGE_VELOCITY,
                format("SELECT AVG((SELECT ([%s]/1) BY [%s])) BY [%s] where [%s]=[%s]",
                        getFactByTitle(FACT_VELOCITY).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        attributeIsActive.getUri(),
                        getMdService().getAttributeElements(attributeIsActive).stream()
                                .filter(element -> "true".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                "#,##0.00");
    }

    protected Metric createExpectedPercentOfGoalMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_EXPECTED_PERCENT_OF_GOAL,
                new String("select ((( select Won where Quarter/Year (Closed) = This -1 and Quarter/Year (Snapshot) "
                        + "=Quarter/Year (Closed) ) /(SelectWon by all other whereQuarter/Year (Closed)= This -1 "
                        + "andQuarter/Year (Snapshot)=Quarter/Year (Closed) ))+ (( select Won where "
                        + "Quarter/Year (Closed)= This -2 and Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /"
                        + "(SelectWon by all other whereQuarter/Year (Closed)= This -2 andQuarter/Year (Snapshot)=Quarter/Year (Closed) ))+ "
                        + "(( select Wonwhere Quarter/Year (Closed)= This -3 and Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /"
                        + "(SelectWon by all other whereQuarter/Year (Closed)= This -3 and Quarter/Year (Snapshot)=Quarter/Year (Closed) )))/3")
                        .replaceAll("Quarter\\/Year \\(Closed\\)", "[" + getAttributeByTitle(ATTR_QUARTER_YEAR_CLOSED).getUri() + "]")
                        .replaceAll("Quarter\\/Year \\(Snapshot\\)", "[" + getAttributeByTitle(ATTR_QUARTER_YEAR_SNAPSHOT).getUri() + "]")
                        .replaceAll("Won", "[" + createWonMetric().getUri() + "]"),
                "[=null]--;[color=999999]#,##0.0%");
    }

    protected Metric createStageDurationMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        return createMetricIfNotExist(getGoodDataClient(), METRIC_STAGE_DURATION,
                format("SELECT AVG((SELECT ([%s]/1) BY [%s])) BY [%s] where [%s]=[%s]",
                        getFactByTitle(FACT_DURATION).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        attributeIsActive.getUri(),
                        getMdService().getAttributeElements(attributeIsActive).stream()
                                .filter(element -> "true".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                "#,##0.00");
    }

    protected Metric createNumberOfOpenOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_NUMBER_OF_OPEN_OPPS,
                format("select [%s] where [%s]=[%s]",
                        createNumberOfOpportunitiesMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    protected Metric createOppFirstSnapshotMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_OPP_FIRST_SNAPSHOT,
                format("select min([%s]) by all in all other dimensions except [%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri()),
                "#,##0.00");
    }

    protected Metric createAvgWonMetric() {
        Metric avgBestCase = createMetricIfNotExist(getGoodDataClient(), "Avg. Best Case",
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);

        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_AVG_WON,
                format("SELECT [%s]where [%s]=[%s]",
                        avgBestCase.getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createDaysUntilCloseMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_DAYS_UNTIL_CLOSE,
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_DAYS_TO_CLOSE).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.0 D");
    }

    protected Metric createExpectedMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_EXPECTED,
                format("SELECT SUM([%s]*[%s]) where [%s]=[%s] and [%s]=[%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createExpectedWonMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(getGoodDataClient(), METRIC_EXPECTED_WON,
                format("SELECT SUM([%s]*[%s]) where [%s]=[%s] and [%s] in ([%s],[%s])",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    protected Metric createExpectedWonVsQuotaMetric() {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_EXPECTED_WON_VS_QUOTA,
                format("select [%s]-([%s]+[%s])",
                        createQuoteVariable(),
                        createExpectedMetric().getUri(),
                        createWonMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    private Metric createNumberOfOppsInPeriodMetric(String name, Metric metric) {
        return createMetricIfNotExist(getGoodDataClient(), name,
                new String("SELECT [# of Won Opps.]" +
                        "WHERE (" +
                        "SELECT MAX([Opp. Close (Date)]) BY [Opportunity]" +
                        "WHERE [Opp. Snapshot (Date)] = [_Snapshot [EOP]]) >= [_Timeline [BOP]]" +
                        "AND (SELECT MAX([Opp. Close (Date)]) BY [Opportunity]" +
                        "WHERE [Opp. Snapshot (Date)] = [_Snapshot [EOP]]) <= [_Timeline [EOP]]")
                        .replaceAll("# of Won Opps\\.", metric.getUri())
                        .replaceAll("Opp\\. Close \\(Date\\)", getFactByTitle(FACT_OPP_CLOSE_DATE).getUri())
                        .replaceAll("Opportunity", getAttributeByTitle(ATTR_OPPORTUNITY).getUri())
                        .replaceAll("Opp\\. Snapshot \\(Date\\)", getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri())
                        .replaceAll("_Snapshot \\[EOP\\]", createSnapshotEOPMetric().getUri())
                        .replaceAll("_Timeline \\[BOP\\]", createTimelineBOPMetric().getUri())
                        .replaceAll("_Timeline \\[EOP\\]", createTimelineEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    private Metric createPercentOfPipelineMetric(Metric metric) {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_PERCENT_OF_PIPLINE,
                format("SELECT (SELECT [%s] WHERE TOP (5) IN (SELECT [%s] BY [%s]) ) /( SELECT [%s]BY ALL OTHER)",
                        metric.getUri(),
                        metric.getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        metric.getUri()),
                "#,##0.0%");
    }

    private Metric createTop5Metric(Metric metric) {
        return createMetricIfNotExist(getGoodDataClient(), METRIC_TOP_5,
                format("SELECT [%s] WHERE TOP (5) IN (SELECT [%s] BY [%s])",
                        metric.getUri(),
                        metric.getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri()),
                "[=null]--;\r\n" +
                        "[>=1000000000]$#,,,.0 B;\r\n" +
                        "[>=1000000]$#,,.0 M;\r\n" +
                        "[>=1000]$#,.0 K;\r\n" +
                        "$#,##0");
    }
    //------------------------- METRIC MD OBJECTS - END  ------------------------
}