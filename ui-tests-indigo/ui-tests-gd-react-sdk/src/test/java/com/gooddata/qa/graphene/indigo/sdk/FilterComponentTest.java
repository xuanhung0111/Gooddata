package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_ATTRIBUTE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_DATE_AND_ATTRIBUTE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_DATE_FILTER;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.sdk.AttributeFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ExtendedDateFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.stream.Stream;

public class FilterComponentTest extends AbstractReactSdkTest {

    private final static String NO_DATA_MESSAGE = "NO DATA\nNo data for your filter selection.";
    private final static String INSIGHT_TITLE = "Column Chart";
    private String uri;
    private int thisYear = LocalDate.now().getYear();

    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
        uri = createInsightHasAttributeOnStackByAndViewBy(INSIGHT_TITLE, METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
    }

    @Test(dependsOnGroups = "createProject")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login")
    public void filterVisualizationByDate() throws IOException {
        createCatalogJSON(
                Pair.of("visualizationUri", uri),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_DATE_FILTER);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();
        assertEquals(openExtendedDateFilterPanel().getSelectedDateFilter(), DateRange.ALL_TIME);
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getChartReport().getDataLabels(), asList("101,054", "53,217"));

        openExtendedDateFilterPanel().selectStaticPeriod("01/01/2006", "01/01/2010").apply();
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getChartReport().getDataLabels(), asList("1,591", "881"));
        openExtendedDateFilterPanel().selectPeriod(DateRange.LAST_7_DAYS).checkExcludeCurrent().apply();
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getWarning(), NO_DATA_MESSAGE);

        assertThat(openExtendedDateFilterPanel().getDateRangeOptions(),
                equalTo(Stream.of(DateRange.values()).map(DateRange::toString).collect(toList())));

        openExtendedDateFilterPanel()
                .selectFloatingRange(ExtendedDateFilterPanel.DateGranularity.DAYS, "yesterday", "4 days ahead").apply();
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getWarning(), NO_DATA_MESSAGE);
    }

    @Test(dependsOnMethods = "login")
    public void filterVisualizationByAttribute() throws IOException {
        createCatalogJSON(
                Pair.of("visualizationUri", uri),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_ATTRIBUTE_FILTER);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();
        AttributeFilter attributeFilter = AttributeFilter.getInstance(browser);
        attributeFilter.clearAllCheckedValues().selectByNames("Email", "Phone Call");
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getChartReport().getDataLabels(), asList("55,035", "29,665"));

        attributeFilter.selectAllValues();
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getChartReport().getDataLabels(), asList("101,054", "53,217"));
    }

    @Test(dependsOnMethods = "login")
    public void associateDateAndAttributeFilters() throws IOException {
        createCatalogJSON(
                Pair.of("visualizationUri", uri),
                Pair.of("dataSet", ATTR_DATE_ACTIVITY),
                Pair.of("attribute", ATTR_ACTIVITY_TYPE),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_DATE_AND_ATTRIBUTE_FILTER);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();
        openExtendedDateFilterPanel().selectFloatingRange(
                ExtendedDateFilterPanel.DateGranularity.YEARS,
                format("%d years ago", thisYear - 2010),
                format("%d years ago", thisYear - 2006)).apply();
        AttributeFilter attributeFilter = AttributeFilter.getInstance(browser);
        attributeFilter.clearAllCheckedValues().selectByNames("Email", "Phone Call");
        sdkAnalysisPage.waitForVisualizationLoading();
        assertEquals(sdkAnalysisPage.getChartReport().getDataLabels(), asList("18,247", "9,710"));
    }

    private String createInsightHasAttributeOnStackByAndViewBy(String title, String metric, String attribute, String stack) {
        return new IndigoRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId()).createInsight(
            new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                    .setMeasureBucket(
                            singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                    .setCategoryBucket(asList(
                            CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                    CategoryBucket.Type.ATTRIBUTE),
                            CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                    CategoryBucket.Type.STACK))));
    }

    private ExtendedDateFilterPanel openExtendedDateFilterPanel() {
        DateFilter dateFilter = Graphene.createPageFragment(DateFilter.class,
                waitForElementVisible(By.className("dash-filters-date"), browser));
        if (!isElementPresent(className("is-active"), waitForFragmentVisible(dateFilter).getRoot())) {
            dateFilter.getRoot().click();
        }
        return ExtendedDateFilterPanel.getInstance(browser);
    }
}
