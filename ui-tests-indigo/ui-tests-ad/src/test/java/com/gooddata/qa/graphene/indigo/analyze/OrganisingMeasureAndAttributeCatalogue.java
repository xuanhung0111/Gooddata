package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.manage.DataPage;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.springframework.util.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.Collections;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_HISTORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.project.ProjectRestRequest.AD_CATALOG_GROUPING;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class OrganisingMeasureAndAttributeCatalogue extends AbstractAnalyseTest {

    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private final String TAG_NAME_METRIC = "adgroup_metric_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Metric_kiểmtra";
    private final String TAG_NAME_ATTRIBUTE = "attribute_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Attribute_kiểmtra";
    private final String TAG_NAME_FACT = "adgroup_fact_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Fact_kiểmtra";
    private final String AD_REPORT_LINK = "https://%s/analyze/#/%s/reportId/edit?dataset=%s";
    private final String expectedTagFact = "Fact You can Define A DRill Down Path For Your Report Viewers Users Viewing" +
        " <button>abc</button><script>alert(\\'hello\\')</script>@$%!~&*09fact Kiểmtra";
    private final String expectedTagMetric= "Metric You can Define A DRill Down Path For Your Report Viewers Users" +
        " Viewing <button>abc</button><script>alert(\\'hello\\')</script>@$%!~&*09metric Kiểmtra";
    private final String FIRST_NAME_ATTRIBUTE = "Firstname";
    private final String LAST_NAME_ATTRIBUTE = "Lastname";
    private final String TAG_NAME_UNGROUPED = "Ungrouped";
    private final String PAYROLL_DATASET = "Payroll";
    private final String VALUE_BY_TAG_NAMING_CONVENTION = "ByTagNamingConvention";
    private final String VALUE_BY_FOLDERS = "ByFolders";
    private DashboardRestRequest dashboardRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Organise Measure/Attribute catalogue in AD";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAmountBOPMetric();
        metrics.createAvgAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createLostMetric();
        metrics.createWonMetric();
        metrics.createCloseEOPMetric();
        metrics.createProbabilityMetric();
        metrics.createLostMetric();
        metrics.createCloseEOPMetric();
        metrics.createTimelineBOPMetric();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkGroupNameIsConvertedCorrectly() throws IOException{
        projectRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING, VALUE_BY_TAG_NAMING_CONVENTION);

        initMetricPage().openMetricDetailPage(METRIC_AMOUNT).getDialogTagName().addTagNameToMetric(TAG_NAME_METRIC);
        initObject(METRIC_AMOUNT).addTag(TAG_NAME_FACT);
        initAttributePage().initAttribute(ATTR_ACCOUNT).addTag(TAG_NAME_ATTRIBUTE);

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();
        assertEquals(cataloguePanel.getTextCatalogGroupLabels(), asList(
            expectedTagFact, expectedTagMetric, TAG_NAME_UNGROUPED));

        cataloguePanel.expandCatalogGroupLabels(expectedTagMetric).expandCatalogGroupLabels(expectedTagFact);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList("Date", METRIC_AMOUNT, METRIC_AMOUNT));
    }

    @Test(dependsOnMethods = {"checkGroupNameIsConvertedCorrectly"})
    public void checkCollapseOrExpandGroupTagNaming() {
        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();

        assertFalse(cataloguePanel.isGroupLabelExpanded(expectedTagFact),
            "Should be show collapsed list of groups upon first loading");
        assertFalse(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Should be show collapsed list of groups upon first loading");
        assertFalse(cataloguePanel.isGroupLabelExpanded(TAG_NAME_UNGROUPED),
            "Should be show collapsed list of groups upon first loading");

        cataloguePanel.expandCatalogGroupLabels(expectedTagMetric);

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES);
        assertTrue(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Groups should be keep their expanded state when switching tabs");

        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Groups should be keep their expanded state when changing the insight");

        analysisPage.clear();
        assertTrue(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Groups should be keep their expanded state when clearing insight");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration("InsightSavedNaming", ReportType.TABLE)
                .setMeasureBucket(singletonList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        analysisPage.openInsight("InsightSavedNaming").waitForReportComputing();

        assertTrue(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Groups should be keep their expanded state when opening saved insight");

        cataloguePanel.expandCatalogGroupLabels(expectedTagFact);
        cataloguePanel.search("a");

        assertFalse(cataloguePanel.isGroupLabelExpanded(expectedTagFact),
            "Should be show collapsed list of groups upon first loading");
        assertFalse(cataloguePanel.isGroupLabelExpanded(expectedTagMetric),
            "Should be show collapsed list of groups upon first loading");

        cataloguePanel.clearInputText();
        assertTrue(cataloguePanel.isGroupLabelExpanded(expectedTagFact),
            "The expanded state should be returned back to the state ");

        assertFalse(analysisPage.getPageHeader().isUndoButtonEnabled(),
            "The expand/collapse should be not handled by the undo/redo functionality.");

        cataloguePanel.search("abcxyz");
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"abcxyz\"");
    }

    @Test(dependsOnMethods = {"checkCollapseOrExpandGroupTagNaming"})
    public void searchItemByGroupTagNaming() throws IOException{
        dashboardRequest.setTagToObject(METRIC_BEST_CASE, "adgroup_first_group", FieldType.METRIC);
        dashboardRequest.setTagToObject(METRIC_LOST, "adgroup_second_group", FieldType.METRIC);
        dashboardRequest.setTagToObject(METRIC_WON, "adgroup_third_group", FieldType.METRIC);

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();
        cataloguePanel.search("s");

        assertEquals(cataloguePanel.getTextCatalogGroupLabels(), asList(
            expectedTagFact, "First Group", expectedTagMetric, "Second Group"));

        initMetricPage().openMetricDetailPage(METRIC_BEST_CASE).getDialogTagName()
            .addTagNameToMetric("adgroup_second_group");
        initAnalysePage().getCatalogPanel().search(METRIC_BEST_CASE);

        assertThat(cataloguePanel.getMetricDescriptionAndGroupCatalog(METRIC_BEST_CASE),
            containsString("First Group\nSecond Group"));
    }

    @Test(dependsOnMethods = {"searchItemByGroupTagNaming"})
    public void switchingBetweenProductionAndCSVDatasetByTagName() throws IOException{
        final CsvFile csvFile = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));

        initDataUploadPage().uploadFile(csvFile.getFilePath());
        DataPreviewPage.getInstance(browser).triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar();

        String tagNamePayroll = "adgroup_payroll";
        dashboardRequest.setTagToObject(FIRST_NAME_ATTRIBUTE, tagNamePayroll, FieldType.ATTRIBUTE);

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();

        cataloguePanel.changeDataset(PAYROLL_DATASET);
        takeScreenshot(browser, "Adding-csv-data-payroll", getClass());
        assertEquals(analysisPage.getCatalogPanel().getTextCatalogGroupLabels(), asList("Payroll", TAG_NAME_UNGROUPED));

        final String adReportLink = format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
            "dataset.csv_payroll");

        openUrl(adReportLink);
        AnalysisPage.getInstance(browser).getCatalogPanel().expandCatalogGroupLabels("Payroll")
            .expandCatalogGroupLabels(TAG_NAME_UNGROUPED);

        assertThat(cataloguePanel.getFieldNamesInViewPort(),
            hasItems(FIRST_NAME_ATTRIBUTE, METRIC_AMOUNT, LAST_NAME_ATTRIBUTE));
    }

    @Test(dependsOnMethods = {"switchingBetweenProductionAndCSVDatasetByTagName"})
    public void combineFilterByTagsByTagName() throws IOException{
        dashboardRequest.setTagToObject(METRIC_AMOUNT_BOP, "combinetag", FieldType.METRIC);
        dashboardRequest.setTagToObject(METRIC_AVG_AMOUNT, "combinetag", FieldType.METRIC);

        openAnalyzePage("excludeObjectsWithTags", "combinetag");
        takeScreenshot(browser, "Exclude-Objects-With-Tags", getClass());

        CatalogPanel cataloguePanel = analysisPage.getCatalogPanel();
        assertThat(cataloguePanel.getTextCatalogGroupLabels(), not(hasItems("combinetag")));
        cataloguePanel.search(METRIC_AMOUNT_BOP);
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"" + METRIC_AMOUNT_BOP + "\"");

        analysisPage.getCatalogPanel().search(METRIC_AVG_AMOUNT);
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"" + METRIC_AVG_AMOUNT + "\"");

        openAnalyzePage("includeObjectsWithTags", "combinetag");
        takeScreenshot(browser, "Include-Objects-With-Tags", getClass());
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(METRIC_AMOUNT_BOP, METRIC_AVG_AMOUNT));
    }

    @Test(dependsOnMethods = {"combineFilterByTagsByTagName"})
    public void checkAllObjectsAreGroupedByFolderCorrectly() throws IOException {
        projectRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING, VALUE_BY_FOLDERS);

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();
        assertThat(cataloguePanel.getTextCatalogGroupLabels(), hasItems(
            "Account", "Activity", "Opp. Snapshot", "Opportunity", "Product"));

        String tagNameForFolder = "tagnamefolder";
        initManagePage();
        DataPage dataPage = DataPage.getInstance(browser);
        dataPage.openPage(ObjectTypes.METRIC);
        dataPage.createNewFolder("FolderMetric");
        String folderOppSnapshotUri = dataPage.getUriFolder("FolderMetric");

        initMetricPage().openMetricDetailPage(METRIC_TIMELINE_BOP).moveToFolder("FolderMetric");

        indigoRestRequest.addTagNameForFolder(folderOppSnapshotUri, tagNameForFolder);

        openAnalyzePage("excludeObjectsWithTags", tagNameForFolder);
        Assert.notEmpty(cataloguePanel.expandCatalogGroupLabels("Opp. Snapshot").getFieldNamesInViewPort());

        openAnalyzePage("includeObjectsWithTags", tagNameForFolder);
        assertEquals(cataloguePanel.getNoObjectsFound(), "No objects found.");
    }

    @Test(dependsOnMethods = {"checkAllObjectsAreGroupedByFolderCorrectly"})
    public void checkCollapseOrExpandGroupTagFolders() {
        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();

        assertFalse(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Should be show collapsed list of groups upon first loading");

        cataloguePanel.expandCatalogGroupLabels(ATTR_STAGE_HISTORY);

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES);
        assertTrue(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Groups should be keep their expanded state when switching tabs");

        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Groups should be keep their expanded state when changing the insight");

        analysisPage.clear();
        assertTrue(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Groups should be keep their expanded state when clearing insight");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration("InsightSavedFolder", ReportType.TABLE)
                .setMeasureBucket(singletonList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));
        analysisPage.openInsight("InsightSavedFolder").waitForReportComputing();

        assertTrue(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Groups should be keep their expanded state when opening saved insight");

        cataloguePanel.expandCatalogGroupLabels(ATTR_STAGE_HISTORY);
        cataloguePanel.search("a");

        assertFalse(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Should be show collapsed list of groups upon first loading");
        assertFalse(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "Should be show collapsed list of groups upon first loading");

        cataloguePanel.clearInputText();
        assertTrue(cataloguePanel.isGroupLabelExpanded(ATTR_STAGE_HISTORY),
            "The expanded state should be returned back to the state ");

        assertFalse(analysisPage.getPageHeader().isUndoButtonEnabled(),
            "The expand/collapse should be not handled by the undo/redo functionality.");

        cataloguePanel.search("abcxyz");
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"abcxyz\"");
    }

    @Test(dependsOnMethods = {"checkCollapseOrExpandGroupTagFolders"})
    public void searchItemByFolders() {
        initMetricPage().getObjectFolder().addFolder("metrics", "FirstFolder", null);
        initMetricPage().getObjectFolder().addFolder("metrics", "SecondFolder", null);
        initMetricPage().getObjectFolder().addFolder("metrics", "ThirdFolder", null);

        initMetricPage().openMetricDetailPage(METRIC_AMOUNT).moveToFolder("FirstFolder");
        initMetricPage().openMetricDetailPage(METRIC_AMOUNT_BOP).moveToFolder("SecondFolder");
        initMetricPage().openMetricDetailPage(METRIC_AVG_AMOUNT).moveToFolder("ThirdFolder");

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();
        cataloguePanel.search("f");

        assertThat(cataloguePanel.getTextCatalogGroupLabels(), hasItems("FirstFolder", "SecondFolder", "ThirdFolder"));
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(ATTR_FORECAST_CATEGORY));

        cataloguePanel.clearInputText();
        cataloguePanel.search(METRIC_AMOUNT);
        assertEquals(cataloguePanel.getTextCatalogGroupLabels(), Collections.EMPTY_LIST);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(
            METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT_BOP, METRIC_AVG_AMOUNT));
    }

    @Test(dependsOnMethods = {"searchItemByFolders"})
    public void switchingBetweenProductionAndCSVDatasetByFolder() {
        final CsvFile csvFile = CsvFile.loadFile(
            getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));

        initDataUploadPage().uploadFile(csvFile.getFilePath());
        DataPreviewPage.getInstance(browser).triggerIntegration();
        Dataset.waitForDatasetLoaded(browser);

        DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar();

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();

        cataloguePanel.changeDataset(PAYROLL_DATASET);
        takeScreenshot(browser, "Adding-csv-data-payroll-folder", getClass());
        assertEquals(analysisPage.getCatalogPanel().getTextCatalogGroupLabels(), asList("Payroll"));

        final String adReportLink = format(AD_REPORT_LINK, testParams.getHost(), testParams.getProjectId(),
            "dataset.csv_payroll");

        openUrl(adReportLink);
        AnalysisPage.getInstance(browser).getCatalogPanel().expandCatalogGroupLabels("Payroll");

        assertThat(cataloguePanel.getFieldNamesInViewPort(),
            hasItems(FIRST_NAME_ATTRIBUTE, "Amount", LAST_NAME_ATTRIBUTE));
    }

    @Test(dependsOnMethods = {"switchingBetweenProductionAndCSVDatasetByFolder"})
    public void combineFilterByTagsByFolder() {
        openAnalyzePage("excludeObjectsWithTags", "combinetag");
        takeScreenshot(browser, "Exclude-Objects-With-Tags", getClass());

        CatalogPanel cataloguePanel = analysisPage.getCatalogPanel();
        assertThat(cataloguePanel.getTextCatalogGroupLabels(), not(hasItems("combinetag")));
        cataloguePanel.search(METRIC_AMOUNT_BOP);
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"" + METRIC_AMOUNT_BOP + "\"");

        analysisPage.getCatalogPanel().search(METRIC_AVG_AMOUNT);
        assertTrue(cataloguePanel.isEmpty(), "Catalogue panel should be empty");
        assertEquals(cataloguePanel.getEmptyMessage(), "No data matching\n\"" + METRIC_AVG_AMOUNT + "\"");

        openAnalyzePage("includeObjectsWithTags", "combinetag");
        takeScreenshot(browser, "Include-Objects-With-Tags", getClass());
        cataloguePanel.expandCatalogGroupLabels("SecondFolder").expandCatalogGroupLabels("ThirdFolder");
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(METRIC_AMOUNT_BOP, METRIC_AVG_AMOUNT));
    }

    private FactDetailPage initObject(String factName) {
        initFactPage();
        ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser).selectObject(factName);
        return FactDetailPage.getInstance(browser);
    }
}
