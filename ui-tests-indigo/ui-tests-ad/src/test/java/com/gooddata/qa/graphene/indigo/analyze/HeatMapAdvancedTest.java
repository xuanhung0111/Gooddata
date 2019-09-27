package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class HeatMapAdvancedTest extends AbstractAnalyseTest {
    private final String INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE = "Insight has Metric and two Attribute" + generateHashString();
    private final String INSIGHT_HAS_METRIC_AND_ATTRIBUTTE = "Insight has Metric and Attribute" + generateHashString();
    private final String INSIGHT_HAS_RESTRICTED_FACT = "Insight has restricted fact" + generateHashString();
    private final String INSIGHT_HAS_PROTECTED_ATTRIBUTE = "Insight has protected attribute" + generateHashString();
    private final String INSIGHT_TEST = "Insight test" + generateHashString();
    private String sourceProjectId;
    private String targetProjectId;
    private IndigoRestRequest indigoRestRequest;
    private FactRestRequest factRestRequest;
    private AttributeRestRequest attributeRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "HeatMap Advanced Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createOppFirstSnapshotMetric();
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, true);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    protected void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createHeatMapHasRestrictedFact() throws IOException {
        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        try {
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.HEAT_MAP).addMetric(METRIC_AMOUNT, FieldType.FACT)
                    .waitForReportComputing().saveInsight(INSIGHT_HAS_RESTRICTED_FACT);

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            initAnalysePage().openInsight(INSIGHT_HAS_RESTRICTED_FACT).waitForReportComputing();
            analysisPage.exportTo(OptionalExportMenu.File.XLSX);
            ExportXLSXDialog.getInstance(browser).confirmExport();
            assertEquals(ElementUtils.getErrorMessage(browser), "You cannot export this insight because it contains restricted data.");
            Screenshots.takeScreenshot(browser, "createHeatMapHasRestrictedFact", getClass());

        } finally {
            factRestRequest.unsetFactRestricted(factUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createHeatMapHasProtectedAttribute() throws IOException {
        String attributeUri = attributeRestRequest.getAttributeByTitle(ATTR_ACTIVITY_TYPE).getUri();
        try {
            attributeRestRequest.setAttributeProtected(attributeUri);
            initAnalysePage().changeReportType(ReportType.HEAT_MAP).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addAttribute(ATTR_ACTIVITY_TYPE)
                    .waitForReportComputing().saveInsight(INSIGHT_HAS_PROTECTED_ATTRIBUTE);

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            initAnalysePage().openInsight(INSIGHT_HAS_PROTECTED_ATTRIBUTE).waitForReportComputing();
            assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\n" +
                    "Contact your administrator.");
            Screenshots.takeScreenshot(browser, "createHeatMapHasProtectedAttribute", getClass());
        } finally {
            attributeRestRequest.unsetAttributeProtected(attributeUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    protected void testInteractionWithLegendLabelAndTooltip() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        ChartReport chartReport = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_DEPARTMENT).waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTrackerLegends(), 7);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Email"), asList("Department", "Direct Sales"), asList("# of Activities", "21,615")));
        assertEquals(chartReport.getYaxisLabels(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
        assertEquals(chartReport.getXaxisLabels(), asList("Direct Sales", "Inside Sales"));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testImportedAndExportedProject() {
        createInsightHasMetricAndTwoAttributesOnRowAndColumn(INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        String exportToken = exportProject(true, true, true, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE)
                    .waitForReportComputing().getChartReport();
            Screenshots.takeScreenshot(browser, "Test Imported and Exported Project", getClass());
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList("Activity Type", "Email"), asList("Department", "Direct Sales"), asList("# of Activities", "21,615")));
            assertEquals(chartReport.getTrackersCount(), 8);
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = "testImportedAndExportedProject")
    protected void testImportedAndExportedPartialProject() {
        String insight = createInsightHasMetricAndAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        String exportToken = exportPartialProject(insight, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE)
                    .waitForReportComputing().getChartReport();
            Screenshots.takeScreenshot(browser, "Imported and Exported Partial Project", getClass());
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0), asList(asList("Activity Type", "Email"), asList("# of Activities", "33,920")));
            assertEquals(chartReport.getTrackersCount(), 4);
        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnGroups = "createProject")
    protected void testSomeActionOnKD() {
        createInsightHasMetricAndTwoAttributesOnRowAndColumn(INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE,
                METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT);
        createInsightHasMetricAndAttribute(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE).waitForWidgetsLoading()
                .addInsightNext(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE).selectDateFilterByName("All time").waitForWidgetsLoading();
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().searchInsight(INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE),
                "Insight " + INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE + " should be visible");

        Widget firstWidget = indigoDashboardsPage.getFirstWidget(Insight.class);
        Widget lastWidget = indigoDashboardsPage.getLastWidget(Insight.class);

        indigoDashboardsPage.dragWidget(firstWidget, lastWidget, Widget.DropZone.NEXT);
        assertEquals(indigoDashboardsPage.getInsightTitles(),
                asList(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE, INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE));
        firstWidget.setHeadline(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE + " RENAME");
        assertEquals(firstWidget.getHeadline(), INSIGHT_HAS_METRIC_AND_ATTRIBUTTE + " RENAME");

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_HAS_METRIC_AND_TWO_ATTRIBUTE);
        indigoDashboardsPage.deleteInsightItem();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(INSIGHT_HAS_METRIC_AND_ATTRIBUTTE + " RENAME"));
    }

    @Test(dependsOnGroups = "createProject")
    protected void testSomeActionsWithInsightHasNotRelateDateAndAttributeUnderMeasure() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing().saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY, "Email with 1 Source Consulting on Apr-27-08")
                .waitForWidgetsLoading().selectDateFilterByName("All time");
        indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_TEST).clickOnContent();

        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertEquals(configurationPanel.getErrorMessage(),
                "The insight cannot be filtered by Date. Unselect the check box.");
        configurationPanel.disableDateFilter();
        assertEquals(configurationPanel.getErrorMessage(),
                "The insight cannot be filtered by Activity. Unselect the check box.");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testSomeActionsWithInsightHasNotRelateDateUnderMeasure() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilterByDate(DATE_DATASET_ACTIVITY, "01/01/2015", "01/01/2019");

        analysisPage.waitForReportComputing().saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
                .waitForWidgetsLoading().selectDateFilterByName("All time");
        indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_TEST).clickOnContent();

        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel isn't checked");
        assertFalse(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel is disabled, unchangeable");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testLocalizationOnAD() {
        try {
            initAccountPage().changeLanguage("Français");

            initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
                    .addStack(ATTR_DEPARTMENT).waitForReportComputing();

            log.info("date fileter" + analysisPage.addDateFilter().getFilterBuckets().openDateFilterPickerPanel().getWarningUnsupportedMessage());

            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "AUCUNE MESURE DANS VOTRE PERCEPTION\n" +
                    "Ajoutez une mesure à votre perception, ou basculez vers la vue de tableau.\n" +
                    "Une fois l'opération terminée, vous pourrez l'enregistrer.");
            assertEquals(analysisPage.getPageHeader().clickOptionsButton().getExportButtonTooltipText(),
                    "La perception est incompatible avec l'Éditeur de rapports. Pour l'ouvrir en tant que rapport," +
                            " sélectionnez un autre type de perception.");
            assertEquals(getTooltipFromElement(ReportType.HEAT_MAP.getLocator(), browser), "Carte thermique");
        } finally {
            initAccountPage().changeLanguage("English US");
        }
    }

    @Test(dependsOnGroups = "createProject")
    protected void testLocalizationTooLargeReport() {
        try {
            initAccountPage().changeLanguage("Français");

            initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            analysisPage.addMetricByAttribute(ATTR_ACCOUNT).addAttribute(ATTR_ACTIVITY).waitForReportComputing();
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "TROP DE POINTS DE DONNÉES À AFFICHER\n" +
                    "Essayez d'appliquer un ou plusieurs filtres.");
        } finally {
            initAccountPage().changeLanguage("English US");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testLocalizationInComputedChartInsight() {
        try {
            initAccountPage().changeLanguage("Français");

            initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing();
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(),
                    "DÉSOLÉ, NOUS NE POUVONS PAS AFFICHER CETTE PERCEPTION\n" +
                            "Essayez d'appliquer différents filtres, ou d'utiliser des mesures ou attributs différents.\n" +
                            "Si ceci n'a pas aidé, contactez votre administrateur.");
        } finally {
            initAccountPage().changeLanguage("English US");
        }
    }

    @Test(dependsOnGroups = "createProject")
    protected void testLocalizationNoDataInsight() {
        try {
            initAccountPage().changeLanguage("Français");

            initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY).waitForReportComputing();
            analysisPage.addFilter(ATTR_ACCOUNT).getFilterBuckets().configAttributeFilter(ATTR_ACCOUNT, "101 Financial");
            analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");
            assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "AUCUNE DONNÉE POUR VOTRE SÉLECTION DE FILTRE\n" +
                    "Essayez d'ajuster ou de supprimer certains des filtres.");
        } finally {
            initAccountPage().changeLanguage("English US");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithInsightHasRelateDateFilterUnderMeasure() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        analysisPage.saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY_TYPE, "Email").waitForWidgetsLoading()
                .selectDateFilterByName("All time");
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_TEST);

        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel is checked");
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel isn't disabled, unchangeable");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithInsightHasDateOnBucketFilter() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();

        analysisPage.addDateFilter().getFilterBuckets().configDateFilter("01/01/2010", "01/01/2019");
        analysisPage.saveInsight(INSIGHT_TEST);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST).waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
                .selectDateFilterByName("All time");

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_TEST);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel is checked");
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel isn't disabled, unchangeable");
    }

    @Test(dependsOnGroups = {"createProject"})
    protected void testSomeActionsWithInsightHasNotDateOnFilterBucket() {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().saveInsight("INSIGHT TEST");

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight("INSIGHT TEST").waitForWidgetsLoading();
        indigoDashboardsPage.addAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
                .selectDateFilterByName("All time");

        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, "INSIGHT TEST");
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(), "Date checkbox on right panel is checked");
        assertTrue(configurationPanel.isDateFilterCheckboxEnabled(),
                "Date checkbox on right panel isn't disabled, unchangeable");
    }

    @Test(dependsOnGroups = "createProject")
    protected void testDeprecated() throws IOException {
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().saveInsight("INSIGHT TEST");
        String visualizationUri = "";
        visualizationUri = indigoRestRequest.updateDeprecated(ReportType.HEAT_MAP.getLabel(), 1, visualizationUri);
        try {
            initAnalysePage().openInsight("INSIGHT TEST").waitForReportComputing();
            assertEquals(analysisPage.getDeprecatedMessage(), "This insight cannot be displayed in its original format because it is not available." +
                    " We used table view to show the insight.");
        } finally {
            indigoRestRequest.updateDeprecated(ReportType.HEAT_MAP.getLabel(), 0, visualizationUri);
        }
    }

    @Test(dependsOnGroups = "createProject")
    protected void testShowAndHideDataByTags() {
        initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES).getDialogTagName().addTagNameToMetric("metrictag");
        initAnalysePage().changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        openAnalyzeEmbeddedPage("excludeObjectsWithTags", "metrictag");
        analysisPage.getCatalogPanel().search(METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(analysisPage.getCatalogPanel().isEmpty(), "Catalogue panel should be empty");
        assertEquals(analysisPage.getCatalogPanel().getEmptyMessage(), "No data matching\n\"" + METRIC_NUMBER_OF_ACTIVITIES + "\"");
        openAnalyzeEmbeddedPage("includeObjectsWithTags", "metrictag");
        analysisPage.getCatalogPanel().search(METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(analysisPage.getCatalogPanel().getFieldNamesInViewPort(), asList(METRIC_NUMBER_OF_ACTIVITIES));
    }

    private String createInsightHasMetricAndTwoAttributesOnRowAndColumn(String title, String metric, String attribute, String stack) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.HEAT_MAP)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        CategoryBucket.Type.ROWS),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                                        CategoryBucket.Type.COLUMNS))));
    }

    private String createInsightHasMetricAndAttribute(String title, String metric, String attribute) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(title, ReportType.HEAT_MAP)
                .setMeasureBucket(singletonList(
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                .setCategoryBucket(singletonList(
                        CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.ATTRIBUTE))));
    }
}
