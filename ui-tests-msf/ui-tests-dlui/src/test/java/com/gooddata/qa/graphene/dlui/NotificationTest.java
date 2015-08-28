package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.graphene.utils.AdsHelper.AdsRole;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.warehouse.Warehouse;

public class NotificationTest extends AbstractDLUINotificationTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Dlui-notification-test-" + System.currentTimeMillis();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        imapEditorUser = testParams.loadProperty("imap.editorUser");
        imapEditorPassword = testParams.loadProperty("imap.editorPassword");

        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
    }

    @DataProvider(name = "basicNotificationData")
    protected static Object[][] provideBasicData() {
        return new Object[][] { {AddedFields.POSITION}, {AddedFields.TOTALPRICE2}};
    }

    @DataProvider(name = "extendedFieldData")
    protected static Object[][] provideExtendedData() {
        return new Object[][] { {AddedFields.LABEL}, {AddedFields.DATE},
            {AddedFields.POSITION_CONNECTION_POINT}};
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"george", "basicTest"})
    public void signInWithGeorge() {
        logout();
        signInAtUI(technicalUser, technicalUserPassword);
    }

    @Test(dataProvider = "basicNotificationData", dependsOnMethods = "signInWithGeorge", groups = {
        "george", "basicTest"})
    public void checkNotificationForAddingSingleBasicField(AddedFields addedField)
            throws JSONException {
        long requestTime = System.currentTimeMillis();
        addNewFieldAndCheckNotification(UserRoles.ADMIN, addedField, requestTime);
    }

    @Test(dataProvider = "extendedFieldData", dependsOnMethods = "signInWithGeorge",
            groups = {"george"})
    public void checkNotificationForAddingSingleField(AddedFields addedField) throws JSONException {
        long requestTime = System.currentTimeMillis();
        addNewFieldAndCheckNotification(UserRoles.ADMIN, addedField, requestTime);
    }

    @Test(dependsOnMethods = {"signInWithGeorge"}, groups = {"george"})
    public void addMultiFieldsFromADSToLDM() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset personDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Date", FieldTypes.DATE, FieldStatus.SELECTED),
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
            Dataset opportunityDataset =
                    new Dataset().withName("opportunity").withFields(
                            new Field("Totalprice2", FieldTypes.FACT, FieldStatus.SELECTED),
                            new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).updateDatasetStatus(
                            personDataset, opportunityDataset);

            checkSuccessfulAddingData(dataSource, "add-new-multi-fields");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropMultiAddedFieldsInLDM.txt"));
        }

        checkSuccessfulDataAddingEmail(requestTime, "Date", "Label", "Position", "Totalprice2");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = {"george"})
    public void failToAddNewField() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            failToAddData(dataSource, "george-fail-to-add-new-field");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/deleteUnmappingField.txt"));
        }

        checkFailedDataAddingEmail(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = {"george"})
    public void failToLoadDataForNewField() {

        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA).updateDatasetStatus(
                            selectedDataset);

            failToLoadData(dataSource, "george-fail-to-load-data");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }

        checkFailedDataAddingEmail(requestTime, "Position");
    }

    @Test(dependsOnGroups = "george", groups = {"basicTest"}, alwaysRun = true)
    public void signInWithAnnie() {
        logout();
        signInAtUI(testParams.getEditorUser(), testParams.getEditorPassword());
    }

    @Test(dataProvider = "basicNotificationData", dependsOnMethods = "signInWithAnnie", groups = {
        "basicTest"})
    public void checkBasicNotificationWithEditorRole(AddedFields addedField) throws JSONException {
        long requestTime = System.currentTimeMillis();
        addNewFieldAndCheckNotification(UserRoles.EDITOR, addedField, requestTime);
    }

    @Test(dataProvider = "extendedFieldData", dependsOnMethods = "signInWithAnnie")
    public void checkExtendedNotificationWithEditor(AddedFields addedField) throws JSONException {
        long requestTime = System.currentTimeMillis();
        addNewFieldAndCheckNotification(UserRoles.EDITOR, addedField, requestTime);
    }

    @Test(dependsOnMethods = "signInWithAnnie")
    public void failToAddNewFieldWithEditorRole() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            failToAddData(dataSource, "annie-fail-to-add-new-field");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/deleteUnmappingField.txt"));
        }

        checkFailedDataAddingEmail(requestTime, "Position");
        checkFailedDataAddingEmailForEditor(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithAnnie")
    public void failToLoadDataForNewFieldWithEdiorRole() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA).updateDatasetStatus(
                            selectedDataset);

            failToLoadData(dataSource, "annie-fail-to-load-data");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }

        checkFailedDataAddingEmail(requestTime, "Position");
        checkFailedDataAddingEmailForEditor(requestTime, "Position");
    }

    @AfterClass
    public void cleanUp() {
        logout();
        signInAtUI(testParams.getUser(), testParams.getPassword());
        deleteADSInstance(ads);
    }

    @Override
    protected void setDefaultSchemaForOutputStage(Warehouse ads) {
        new AdsHelper(getGoodDataClient(), getRestApiClient(technicalUser, technicalUserPassword))
                .associateAdsWithProject(ads, testParams.getProjectId());
    }

    @Override
    protected void addUsersToProject() {
        try {
            addUserToProject(technicalUser, UserRoles.ADMIN);
            addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when adding users to project!",
                    e);
        }
    }

    @Override
    protected void addUsersToAdsInstance() {
        adsHelper.addUserToAdsInstance(ads, technicalUser, AdsRole.DATA_ADMIN);
    }

    private void failToAddData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);

        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/addUnmappingFieldToLDM.txt"));

        annieUIDialog.clickOnApplyButton();
        checkDataAddingProgress();
        checkFailedDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName, getClass());
    }

    private void failToLoadData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();

        Collection<ExecutionParameter> params =
                prepareParamsToUpdateADS("dropTableWithAdditionalFields_Person.txt",
                        "copyTableWithAdditionalFields_Drop_Person.txt", ads.getId());

        String executionUri =
                executeCloudConnectProcess(cloudconnectProcess,
                        DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS, params);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));

        checkFailedDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName, getClass());
    }

    private void addNewFieldAndCheckNotification(UserRoles role, AddedFields addedField,
            long requestTime) throws JSONException {
        try {
            checkNewDataAdding(role, addedField);
            checkSuccessfulDataAddingNotification(role, requestTime, addedField.getField()
                    .getName());
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/"
                    + addedField.getCleanupMaqlFile()));
        }
    }
}
