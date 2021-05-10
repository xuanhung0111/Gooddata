package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.gooddata.qa.graphene.fragments.manage.DataPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectPropertiesPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;

public abstract class ManageObjectsAbstractTest extends GoodSalesAbstractTest {

    protected static final List<String> attributesList = Arrays.asList("Account", "Activity",
            "Activity Type", "Date (Activity)", "Date (Closed)", "Date (Created)",
            "Date (Snapshot)", "Date (Timeline)", "Day of Month (Activity)",
            "Day of Month (Closed)", "Day of Month (Created)", "Day of Month (Snapshot)",
            "Day of Month (Timeline)", "Day of Quarter (Activity)", "Day of Quarter (Closed)",
            "Day of Quarter (Created)", "Day of Quarter (Snapshot)", "Day of Quarter (Timeline)",
            "Day of Week (Mon-Sun) (Activity)", "Day of Week (Mon-Sun) (Closed)",
            "Day of Week (Mon-Sun) (Created)", "Day of Week (Mon-Sun) (Snapshot)",
            "Day of Week (Mon-Sun) (Timeline)", "Day of Week (Sun-Sat) (Activity)",
            "Day of Week (Sun-Sat) (Closed)", "Day of Week (Sun-Sat) (Created)",
            "Day of Week (Sun-Sat) (Snapshot)", "Day of Week (Sun-Sat) (Timeline)",
            "Day of Year (Activity)", "Day of Year (Closed)", "Day of Year (Created)",
            "Day of Year (Snapshot)", "Day of Year (Timeline)", "Department", "Forecast Category",
            "Is Active?", "Is Closed?", "Is Closed?", "Is Task?", "Is Won?", "Month (Activity)",
            "Month (Closed)", "Month (Created)", "Month (Snapshot)", "Month (Timeline)",
            "Month of Quarter (Activity)", "Month of Quarter (Closed)",
            "Month of Quarter (Created)", "Month of Quarter (Snapshot)",
            "Month of Quarter (Timeline)", "Month/Year (Activity)", "Month/Year (Closed)",
            "Month/Year (Created)", "Month/Year (Snapshot)", "Month/Year (Timeline)",
            "Opp. Snapshot", "Opportunity", "Priority", "Product", "Quarter (Activity)",
            "Quarter (Closed)", "Quarter (Created)", "Quarter (Snapshot)", "Quarter (Timeline)",
            "Quarter/Year (Activity)", "Quarter/Year (Closed)", "Quarter/Year (Created)",
            "Quarter/Year (Snapshot)", "Quarter/Year (Timeline)", "Records of Timeline", "Region",
            "Sales Rep", "Stage History", "Stage Name", "Status", "Status",
            "Week (Mon-Sun) (Activity)", "Week (Mon-Sun) (Closed)", "Week (Mon-Sun) (Created)",
            "Week (Mon-Sun) (Snapshot)", "Week (Mon-Sun) (Timeline)",
            "Week (Mon-Sun) of Qtr (Activity)", "Week (Mon-Sun) of Qtr (Closed)",
            "Week (Mon-Sun) of Qtr (Created)", "Week (Mon-Sun) of Qtr (Snapshot)",
            "Week (Mon-Sun) of Qtr (Timeline)", "Week (Mon-Sun)/Year (Activity)",
            "Week (Mon-Sun)/Year (Closed)", "Week (Mon-Sun)/Year (Created)",
            "Week (Mon-Sun)/Year (Snapshot)", "Week (Mon-Sun)/Year (Timeline)",
            "Week (Sun-Sat) (Activity)", "Week (Sun-Sat) (Closed)", "Week (Sun-Sat) (Created)",
            "Week (Sun-Sat) (Snapshot)", "Week (Sun-Sat) (Timeline)",
            "Week (Sun-Sat) of Qtr (Activity)", "Week (Sun-Sat) of Qtr (Closed)",
            "Week (Sun-Sat) of Qtr (Created)", "Week (Sun-Sat) of Qtr (Snapshot)",
            "Week (Sun-Sat) of Qtr (Timeline)", "Week (Sun-Sat)/Year (Activity)",
            "Week (Sun-Sat)/Year (Closed)", "Week (Sun-Sat)/Year (Created)",
            "Week (Sun-Sat)/Year (Snapshot)", "Week (Sun-Sat)/Year (Timeline)", "Year (Activity)",
            "Year (Closed)", "Year (Created)", "Year (Snapshot)", "Year (Timeline)");

