package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;

import java.io.IOException;

import com.gooddata.qa.graphene.enums.report.ExportFormat;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;

import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.utils.http.fact.FactRestUtils;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;

public class GoodSalesDashboardRestrictedFacts extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Expected + Won";
    private static final String REPORT_FILE_NAME = "Expected _ Won";

    private static final long expectedTabularReportExportPDFSize = 20000L;
    private static final long expectedTabularReportExportCSVSize = 50L;

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-restricted-facts-test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void setupPrecondition() throws ParseException, JSONException, IOException {
        FactRestUtils.setFactRestricted(getRestApiClient(), getProject(), getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT)));
    }

    @Test(dependsOnMethods = {"setupPrecondition"}, groups = {"restricted-fact"})
    public void checkDashboardRenderedCorrectly() {
        initDashboardsPage();
        waitForDashboardPageLoaded(browser);
        // no red bar is showed over restricted data
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"setupPrecondition"}, groups = {"restricted-fact"})
    public void exportRestrictedReport() {
        ReportPage my_report = initReportsPage()
                .openFolder("My Reports")
                .openReport(REPORT_NAME);

        // export to pdf
        my_report.exportReport(ExportFormat.PDF_PORTRAIT);
        checkRedBar(browser);
        verifyReportExport(ExportFormat.PDF_PORTRAIT, REPORT_FILE_NAME, expectedTabularReportExportPDFSize);

        // export to csv
        my_report.exportReport(ExportFormat.CSV);
        waitForElementVisible(BY_RED_BAR, browser);
    }

    @Test(dependsOnGroups = {"restricted-fact"}, groups = {"unrestricted-fact"})
    public void unsetRestrictedFact() throws ParseException, JSONException, IOException {
        FactRestUtils.unsetFactRestricted(getRestApiClient(), getProject(), getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT)));
    }

    @Test(dependsOnMethods = {"unsetRestrictedFact"}, groups = {"unrestricted-fact"})
    public void exportReport() {
        ReportPage my_report = initReportsPage()
                .openFolder("My Reports")
                .openReport(REPORT_NAME);

        // export to csv
        my_report.exportReport(ExportFormat.CSV);
        checkRedBar(browser);
        verifyReportExport(ExportFormat.CSV, REPORT_FILE_NAME, expectedTabularReportExportCSVSize);
    }
}
