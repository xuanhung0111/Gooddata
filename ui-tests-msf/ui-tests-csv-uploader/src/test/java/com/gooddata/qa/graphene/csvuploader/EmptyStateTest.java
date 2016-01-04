package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.graphene.Screenshots.toScreenshotName;

import org.testng.annotations.Test;

public class EmptyStateTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        datasetsListPage.waitForHeaderVisible();
        datasetsListPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        datasetsListPage.waitForEmptyStateLoaded();

        takeScreenshot(browser, toScreenshotName(DATA_PAGE_NAME, "empty"), getClass());

        log.info("Empty state message: " + datasetsListPage.getEmptyStateMessage());
    }
}
