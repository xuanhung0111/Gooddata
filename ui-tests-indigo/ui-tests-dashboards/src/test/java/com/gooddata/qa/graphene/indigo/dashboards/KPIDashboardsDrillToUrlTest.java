package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillCustomUrlDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;

import org.json.JSONException;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_WARNING_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_ERROR_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

public class KPIDashboardsDrillToUrlTest extends AbstractDashboardTest {

    private String hyperlinkType = "GDC.link";
    private String geoPinType = "GDC.geo.pin";
    private String projectID = "#project_id";
    private String dashboardID = "#dashboard_id";
    private String widgetID = "#widget_id";
    private String insightID = "#insight_id";

    private final String COLUMN_CHART_ONLY_MEASURES = "Column chart has only measures";
    private final String COLUMN_CHART_HAS_NOT_HYPERLINK = "Column chart has not url hyperlink";
    private final String COLUMN_CHART_HAS_HYPERLINK = "Column chart has url hyperlink";
    private final String COLUMN_CHART_HAS_MANY_HYPERLINKS = "Column chart has many url hyperlinks";
    private final String COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK = "Column chart has long attribute url hyperlinks";
    private final String COLUMN_CHART_REMOVE_MEASURE = "Column chart to test remove measure which is set drill to Url on KD";
    private final String COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE = "Column chart with only Department is attribute";
    private final String COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE = "Column chart to test change attribute type from hyperlink";
    private final String COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL = "Column chart to test remove attribute which used in custom URL";
    private final String COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER = "Column chart has drill url and drill down";

    private final String DASHBOARD_COLUMN_CHART_ONLY_MEASURES = "Dashboard with Column chart has only measures";
    private final String DASHBOARD_COLUMN_CHART_HAS_NOT_HYPERLINK = "Dashboard with Column chart has not url hyperlink";
    private final String DASHBOARD_COLUMN_CHART_HAS_HYPERLINK = "Dashboard hyperlink";
    private final String DASHBOARD_COLUMN_CHART_HAS_MANY_HYPERLINKS = "Dashboard two hyperlinks";
    private final String DASHBOARD_COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK = "Dashboard long hyperlink";
    private final String DASHBOARD_COLUMN_CHART_SOME_ATTRIBUTES_HAS_HYPERLINK = "Dashboard many config";
    private final String DASHBOARD_COLUMN_CHART_REMOVE_MEASURE = "Removed measure";
    private final String DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE = "Removed attribute";
    private final String DASHBOARD_COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE = "Updated attribute type";
    private final String DASHBOARD_COLUMN_CHART_HYPERLINK_EMBEDDED = "Checked Embedded";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_IDENTIFIER = "Url custom identifier";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_INSIGHTS = "Url custom insights";
    private final String DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL = "Invalid custom url";
    private final String DASHBOARD_COLUMN_CHART_CHANGE_DRILL_CONFIG = "Hyperlink to insight";
    private final String DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_HYPERLINK = "Custom to hyperlink";
    private final String DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_DRILL_DASHBOARD = "Custom to dashboard";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_CONTAIN_INDENTIFIER = "Contain Identifier";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_CONTAIN_INSIGHTS = "Contain Insights";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_NOT_CONTAIN_INDENTIFIER = "Not contain Identifier";
    private final String DASHBOARD_COLUMN_CHART_CUSTOM_URL_WITHOUT_PROTOCOL = "Without protocol";
    private final String DASHBOARD_COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER = "Drill picker";
    private final String DASHBOARD_COLUMN_CHART_DRILL_URL_TO_DRILL_DASHBOARD = "Updated drill hyperlink to drill to dashboard";
    private final String DASHBOARD_COLUMN_CHART_DRILL_URL_TO_CUSTOM_URL = "Updated drill hyperlink to drill to custom url";

    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private ChartReport chartReport;
    private ConfigurationPanel configurationPanel;
    AttributeRestRequest attributeRestRequest;

