package com.gooddata.qa.graphene.upload;

import com.gooddata.qa.graphene.AbstractUITest;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = { "uploadTests", "brokenTest" }, description = "Simple upload test (using upload.html page) - deprecated, replaced with UploadTests")
public class SimpleUploadTest extends AbstractUITest {

    private String csvFilePath;

    @FindBy(css = ".l-primary")
    private UploadFragment upload;

    @BeforeClass
    public void initStartPage() {
        startPage = PAGE_UPLOAD;

        csvFilePath = testParams.loadProperty("upload.file.simple.csv");
    }

    @Test(groups = {"SimpleUploadInit"})
    public void init() throws JSONException {
        // sign in with demo user
        validSignInWithDemoUser(false);
    }

    @Test(dependsOnGroups = {"SimpleUploadInit"})
    public void testUploadFile() throws InterruptedException {
        waitForElementVisible(upload.getRoot());
        Screenshots.takeScreenshot(browser, "1-upload-csv", this.getClass());
        // upload file
        upload.uploadFile(csvFilePath);
        UploadColumns uploadColumns = upload.getUploadColumns();
        System.out.println(uploadColumns.getNumberOfColumns() + " columns are available for upload, " +
                uploadColumns.getColumnNames() + " ," + uploadColumns.getColumnTypes());
        // change label of first column
        uploadColumns.setColumnName(0, "Test");
        // change column type of eighth column
        uploadColumns.setColumnType(7, UploadColumns.OptionDataType.TEXT);
        Screenshots.takeScreenshot(browser, "2-upload-definition", this.getClass());
        //confirm CSV load
        upload.confirmloadCsv();
        //verify redirect to dashboard
        waitForElementVisible(By.cssSelector(".s-we_made_some_kpis_for_you"), browser);
        waitForDashboardPageLoaded(browser);
        Screenshots.takeScreenshot(browser, "3-upload-dashboard", this.getClass());
    }
}
