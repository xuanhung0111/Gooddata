package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class ExportDashboardTest extends AbstractDashboardWidgetTest {

    private static final long expectedDashboardExportSize = 50000L;
    private final String FIRST_TAB = "First Tab";
    private final String SECOND_TAB = "Second Tab";
    private final String COMPUSCI = "CompuSci";
    private final String EDUCATIONLY = "Educationly";
    private final String DASHBOARD_TEST = "Dashboard Test";
    private final String DASHBOARD_HAVING_REPORT_AND_FILTER = "Dashboard Having Report And Filter";
    private String exportedDashboardName;

    @Override
    protected void customizeProject() throws Throwable {
        getReportCreator().createAmountByProductReport();
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareDashboard() {
        initDashboardsPage().addNewDashboard(DASHBOARD_TEST);
        dashboardsPage.editDashboard()
                .addReportToDashboard(REPORT_AMOUNT_BY_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_PRODUCT).getRoot());
        dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_PRODUCT))
                .changeAttributeFilterValues(COMPUSCI, EDUCATIONLY);
        dashboardsPage.saveDashboard();
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void verifyExportedDashboardPDF() throws IOException {
        if (testParams.isClientDemoEnvironment()) {
            throw new SkipException("There isn't exported feature in client demo environment");
        }
        initDashboardsPage().selectDashboard(DASHBOARD_TEST);
        waitForDashboardPageLoaded(browser);
        exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, FIRST_TAB, expectedDashboardExportSize);

            List<String> contents = asList(getContentFrom(FIRST_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(ATTR_PRODUCT + " " + METRIC_AMOUNT));
            //verify content
            assertThat(contents, hasItems(COMPUSCI, EDUCATIONLY));
            assertThat(contents, hasItems("$27,222,899.64", "$22,946,895.47"));
            //verify filter
            assertThat(contents, not(hasItems("Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid")));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName + "." + ExportFormat.PDF.getName()));
        }
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void exportCopiedDashboard() throws IOException {
        String copiedDashboard = "Copied Dashboard";
        if (testParams.isClientDemoEnvironment()) {
            throw new SkipException("There isn't exported feature in client demo environment");
        }
        initDashboardsPage().selectDashboard(DASHBOARD_TEST)
                .editDashboard()
                .saveAsDashboard(copiedDashboard, false, PermissionType.USE_EXISTING_PERMISSIONS);
        exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, FIRST_TAB, expectedDashboardExportSize);

            List<String> contents = asList(getContentFrom(FIRST_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(format("%s %s", ATTR_PRODUCT, METRIC_AMOUNT)));
            //verify content
            assertThat(contents, hasItems(COMPUSCI, EDUCATIONLY));
            assertThat(contents, hasItems("$27,222,899.64", "$22,946,895.47"));
            //verify filter
            assertThat(contents, not(hasItems("Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid")));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName + "." + ExportFormat.PDF.getName()));
        }
    }

    @Test(dependsOnMethods = "prepareDashboard")
    public void exportDashboardWithDuplicateFilter() throws IOException {
        createDashboardWithDuplicateFilter();
        initDashboardsPage().selectDashboard(DASHBOARD_HAVING_REPORT_AND_FILTER)
                .getFirstFilter().changeAttributeFilterValues(COMPUSCI);
        exportedDashboardName = dashboardsPage.printDashboardTab(0);
        try {
            checkRedBar(browser);
            verifyDashboardExport(exportedDashboardName, SECOND_TAB, expectedDashboardExportSize);

            List<String> contents = asList(getContentFrom(SECOND_TAB).split("\n"));
            //verify report title
            assertThat(contents, hasItem(REPORT_AMOUNT_BY_PRODUCT));
            //verify header title
            assertThat(contents, hasItem(format("%s %s", ATTR_PRODUCT, METRIC_AMOUNT)));
            //verify content
            assertThat(contents, hasItem("CompuSci $27,222,899.64"));
            //verify filter
            assertThat(contents, not(hasItems("Educationly $22,946,895.47")));
            assertThat(contents, hasItem("PRODUCT"));
        } finally {
            deleteIfExists(Paths.get(testParams.getDownloadFolder() + testParams.getFolderSeparator() +
                    exportedDashboardName + "." + ExportFormat.PDF.getName()));
        }
    }

    private void createDashboardWithDuplicateFilter() throws IOException {
        FilterItemContent productFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_PRODUCT));

        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_REPORT_AND_FILTER);
            dash.addTab(Builder.of(Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(productFilter.getId());
                            item.setPosition(TabItem.ItemPosition.RIGHT);
                        }).build();

                        tab.setTitle(SECOND_TAB);
                        tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                            reportItem.setObjUri(getReportByTitle(REPORT_AMOUNT_BY_PRODUCT).getUri());
                            reportItem.setPosition(TabItem.ItemPosition.LEFT);
                            reportItem.setAppliedFilterIds(singletonList(filterItem.getId()));
                        }).build());
                        tab.addItem(filterItem);
                    })
                    .build());
            dash.addFilter(productFilter).addFilter(productFilter);
        }).build();
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(dashboard.getMdObject());
    }
}
