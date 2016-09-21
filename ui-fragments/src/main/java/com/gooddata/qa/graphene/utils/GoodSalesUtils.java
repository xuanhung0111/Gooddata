package com.gooddata.qa.graphene.utils;

public final class GoodSalesUtils {

    private GoodSalesUtils() {
    }

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
    public static final String METRIC_NUMBER_OF_WON = "# of Won";
    public static final String METRIC_SNAPSHOT_BOP = "_Snapshot [BOP]";
    public static final String METRIC_PERCENT_OF_GOAL = "% of Goal";
    public static final String METRIC_QUOTA = "Quota";
    public static final String METRIC_AMOUNT = "Amount";
    public static final String METRIC_LOST = "Lost";
    public static final String METRIC_VELOCITY = "Velocity";
    public static final String METRIC_DURATION = "Duration";
    public static final String METRIC_PROBABILITY = "Probability";
    public static final String METRIC_DAYS_TO_CLOSE = "Days to Close";
    public static final String METRIC_WIN_RATE = "Win Rate";
    public static final String METRIC_REMAINING_QUOTA = "Remaining Quota";
    public static final String METRIC_BEST_CASE = "Best Case";
    public static final String METRIC_WON = "Won";
    public static final String METRIC_AVG_AMOUNT = "Avg. Amount";
    public static final String METRIC_PRODUCTIVE_REPS = "Productive Reps";
    public static final String METRIC_STAGE_VELOCITY = "Stage Velocity";
    public static final String METRIC_EXPECTED_PERCENT_OF_GOAL = "Expected % of Goal";
    public static final String METRIC_STAGE_DURATION = "Stage Duration";
    public static final String METRIC_OPP_FIRST_SNAPSHOT = "_Opp. First Snapshot";

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
    public static final String ATTR_IS_WON = "Is Won?";
    public static final String ATTR_OPPORTUNITY = "Opportunity";
    public static final String ATTR_OPP_SNAPSHOT = "Opp. Snapshot";
    public static final String ATTR_SNAPSHOT_EOP1 = "_Snapshot [EOP-1]";
    public static final String ATTR_SNAPSHOT_EOP2 = "_Snapshot [EOP-2]";
    public static final String ATTR_SALES_REP = "Sales Rep";
    public static final String ATTR_QUARTER_YEAR_CLOSED = "Quarter/Year (Closed)";
    public static final String ATTR_DATE_DIMENSION_CLOSE = "Date dimension (Closed)";
    public static final String ATTR_YEAR_CLOSE = "Year (Closed)";
    public static final String ATTR_DATE_SNAPSHOT = "Date (Snapshot)";
    public static final String ATTR_YEAR_SNAPSHOT = "Year (Snapshot)";
    public static final String ATTR_DATE_DIMENSION_SNAPSHOT = "Date dimension (Snapshot)";
    public static final String ATTR_QUARTER_YEAR_SNAPSHOT = "Quarter/Year (Snapshot)";
    public static final String ATTR_MONTH_YEAR_SNAPSHOT = "Month/Year (Snapshot)";
    public static final String ATTR_QUARTER_YEAR_CREATED = "Quarter/Year (Created)";
    public static final String ATTR_MONTH_YEAR_CREATED = "Month/Year (Created)";
    public static final String ATTR_DATE_CREATED = "Date (Created)";
    public static final String ATTR_YEAR_CREATED = "Year (Created)";

    // facts
    public static final String FACT_AMOUNT = "Amount";
    public static final String FACT_ACTIVITY_DATE = "Activity (Date)";
    public static final String FACT_DURATION = "Duration";

    // reports
    public static final String REPORT_ACTIVITIES_BY_TYPE = "Activities by Type";

    // variables
    public static final String VARIABLE_STATUS = "Status";

    // date dimensions
    public static final String DATE_DIMENSION_CREATED = "Date dimension (Created)";
    public static final String DATE_DIMENSION_CLOSED = "Date dimension (Closed)";
    public static final String DATE_DIMENSION_SNAPSHOT = "Date dimension (Snapshot)";
    public static final String DATE_DIMENSION_ACTIVITY = "Date dimension (Activity)";
    public static final String DATE_DIMENSION_TIMELINE = "Date dimension (Timeline)";
}
