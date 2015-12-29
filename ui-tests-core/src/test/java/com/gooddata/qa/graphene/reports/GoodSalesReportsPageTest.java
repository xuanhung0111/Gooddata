package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

public class GoodSalesReportsPageTest extends GoodSalesAbstractTest {

    private static final String TAG_REPORT = "New Lost [Drill-In]";
    private static final String TAG_NAME = "GDC";
    private static final String CURRENT_SALES_FOLDER = "Current Sales";
    private static final String FAVORITES_FOLDER = "Favorites";
    private static final String ACTIVITY_REPORTS_FOLDER = "Activity Reports";
    private static final String ALL_FOLDER = "All";

    @Test(dependsOnMethods = {"createProject"})
    public void addTagToReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getReportsList().openReport(TAG_REPORT);
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage).addTag(TAG_NAME);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void verifyReportsPage() {
        initReportsPage();
        assertTrue(isEqualCollection(waitForFragmentVisible(reportsPage).getDefaultFolders().getAllFolderNames(),
                asList(ALL_FOLDER, FAVORITES_FOLDER, "My Reports", "Unsorted")));
        assertTrue(isEqualCollection(waitForFragmentVisible(reportsPage).getCustomFolders().getAllFolderNames(),
                asList(ACTIVITY_REPORTS_FOLDER, CURRENT_SALES_FOLDER, "Leaderboards", "Opportunity Historicals",
                        "Outlook Headlines", "Velocity Reports", "Waterfall Analysis", "What's Changed",
                        "_Drill Reports")));
        assertTrue(isEqualCollection(reportsPage.getGroupByVisibility(), asList("Time", "Author", "Report Name",
                "Folders")));
    }

    @Test(dependsOnMethods = {"addTagToReport"})
    public void verifyTagReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(ALL_FOLDER);
        assertTrue(reportsPage.isTagCloudVisible());
        assertTrue(reportsPage.getReportsList().getNumberOfReports() > 1);
        assertEquals(reportsPage.filterByTag(TAG_NAME).getReportsList().getNumberOfReports(), 1);

        reportsPage.getCustomFolders().openFolder(CURRENT_SALES_FOLDER);
        assertFalse(reportsPage.isTagCloudVisible());
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 3);

        reportsPage.getDefaultFolders().openFolder(ALL_FOLDER);
        assertTrue(reportsPage.deselectAllTags().getReportsList().getNumberOfReports() > 1);
    }

    @Test(dependsOnMethods = {"verifyTagReport"})
    public void moveReports() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getCustomFolders().openFolder(CURRENT_SALES_FOLDER);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 3);

        reportsPage.getDefaultFolders().openFolder(ALL_FOLDER);
        reportsPage.moveReportsToFolder(CURRENT_SALES_FOLDER, TAG_REPORT);
        reportsPage.getCustomFolders().openFolder(CURRENT_SALES_FOLDER);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 4);

        assertEquals(reportsPage.moveReportsToFolderByDragDrop(ACTIVITY_REPORTS_FOLDER, TAG_REPORT)
                .getReportsList()
                .getNumberOfReports(), 3);
        reportsPage.getCustomFolders().openFolder(ACTIVITY_REPORTS_FOLDER);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 6);
    }

    @Test(dependsOnMethods = {"addTagToReport"})
    public void favoriteReport() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(ALL_FOLDER);
        waitForFragmentVisible(reportsPage).addFavorite(TAG_REPORT);

        waitForFragmentVisible(reportsPage).getDefaultFolders().openFolder(FAVORITES_FOLDER);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 1);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void testRememberViewing() {
        initReportsPage();
        waitForFragmentVisible(reportsPage).getCustomFolders().openFolder(ACTIVITY_REPORTS_FOLDER);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), 5);

        initManagePage();
        initReportsPage();
        assertTrue(reportsPage.isFolderSelected(ACTIVITY_REPORTS_FOLDER));
        assertEquals(waitForFragmentVisible(reportsPage).getReportsList().getNumberOfReports(), 5);
    }
}
