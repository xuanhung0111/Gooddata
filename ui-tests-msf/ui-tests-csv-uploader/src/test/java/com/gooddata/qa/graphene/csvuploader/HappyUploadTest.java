package com.gooddata.qa.graphene.csvuploader;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

public class HappyUploadTest extends AbstractCsvUploaderTest {

    @Test(dependsOnMethods = {"createProject"})
    public void checkCsvUploadHappyPath() {
        assertTrue(uploadCsv(PAYROLL)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
    }
}
