package com.gooddata.qa.graphene.dlui;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.enums.ADSTables;
import com.gooddata.qa.graphene.enums.AdditionalDatasets;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public class AnnieDialogTest extends AbstractDLUITest {

    private static final String INITIAL_LDM_MAQL_FILE = "create-ldm.txt";

    private static final String DEFAULT_DATA_SOURCE_NAME = "Unknown data source";

    private static final String ADS_URL =
            "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";

    private ProcessInfo cloudconnectProcess;
    private ADSInstance adsInstance;

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        projectTitle = "Dlui-annie-dialog-test";
    }

    @Test(dependsOnMethods = "createProject")
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

        setDefaultSchemaForOutputStage(adsInstance.getId());
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");

        cloudconnectProcess =
                new ProcessInfo().withProjectId(testParams.getProjectId())
                        .withProcessName("Initial Data for ADS Instance").withProcessType("GRAPH");
        createCloudConnectProcess(cloudconnectProcess);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkEmptyStateInAnnieDialog() {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkEmptyAnnieDialog();
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalFields() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "available-additional-fields", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalAttributes() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ATTRIBUTE);
        Screenshots.takeScreenshot(browser, "available-additional-attributes", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalFacts() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.FACT);
        Screenshots.takeScreenshot(browser, "available-additional-facts", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalLabelHyperlink() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.LABEL_HYPERLINK);
        Screenshots.takeScreenshot(browser, "available-additional-label-hyperlinks", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkAdditionalDateField() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
        Screenshots.takeScreenshot(browser, "available-additional-date", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkEmptyStateWithDateFilter() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkSearchAllFields() {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        Field field = new Field("Position", FieldTypes.ATTRIBUTE);
        Dataset dataset =
                new Dataset().withName(AdditionalDatasets.PERSON_WITH_NEW_FIELDS.getName())
                        .withFields(field);
        DataSource dataSource =
                new DataSource().withName(DEFAULT_DATA_SOURCE_NAME).withDatasets(dataset);

        openAnnieDialog();
        annieUIDialog.enterSearchKey("Pos");
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkErrorMappingOnAnnieDialog() {
        // Prepare an ADS which doesn't contain some fields in LDM
        createUpdateADSTable(ADSTables.WITH_ERROR_MAPPING);

        openAnnieDialog();
        annieUIDialog.assertErrorMessage();
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void selectAndDeselectFields() {
        Field field1 = new Field("Position", FieldTypes.ATTRIBUTE);
        Field field2 = new Field("Title2", FieldTypes.ATTRIBUTE);

        Dataset dataset1 =
                new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS).withSelectedFields(field1);
        Dataset dataset2 =
                new Dataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS)
                        .withSelectedFields(field2);

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset1,
                        dataset2);

        openAnnieDialog();
        annieUIDialog.assertNoDataSelectedState();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.checkSelectionArea(Lists.newArrayList(field2, field1));
        annieUIDialog.assertSelectedFieldNumber(dataSource);
        annieUIDialog.deselectFields(dataSource);
        annieUIDialog.assertNoDataSelectedState();
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 0)
    public void checkCancelAddNewFieldFromADSToLDM() {
        Dataset dataset =
                new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS)
                        .withSelectedFields(new Field("Position", FieldTypes.ATTRIBUTE));

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnDismissButton();

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "cancel-add-new-field", getClass());
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void addNewAttributeFromADSToLDM() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS)
                            .withSelectedFields(new Field("Position", FieldTypes.ATTRIBUTE));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

            checkSuccessfulAddingData(dataSource, "add-new-attribute");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void addNewFactFromADSToLDM() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS)
                            .withSelectedFields(new Field("Totalprice2", FieldTypes.FACT));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

            checkSuccessfulAddingData(dataSource, "add-new-fact");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedFactInLDM_Opportunity_Totalprice2.txt");
        }
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void addNewLabelFromADSToLDM() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS)
                            .withSelectedFields(new Field("Label", FieldTypes.LABEL_HYPERLINK));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

            checkSuccessfulAddingData(dataSource, "add-new-label");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedLabelInLDM_Opportunity_Label.txt");
        }
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void addNewDateFieldFromADSToLDM() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.PERSON_WITH_NEW_DATE_FIELD)
                            .withSelectedFields(new Field("Date", FieldTypes.DATE));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).withSelectedDatasets(dataset);

            checkSuccessfulAddingData(dataSource, "add-new-date");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedDateInLDM_Person_Date.txt");
        }
    }

    @Test(dependsOnMethods = {"addNewAttributeFromADSToLDM", "addNewFactFromADSToLDM",
        "addNewLabelFromADSToLDM", "addNewDateFieldFromADSToLDM"}, groups = "annieDialogTest",
            priority = 1)
    public void addMultiFieldsFromADSToLDM() {
        try {
            Dataset personDataset =
                    new Dataset(AdditionalDatasets.PERSON_WITH_NEW_DATE_FIELD).withSelectedFields(
                            new Field("Date", FieldTypes.DATE), new Field("Position",
                                    FieldTypes.ATTRIBUTE));
            Dataset opportunityDataset =
                    new Dataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS).withSelectedFields(
                            new Field("Totalprice2", FieldTypes.FACT), new Field("Label",
                                    FieldTypes.LABEL_HYPERLINK));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).withSelectedDatasets(
                            personDataset, opportunityDataset);

            checkSuccessfulAddingData(dataSource, "add-new-multi-fields");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropMultiAddedFieldsInLDM.txt");
        }
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void checkFailToAddNewField() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS)
                            .withSelectedFields(new Field("Position", FieldTypes.ATTRIBUTE));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

            openAnnieDialog();
            annieUIDialog.selectFields(dataSource);

            updateModelOfGDProject(maqlFilePath + "addUnmappingFieldToLDM.txt");

            annieUIDialog.clickOnApplyButton();
            annieUIDialog.checkDataAddingProgress();
            annieUIDialog.checkFailedDataAddingResult();
            Screenshots.takeScreenshot(browser, "add-new-field-failed", getClass());
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "deleteUnmappingField.txt");
        }
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest", priority = 1)
    public void addNewFieldsToLDMWithEditorRole() {
        try {
            Dataset dataset =
                    new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS)
                            .withSelectedFields(new Field("Position", FieldTypes.ATTRIBUTE));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).withSelectedDatasets(dataset);

            try {
                addUserToProject(testParams.getEditorProfileUri(), UserRoles.EDITOR);
            } catch (Exception e) {
                throw new IllegalStateException("There is exeception when adding user to project!",
                        e);
            }

            logout();

            try {
                signInAtUI(testParams.getEditorUser(), testParams.getEditorPassword());
            } catch (Exception e) {
                signInAtUI(testParams.getUser(), testParams.getPassword());
                throw new IllegalStateException(
                        "There is an exception when signIn as Editor user!", e);
            }

            checkSuccessfulAddingData(dataSource, "add-new-fields-with-editor-role");
        } finally {
            logout();
            signInAtUI(testParams.getUser(), testParams.getPassword());
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }
    }

    @Test(dependsOnGroups = "annieDialogTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private void checkSuccessfulAddingData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        Screenshots.takeScreenshot(browser, "select-fields-to-" + screenshotName, getClass());
        annieUIDialog.clickOnApplyButton();

        annieUIDialog.checkDataAddingProgress();
        annieUIDialog.checkSuccessfulDataAddingResult();
        Screenshots.takeScreenshot(browser, "sucessful-" + screenshotName, getClass());
        annieUIDialog.clickOnCloseButton();

        dataSource.removeAddedDataset();

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    private DataSource prepareADSTable(ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        DataSource dataSource = new DataSource(adsTable);

        return dataSource;
    }

    private void createUpdateADSTable(ADSTables adsTable) {
        executeProcess(
                cloudconnectProcess.getProcessId(),
                ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                        adsInstance.getId()), sqlFilePath + adsTable.getCreateTableSqlFile(),
                sqlFilePath + adsTable.getCopyTableSqlFile());
    }
}
