package com.gooddata.qa.graphene.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

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

    protected static final List<String> metricsList = Arrays.asList("# of Activities",
            "# of Lost Opps.", "# of Open Opps.", "# of Opportunities", "# of Opportunities [BOP]",
            "# of Won Opps.", "% of Goal", "Amount", "Amount [BOP]", "Avg. Amount", "Avg. Won",
            "Best Case", "Best Case + Won", "Best Case + Won above Quota", "Best Case [BOP]",
            "Days until Close", "Expected", "Expected % of Goal", "Expected + Won",
            "Expected + Won vs. Quota", "Lost", "Probability", "Probability [BOP]",
            "Productive Reps", "Quota", "Remaining Quota", "Stage Duration", "Stage Velocity",
            "Win Rate", "Won", "_Close [BOP]", "_Close [EOP]", "_Opp. First Snapshot",
            "_Snapshot [BOP]", "_Snapshot [EOP-1]", "_Snapshot [EOP-2]", "_Snapshot [EOP]",
            "_Timeline [BOP]", "_Timeline [EOP]");

    protected static final List<String> variablesList = Arrays.asList("Quota", "Status");

    protected void createNewFolder(ObjectsTable objectsTable, ObjectTypes objectType,
                                   String newFolderName) throws InterruptedException {
        if (objectType == ObjectTypes.ATTRIBUTE) {
            dataPage.getAddDimensionButton().click();
            createNewFolderForObjects(dataPage.getDimensionList(), newFolderName);
        } else {
            dataPage.getAddFolderButton().click();
            createNewFolderForObjects(dataPage.getFoldersList(), newFolderName);
        }
        Screenshots.takeScreenshot(browser, "create-new-folder-for-"
                + objectType.getObjectsTableID(), this.getClass());
    }

    protected void createNewFolderForObjects(WebElement listOfFoldersDimensions,
                                             String newFolderName) throws InterruptedException {
        waitForElementVisible(dataPage.getAddFolderDialog());
        dataPage.setNewFolderTitle(newFolderName);
        dataPage.getConfirmAddFolderButton().click();
        waitForElementVisible(listOfFoldersDimensions);
        Thread.sleep(3000);
        Assert.assertTrue(dataPage.getFolderDimension(listOfFoldersDimensions, newFolderName)
                .isDisplayed());
    }

    protected String moveObjectsBySelectFolderName(ObjectTypes objectType,
                                                   String targetFolderIndex, List<String> objectsList) {
        dataPage.getMoveObjectsButton().click();
        waitForElementVisible(dataPage.getMoveObjectsDialog());
        String targetFolderName = waitForElementVisible(dataPage.getTargetFolder(targetFolderIndex))
                .getText();
        dataPage.getTargetFolder(targetFolderIndex).click();
        waitForElementVisible(dataPage.getProgressMessageBox());
        Assert.assertTrue(dataPage.getProgressMessageBox().getText().contains("Moving "));
        String message = String.format("Success! %d %s(s) moved to \"%s\" folder.",
                objectsList.size(), objectType.getName(), targetFolderName);
        waitForElementVisible(dataPage.getStatusMessageOnGreenBar());
        Assert.assertEquals(dataPage.getStatusMessageOnGreenBar().getText(), message);
        Screenshots.takeScreenshot(browser,
                "move-objects-by-select-folder-name-in-" + objectType.getObjectsTableID(),
                this.getClass());
        return targetFolderName;
    }

    protected void moveObjectsByTypeFolderName(ObjectTypes objectType, String targetFolder,
                                               List<String> objectsList) {
        dataPage.getMoveObjectsButton().click();
        waitForElementVisible(dataPage.getMoveObjectsDialog());
        dataPage.getMoveObjectsDialogInput().sendKeys(targetFolder);
        dataPage.getMoveObjectsDialogConfirmButton().click();
        waitForElementVisible(dataPage.getProgressMessageBox());

        Assert.assertTrue(dataPage.getProgressMessageBox().getText().contains("Moving"));
        String message = String.format("Success! %d %s(s) moved to \"%s\" folder.",
                objectsList.size(), objectType.getName(), targetFolder);
        waitForElementVisible(dataPage.getStatusMessageOnGreenBar());
        Assert.assertEquals(dataPage.getStatusMessageOnGreenBar().getText(), message);
        Screenshots.takeScreenshot(browser,
                "move-objects-by-enter-folder-name-in-" + objectType.getObjectsTableID(),
                this.getClass());
    }

    protected void moveObjectsBetweenFolders(ObjectTypes objectType, ObjectsTable objectsTable,
                                             List<String> movedObjects, String targetFolderID1, String targetFolderName2)
            throws InterruptedException {
        openObjectsTable(objectsTable, objectType);
        checkAllCheckboxes(objectsTable);
        checkNoneCheckboxes(objectsTable);
        objectsTable.checkOnCheckboxes(movedObjects);
        String targetFolderName1 = moveObjectsBySelectFolderName(objectType, targetFolderID1, movedObjects);
        assertMovedObjectsInTargetFolder(targetFolderName1, objectsTable, movedObjects);
        objectsTable.checkOnCheckboxes(movedObjects);
        moveObjectsByTypeFolderName(objectType, targetFolderName2, movedObjects);
        assertMovedObjectsInTargetFolder(targetFolderName2, objectsTable, movedObjects);
    }

    protected void deleteObjectsTable(ObjectsTable objectsTable, List<String> deletedObjectsList, List<String> defaultObjectsList, ObjectTypes objectType) throws InterruptedException {
        List<String> remainedObjectsList = new ArrayList<String>();
        remainedObjectsList.addAll(defaultObjectsList);
        remainedObjectsList.removeAll(deletedObjectsList);
        openObjectsTable(objectsTable, objectType);
        objectsTable.checkOnCheckboxes(deletedObjectsList);
        this.cancelDeleteObjects(objectType, objectsTable, deletedObjectsList, defaultObjectsList);
        this.deleteObjects(objectType, objectsTable, deletedObjectsList,
                remainedObjectsList);
    }

    protected void checkDeleteConfirmDialog(ObjectTypes objectType, ObjectsTable objectsTable,
                                            List<String> deletedObjects) throws InterruptedException {
        dataPage.getDeleteObjectsButton().click();
        waitForElementVisible(dataPage.getDeleteConfirmDialog());
        String deleteConfirmDialogTitle = String.format("Delete %s(s)", objectType.getName());
        String deleteConfirmDialogMessage = String.format(
                "Are you sure you want to delete %d %s(s)", deletedObjects.size(),
                objectType.getName());
        Assert.assertEquals(dataPage.getDeleteConfirmDialogHeader().getText(),
                deleteConfirmDialogTitle);
        Assert.assertTrue(dataPage.getDeleteConfirmDialogMessage().getText()
                .contains(deleteConfirmDialogMessage));
        Screenshots.takeScreenshot(browser,
                "delete-confirm-dialog-for-" + objectType.getObjectsTableID(), this.getClass());
    }

    protected void deleteObjects(ObjectTypes objectType, ObjectsTable objectsTable,
                                 List<String> deletedObjects, List<String> existingObjects) throws InterruptedException {
        checkDeleteConfirmDialog(objectType, objectsTable, deletedObjects);
        Screenshots.takeScreenshot(browser, objectType.getObjectsTableID()
                + "-before-deleting-selected-objects", this.getClass());
        dataPage.getDeleteConfirmButton().click();
        Assert.assertTrue(waitForElementVisible(dataPage.getProgressMessageBox()).getText()
                .contains("Deleting "));
        waitForElementNotPresent(dataPage.getProgressMessageBox());
        String message = String.format("%d %s(s) deleted.", deletedObjects.size(),
                objectType.getName());
        Assert.assertEquals(waitForElementVisible(dataPage.getStatusMessageOnGreenBar()).getText(),
                message);
        waitForElementVisible(objectsTable.getRoot());
        this.assertRowTitles(objectsTable, existingObjects);
        Screenshots.takeScreenshot(browser, objectType.getObjectsTableID()
                + "-after-deleting-selected-objects", this.getClass());
    }

    protected void cancelDeleteObjects(ObjectTypes objectType, ObjectsTable objectsTable,
                                       List<String> deletedObjects, List<String> defaultObjectsList)
            throws InterruptedException {
        checkDeleteConfirmDialog(objectType, objectsTable, deletedObjects);
        dataPage.getCancelDeleteButton().click();
        this.assertRowTitles(objectsTable, defaultObjectsList);
        Screenshots.takeScreenshot(browser, objectType.getObjectsTableID()
                + "-after-canceling-deleting-objects", this.getClass());
    }

    protected boolean assertMovedObjectsInTargetFolder(String targetFolderName, ObjectsTable objectsTable,
                                                       List<String> movedObjects) throws InterruptedException {
        dataPage.getFolder(targetFolderName).click();
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
        Screenshots.takeScreenshot(browser, "objects-list-of-" + targetFolderName + "-folder-in-"
                + objectsTable.getRoot().getAttribute("id"), this.getClass());
        return assertResult;
    }

    protected void openObjectsTable(ObjectsTable objectsTable, ObjectTypes objectType) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage");
        waitForDataPageLoaded(browser);
        waitForElementVisible(dataPage.getMenuItem(objectType)).click();
        waitForDataPageLoaded(browser);
        waitForElementVisible(objectsTable.getRoot());
        Screenshots.takeScreenshot(browser, "open-" + objectsTable.getRoot().getAttribute("id"),
                this.getClass());
    }

    protected void viewObjectsTable(ObjectsTable objectsTable, ObjectTypes objectType)
            throws InterruptedException {
        openObjectsTable(objectsTable, objectType);
        objectsTable.assertTableHeader();
        objectsTable.assertCheckboxes(true, false);
        dataPage.assertMassActions();
        Screenshots.takeScreenshot(browser, "view-" + objectsTable.getRoot().getAttribute("id"),
                this.getClass());
    }

    protected void viewSortObjectsTable(ObjectsTable objectsTable, ObjectTypes objectType, List<String> defaultObjectsList) throws InterruptedException {
        viewObjectsTable(objectsTable, objectType);
        createNewFolder(objectsTable, objectType, String.format("New %s folder", objectType.getName()));
        objectsTable.sortObjectsTable(ObjectsTable.SORT_DESC, defaultObjectsList);
        objectsTable.sortObjectsTable(ObjectsTable.SORT_ASC, defaultObjectsList);
    }

    protected void checkObjectLinkInTable(ObjectsTable objectsTable, String selectedObjectName) {
        objectsTable.selectObject(selectedObjectName);
        waitForObjectPageLoaded(browser);
        Assert.assertEquals(objectDetailPage.getObjectName(), selectedObjectName);
    }

    protected void assertRowTitles(ObjectsTable objectsTable, List<String> rowTitles)
            throws InterruptedException {
        int index = 0;
        for (WebElement row : objectsTable.getRows()) {
            Assert.assertTrue(row.getText().contains(rowTitles.get(index)));
            index++;
        }
        Screenshots.takeScreenshot(browser, "assert-row-titles-in-"
                + objectsTable.getRoot().getAttribute("id"), this.getClass());
    }

    protected void addTagsToObject(ObjectsTable objectsTable, String objectName,
                                   List<String> tagsList) throws InterruptedException {
        objectsTable.selectObject(objectName);
        waitForObjectPageLoaded(browser);
        for (String tagName : tagsList) {
            objectDetailPage.addTag(tagName);
        }
    }

    protected void filterObjectByTagCloud(ObjectsTable objectsTable,
                                          List<String> filteredObjectTitles, Map<String, List<String>> tagsCloud)
            throws InterruptedException {
        if (dataPage.getSwitchTagsDisplayedForm().getText().equals("Show as Cloud"))
            dataPage.getShowAsCloudButton().click();
        for (String tagName : tagsCloud.keySet()) {
            String fontSize = tagsCloud.get(tagName).get(1);
            dataPage.getTagByCloud(tagName, fontSize).click();
        }
        assertRowTitles(objectsTable, filteredObjectTitles);
        dataPage.getDeselectAllButton().click();
        Screenshots.takeScreenshot(browser, objectsTable.getRoot().getAttribute("id")
                + "-filtered-by-tags-cloud", this.getClass());
    }

    protected void filterObjectByTagList(ObjectsTable objectsTable,
                                         List<String> filteredObjectTitles, Map<String, List<String>> tagsList)
            throws InterruptedException {
        if (dataPage.getSwitchTagsDisplayedForm().getText().equals("Show as List"))
            dataPage.getShowAsListButton().click();
        for (String tagName : tagsList.keySet()) {
            String index = tagsList.get(tagName).get(0);
            dataPage.getTagByList(tagName, index).click();
        }
        assertRowTitles(objectsTable, filteredObjectTitles);
        dataPage.getDeselectAllButton().click();
        Screenshots.takeScreenshot(browser, objectsTable.getRoot().getAttribute("id")
                + "-filtered-by-tags-list", this.getClass());
    }

    protected void checkTagObjects(ObjectsTable objectsTable,
                                   Map<String, List<String>> taggedObjects, Map<String, List<String>> tagsMap,
                                   List<String> filterObjectsList) throws InterruptedException {
        Set<String> objectNames = taggedObjects.keySet();
        for (String objectName : objectNames) {
            addTagsToObject(objectsTable, objectName, taggedObjects.get(objectName));
            Thread.sleep(3000);
            objectDetailPage.getBackDataPageLink().click();
            waitForDataPageLoaded(browser);
        }
        this.filterObjectByTagList(objectsTable, filterObjectsList, tagsMap);
        this.filterObjectByTagCloud(objectsTable, filterObjectsList, tagsMap);
    }

    protected void checkAllCheckboxes(ObjectsTable objectsTable) throws InterruptedException {
        dataPage.getAllAction().click();
        objectsTable.assertCheckboxes(true, true);
    }

    protected void checkNoneCheckboxes(ObjectsTable objectsTable) throws InterruptedException {
        dataPage.getNoneAction().click();
        objectsTable.assertCheckboxes(true, false);
    }
}