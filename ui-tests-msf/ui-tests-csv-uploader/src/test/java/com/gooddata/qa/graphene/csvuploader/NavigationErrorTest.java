package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;

public class NavigationErrorTest extends AbstractCsvUploaderTest {

    @Test
    public void navigateToProjectsPageWhenInvalidProjectId() throws Exception {
        openUrl(String.format(DATA_UPLOAD_PAGE_URI_TEMPLATE, "nonExistingProjectIdL123321"));

        waitForStringInUrl("/projects.html#status=notAuthorized");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void showErrorOnUploadsPageWhenInvalidDatasetId() throws Exception {
        openUrl(String.format(CSV_DATASET_DETAIL_PAGE_URI_TEMPLATE, testParams.getProjectId(), "nonExistingDataset"));

        final String errorMessage = waitForFragmentVisible(csvDatasetMessageBar).waitForErrorMessageBar().getText();

        takeScreenshot(browser, "invalid-dataset-id", getClass());

        assertThat(errorMessage, containsString("The dataset you are looking for no longer exists."));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void showUploadsPageWhenBadUrlAfterExistingProjectId() throws Exception {
        openUrl(String.format(CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/this/is/bad/url", testParams.getProjectId()));

        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void redirectToErrorPageWhenInsufficientAccessRights() throws Exception {
        addViewerUserToProject();

        try {
            logout();
            signIn(true, UserRoles.VIEWER);

            openUrl(String.format(DATA_UPLOAD_PAGE_URI_TEMPLATE, testParams.getProjectId()));

            final String insufficientAccessHeader = waitForFragmentVisible(insufficientAccessRightsPage).getHeader1();

            takeScreenshot(browser, "insufficient-access-rights", getClass());

            assertThat(insufficientAccessHeader, containsString("you do not have access to the Load section."));
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }
}
