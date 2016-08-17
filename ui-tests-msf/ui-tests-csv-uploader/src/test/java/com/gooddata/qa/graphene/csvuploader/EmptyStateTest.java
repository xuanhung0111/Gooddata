package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.csvuploader.DatasetsListPage;

public class EmptyStateTest extends AbstractCsvUploaderTest {

    @Test(dependsOnGroups = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage().waitForHeaderVisible();
        DatasetsListPage.getInstance(browser).waitForAddDataButtonVisible();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage().waitForEmptyStateLoaded();
        takeScreenshot(browser, "empty-state", getClass());
        log.info("Empty state message: " + DatasetsListPage.getInstance(browser).getEmptyStateMessage());
    }
}
