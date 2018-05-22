package com.gooddata.qa.graphene.reports;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.report.AttributeSndPanel;
import com.gooddata.qa.graphene.fragments.reports.report.MetricSndPanel;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.Keys;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.name;

public class GoodSalesCreateReportTest extends GoodSalesAbstractTest {

    private static final String INAPPLICABLE_ATTR_MESSAGE = "Attributes that are unavailable may lead to"
            + " nonsensical results if used to break down the metric(s) in this report."
            + " To select an unavailable attribute anyway, hold Shift while clicking its name.";

    private static final String INVALID_DATA_REPORT_MESSAGE =
            "Report not computable due to improper metric definition";

    private static final String WRONG_STATE_FILTER_MESSAGE = "Please confirm or cancel your changes in the"
            + " Slice and Dice dialog box before proceeding.";

    private static final String REPORT_NOT_COMPUTABLE_MESSAGE =
            "Report not computable due to improper metric definition";

    private static final String ATTRIBUTE_LIMIT_MESSAGE = "You have reached the limit of attributes in report.";

    private static final String METRIC_LIMIT_MESSAGE = "You have reached the limit of metrics in report.";

    private Project project;
    private MetadataService mdService;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-create-report";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportWithInapplicableAttribute() {
        initReportCreation();

        reportPage.initPage().openWhatPanel().selectItem(METRIC_NUMBER_OF_ACTIVITIES);

        AttributeSndPanel attributePanel = reportPage.openHowPanel();
        attributePanel.trySelectItem(ATTR_PRODUCT);

        assertThat(getErrorMessage(), startsWith(INAPPLICABLE_ATTR_MESSAGE));
        assertThat(attributePanel.getUnReachableAttributeDescription(ATTR_PRODUCT), equalTo(
                "Product is unavailable due to the metric(s) in this report. Use Shift + click to override."));

