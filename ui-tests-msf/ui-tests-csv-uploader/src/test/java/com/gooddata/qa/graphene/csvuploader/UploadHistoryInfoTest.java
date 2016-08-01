package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class UploadHistoryInfoTest extends AbstractCsvUploaderTest {

    private static final String DATASET_NAME = "Check Upload Info";

    private static final String DATE_FORMAT = "Today at \\d+:\\d+ (AM|PM)";

    private String otherAdminUser;
    private String otherAdminPassword;

    private CsvFile csvFile;

    @Test(dependsOnGroups = {"createProject"}, groups = "precondition")
    public void inviteUser() throws ParseException, IOException, JSONException {
        otherAdminUser = testParams.getEditorUser();
        otherAdminPassword = testParams.getEditorPassword();

        addUserToProject(otherAdminUser, UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = "precondition", groups = "csv")
    public void checkInfoWhenAddingData() throws IOException {
        csvFile = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"))
                .rows("Khoa", "100000");
        csvFile.saveToDisc(testParams.getCsvFolder());

        initDataUploadPage()
            .uploadFile(csvFile.getFilePath())
            .triggerIntegration();

        try {
            final String addingText = Dataset.waitForDatasetLoading(browser).getText();
            takeScreenshot(browser, "Adding-csv-data-progress", getClass());

            assertEquals(addingText, "Adding data ...");
        } catch (NoSuchElementException | TimeoutException e) {
            log.info("Selenium is too slow to capture the adding dataset process");
            log.info("Skip checking!");
        }

        Dataset.waitForDatasetLoaded(browser);
        takeScreenshot(browser, "Date-format-show-in-Date-Created-column", getClass());

        assertTrue(datasetsListPage
                .getMyDatasetsTable()
                .getDataset(DATASET_NAME)
                .getCreatedDate()
                .matches(DATE_FORMAT), "Date format is invalid");
    }

    @Test(dependsOnMethods = "checkInfoWhenAddingData", groups = "csv")
    public void checkInfoWhenUpdatingData() throws JSONException, ParseException, IOException {
        final String adminUserName = getFullNameOf(testParams.getUser());
        final String otherAdminUserName = getFullNameOf(otherAdminUser);

        logout();
        signInAtGreyPages(otherAdminUser, otherAdminPassword);

        final Dataset dataset = initDataUploadPage().getOthersDatasetsTable().getDataset(DATASET_NAME);
        takeScreenshot(browser, "Date-format-show-in-Date-Created-column-by-another-user", getClass());

        assertTrue(dataset.getCreatedDate().matches(DATE_FORMAT + " by " + adminUserName), "Date format is invalid");

        initDataUploadPage()
            .updateCsv(dataset, csvFile.getFilePath())
            .triggerIntegration();

        try {
            final String updatingText = Dataset.waitForDatasetLoading(browser).getText();
            takeScreenshot(browser, "Updating-csv-data-progress", getClass());

            assertEquals(updatingText, "Updating data ...");
        } catch (NoSuchElementException | TimeoutException e) {
            log.info("Selenium is too slow to capture the updating dataset process");
            log.info("Skip checking!");
        }

        Dataset.waitForDatasetLoaded(browser);
        takeScreenshot(browser, "Date-format-show-in-Date-Updated-column", getClass());

        assertTrue(dataset.getUpdatedDate().matches(DATE_FORMAT), "Date format is invalid");

        logoutAndLoginAs(true, UserRoles.ADMIN);

        initDataUploadPage();
        takeScreenshot(browser, "Date-format-show-in-Date-Updated-column-by-another-user", getClass());

        assertTrue(datasetsListPage.getMyDatasetsTable()
                .getDataset(DATASET_NAME)
                .getUpdatedDate()
                .matches(DATE_FORMAT + " by " + otherAdminUserName), "Date format is invalid");
    }

    private String getFullNameOf(String userEmail) throws ParseException, IOException, JSONException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        JSONObject userInfo = UserManagementRestUtils.getUserProfileByEmail(restApiClient, testParams.getUserDomain(),
                userEmail);
        return userInfo.getString("firstName") + " " + userInfo.getString("lastName");
    }
}
