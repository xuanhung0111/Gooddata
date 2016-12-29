package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;

public class GoodSalesDefaultFilterMiscTest extends AbstractDashboardWidgetTest {

    private static final String TAB = "new-tab";

    private static final String DF_VARIABLE = "DF-Variable";
    private static final String REPORT_WITH_PROMPT_FILTER = "Report-with-prompt-filter";

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String DIRECT_SALES = "Direct Sales";

    @Test(dependsOnGroups = {"createProject"})
    public void initData() throws JSONException, IOException {
        initVariablePage().createVariable(new AttributeVariable(DF_VARIABLE)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT)));

        Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));
        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), DF_VARIABLE);

        createReportViaRest(GridReportDefinitionContent.create(REPORT_WITH_PROMPT_FILTER,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric)),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));
    }

    @Test(dependsOnMethods = {"initData"})
    public void switchBetweenDefaultFilterMultipleAndSingleOption() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(INTEREST, DISCOVERY);
        dashboardsPage.saveDashboard();

        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), String.join(", ", INTEREST, DISCOVERY));
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY));

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
        dashboardsPage.saveDashboard();

        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues();
        dashboardsPage.saveDashboard();

        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), String.join(", ", INTEREST, DISCOVERY));
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY));
    }

    @DataProvider(name = "filterCombinationProvider")
    public Object[][] getFilterCombinationProvider() {
        return new Object[][] {
            {FilterCombination.USING_GROUP},
            {FilterCombination.NOT_USING_GROUP}
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "filterCombinationProvider")
    public void combineSingleAndMultipleFilterWithoutUsingGroup(FilterCombination combinationType) {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        DashboardWidgetDirection.UP.moveElementToRightPlace(getFilter(DF_VARIABLE).getRoot());

        if (combinationType == FilterCombination.USING_GROUP)
            dashboardsPage.groupFiltersOnDashboard(ATTR_STAGE_NAME, DF_VARIABLE);

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(INTEREST, DISCOVERY);
        getFilter(DF_VARIABLE)
                .changeSelectionToOneValue()
                .editAttributeFilterValues(DISCOVERY);

        if (combinationType == FilterCombination.USING_GROUP)
            dashboardsPage.applyValuesForGroupFilter();

        dashboardsPage.saveDashboard();
        getReport(REPORT_WITH_PROMPT_FILTER).waitForReportLoading();

        takeScreenshot(browser,
                "Combination-of-single-and-multiple-filter-works-correctly-for-type-" + combinationType, getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), String.join(", ", INTEREST, DISCOVERY));
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkFilterGroupValueWhenSwitchingTabs() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_DEPARTMENT)
                .addNewTab(TAB)
                .openTab(0);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);

        dashboardsPage
                .applyValuesForGroupFilter()
                .openTab(1)
                .openTab(0);

        takeScreenshot(browser, "Filter-group-values-are-kept-when-switching-tabs", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkFilterGroupConnectedBetweenSameTabs() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_DEPARTMENT)
                .addNewTab(TAB)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();

        dashboardsPage.openTab(0);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.applyValuesForGroupFilter().openTab(1);

        takeScreenshot(browser, "Filter-group-connected-between-same-tabs", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkFilterGroupConnectedBetweenDuplicatedTabs() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        dashboardsPage.duplicateDashboardTab(0);

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.applyValuesForGroupFilter().openTab(0);

        takeScreenshot(browser, "Filter-group-connected-between-duplicated-tabs", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkFilterGroupValueAfterCopyTab() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_DEPARTMENT);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_DEPARTMENT).getRoot());

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT)
                .changeSelectionToOneValue()
                .editAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.applyValuesForGroupFilter().copyDashboardTab(0, DASH_PIPELINE_ANALYSIS);

        takeScreenshot(browser, "Filter-group-value-kept-after-copy-tab", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    @Test(dependsOnMethods = {"initData"})
    public void setInitialValueForConnectedFilters() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();

        dashboardsPage
                .addNewTab(TAB)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT)
                .changeSelectionToOneValue()
                .editAttributeFilterValues(DIRECT_SALES);

        dashboardsPage.saveDashboard().openTab(0);
        takeScreenshot(browser, "Initial-value-for-connected-filters-applied", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    private enum FilterCombination {
        USING_GROUP, NOT_USING_GROUP
    }
}
