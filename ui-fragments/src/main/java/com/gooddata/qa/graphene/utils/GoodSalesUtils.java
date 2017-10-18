package com.gooddata.qa.graphene.utils;

public final class GoodSalesUtils {

    private GoodSalesUtils() {
    }

    //@TODO GOODSALES TEMPLATE, will removed when QA-6396 completes
    public static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";

    // dashboards
    public static final String DASH_PIPELINE_ANALYSIS = "Pipeline Analysis";

    // tabs
    public static final String DASH_TAB_OUTLOOK = "Outlook";
    public static final String DASH_TAB_WHATS_CHANGED = "What's Changed";
    public static final String DASH_TAB_WATERFALL_ANALYSIS = "Waterfall Analysis";
    public static final String DASH_TAB_LEADERBOARDS = "Leaderboards";
    public static final String DASH_TAB_ACTIVITIES = "Activities";
    public static final String DASH_TAB_SALES_VELOCITY = "Sales Velocity";
    public static final String DASH_TAB_QUARTERLY_TRENDS = "Quarterly Trends";
    public static final String DASH_TAB_SEASONALITY = "Seasonality";
    public static final String DASH_TAB_AND_MORE = "...and more";

    // metrics
    public static final String METRIC_NUMBER_OF_ACTIVITIES = "# of Activities";
    public static final String METRIC_NUMBER_OF_LOST_OPPS = "# of Lost Opps.";
    public static final String METRIC_NUMBER_OF_OPEN_OPPS = "# of Open Opps.";
    public static final String METRIC_NUMBER_OF_OPPORTUNITIES = "# of Opportunities";
    public static final String METRIC_NUMBER_OF_OPPORTUNITIES_BOP = "# of Opportunities [BOP]";
    public static final String METRIC_NUMBER_OF_WON_OPPS = "# of Won Opps.";
    public static final String METRIC_NUMBER_OF_WON = "# of Won"; //metric name in report, originally METRIC_NUMBER_OF_WON_OPPS
    public static final String METRIC_SNAPSHOT_BOP = "_Snapshot [BOP]";
    public static final String METRIC_SNAPSHOT_EOP = "_Snapshot [EOP]";
    public static final String METRIC_SNAPSHOT_EOP1 = "_Snapshot [EOP-1]";
    public static final String METRIC_SNAPSHOT_EOP2 = "_Snapshot [EOP-2]";
    public static final String METRIC_TIMELINE_EOP = "_Timeline [EOP]";
    public static final String METRIC_TIMELINE_BOP = "_Timeline [BOP]";
    public static final String METRIC_PERCENT_OF_GOAL = "% of Goal";
    public static final String METRIC_QUOTA = "Quota";
    public static final String METRIC_AMOUNT = "Amount";
    public static final String METRIC_LOST = "Lost";
    public static final String METRIC_PROBABILITY = "Probability";
    public static final String METRIC_WIN_RATE = "Win Rate";
    public static final String METRIC_BEST_CASE = "Best Case";
    public static final String METRIC_WON = "Won";
    public static final String METRIC_AVG_AMOUNT = "Avg. Amount";
    public static final String METRIC_PRODUCTIVE_REPS = "Productive Reps";
    public static final String METRIC_STAGE_VELOCITY = "Stage Velocity";
    public static final String METRIC_EXPECTED_PERCENT_OF_GOAL = "Expected % of Goal";
    public static final String METRIC_STAGE_DURATION = "Stage Duration";
    public static final String METRIC_OPP_FIRST_SNAPSHOT = "_Opp. First Snapshot";
    public static final String METRIC_AVG_WON = "Avg. Won";
    public static final String METRIC_DAYS_UNTIL_CLOSE = "Days until Close";
    public static final String METRIC_EXPECTED = "Expected";
    public static final String METRIC_EXPECTED_WON = "Expected + Won";
    public static final String METRIC_EXPECTED_WON_VS_QUOTA = "Expected + Won vs. Quota";
    public static final String METRIC_AMOUNT_BOP = "Amount [BOP]";

