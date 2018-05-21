package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

public class GoodSalesReportsPageTest extends GoodSalesAbstractTest {

    private static final String TAG_REPORT = "New Lost [Drill-In]";
    private static final String TAG_NAME = "GDC";
    private static final String CURRENT_SALES_FOLDER = "Current Sales";
    private static final String FAVORITES_FOLDER = "Favorites";
    private static final String UNSORTED_FOLDER = "Unsorted";
    private static final String ALL_FOLDER = "All";

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createLostMetric();
        getReportCreator().createActivitiesByTypeReport();
        createReport(GridReportDefinitionContent.create(TAG_REPORT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_OPPORTUNITY))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_LOST)))));
        ReportsPage reportsPage = initReportsPage();
        reportsPage
            .addNewFolder(CURRENT_SALES_FOLDER)
            .openReport(TAG_REPORT);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).addTag(TAG_NAME);

        initReportsPage().openFolder(ALL_FOLDER).moveReportsToFolder(CURRENT_SALES_FOLDER, REPORT_ACTIVITIES_BY_TYPE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyReportsPage() {
        ReportsPage reportsPage = initReportsPage();
        assertTrue(isEqualCollection(reportsPage.getAllFolderNames(),
                asList(ALL_FOLDER, FAVORITES_FOLDER, "My Reports", "Unsorted", CURRENT_SALES_FOLDER)));
        assertTrue(isEqualCollection(reportsPage.getGroupByVisibility(), asList("Time", "Author", "Report Name",
                "Folders")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyTagReport() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(ALL_FOLDER);
        assertTrue(reportsPage.isTagCloudVisible());
        assertTrue(reportsPage.getReportsCount() > 1);
        assertEquals(reportsPage.filterByTag(TAG_NAME).getReportsCount(), 1);

        reportsPage.openFolder(CURRENT_SALES_FOLDER);
        assertFalse(reportsPage.isTagCloudVisible());
        assertEquals(reportsPage.getReportsCount(), 1);

        reportsPage.openFolder(ALL_FOLDER);
        assertTrue(reportsPage.deselectAllTags().getReportsCount() > 1);
    }

    @Test(dependsOnMethods = {"verifyTagReport"})
    public void moveReports() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(CURRENT_SALES_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 1);

        reportsPage.openFolder(ALL_FOLDER);
        reportsPage.moveReportsToFolder(CURRENT_SALES_FOLDER, TAG_REPORT);
        reportsPage.openFolder(CURRENT_SALES_FOLDER);

        assertEquals(reportsPage.getReportsCount(), 2);
        assertEquals(reportsPage.moveReportsToFolderByDragDrop(UNSORTED_FOLDER, TAG_REPORT)
                .getReportsCount(), 1);
        reportsPage.openFolder(UNSORTED_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void favoriteReport() {
        ReportsPage reportsPage = initReportsPage();
        waitForFragmentVisible(reportsPage).openFolder(ALL_FOLDER);
        waitForFragmentVisible(reportsPage).addFavorite(TAG_REPORT);

        waitForFragmentVisible(reportsPage).openFolder(FAVORITES_FOLDER);
        assertEquals(reportsPage.getReportsCount(), 1);
    }
}
