package com.gooddata.qa.graphene.manage;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition.AttributeBucket;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.AttributePage;
import com.gooddata.qa.graphene.fragments.manage.CreateAttributePage;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_EXPECTED_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricExpression;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class ComputedAttributesTest extends GoodSalesAbstractTest {

    private static final String COMPUTED_ATTRIBUTE_NAME = "A Sales Rep Ranking";
    private static final String COMPUTED_ANOTHER_ATTRIBUTE_1 = "Another Computed Attribute 1";
    private static final String COMPUTED_ANOTHER_ATTRIBUTE_2 = "Another Computed Attribute 2";
    private static final String COMPUTED_ANOTHER_ATTRIBUTE_3 = "Another Computed Attribute 3";
    private static final String COMPUTED_ANOTHER_ATTRIBUTE_4 = "Another Computed Attribute 4";
    private static final String REPORT_NAME = "Computed Attribute Report";
    private static final String DRILL_REPORT_NAME_1 = "Drill Computed Attribute Report 1";
    private static final String DRILL_REPORT_NAME_2 = "Drill Computed Attribute Report 2";
    private static final String DRILL_REPORT_NAME_3 = "Drill Computed Attribute Report 3";
    private static final String CHANGEMAQL_REPORT_NAME = "Change MAQL Report";
    private static final String CHANGEMAQL_REPORT_NAME_2 = "Change MAQL Report 2";
    private static final String ATTRIBUTE_VALUE_TO_DRILL = "Poor";
    private static final String CA_VARIABLE_REPORT_NAME = "Computed Attribute Report with Variable";
    private static final String TEST_DASHBOAD_NAME = "Test computed attribute";
    private static final String VARIABLE_NAME = "F Stage Name";
    private static final String EXPECTED_DELETE_DESCRIPTION = "To delete this computed attribute you must delete "
            + "its data set. Go to its Data Set administration page and click Delete.";

    private static final ComputedAttributeDefinition DEFINITION = new ComputedAttributeDefinition()
            .withName(COMPUTED_ATTRIBUTE_NAME)
            .withAttribute(ATTR_SALES_REP)
            .withMetric(METRIC_NUMBER_OF_WON_OPPS)
            .withBucket(new AttributeBucket(0, "Poor", "120"))
            .withBucket(new AttributeBucket(1, "Good", "200"))
            .withBucket(new AttributeBucket(2, "Great", "250"))
            .withBucket(new AttributeBucket(3, "Best"));

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-computed-attribute";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        createExpectedPercentOfGoalMetric();
        createNumberOfWonOppsMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 0,
            description = "AQE-1684 starjoin_failed when create compute attribute based on complex metric")
    public void testComputedAttributeInComplexMetric() {
        final String computedAttr = "AQE-1684";
        initAttributePage().moveToCreateAttributePage().createComputedAttribute(
                new ComputedAttributeDefinition()
                    .withAttribute(ATTR_STAGE_NAME)
                    .withMetric(METRIC_EXPECTED_PERCENT_OF_GOAL)
                    .withName(computedAttr));
        // comine created computedAtt with any att to make an empty report
        createReport(new UiReportDefinition()
            .withName("Test computed attr in complex metric")
            .withHows(computedAttr, ATTR_DEPARTMENT), "Test computed attr in complex metric");
        assertEquals(waitForFragmentVisible(reportPage)
                .waitForReportExecutionProgress()
                .getDataReportHelpMessage(),
                "No data match the filtering criteria");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 0)
    public void createComputedAttributeTest() {
        CreateAttributePage createAttributePage = initAttributePage().moveToCreateAttributePage();
        assertFalse(createAttributePage.isCreateComputedAttributeButtonEnable(),
                "Create computed attribute button is enabled");

        createAttributePage
                .fillInComputedAttributeForm(DEFINITION)
                .cancel();
        waitForDataPageLoaded(browser);
        By computedAttributeItem =
                By.cssSelector(String.format(".s-title-%s a", CssUtils.simplifyText(COMPUTED_ATTRIBUTE_NAME)));
        assertTrue(browser.findElements(computedAttributeItem).isEmpty());

        AttributePage.getInstance(browser).moveToCreateAttributePage().createComputedAttribute(DEFINITION);
        Screenshots.takeScreenshot(browser, "computed-attribute-details-page", this.getClass());

        List<String> expectedBucketNames = Arrays.asList("Poor", "Good", "Great", "Best");
        List<String> expectedBucketRanges =
                Arrays.asList("# of Won Opps. <= 120", "120 < # of Won Opps. <= 200",
                        "200 < # of Won Opps. <= 250", "250 < # of Won Opps.");
        CreateAttributePage.getInstance(browser).checkCreatedComputedAttribute(COMPUTED_ATTRIBUTE_NAME, expectedBucketNames,
                expectedBucketRanges);

        // check delete button is disabled right after computed attribute creation
        checkDeleteButtonAndInfo();
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 0)
    public void checkValidationOfBucketFields() {
        CreateAttributePage createAttributePage = initAttributePage()
                .moveToCreateAttributePage()
                .selectAttribute(ATTR_SALES_REP)
                .selectMetric(METRIC_NUMBER_OF_WON_OPPS)
                .setBucket(0, "Poor", "");
        assertTrue("is not a number".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "", "1000").setBucket(0, "");
        assertTrue("Field is required.".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "Poor", "abc");
        assertTrue("is not a number".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "Poor", "3000").setBucket(1, "Good", "2000");
        assertTrue("Value should be greater than 3000".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "This is a long name which is longer than 128 characters. "
                + "So it should show error that the Values is so long. And I am checking it", "1000")
                .setBucket(0, "This is a long name which is longer than 128 characters. "
                + "So it should show error that the Values is so long. And I am checking it");
        assertTrue("Value is too long.".equals(getBubbleMessage(browser)), "error message");
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 1)
    public void checkOtherPagesAfterComputedAttributeCreated() throws ParseException, IOException, JSONException {
        initAttributePage();
        Screenshots.takeScreenshot(browser, "attribute-list", this.getClass());

        initModelPage();
        waitForDataModelImageDisplayed();

        Screenshots.takeScreenshot(browser, "project-model", this.getClass());

        verifyLDMModelProject(33304);
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 1)
    public void checkRenameComputedAttribute() {
        String newAttributeName = "New Attribute Name";
        initAttributePage().renameAttribute(COMPUTED_ATTRIBUTE_NAME, newAttributeName);
        By newNameAttributeItem =
                By.cssSelector(String.format(".s-title-%s a", CssUtils.simplifyText(newAttributeName)));
        initAttributePage();
        assertTrue(!browser.findElements(newNameAttributeItem).isEmpty());
        initAttributePage().renameAttribute(newAttributeName, COMPUTED_ATTRIBUTE_NAME);
        Screenshots.takeScreenshot(browser, "attribute-list", this.getClass());
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 2)
    public void checkAttributePageAfterComputedAttributeCreated() {
        initAttributePage();
        By computedAttributeItem =
                By.cssSelector(String.format(".s-title-%s a", CssUtils.simplifyText(COMPUTED_ATTRIBUTE_NAME)));
        waitForElementVisible(computedAttributeItem, browser).click();
        // check after fresh attribute load
        checkDeleteButtonAndInfo();
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 3)
    public void checkNavigationToDatasetAndBack() {
        initAttributePage();
        String titleSelector = ".s-title-" + CssUtils.simplifyText(COMPUTED_ATTRIBUTE_NAME);

        By computedAttributeItem = By.cssSelector(titleSelector + " a");
        waitForElementVisible(computedAttributeItem, browser).click();

        By linkToDatasetItem = By.cssSelector(".specialAction .info a");
        waitForElementVisible(linkToDatasetItem, browser).click();

        By linkToAttribute = By.cssSelector("td " + titleSelector);
        waitForElementVisible(linkToAttribute, browser).click();

        checkDeleteButtonAndInfo();
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 3)
    public void checkComputedAttributeWhenChangeMetricExpression() throws JSONException, IOException {
        String SUM_OF_AMOUNT = "SumOfAmount";
        String amountFactUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));
        Attribute saleRepAttribute = getMdService().getObj(getProject(), Attribute.class, Restriction.
                title(ATTR_SALES_REP));
        String saleRepAttributeUri = saleRepAttribute.getUri();
        String saleRepValueUri = getMdService().getAttributeElements(saleRepAttribute)
                .stream()
                .filter(e -> "Cory Owens".equals(e.getTitle()))
                .findFirst()
                .get()
                .getUri();
        String maqlExpression = format("SELECT SUM([%s])", amountFactUri);
        String sumOfAmountMetricUri = getMdService()
             .createObj(getProject(), new Metric(SUM_OF_AMOUNT, maqlExpression, "#,##0"))
             .getUri();
        createAndOpenComputedAttribute(ATTR_SALES_REP, SUM_OF_AMOUNT, COMPUTED_ANOTHER_ATTRIBUTE_4);
        createReport(new UiReportDefinition().withName(CHANGEMAQL_REPORT_NAME).withWhats(SUM_OF_AMOUNT)
             , "SaleRepReport");
        createReport(new UiReportDefinition().withName(CHANGEMAQL_REPORT_NAME_2).withWhats(SUM_OF_AMOUNT)
             .withHows(ATTR_SALES_REP, COMPUTED_ANOTHER_ATTRIBUTE_4), "SaleRepReport2");

         String newMaqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                 amountFactUri, saleRepAttributeUri, saleRepValueUri);
        changeMetricExpression(getRestApiClient(), sumOfAmountMetricUri, newMaqlExpression);

        initReportsPage().openReport(CHANGEMAQL_REPORT_NAME).initPage();
        checkRedBar(browser);

        initReportsPage().openReport(CHANGEMAQL_REPORT_NAME_2).initPage();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 4)
    public void checkDrillOnComputedAttribute() {
        createAndOpenComputedAttribute(ATTR_SALES_REP, METRIC_NUMBER_OF_WON_OPPS, COMPUTED_ANOTHER_ATTRIBUTE_1);
        drillToAttribute(ATTR_DEPARTMENT);
        checkRedBar(browser);

        createAndOpenComputedAttribute(ATTR_SALES_REP, METRIC_NUMBER_OF_WON_OPPS, COMPUTED_ANOTHER_ATTRIBUTE_2);
        drillToAttribute(ATTR_MONTH_YEAR_CREATED);
        checkRedBar(browser);

        createAndOpenComputedAttribute(ATTR_SALES_REP, METRIC_NUMBER_OF_WON_OPPS, COMPUTED_ANOTHER_ATTRIBUTE_3);
        drillToAttribute(COMPUTED_ATTRIBUTE_NAME);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 4)
    public void createReportWithComputedAttribute() {
        createReport(new UiReportDefinition().withName(REPORT_NAME).withWhats("Amount")
                .withHows(COMPUTED_ATTRIBUTE_NAME), REPORT_NAME);
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
        List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
        List<Float> metricValues = reportPage.getTableReport().getMetricValues();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f),
                "Metric values are incorrrect");
    }

    @DataProvider
    public Object[][] drillComputedAttributeTest() {
        return new Object[][] {
            {DRILL_REPORT_NAME_1, COMPUTED_ANOTHER_ATTRIBUTE_1, ATTRIBUTE_VALUE_TO_DRILL, ATTR_DEPARTMENT},
            {DRILL_REPORT_NAME_2, COMPUTED_ANOTHER_ATTRIBUTE_2, ATTRIBUTE_VALUE_TO_DRILL, ATTR_MONTH_YEAR_CREATED},
            {DRILL_REPORT_NAME_3, COMPUTED_ANOTHER_ATTRIBUTE_3, ATTRIBUTE_VALUE_TO_DRILL, COMPUTED_ATTRIBUTE_NAME}
        };
    }

    @Test(dependsOnMethods = {"checkDrillOnComputedAttribute"}, priority = 5, dataProvider = "drillComputedAttributeTest")
    public void createReportWithDrillComputedAttribute(String name, String attribute, String value, String target){
        UiReportDefinition rd = new UiReportDefinition().withName(name)
                  .withWhats("Amount").withHows(attribute);
        createReport(rd, rd.getName());
        checkRedBar(browser);

        TableReport report = openTableReport(name);
        assertEquals(report.getAttributeHeaders(), asList(attribute));
        assertTrue(reportPage.getFilters().isEmpty());

        report = drillOnAttributeFromReport(report,value);
        assertEquals(report.getAttributeHeaders(), asList(target));
        assertEquals(reportPage.getFilters(), asList(attribute + " is " + value));

        report = backToPreviousReport();
        assertEquals(report.getAttributeHeaders(), asList(attribute));
        assertTrue(reportPage.getFilters().isEmpty());
    }

    @Test(dependsOnMethods = {"createReportWithComputedAttribute"})
    public void applyListFiltersOnReportWithComputedAttribute() {
        initReportsPage()
            .openReport(REPORT_NAME)
            .initPage()
            .addFilter(FilterItem.Factory.createAttributeFilter("Year (Created)", "2011"));
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute-and-applied-list-filter",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
        List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
        List<Float> metricValues = reportPage.getTableReport().getMetricValues();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Good", "Poor"), "Attribute values are incorrrect "
                + attributeValues);
        assertEquals(metricValues, Arrays.asList(2.4336918E7f, 3.5933152E7f), "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"})
    public void applyVariableFiltersOnReportWithComputedAttribute() {
        initVariablePage().createVariable(new AttributeVariable(VARIABLE_NAME).withAttribute("Stage Name")
                .withAttributeValues("Closed Won", "Closed Lost"));
        createReport(
                new UiReportDefinition().withName(CA_VARIABLE_REPORT_NAME).withWhats("Amount")
                        .withHows(COMPUTED_ATTRIBUTE_NAME), CA_VARIABLE_REPORT_NAME);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter(VARIABLE_NAME, "Best", "Good", "Great",
                "Poor"));
        reportPage.saveReport();
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute-and-applied-variable-filter",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
        List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
        List<Float> metricValues = reportPage.getTableReport().getMetricValues();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(1.797468E7f, 6844677.5f, 3.1002138E7f, 2.4959828E7f),
                "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"applyVariableFiltersOnReportWithComputedAttribute"})
    public void computedAttributeVariableOnDashboard() {
        try {
            addReportToNewDashboard(CA_VARIABLE_REPORT_NAME, TEST_DASHBOAD_NAME);
            dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_NAME)
                    .saveDashboard();

            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            dashboardsPage.getFirstFilter().changeAttributeFilterValues("Closed Won");
            sleepTight(2000);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> attributeHeaders = tableReport.getAttributeHeaders();
            List<String> attributeValues = tableReport.getAttributeValues();
            List<Float> metricValues = tableReport.getMetricValues();
            assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
            assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                    "Attribute values are incorrrect " + attributeValues);
            assertEquals(metricValues, Arrays.asList(9918483.0f, 3659047.8f, 1.2815729E7f, 1.1917493E7f),
                    "Metric values are incorrrect");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createReportWithComputedAttribute"})
    public void computedAttributeReportOnDashboard() {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            dashboardsPage
                    .editDashboard()
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, COMPUTED_ATTRIBUTE_NAME)
                    .saveDashboard();

            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            dashboardsPage.getFirstFilter().changeAttributeFilterValues("Best", "Great");
            sleepTight(2000);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> attributeHeaders = tableReport.getAttributeHeaders();
            List<String> attributeValues = tableReport.getAttributeValues();
            List<Float> metricValues = tableReport.getMetricValues();
            System.out.println(metricValues);
            assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
            assertEquals(attributeValues, Arrays.asList("Best", "Great"), "Attribute values are incorrrect "
                    + attributeValues);
            assertEquals(metricValues, Arrays.asList(3.4506136E7f, 3.8943492E7f), "Metric values are incorrrect");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createReportWithComputedAttribute"})
    public void applyMufFiltersOnReportWithComputedAttribute() throws IOException, JSONException {
        try {
            initReportsPage()
                .openReport(REPORT_NAME)
                .initPage()
                .setReportVisible();

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
            initReportsPage().openReport(REPORT_NAME);
            Screenshots.takeScreenshot(browser, "editor-user-report-created-with-computed-attribute",
                    this.getClass());
            List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
            List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
            List<Float> metricValues = reportPage.getTableReport().getMetricValues();
            assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
            assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                    "Attribute values are incorrrect " + attributeValues);
            assertEquals(metricValues, Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f),
                    "Metric values are incorrrect");
            logout();

            signIn(false, UserRoles.ADMIN);
            restApiClient = getRestApiClient();

            String mufUri = createStageMuf(Arrays.asList("Won", "Lost"), "Status User Filters");
            DashboardsRestUtils.addMufToUser(restApiClient, testParams.getProjectId(), testParams.getEditorUser(),
                    mufUri);
            logout();

            signInAtUI(testParams.getEditorUser(), testParams.getPassword());
            initReportsPage().openReport(REPORT_NAME);
            Screenshots.takeScreenshot(browser, "editor-user-report-created-with-computed-attribute",
                    this.getClass());
            attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
            attributeValues = reportPage.getTableReport().getAttributeValues();
            metricValues = reportPage.getTableReport().getMetricValues();
            assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
            assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                    "Attribute values are incorrrect " + attributeValues);
            assertEquals(metricValues, Arrays.asList(1.797468E7f, 6844677.5f, 3.1002138E7f, 2.4959828E7f),
                    "Metric values are incorrrect");
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"createReportWithComputedAttribute",
        "applyListFiltersOnReportWithComputedAttribute", "computedAttributeReportOnDashboard",
        "applyMufFiltersOnReportWithComputedAttribute"})
    public void deleteAttributeAndMetricUsedInComputedAttribute() {
        initAttributePage().initAttribute(ATTR_SALES_REP)
            .deleteObject();

        initMetricPage();
        waitForDataPageLoaded(browser);
        ObjectsTable.getInstance(id(ObjectTypes.METRIC.getObjectsTableID()), browser).selectObject(METRIC_NUMBER_OF_WON_OPPS);
        waitForObjectPageLoaded(browser);
        MetricDetailsPage.getInstance(browser).deleteObject();

        initReportsPage().openReport(REPORT_NAME);
        Screenshots.takeScreenshot(browser, "delete-metric-and-attribute-used-in-computed-attribute",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
        List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
        List<Float> metricValues = reportPage.getTableReport().getMetricValues();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f),
                "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"computedAttributeVariableOnDashboard"})
    public void deleteVariableUsedInComputedAttributeReport() {
        initVariablePage()
                .openVariableFromList(VARIABLE_NAME)
                .deleteObject();

        initReportsPage().openReport(CA_VARIABLE_REPORT_NAME);
        Screenshots.takeScreenshot(browser, "delete-variable-used-in-computed-attribute-report", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributeHeaders();
        List<String> attributeValues = reportPage.getTableReport().getAttributeValues();
        List<Float> metricValues = reportPage.getTableReport().getMetricValues();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(1.797468E7f, 6844677.5f, 3.1002138E7f, 2.4959828E7f),
                "Metric values are incorrrect");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void renderReportContainComputedAttributeWithoutMetric() {
        String factAmountUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));
        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        String stageNameValues = getMdService()
                .getAttributeElements(stageNameAttribute)
                .subList(0, 2)
                .stream()
                .map(AttributeElement::getUri)
                .map(e -> format("[%s]", e))
                .collect(joining(","));

        String expression = format("SELECT SUM([%s]) WHERE [%s] IN (%s)",
                factAmountUri, stageNameAttribute.getUri(), stageNameValues);
        String metric = createMetric("New-metric", expression, "#,##0").getTitle();

        final String computedAttribute = "Computed-Attribute";
        final String attributeUri = initAttributePage()
                .moveToCreateAttributePage()
                .createComputedAttribute(new ComputedAttributeDefinition()
                        .withName(computedAttribute)
                        .withAttribute(ATTR_STAGE_NAME)
                        .withMetric(metric))
                .getAttributeUri();

        try {
            createReport(new UiReportDefinition()
                    .withName("Simple-report")
                    .withHows(computedAttribute, ATTR_DEPARTMENT),
                    "Report-renders-well-with-computed-attribute-without-metric");
            assertEquals(reportPage.getTableReport().getAttributeValues(),
                    asList("Large", "Direct Sales", "Inside Sales"));

        } finally {
            getMdService().removeObjByUri(attributeUri);
        }
    }

    private void waitForDataModelImageDisplayed() {
        waitForElementVisible(By.className("modelThumbContentImage"), browser);
    }

    // check that delete button is disabled and that there's expected explanation message
    private void checkDeleteButtonAndInfo() {
        AttributeDetailPage attributeDetailPage = AttributeDetailPage.getInstance(browser);
        assertTrue(attributeDetailPage.isDeleteButtonDisabled(), "Delete Button is Disabled");
        assertEquals(attributeDetailPage.getDeleteButtonDescription(), EXPECTED_DELETE_DESCRIPTION);
    }

    private void createAndOpenComputedAttribute(String attribute, String metric, String name){
        initAttributePage().moveToCreateAttributePage().createComputedAttribute(
                new ComputedAttributeDefinition()
                .withAttribute(attribute)
                .withMetric(metric)
                .withName(name)
                .withBucket(new AttributeBucket(0, "Poor", "120"))
                .withBucket(new AttributeBucket(1, "Good", "200"))
                .withBucket(new AttributeBucket(2, "Great", "250"))
                .withBucket(new AttributeBucket(3, "Best")));

        initAttributePage();
        String titleSelector = ".s-title-" + CssUtils.simplifyText(name);
        By computedAttributeItem = By.cssSelector(titleSelector + " a");
        waitForElementVisible(computedAttributeItem, browser).click();
    }

    private void drillToAttribute(String attribute) {
        AttributeDetailPage attributeDetailPage = AttributeDetailPage.getInstance(browser);
        attributeDetailPage.setDrillToAttribute(attribute);
    }

    private TableReport openTableReport(String reportName) {
        return initReportsPage().openReport(reportName).getTableReport();
    }

    private TableReport drillOnAttributeFromReport(TableReport report, String value) {
        report.drillOn(value, CellType.ATTRIBUTE_VALUE);
        return ReportPage.getInstance(browser).waitForReportExecutionProgress().getTableReport();
    }

    private TableReport backToPreviousReport() {
        browser.navigate().back();
        return ReportPage.getInstance(browser).waitForReportExecutionProgress().getTableReport();
    }

    private String createStageMuf(final List<String> expectedStageElements, final String mufTitle)
                    throws ParseException, JSONException, IOException {
        final Attribute stage = getMdService().getObj(getProject(), Attribute.class, Restriction.identifier("attr.stage.status"));
        final List<AttributeElement> filteredElements = getMdService().getAttributeElements(stage).stream()
                .filter(e -> expectedStageElements.contains(e.getTitle())).collect(Collectors.toList());

        final List<String> elementUris = filteredElements.stream().map(e -> e.getUri()).collect(Collectors.toList());

        final Map<String, Collection<String>> conditions = new HashMap<>();
        conditions.put(stage.getUri(), elementUris);

        return DashboardsRestUtils
                .createSimpleMufObjByUri(getRestApiClient(), testParams.getProjectId(), mufTitle, conditions);
    }
}