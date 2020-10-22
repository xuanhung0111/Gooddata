package com.gooddata.qa.graphene.indigo.sdk;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_NO_DATA_INSIGHT;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATIONS;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_BY_IDENTIFIER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_BY_URI;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_ABSOLUTE_DATE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.TEMPLATE_VISUALIZATION_WITH_NEGATIVE_FILTER;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.WARNING_CAN_NOT_DISPLAY;
import static com.gooddata.qa.graphene.utils.ReactSKDUtils.WARNING_NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.sdk.SDKAnalysisPage;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.sdk.common.AbstractReactSdkTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class EmbeddedHeadlineTest extends AbstractReactSdkTest {

    private IndigoRestRequest indigoRestRequest;
    private DashboardRestRequest dashboardRestRequest;

    @Override
    protected void customizeProject() {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject", groups = "hasData")
    public void login() {
        signInFromReact(UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void embedHeadlineInsight() throws IOException {
        String headline = "Headline " + generateHashString();
        String insightUri = createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        createCatalogJSON(Pair.of("visualizationName", headline), Pair.of("visualizationUrl", insightUri));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271");

        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_URI);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271");
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void embedHeadlineInsights() throws IOException {
        String headline = "Headline " + generateHashString();
        String tableReport = "Table " + generateHashString();
        createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        createInsight(tableReport, ReportType.TABLE, METRIC_AMOUNT);
        Graphene.waitGui().until(browser -> indigoRestRequest.getAllInsightNames().contains(tableReport));
        File catalogJSON = createCatalogJSON(Pair.of("firstVisualizationName", headline),
                Pair.of("secondVisualizationName", tableReport));
        exportCatalogJSON(catalogJSON);
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATIONS);
        SDKAnalysisPage sdkAnalysisPage = initSDKAnalysisPage();
        assertEquals(sdkAnalysisPage.getHeadline().getPrimaryItem(), "154,271");
        assertEquals(sdkAnalysisPage.getPivotTableReport().getHeaders(), singletonList(METRIC_AMOUNT));
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void updateHeadlineInsight() throws IOException {
        String headline = "Headline " + generateHashString();
        createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        createCatalogJSON(Pair.of("visualizationName", headline));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        initAnalysePage().openInsight(headline).addMetricToSecondaryBucket(METRIC_AMOUNT)
                .changeReportType(ReportType.TABLE).saveInsight();
        assertEquals(initSDKAnalysisPage().getPivotTableReport().getHeaders(),
                asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_AMOUNT));

        initAnalysePage().openInsight(headline).removeMetric(METRIC_AMOUNT).changeReportType(ReportType.HEADLINE).saveInsight();
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271");
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void changeFormatEmbeddedHeadlineInsight() throws IOException {
        String headline = "Headline " + generateHashString();
        createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), Formatter.DEFAULT.toString());
        createCatalogJSON(Pair.of("visualizationName", headline));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        try {
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES).getMetricFormat(),
                    Formatter.DEFAULT.toString());
            assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "154,271.00");
        } finally {
            dashboardRestRequest.changeMetricFormat(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri(), "#,##0");
        }
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void deleteHeadlineInsight() throws IOException {
        String headline = "Headline " + generateHashString();
        createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        createCatalogJSON(Pair.of("visualizationName", headline));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_BY_IDENTIFIER);
        indigoRestRequest.deleteObjectsUsingCascade(indigoRestRequest.getInsightUri(headline));
        assertThat(initSDKAnalysisPage().getWarning(), isUIsdk8() ? containsString(WARNING_NOT_FOUND) : containsString(WARNING_CAN_NOT_DISPLAY));
    }

    @Test(dependsOnMethods = "login", groups = "hasData")
    public void filterEmbedHeadlineInsight() throws IOException {
        String headline = "Headline " + generateHashString();
        createInsight(headline, ReportType.HEADLINE, METRIC_NUMBER_OF_ACTIVITIES);
        createCatalogJSON(Pair.of("visualizationName", headline),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, "Direct Sales")),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_NEGATIVE_FILTER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "53,217");

        createCatalogJSON(Pair.of("visualizationName", headline),
                Pair.of("elementAttributeUri", getAttributeElementUri(ATTR_DEPARTMENT, "Direct Sales")),
                Pair.of("attributeUri", getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri()));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_FILTER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "101,054");

        createCatalogJSON(Pair.of("visualizationName", headline),
                Pair.of("dateAttributeName", "Date (Created)"),
                Pair.of("from", "2010-01-01"),
                Pair.of("to", "2011-01-01"));
        replaceContentAppJSFrom(TEMPLATE_VISUALIZATION_WITH_ABSOLUTE_DATE_FILTER);
        assertEquals(initSDKAnalysisPage().getHeadline().getPrimaryItem(), "50,494");
    }

    //Test has to run lastly
    @Test(dependsOnGroups = "hasData")
    public void verifyHeadlineInsightWithNoData() throws IOException {
        replaceContentAppJSFrom(TEMPLATE_NO_DATA_INSIGHT);
        assertThat(initSDKAnalysisPage().getWarning(),
                isUIsdk8() ? containsString(WARNING_NOT_FOUND) : containsString(WARNING_CAN_NOT_DISPLAY));
    }

    private void exportCatalogJSON(File catalogJSON) throws IOException {
        File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory",
                "./target/screenshots/"));
        FileUtils.forceMkdir(mavenProjectBuildDirectory);
        FileUtils.copyFileToDirectory(catalogJSON, mavenProjectBuildDirectory);
    }

    private String createInsight(String title, ReportType type, String metric) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, type).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }
}
