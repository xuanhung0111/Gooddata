package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DateFilterADMeasureTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_DATE_ON_VIEWBY = "Insight has date on view by and no date on filter section";
    private static final String INSIGHT_HAS_DATE_ON_FILTER_SECTION = "Insight has date on filter section and no date" +
            " on view by";
    private static final String INSIGHT_HAS_DATE_ON_VIEWBY_AND_FILTER_SECTION = "Insight has date on filter section" +
            " and date on view by";
    private static final String INSIGHT_HAS_A_MEASURE = "Insight has one measure, date filter added to this measure";
    private static final String INSIGHT_HAS_MANY_MEASURE = "Have 2+ measures, date filter added to some of" +
            " or all these measures";
    private static final String EXPORT_TOOLTIP_CONTENT = "The insight is not compatible with Report Editor. " +
            "To open the insight as a report, remove date filters from the measure definition.";
    private static final String ADD_ATTRIBUTE_FILTER_BUTTON = "Add attribute filter";
    private static final String FILTER_BY_DATE_BUTTON = "Filter by date";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Filter-AD-Measures-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();

        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkFilterByDateButton() {
        assertEquals(initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().getByDateAndAttributeFilter(),
                asList(FILTER_BY_DATE_BUTTON, ADD_ATTRIBUTE_FILTER_BUTTON));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateFilterWithSameMeasures() {
        initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.THIS_YEAR.toString()).collapseConfiguration();
        MetricConfiguration metricConfiguration = analysisPage.addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getLastMetricConfiguration()
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.THIS_YEAR.toString());
        metricConfiguration.collapseConfiguration();
        metricConfiguration.expandConfiguration().expandFilterByDate().changeDateDimension(DATE_DATASET_CREATED);
        metricConfiguration.collapseConfiguration();
        assertEquals(metricConfiguration.expandConfiguration().expandFilterByDate()
                .getDateDimension(), DATE_DATASET_CREATED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateFilterWithMeasuresRelatedDate() {
        initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.THIS_YEAR.toString()).collapseConfiguration();
        MetricConfiguration metricConfiguration = analysisPage.addMetric(METRIC_AVG_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AVG_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.THIS_YEAR.toString());
        metricConfiguration.collapseConfiguration();
        metricConfiguration.expandConfiguration().expandFilterByDate().changeDateDimension(DATE_DATASET_CREATED);
        metricConfiguration.collapseConfiguration();
        assertEquals(metricConfiguration.expandConfiguration().expandFilterByDate()
                .getDateDimension(), DATE_DATASET_CREATED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateFilterWithMeasuresNotRelatedDate() {
        MetricConfiguration metricConfiguration;
        analysisPage = initAnalysePage();

        metricConfiguration = analysisPage.addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, DateRange.THIS_YEAR.toString());
        metricConfiguration.collapseConfiguration();

        metricConfiguration = analysisPage.addMetric(ATTR_ACCOUNT, FieldType.ATTRIBUTE).getMetricsBucket()
                .getMetricConfiguration("Count of " + ATTR_ACCOUNT).expandConfiguration();

        assertTrue(metricConfiguration.isFilterByDateButtonVisible(), "Filter By Date button is still added");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterByDateWithMaximumAtributeFilters() {
        final List<String> attributeFilterList = asList(ATTR_ACCOUNT, ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY,
                ATTR_IS_ACTIVE, ATTR_IS_CLOSED, ATTR_IS_WON, ATTR_OPP_SNAPSHOT, ATTR_OPPORTUNITY, ATTR_PRODUCT,
                ATTR_REGION, ATTR_SALES_REP, ATTR_STAGE_NAME, ATTR_STATUS);

        final MetricConfiguration metricAmountConfig, metricAvgAmountConfig;

        analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT);

        metricAmountConfig = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        attributeFilterList.stream().forEach(attribute -> metricAmountConfig.addFilterWithAllValue(attribute));

        metricAvgAmountConfig = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AVG_AMOUNT)
                .expandConfiguration();
        attributeFilterList.stream().forEach(attribute -> metricAvgAmountConfig.addFilterWithAllValue(attribute));

        metricAmountConfig.expandConfiguration();
        assertTrue(metricAmountConfig.isFilterByDateButtonVisible(), "Filter by date button is hidden");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareThreeInsights() {
        analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).addDate()
                .removeDateFilter()
                .waitForReportComputing()
                .saveInsight(INSIGHT_HAS_DATE_ON_VIEWBY);

        analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).addDateFilter()
                .waitForReportComputing()
                .saveInsight(INSIGHT_HAS_DATE_ON_FILTER_SECTION);

        analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).addDate()
                .waitForReportComputing()
                .saveInsight(INSIGHT_HAS_DATE_ON_VIEWBY_AND_FILTER_SECTION);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void updateDateDimension() {
        analysisPage = initAnalysePage();
        MetricConfiguration metricConfiguration;
        DateDimensionSelect dateDatasetSelect;
        AbstractReactDropDown abstractReactDropDown;

        analysisPage.openInsight(INSIGHT_HAS_DATE_ON_VIEWBY).waitForReportComputing();

        dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();
        abstractReactDropDown = dateDatasetSelect.selectByName(DATE_DATASET_CREATED);

        assertEquals(abstractReactDropDown.getSelection(), DATE_DATASET_CREATED);

        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();

        assertEquals(metricConfiguration.expandFilterByDate().getDateDimension(), DATE_DATASET_CREATED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void selectDateDimensionWithoutAppliedDateFilter() {
        initAnalysePage().openInsight(INSIGHT_HAS_DATE_ON_VIEWBY).waitForReportComputing().removeAttribute("Date")
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .expandFilterByDate().changeDateDimension(DATE_DATASET_CREATED).getDateDimension(), DATE_DATASET_CREATED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareTwoInsights() {
        analysisPage = initAnalysePage();
        analysisPage.addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CREATED, DateRange.THIS_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_A_MEASURE);

        analysisPage = initAnalysePage();
        analysisPage.addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CREATED, DateRange.THIS_YEAR.toString());

        analysisPage.addMetric(METRIC_AVG_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AVG_AMOUNT)
                .expandConfiguration()
                .addFilterByDate(DATE_DATASET_CREATED, DateRange.THIS_YEAR.toString());
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_MANY_MEASURE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOpenAsReportButtonDisabled() {
        analysisPage = initAnalysePage();
        AnalysisPageHeader pageHeader = analysisPage.getPageHeader();
        analysisPage.openInsight(INSIGHT_HAS_A_MEASURE).waitForReportComputing().getPageHeader();

        assertFalse(pageHeader.isExportButtonEnabled(), "This Export button isn't disabled");
        assertEquals(pageHeader.getExportButtonTooltipText(), EXPORT_TOOLTIP_CONTENT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOpenAsReportButtonEnabled() {
        analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_MANY_MEASURE).waitForReportComputing();

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration()
                .removeFilterByDate();

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_AVG_AMOUNT)
                .expandConfiguration()
                .removeFilterByDate();

        checkingOpenAsReport("testOpenAsReportButtonEnabled");
    }
}
