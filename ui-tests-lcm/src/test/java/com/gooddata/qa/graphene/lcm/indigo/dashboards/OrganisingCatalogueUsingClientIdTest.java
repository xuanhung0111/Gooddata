package com.gooddata.qa.graphene.lcm.indigo.dashboards;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;
import static com.gooddata.qa.graphene.enums.DateRange.THIS_YEAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_TASK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.AD_CATALOG_GROUPING;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.ATT_LCM_DATA_PRODUCT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.AbstractTest.Profile;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.manage.DataPage;
import com.gooddata.qa.graphene.fragments.manage.FactDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;

import org.springframework.util.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

public class OrganisingCatalogueUsingClientIdTest extends AbstractProjectTest {
    protected boolean useK8sExecutor = false;

    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String CLIENT_ID = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE = "Client project " + generateHashString();

    private String devProjectId;
    private String clientProjectId;

    private LcmBrickFlowBuilder lcmBrickFlowBuilder;

    private ProjectRestRequest projectRestRequest;
    private ProjectRestRequest projectClientRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private final String TAG_NAME_METRIC = "adgroup_metric_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Metric_kiểmtra";
    private final String TAG_NAME_ATTRIBUTE = "attribute_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Attribute_kiểmtra";
    private final String TAG_NAME_FACT = "adgroup_fact_you__can_define_a_d^rill_down_path_for_your_report_viewers_Users_viewing_" +
        "<button>abc</button><script>alert(\\'Hello\\')</script>^@$%!~&*09Fact_kiểmtra";
    private final String expectedTagFact = "Fact You can Define A DRill Down Path For Your Report Viewers Users Viewing" +
        " <button>abc</button><script>alert(\\'hello\\')</script>@$%!~&*09fact Kiểmtra";
    private final String expectedTagMetric= "Metric You can Define A DRill Down Path For Your Report Viewers Users" +
        " Viewing <button>abc</button><script>alert(\\'hello\\')</script>@$%!~&*09metric Kiểmtra";
    private final String TAG_NAME_UNGROUPED = "Ungrouped";
    private final String VALUE_BY_TAG_NAMING_CONVENTION = "ByTagNamingConvention";
    private final String VALUE_BY_FOLDERS = "ByFolders";
    private DashboardRestRequest dashboardRequest;
    @Override
    protected void initProperties() {
        appliedFixture = GOODSALES;
        validateAfterClass = false;
        projectTitle = "Organising Catalogue";
    }