    // attributes
    public static final String ATTR_PRODUCT = "Product";
    public static final String ATTR_STAGE_HISTORY = "Stage History";
    public static final String ATTR_ACTIVITY = "Activity";
    public static final String ATTR_ACTIVITY_TYPE = "Activity Type";
    public static final String ATTR_STAGE_NAME = "Stage Name";
    public static final String ATTR_ACCOUNT = "Account";
    public static final String ATTR_DEPARTMENT = "Department";
    public static final String ATTR_STATUS = "Status";
    public static final String ATTR_REGION = "Region";
    public static final String ATTR_IS_CLOSED = "Is Closed?";
    public static final String ATTR_IS_ACTIVE = "Is Active?";
    public static final String ATTR_IS_TASK = "Is Task?";
    public static final String ATTR_IS_WON = "Is Won?";
    public static final String ATTR_OPPORTUNITY = "Opportunity";
    public static final String ATTR_OPP_SNAPSHOT = "Opp. Snapshot";
    public static final String ATTR_SALES_REP = "Sales Rep";
    public static final String ATTR_PRIORITY = "Priority";
    public static final String ATTR_FORECAST_CATEGORY = "Forecast Category";

    public static final String ATTR_QUARTER_YEAR_CLOSED = "Quarter/Year (Closed)";
    public static final String ATTR_DATE_CLOSE = "Date (Closed)";
    public static final String ATTR_YEAR_CLOSE = "Year (Closed)";

    public static final String ATTR_DATE_SNAPSHOT = "Date (Snapshot)";
    public static final String ATTR_YEAR_SNAPSHOT = "Year (Snapshot)";
    public static final String ATTR_QUARTER_YEAR_SNAPSHOT = "Quarter/Year (Snapshot)";
    public static final String ATTR_MONTH_YEAR_SNAPSHOT = "Month/Year (Snapshot)";

    public static final String ATTR_DATE_TIMELINE =  "Date (Timeline)";

    public static final String ATTR_QUARTER_YEAR_CREATED = "Quarter/Year (Created)";
    public static final String ATTR_MONTH_CREATED = "Month (Created)";
    public static final String ATTR_MONTH_YEAR_CREATED = "Month/Year (Created)";
    public static final String ATTR_DATE_CREATED = "Date (Created)";
    public static final String ATTR_YEAR_CREATED = "Year (Created)";
    public static final String ATTR_MONTH_OF_QUARTER_CREATED = "Month of Quarter (Created)";

    public static final String ATTR_DATE_ACTIVITY = "Date (Activity)";
    public static final String ATTR_MONTH_ACTIVITY = "Month (Activity)";
    public static final String ATTR_MONTH_YEAR_ACTIVITY = "Month/Year (Activity)";
    public static final String ATTR_MONTH_OF_QUARTER_ACTIVITY = "Month of Quarter (Activity)";
    public static final String ATTR_QUARTER_YEAR_ACTIVITY = "Quarter/Year (Activity)";
    public static final String ATTR_YEAR_ACTIVITY = "Year (Activity)";

    // facts
    public static final String FACT_AMOUNT = "Amount";
    public static final String FACT_ACTIVITY_DATE = "Activity (Date)";
    public static final String FACT_TIMELINE_DATE = "Timeline (Date)";
    public static final String FACT_OPP_SNAPSHOT_DATE = "Opp. Snapshot (Date)";
    public static final String FACT_VELOCITY = "Velocity";
    public static final String FACT_DURATION = "Duration";
    public static final String FACT_DAYS_TO_CLOSE = "Days to Close";
    public static final String FACT_PROBABILITY = "Probability";

    // reports
    public static final String REPORT_ACTIVITIES_BY_TYPE = "Activities by Type";
    public static final String REPORT_AMOUNT_BY_PRODUCT = "Sum of all deals by Product";
    public static final String REPORT_TOP_SALES_REPS_BY_WON_AND_LOST = "Top Reps. by Won and Lost";
    public static final String REPORT_AMOUNT_BY_DATE_CLOSED = "Sum of all deals by Date Closed";
    public static final String REPORT_ACTIVITY_LEVEL = "Activiy Level";

    // variables
    public static final String VARIABLE_STATUS = "Status";
    public static final String VARIABLE_QUOTA = "Quota";

    // date dimensions
    public static final String DATE_DIMENSION_CREATED = "Date (Created)";
    public static final String DATE_DIMENSION_CLOSED = "Date (Closed)";
    public static final String DATE_DIMENSION_SNAPSHOT = "Date (Snapshot)";
    public static final String DATE_DIMENSION_ACTIVITY = "Date (Activity)";

    // date dataset
    public static final String DATE_DATASET_CREATED = "Created";
    public static final String DATE_DATASET_CLOSED = "Closed";
    public static final String DATE_DATASET_ACTIVITY = "Activity";
    public static final String DATE_DATASET_SNAPSHOT = "Snapshot";
    public static final String DATE_DATASET_TIMELINE = "Timeline";
}
