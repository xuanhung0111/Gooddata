package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.RowHeader;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

public class DashboardHeadersTest extends AbstractDashboardTest {
    private static String TEST_INSIGHT = "Test-Insight";
    private ProjectRestRequest projectRestRequest;
    private KpiConfiguration kpi;

    @FindBy(id = IndigoDashboardsPage.MAIN_ID)
    private IndigoDashboardsPage indigoDashboardsClient;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard-Header-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_SECTION_HEADERS, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_CHANGE_LANGUAGE, true);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createInsightWidget(new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART).setMeasureBucket(
                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)))));
        kpi = new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CLOSED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        initIndigoDashboardsPage().addDashboard();
        indigoDashboardsPage.addKpiToBeginningOfRow(kpi);
        indigoDashboardsPage.addInsightToCreateANewRow(TEST_INSIGHT, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
                .addInsightToCreateANewRow(TEST_INSIGHT, METRIC_AMOUNT, Widget.FluidLayoutPosition.BOTTOM)
                .saveEditModeWithWidgets();
    }

    @DataProvider
    public Object[][] inputText() {
        return new Object[][] { { "12AbcTest", 0, "12AbcTest", "12AbcTest", false },
                { "!@#$%^^&*", 1, "!@#$%^^&*", "!@#$%^^&*", false }, { "  Test", 2, "Test", "Test", true } };
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "inputText")
    public void checkNormalHeaderRows(String newTitle, int index, String titleResult, String descriptionResult, boolean scroll) {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
        RowHeader rowheader = indigoDashboardsPage.getRows().get(index).getRowHeader();
        rowheader.changeDashboardRowTitle(newTitle, scroll);
        rowheader.changeDashboardRowDescription(newTitle, scroll);
        assertEquals(rowheader.getHeaderRowInEditMode(), titleResult);
        assertEquals(rowheader.getDescriptionRowInEditMode(), descriptionResult);
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkRedBar(browser);
        assertEquals(rowheader.getHeaderRowInViewMode(), titleResult);
        assertEquals(rowheader.getDescriptionRowInViewMode(), descriptionResult);
    }

    @Test(dependsOnMethods = "checkNormalHeaderRows")
    public void checkEditBlankHeaderRows() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
        RowHeader rowheader = indigoDashboardsPage.getRows().get(0).getRowHeader();
        rowheader.changeDashboardRowTitle(" ", false);
        rowheader.changeDashboardRowDescription(" ", false);
        assertEquals(rowheader.getHeaderRowInEditMode(), "Add Title here...");
        assertEquals(rowheader.getDescriptionRowInEditMode(), "Add Description here...");
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkRedBar(browser);
        assertEquals(indigoDashboardsPage.getRows().get(0).getRowHeader().hasHeader(), false);
        assertEquals(indigoDashboardsPage.getRows().get(0).getRowHeader().hasDescription(), false);
    }

    @Test(dependsOnMethods = "checkEditBlankHeaderRows")
    public void checkLocalization() {
        try {
            initAccountPage().changeLanguage("Français");
            initIndigoDashboardsPageWithWidgets().switchToEditMode();
            indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
            RowHeader rowheader = indigoDashboardsPage.getRows().get(0).getRowHeader();
            rowheader.changeDashboardRowTitle("", false);
            rowheader.changeDashboardRowDescription("", false);
            assertEquals(rowheader.getHeaderRowInEditMode(), "Ajouter un titre ici...");
            assertEquals(rowheader.getDescriptionRowInEditMode(), "Ajouter une description ici...");
        } finally {
            initAccountPage().changeLanguage("English US");
        }
    }

    @DataProvider
    public Object[][] inputLimitCharacter() {
        return new Object[][]{{"/string-content/limit256characters.txt", "/string-content/limit1024characters.txt",
                "/string-content/limit256characters_result.txt", "/string-content/limit1024characters_result.txt", 0}};
    }

    @Test(dependsOnMethods = "checkLocalization", dataProvider = "inputLimitCharacter")
    public void checkLimitCharacterInHeaderRows(String newTitle, String newDescription, String newTitleResult
            , String newDescriptionResult, int index) {
        String contentTitle = getResourceAsString(newTitle);
        String contentTitleResult = getResourceAsString(newTitleResult);
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
        RowHeader rowheader = indigoDashboardsPage.getRows().get(index).getRowHeader();
        rowheader.changeDashboardRowTitle(contentTitle, false);
        rowheader.clickOnTitle();
        assertEquals(getBubbleMessage(browser), "0 / 256 characters left");
        String contentDescription = getResourceAsString(newDescription);
        String contentDescriptionResult = getResourceAsString(newDescriptionResult);
        rowheader.clickOnDescription();
        rowheader.changeDashboardRowDescription(contentDescription, false);
        rowheader.clickOnDescription();
        assertEquals(getBubbleMessage(browser), "0 / 1024 characters left");
        indigoDashboardsPage.saveEditModeWithWidgets();
        checkRedBar(browser);
        assertEquals(rowheader.getHeaderRowInViewMode(), contentTitleResult);
        assertEquals(rowheader.getDescriptionRowInViewMode(), contentDescriptionResult);
    }

    @Test(dependsOnMethods = "checkLimitCharacterInHeaderRows")
    public void removeWidgetTest() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        indigoDashboardsPage.waitForDashboardLoad().waitForWidgetsLoading();
        RowHeader rowheader = indigoDashboardsPage.getRows().get(0).getRowHeader();
        indigoDashboardsPage.selectWidgetByHeadline(Kpi.class, METRIC_AMOUNT).delete();
        assertEquals(rowheader.hasHeader(), false);
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT).clickDeleteButton();
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, TEST_INSIGHT).clickDeleteButton();
        assertTrue(indigoDashboardsPage.getDashboardBodyText().contains("Get Started"), "Dashboard does not empty!!!");
    }
}
