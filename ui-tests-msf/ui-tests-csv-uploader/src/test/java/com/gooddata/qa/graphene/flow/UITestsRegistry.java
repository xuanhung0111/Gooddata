package com.gooddata.qa.graphene.flow;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;

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

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Set<Object> tests = new HashSet<>();

        for (String suite: args) {
            if ("all".equals(suite)) {
                tests.addAll(asList(
                    DatasetDetailTest.class,
                    DataPreviewAfterUploadTest.class,
                    DeleteDatasetTest.class,
                    EmptyStateTest.class,
                    UploadErrorTest.class,
                    UploadTest.class,
                    UploadDateTest.class,
                    ProjectSwitchTest.class,
                    TooltipValidationTest.class,
                    "testng-csv-permissions-DataOfOtherUsers.xml",
                    "testng-csv-permissions-RefreshTest.xml",
                    "testng-csv-permissions-NavigationError.xml",
                    "testng-csv-permissions-UploadHistoryInfo.xml",
                    "testng-csv-imap-Notification.xml"
                ));
            }
        }

        TestsRegistry.getInstance()
            .register(tests)
            .toTextFile();
    }
}