    protected static final List<String> factsList = Arrays.asList("Activity (Date)", "Amount",
            "Days to Close", "Duration", "Opp. Close (Date)", "Opp. Created (Date)",
            "Opp. Snapshot (Date)", "Probability", "Timeline (Date)", "Velocity");

    protected void createNewFolder(ObjectTypes objectType, String newFolderName) {
        DataPage.getInstance(browser).createNewFolder(newFolderName);
        assertTrue(DataPage.getInstance(browser).getFolders().stream()
            .map(e -> e.getAttribute("title"))
            .anyMatch(e -> newFolderName.equals(e)));
        takeScreenshot(browser, "create-new-folder-for-"
                + objectType.getObjectsTableID(), this.getClass());
    }

    protected void moveObjectsByTypeFolderName(ObjectTypes objectType, String targetFolder, List<String> objectsList) {
        DataPage.getInstance(browser).getMoveObjectsButton().sendKeys(Keys.ENTER);
        IpeEditor.getInstance(browser).setText(targetFolder);
        String message = String.format("Success! %d %s(s) moved to \"%s\" folder.",
                objectsList.size(), objectType.getName(), targetFolder);
        assertEquals(DataPage.getInstance(browser).getStatusMessageOnGreenBar(), message);
        takeScreenshot(browser,
                "move-objects-by-enter-folder-name-in-" + objectType.getObjectsTableID(),
                this.getClass());
    }

    protected void moveObjectsBetweenFolders(ObjectTypes objectType, List<String> movedObjects, String targetFolder) {
        ObjectsTable objectsTable = openObjectsTable(objectType);
        checkAllCheckboxes(objectsTable);
        checkNoneCheckboxes(objectsTable);
        objectsTable.checkOnCheckboxes(movedObjects);
        moveObjectsByTypeFolderName(objectType, targetFolder, movedObjects);
        assertMovedObjectsInTargetFolder(targetFolder, objectsTable, movedObjects);
    }

    protected void deleteObjectsTable(List<String> deletedObjectsList, List<String> defaultObjectsList, ObjectTypes objectType) {
        List<String> remainedObjectsList = new ArrayList<String>();
        remainedObjectsList.addAll(defaultObjectsList);
        remainedObjectsList.removeAll(deletedObjectsList);
        openObjectsTable(objectType)
            .checkOnCheckboxes(deletedObjectsList);
        this.cancelDeleteObjects(objectType, deletedObjectsList, defaultObjectsList);
        this.deleteObjects(objectType, deletedObjectsList, remainedObjectsList);
    }

    protected void checkDeleteConfirmDialog(ObjectTypes objectType, List<String> deletedObjects) {
        DataPage.getInstance(browser).getDeleteObjectsButton().sendKeys(Keys.ENTER);
        WebElement confirmDialog = waitForElementVisible(className("t-confirmDelete"), browser);
        String deleteConfirmDialogTitle = String.format("Delete %s(s)", objectType.getName());
        String deleteConfirmDialogMessage = String.format(
                "Are you sure you want to delete %d %s(s)", deletedObjects.size(),
                objectType.getName());
        assertEquals(waitForElementVisible(cssSelector(".hd span"), confirmDialog).getText(),
                deleteConfirmDialogTitle);
        assertThat(waitForElementVisible(tagName("form"), confirmDialog).getText(),
                containsString(deleteConfirmDialogMessage));
        takeScreenshot(browser,
                "delete-confirm-dialog-for-" + objectType.getObjectsTableID(), this.getClass());
    }

