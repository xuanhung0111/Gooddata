package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.*;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.graphene.Screenshots;

public class NotificationTest extends AbstractDLUINotificationTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Dlui-notification-test-" + System.currentTimeMillis();

        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        imapEditorUser = testParams.loadProperty("imap.editorUser");
        imapEditorPassword = testParams.loadProperty("imap.editorPassword");

        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
        technicalUserUri = testParams.loadProperty("technicalUserUri");
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "george")
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
    public void addDataWithEditorRole() {
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

    @Override
    protected void setDefaultSchemaForOutputStage() {
        setDefaultSchemaForOutputStage(getRestApiClient(technicalUser, technicalUserPassword),
                adsInstance.getId());
    }

    @Override
    protected void addUsersToProject() {
        try {
            addUserToProject(technicalUserUri, UserRoles.ADMIN);
            addUserToProject(testParams.getEditorProfileUri(), UserRoles.EDITOR);
        } catch (Exception e) {
            throw new IllegalStateException("There is an exception when adding users to project!",
                    e);
        }
    }

    @Override
    protected void addUsersToAdsInstance() {
        addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");
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

        List<ExecutionParameter> params =
                prepareParamsToUpdateADS("dropTableWithAdditionalFields_Person.txt",
                        "copyTableWithAdditionalFields_Drop_Person.txt");

        String executionUri =
                executeCloudConnectProcess(cloudconnectProcess, DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS,
                        params);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));

        checkFailedDataAddingResult();
        Screenshots.takeScreenshot(browser, screenshotName, getClass());
    }
}