    @Override
    protected void customizeProject() throws Throwable {
        devProjectId = testParams.getProjectId();
        clientProjectId = createProjectUsingFixture(CLIENT_PROJECT_TITLE, ResourceTemplate.GOODSALES,
                testParams.getDomainUser());

        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAmountBOPMetric();
        metrics.createAvgAmountMetric();
        metrics.createBestCaseMetric();
        metrics.createLostMetric();
        metrics.createWonMetric();
        metrics.createTimelineBOPMetric();

        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        lcmBrickFlowBuilder.setSegmentId(SEGMENT_ID).setClientId(CLIENT_ID)
                .setDevelopProject(devProjectId).setClientProjects(clientProjectId).buildLcmProjectParameters();
        System.out.println("*******************************************************");
        System.out.println(format("* dev project id: %s    *", devProjectId));
        System.out.println(format("* client project id: %s *", clientProjectId));
        System.out.println("*******************************************************");
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), devProjectId);
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), devProjectId);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), devProjectId);
        projectClientRestRequest = new ProjectRestRequest(getAdminRestClient(), clientProjectId);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkGroupName() throws IOException{
        projectRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING.getFlagName(), VALUE_BY_TAG_NAMING_CONVENTION);
        testParams.setProjectId(devProjectId);

        initMetricPage().openMetricDetailPage(METRIC_AMOUNT).getDialogTagName().addTagNameToMetric(TAG_NAME_METRIC);
        initObject(METRIC_AMOUNT).addTag(TAG_NAME_FACT);
        initAttributePage().initAttribute(ATTR_ACCOUNT).addTag(TAG_NAME_ATTRIBUTE);

        dashboardRequest.setTagToObject(METRIC_BEST_CASE, "adgroup_first_group", FieldType.METRIC);
        dashboardRequest.setTagToObject(METRIC_LOST, "adgroup_second_group", FieldType.METRIC);
        dashboardRequest.setTagToObject(METRIC_WON, "adgroup_third_group", FieldType.METRIC);

        lcmBrickFlowBuilder.runLcmFlow();

        testParams.setProjectId(clientProjectId);
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
        projectClientRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING.getFlagName(), VALUE_BY_TAG_NAMING_CONVENTION);

        CatalogPanel cataloguePanel = initAnalysePage().getCatalogPanel();
        assertEquals(cataloguePanel.getTextCatalogGroupLabels(),
                asList(expectedTagFact, "First Group", expectedTagMetric, "Second Group", "Third Group", TAG_NAME_UNGROUPED));

        cataloguePanel.expandCatalogGroupLabels(expectedTagMetric).expandCatalogGroupLabels(expectedTagFact);
        assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList("Date", METRIC_AMOUNT, METRIC_AMOUNT));

        cataloguePanel.search("s");
        assertEquals(cataloguePanel.getTextCatalogGroupLabels(), asList(
            expectedTagFact, "First Group", expectedTagMetric, "Second Group"));

        initMetricPage().openMetricDetailPage(METRIC_BEST_CASE).getDialogTagName()
            .addTagNameToMetric("adgroup_second_group");
        initAnalysePage().getCatalogPanel().search(METRIC_BEST_CASE);

        assertThat(cataloguePanel.getMetricDescriptionAndGroupCatalog(METRIC_BEST_CASE),
            containsString("First Group\nSecond Group"));
    }

    @Test(dependsOnMethods = {"checkGroupName"})
    public void checkAllObjectsAreGroupedByFolder() throws IOException {
        projectRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING.getFlagName(), VALUE_BY_FOLDERS);
        projectClientRestRequest.updateProjectConfiguration(AD_CATALOG_GROUPING.getFlagName(), VALUE_BY_FOLDERS);

        testParams.setProjectId(devProjectId);
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
        initMetricPage().getObjectFolder().addFolder("metrics", "FirstFolder", null);
        initMetricPage().getObjectFolder().addFolder("metrics", "SecondFolder", null);
        initMetricPage().getObjectFolder().addFolder("metrics", "ThirdFolder", null);

        initMetricPage().openMetricDetailPage(METRIC_AMOUNT).moveToFolder("FirstFolder");
        initMetricPage().openMetricDetailPage(METRIC_AMOUNT_BOP).moveToFolder("SecondFolder");
        initMetricPage().openMetricDetailPage(METRIC_AVG_AMOUNT).moveToFolder("ThirdFolder");

        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();

        testParams.setProjectId(clientProjectId);
        CatalogPanel catalogueClientPanel = initAnalysePage().getCatalogPanel();
        openAnalyzePage("excludeObjectsWithTags", tagNameForFolder);
        Assert.notEmpty(catalogueClientPanel.expandCatalogGroupLabels("Opp. Snapshot").getFieldNamesInViewPort());

        openAnalyzePage("includeObjectsWithTags", tagNameForFolder);
        //TODO: Change message Workaround issue BB-2714
        assertEquals(catalogueClientPanel.getNoObjectsFound(), "No data items available.");

        CatalogPanel cataloguePanelSearch = initAnalysePage().getCatalogPanel();
        cataloguePanelSearch.search("f");

        assertThat(cataloguePanelSearch.getTextCatalogGroupLabels(), hasItems("FirstFolder", "SecondFolder", "ThirdFolder"));
        assertEquals(cataloguePanelSearch.getFieldNamesInViewPort(), asList(ATTR_FORECAST_CATEGORY));

        cataloguePanelSearch.clearInputText();
        cataloguePanelSearch.search(METRIC_AMOUNT);
        assertEquals(cataloguePanelSearch.getTextCatalogGroupLabels(), Collections.EMPTY_LIST);
        assertEquals(cataloguePanelSearch.getFieldNamesInViewPort(), asList(
            METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT_BOP, METRIC_AVG_AMOUNT));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(devProjectId);
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }

    private Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    private FactDetailPage initObject(String factName) {
        initFactPage();
        ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser).selectObject(factName);
        return FactDetailPage.getInstance(browser);
    }

}