    protected void deleteObjects(ObjectTypes objectType, List<String> deletedObjects, List<String> existingObjects) {
        checkDeleteConfirmDialog(objectType, deletedObjects);
        takeScreenshot(browser, objectType.getObjectsTableID()
                + "-before-deleting-selected-objects", this.getClass());
        waitForElementVisible(cssSelector(".t-confirmDelete .s-btn-delete"), browser).click();
        String message = String.format("%d %s(s) deleted.", deletedObjects.size(),
                objectType.getName());
        assertEquals(DataPage.getInstance(browser).getStatusMessageOnGreenBar(), message);
        this.assertRowTitles(objectType, existingObjects);
        takeScreenshot(browser, objectType.getObjectsTableID()
                + "-after-deleting-selected-objects", this.getClass());
    }

    protected void cancelDeleteObjects(ObjectTypes objectType, List<String> deletedObjects, List<String> defaultObjectsList) {
        checkDeleteConfirmDialog(objectType, deletedObjects);
        waitForElementVisible(cssSelector(".t-confirmDelete .s-btn-cancel"), browser).click();
        this.assertRowTitles(objectType, defaultObjectsList);
        takeScreenshot(browser, objectType.getObjectsTableID()
                + "-after-canceling-deleting-objects", this.getClass());
    }

    protected boolean assertMovedObjectsInTargetFolder(String targetFolderName, ObjectsTable objectsTable,
                                                       List<String> movedObjects) {
        DataPage.getInstance(browser).getFolders()
            .stream()
            .filter(e -> targetFolderName.equals(e.getAttribute("title")))
            .findFirst()
            .get()
            .click();
        waitForElementVisible(objectsTable.getRoot());
        waitForDataPageLoaded(browser);
        boolean assertResult = false;
        for (String movedObject : movedObjects) {
            for (int i = 0; i < objectsTable.getNumberOfRows(); i++) {
                if (objectsTable.getRow(i).getAttribute("title").equals(movedObject)) {
                    assertResult = true;
                    continue;
                }
            }

        }
        takeScreenshot(browser, "objects-list-of-" + targetFolderName + "-folder-in-"
                + objectsTable.getRoot().getAttribute("id"), this.getClass());
        return assertResult;
    }

    protected ObjectsTable openObjectsTable(ObjectTypes objectType) {
        initManagePage().openPage(objectType);
        takeScreenshot(browser, "open-" + objectType.getObjectsTableID(), this.getClass());
        return ObjectsTable.getInstance(id(objectType.getObjectsTableID()), browser);
    }

    protected ObjectsTable viewObjectsTable(ObjectTypes objectType) {
        ObjectsTable table = openObjectsTable(objectType)
            .assertTableHeader()
            .assertCheckboxes(true, false);
        takeScreenshot(browser, "view-" + objectType.getObjectsTableID(), this.getClass());
        return table;
    }

    protected void viewSortObjectsTable(ObjectTypes objectType, List<String> defaultObjectsList) {
        ObjectsTable objectsTable = viewObjectsTable(objectType);
        createNewFolder(objectType, String.format("New %s folder", objectType.getName()));
        objectsTable.sortObjectsTable(ObjectsTable.SORT_DESC, defaultObjectsList);
        objectsTable.sortObjectsTable(ObjectsTable.SORT_ASC, defaultObjectsList);
    }

    protected void checkObjectLinkInTable(ObjectTypes objectType, String selectedObjectName) {
        ObjectsTable.getInstance(id(objectType.getObjectsTableID()), browser).selectObject(selectedObjectName);
        waitForObjectPageLoaded(browser);
        assertEquals(Graphene.createPageFragment(objectType.getDetailPage(),
                waitForElementVisible(ObjectPropertiesPage.LOCATOR, browser)).getName(),
                selectedObjectName);
    }

