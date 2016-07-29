package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.testng.annotations.Test;

public class EmptyStateTest extends AbstractCsvUploaderTest {

    @Test(dependsOnGroups = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage().waitForHeaderVisible();
        datasetsListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage().waitForEmptyStateLoaded();
        takeScreenshot(browser, "empty-state", getClass());
        log.info("Empty state message: " + datasetsListPage.getEmptyStateMessage());
    }
}
