package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotEquals;

import java.io.IOException;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;
import com.gooddata.sdk.model.md.*;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import org.testng.annotations.DataProvider;

import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

import java.util.*;
import java.util.stream.Collectors;

public class AttributeFilteringTest extends AbstractDashboardTest {

    private static String TEST_INSIGHT = "Test-Insight";
    private static final String WITHOUT_DATE_CSV_PATH = "/" + UPLOAD_CSV + "/without.date.csv";
    private static final String WITHOUT_DATE_DATASET = "Without Date";
    private IndigoRestRequest indigoRestRequest;
    private AttributeFiltersPanel filterPanel;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Filtering-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfWonOppsMetric();
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());

        String insightWidget = createInsightWidget(new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        indigoRestRequest.createAnalyticalDashboard(asList(createAmountKpi(), insightWidget));
    }

    @DataProvider(name = "widgetTypeProvider")
    public Object[][] getWidgetTypeProvider() {
        return new Object[][] {
            {Kpi.class.getCanonicalName(), METRIC_AMOUNT},
            {Insight.class.getCanonicalName(), TEST_INSIGHT}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "widgetTypeProvider")
    public void checkStatusOfAttributeOnConfigurationPanelWithEditor(final String clazz, final String headLine)
            throws ClassNotFoundException, JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            Class widget = Class.forName(clazz);

            initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT)
                    .getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).ensureDropdownClosed();
            indigoDashboardsPage.selectWidgetByHeadline(widget, headLine);

            FilterByItem accountFilter = indigoDashboardsPage.getConfigurationPanel()
                    .getFilterByAttributeFilter(ATTR_ACCOUNT);
            assertTrue(accountFilter.isChecked(), "The account attribute filter is not checked by default");

            accountFilter.setChecked(false);
            indigoDashboardsPage.saveEditModeWithWidgets();

            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(widget, headLine);
            assertFalse(accountFilter.isChecked(), "The account attribute filter is not unchecked");

            accountFilter.setChecked(true);

            indigoDashboardsPage.cancelEditModeWithChanges().switchToEditMode()
                    .selectWidgetByHeadline(widget, headLine);

            assertFalse(accountFilter.isChecked(), "The account attribute filter is checked");
        } finally {
            indigoDashboardsPage.deleteAttributeFilter(ATTR_ACCOUNT).saveEditModeWithWidgets();
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkDashboardWithNoAttributeFilters() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

        takeScreenshot(browser, "checkDashboardWithNoAttributeFilters", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void testMakingNoAffectOnWidgetsWhenUsingUnconnectedFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_PRIORITY, "NORMAL")
                .addAttributeFilter(ATTR_PRODUCT, "CompuSci");

        indigoDashboardsPage.waitForWidgetsLoading().selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertEquals(configurationPanel.getErrorMessage(), "The kpi cannot be filtered by Priority. Unselect the check box.");
        configurationPanel.disableDateFilter();
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT)
                .getValue(), "$27,222,899.64", "Unconnected filter make impact to kpi");

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT);
        configurationPanel.disableDateFilter();
        assertEquals(configurationPanel.getErrorMessage(), "The insight cannot be filtered by Product. Unselect the check box.");
        assertEquals(
                indigoDashboardsPage.getWidgetByHeadline(Insight.class, TEST_INSIGHT)
                        .getChartReport()
                        .getDataLabels(),
                singletonList("62,136"), "Unconnected filter make impact to insight");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
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
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_OPP_SNAPSHOT));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
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
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_OPP_SNAPSHOT));
        }
    }

    @DataProvider
    public Object[][] booleanProvider() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "booleanProvider")
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

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "booleanProvider")
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

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "testWidgets")
    public void testAddingIgnoredCheckboxesOnConfigurationPanel(String widgetHeadline, Class<? extends Widget> widgetClass) {
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

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkAttributeFilterDefaultState() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().
                addAttributeFilter(ATTR_ACTIVITY_TYPE).waitForWidgetsLoading();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACTIVITY_TYPE)
                .getSelection(), "All");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"},
            description = "ONE-1981: Attribute filter have long name that is not shorten")
    public void shortenLongAttributeNameOnFilter() {
        String longNameAttribute = "Attribute-Having-Long-Name" + UUID.randomUUID().toString().substring(0, 10);

        // below code is the only way to create an attribute which has long name for now
        // renaming existing one is not a recommended option.
        initAttributePage().moveToCreateAttributePage()
                .createComputedAttribute(new ComputedAttributeDefinition().withName(longNameAttribute)
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

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void testEmptyStateWhenFilterOut() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales")
                .addAttributeFilter(ATTR_SALES_REP,"Huey Jonas");

        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "testEmptyStateWhenFilterOut", getClass());
        assertTrue(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).isEmptyValue(),
                "The empty state on Kpi is not correct");
        assertTrue(indigoDashboardsPage.getWidgetByHeadline(Insight.class, TEST_INSIGHT).isEmptyValue(),
                "The empty state on Insight is not correct");
    }

    @Test(dependsOnGroups = "createProject", groups = {"desktop"},
            description = "ONE-2044: Cannot delete dataset if there are filters in KPIs")
    public void deleteDatasetUsedByFilter() {
        String workingProject = testParams.getProjectId();
        String attributeName = "Name";

        testParams.setProjectId(createNewEmptyProject("Delete-Dataset-Having-KPI-Attribute-Filter"));
        try {
            uploadCSV(getFilePathFromResource(WITHOUT_DATE_CSV_PATH));
            takeScreenshot(browser, "uploaded-" + WITHOUT_DATE_DATASET + "-dataset", getClass());

            String metric = "Metric-Using-Dataset-Without-Date";
            getMdService().createObj(getProject(), new Metric(metric,
                    MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]",
                            getMdService().getObjUri(getProject(), Fact.class, title("Censusarea")))),
                    "#,##0.00"));

            initIndigoDashboardsPage().getSplashScreen()
                    .startEditingWidgets()
                    .addAttributeFilter(attributeName)
                    .getAttributeFiltersPanel()
                    .getAttributeFilter(attributeName)
                    .selectAllValues();            

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
            deleteProject(testParams.getProjectId());
            testParams.setProjectId(workingProject);
        }
    }

    @Test(dependsOnGroups = "createProject", groups = {"desktop"})
    public void testAddAttributePlaceHolderExistence() {
        assertTrue(initIndigoDashboardsPageWithWidgets().switchToEditMode().hasAttributeFilterPlaceholder(),
                "The attribute filter placeholder is not displayed");
    }

    @Test(dependsOnGroups = "createProject", groups = {"desktop"})
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
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(att));
        }
    }

    private void addMultipleFilters(Collection<String> filters) {
        filters.forEach(att -> indigoDashboardsPage.addAttributeFilter(att)
                .getAttributeFiltersPanel().getAttributeFilter(att).ensureDropdownClosed());
    }

    @Test(dependsOnMethods = "testListOfAttributesAndValuesOnFilter", groups = {"desktop"})
    public void createKpisDashboard() {
        Collection<String> filters = asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT);
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        addMultipleFilters(filters);
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"createKpisDashboard"}, groups = {"desktop"})
    public void changePositionExistingAttributeFilter() {
        indigoDashboardsPage.switchToEditMode();
        filterPanel = indigoDashboardsPage.getAttributeFiltersPanel();
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_ACTIVITY));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getLastIndexWebElementAttributeFilter(),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_DEPARTMENT, ATTR_ACTIVITY, ATTR_ACCOUNT));
    }

    @Test(dependsOnMethods = {"changePositionExistingAttributeFilter"}, groups = {"desktop"})
    public void changePositionNewAttributeFilter() {
        indigoDashboardsPage.addAttributeFilter(ATTR_REGION, "East Coast");
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_DEPARTMENT, ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_REGION));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_REGION, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(2),
                filterPanel.getIndexWebElementAttributeFilter(0));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_REGION, ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getIndexWebElementAttributeFilter(2));
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_REGION, ATTR_ACCOUNT, ATTR_DEPARTMENT));
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(1),
                indigoDashboardsPage.getDropzonePosition());
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_REGION));
    }

    @Test(dependsOnMethods = {"changePositionNewAttributeFilter"}, groups = {"desktop"})
    public void changePositionOfDateFilter() {
        indigoDashboardsPage.dragDateAttributeToFilterPlaceholder();
        assertEquals(indigoDashboardsPage.getFirstAttributeFilter(), "Date range");
        assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                asList(ATTR_ACTIVITY, ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_REGION));
    }

    @Test(dependsOnMethods = {"changePositionOfDateFilter"}, groups = {"desktop"}, expectedExceptions = {TimeoutException.class})
    public void changePositionAttributeOnTwoRows() {
        indigoDashboardsPage.addAttributeFilter(ATTR_STAGE_HISTORY).addAttributeFilter(ATTR_STATUS)
                .addAttributeFilter(ATTR_IS_CLOSED).addAttributeFilter(ATTR_OPP_SNAPSHOT)
                .addAttributeFilter(ATTR_IS_ACTIVE).addAttributeFilter(ATTR_IS_TASK)
                .addAttributeFilter(ATTR_FORECAST_CATEGORY).addAttributeFilter(ATTR_OPPORTUNITY)
                .addAttributeFilter(ATTR_PRIORITY).addAttributeFilter(ATTR_ACTIVITY_TYPE, "In Person Meeting")
                .addAttributeFilter(ATTR_STAGE_NAME, "Risk Assessment").clickFilterShowAllOnFilterBar()
                .addAttributeFilter(ATTR_SALES_REP, "Alejandro Vabiano")
                .addAttributeFilter(ATTR_IS_WON, "false")
                .addAttributeFilter(ATTR_PRODUCT, "Grammar Plus").clickFilterShowAllOnFilterBar();

        List<String> expectedAttFilter = indigoDashboardsPage.getListCurrentAttributeFilter();
        filterPanel.dragAndDropAttributeFilter(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
        List<String> currentAttFilter = indigoDashboardsPage.getListCurrentAttributeFilter();
        assertNotEquals(expectedAttFilter, currentAttFilter);
        indigoDashboardsPage.clickFilterShowLessOnFilterBar();
        sleepTightInSeconds(1);
        dragAndDropWithCustomBackend(browser, filterPanel.getIndexWebElementAttributeFilter(0),
                filterPanel.getLastIndexWebElementAttributeFilter());
    }

    @Test(dependsOnMethods = {"changePositionAttributeOnTwoRows"}, groups = {"desktop"})
    public void verifyAttributeFilterWithoutSaving() {
        try {
            indigoDashboardsPage.cancelEditModeWithChanges();
            assertEquals(indigoDashboardsPage.getListCurrentAttributeFilter(),
                    asList(ATTR_ACCOUNT, ATTR_ACTIVITY, ATTR_DEPARTMENT));
        } finally {
            indigoDashboardsPage.switchToEditMode()
                    .deleteAttributeFilter(ATTR_ACCOUNT)
                    .deleteAttributeFilter(ATTR_ACTIVITY).deleteAttributeFilter(ATTR_DEPARTMENT)
                    .saveEditModeWithWidgets();
        }
    }
}