    protected void assertRowTitles(ObjectTypes objectType, List<String> rowTitles) {
        int index = 0;
        for (WebElement row : ObjectsTable.getInstance(id(objectType.getObjectsTableID()), browser).getRows()) {
            assertThat(row.getText(), containsString(rowTitles.get(index)));
            index++;
        }
        takeScreenshot(browser, "assert-row-titles-in-" + objectType.getObjectsTableID(), this.getClass());
    }

    protected void addTagsToObject(ObjectTypes objectType, String objectName, List<String> tagsList) {
        ObjectsTable.getInstance(id(objectType.getObjectsTableID()), browser).selectObject(objectName);
        waitForObjectPageLoaded(browser);
        for (String tagName : tagsList) {
            Graphene.createPageFragment(objectType.getDetailPage(),
                    waitForElementVisible(ObjectPropertiesPage.LOCATOR, browser)).waitForLoaded().addTag(tagName);
        }
    }

    protected void filterObjectByTagCloud(ObjectTypes objectType, List<String> filteredObjectTitles,
            Map<String, List<String>> tagsCloud) {
        if (DataPage.getInstance(browser).getSwitchTagsDisplayedForm().getText().equals("Show as Cloud"))
            DataPage.getInstance(browser).getSwitchTagsDisplayedForm().click();
        for (String tagName : tagsCloud.keySet()) {
            String fontSize = tagsCloud.get(tagName).get(1);
            DataPage.getInstance(browser).getTagByCloud(tagName, fontSize).click();
        }
        assertRowTitles(objectType, filteredObjectTitles);
        DataPage.getInstance(browser).deselectAllTags();
        takeScreenshot(browser, objectType.getObjectsTableID() + "-filtered-by-tags-cloud", this.getClass());
    }

    protected void filterObjectByTagList(ObjectTypes objectType, List<String> filteredObjectTitles,
            Map<String, List<String>> tagsList) {
        if (DataPage.getInstance(browser).getSwitchTagsDisplayedForm().getText().equals("Show as List"))
            DataPage.getInstance(browser).getSwitchTagsDisplayedForm().click();
        for (String tagName : tagsList.keySet()) {
            String index = tagsList.get(tagName).get(0);
            DataPage.getInstance(browser).getTagByList(tagName, index).click();
        }
        assertRowTitles(objectType, filteredObjectTitles);
        DataPage.getInstance(browser).deselectAllTags();
        takeScreenshot(browser, objectType.getObjectsTableID() + "-filtered-by-tags-list", this.getClass());
    }

    protected void checkTagObjects(ObjectTypes objectType, Map<String, List<String>> taggedObjects,
            Map<String, List<String>> tagsMap, List<String> filterObjectsList) {
        Set<String> objectNames = taggedObjects.keySet();
        for (String objectName : objectNames) {
            addTagsToObject(objectType, objectName, taggedObjects.get(objectName));
            sleepTightInSeconds(3);
            Graphene.createPageFragment(objectType.getDetailPage(),
                    waitForElementVisible(ObjectPropertiesPage.LOCATOR, browser))
                    .clickDataPageLink();
            waitForDataPageLoaded(browser);
        }
        this.filterObjectByTagList(objectType, filterObjectsList, tagsMap);
        this.filterObjectByTagCloud(objectType, filterObjectsList, tagsMap);
    }

    protected void checkAllCheckboxes(ObjectsTable objectsTable) {
        DataPage.getInstance(browser).getAllAction().click();
        objectsTable.assertCheckboxes(true, true);
    }

    protected void checkNoneCheckboxes(ObjectsTable objectsTable) {
        DataPage.getInstance(browser).getNoneAction().click();
        objectsTable.assertCheckboxes(true, false);
    }
}
