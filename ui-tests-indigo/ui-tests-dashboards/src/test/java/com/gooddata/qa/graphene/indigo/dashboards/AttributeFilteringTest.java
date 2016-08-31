package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascase;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class AttributeFilteringTest extends GoodSalesAbstractDashboardTest {

    private static final String STAT_REGION = "stat_region";

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void setupAttributeFiltersFeatureFlag() throws JSONException {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ATTRIBUTE_FILTERS, true);
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterDefaultState() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().waitForAttributeFilters();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter(STAT_REGION).getSelection(), "All");
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelection(), "All");
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterChangeValue() {
        String attributeFilterWestCoast = "West Coast";
        String attributeFilterSourceConsulting = "1 Source Consulting";
        String attributeFilterAgileThought = "AgileThought";
        String attributeFilterVideo = "1st in Video - Music World";
        String attributeFilterShoppingCart = "3dCart Shopping Cart Software";

        AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().waitForAttributeFilters();

        attributeFiltersPanel.getAttributeFilter(STAT_REGION)
            .clearAllCheckedValues()
            .selectByNames(attributeFilterWestCoast);

        attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT)
            .clearAllCheckedValues()
            .selectByNames(attributeFilterSourceConsulting, attributeFilterAgileThought, attributeFilterVideo,
                    attributeFilterShoppingCart);

        takeScreenshot(browser, "checkAttributeFilterDefaultState-West_Coast", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter(STAT_REGION).getSelection(),
                attributeFilterWestCoast);
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), join(", ",
                attributeFilterSourceConsulting, attributeFilterVideo, attributeFilterShoppingCart,
                attributeFilterAgileThought));
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItemsCount(), "(4)");
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop"})
    public void testFilterBySuggestedAttributes() throws JSONException, IOException {
        String attribute14West = "14 West";
        String attribute123Exteriors = "123 Exteriors";

        Metric accountFilterMetric = createAccountFilterMetric();

        String kpiUri = addWidgetToWorkingDashboard(
                createKpiUsingRest(createDefaultKpiConfiguration(accountFilterMetric, DATE_CREATED)));

        try {
            initIndigoDashboardsPageWithWidgets().waitForDateFilter()
                .selectByName(DATE_FILTER_ALL_TIME);
            AttributeFiltersPanel attributeFiltersPanel = indigoDashboardsPage.waitForWidgetsLoading()
                .waitForAttributeFilters();

            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-all", getClass());
            assertThat(
                    indigoDashboardsPage.getWidgetByHeadline(Kpi.class, accountFilterMetric.getTitle()).getValue(),
                    equalTo("12,318,347"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), equalTo("All"));

            attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames(attribute14West, attribute123Exteriors);

            indigoDashboardsPage.waitForWidgetsLoading();
            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-westAndExteriors", getClass());
            assertThat(
                    indigoDashboardsPage.getWidgetByHeadline(Kpi.class, accountFilterMetric.getTitle()).getValue(),
                    equalTo("9,258,347"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(),
                    equalTo(join(", ", attribute123Exteriors, attribute14West)));

            attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames(attribute14West);

            indigoDashboardsPage.waitForWidgetsLoading();
            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-west", getClass());
            assertThat(
                    indigoDashboardsPage.getWidgetByHeadline(Kpi.class, accountFilterMetric.getTitle()).getValue(),
                    equalTo("8,264,747"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(),
                    equalTo(attribute14West));

            initIndigoDashboardsPageWithWidgets();

            takeScreenshot(browser, "testFilterBySuggestedAttributes-refresh", getClass());
            assertThat(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), equalTo("All"));
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    private Metric createAccountFilterMetric() {
        String element14WestId = "/elements?id=961042";
        String elementExteriorsId = "/elements?id=961040";
        String elementFinancialId = "/elements?id=958077";

        String accountFilterMetricName = "Account filter metric";
        String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_ACCOUNT));
        String expression = format("SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])",
                amountUri, accountUri, accountUri + elementExteriorsId,
                accountUri + element14WestId, accountUri + elementFinancialId);

        return getMdService().createObj(getProject(), new Metric(accountFilterMetricName, expression, "#,##0"));
    }
}
