package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;

public class GoodSalesDashboardRestrictedFacts extends GoodSalesAbstractTest {

    private static final long expectedTabularReportExportPDFSize = 26000L;
    private static final long expectedTabularReportExportCSVSize = 175L;
    private FactRestRequest factRestRequest;
    private String factUri;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-restricted-facts-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getReportCreator().createAmountByProductReport();
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        factUri = factRestRequest.getFactByTitle(FACT_AMOUNT).getUri();
        factRestRequest.setFactRestricted(factUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDashboardRenderedCorrectly() {
        initDashboardsPage();
        waitForDashboardPageLoaded(browser);
        // no red bar is showed over restricted data
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportRestrictedReport() {
        ReportPage my_report = initReportsPage()
                .openReport(REPORT_AMOUNT_BY_PRODUCT);

        // export to pdf
        my_report.exportReport(ExportFormat.PDF_PORTRAIT);
        checkRedBar(browser);
        verifyReportExport(ExportFormat.PDF_PORTRAIT, REPORT_AMOUNT_BY_PRODUCT, expectedTabularReportExportPDFSize);

        // export to csv
        my_report.exportReport(ExportFormat.CSV);
        waitForElementVisible(BY_RED_BAR, browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportReportAfterUnsetFactRestricted() {
        try {
            factRestRequest.unsetFactRestricted(factUri);
            ReportPage my_report = initReportsPage()
                    .openReport(REPORT_AMOUNT_BY_PRODUCT);

            // export to csv
            my_report.exportReport(ExportFormat.CSV);
            checkRedBar(browser);
            verifyReportExport(ExportFormat.CSV, REPORT_AMOUNT_BY_PRODUCT, expectedTabularReportExportCSVSize);
        } finally {
            factRestRequest.setFactRestricted(factUri);
        }
    }
}
