package com.gooddata.qa.fixture.utils.GoodSales;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.lang.String.format;

public class Metrics extends CommonRestRequest{

    public static final String DEFAULT_METRIC_FORMAT = "#,##0";
    public static final String DEFAULT_CURRENCY_METRIC_FORMAT = "$#,##0.00";

    private Variables variables = new Variables(restClient, projectId);

    public Metrics(RestClient client, String projectId) {
        super(client, projectId);
    }

    public Metric createNumberOfActivitiesMetric() {
        return createMetricIfNotExist(METRIC_NUMBER_OF_ACTIVITIES,
                format("SELECT COUNT([%s])", getAttributeByTitle(ATTR_ACTIVITY).getUri()), DEFAULT_METRIC_FORMAT);
    }

    public Metric createAmountMetric() {
        return createMetricIfNotExist(METRIC_AMOUNT,
                format("SELECT SUM([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    public Metric createAmountBOPMetric() {
        return createMetricIfNotExist(METRIC_AMOUNT_BOP,
                format("SELECT SUM([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    public Metric createBestCaseBOPMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        String elementOpenOfStatus = getMdService()
                .getAttributeElements(attributeStatus).stream()
                .filter(element -> "Open".equals(element.getTitle()))
                .findFirst()
                .get()
                .getUri();

        return createMetricIfNotExist(METRIC_BEST_CASE_BOP,
                format("SELECT [%s] where [%s]= [%s]",
                        createAmountBOPMetric().getUri(),
                        attributeStatus.getUri(),
                        elementOpenOfStatus),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    public Metric createTimelineEOPMetric() {
        return createMetricIfNotExist(METRIC_TIMELINE_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createSnapshotEOPMetric() {
        return createMetricIfNotExist(METRIC_SNAPSHOT_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]<=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createSnapshotEOP1Metric() {
        return createMetricIfNotExist(METRIC_SNAPSHOT_EOP1,
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

    public Metric createSnapshotEOP2Metric() {
        return createMetricIfNotExist(METRIC_SNAPSHOT_EOP2,
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

    public Metric createNumberOfLostOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_NUMBER_OF_LOST_OPPS,
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

    public Metric createNumberOfOpportunitiesMetric() {
        return createMetricIfNotExist(METRIC_NUMBER_OF_OPPORTUNITIES,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createTimelineBOPMetric() {
        return createMetricIfNotExist(METRIC_TIMELINE_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createSnapshotBOPMetric() {
        return createMetricIfNotExist(METRIC_SNAPSHOT_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]>=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createTimelineBOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createProbabilityBOPMetric() {
        return createMetricIfNotExist(METRIC_PROBABILITY_BOP,
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                "#,##0.0%");
    }

    public Metric createCloseEOPMetric() {
        return createMetricIfNotExist(METRIC_CLOSE_EOP,
                format("SELECT max([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_OPP_CLOSE_DATE).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.00");
    }

    public Metric createNumberOfOpportunitiesBOPMetric() {
        return createMetricIfNotExist(METRIC_NUMBER_OF_OPPORTUNITIES_BOP,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotBOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createWonMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_WON,
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

    public Metric createPercentOfGoalMetric() {
        return createMetricIfNotExist(METRIC_PERCENT_OF_GOAL,
                format("select [%s]/[%s]",
                        createWonMetric().getUri(),
                        variables.createQuoteVariable()),
                "[=null]--;[<.3][red]#,##0.0%;[>.8][green]#,##0.0%;#,##0.0%");
    }

    public Metric createQuotaMetric() {
        return createMetricIfNotExist(METRIC_QUOTA,
                format("Select [%s] by all other", variables.createQuoteVariable()), "$#,##0");
    }

    public Metric createLostMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_LOST,
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

    public Metric createProbabilityMetric() {
        return createMetricIfNotExist(METRIC_PROBABILITY,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.0%");
    }

    public Metric createNumberOfWonOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_NUMBER_OF_WON_OPPS,
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

    public Metric createWinRateMetric() {
        return createMetricIfNotExist(METRIC_WIN_RATE,
                format("select [%s]/ [%s]",
                        createNumberOfWonOppsMetric().getUri(),
                        createNumberOfOpportunitiesMetric().getUri()),
                "#,##0.0%");
    }

    public Metric createBestCaseMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_BEST_CASE,
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

    public Metric createAvgAmountMetric() {
        return createMetricIfNotExist(METRIC_AVG_AMOUNT,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    public Metric createProductiveRepsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_PRODUCTIVE_REPS,
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

    public Metric createStageVelocityMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        return createMetricIfNotExist(METRIC_STAGE_VELOCITY,
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

    public Metric createExpectedPercentOfGoalMetric() {
        return createMetricIfNotExist(METRIC_EXPECTED_PERCENT_OF_GOAL,
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

    public Metric createStageDurationMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        return createMetricIfNotExist(METRIC_STAGE_DURATION,
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

    public Metric createNumberOfOpenOppsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_NUMBER_OF_OPEN_OPPS,
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

    public Metric createOppFirstSnapshotMetric() {
        return createMetricIfNotExist(METRIC_OPP_FIRST_SNAPSHOT,
                format("select min([%s]) by all in all other dimensions except [%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri()),
                "#,##0.00");
    }

    public Metric createAvgWonMetric() {
        Metric avgBestCase = createMetricIfNotExist("Avg. Best Case",
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);

        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_AVG_WON,
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

    public Metric createDaysUntilCloseMetric() {
        return createMetricIfNotExist(METRIC_DAYS_UNTIL_CLOSE,
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_DAYS_TO_CLOSE).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        createSnapshotEOPMetric().getUri()),
                "#,##0.0 D");
    }

    public Metric createExpectedMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_EXPECTED,
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

    public Metric createExpectedWonMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetricIfNotExist(METRIC_EXPECTED_WON,
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

    public Metric createExpectedWonVsQuotaMetric() {
        return createMetricIfNotExist(METRIC_EXPECTED_WON_VS_QUOTA,
                format("select [%s]-([%s]+[%s])",
                        variables.createQuoteVariable(),
                        createExpectedMetric().getUri(),
                        createWonMetric().getUri()),
                DEFAULT_CURRENCY_METRIC_FORMAT);
    }

    public Metric createNumberOfOppsInPeriodMetric(String name, Metric appliedMetric) {
        return createMetricIfNotExist(name,
                new String("SELECT [# of Won Opps.]" +
                        "WHERE (" +
                        "SELECT MAX([Opp. Close (Date)]) BY [Opportunity]" +
                        "WHERE [Opp. Snapshot (Date)] = [_Snapshot [EOP]]) >= [_Timeline [BOP]]" +
                        "AND (SELECT MAX([Opp. Close (Date)]) BY [Opportunity]" +
                        "WHERE [Opp. Snapshot (Date)] = [_Snapshot [EOP]]) <= [_Timeline [EOP]]")
                        .replaceAll("# of Won Opps\\.", appliedMetric.getUri())
                        .replaceAll("Opp\\. Close \\(Date\\)", getFactByTitle(FACT_OPP_CLOSE_DATE).getUri())
                        .replaceAll("Opportunity", getAttributeByTitle(ATTR_OPPORTUNITY).getUri())
                        .replaceAll("Opp\\. Snapshot \\(Date\\)", getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri())
                        .replaceAll("_Snapshot \\[EOP\\]", createSnapshotEOPMetric().getUri())
                        .replaceAll("_Timeline \\[BOP\\]", createTimelineBOPMetric().getUri())
                        .replaceAll("_Timeline \\[EOP\\]", createTimelineEOPMetric().getUri()),
                DEFAULT_METRIC_FORMAT);
    }

    public Metric createPercentOfPipelineMetric(String name, Metric appliedMetric) {
        return createMetricIfNotExist(name,
                format("SELECT (SELECT [%s] WHERE TOP (5) IN (SELECT [%s] BY [%s]) ) /( SELECT [%s]BY ALL OTHER)",
                        appliedMetric.getUri(),
                        appliedMetric.getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        appliedMetric.getUri()),
                "#,##0.0%");
    }

    public Metric createTop5Metric(String name, Metric appliedMetric) {
        return createMetricIfNotExist(name,
                format("SELECT [%s] WHERE TOP (5) IN (SELECT [%s] BY [%s])",
                        appliedMetric.getUri(),
                        appliedMetric.getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri()),
                "[=null]--;\r\n" +
                        "[>=1000000000]$#,,,.0 B;\r\n" +
                        "[>=1000000]$#,,.0 M;\r\n" +
                        "[>=1000]$#,.0 K;\r\n" +
                        "$#,##0");
    }

    private Metric createMetric(String name, String expression) {
        return createMetric(name, expression, DEFAULT_METRIC_FORMAT);
    }

    private Metric createMetric(String name, String expression, String format) {
        return getMdService().createObj(getProject(), new Metric(name, expression, format));
    }

    private Metric createMetricIfNotExist(String name, String expression, String format) {
        try {
            return getMetricByTitle(name);
        } catch (ObjNotFoundException e) {
            return getMdService().createObj(getProject(), new Metric(name, expression, format));
        }
    }


}
