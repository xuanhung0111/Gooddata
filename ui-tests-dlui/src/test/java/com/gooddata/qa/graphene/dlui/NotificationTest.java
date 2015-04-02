package com.gooddata.qa.graphene.dlui;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

public class NotificationTest extends AbstractDLUINotificationTest {

    private static final String INITIAL_LDM_MAQL_FILE = "create-ldm.txt";

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        projectTitle = "Dlui-annie-dialog-test";

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        imapEditorUser = testParams.loadProperty("imap.editorUser");
        imapEditorPassword = testParams.loadProperty("imap.editorPassword");

        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
        technicalUserUri = testParams.loadProperty("technicalUserUri");
    }

    // Domain user will add george and annie to project
    @Test(dependsOnMethods = "createProject")
    public void addGeorgeAndAnnieUserToProject() throws ParseException, JSONException, IOException {
        addUserToProject(technicalUserUri, UserRoles.ADMIN);
        addUserToProject(testParams.getEditorProfileUri(), UserRoles.EDITOR);
    }

    @Test(dependsOnMethods = "addGeorgeAndAnnieUserToProject")
    public void initialData() {
        try {
            RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_DATA_EXPLORER);
        } catch (JSONException e) {
            throw new IllegalStateException("There is a problem when enable data explorer! ", e);
        }

        updateModelOfGDProject(maqlFilePath + INITIAL_LDM_MAQL_FILE);

        adsInstance =
                new ADSInstance().withName("ADS Instance for DLUI test").withAuthorizationToken(
                        testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(adsInstance);
        addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");

        setDefaultSchemaForOutputStage(getRestApiClient(technicalUser, technicalUserPassword),
                adsInstance.getId());
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");

        cloudconnectProcess =
                new ProcessInfo().withProjectId(testParams.getProjectId())
                        .withProcessName("Initial Data for ADS Instance").withProcessType("GRAPH");
        createCloudConnectProcess(cloudconnectProcess);
    }

    @Test(dependsOnMethods = "initialData", groups = "george")
    public void signInWithGeorge() {
        logout();
        signInAtUI(technicalUser, technicalUserPassword);
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
    public void addNewAttributeFromADSToLDM() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "george-add-new-attribute");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }

        checkSuccessfulDataAddingEmail(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
    public void addNewFactFromADSToLDM() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("opportunity").withFields(
                            new Field("Totalprice2", FieldTypes.FACT, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "george-add-new-fact");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedFactInLDM_Opportunity_Totalprice2.txt");
        }

        checkSuccessfulDataAddingEmail(requestTime, "Totalprice2");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
    public void addNewLabelFromADSToLDM() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("opportunity").withFields(
                            new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "george-add-new-label");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedLabelInLDM_Opportunity_Label.txt");
        }

        checkSuccessfulDataAddingEmail(requestTime, "Label");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
    public void addNewDateFieldFromADSToLDM() {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Date", FieldTypes.DATE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "george-add-new-date");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedDateInLDM_Person_Date.txt");
        }

        checkSuccessfulDataAddingEmail(requestTime, "Date");
    }

    @Test(dependsOnMethods = {"signInWithGeorge"}, groups = "george")
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
            dropAddedFieldsInLDM(maqlFilePath + "dropMultiAddedFieldsInLDM.txt");
        }

        checkSuccessfulDataAddingEmail(requestTime, "Date", "Label", "Position", "Totalprice2");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
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
            dropAddedFieldsInLDM(maqlFilePath + "deleteUnmappingField.txt");
        }

        checkFailedDataAddingEmail(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithGeorge", groups = "george")
    public void failToLoadDataForNewField() throws InterruptedException {

        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            failToLoadData(dataSource, "george-fail-to-load-data");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }

        checkFailedDataAddingEmail(requestTime, "Position");
    }

    @Test(dependsOnGroups = "george", groups = "annie", alwaysRun = true)
    public void signInWithAnnie() {
        logout();
        signInAtUI(testParams.getEditorUser(), testParams.getEditorPassword());
    }

    @Test(dependsOnMethods = "signInWithAnnie", groups = "annie")
    public void addDataWithEditorRole() throws ParseException, JSONException, IOException {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "annie-add-new-fields");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }

        checkSuccessfulDataAddingEmailForEditor(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithAnnie", groups = "annie")
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
            dropAddedFieldsInLDM(maqlFilePath + "deleteUnmappingField.txt");
        }

        checkFailedDataAddingEmail(requestTime, "Position");
        checkFailedDataAddingEmailForEditor(requestTime, "Position");
    }

    @Test(dependsOnMethods = "signInWithAnnie", groups = "annie")
    public void failToLoadDataForNewFieldWithEdiorRole() throws InterruptedException {
        long requestTime = System.currentTimeMillis();
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            failToLoadData(dataSource, "annie-fail-to-load-data");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }

        checkFailedDataAddingEmail(requestTime, "Position");
        checkFailedDataAddingEmailForEditor(requestTime, "Position");
    }

    @Test(dependsOnGroups = {"george", "annie"}, alwaysRun = true)
    public void cleanUp() {
        logout();
        signInAtUI(testParams.getUser(), testParams.getPassword());
        deleteADSInstance(adsInstance);
    }

    private void checkSuccessfulAddingData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        Screenshots.takeScreenshot(browser, screenshotName + "-select-fields", getClass());
        annieUIDialog.clickOnApplyButton();

        checkDataAddingProgress();
        checkSuccessfulDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName + "-result", getClass());
    }

    private void failToAddData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);

        updateModelOfGDProject(maqlFilePath + "addUnmappingFieldToLDM.txt");

        annieUIDialog.clickOnApplyButton();
        checkDataAddingProgress();
        checkFailedDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName, getClass());
    }

    private void failToLoadData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();

        // Delete existing ads table to make data load process failed
        executeProcess(
                cloudconnectProcess.getProcessId(),
                ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                        adsInstance.getId()), sqlFilePath
                        + "dropTableWithAdditionalFields_Person.txt", sqlFilePath
                        + "copyTableWithAdditionalFields_Drop_Person.txt");

        checkFailedDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName, getClass());
    }
}
