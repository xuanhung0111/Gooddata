package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.csvuploader.DataPreviewAfterUploadTest;
import com.gooddata.qa.graphene.csvuploader.DatasetDetailTest;
import com.gooddata.qa.graphene.csvuploader.DeleteDatasetTest;
import com.gooddata.qa.graphene.csvuploader.EmptyStateTest;
import com.gooddata.qa.graphene.csvuploader.ProjectSwitchTest;
import com.gooddata.qa.graphene.csvuploader.TooltipValidationTest;
import com.gooddata.qa.graphene.csvuploader.UploadDateTest;
import com.gooddata.qa.graphene.csvuploader.UploadErrorTest;
import com.gooddata.qa.graphene.csvuploader.UploadTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class AllTestsRegistry {

    public static void main(String[] args) throws Throwable {
        TestsRegistry.getInstance()
            .register(DatasetDetailTest.class)
            .register(DataPreviewAfterUploadTest.class)
            .register(DeleteDatasetTest.class)
            .register(EmptyStateTest.class)
            .register(UploadErrorTest.class)
            .register(UploadTest.class)
            .register(UploadDateTest.class)
            .register(ProjectSwitchTest.class)
            .register(TooltipValidationTest.class)
            .register("testng-csv-permissions-DataOfOtherUsers.xml")
            .register("testng-csv-permissions-RefreshTest.xml")
            .register("testng-csv-permissions-NavigationError.xml")
            .register("testng-csv-permissions-UploadHistoryInfo.xml")
            .register("testng-csv-imap-Notification.xml")
            .toTextFile();
    }
}
