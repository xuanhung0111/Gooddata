package com.gooddata.qa.graphene.manage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.http.RestUtils;

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
    private List<String> attributeFolderList;
    private List<String> factFolderList;
    private List<String> metricFolderList;
    private List<String> pages;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-folder-test";
        testParams.setUser(testParams.getDomainUser());
        testParams.setPassword(testParams.getDomainPassword());
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
    public void initialize() throws InterruptedException, JSONException {
        newName = "New Folder";
        unicodeName = "ພາສາລາວ résumé اللغة";
        description = "This is a description of folder";
        sndFolder = "Slide-n-Dice Folder";
        pages = Arrays.asList("attributes", "facts", "metrics");
        attributeFolderList = new ArrayList<String>(Arrays.asList("Unsorted",
                "Account", "Activity", "Date dimension (Activity)",
                "Date dimension (Closed)", "Date dimension (Created)",
                "Date dimension (Snapshot)", "Date dimension (Timeline)",
                "Opp. Snapshot", "Opportunity", "Product", "Sales Rep",
                "Stage", "Stage History"));
        factFolderList = new ArrayList<String>(Arrays.asList("Unsorted",
                "Opp. Snapshot", "Stage History"));
        metricFolderList = new ArrayList<String>(Arrays.asList("Unsorted",
                "Activities", "Opportunity Counts", "Quota Attainment",
                "Sales Cycles", "Sales Figures", "Sales Rep", "_System"));
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "admin-tests" })
    public void verifyFolderListTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            dataPage.getObjectFolder().verifyFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = { "verifyFolderListTest" }, groups = { "admin-tests" })
    public void addFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, newName, null);
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "admin-tests" })
    public void checkUnicodeNameTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, unicodeName, null);
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "admin-tests" })
    public void checkUniqueNameTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().addFolder(page, uniqueName,
                    "Folder with that name already exists.");
        }
    }

    @Test(dependsOnMethods = { "addFolderTest" }, groups = { "admin-tests" })
    public void editFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().editFolder(oldEditName, newEditName, description);
            removeFolderFromList(page, oldEditName);
        }
    }

    @Test(dependsOnMethods = { "editFolderTest" }, groups = { "admin-tests" })
    public void deleteFolderTest() throws InterruptedException {
        for (String page : pages) {
            initDataPage(page);
            initVariables(page);
            dataPage.getObjectFolder().deleteFolder(deleteName);
            removeFolderFromList(page, deleteName);
        }
    }

    @Test(dependsOnMethods = { "verifyFolderListTest" }, groups = { "admin-tests" })
    public void createSnDFolderTest() throws InterruptedException {
        createSnDFolder(sndFolder);
    }

    @Test(dependsOnGroups = { "admin-tests" }, groups = { "editor-tests" })
    public void editorVerifyFolderListTest() throws ParseException, IOException, JSONException  {
        addEditorUserToProject();
        for (String page : pages) {
            initDataPage(page);
            dataPage.getObjectFolder().checkEditorViewFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = { "editorVerifyFolderListTest" }, groups = { "editor-tests" })
    public void editorCreateMetricFolderTest() throws InterruptedException {
        initDataPage("metrics");
        dataPage.getObjectFolder().addFolder("metrics", "New Folder 1", null);
    }

    @Test(dependsOnMethods = { "editorVerifyFolderListTest" }, groups = { "editor-tests" })
    public void editorEditMetricFolderTest() throws InterruptedException {
        initDataPage("metrics");
        dataPage.getObjectFolder().editFolder("Sales Figures",
                "Sales Figures Renamed", "This is a description of folder");
    }

    @Test(dependsOnMethods = { "editorVerifyFolderListTest" }, groups = { "editor-tests" })
    public void editorDeleteMetricFolderTest() throws InterruptedException {
        initDataPage("metrics");
        dataPage.getObjectFolder().deleteFolder("Sales Rep");
    }

    @Test(dependsOnMethods = { "editorVerifyFolderListTest" }, groups = { "editor-tests" })
    public void editorCreateSnDFolderTest() throws InterruptedException {
        createSnDFolder("Editor slide-n-dice folder");
    }

    @Test(dependsOnGroups = { "editor-tests" }, groups = { "tests" })
    public void finalTest() {
        successfulTest = true;
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getDomainPassword());
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

    private void addEditorUserToProject() throws ParseException, IOException, JSONException {
        String projectId = testParams.getProjectId();
        RestUtils.addUserToProject(testParams.getHost(),
                testParams.getProjectId(), testParams.getDomainUser(),
                testParams.getDomainPassword(), testParams.getProfileUri(),
                UserRoles.EDITOR);
        logout();
        loadProperties();
        testParams.setProjectId(projectId);
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    private void createSnDFolder(String folderName) throws InterruptedException {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        reportPage.getVisualiser().createSnDFolder("Amount", folderName);
        initDataPage("metrics");
        dataPage.getObjectFolder().checkFolderVisible(folderName);
    }

    private void removeFolderFromList(String page, String folderName) {
        if (page.equalsIgnoreCase("attributes")) {
            attributeFolderList.remove(folderName);
        } else if (page.equalsIgnoreCase("facts")) {
            factFolderList.remove(folderName);
        } else {
            metricFolderList.remove(folderName);
        }
    }

    private List<String> getFolderList(String page) {
        if (page.equalsIgnoreCase("attributes")) {
            return attributeFolderList;
        } else if (page.equalsIgnoreCase("facts")) {
            return factFolderList;
        } else {
            return metricFolderList;
        }
    }
}