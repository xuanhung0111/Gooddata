package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.aqe.ValidElementsResourceTest;
import com.gooddata.qa.graphene.dashboards.DashboardPermissionsTest;
import com.gooddata.qa.graphene.account.GoodSalesMufOnUserProfileTest;
import com.gooddata.qa.graphene.account.InviteUserInOtherDomainsTest;
import com.gooddata.qa.graphene.account.ManageUserTest;
import com.gooddata.qa.graphene.account.UserAccountSettingTest;
import com.gooddata.qa.graphene.account.UserProfileInformationTest;
import com.gooddata.qa.graphene.dashboards.DashboardSavedFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesCascadingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesCellLimitTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesConnectingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardAllKindsFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesEditEmbeddedDashboardTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesEmbeddedDashboardTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardWidgetManipulationTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterDropdownAttributeValueTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterGroupTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesAdvancedConnectingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesKeyMetricTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesPersonalObjectsInDashboardWidgetTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesReportWidgetOnDashboardTest;
import com.gooddata.qa.graphene.filters.DashboardFilterVisualTest;
import com.gooddata.qa.graphene.i18n.LocalizationTest;
import com.gooddata.qa.graphene.manage.AttributeLabelsTest;
import com.gooddata.qa.graphene.manage.ComputedAttributesTest;
import com.gooddata.qa.graphene.manage.GoodSalesFactTest;
import com.gooddata.qa.graphene.manage.GoodSalesFolderTest;
import com.gooddata.qa.graphene.manage.GoodSalesManageObjectsTest;
import com.gooddata.qa.graphene.manage.GoodSalesMetricEditorTest;
import com.gooddata.qa.graphene.manage.GoodSalesMetricNumberFormatterTest;
import com.gooddata.qa.graphene.manage.GoodSalesVariableTest;
import com.gooddata.qa.graphene.manage.GoodSalesViewModelVisualizationTest;
import com.gooddata.qa.graphene.manage.MetricEditorTest;
import com.gooddata.qa.graphene.manage.SimpleProjectGeoLabelTest;
import com.gooddata.qa.graphene.project.CreateAndDeleteProjectTest;
import com.gooddata.qa.graphene.project.ExportAndImportProjectTest;
import com.gooddata.qa.graphene.project.PartialExportAndImportProjectTest;
import com.gooddata.qa.graphene.project.ProjectSwitchingTest;
import com.gooddata.qa.graphene.project.SimpleProjectEtlTest;
import com.gooddata.qa.graphene.project.ValidateProjectTest;
import com.gooddata.qa.graphene.reports.CopyReportTableTest;
import com.gooddata.qa.graphene.reports.DynamicImageTest;
import com.gooddata.qa.graphene.reports.GoodSalesAddingFilterFromReportContextMenuTest;
import com.gooddata.qa.graphene.reports.GoodSalesAdvanceRangeFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesBasicFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesCreateReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillDownToExportSpecialTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportInReportPageTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportToExportTest;
import com.gooddata.qa.graphene.reports.GoodSalesEmbeddedReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesGridModificationTest;
import com.gooddata.qa.graphene.reports.GoodSalesManipulationFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportFilterTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportStatisticsTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportsPageTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportsTest;
import com.gooddata.qa.graphene.reports.GoodSalesRunningTotalsTest;
import com.gooddata.qa.graphene.reports.GoodSalesSaveReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesSortByTotalsTest;
import com.gooddata.qa.graphene.reports.GoodSalesTotalsInReportTest;
import com.gooddata.qa.graphene.reports.GoodsalesMufReportTest;
import com.gooddata.qa.graphene.reports.ReportWithEmptyValuesInTimeDimensionTest;
import com.gooddata.qa.graphene.reports.SimpleCompAttributesTest;
import com.gooddata.qa.graphene.reports.TimeFormattingTest;
import com.gooddata.qa.graphene.schedules.GoodSalesScheduleDialogFiltersTest;
import com.gooddata.qa.graphene.schedules.GoodSalesScheduleDialogRecurrenceTest;
import com.gooddata.qa.graphene.rolap.GoodSalesMetadataDeletedTest;
import com.gooddata.qa.utils.flow.PredefineParameterTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object[]> suites = new HashMap<>();

        suites.put("basic", new Object[] {
            SimpleProjectEtlTest.class,
            GoodSalesDashboardTest.class,
            GoodSalesReportsTest.class,

            new PredefineParameterTest("testng-imap-GoodSales-email-schedule.xml")
                .param("GRAPHENE_USER", "gd.scheduledemail@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),

            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "fr-FR"),

            "testng-imap-project-n-users-sanity-test.xml"
        });

        suites.put("basic-vertica", new Object[] {
            new PredefineParameterTest(SimpleProjectEtlTest.class)
                .param("PROJECT_AUTHORIZATION_TOKEN", "$PROJECT_AUTHORIZATION_TOKEN2"),

            GoodSalesDashboardTest.class,
            GoodSalesReportsTest.class
        });

        suites.put("filters", new Object[] {
            GoodSalesDashboardAllKindsFiltersTest.class,
            GoodSalesFilterDropdownAttributeValueTest.class,
            GoodSalesCascadingFilterTest.class,
            GoodSalesConnectingFilterTest.class,
            GoodSalesFilterGroupTest.class,
            DashboardFilterVisualTest.class,
            DashboardSavedFiltersTest.class,
            GoodSalesAdvancedConnectingFilterTest.class
        });

        suites.put("reports", new Object[] {
            GoodSalesDrillReportTest.class,
            GoodSalesDrillReportToExportTest.class,
            GoodSalesDrillDownToExportSpecialTest.class,
            GoodSalesDrillReportInReportPageTest.class,
            GoodSalesTotalsInReportTest.class,
            GoodSalesSortByTotalsTest.class,
            GoodSalesRunningTotalsTest.class,
            GoodSalesReportFilterTest.class,
            GoodSalesBasicFilterReportTest.class,
            GoodSalesManipulationFilterReportTest.class,
            GoodSalesAdvanceRangeFilterReportTest.class,
            GoodSalesAddingFilterFromReportContextMenuTest.class,
            GoodsalesMufReportTest.class,
            GoodSalesGridModificationTest.class,
            SimpleCompAttributesTest.class,
            GoodSalesReportStatisticsTest.class,
            CopyReportTableTest.class,
            GoodSalesReportsPageTest.class,
            GoodSalesSaveReportTest.class,
            GoodSalesCreateReportTest.class,
            ReportWithEmptyValuesInTimeDimensionTest.class,
            DynamicImageTest.class,
            TimeFormattingTest.class,
            GoodSalesEmbeddedReportTest.class
        });

        suites.put("dashboards", new Object[] {
            GoodSalesDashboardAllKindsFiltersTest.class,
            GoodSalesFilterDropdownAttributeValueTest.class,
            GoodSalesCascadingFilterTest.class,
            GoodSalesConnectingFilterTest.class,
            GoodSalesAdvancedConnectingFilterTest.class,
            GoodSalesFilterGroupTest.class,
            DashboardFilterVisualTest.class,
            GoodSalesDashboardWidgetManipulationTest.class,
            DashboardSavedFiltersTest.class,
            GoodSalesKeyMetricTest.class,
            GoodSalesEmbeddedDashboardTest.class,
            GoodSalesPersonalObjectsInDashboardWidgetTest.class,
            GoodSalesReportWidgetOnDashboardTest.class,
            DashboardPermissionsTest.class,
            GoodSalesEditEmbeddedDashboardTest.class
        });

        suites.put("dashboards-cell-limit", new Object[] {
            GoodSalesCellLimitTest.class
        });

        suites.put("manage", new Object[] {
            GoodSalesManageObjectsTest.class,
            GoodSalesFactTest.class,
            GoodSalesViewModelVisualizationTest.class,
            GoodSalesMetricNumberFormatterTest.class,
            AttributeLabelsTest.class,
            SimpleProjectGeoLabelTest.class,
            ComputedAttributesTest.class,
            GoodSalesFolderTest.class,
            GoodSalesVariableTest.class,
            MetricEditorTest.class,
            GoodSalesMetricEditorTest.class,
            "testng-manage-aggregation-metric-test.xml",
            "testng-manage-different-granularity-logical-metric-test.xml",
            "testng-manage-filter-share-ratio-metric-test.xml",
            "testng-manage-numeric-metric-test.xml",
            "testng-manage-non-UI-metric-test.xml"
        });

        suites.put("schedules", new Object[] {
            new PredefineParameterTest("testng-imap-GoodSales-email-schedule-full.xml")
                .param("GRAPHENE_USER", "gd.scheduledemail@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),
            new PredefineParameterTest("testng-imap-GoodSales-email-unsubscribe.xml")
                .param("GRAPHENE_USER", "gd.scheduledemail@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),
            GoodSalesScheduleDialogRecurrenceTest.class,
            GoodSalesScheduleDialogFiltersTest.class,
            "testng-imap-GoodSales-email-schedule-dashboard.xml"
        });

        suites.put("misc", new Object[] {
            GoodSalesMetadataDeletedTest.class,
            ValidElementsResourceTest.class
        });

        suites.put("localization", new Object[] {
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "en-US"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "es-ES"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "fr-FR"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "de-DE"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "nl-NL"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "ja-JP"),
            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "pt-BR"),
            "testng-embedded-dashboard-localization-test.xml"
        });

        suites.put("project-and-user", new Object[] {
            CreateAndDeleteProjectTest.class,
            UserProfileInformationTest.class,

            // Need use this user for test user account section include changing user info,
            // password, reset password.
            // This will not affect the other test when there are some failure
            new PredefineParameterTest(UserAccountSettingTest.class)
                .param("GRAPHENE_USER", "gd.accsetting@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),

            "testng-imap-register-and-delete-user.xml",
            ProjectSwitchingTest.class,
            ManageUserTest.class,
            "testng-imap-invite-user-basic.xml",
            "testng-imap-invite-user-with-muf.xml",
            InviteUserInOtherDomainsTest.class,
            ExportAndImportProjectTest.class,
            PartialExportAndImportProjectTest.class,
            ValidateProjectTest.class,
            GoodSalesMufOnUserProfileTest.class
        });

        suites.put("extended-project-and-user", new Object[] {
            // Need use this user for test user account section include changing user info,
            // password, reset password.
            // This will not affect the other test when there are some failure
            new PredefineParameterTest("testng-imap-reset-password.xml")
                .param("GRAPHENE_USER", "gd.accsetting@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD")
                .param("IMAP_USER", "gd.accsetting@gmail.com"),

            "testng-imap-invite-non-register-user.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