        attributePanel.selectInapplicableItem(ATTR_PRODUCT).done();
        assertThat(reportPage.getInvalidDataReportMessage(), equalTo(INVALID_DATA_REPORT_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryToAddFilterInWrongState() {
        initReportCreation();

        reportPage.initPage()
            .openWhatPanel()
            .selectItem(METRIC_NUMBER_OF_ACTIVITIES);
        reportPage.tryOpenFilterPanelInDisabledState();

        String wrongStateFilterMessage = waitForElementVisible(cssSelector(".c-infoDialog .message"), browser)
                .getText().trim();
        assertThat(wrongStateFilterMessage, equalTo(WRONG_STATE_FILTER_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelCreatingReport() {
        initReportCreation();

        reportPage.initPage().openWhatPanel().selectItem(METRIC_NUMBER_OF_ACTIVITIES);
        reportPage.openHowPanel().selectItem(ATTR_ACTIVITY_TYPE).done();
        reportPage.selectReportVisualisation(ReportTypes.TABLE)
            .clickSaveReport()
            .cancelCreateReportInDialog();

        waitForFragmentVisible(reportPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchBetweenFolderAndTagView() {
        AttributeSndPanel attributePanel = initReportCreation().openHowPanel();
        attributePanel.switchViewBy("Tags");

        assertThat(attributePanel.getViewGroups(),
                equalTo(asList("All Attributes", "date", "day", "eu", "month", "quarter", "week", "year")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void loadAllAttributesInFilterPanel() {
        initReportCreation().openHowPanel().selectItem("Date (Snapshot)");

        waitForElementVisible(cssSelector(".s-btn-filter_this_attribute"), browser).sendKeys(Keys.ENTER);
        waitForElementVisible(cssSelector(".guidedNavigation .hyperlinkOn:not(.hidden) a"), browser).sendKeys(Keys.ENTER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportWithNonDefaultAttributeLabel() {
        AttributeSndPanel attributePanel = initReportCreation().openHowPanel();
        attributePanel.selectItem("Stage Name");
        attributePanel.changeDisplayLabel("Order").done();
        waitForAnalysisPageLoaded(browser);

        assertThat(reportPage.getTableReport().getAttributeValues(),
                equalTo(asList("101", "102", "103", "104", "105", "106", "107", "108")));

    }

    @Test(dependsOnGroups = {"createProject"})
    public void createFilterBeforeAddingMetricAttribute() {
        initReportCreation();

        reportPage.initPage()
            .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_ACTIVITY_TYPE, "Email"))
            .openHowPanel()
            .selectItem(ATTR_ACTIVITY_TYPE)
            .done();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterNotRemoveWhenAttributeRemovedFromHow() {
        initReportCreation().openHowPanel().selectItem(ATTR_ACTIVITY_TYPE).done();

        assertThat(reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_ACTIVITY_TYPE, "Email"))
            .getFilters().size(), equalTo(1));

        reportPage.openHowPanel().deselectItem(ATTR_ACTIVITY_TYPE).done();
        assertThat(reportPage.getFilters().size(), equalTo(1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initGoodDataClient() {
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"initGoodDataClient"})
    public void createReportNotComputable() throws ParseException, JSONException, IOException {
        String metricName = "test-metric";
        String reportName = "Test Report";

        Fact amount = mdService.getObj(project, Fact.class, Restriction.title("Amount"));
        Metric testMetric = mdService.createObj(project,
                new Metric(metricName, "SELECT SUM([" + amount.getUri() + "])", "#,##0"));
        Attribute yearSnapshot = mdService.getObj(project, Attribute.class, Restriction.title("Year (Snapshot)"));

        ReportDefinition definition = GridReportDefinitionContent.create(
                        reportName,
                        singletonList(METRIC_GROUP),
                        singletonList(new AttributeInGrid(yearSnapshot.getDefaultDisplayForm().getUri(), yearSnapshot.getTitle())),
                        singletonList(new MetricElement(testMetric)));
        definition = mdService.createObj(project, definition);
        mdService.createObj(project, new Report(definition.getTitle(), definition));

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .changeMetricExpression(testMetric.getUri(), "SELECT ["+ amount.getUri() + "]");
        assertThat(initReportsPage().openReport(reportName).getInvalidDataReportMessage(),
                equalTo(REPORT_NOT_COMPUTABLE_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLimitAttributes() {
        AttributeSndPanel attributePanel = initReportCreation().openHowPanel();
        attributePanel.selectItems("Account", "Activity", "Activity Type", "Department", "Is Task?", "Opportunity",
                "Priority", "Region", "Sales Rep", "Date (Activity)", "Month (Activity)", "Month/Year (Activity)",
                "Month of Quarter (Activity)", "Quarter/Year (Activity)", "Year (Activity)", "Date (Created)", 
                "Month (Created)", "Month/Year (Created)", "Quarter/Year (Created)", "Year (Created)");

        attributePanel.trySelectItem("Month of Quarter (Created)");
        assertThat(getErrorMessage(), startsWith(ATTRIBUTE_LIMIT_MESSAGE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLimitMetrics() throws IOException, JSONException {
        Metrics metricCreator = getMetricCreator();
        List<Metric> metrics = asList(
                metricCreator.createNumberOfLostOppsMetric(), metricCreator.createNumberOfOpenOppsMetric(),
                metricCreator.createNumberOfOpportunitiesBOPMetric(), metricCreator.createNumberOfWonOppsMetric(),
                metricCreator.createPercentOfGoalMetric(), metricCreator.createAvgAmountMetric(),
                metricCreator.createAvgWonMetric(), metricCreator.createBestCaseMetric(),
                metricCreator.createDaysUntilCloseMetric(), metricCreator.createLostMetric(),
                metricCreator.createExpectedWonMetric(), metricCreator.createQuotaMetric(),
                metricCreator.createStageDurationMetric(), metricCreator.createStageVelocityMetric(),
                metricCreator.createWinRateMetric(), metricCreator.createExpectedWonVsQuotaMetric());

        try {
            MetricSndPanel metricPanel = initReportCreation().openWhatPanel();
            metricPanel.selectItems("# of Activities", "# of Lost Opps.", "# of Open Opps.", "# of Opportunities",
                    "# of Opportunities [BOP]", "# of Won Opps.", "% of Goal", "Amount", "Avg. Amount", "Avg. Won",
                    "Best Case", "Days until Close", "Expected", "Lost", "Expected + Won", "Quota",
                    "Stage Duration", "Stage Velocity", "Win Rate", "Won");

            metricPanel.trySelectItem("Expected + Won vs. Quota");
            assertThat(getErrorMessage(), startsWith(METRIC_LIMIT_MESSAGE));

        } finally {
            final CommonRestRequest restRequest = new CommonRestRequest(
                    new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
            for (Metric metric : metrics) {
                restRequest.deleteObjectsUsingCascade(metric.getUri());
            }
        }
    }

    private String getErrorMessage() {
        try {
            return waitForElementVisible(name("reportEditorForm"), browser).getText().trim();
        } finally {
            waitForElementVisible(cssSelector("[name=reportEditorForm] .s-btn-close"), browser).click();
        }
    }
}
