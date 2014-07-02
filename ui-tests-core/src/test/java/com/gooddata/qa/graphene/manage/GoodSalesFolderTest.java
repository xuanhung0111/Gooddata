package com.gooddata.qa.graphene.manage;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = { "GoodSalesFolder" }, description = "Tests for view and edit folder on GoodSales project in GD platform")
public class GoodSalesFolderTest extends GoodSalesAbstractTest {
    private String newName;
    private String oldEditName;
    private String newEditName;
    private String deleteName;
    private String uniqueName;
    private String unicodeName;
    private String description;
    private String sndFolder;
    private List<String> folderList;
    private List<String> pages;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-folder-test";
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
    public void initialize() throws InterruptedException, JSONException {
        newName = "New Folder";
        unicodeName = "ພາສາລາວ résumé اللغة";
        description = "This is a description of folder";
        sndFolder = "Slide-n-Dice Folder";
        pages = Arrays.asList("attributes", "facts", "metrics");
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "folder-tests" })
    public void verifyFolderListTest() throws InterruptedException {
        for (String page : pages) {
            initFolderList(page);
            initDataPage(page);
            dataPage.getObjectFolder().verifyFolderList(folderList);
        }
    }

    @Test(dependsOnMethods = { "verifyFolderListTest" }, groups = { "folder-tests" })
    public void addFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, newName, null);
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "folder-tests" })
    public void checkUnicodeNameTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, unicodeName, null);
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "folder-tests" })
    public void checkUniqueNameTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, uniqueName,
                    "Folder with that name already exists.");
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "folder-tests" })
    public void editFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().editFolder(oldEditName, newEditName, description);
        }
    }

    @Test(dependsOnMethods = { "editFolderTest" }, groups = { "folder-tests" })
    public void deleteFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().deleteFolder(deleteName);
        }
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "folder-tests" })
    public void createSnDFolderTest() throws InterruptedException {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        reportPage.getVisualiser().createSnDFolder("Amount", sndFolder);
        initDataPage("metrics");
        dataPage.getObjectFolder().checkFolderVisible(sndFolder);
    }

    @Test(dependsOnGroups = { "folder-tests" }, groups = { "tests" })
    public void finalTest() {
        successfulTest = true;
    }

    private void initFolderList(String page) {
        if (page.equalsIgnoreCase("attributes")) {
            folderList = Arrays.asList("Unsorted", "Account", "Activity",
                    "Date dimension (Activity)", "Date dimension (Closed)",
                    "Date dimension (Created)", "Date dimension (Snapshot)",
                    "Date dimension (Timeline)", "Opp. Snapshot",
                    "Opportunity", "Product", "Sales Rep", "Stage",
                    "Stage History");
        } else if (page.equalsIgnoreCase("facts")) {
            folderList = Arrays.asList("Unsorted", "Opp. Snapshot",
                    "Stage History");
        } else {
            folderList = Arrays
                    .asList("Unsorted", "Activities", "Opportunity Counts",
                            "Quota Attainment", "Quota Attainment",
                            "Sales Figures", "Sales Rep", "_System");
        }
    }

    private void initVariables(String page) {
        if (page.equalsIgnoreCase("attributes")) {
            oldEditName = "Account";
            newEditName = "Account Renamed";
            deleteName = "Activity";
            uniqueName = "Opportunity";
        } else if (page.equalsIgnoreCase("facts")) {
            oldEditName = "Stage History";
            newEditName = "Stage History Renamed";
            deleteName = "New Folder";
            uniqueName = "Opp. Snapshot";
        } else {
            oldEditName = "Activities";
            newEditName = "Activities Renamed";
            deleteName = "Sales Cycles";
            uniqueName = "Quota Attainment";
        }
    }

    private void initDataPage(String page) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId()
                + "|dataPage|" + page);
        waitForDataPageLoaded(browser);
    }
}