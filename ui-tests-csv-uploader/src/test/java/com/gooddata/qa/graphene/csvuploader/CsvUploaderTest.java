package com.gooddata.qa.graphene.csvuploader;

import java.io.File;

import com.gooddata.qa.graphene.fragments.csvuploader.DataUploadPage;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.*;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.utils.io.ResourceUtils;

public class CsvUploaderTest extends AbstractProjectTest {

    private static final String DATA_UPLOAD_PAGE_URI = "data/#/project/%s/datasets";

    @FindBy(css = "#app-data")
    private DataUploadPage dataUploadPage;


    @BeforeClass
    public void initProperties() {
        projectTitle = "Csv-uploader-test";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataUploadPageHeader() {
        initDataUploadPage();
        dataUploadPage.waitForHeaderVisible();
        dataUploadPage.waitForAddDataButtonVisible();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyState() {
        initDataUploadPage();
        dataUploadPage.waitForEmptyStateLoaded();
        System.out.println("Empty state message: " + dataUploadPage.getEmptyStateMessage());
    }

    @Test(dependsOnMethods = "checkEmptyState")
    public void getCsvFileToUpload() {
        File csvFile =
                ResourceUtils
                        .getResourceAsFile("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv");
        assertTrue(csvFile.exists());
    }

    public void initDataUploadPage() {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI, testParams.getProjectId()));
        waitForFragmentVisible(dataUploadPage);
    }

}
