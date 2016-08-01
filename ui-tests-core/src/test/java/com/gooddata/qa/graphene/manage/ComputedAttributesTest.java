package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;

public class ComputedAttributesTest extends GoodSalesAbstractTest {

    @FindBy(css = ".bucket-values")
    private WebElement bucketValues;

    @FindBy(css = ".buckets .row")
    private List<WebElement> bucketingRows;

    @FindBy(css = ".s-btn-create_computed_attribute")
    private WebElement btnCreateComputedAttribute;

    @FindBy(css = ".s-attributeBucketName")
    private WebElement attributeBucketName;

    @FindBy(css = ".modelThumbContentImage")
    private WebElement modelImage;

    @FindBy(css = ".s-btn-delete")
    private WebElement btnDelete;

    private static final String COMPUTED_ATTRIBUTE_NAME = "A Sales Rep Ranking";
    private static final String REPORT_NAME = "Computed Attribute Report";
    private static final String CA_VARIABLE_REPORT_NAME = "Computed Attribute Report with Variable";
    private static final String TEST_DASHBOAD_NAME = "Test computed attribute";
    private static final String VARIABLE_NAME = "F Stage Name";
    private static final String EXPECTED_DELETE_DESCRIPTION = "To delete this computed attribute you must delete "
            + "its data set. Go to its Data Set administration page and click Delete.";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-computed-attribute";
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 0)
    public void createComputedAttributeTest() {
        createComputedAttribute();
        createAttributePage.cancel();
        waitForDataPageLoaded(browser);
        By computedAttributeItem =
                By.cssSelector(String.format(".s-title-%s a", CssUtils.simplifyText(COMPUTED_ATTRIBUTE_NAME)));
        assertTrue(browser.findElements(computedAttributeItem).isEmpty());

        createComputedAttribute();
        createAttributePage.submit();

        waitForElementVisible(attributeBucketName);
        Screenshots.takeScreenshot(browser, "computed-attribute-details-page", this.getClass());

        List<String> expectedBucketNames = Arrays.asList("Poor", "Good", "Great", "Best");
        List<String> expectedBucketRanges =
                Arrays.asList("# of Won Opps. <= 120", "120 < # of Won Opps. <= 200",
                        "200 < # of Won Opps. <= 250", "250 < # of Won Opps.");
        createAttributePage.checkCreatedComputedAttribute(COMPUTED_ATTRIBUTE_NAME, expectedBucketNames,
                expectedBucketRanges);

        // check delete button is disabled right after computed attribute creation
        checkDeleteButtonAndInfo();
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 0)
    public void checkValidationOfBucketFields() {
        initAttributePage();
        attributePage.createAttribute();
        waitForElementVisible(btnCreateComputedAttribute);

        createAttributePage.selectAttribute("Sales Rep");
        createAttributePage.selectMetric("# of Won Opps.");

        createAttributePage.setBucket(0, "Poor", "");
        assertTrue("is not a number".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "", "1000");
        createAttributePage.setBucket(0, "");
        assertTrue("Field is required.".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "Poor", "abc");
        assertTrue("is not a number".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "Poor", "3000");
        createAttributePage.setBucket(1, "Good", "2000");
        assertTrue("Value should be greater than 3000".equals(getBubbleMessage(browser)), "error message");

        createAttributePage.setBucket(0, "This is a long name which is longer than 128 characters. "
                + "So it should show error that the Values is so long. And I am checking it", "1000");
        createAttributePage.setBucket(0, "This is a long name which is longer than 128 characters. "
                + "So it should show error that the Values is so long. And I am checking it");
        assertTrue("Value is too long.".equals(getBubbleMessage(browser)), "error message");
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 1)
    public void checkOtherPagesAfterComputedAttributeCreated() throws ParseException, IOException, JSONException {
        initAttributePage();
        Screenshots.takeScreenshot(browser, "attribute-list", this.getClass());

        initModelPage();
        String src = modelImage.getAttribute("src");
        for (int i = 0; i < 10 && src.equals(""); i++) {
            sleepTight(1000);
            src = modelImage.getAttribute("src");
        }
        // time for the ldm image loaded from src.
        sleepTight(4000);
        Screenshots.takeScreenshot(browser, "project-model", this.getClass());

        verifyLDMModelProject(32473);
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

    @Test(dependsOnMethods = {"createComputedAttributeTest"}, priority = 4)
    public void createReportWithComputedAttribute() {
        createReport(new UiReportDefinition().withName(REPORT_NAME).withWhats("Amount")
                .withHows(COMPUTED_ATTRIBUTE_NAME), REPORT_NAME);
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f),
                "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"createReportWithComputedAttribute"})
    public void applyListFiltersOnReportWithComputedAttribute() {
        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
        reportPage.initPage()
            .addFilter(FilterItem.Factory.createAttributeFilter("Year (Created)", "2011"));
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute-and-applied-list-filter",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Good", "Poor"), "Attribute values are incorrrect "
                + attributeValues);
        assertEquals(metricValues, Arrays.asList(2.4336918E7f, 3.5933152E7f), "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"createComputedAttributeTest"})
    public void applyVariableFiltersOnReportWithComputedAttribute() {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable(VARIABLE_NAME).withAttribute("Stage Name")
                .withAttributeElements("Closed Won", "Closed Lost"));
        createReport(
                new UiReportDefinition().withName(CA_VARIABLE_REPORT_NAME).withWhats("Amount")
                        .withHows(COMPUTED_ATTRIBUTE_NAME), CA_VARIABLE_REPORT_NAME);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter(VARIABLE_NAME, "Best", "Good", "Great",
                "Poor"));
        reportPage.saveReport();
        Screenshots.takeScreenshot(browser, "report-created-with-computed-attribute-and-applied-variable-filter",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
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

            dashboardsPage.getFirstFilter().changeAttributeFilterValue("Closed Won");
            sleepTight(2000);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> attributeHeaders = tableReport.getAttributesHeader();
            List<String> attributeValues = tableReport.getAttributeElements();
            List<Float> metricValues = tableReport.getMetricElements();
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

            dashboardsPage.getFirstFilter().changeAttributeFilterValue("Best", "Great");
            sleepTight(2000);
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> attributeHeaders = tableReport.getAttributesHeader();
            List<String> attributeValues = tableReport.getAttributeElements();
            List<Float> metricValues = tableReport.getMetricElements();
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
            initReportsPage();
            reportsPage.getReportsList().openReport(REPORT_NAME);
            reportPage.initPage().setReportVisible();

            addEditorUserToProject();
            logout();

            signInAtUI(testParams.getEditorUser(), testParams.getEditorPassword());
            initReportsPage();
            reportsPage.getReportsList().openReport(REPORT_NAME);
            Screenshots.takeScreenshot(browser, "editor-user-report-created-with-computed-attribute",
                    this.getClass());
            List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
            List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
            List<Float> metricValues = reportPage.getTableReport().getMetricElements();
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

            signInAtUI(testParams.getEditorUser(), testParams.getEditorPassword());
            initReportsPage();
            reportsPage.getReportsList().openReport(REPORT_NAME);
            Screenshots.takeScreenshot(browser, "editor-user-report-created-with-computed-attribute",
                    this.getClass());
            attributeHeaders = reportPage.getTableReport().getAttributesHeader();
            attributeValues = reportPage.getTableReport().getAttributeElements();
            metricValues = reportPage.getTableReport().getMetricElements();
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
        initAttributePage();
        attributePage.initAttribute("Sales Rep");
        attributeDetailPage.deleteAttribute();

        initMetricPage();
        waitForElementVisible(metricsTable.getRoot());
        waitForDataPageLoaded(browser);
        metricsTable.selectObject("# of Won Opps.");
        waitForObjectPageLoaded(browser);
        metricDetailPage.deleteMetric();

        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
        Screenshots.takeScreenshot(browser, "delete-metric-and-attribute-used-in-computed-attribute",
                this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(3.4506136E7f, 8632501.0f, 3.8943492E7f, 3.4543328E7f),
                "Metric values are incorrrect");
    }

    @Test(dependsOnMethods = {"computedAttributeVariableOnDashboard"})
    public void deleteVariableUsedInComputedAttributeReport() {
        initVariablePage();
        variablePage.openVariableFromList(VARIABLE_NAME);
        variableDetailPage.deleteVariable();

        initReportsPage();
        reportsPage.getReportsList().openReport(CA_VARIABLE_REPORT_NAME);
        Screenshots.takeScreenshot(browser, "delete-variable-used-in-computed-attribute-report", this.getClass());
        List<String> attributeHeaders = reportPage.getTableReport().getAttributesHeader();
        List<String> attributeValues = reportPage.getTableReport().getAttributeElements();
        List<Float> metricValues = reportPage.getTableReport().getMetricElements();
        assertEquals(attributeHeaders, Arrays.asList(COMPUTED_ATTRIBUTE_NAME), "Attribute name is incorrrect");
        assertEquals(attributeValues, Arrays.asList("Best", "Good", "Great", "Poor"),
                "Attribute values are incorrrect " + attributeValues);
        assertEquals(metricValues, Arrays.asList(1.797468E7f, 6844677.5f, 3.1002138E7f, 2.4959828E7f),
                "Metric values are incorrrect");
    }

    private void createComputedAttribute() {
        initAttributePage();
        attributePage.createAttribute();
        waitForElementVisible(btnCreateComputedAttribute);
        assertFalse(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        createAttributePage.selectAttribute("Sales Rep");
        assertFalse(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        createAttributePage.selectMetric("# of Won Opps.");
        assertEquals(waitForElementVisible(bucketValues).getText(), "# of Won Opps.");
        assertTrue(isCreatedButtonEnabled(), "Create Computed Attribute is Enabled");

        assertEquals(bucketingRows.size(), 3);
        createAttributePage.addBucket();
        assertEquals(bucketingRows.size(), 4);
        createAttributePage.setBucket(0, "Poor", "120");
        createAttributePage.setBucket(1, "Good", "200");
        createAttributePage.setBucket(2, "Great", "250");
        createAttributePage.setBucket(3, "Best");
        createAttributePage.setComputedAttributeName(COMPUTED_ATTRIBUTE_NAME);
        Screenshots.takeScreenshot(browser, "computed-attribute-creation-page", this.getClass());
    }

    private boolean isCreatedButtonEnabled() {
        return !btnCreateComputedAttribute.getAttribute("class").contains("disabled");
    }

    // check that delete button is disabled and that there's expected explanation message
    private void checkDeleteButtonAndInfo() {
        assertTrue(attributeDetailPage.isDeleteButtonDisabled(), "Delete Button is Disabled");
        assertEquals(attributeDetailPage.getDeleteButtonDescription(), EXPECTED_DELETE_DESCRIPTION);
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
