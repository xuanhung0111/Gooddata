package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class EmbeddedAdTest extends GoodSalesAbstractTest {

    private static final String EMBEDDED_URI = "analyze/embedded/#/%s/reportId/edit";
    private static final String PERMISSION_ERROR_MESSAGE = "SORRY, YOU DON'T HAVE ACCESS TO THIS PAGE.";
    private static final String IFRAME_WRAPPER_URL = "https://gdc.sitina.net/wrapper.html";

    @BeforeClass(alwaysRun = true)
    public void initializeProject() {
        projectTitle += "Embedded-Ad-Test";
    }

    @Test(dependsOnGroups = { "createProject" })
    public void viewEnbeddedAdUsingEditorRole() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            embedAdToWrapperPage(getEmbeddedAdUrl());
            assertTrue(getEmbeddedAnalysisPage().isEmbeddedPage(), "Embeded AD page is not loaded");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void viewEnbeddedAdUsingViewerRole() throws JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            embedAdToWrapperPage(getEmbeddedAdUrl());
            takeScreenshot(browser, "Test-Embedded-Ad-Using-Viewer-Role", getClass());
            assertEquals(EmbeddedAnalysisPage.getErrorMessage(browser), PERMISSION_ERROR_MESSAGE,
                    "Expected error message is not displayed");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testNoExportButton() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        assertFalse(getEmbeddedAnalysisPage().getPageHeader().isExportButtonPresent(),
                "Export button is added to embedded AD");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testNoHeaderBar() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        // wait until main editor is loaded
        getEmbeddedAnalysisPage();
        assertFalse(isElementPresent(className(ApplicationHeaderBar.ROOT_LOCATOR), browser),
                "Header bar is displayed");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testNoAddDataButton() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        assertFalse(getEmbeddedAnalysisPage().isAddDataButtonPresent(),
                "Add Data button is added to embedded AD");
    }

    @DataProvider(name = "chartTypeProvider")
    public Object[][] chartTypeProvider() {
        return new Object[][] {
            { ReportType.COLUMN_CHART },
            { ReportType.BAR_CHART },
            { ReportType.LINE_CHART }
        };
    }

    @Test(dependsOnGroups = { "createProject" }, dataProvider = "chartTypeProvider")
    public void testChartReportRender(ReportType type) {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(type)
                .waitForReportComputing();

        takeScreenshot(browser, "Test-Chart-Report-Render-On-Embedded-Ad-" + type.getLabel(), getClass());
        assertTrue(getEmbeddedAnalysisPage().getChartReport().getTrackersCount() >= 1, "Chart is not rendered");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testTableReportRender() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        takeScreenshot(browser, "Test-Table-Report-Render-On-Embedded-Ad", getClass());
        final TableReport table = getEmbeddedAnalysisPage().getTableReport();
        assertTrue(table.getContent().size() >= 1 && CollectionUtils.isEqualCollection(table.getHeaders(),
                singletonList(METRIC_NUMBER_OF_ACTIVITIES.toUpperCase())), "Table is not rendered");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testChartRenderUsingDateFilter() throws ParseException {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDateFilter()
                .getFilterBuckets()
                .configDateFilter("01/01/2015", "12/31/2015");

        assertEquals(getEmbeddedAnalysisPage().waitForReportComputing().getChartReport().getDataLabels(),
                singletonList("5"), "Chart does not render correctly");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testChartRenderUsingAttributeFilter() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");

        assertEquals(getEmbeddedAnalysisPage().waitForReportComputing().getChartReport().getTrackersCount(), 2,
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testChartRenderUsingAttributeFilterOnMetricConfiguration() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration()
                .addFilter(ATTR_DEPARTMENT, "Direct Sales");

        assertEquals(getEmbeddedAnalysisPage().waitForReportComputing().getChartReport().getDataLabels(),
                asList("21,615", "22,088", "33,420", "23,931"), "Chart does not render correctly");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testChartRenderUsingRecommendation() {
        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();

        // use any recommendation
        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(getEmbeddedAnalysisPage().waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                "Chart does not render correctly");
        assertEquals(getEmbeddedAnalysisPage().getFilterBuckets().getDateFilterText(),"Activity: Last 4 quarters");
        assertEquals(getEmbeddedAnalysisPage().getAttributesBucket().getSelectedDimensionSwitch(),"Activity");
        assertEquals(getEmbeddedAnalysisPage().getAttributesBucket().getSelectedGranularity(),"Quarter");
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws org.apache.http.ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private String getEmbeddedAdUrl() {
        return getRootUrl() + format(EMBEDDED_URI, testParams.getProjectId());
    }

    private void embedAdToWrapperPage(final String url) {
        browser.get(IFRAME_WRAPPER_URL);
        final WebElement urlTextBox = waitForElementVisible(By.id("url"), browser);
        urlTextBox.sendKeys(url);
        // clicking on go button is not stable
        urlTextBox.submit();

        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
    }

    private EmbeddedAnalysisPage getEmbeddedAnalysisPage() {
        return EmbeddedAnalysisPage.getInstance(browser);
    }
}
