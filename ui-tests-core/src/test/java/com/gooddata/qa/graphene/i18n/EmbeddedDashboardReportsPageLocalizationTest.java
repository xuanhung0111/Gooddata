package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;

public class EmbeddedDashboardReportsPageLocalizationTest extends GoodSalesAbstractLocalizationTest {

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-embeded-dashboard-reports-page-localization-test";
    }

    @Test(dependsOnMethods = {"createAndUsingTestUser"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        embeddedUri = initDashboardsPage()
            .openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkMoveDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenMoveDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDeleteDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenDeleteDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkPermissionDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenPermissionDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkFolderDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .clickAddFolderButton();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkOpenExistingReport() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .getReportsList()
            .openReport(REPORT_ACTIVITIES_BY_TYPE);
        EmbeddedReportPage.waitForPageLoaded(browser);
        checkLocalization(browser);

        EmbeddedReportPage reportPage = Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser));
        reportPage.openOptionsMenu();
        checkLocalization(browser);

        reportPage.openWhatPanel();
        checkLocalization(browser);

        reportPage.openHowPanel();
        checkLocalization(browser);

        reportPage.openFilterPanel();
        checkLocalization(browser);

        reportPage.showConfiguration();
        checkLocalization(browser);

        reportPage.showCustomNumberFormat();
        checkLocalization(browser);

        MetricFormatterDialog formatterDialog = reportPage.openMetricFormatterDialogFromConfiguration();
        checkLocalization(browser);
        formatterDialog.discard();

        reportPage.showMoreReportInfo();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkCancelCreateNewReport() {
        EmbeddedReportPage reportPage = initEmbeddedDashboard()
            .openEmbeddedReportPage();

        reportPage.openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .doneSndPanel()
            .waitForReportExecutionProgress();
        reportPage.cancel();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkOpenNewReport() {
        EmbeddedReportPage reportPage = initEmbeddedDashboard()
            .openEmbeddedReportPage();
        checkLocalization(browser);

        reportPage.openWhatPanel()
            .selectMetric(METRIC_NUMBER_OF_ACTIVITIES);
        reportPage.clickDeleteInSndMetricDetail();
        checkLocalization(browser);

        waitForElementVisible(cssSelector(".c-confirmDeleteDialog button[class*='s-btn-cancel']"), browser).click();
        MetricEditorDialog metricEditorDialog = reportPage.clickEditInSndMetricDetail();
        checkLocalization(browser);
        metricEditorDialog.back();
        browser.switchTo().defaultContent();

        reportPage.clickAddNewSimpleMetric();
        checkLocalization(browser);

        reportPage.clickAddAdvanceMetric();
        browser.switchTo().frame(waitForElementVisible(By.tagName("iframe"), browser));
        metricEditorDialog = Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser));

        metricEditorDialog.clickShareMetricLink();
        checkLocalization(browser);
        metricEditorDialog.back();

        metricEditorDialog.clickDifferentMetricLink();
        checkLocalization(browser);
        metricEditorDialog.back();

        metricEditorDialog.clickRatioMetricLink();
        checkLocalization(browser);
        metricEditorDialog.back();

        metricEditorDialog.clickCustomMetricLink();
        checkLocalization(browser);
        metricEditorDialog.cancel();
        browser.switchTo().defaultContent();
    }
}
