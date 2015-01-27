package com.gooddata.qa.graphene.indigo.analyze;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class AnalyticalDesignerMarketingFunnelTest extends AbstractAnalyticalDesignerProjectTest {

    private static final String EMAIL_OPEN = "% Email Open";
    private static final String EMAIL_SENT = "% Emails Sent";
    private static final String CHANNEL = "Channel";
    private static final String CAMPAIGNS = "Campaigns";
    private static final String CLICKTHROUGHS_ID = "ClickthroughsID";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/MarketingFunnelDemo/1";
        projectTitle = "Indigo-Marketing-Funnel-test";

        metric1 = EMAIL_OPEN;
        metric2 = EMAIL_SENT;
        metric3 = EMAIL_OPEN;
        attribute1 = CAMPAIGNS;
        attribute2 = CHANNEL;
        attribute3 = CLICKTHROUGHS_ID;
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {COMPARISON_GROUP})
    public void testComparisonAndPoPAttribute() {
        testComparisonAndPoPAttribute(12);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreAttribute() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(CAMPAIGNS).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Dreamforce Outreach\n")
                .append("Email Promotions\n")
                .append("Top Sellers Blog Series\n");
        assertEquals(analysisPage.getAttributeDescription(CAMPAIGNS), expected.toString());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreMetric() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(EMAIL_OPEN).append("\n")
                .append("Field Type\n")
                .append("Metric\n")
                .append("Defined As\n")
                .append("SELECT Email Status = Open / # of Emails\n");
        assertEquals(analysisPage.getMetricDescription(EMAIL_OPEN), expected.toString());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void compararisonRecommendationOverrideDateFilter() {
        compararisonRecommendationOverrideDateFilter(12);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void filterOnAttribute() {
        filterOnAttribute(CAMPAIGNS + ": Email Promot..., Top Sellers ...",
                "Email Promotions", "Top Sellers Blog Series");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void attributeFilterIsRemovedWhenRemoveAttributeInCatalogue() {
        attributeFilterIsRemovedWhenRemoveAttributeInCatalogue(CAMPAIGNS + ": Email Promot..., Top Sellers ...",
                "Email Promotions", "Top Sellers Blog Series");
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"createProject"}, groups = {CHART_REPORT_GROUP})
    public void verifyChartReport() {
        ReportDefinition reportDefinition = new ReportDefinition()
            .withMetrics(EMAIL_OPEN)
            .withCategories(CAMPAIGNS)
            .withType(ReportType.BAR_CHART)
            .withFilters(DATE);

        verifyChartReport(reportDefinition, Arrays.asList(Arrays.asList(CAMPAIGNS, "Top Sellers Blog Series"),
                Arrays.asList(EMAIL_OPEN, "0.50")));
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"createProject"}, groups = {TABLE_REPORT_GROUP})
    public void verifyTableReportContent() {
        ReportDefinition reportDefinition = new ReportDefinition()
            .withMetrics(EMAIL_OPEN)
            .withCategories(CAMPAIGNS)
            .withType(ReportType.TABLE)
            .withFilters(CHANNEL, DATE);

        verifyTableReportContent(reportDefinition, Arrays.asList("CAMPAIGNS", "% EMAIL OPEN"),
                Arrays.asList(Arrays.asList("Dreamforce Outreach", "0.49"),
                Arrays.asList("Email Promotions", "0.50"),
                Arrays.asList("Top Sellers Blog Series", "0.50")));
    }
}
