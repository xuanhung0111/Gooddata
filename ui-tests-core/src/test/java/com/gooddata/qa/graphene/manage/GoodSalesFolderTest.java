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
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.manage.DataPage;

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
        projectTitle = "GoodSales-test-folder";
    }

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initialize() throws JSONException {
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

    @Test(dependsOnMethods = {"initialize"}, groups = {"admin-tests"})
    public void verifyFolderListTest() {
        for (String page : pages) {
            initDataPage(page).getObjectFolder().verifyFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = {"verifyFolderListTest"}, groups = {"admin-tests"})
    public void addFolderTest() {
        for (String page : pages) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, newName, null);
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void checkUnicodeNameTest() {
        for (String page : pages) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, unicodeName, null);
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void checkUniqueNameTest() {
        for (String page : pages) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, uniqueName,
                    "Folder with that name already exists.");
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void editFolderTest() {
        for (String page : pages) {
            initVariables(page);
            initDataPage(page).getObjectFolder().editFolder(oldEditName, newEditName, description);
            removeFolderFromList(page, oldEditName);
        }
    }

    @Test(dependsOnMethods = {"editFolderTest"}, groups = {"admin-tests"})
    public void deleteFolderTest() {
        for (String page : pages) {
            initVariables(page);
            initDataPage(page).getObjectFolder().deleteFolder(deleteName);
            removeFolderFromList(page, deleteName);
        }
    }

    @Test(dependsOnMethods = {"verifyFolderListTest"}, groups = {"admin-tests"})
    public void createSnDFolderTest() {
        createSnDFolder(sndFolder);
    }

    @Test(dependsOnGroups = {"admin-tests"}, groups = {"editor-tests"})
    public void editorVerifyFolderListTest() throws ParseException, IOException, JSONException {
        logout();
        signIn(false, UserRoles.EDITOR);
        for (String page : pages) {
            initDataPage(page).getObjectFolder().checkEditorViewFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorCreateMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().addFolder("metrics", "New Folder 1", null);
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorEditMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().editFolder("Sales Figures",
                "Sales Figures Renamed", "This is a description of folder");
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorDeleteMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().deleteFolder("Sales Rep");
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorCreateSnDFolderTest() {
        createSnDFolder("Editor slide-n-dice folder");
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorCannotEditAttributeAndFactFolderTest() {
        for (String page : Arrays.asList("attributes", "facts")) {
            String folder;
            if (page.equalsIgnoreCase("attributes"))
                folder = "Opportunity";
            else
                folder = "Opp. Snapshot";
            initDataPage(page).getObjectFolder().checkEditorCannotEditFolder(folder);
        }
    }

    @Test(dependsOnGroups = {"editor-tests"})
    public void finalTest() throws JSONException {
        logout();
        signIn(false, UserRoles.ADMIN);
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

    private DataPage initDataPage(String page) {
        if (page.equalsIgnoreCase("attributes")) {
            return initAttributePage();
        }

        if (page.equalsIgnoreCase("facts")) {
            initFactPage();
            return DataPage.getInstance(browser);
        }

        return initMetricPage();
    }

    private void createSnDFolder(String folderName) {
        initReportsPage()
            .startCreateReport()
            .initPage()
            .openWhatPanel()
            .createGlobalSimpleMetric(SimpleMetricTypes.SUM, "Amount", folderName);
        initDataPage("metrics").getObjectFolder().checkFolderVisible(folderName);
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
