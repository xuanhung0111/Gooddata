package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_ABSOLUTE_DATE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_ALIAS_MEASURES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_ATTRIBUTE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_FILTERS;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_MEASURE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_MEASURES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_MEASURE_AND_FORMAT;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_NEGATIVE_FILTERS;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_PERCENT_MEASURE;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_PP_COMPARISON;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_HEADLINE_WITH_SPPY_COMPARISON;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.WARNING_CAN_NOT_DISPLAY;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.Headline;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;

public class HeadlineByBucketComponentTest extends AbstractReactSdkTest {

    private static final String DIRECT_SALES = "Direct Sales";
    private DashboardRestRequest dashboardRestRequest;

    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAvgAmountMetric();
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithMeasures() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_MEASURE);
        Headline headline = initSDKAnalysisPage().getHeadline();
        assertEquals(headline.getPrimaryItem(), "154,271");

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("secondaryMeasureTitle", METRIC_AVG_AMOUNT));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_MEASURES);
        initSDKAnalysisPage();
        assertEquals(headline.getPrimaryItem(), "154,271");
        assertEquals(headline.getSecondItem(), asList("$20,286.22", "Avg. Amount"));
    }
      //ONE-3462
//    @Test(dependsOnMethods = "login")
//    public void createInsightWithMeasuresHasAggregation() throws IOException {
//        createCatalogJSON(Pair.of("primaryMeasureTitle", FACT_VELOCITY),
//                Pair.of("typeAggregation", "SUM"));
//        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_AGGREGATION);
//        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,2711");
//    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithMeasureAndFormat() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("typeFormat", Formatter.DEFAULT.toString()));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_MEASURE_AND_FORMAT);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271.00");

          //RAIL-941 not apply format
//        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
//                Pair.of("typeFormat", "#.##0.00"));
//        replaceContentAppJSFrom("HeadlineWithMeasureAndFormat.js");
//        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271");

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_MEASURE);
        dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), Formatter.DEFAULT.toString());
        try {
            assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271.00");
        } finally {
            dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), "#,##0");
        }
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithAliasMeasures() throws IOException {
        String alias = "alias";
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("secondaryMeasureTitle", METRIC_AVG_AMOUNT),
                Pair.of("aliasMeasureTitle", alias));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_ALIAS_MEASURES);

        Headline headline = initSDKAnalysisPage().getHeadline();
        assertEquals(headline.getPrimaryItem(), "154,271");
        assertEquals(headline.getSecondItem(), asList("$20,286.22", alias));
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithSPPYComparison() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("dateDataSetTitle", DATE_DIMENSION_ACTIVITY),
                Pair.of("popAttributeTitle", ATTR_YEAR_ACTIVITY));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_SPPY_COMPARISON);
        Headline headline = initSDKAnalysisPage().getHeadline();
        assertEquals(headline.getPrimaryItem(), "154,271");
        assertEquals(headline.getSecondItem(), asList("154,271", METRIC_NUMBER_OF_ACTIVITIES + " - SP year ago"));
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithPPComparison() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("dateDataSetTitle", DATE_DIMENSION_ACTIVITY),
                Pair.of("from", "2011-01-01"),
                Pair.of("to", "2012-01-01"));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_PP_COMPARISON);
        Headline headline = initSDKAnalysisPage().getHeadline();
        assertEquals(headline.getPrimaryItem(), "73,239");
        assertEquals(headline.getSecondItem(), asList("46,787", METRIC_NUMBER_OF_ACTIVITIES + " - period ago"));
        assertEquals(headline.getTertiaryItem(), asList("57%", "Versus"));
    }

    @Test(dependsOnMethods = "login")
    public void filterEmbedHeadlineInsight() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES)),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_NEGATIVE_FILTERS);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "53,217");

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES)),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_FILTER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "101,054");

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("dateAttributeName", DATE_DIMENSION_ACTIVITY),
                Pair.of("from", "2010-01-01"),
                Pair.of("to", "2011-01-01"));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_ABSOLUTE_DATE_FILTER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "46,868");

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("dateAttributeName", DATE_DIMENSION_ACTIVITY),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES)),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()),
                Pair.of("from", "2010-01-01"),
                Pair.of("to", "2011-01-01"));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_FILTERS);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "30,934");
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithAttribute() throws IOException {
        createCatalogJSON(Pair.of("firstAttributeTitle", ATTR_DEPARTMENT));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_ATTRIBUTE);
        assertEquals(initSDKAnalysisPage().getWarning(), WARNING_CAN_NOT_DISPLAY);
    }

    @Test(dependsOnMethods = "login")
    public void createInsightWithPercentMeasure() throws IOException {
        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("isPercent", "true"));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_PERCENT_MEASURE);
        assertEquals(initSDKAnalysisPage().getWarning(), WARNING_CAN_NOT_DISPLAY);

        createCatalogJSON(Pair.of("primaryMeasureTitle", METRIC_NUMBER_OF_ACTIVITIES),
                Pair.of("isPercent", "false"));
        replaceContentAppJSFrom(TEMPLATE_HEADLINE_WITH_PERCENT_MEASURE);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271");
    }
}
