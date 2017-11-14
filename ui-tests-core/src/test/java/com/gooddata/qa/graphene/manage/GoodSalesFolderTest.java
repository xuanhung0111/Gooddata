package com.gooddata.qa.graphene.manage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.manage.DataPage;

public class GoodSalesFolderTest extends GoodSalesAbstractTest {

    private static final List<String> PAGES = Arrays.asList("attributes", "facts", "metrics");
    private static final List<String> ATTRIBUTE_FOLDER_LIST = new ArrayList<String>(Arrays.asList("Unsorted",
            "Account", "Activity", "Date dimension (Activity)",
            "Date dimension (Closed)", "Date dimension (Created)",
            "Date dimension (Snapshot)", "Date dimension (Timeline)",
            "Opp. Snapshot", "Opportunity", "Product", "Sales Rep",
            "Stage", "Stage History"));
    private static final List<String> FACT_FOLDER_LIST = new ArrayList<String>(Arrays.asList("Unsorted",
            "Opp. Snapshot", "Stage History"));
    private static final List<String> METRICFOLDERLIST = new ArrayList<String>(Arrays.asList("Unsorted"));

    private String oldEditName;
    private String newEditName;
    private String deleteName;
    private String uniqueName;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-folder";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"admin-tests"})
    public void verifyFolderListTest() {
        for (String page : PAGES) {
            initDataPage(page).getObjectFolder().verifyFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = {"verifyFolderListTest"}, groups = {"admin-tests"})
    public void addFolderTest() {
        for (String page : PAGES) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, "New Folder", null);
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void checkUnicodeNameTest() {
        for (String page : PAGES) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, "ພາສາລາວ résumé اللغة", null);
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void checkUniqueNameTest() {
        for (String page : PAGES) {
            initVariables(page);
            initDataPage(page).getObjectFolder().addFolder(page, uniqueName,
                    "Folder with that name already exists.");
        }
    }

    @Test(dependsOnMethods = {"addFolderTest"}, groups = {"admin-tests"})
    public void editFolderTest() {
    	final String description = "This is a description of folder";
        for (String page : PAGES) {
            initVariables(page);
            initDataPage(page).getObjectFolder().editFolder(oldEditName, newEditName, description);
            removeFolderFromList(page, oldEditName);
        }
    }

    @Test(dependsOnMethods = {"editFolderTest"}, groups = {"admin-tests"})
    public void deleteFolderTest() {
        for (String page : PAGES) {
            initVariables(page);
            initDataPage(page).getObjectFolder().deleteFolder(deleteName);
            removeFolderFromList(page, deleteName);
        }
    }

    @Test(dependsOnMethods = {"verifyFolderListTest"}, groups = {"admin-tests"})
    public void createSnDFolderTest() {
    	final String sndFolder = "Slide-n-Dice Folder";
        createSnDFolder(sndFolder);
    }

    @Test(dependsOnGroups = {"admin-tests"}, groups = {"editor-tests"})
    public void editorVerifyFolderListTest() throws ParseException, IOException, JSONException {
        logout();
        signIn(false, UserRoles.EDITOR);
        for (String page : PAGES) {
            initDataPage(page).getObjectFolder().checkEditorViewFolderList(page, getFolderList(page));
        }
    }

    @Test(dependsOnMethods = {"editorVerifyFolderListTest"}, groups = {"editor-tests"})
    public void editorCreateMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().addFolder("metrics", "New Folder 1", null);
    }

    @Test(dependsOnMethods = {"editorCreateMetricFolderTest"}, groups = {"editor-tests"})
    public void editorEditMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().editFolder("New Folder 1",
                "Renamed", "This is a description of folder");
    }

    @Test(dependsOnMethods = {"editorEditMetricFolderTest"}, groups = {"editor-tests"})
    public void editorDeleteMetricFolderTest() {
        initDataPage("metrics").getObjectFolder().deleteFolder("Renamed");
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

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
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
            oldEditName = "New Folder";
            newEditName = "Renamed";
            deleteName = "Renamed";
            uniqueName = "New Folder";
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
            .clickAddNewMetric()
            .createGlobalSimpleMetric(SimpleMetricTypes.SUM, "Amount", folderName);
        initDataPage("metrics").getObjectFolder().checkFolderVisible(folderName);
    }

    private void removeFolderFromList(String page, String folderName) {
        if (page.equalsIgnoreCase("attributes")) {
        	ATTRIBUTE_FOLDER_LIST.remove(folderName);
        } else if (page.equalsIgnoreCase("facts")) {
        	FACT_FOLDER_LIST.remove(folderName);
        } else {
        	METRICFOLDERLIST.remove(folderName);
        }
    }

    private List<String> getFolderList(String page) {
        if (page.equalsIgnoreCase("attributes")) {
            return ATTRIBUTE_FOLDER_LIST;
        } else if (page.equalsIgnoreCase("facts")) {
            return FACT_FOLDER_LIST;
        } else {
            return METRICFOLDERLIST;
        }
    }
}
