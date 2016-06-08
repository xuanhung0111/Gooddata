package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.csvuploader.InsufficientAccessRightsPage;

public class NavigationErrorTest extends AbstractCsvUploaderTest {

    @FindBy(className = "s-insufficient-access-rights")
    private InsufficientAccessRightsPage insufficientAccessRightsPage;

    @Test
    public void navigateToProjectsPageWhenInvalidProjectId() {
        openUrl(format(DATA_UPLOAD_PAGE_URI_TEMPLATE, "nonExistingProjectIdL123321"));
        waitForStringInUrl("/projects.html#status=notAuthorized");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = "csv")
    public void showErrorOnUploadsPageWhenInvalidDatasetId() {
        openUrl(format(CSV_DATASET_DETAIL_PAGE_URI_TEMPLATE, testParams.getProjectId(), "nonExistingDataset"));
        final String errorMessage = DatasetMessageBar.getInstance(browser).waitForErrorMessageBar().getText();
        takeScreenshot(browser, "invalid-dataset-id", getClass());
        assertThat(errorMessage, containsString("The dataset you are looking for no longer exists."));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = "csv")
    public void showUploadsPageWhenBadUrlAfterExistingProjectId() {
        openUrl(format(CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/this/is/bad/url", testParams.getProjectId()));
        waitForFragmentVisible(datasetsListPage);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = "csv")
    public void redirectToErrorPageWhenInsufficientAccessRights() throws ParseException, IOException, JSONException {
        addViewerUserToProject();

        try {
            logoutAndLoginAs(true, UserRoles.VIEWER);

            openUrl(format(DATA_UPLOAD_PAGE_URI_TEMPLATE, testParams.getProjectId()));
            final String insufficientAccessHeader = waitForFragmentVisible(insufficientAccessRightsPage).getHeader1();
            takeScreenshot(browser, "insufficient-access-rights", getClass());
            assertThat(insufficientAccessHeader, containsString("you do not have access to the Load section."));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
