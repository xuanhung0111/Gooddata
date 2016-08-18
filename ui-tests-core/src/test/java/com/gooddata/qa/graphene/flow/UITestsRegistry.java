package com.gooddata.qa.graphene.flow;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

import com.gooddata.qa.graphene.dashboards.DashboardSavedFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesCascadingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesConnectingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardAllKindsFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDefaultFilterMultipleChoiceTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterDropdownAttributeValueTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterGroupTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesAdvancedConnectingFilterTest;
import com.gooddata.qa.graphene.filters.DashboardFilterVisualTest;
import com.gooddata.qa.graphene.i18n.LocalizationTest;
import com.gooddata.qa.graphene.project.SimpleProjectEtlTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportsTest;
import com.gooddata.qa.utils.flow.GdcTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Set<Object> tests = new HashSet<>();

        for (String suite: args) {
            if ("basic".equals(suite)) {
                tests.addAll(asList(
                    SimpleProjectEtlTest.class,
                    GoodSalesDashboardTest.class,
                    GoodSalesReportsTest.class,
                    new GdcTest("testng-imap-GoodSales-email-schedule.xml")
                        .param("GRAPHENE_USER", "gd.scheduledemail@gmail.com")
                        .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),
                    new GdcTest(LocalizationTest.class)
                        .param("LANGUAGE_CODE", "fr-FR")
                    /**
                     * @TODO: this test needs to be removed from basic test until the issue with wrong link 
                     * (use backend instead client-demo)
                     */
//                    "testng-imap-project-n-users-sanity-test.xml"
                ));
            } else if ("filters".equals(suite)) {
                tests.addAll(asList(
                    GoodSalesDashboardAllKindsFiltersTest.class,
                    GoodSalesFilterDropdownAttributeValueTest.class,
                    GoodSalesCascadingFilterTest.class,
                    GoodSalesConnectingFilterTest.class,
                    GoodSalesFilterGroupTest.class,
                    DashboardFilterVisualTest.class,
                    DashboardSavedFiltersTest.class,
                    GoodSalesAdvancedConnectingFilterTest.class
                ));
            } else if ("default-filter-feature".equals(suite)) {
                tests.addAll(asList(
                    GoodSalesDefaultFilterMultipleChoiceTest.class
                ));
            }
        }

        TestsRegistry.getInstance()
            .register(tests)
            .toTextFile();
    }
}
