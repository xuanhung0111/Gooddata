package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;

public class AttributeFilteringTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void setupAttributeFiltersFeatureFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ATTRIBUTE_FILTERS, true);
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterDefaultState() {
        final AttributeFiltersPanel attributeFiltersPanel = initIndigoDashboardsPageWithWidgets()
                .waitForAttributeFilters();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter(STAT_REGION).getSelection(), "All");
        assertEquals(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelection(), "All");
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterChangeValue() {
        String attributeFilterWestCoast = "West Coast";
        String attributeFilterSourceConsulting = "1 Source Consulting";
        String attributeFilterAgileThought = "AgileThought";
        String attributeFilterVideo = "1st in Video - Music World";
        String attributeFilterShoppingCart = "3dCart Shopping Cart Software";

        AttributeFiltersPanel attributeFiltersPanel = initIndigoDashboardsPageWithWidgets()
                .waitForAttributeFilters();

        attributeFiltersPanel.getAttributeFilter(STAT_REGION)
            .clearAllCheckedValues()
            .selectByNames(attributeFilterWestCoast);

        attributeFiltersPanel.getAttributeFilter(ACCOUNT)
            .clearAllCheckedValues()
            .selectByNames(attributeFilterSourceConsulting, attributeFilterAgileThought, attributeFilterVideo,
                    attributeFilterShoppingCart);

        takeScreenshot(browser, "checkAttributeFilterDefaultState-West_Coast", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter(STAT_REGION).getSelection(),
                attributeFilterWestCoast);
        assertEquals(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItems(), join(", ",
                attributeFilterSourceConsulting, attributeFilterVideo, attributeFilterShoppingCart,
                attributeFilterAgileThought));
        assertEquals(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItemsCount(), "(4)");
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = "desktop")
    public void testFilterBySuggestedAttributes() {
        String attribute14West = "14 West";
        String attribute123Exteriors = "123 Exteriors";

        String accountFilterMetricName = createAccountFilterMetric();

        setupKpi(new KpiConfiguration.Builder()
            .metric(accountFilterMetricName)
            .dataSet(DATE_CREATED)
            .build()
        );

        try {
            waitForFragmentVisible(indigoDashboardsPage).waitForDateFilter()
                .selectByName(DATE_FILTER_ALL_TIME);
            AttributeFiltersPanel attributeFiltersPanel = indigoDashboardsPage.waitForAllKpiWidgetContentLoaded()
                .waitForAttributeFilters();

            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-all", getClass());
            assertThat(indigoDashboardsPage.getKpiByHeadline(accountFilterMetricName).getValue(),
                    equalTo("12,318,347"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItems(), equalTo("All"));

            attributeFiltersPanel.getAttributeFilter(ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames(attribute14West, attribute123Exteriors);

            indigoDashboardsPage.waitForAllKpiWidgetContentLoaded();
            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-westAndExteriors", getClass());
            assertThat(indigoDashboardsPage.getKpiByHeadline(accountFilterMetricName).getValue(),
                    equalTo("9,258,347"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItems(),
                    equalTo(join(", ", attribute123Exteriors, attribute14West)));

            attributeFiltersPanel.getAttributeFilter(ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames(attribute14West);

            indigoDashboardsPage.waitForAllKpiWidgetContentLoaded();
            takeScreenshot(browser, "testFilterBySuggestedAttributes-account-west", getClass());
            assertThat(indigoDashboardsPage.getKpiByHeadline(accountFilterMetricName).getValue(),
                    equalTo("8,264,747"));
            assertThat(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItems(),
                    equalTo(attribute14West));

            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "testFilterBySuggestedAttributes-refresh", getClass());
            assertThat(attributeFiltersPanel.getAttributeFilter(ACCOUNT).getSelectedItems(), equalTo("All"));
        } finally {
            teardownKpi();
        }
    }

    private String createAccountFilterMetric() {
        String element14WestId = "/elements?id=961042";
        String elementExteriorsId = "/elements?id=961040";
        String elementFinancialId = "/elements?id=958077";

        String accountFilterMetricName = "Account filter metric";
        String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));
        String accountUri = getMdService().getObjUri(getProject(), Attribute.class, title(ACCOUNT));
        String expression = format("SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])",
                amountUri, accountUri, accountUri + elementExteriorsId,
                accountUri + element14WestId, accountUri + elementFinancialId);

        getMdService().createObj(getProject(), new Metric(accountFilterMetricName, expression, "#,##0"));
        return accountFilterMetricName;
    }
}
