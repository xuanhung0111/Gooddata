package com.gooddata.qa.graphene.reports;

import java.util.List;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.reports.ReportsFolders;
import com.gooddata.qa.utils.graphene.Screenshots;


@Test(groups = {"report"}, description = "Tests for basic reports functionality in GD platform")
public class ReportsPageTest extends AbstractTest {

    private static final String[] expectedDefaultFolderNames = {"All", "Favorites", "My Reports", "Unsorted"};
    private static final String[] expectedDefaultFoldersLinksSuffix =
            {"all-reports", "favorites", "myreports", "unsorted"};
    private static final String[] expectedDefaultFolderDescriptions =
            {"This folder contains all reports within the project.",
                    "This folder contains your favorite reports. You can drag reports into this folder or open a report and " +
                            "click the \"Add to Favorites\" link.",
                    "This folder contains all reports created by you.",
                    "Reports that do not belong to any folder are listed here."};

    @BeforeClass
    public void initStartPage() {
        testParams.setProjectId(testParams.loadProperty("projectId"));

        startPage = uiUtils.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage";
    }

    /**
     * Initial test for reports page
     * - verifies/do login at the beginning of the test
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test(groups = {"reportsInit"}, alwaysRun = true)
    public void gd_Report_001_ReportsElements() throws InterruptedException, JSONException {
        Thread.sleep(5000);
        validSignInWithDemoUser(false);
        checkUtils.waitForReportsPageLoaded();
        waitForElementVisible(uiUtils.reportsPage.getRoot());
        Assert.assertNotNull(uiUtils.reportsPage, "Reports page not initialized!");
    }

    @Test(dependsOnGroups = {"reportsInit"})
    public void gd_Report_002_GetNumberOfDefaultFolders() throws InterruptedException {
        checkUtils.waitForReportsPageLoaded();
        int numberOfFolders = uiUtils.reportsPage.getDefaultFolders().getNumberOfFolders();
        Assert.assertTrue(numberOfFolders == 4, "4 default folders are present");
    }

    @Test(dependsOnGroups = {"reportsInit"})
    public void gd_Report_003_GetNumberOfCustomFolders() throws InterruptedException {
        checkUtils.waitForReportsPageLoaded();
        int numberOfFolders = uiUtils.reportsPage.getCustomFolders().getNumberOfFolders();
        System.out.println("Number of custom folders for selected project: " + numberOfFolders);
        Assert.assertTrue(numberOfFolders >= 0);
    }

    @Test(dependsOnGroups = {"reportsInit"})
    public void gd_Report_004_DefaultFolderNames() throws InterruptedException {
        checkUtils.waitForReportsPageLoaded();
        List<String> folderNames = uiUtils.reportsPage.getDefaultFolders().getAllFolderNames();
        Assert.assertTrue(folderNames.size() == 4);
        for (int i = 0; i < folderNames.size(); i++) {
            Assert.assertEquals(folderNames.get(i), expectedDefaultFolderNames[i], "Default folder name match - " +
                    folderNames.get(i) + "/" + expectedDefaultFolderNames[i]);
        }
    }

    @Test(dependsOnGroups = {"reportsInit"})
    public void gd_Report_005_DefaultFolderLinks() throws InterruptedException {
        checkUtils.waitForReportsPageLoaded();
        ReportsFolders folders = uiUtils.reportsPage.getDefaultFolders();
        for (int i = 0; i < folders.getNumberOfFolders(); i++) {
            Assert.assertTrue(folders.getFolderLink(i).endsWith(expectedDefaultFoldersLinksSuffix[i]));
        }
    }

    @Test(dependsOnGroups = {"reportsInit"})
    public void gd_Report_006_DefaultFoldersSwitching() throws InterruptedException {
        checkUtils.waitForReportsPageLoaded();
        ReportsFolders folders = uiUtils.reportsPage.getDefaultFolders();
        for (int i = 0; i < folders.getNumberOfFolders(); i++) {
            folders.openFolder(i);
            checkUtils.waitForReportsPageLoaded();
            if (i > 0) { //all-reports isn't added at URL on first access
                Assert.assertTrue(browser.getCurrentUrl().endsWith(expectedDefaultFoldersLinksSuffix[i]),
                        "Current Browser URL match: " + browser.getCurrentUrl());
            }
            Screenshots.takeScreenshot(browser, "reports-folder-" + i + "-" + uiUtils.reportsPage.getSelectedFolderName(),
                    this.getClass());
            Assert.assertEquals(uiUtils.reportsPage.getSelectedFolderName(), expectedDefaultFolderNames[i],
                    "Selected folder name match: " + uiUtils.reportsPage.getSelectedFolderName());
            Assert.assertEquals(uiUtils.reportsPage.getSelectedFolderDescription(), expectedDefaultFolderDescriptions[i],
                    "Selected folder description match: " + uiUtils.reportsPage.getSelectedFolderDescription());
        }
    }
    /**
     @Test(dependsOnGroups = {"reportsInit"})
     public void gd_Report_007_AddCustomFolder() throws InterruptedException {
     waitForReportsPageLoaded();
     reports.addNewFolder("007_folder");
     }

     @Test(dependsOnGroups = {"reportsInit"}, dependsOnMethods = {"gd_Report_007_AddCustomFolder"})
     public void gd_Report_007_SelectNewCustomFolder() throws InterruptedException {
     waitForReportsPageLoaded();
     reports.getCustomFolders().openFolder("007_folder");
     Assert.assertEquals(reports.getSelectedFolderName(), "007_folder", "Selected folder name match: " + reports.getSelectedFolderName());
     }
     */
}
