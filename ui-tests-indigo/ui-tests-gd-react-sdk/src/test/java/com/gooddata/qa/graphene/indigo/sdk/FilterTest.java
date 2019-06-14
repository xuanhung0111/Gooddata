package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_ATTR_FILTER_INSIGHT_HAS_MEASURE_FILTER_ATTR;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_ATTR_FILTER_INSIGHT_HAS_MEASURE_FILTER_DATE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_COMBINE_MANY_TYPE_FILTER_INSIGHT_HAS_MEASURE_FILTER_MANY_TYPE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_DATE_FILTER_INSIGHT_HAS_MEASURE_FILTER_ATTR;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_DATE_FILTER_INSIGHT_HAS_MEASURE_FILTER_DATE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_INSIGHT_HAS_MEASURE_FILTER_ATTRIBUTE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_INSIGHT_HAS_MEASURE_FILTER_DATE;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

public class FilterTest extends AbstractReactSdkTest {

    private static final int currentYear = LocalDate.now(ZoneId.of("America/Los_Angeles")).getYear();
    private static final String EMAIL = "Email";
    private static final String IN_PERSON_MEETING = "In Person Meeting";
    private static final String PHONE_CALL = "Phone Call";
    private static final String WEB_MEETING = "Web Meeting";
    private static final String ACTIVITY_TYPE = "Activity Type";


    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login")
    public void renderInsightHasMeasureFilterAttribute() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("elementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, EMAIL)),
                Pair.of("anotherElementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING))
        );
        replaceContentAppJSFrom(TEMPLATE_INSIGHT_HAS_MEASURE_FILTER_ATTRIBUTE);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "33,920")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "35,975")));
    }

    @Test(dependsOnMethods = "login")
    public void renderInsightHasMeasureFilterDate() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("from", "2012-01-01"),
                Pair.of("to", "2013-01-01")
        );
        replaceContentAppJSFrom(TEMPLATE_INSIGHT_HAS_MEASURE_FILTER_DATE);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "6,347")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "7,076")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 2),
                asList(asList(ATTR_ACTIVITY_TYPE, PHONE_CALL), asList(METRIC_NUMBER_OF_ACTIVITIES, "11,032")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 3),
                asList(asList(ATTR_ACTIVITY_TYPE, WEB_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "7,312")));
    }

    @Test(dependsOnMethods = "login")
    public void renderAttributeFilterInsightHasMeasureFilterAttribute() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("elementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, EMAIL)),
                Pair.of("anotherElementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING))
        );
        replaceContentAppJSFrom(TEMPLATE_ATTR_FILTER_INSIGHT_HAS_MEASURE_FILTER_ATTR);
        initDashboardsPage(); //clear cache of localhost:3000
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 1);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "35,975")));
    }

    @Test(dependsOnMethods = "login")
    public void renderAttributeFilterInsightHasMeasureFilterDate() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("elementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, EMAIL)),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("from", String.valueOf(2012 - currentYear)),
                Pair.of("to", String.valueOf(2013 - currentYear))
        );
        replaceContentAppJSFrom(TEMPLATE_ATTR_FILTER_INSIGHT_HAS_MEASURE_FILTER_DATE);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 1);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "6,440")));
    }

    @Test(dependsOnMethods = "login")
    public void renderDateFilterInsightHasMeasureFilterAttribute() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("elementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, EMAIL)),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("from", "2012-01-01"),
                Pair.of("to", "2013-01-01")
        );
        replaceContentAppJSFrom(TEMPLATE_DATE_FILTER_INSIGHT_HAS_MEASURE_FILTER_ATTR);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 1);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "6,347")));
    }

    @Test(dependsOnMethods = "login")
    public void renderDateFilterInsightHasMeasureFilterDate() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("fromNow", String.valueOf(2012 - currentYear)),
                Pair.of("toNow", String.valueOf(2014 - currentYear)),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("from", "2012-01-01"),
                Pair.of("to", "2013-01-01")
        );
        replaceContentAppJSFrom(TEMPLATE_DATE_FILTER_INSIGHT_HAS_MEASURE_FILTER_DATE);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 4);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "6,448")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "7,099")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 2),
                asList(asList(ATTR_ACTIVITY_TYPE, PHONE_CALL), asList(METRIC_NUMBER_OF_ACTIVITIES, "11,081")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 3),
                asList(asList(ATTR_ACTIVITY_TYPE, WEB_MEETING), asList(METRIC_NUMBER_OF_ACTIVITIES, "7,340")));
    }

    @Test(dependsOnMethods = "login")
    public void renderCombineManyFilterInsightHasMeasureFilterManyType() throws IOException {
        createCatalogJSON(
                Pair.of("metric", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("displayForm", ACTIVITY_TYPE),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("elementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, EMAIL)),
                Pair.of("fromNow", String.valueOf(2012 - currentYear)),
                Pair.of("toNow", String.valueOf(2013 - currentYear)),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("from", "2011-01-01"),
                Pair.of("to", "2014-01-01"),
                Pair.of("anotherElementOfAttribute", getAttributeElementUri(ATTR_ACTIVITY_TYPE, IN_PERSON_MEETING))
        );
        replaceContentAppJSFrom(TEMPLATE_COMBINE_MANY_TYPE_FILTER_INSIGHT_HAS_MEASURE_FILTER_MANY_TYPE);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();

        ChartReport chartReport = sdkAnalysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 1);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_ACTIVITY_TYPE, EMAIL), asList(METRIC_NUMBER_OF_ACTIVITIES, "21,175")));
    }
}
