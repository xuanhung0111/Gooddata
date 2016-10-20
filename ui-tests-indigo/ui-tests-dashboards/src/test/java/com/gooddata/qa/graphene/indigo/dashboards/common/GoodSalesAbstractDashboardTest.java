package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.GOODSALES_TEMPLATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.lang.String.format;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;

import com.gooddata.md.Dataset;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;

public abstract class GoodSalesAbstractDashboardTest extends AbstractDashboardTest {

    protected static final String DATE_CREATED = "Created";
    protected static final String DATE_CLOSED = "Closed";
    protected static final String DATE_ACTIVITY = "Activity";
    protected static final String DATE_SNAPSHOT = "Snapshot";
    protected static final String DATE_TIMELINE = "Timeline";

    @BeforeClass(alwaysRun = true)
    protected void initProperties() {
        projectTemplate = GOODSALES_TEMPLATE;
    }

    protected String getDateDatasetUri(final String dataset) {
        return getMdService().getObjUri(getProject(), Dataset.class, title(format("Date (%s)", dataset)));
    }

    protected String createAmountKpi() throws JSONException, IOException {
        final Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(amountMetric.getTitle())
                .metric(amountMetric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .build());
    }

    protected String createLostKpi() throws JSONException, IOException {
        final Metric lostMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_LOST));

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(lostMetric.getTitle())
                .metric(lostMetric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_CREATED))
                .comparisonType(Kpi.ComparisonType.LAST_YEAR)
                .comparisonDirection(Kpi.ComparisonDirection.BAD)
                .build());
    }

    protected String createNumOfActivitiesKpi() throws JSONException, IOException {
        final Metric numOfActivities =
                getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES));

        return createKpiUsingRest(new KpiMDConfiguration.Builder()
                .title(numOfActivities.getTitle())
                .metric(numOfActivities.getUri())
                .dateDataSet(getDateDatasetUri(DATE_CREATED))
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
    }

    protected KpiMDConfiguration createDefaultKpiConfiguration(final Metric metric, final String dateDataset) {
        return new KpiMDConfiguration.Builder()
                .title(metric.getTitle())
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(dateDataset))
                .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(ComparisonDirection.GOOD)
                .build();
    }
}
