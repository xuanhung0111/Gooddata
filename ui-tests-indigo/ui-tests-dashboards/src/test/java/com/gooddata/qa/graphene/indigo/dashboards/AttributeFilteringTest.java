package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.createBlankProject;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;
import com.gooddata.md.*;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeFilteringTest extends GoodSalesAbstractDashboardTest {

    private static String TEST_INSIGHT = "Test-Insight";
    private static final String WITHOUT_DATE_CSV_PATH = "/" + UPLOAD_CSV + "/without.date.csv";
    private static final String WITHOUT_DATE_DATASET = "Without Date";

    @BeforeClass(alwaysRun = true)
    public void setTitle() {
        projectTitle += "Attribute-Filtering-Test";
    }

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        String insightWidget = createInsightWidget(new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.getSimpleInstance(getMdService().getObj(getProject(),
                        Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES))))));

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                asList(createAmountKpi(), insightWidget));
    }



    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void checkDashboardWithNoAttributeFilters() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

        takeScreenshot(browser, "checkDashboardWithNoAttributeFilters", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void testMakingNoAffectOnWidgetsWhenUsingUnconnectedFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addAttributeFilter(ATTR_PRIORITY)
                .getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_PRIORITY)
                .clearAllCheckedValues()
                .selectByNames("NORMAL");


        indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT)
                .getValue(), "$116,625,456.54", "Unconnected filter make impact to kpi");

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        assertEquals(
                indigoDashboardsPage.getWidgetByHeadline(Insight.class, TEST_INSIGHT)
                        .getChartReport()
                        .getDataLabels(),
                singletonList("62,136"), "Unconnected filter make impact to insight");
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void keepFilterValuesAfterSave() throws IOException, JSONException {
        List<String> selectedItems = asList("236349", "236350", "236351");

        addAttributeFilterToDashboard(ATTR_OPP_SNAPSHOT).switchToEditMode().getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_OPP_SNAPSHOT)
                .clearAllCheckedValues()
                .selectByNames(selectedItems.toArray(new String[0]));

        indigoDashboardsPage.saveEditModeWithWidgets();
        takeScreenshot(browser, "keepFilterValuesAfterSave", getClass());

        try {
            assertEquals(
                    indigoDashboardsPage.getAttributeFiltersPanel()
                            .getAttributeFilter(ATTR_OPP_SNAPSHOT)
                            .getSelectedItems(),
                    join(", ", selectedItems), "Selected items of attribute value are not correct after save");
            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                    "$33,622.95", "Kpi value is not correct");
            assertEquals(initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT)
                            .getValue(),
                    "$33,622.95", "Kpi value is not correct after refresh");
        } finally {
            IndigoRestUtils.deleteAttributeFilterIfExist(getRestApiClient(), testParams.getProjectId(),
                    getAttributeDisplayFormUri(ATTR_OPP_SNAPSHOT));
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void revertFilterValuesAfterCancelling() throws IOException, JSONException {
        addAttributeFilterToDashboard(ATTR_OPP_SNAPSHOT);
        try {
            indigoDashboardsPage.switchToEditMode().getAttributeFiltersPanel()
                    .getAttributeFilter(ATTR_OPP_SNAPSHOT)
                    .clearAllCheckedValues()
                    .selectByNames("236352");

            indigoDashboardsPage.cancelEditModeWithChanges().waitForWidgetsLoading();
            takeScreenshot(browser, "revertFilterValuesAfterCancelling", getClass());
            assertEquals(
                    indigoDashboardsPage.getAttributeFiltersPanel()
                            .getAttributeFilter(ATTR_OPP_SNAPSHOT)
                            .getSelectedItems(),
                    "All", "Selected items of attribute filter are not reverted correctly");

            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                    "$116,625,456.54", "Kpi value is not reverted");
            assertEquals(initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT)
                            .getValue(),
                    "$116,625,456.54", "Kpi value is not correct after refresh");
        } finally {
            IndigoRestUtils.deleteAttributeFilterIfExist(getRestApiClient(), testParams.getProjectId(),
                    getAttributeDisplayFormUri(ATTR_OPP_SNAPSHOT));
        }
    }

    @DataProvider
    public Object[][] booleanProvider() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"}, dataProvider = "booleanProvider")
    public void applyMultipleFiltersOnKpi(boolean isSaved) throws IOException, JSONException {
        Collection<String> filters = asList(ATTR_STAGE_NAME, ATTR_PRODUCT, ATTR_PRIORITY);
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        addMultipleFilters(filters);

        AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();
        panel.getAttributeFilter(ATTR_STAGE_NAME).clearAllCheckedValues().selectByNames("Interest", "Closed Lost");
        panel.getAttributeFilter(ATTR_PRIORITY).clearAllCheckedValues().selectByNames("LOW");
        indigoDashboardsPage.selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT);

        assertEquals(
                indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilters().stream()
                        .map(FilterByItem::getTitle)
                        .collect(Collectors.toList()),
                filters, "Added attribute filters are not displayed on configuration panel");
        assertTrue(
                indigoDashboardsPage.getConfigurationPanel()
                        .getFilterByAttributeFilters()
                        .stream()
                        .allMatch(FilterByItem::isChecked),
                "There are some unchecked items");

        if (isSaved) indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            assertEquals(indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Kpi.class,
                    METRIC_AMOUNT)
                    .getValue(), "$60,917,837.30", "The Kpi value is not correct");
        } finally {
            deleteAttributeFilters(filters);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"}, dataProvider = "booleanProvider")
    public void applyMultipleFiltersOnInsight(boolean isSaved) throws IOException, JSONException {
        List<String> filters = asList(ATTR_DEPARTMENT, ATTR_PRODUCT, ATTR_PRIORITY);
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        addMultipleFilters(filters);

        indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT)
                .clearAllCheckedValues().selectByNames("Inside Sales");
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT);

        assertEquals(
                indigoDashboardsPage.getConfigurationPanel()
                        .getFilterByAttributeFilters()
                        .stream()
                        .map(FilterByItem::getTitle)
                        .collect(Collectors.toList()),
                asList(ATTR_DEPARTMENT, ATTR_PRODUCT, ATTR_PRIORITY),
                "Added attribute filters are not displayed on configuration panel");

        assertTrue(
                indigoDashboardsPage.waitForWidgetsLoading()
                        .getConfigurationPanel()
                        .getFilterByAttributeFilters()
                        .stream()
                        .allMatch(FilterByItem::isChecked),
                "There are some unchecked items");

        if (isSaved) indigoDashboardsPage.saveEditModeWithWidgets();

        try {
            assertEquals(
                    indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT)
                            .getChartReport().getDataLabels(),
                    singletonList("53,217"), "The Insight value is not correct");
        } finally {
            deleteAttributeFilters(filters);
        }
    }

    @DataProvider
    public Object[][] testWidgets() {
        return new Object[][] {
                {TEST_INSIGHT, Insight.class},
                {METRIC_AMOUNT, Kpi.class}
        };
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"}, dataProvider = "testWidgets")
    public void testAddingIgnoredCheckboxesOnConfigurationPanel(String widgetHeadline, Class widgetClass) {
        initIndigoDashboardsPage().switchToEditMode()
                .addAttributeFilter(ATTR_STAGE_NAME)
                .addAttributeFilter(ATTR_STAGE_HISTORY)
                .waitForWidgetsLoading()
                .selectWidgetByHeadline(Widget.class, widgetHeadline);

        takeScreenshot(browser, "testAddingIgnoreCheckboxesOnKpi - " + widgetClass.toString(), getClass());
        assertEquals(
                indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilters()
                        .stream()
                        .map(FilterByItem::getTitle)
                        .collect(Collectors.toList()),
                asList(ATTR_STAGE_NAME, ATTR_STAGE_HISTORY),
                "List of ignored checkboxes is not correct");
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkAttributeFilterDefaultState() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().
                addAttributeFilter(ATTR_ACTIVITY_TYPE).waitForWidgetsLoading();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACTIVITY_TYPE)
                .getSelection(), "All");
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"},
            description = "ONE-1981: Attribute filter have long name that is not shorten")
    public void shortenLongAttributeNameOnFilter() {
        String longNameAttribute = "Attribute-Having-Long-Name" + UUID.randomUUID().toString().substring(0, 10);

        // below code is the only way to create an attribute which has long name for now
        // renaming existing one is not a recommended option.
        initAttributePage().createComputedAttribute(new ComputedAttributeDefinition().withName(longNameAttribute)
                .withAttribute(ATTR_SALES_REP).withMetric(METRIC_NUMBER_OF_WON_OPPS)
                .withBucket(new ComputedAttributeDefinition.AttributeBucket(0, "Poor", "120"))
                .withBucket(new ComputedAttributeDefinition.AttributeBucket(1, "Good", "200"))
                .withBucket(new ComputedAttributeDefinition.AttributeBucket(2, "Great", "250"))
                .withBucket(new ComputedAttributeDefinition.AttributeBucket(3, "Best")));

        try {
            AttributeSelect dropdown = initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .dragAddAttributeFilterPlaceholder()
                    .getAttributeSelect();
            assertEquals(dropdown.getTooltipOnAttribute(longNameAttribute), longNameAttribute,
                    "The attribute name is not shortened or the tooltip is not correct");

            dropdown.selectByName(longNameAttribute);
            indigoDashboardsPage.getAttributeFiltersPanel().getLastFilter().selectAllValues();
            assertTrue(indigoDashboardsPage.getAttributeFiltersPanel().getLastFilter()
                    .getTitle().length() < longNameAttribute.length(), "long name attribute is not shortened");
        } finally {
            getMdService().removeObjByUri(
                    getMdService().getObjUri(getProject(), Attribute.class, title(longNameAttribute)));
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void testEmptyStateWhenFilterOut() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addAttributeFilter(ATTR_OPPORTUNITY)
                .getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_OPPORTUNITY)
                .clearAllCheckedValues()
                .selectByNames("1000Bulbs.com > Educationly");

        indigoDashboardsPage.addAttributeFilter(ATTR_ACCOUNT)
                .getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames("(add)ventures");

        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "testEmptyStateWhenFilterOut", getClass());
        assertTrue(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).isEmptyValue(),
                "The empty state on Kpi is not correct");
        assertTrue(indigoDashboardsPage.getWidgetByHeadline(Insight.class, TEST_INSIGHT).isEmptyValue(),
                "The empty state on Insight is not correct");
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"},
            description = "ONE-2044: Cannot delete dataset if there are filters in KPIs")
    public void deleteDatasetUsedByFilter() {
        String workingProject = testParams.getProjectId();
        String attributeName = "Name";

        testParams.setProjectId(createBlankProject(getGoodDataClient(),
                "Delete-Dataset-Having-KPI-Attribute-Filter", testParams.getAuthorizationToken(),
                testParams.getProjectDriver(), testParams.getProjectEnvironment()));
        try {
            setDashboardFeatureFlags();

            uploadCSV(getFilePathFromResource(WITHOUT_DATE_CSV_PATH));
            takeScreenshot(browser, "uploaded-" + WITHOUT_DATE_DATASET + "-dataset", getClass());

            initIndigoDashboardsPage().getSplashScreen()
                    .startEditingWidgets()
                    .addAttributeFilter(attributeName)
                    .getAttributeFiltersPanel()
                    .getAttributeFilter(attributeName)
                    .selectAllValues();

            String metric = "Metric-Using-Dataset-Without-Date";
            getMdService().createObj(getProject(), new Metric(metric,
                    MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]",
                            getMdService().getObjUri(getProject(), Fact.class, title("Censusarea")))),
                    "#,##0.00"));

            // can't use addKpi() because Using dataset has no date;
            indigoDashboardsPage.dragAddKpiPlaceholder().getConfigurationPanel().selectMetricByName(metric);
            indigoDashboardsPage.saveEditModeWithWidgets();

            // this will open Data Sets tab by default
            initManagePage();
            ObjectsTable datasetTable = ObjectsTable
                    .getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser);
            datasetTable.selectObject(WITHOUT_DATE_DATASET);
            DatasetDetailPage page = DatasetDetailPage.getInstance(browser);
            page.deleteObject();

            assertEquals(datasetTable.getAllItems().size(), 0, "The dataset is not deleted");
            assertEquals(initIndigoDashboardsPage().getAttributeFiltersPanel().getAttributeFilters().size(),
                    0, "Attribute filter is not deleted");
        } finally {
            ProjectRestUtils.deleteProject(getGoodDataClient(), testParams.getProjectId());
            testParams.setProjectId(workingProject);
        }
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"})
    public void testAddAttributePlaceHolderExistence() {
        assertTrue(initIndigoDashboardsPageWithWidgets().switchToEditMode().hasAttributeFilterPlaceholder(),
                "The attribute filter placeholder is not displayed");
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"})
    public void testListOfAttributesAndValuesOnFilter() {
        AttributeSelect dropdown = initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .dragAddAttributeFilterPlaceholder().getAttributeSelect();

        assertEquals(dropdown.getValuesWithScrollbar(), asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_ACTIVITY_TYPE,
                ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_IS_ACTIVE, ATTR_IS_CLOSED, ATTR_IS_CLOSED,
                ATTR_IS_TASK, ATTR_IS_WON, ATTR_OPP_SNAPSHOT, ATTR_OPPORTUNITY, ATTR_PRIORITY, ATTR_PRODUCT,
                ATTR_REGION, ATTR_SALES_REP, ATTR_STAGE_HISTORY, ATTR_STAGE_NAME, ATTR_STATUS, ATTR_STATUS),
                "Available attributes are not correct");

        dropdown.selectByName(ATTR_DEPARTMENT);

        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT)
                        .getValues(), asList("Direct Sales", "Inside Sales"),
                "List of values is not correct");
    }

    private IndigoDashboardsPage addAttributeFilterToDashboard(String attribute) {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(attribute)
                .saveEditModeWithWidgets();

        return indigoDashboardsPage;
    }

    private void deleteAttributeFilters(Collection<String> attributes) throws IOException, JSONException {
        for (String att : attributes) {
            IndigoRestUtils.deleteAttributeFilterIfExist(getRestApiClient(), testParams.getProjectId(),
                    getAttributeDisplayFormUri(att));
        }
    }

    private void addMultipleFilters(Collection<String> filters) {
        filters.forEach(att -> indigoDashboardsPage.addAttributeFilter(att)
                .getAttributeFiltersPanel().getAttributeFilter(att).ensureDropdownClosed());
    }
}
