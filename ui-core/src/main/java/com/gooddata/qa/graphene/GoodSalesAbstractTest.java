package com.gooddata.qa.graphene;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.ObjNotFoundException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.*;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.io.IOException;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    protected Map<String, String[]> expectedGoodSalesDashboardsAndTabs;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "GoodSales ";
        appliedFixture = GOODSALES;

        // going to be removed when https://jira.intgdc.com/browse/QA-6503 is done
        projectTemplate = GOODSALES_TEMPLATE;
        expectedGoodSalesDashboardsAndTabs = new HashMap<>();
        expectedGoodSalesDashboardsAndTabs.put(DASH_PIPELINE_ANALYSIS, new String[]{
                DASH_TAB_OUTLOOK, DASH_TAB_WHATS_CHANGED, DASH_TAB_WATERFALL_ANALYSIS, DASH_TAB_LEADERBOARDS, DASH_TAB_ACTIVITIES, DASH_TAB_SALES_VELOCITY,
                DASH_TAB_QUARTERLY_TRENDS, DASH_TAB_SEASONALITY, DASH_TAB_AND_MORE
        });
    }

    //------------------------- REPORT MD OBJECTS - BEGIN  ------------------------
    protected String createTopSalesRepsByWonAndLostReport() {
        try {
            getMetricByTitle(METRIC_WON);
        } catch (ObjNotFoundException e) {
            createWonMetric();
        }
        try {
            getMetricByTitle(METRIC_LOST);
        } catch (ObjNotFoundException e) {
            createLostMetric();
        }
        return createReport(
                GridReportDefinitionContent.create(
                        REPORT_TOP_SALES_REPS_BY_WON_AND_LOST,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_SALES_REP))),
                        Arrays.asList(
                                new MetricElement(getMetricByTitle(METRIC_WON)),
                                new MetricElement(getMetricByTitle(METRIC_LOST)))));
    }

    protected String createAmountByProductReport() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            createAmountMetric();
        }
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_PRODUCT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
    }

    protected String createAmountByDateClosedReport() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            createAmountMetric();
        }
        return createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_PRODUCT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_DATE_CLOSE))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
    }

    protected String createActivitiesByTypeReport() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES);
        } catch (ObjNotFoundException e) {
            createNumberOfActivitiesMetric();
        }
        return createReport(GridReportDefinitionContent.create(REPORT_ACTIVITIES_BY_TYPE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_ACTIVITY_TYPE))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
    }

    //------------------------- REPORT MD OBJECTS - END  ------------------------

    //------------------------- VARIABLE MD OBJECTS - BEGIN  ------------------------
    protected String createQuoteVariable() {
        return createNumericVariable(getRestApiClient(), testParams.getProjectId(), VARIABLE_QUOTA, "3300000");
    }

    protected String createStatusVariable() {
        return createFilterVariable(getRestApiClient(), testParams.getProjectId(),
                VARIABLE_STATUS, getAttributeByIdentifier("attr.stage.status").getUri());
    }
    //------------------------- VARIABLE MD OBJECTS - END  ------------------------

    //------------------------- METRIC MD OBJECTS - BEGIN  ------------------------
    protected Metric createNumberOfActivitiesMetric() {
        return createMetric(METRIC_NUMBER_OF_ACTIVITIES,
                format("SELECT COUNT([%s])",
                        getAttributeByTitle(ATTR_ACTIVITY).getUri()));
    }

    protected Metric createAmountMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        // SELECT SUM(Amount) where Opp. Snapshot (Date)=_Snapshot [EOP]
        return createMetric(METRIC_AMOUNT,
                format("SELECT SUM([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()));
    }

    protected Metric createTimelineEOPMetric() {
        // SELECT MAX(Timeline (Date)) BY ALL IN ALL OTHER DIMENSIONS EXCEPT Date (Timeline)
        return createMetric(METRIC_TIMELINE_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()));
    }

    protected Metric createSnapshotEOPMetric() {
        try {
            getMetricByTitle(METRIC_TIMELINE_EOP);
        } catch (ObjNotFoundException e) {
            createTimelineEOPMetric();
        }
        // SELECT MAX(Opp. Snapshot (Date)) BY ALL IN ALL OTHER DIMENSIONS EXCEPT
        // Date (Snapshot)where Opp. Snapshot (Date)<=_Timeline [EOP]
        return createMetric(METRIC_SNAPSHOT_EOP,
                format("SELECT MAX([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]<=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_TIMELINE_EOP).getUri()));
    }

    protected Metric createNumberOfLostOppsMetric() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES);
        } catch (ObjNotFoundException e) {
            createNumberOfOpportunitiesMetric();
        }
        // SELECT # of Opportunities where Status=Lost
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetric(METRIC_NUMBER_OF_LOST_OPPS,
                format("SELECT [%s] where [%s]=[%s]",
                        getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Lost".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createNumberOfOpportunitiesMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        // SELECT count(Opportunity,Opp. Snapshot) where Opp. Snapshot (Date)=_Snapshot [EOP]
        return createMetric(METRIC_NUMBER_OF_OPPORTUNITIES,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()));
    }

    protected Metric createTimelineBOPMetric() {
        // SELECT MIN(Timeline (Date)) BY ALL IN ALL OTHER DIMENSIONS EXCEPT Date (Timeline)
        return createMetric(METRIC_TIMELINE_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]",
                        getFactByTitle(FACT_TIMELINE_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_TIMELINE).getUri()));
    }

    protected Metric createSnapshotBOPMetric() {
        try {
            getMetricByTitle(METRIC_TIMELINE_BOP);
        } catch (ObjNotFoundException e) {
            createTimelineBOPMetric();
        }
        // SELECT MIN(Opp. Snapshot (Date)) BY ALL IN ALL OTHER DIMENSIONS EXCEPT Date (Snapshot)
        // where Opp. Snapshot (Date)>=_Timeline [BOP]
        return createMetric(METRIC_SNAPSHOT_BOP,
                format("SELECT MIN([%s]) BY ALL IN ALL OTHER DIMENSIONS EXCEPT [%s]where [%s]>=[%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_TIMELINE_BOP).getUri()));
    }

    protected Metric createNumberOfOpportunitiesBOPMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_BOP);
        } catch (ObjNotFoundException e) {
            createSnapshotBOPMetric();
        }
        // SELECT count(Opportunity,Opp. Snapshot) where Opp. Snapshot (Date)=_Snapshot [BOP]
        return createMetric(METRIC_NUMBER_OF_OPPORTUNITIES_BOP,
                format("SELECT count([%s],[%s]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_BOP).getUri()));
    }

    protected Metric createWonMetric() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            createAmountMetric();
        }
        // select Amount where Status=Won
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetric(METRIC_WON,
                format("select [%s] where [%s]=[%s]",
                        getMetricByTitle(METRIC_AMOUNT).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createPercentOfGoalMetric() {
        try {
            getMetricByTitle(METRIC_WON);
        } catch (ObjNotFoundException e) {
            createWonMetric();
        }
        String variableUri = "";
        try {
            variableUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), VARIABLE_QUOTA);
        } catch (RuntimeException e) {
            variableUri = createQuoteVariable();
        }
        // select Won/Quota
        return createMetric(METRIC_PERCENT_OF_GOAL,
                format("select [%s]/[%s]",
                        getMetricByTitle(METRIC_WON).getUri(),
                        variableUri),
                "[=null]--;[<.3][red]#,##0.0%;[>.8][green]#,##0.0%;#,##0.0%");
    }

    protected Metric createQuotaMetric() {
        String variableUri = "";
        try {
            variableUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), VARIABLE_QUOTA);
        } catch (RuntimeException e) {
            variableUri = createQuoteVariable();
        }
        // Select Quota by all other
        return createMetric(METRIC_QUOTA, format("Select [%s] by all other", variableUri), "$#,##0");
    }

    protected Metric createLostMetric() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            createAmountMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        // select Amount where Status=Lost
        return createMetric(METRIC_LOST,
                format("select [%s] where [%s]=[%s]",
                        getMetricByTitle(METRIC_AMOUNT).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Lost".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createProbabilityMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        // SELECT AVG(Probability) where Opp. Snapshot (Date)=_Snapshot [EOP]
        return createMetric(METRIC_PROBABILITY,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()),
                "#,##0.0%");
    }

    protected Metric createNumberOfWonOppsMetric() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES);
        } catch (ObjNotFoundException e) {
            createNumberOfOpportunitiesMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        // SELECT # of Opportunitieswhere Status=Won
        return createMetric(METRIC_NUMBER_OF_WON_OPPS,
                format("SELECT [%s]where [%s]=[%s]",
                        getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createWinRateMetric() {
        try {
            getMetricByTitle(METRIC_NUMBER_OF_WON_OPPS);
        } catch (ObjNotFoundException e) {
            createNumberOfWonOppsMetric();
        }
        try {
            getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES);
        } catch (ObjNotFoundException e) {
            createNumberOfOpportunitiesMetric();
        }
        // select # of Won Opps./ # of Opportunities
        return createMetric(METRIC_WIN_RATE,
                format("select [%s]/ [%s]",
                        getMetricByTitle(METRIC_NUMBER_OF_WON_OPPS).getUri(),
                        getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri()),
                "#,##0.0%");
    }

    protected Metric createBestCaseMetric() {
        try {
            getMetricByTitle(METRIC_AMOUNT);
        } catch (ObjNotFoundException e) {
            createAmountMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        // select Amount where Status=Open
        return createMetric(METRIC_BEST_CASE,
                format("select [%s] where [%s]=[%s]",
                        getMetricByTitle(METRIC_AMOUNT).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createAvgAmountMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        //SELECT AVG(Amount) where Opp. Snapshot (Date)=_Snapshot [EOP]
        return createMetric(METRIC_AVG_AMOUNT,
                format("SELECT AVG([%s]) where [%s]=[%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()),
                "$#,##0.00");
    }

    protected Metric createProductiveRepsMetric() {
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        // SELECT COUNT(Sales Rep,Opp. Snapshot) where Status=Won
        return createMetric(METRIC_PRODUCTIVE_REPS,
                format("SELECT COUNT([%s],[%S]) where [%s]=[%s]",
                        getAttributeByTitle(ATTR_SALES_REP).getUri(),
                        getAttributeByTitle(ATTR_OPP_SNAPSHOT).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createStageVelocityMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        // SELECT AVG((SELECT (Velocity/1) BY Opportunity)) BY Opportunity where Is Active?=true
        return createMetric(METRIC_STAGE_VELOCITY,
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
        try {
            getMetricByTitle(METRIC_WON);
        } catch (ObjNotFoundException e) {
            createWonMetric();
        }
        // select ((( select Won where Quarter/Year (Closed) = This -1 and
        // Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /(SelectWon by all other where
        // Quarter/Year (Closed)= This -1 and
        // Quarter/Year (Snapshot)=Quarter/Year (Closed) ))+ (( select Won where
        // Quarter/Year (Closed)= This -2 and
        // Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /(SelectWon by all other where
        // Quarter/Year (Closed)= This -2 and
        // Quarter/Year (Snapshot)=Quarter/Year (Closed) ))+ (( select Wonwhere Quarter/Year (Closed)= This -3 and
        // Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /(SelectWon by all other where
        // Quarter/Year (Closed)= This -3 and Quarter/Year (Snapshot)=Quarter/Year (Closed) )))/3
        return createMetric(METRIC_EXPECTED_PERCENT_OF_GOAL,
                new String("select ((( select Won where Quarter/Year (Closed) = This -1 and Quarter/Year (Snapshot) "
                        + "=Quarter/Year (Closed) ) /(SelectWon by all other whereQuarter/Year (Closed)= This -1 "
                        + "andQuarter/Year (Snapshot)=Quarter/Year (Closed) ))+ (( select Won where "
                        + "Quarter/Year (Closed)= This -2 and Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /"
                        + "(SelectWon by all other whereQuarter/Year (Closed)= This -2 andQuarter/Year (Snapshot)=Quarter/Year (Closed) ))+ "
                        + "(( select Wonwhere Quarter/Year (Closed)= This -3 and Quarter/Year (Snapshot) =Quarter/Year (Closed) ) /"
                        + "(SelectWon by all other whereQuarter/Year (Closed)= This -3 and Quarter/Year (Snapshot)=Quarter/Year (Closed) )))/3")
                        .replaceAll("Quarter/Year (Closed)", "[" + getAttributeByTitle(ATTR_QUARTER_YEAR_CLOSED).getUri() + "]")
                        .replaceAll("Quarter/Year (Snapshot)", "[" + getAttributeByTitle(ATTR_QUARTER_YEAR_SNAPSHOT).getUri() + "]")
                        .replaceAll("Won", "[" + getMetricByTitle(METRIC_WON).getUri() + "]"),
                "[=null]--;[color=999999]#,##0.0%");
    }

    protected Metric createStageDurationMetric() {
        Attribute attributeIsActive = getAttributeByTitle(ATTR_IS_ACTIVE);
        // SELECT AVG((SELECT (Duration/1) BY Opportunity)) BY Opportunitywhere Is Active?=true
        return createMetric(METRIC_STAGE_DURATION,
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
        try {
            getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES);
        } catch (ObjNotFoundException e) {
            createNumberOfOpportunitiesMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        // select # of Opportunities where Status=Open
        return createMetric(METRIC_NUMBER_OF_OPEN_OPPS,
                format("select [%s] where [%s]=[%s]",
                        getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()));
    }

    protected Metric createOppFirstSnapshotMetric() {
        //select min(Opp. Snapshot (Date)) by all in all other dimensions except Opportunity
        return createMetric(METRIC_OPP_FIRST_SNAPSHOT,
                format("select min([%s]) by all in all other dimensions except [%s]",
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getAttributeByTitle(ATTR_OPPORTUNITY).getUri()),
                "#,##0.00");
    }

    protected Metric createAvgWonMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        // SELECT AVG(Amount) where Opp. Snapshot (Date)=_Snapshot [EOP]
        createMetric("Avg. Best Case",
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()),
                "$#,##0.00");


        //SELECT Avg. Best Casewhere Status=Won
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        return createMetric(METRIC_AVG_WON,
                format("SELECT [%s]where [%s]=[%s]",
                        getMetricByTitle("Avg. Best Case").getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Won".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                "$#,##0.00");
    }

    protected Metric createDaysUntilCloseMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        // SELECT AVG(Days to Close) where Opp. Snapshot (Date)=_Snapshot [EOP]
        return createMetric(METRIC_DAYS_UNTIL_CLOSE,
                format("SELECT AVG([%s]) where [%s]= [%s]",
                        getFactByTitle(FACT_DAYS_TO_CLOSE).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri()),
                "#,##0.0 D");
    }

    protected Metric createExpectedMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        //SELECT SUM(Amount*Probability) where Opp. Snapshot (Date)=_Snapshot [EOP] and Status=Open
        return createMetric(METRIC_EXPECTED,
                format("SELECT SUM([%s]*[%s]) where [%s]=[%s] and [%s]=[%s]",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri(),
                        attributeStatus.getUri(),
                        getMdService().getAttributeElements(attributeStatus).stream()
                                .filter(element -> "Open".equals(element.getTitle()))
                                .findFirst()
                                .get()
                                .getUri()),
                "$#,##0.00");
    }

    protected Metric createExpectedWonMetric() {
        try {
            getMetricByTitle(METRIC_SNAPSHOT_EOP);
        } catch (ObjNotFoundException e) {
            createSnapshotEOPMetric();
        }
        Attribute attributeStatus = getAttributeByIdentifier("attr.stage.status");
        //SELECT SUM(Amount*Probability) where Opp. Snapshot (Date)=_Snapshot [EOP] and Status in (Open,Won)
        return createMetric(METRIC_EXPECTED_WON,
                format("SELECT SUM([%s]*[%s]) where [%s]=[%s] and [%s] in ([%s],[%s])",
                        getFactByTitle(FACT_AMOUNT).getUri(),
                        getFactByTitle(FACT_PROBABILITY).getUri(),
                        getFactByTitle(FACT_OPP_SNAPSHOT_DATE).getUri(),
                        getMetricByTitle(METRIC_SNAPSHOT_EOP).getUri(),
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
                "$#,##0.00");
    }

    protected Metric createExpectedWonVsQuotaMetric() throws IOException, JSONException {
        try {
            getMetricByTitle(METRIC_EXPECTED);
        } catch (ObjNotFoundException e) {
            createExpectedMetric();
        }
        try {
            getMetricByTitle(METRIC_WON);
        } catch (ObjNotFoundException e) {
            createWonMetric();
        }

        String variableUri = "";
        try {
            variableUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), VARIABLE_QUOTA);
        } catch (RuntimeException e) {
            variableUri = createQuoteVariable();
        }
        //select Quota-(Expected+Won)
        return createMetric(METRIC_EXPECTED_WON_VS_QUOTA,
                format("select [%s]-([%s]+[%s])",
                        variableUri,
                        getMetricByTitle(METRIC_EXPECTED).getUri(),
                        getMetricByTitle(METRIC_WON).getUri()),
                "$#,##0.00");
    }
    //------------------------- METRIC MD OBJECTS - END  ------------------------

}