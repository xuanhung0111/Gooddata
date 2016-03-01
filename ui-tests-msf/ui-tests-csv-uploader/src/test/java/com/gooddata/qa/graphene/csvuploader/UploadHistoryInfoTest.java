package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;

import java.io.IOException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class UploadHistoryInfoTest extends AbstractCsvUploaderTest {

    private static final String DATASET_NAME = "Check Upload Info";

    private static final String DATE_FORMAT = "Today at \\d+:\\d+ (AM|PM)";

    private String otherAdminUser;
    private String otherAdminPassword;

    private CsvFile csvFile;

    @Test(dependsOnMethods = {"createProject"})
    public void inviteUser() throws ParseException, IOException, JSONException {
        otherAdminUser = testParams.getEditorUser();
        otherAdminPassword = testParams.getEditorPassword();

        addUserToProject(otherAdminUser, UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = "inviteUser")
    public void checkInfoWhenAddingData() throws IOException {
        csvFile = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"))
                .rows("Khoa", "100000");
        csvFile.saveToDisc(testParams.getCsvFolder());

        initDataUploadPage();

        uploadCsv(csvFile);
        WebElement progressLoadingItem = Dataset.waitForProgressLoadingItem(browser);

        takeScreenshot(browser, "Adding-csv-data-progress", getClass());
        assertEquals(progressLoadingItem.getText(), "Adding data ...");

        waitForElementNotPresent(progressLoadingItem);

        takeScreenshot(browser, "Date-format-show-in-Date-Created-column", getClass());
        assertTrue(datasetsListPage
                .getMyDatasetsTable()
                .getDataset(DATASET_NAME)
                .getCreatedDate()
                .matches(DATE_FORMAT), "Date format is invalid");
    }

    @Test(dependsOnMethods = "checkInfoWhenAddingData")
    public void checkInfoWhenUpdatingData() throws JSONException, ParseException, IOException {
        String adminUserName = getFullNameOf(testParams.getUser());
        String otherAdminUserName = getFullNameOf(otherAdminUser);

        logout();
        signInAtGreyPages(otherAdminUser, otherAdminPassword);

        initDataUploadPage();

        Dataset dataset = datasetsListPage.getOthersDatasetsTable().getDataset(DATASET_NAME);

        takeScreenshot(browser, "Date-format-show-in-Date-Created-column-by-another-user", getClass());
        assertTrue(dataset
                .getCreatedDate()
                .matches(DATE_FORMAT + " by " + adminUserName), "Date format is invalid");

        dataset.clickUpdateButton();

        uploadCsv(csvFile.getFilePath());
        WebElement progressLoadingItem = Dataset.waitForProgressLoadingItem(browser);

        takeScreenshot(browser, "Updating-csv-data-progress", getClass());
        assertEquals(progressLoadingItem.getText(), "Updating data ...");

        waitForElementNotPresent(progressLoadingItem);

        takeScreenshot(browser, "Date-format-show-in-Date-Updated-column", getClass());
        assertTrue(dataset
                .getUpdatedDate()
                .matches(DATE_FORMAT), "Date format is invalid");

        logout();
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

        initDataUploadPage();

        takeScreenshot(browser, "Date-format-show-in-Date-Updated-column-by-another-user", getClass());
        assertTrue(datasetsListPage
                .getMyDatasetsTable()
                .getDataset(DATASET_NAME)
                .getUpdatedDate()
                .matches(DATE_FORMAT + " by " + otherAdminUserName), "Date format is invalid");
    }

    private void uploadCsv(String csvFilePath) {
        Graphene.createPageFragment(FileUploadDialog.class,
                waitForElementVisible(By.className("s-upload-dialog"), browser))
                .pickCsvFile(csvFilePath)
                .clickUploadButton();

        Graphene.createPageFragment(DataPreviewPage.class,
                waitForElementVisible(By.className("s-data-preview"), browser))
                .triggerIntegration();
    }

    private String getFullNameOf(String userEmail) throws ParseException, IOException, JSONException {
        JSONObject userInfo = UserManagementRestUtils.getUserProfileByEmail(getRestApiClient(), userEmail);
        return userInfo.getString("firstName") + " " + userInfo.getString("lastName");
    }
}