    @Override
    protected void customizeProject() throws JSONException, IOException {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createAmountBOPMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        
        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_DEPARTMENT, hyperlinkType);
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_REGION, hyperlinkType);
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_PRODUCT, hyperlinkType);

        createColumnChart(COLUMN_CHART_HAS_HYPERLINK, asList(METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_OPPORTUNITY, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_ONLY_MEASURES, asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE), asList());
        createColumnChart(COLUMN_CHART_HAS_NOT_HYPERLINK, asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE), 
            asList(Pair.of(ATTR_SALES_REP, CategoryBucket.Type.VIEW),Pair.of(ATTR_STAGE_NAME, CategoryBucket.Type.VIEW))); 
        createColumnChart(COLUMN_CHART_HAS_MANY_HYPERLINKS, asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE), 
            asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.STACK),Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW))); 
        createColumnChart(COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK, asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE), 
            asList(Pair.of(ATTR_PRODUCT, CategoryBucket.Type.STACK)));
        createColumnChart(COLUMN_CHART_REMOVE_MEASURE, asList(METRIC_AMOUNT_BOP, METRIC_BEST_CASE), asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE, asList(METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE, asList(METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL, asList(METRIC_AMOUNT_BOP), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER, asList(METRIC_AMOUNT_BOP), 
            asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW), Pair.of(ATTR_YEAR_CLOSE, CategoryBucket.Type.VIEW)));

        attributeRestRequest.setAttributeName(ATTR_PRODUCT, "Product that is renamed to test the long attribute name");
    }

    @DataProvider(name = "insightHasNotHyperlink")
    public Object[][] getInsightHasNotHyperlink() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_ONLY_MEASURES, COLUMN_CHART_ONLY_MEASURES},
            {
                DASHBOARD_COLUMN_CHART_HAS_NOT_HYPERLINK, COLUMN_CHART_HAS_NOT_HYPERLINK}
        };
    }

    @DataProvider(name = "insightHasHyperlink")
    public Object[][] getInsightHasHyperlink() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_HAS_HYPERLINK, COLUMN_CHART_HAS_HYPERLINK, ATTR_OPPORTUNITY,
                "www.google.com", "Should drill to Url with correctly url."},
            {
                DASHBOARD_COLUMN_CHART_HAS_MANY_HYPERLINKS, COLUMN_CHART_HAS_MANY_HYPERLINKS, ATTR_DEPARTMENT,
                "intgdc.com/dashboards/Direct%20Sales", "Should drill to Url with correctly url."},
            {
                DASHBOARD_COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK, COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK, "Product that is renamed to test",
                "intgdc.com/dashboards/CompuSci", "Should drill to Url with correctly url."},
            {
                DASHBOARD_COLUMN_CHART_CHANGE_DRILL_CONFIG, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE, ATTR_DEPARTMENT,
                "intgdc.com/dashboards/Direct%20Sales", "Should drill to Url with correctly url."},
        };
    }

    @DataProvider(name = "configDrillToUrlHyperlink")
    public Object[][] getConfigDrillToUrlHyperlink() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_REMOVE_MEASURE, COLUMN_CHART_REMOVE_MEASURE, ATTR_DEPARTMENT },
            {
                DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE, ATTR_DEPARTMENT },
            {
                DASHBOARD_COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE, COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE, ATTR_REGION },
            {
                DASHBOARD_COLUMN_CHART_HYPERLINK_EMBEDDED, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE, ATTR_DEPARTMENT },
        };
    }

    @DataProvider(name = "configDrillToCustomUrl")
    public Object[][] getConfigDrillToCustomUrl() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_HYPERLINK, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE },
            {
                DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_DRILL_DASHBOARD, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE },
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "insightHasNotHyperlink")
    public void drillToUrlHasNotHyperlink(String dashboard, String insight) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboard).addInsight(insight).selectWidgetByHeadline(Insight.class, insight);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillHasNotUrlHyperlink(METRIC_AMOUNT_BOP);
        assertFalse(configurationPanel.hasHyperlink(), "Should not allow to drill to URL Hyperlink without hyperlink attribute.");
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "insightHasHyperlink")
    public void drillToUrlHyperlink(String dashboard, String insigh, String attribute, String urlCompare, String messageCompare) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboard).addInsight(insigh).selectWidgetByHeadline(Insight.class, insigh);
        
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, attribute);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("10/09/2010", "11/11/2010").apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insigh).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(urlCompare);
            assertTrue(isDirectToGoodData, messageCompare);
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnMethods = "drillToUrlHyperlink")
    public void editDrillHiperlinkToDrillInsight() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_HAS_HYPERLINK).waitForWidgetsLoading();
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK);
            configurationPanel = indigoDashboardsPage.getConfigurationPanel().clickRemoveMetricDrillInteractions();
            configurationPanel.drillIntoInsight(METRIC_AMOUNT_BOP, COLUMN_CHART_ONLY_MEASURES);
            indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
    }

    @Test(dependsOnGroups = "createProject" , description = "This test case covered the editing from drill to hyperlink url to drill to dashboard")
    public void editDrillHiperlinkToDrillDashboard() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_DRILL_URL_TO_DRILL_DASHBOARD)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
                    
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, ATTR_DEPARTMENT);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel().clickRemoveMetricDrillInteractions();
        configurationPanel.drillIntoDashboard(METRIC_AMOUNT_BOP, DASHBOARD_COLUMN_CHART_HAS_HYPERLINK);
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK).getChartReport();
        indigoDashboardsPage.waitForWidgetsLoading();
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the editing from drill to hyperlink url to custom url")
    public void editDrillHiperlinkToDrillCustomUrl() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_DRILL_URL_TO_CUSTOM_URL)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);    
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, ATTR_DEPARTMENT);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel().clickRemoveMetricDrillInteractions();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).addDashboardID().apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
    }

    @Test(dependsOnMethods = "drillToUrlHyperlink")
    public void deleteDrillHiperlinkConfig() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK).waitForWidgetsLoading();
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK);
            configurationPanel = indigoDashboardsPage.getConfigurationPanel().clickRemoveMetricDrillInteractions();
            indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_LONG_ATTRIBUTE_HAS_HYPERLINK).getChartReport();
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should not allow to drill to Url.");
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling with many hyperlinks")
    public void drillConfigManyUrlHyperlink() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_SOME_ATTRIBUTES_HAS_HYPERLINK)
            .addInsight(COLUMN_CHART_HAS_MANY_HYPERLINKS)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_MANY_HYPERLINKS);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, ATTR_DEPARTMENT);
        configurationPanel.drillIntoUrlHyperlink(METRIC_BEST_CASE, ATTR_REGION);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_MANY_HYPERLINKS).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/Direct%20Sales");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnMethods = "drillConfigManyUrlHyperlink")
    public void deleteAllDrillHiperlinkConfig() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_SOME_ATTRIBUTES_HAS_HYPERLINK).waitForWidgetsLoading();
            indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_MANY_HYPERLINKS);
            configurationPanel = indigoDashboardsPage.getConfigurationPanel();
            configurationPanel.clickRemoveMetricDrillInteractions().clickRemoveMetricDrillInteractions();
            indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_MANY_HYPERLINKS).getChartReport();
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should not allow to drill to Url.");
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "configDrillToUrlHyperlink")
    public void prepareDashboardWithConfigDrillToUrl(String dashboard, String insight, String attribute) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboard).addInsight(insight).selectWidgetByHeadline(Insight.class, insight);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, attribute);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
    } 

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToUrl" , 
        description = "This test case covered the emoving measure which is set drill drill to Url on KD")
    public void removeMeasureWhichSetDrill() {
        initAnalysePage().openInsight(COLUMN_CHART_REMOVE_MEASURE).waitForReportComputing().removeMetric(METRIC_AMOUNT_BOP).saveInsight().waitForReportComputing();
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_REMOVE_MEASURE).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_MEASURE).getChartReport();
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should not allow to drill to Url.");
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_MEASURE);
        assertEquals(waitForElementVisible(BY_WARNING_MESSAGE_BAR, browser).getText(), "Some interactions were removedShow more");
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToUrl" , 
        description = "This test case covered the removing attribute which is used in URL as a hyperlink label from insight")
    public void removeAttributeWhichSetDrill() {
        initAnalysePage().openInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).waitForReportComputing()
            .removeAttribute(ATTR_DEPARTMENT).saveInsight().waitForReportComputing();
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        assertEquals(waitForElementVisible(BY_ERROR_MESSAGE_BAR, browser).getText(),"Failed to load URL.");
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        assertEquals(waitForElementVisible(BY_WARNING_MESSAGE_BAR, browser).getText(),"Some interactions were removedShow more");
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should not allow to drill to Url.");
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToUrl" , 
        description = "This test case covered the changing attribute which is used in URL as hyperlink label to other label, such as Text")
    public void changeAttributeHyperlink() throws IOException {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE).waitForWidgetsLoading();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/East%20Coast");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
        attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_REGION, geoPinType);
        try {
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_CHANGE_ATTRIBUTE_TYPE);
        assertEquals(waitForElementVisible(BY_WARNING_MESSAGE_BAR, browser).getText(), "Some interactions were removedShow more");
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should not allow to drill to Url.");
        } finally {
            attributeRestRequest.setHyperlinkTypeForAttribute(ATTR_REGION, hyperlinkType);
        }
    }

    @Test(dependsOnMethods = "prepareDashboardWithConfigDrillToUrl", description = "This test case covered the drilling to Url on Embedded mode")
    public void drillUrlHyperlinkOnEmbeddedMode() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_HYPERLINK_EMBEDDED).waitForWidgetsLoading();
        chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL).waitForWidgetsLoading();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/embedded/Direct%20Sales");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel().clickRemoveMetricDrillInteractions();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog drillCustomUrlDialog = DrillCustomUrlDialog.getInstance(browser);
        String insightIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(insightID).toLowerCase();
        drillCustomUrlDialog.addInsightID().apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(insightIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }
    
    @Test(dependsOnGroups = "createProject", description = "This test case covered the removing attribute which used as a parameter in custom URL")
    public void removeAttributeUsedCustomUrl() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL)
            .addInsight(COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL);         
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).addInsight(ATTR_REGION).apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        
        initAnalysePage().openInsight(COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL).waitForReportComputing()
            .removeAttribute(ATTR_REGION).saveInsight().waitForReportComputing();
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL).waitForWidgetsLoading();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        assertEquals(waitForElementVisible(BY_ERROR_MESSAGE_BAR, browser).getText(), "Failed to load URL.");
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_ATTRIBUTE_CUSTOM_URL);
        assertEquals(waitForElementVisible(BY_WARNING_MESSAGE_BAR, browser).getText(), "Some drill into URL interactions were disabledShow more");
        assertTrue(configurationPanel.isWainingIcon(), "Appear warning icon.");
        assertEquals(configurationPanel.getWarningText(), "Invalid URL parameters");
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling to custom Url that only contains Identifiers")
    public void drilltoCustomUrlByIdentifier() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_IDENTIFIER)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);    
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog drillCustomUrlDialog = DrillCustomUrlDialog.getInstance(browser);
        String projectIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(projectID).toLowerCase();
        drillCustomUrlDialog.addProjectID().apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(projectIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        String insightIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(insightID).toLowerCase();
        drillCustomUrlDialog.addInsightID().apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(insightIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        String widgetIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(widgetID).toLowerCase();
        drillCustomUrlDialog.addWidgetID().apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(widgetIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        String dashboardIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(dashboardID).toLowerCase();
        drillCustomUrlDialog.addDashboardID().apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains(dashboardIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling to custom Url that only contain insight identifiers")
    public void drilltoCustomUrlByInsights() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_INSIGHTS)
            .addInsight(COLUMN_CHART_REMOVE_MEASURE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_MEASURE);         
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).addInsight(ATTR_DEPARTMENT).apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_REMOVE_MEASURE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/Direct%20Sales");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "configDrillToCustomUrl")
    public void prepareDashboardHasCustomUrl(String dashboard, String insight) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboard).addInsight(insight).selectWidgetByHeadline(Insight.class, insight);         
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).addDashboardID().apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
    }

    @Test(dependsOnMethods = "prepareDashboardHasCustomUrl", description = "This test case covered the changing from drill to custom Url to hyperlink")
    public void editDrillCustomUrlToDrillHiperlink() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_HYPERLINK).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoUrlHyperlink(METRIC_AMOUNT_BOP, ATTR_DEPARTMENT);
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("intgdc.com/dashboards/Direct%20Sales");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnMethods = "prepareDashboardHasCustomUrl", description = "This test case covered the changing from drill to custom Url tto drill to dashboard")
    public void editDrillCustomUrlToDrillDashboard() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_CHANGE_CUSTOM_TO_DRILL_DASHBOARD).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoDashboard(METRIC_AMOUNT_BOP, DASHBOARD_COLUMN_CHART_HAS_HYPERLINK);
        indigoDashboardsPage.saveEditModeWithWidgets();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK).getChartReport();
        indigoDashboardsPage.waitForWidgetsLoading();
    }

    @Test(dependsOnMethods = "editDrillHiperlinkToDrillInsight", description = "This test case covered the changing from drill to insight to drill to custom Url")
    public void editDrillInsightToDrillCustomUrl() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_COLUMN_CHART_HAS_HYPERLINK).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.clickRemoveMetricDrillInteractions().drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).addInsight(ATTR_OPPORTUNITY).apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("10/09/2010", "11/11/2010").apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_HAS_HYPERLINK).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("www.google.com");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling custon Url that is inputed by keyboard and contained Identifiers")
    public void drilltoCustomUrlByInputContainIdentifiers() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_CONTAIN_INDENTIFIER)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        indigoDashboardsPage.saveEditModeWithWidgets();

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog drillCustomUrlDialog = DrillCustomUrlDialog.getInstance(browser);
        String dashboardIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(dashboardID);
        String projectIDValue = drillCustomUrlDialog.getToolTipFromVisibilityQuestionIcon(projectID);
        String domain = testParams.getHost();
        drillCustomUrlDialog.inputUrl("https://" + domain + "/dashboards/#/project/{project_id}/dashboard/{dashboard_id}").apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("project/" + projectIDValue + "/dashboard/" + dashboardIDValue);
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", 
        description = "This test case covered the drilling custom Url that is inputed by keyboard and contained insight identifiers")
    public void drilltoCustomUrlByInputContainInsights() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_CONTAIN_INSIGHTS)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        indigoDashboardsPage.saveEditModeWithWidgets();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).inputUrl("https://www.google.com/search?q={attribute_title(label.owner.department)}").apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("https://www.google.com/search?q=Direct%20Sales");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling custom Url that has not contained identifier")
    public void drilltoCustomUrlHasNotIdentifier() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_NOT_CONTAIN_INDENTIFIER)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        indigoDashboardsPage.saveEditModeWithWidgets();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).inputUrl("https://www.google.com").apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("www.google.com");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling to custom Url that is not contained protocol")
    public void drilltoCustomUrlWithoutProtocol() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_CUSTOM_URL_WITHOUT_PROTOCOL)
            .addInsight(COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        indigoDashboardsPage.saveEditModeWithWidgets();
        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).inputUrl("google.com").apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_WITH_DEPARTMENT_ATTRIBUTE).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.clickOnElement(Pair.of(0, 0));
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("google.com");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject", description = "This test case covered the drilling with drill down to show drill picker")
    public void DrillUrlWithDrillPicker() {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(DASHBOARD_COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER)
            .addInsight(COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER)
            .selectWidgetByHeadline(Insight.class, COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER);
        configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.drillIntoCustomUrl(METRIC_AMOUNT_BOP);
        DrillCustomUrlDialog.getInstance(browser).inputUrl("google.com").apply();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_DRILL_URL_WITH_DRILL_PICKER).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Should allow to drill to Url.");
        chartReport.openDrillingPicker(Pair.of(0, 0)).drillToUrl();
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("google.com");
            assertTrue(isDirectToGoodData, "Should drill to Url with correctly url."); 
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    private String createColumnChart(String insightTitle, List<String> metricsTitle,
        List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.COLUMN_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList()))); 
    }
}
