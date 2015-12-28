package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AnnieDialogTest extends AbstractAnnieDialogTest {

    private static final String DEFAULT_DATA_SOURCE_NAME = "Unknown data source";
    private static final String DATA_CAN_NOT_ADD_HEADLINE = "Data cannot be added right now";
    private static final String ADDING_DATA_HEADLINE = "Adding data...";
    private static final String DIALOG_STATE_FIRST_MESSAGE =
            "We’re sorry, but it’s not possible to add new data at the moment."
                    + " Other data is currently being uploaded into the project "
                    + "and due to technical limitations it is not possible to add new data at the same time.";
    private static final String DIALOG_STATE_SECOND_MESSAGE = "It can take a while so please come later.";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Dlui-annie-dialog-test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest"}, priority = 0)
    public void checkEmptyStateInAnnieDialog() {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkEmptyAnnieDialog();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest"}, priority = 0)
    public void checkAvailableAdditionalFields() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "available-additional-fields", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkAvailableAdditionalAttributes() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ATTRIBUTE);
        Screenshots.takeScreenshot(browser, "available-additional-attributes", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkAvailableAdditionalFacts() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.FACT);
        Screenshots.takeScreenshot(browser, "available-additional-facts", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkAvailableAdditionalLabelHyperlink() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.LABEL_HYPERLINK);
        Screenshots.takeScreenshot(browser, "available-additional-label-hyperlinks", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkAdditionalDateField() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
        Screenshots.takeScreenshot(browser, "available-additional-date", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkEmptyStateWithDateFilter() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkSearchAllFields() {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        Field field = new Field("Position", FieldTypes.ATTRIBUTE);
        Dataset dataset = new Dataset().withName("person").withFields(field);
        DataSource dataSource = new DataSource().withName(DEFAULT_DATA_SOURCE_NAME).withDatasets(dataset);

        openAnnieDialog();
        annieUIDialog.enterSearchKey("Pos");
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "search-additional-fields", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkErrorMappingOnAnnieDialog() {
        // Prepare an ADS which doesn't contain some fields in LDM
        createUpdateADSTable(ADSTables.WITH_ERROR_MAPPING);

        openAnnieDialog();
        assertErrorMessage();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void selectAndDeselectFields() {
        Field selectedField1 = new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED);
        Field selectedField2 = new Field("Title2", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED);

        Dataset selectedDataset1 = new Dataset().withName("person").withFields(selectedField1);
        Dataset selectedDataset2 = new Dataset().withName("opportunity").withFields(selectedField2);

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(selectedDataset1,
                        selectedDataset2);

        openAnnieDialog();
        assertNoDataSelectedState();
        annieUIDialog.selectFields(dataSource);
        checkSelectionArea(Lists.newArrayList(selectedField2, selectedField1));
        checkSelectedFieldNumber(dataSource);
        annieUIDialog.deselectFields(dataSource);
        assertNoDataSelectedState();
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkCancelAddNewFieldFromADSToLDM() {
        Dataset selectedDataset =
                new Dataset().withName("person").withFields(
                        new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(selectedDataset);

        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnDismissButton();

        dataSource.cancelAddSelectedFields();

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "cancel-add-new-field", getClass());
        annieUIDialog.clickOnDismissButton();
    }

    @Test(dataProvider = "basicFieldData", dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest",
        "addOneField"}, priority = 1)
    public void adminAddSingleBasicFieldFromADSToLDM(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewDataAddingAndCleanAddedData(UserRoles.ADMIN, addedField);
    }

    @Test(dataProvider = "newFieldData", dependsOnGroups = {"initialDataForDLUI"}, groups = {"addOneField"},
            priority = 1)
    public void adminAddSingleFieldFromADSToLDM(AddedFields addedField, ReportWithAddedFields reportWithAddedFields)
            throws JSONException {
        checkNewDataAddingAndCleanAddedData(UserRoles.ADMIN, addedField);
    }

    @Test(dependsOnGroups = {"addOneField"}, priority = 1)
    public void adminAddMultiFieldsFromADSToLDM() throws JSONException {
        addMultiFieldsAndAssertAnnieDialog(UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"basicTest"}, priority = 1)
    public void addEditorUser() {
        try {
            addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when adding user to project!", e);
        }
    }

    @Test(dataProvider = "basicFieldData", dependsOnMethods = {"addEditorUser"}, groups = {"basicTest",
        "editorAddOneField"}, priority = 1)
    public void editorAddSingleBaiscFieldFromADSToLDM(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewDataAddingAndCleanAddedData(UserRoles.EDITOR, addedField);
    }

    @Test(dataProvider = "newFieldData", dependsOnMethods = {"addEditorUser"}, groups = {"editorAddOneField"},
            priority = 1)
    public void editorAddSingleFieldFromADSToLDM(AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) throws JSONException {
        checkNewDataAddingAndCleanAddedData(UserRoles.EDITOR, addedField);
    }

    @Test(dependsOnGroups = {"editorAddOneField"}, priority = 1)
    public void editorAddMultiFieldsFromADSToLDM() throws JSONException {
        addMultiFieldsAndAssertAnnieDialog(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 1)
    public void checkFailToAddNewField() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(selectedDataset);

            openAnnieDialog();
            annieUIDialog.selectFields(dataSource);

            updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/addUnmappingFieldToLDM.txt"));

            annieUIDialog.clickOnApplyButton();
            checkDataAddingProgress();
            checkFailedDataAddingResult();
            Screenshots.takeScreenshot(browser, "add-new-field-failed", getClass());
            annieUIDialog.clickOnCloseButton();

            openAnnieDialog();
            assertErrorMessage();
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/deleteUnmappingField.txt"));
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"})
    public void checkConcurrentDataLoadWithAnnieDialog() {
        Dataset dataset =
                new Dataset().withName("person").withFields(
                        new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA).updateDatasetStatus(dataset);
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();
        try {
            waitForFragmentVisible(annieUIDialog);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return ADDING_DATA_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline());
                }
            });
            sleepTight(500);
            annieUIDialog.clickOnCloseButton();
            openAnnieDialog();
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return DATA_CAN_NOT_ADD_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline());
                }
            });

            assertEquals(annieUIDialog.getIntegrationFirstMessage(), DIALOG_STATE_FIRST_MESSAGE);
            assertEquals(annieUIDialog.getIntegrationSecondMessage(), DIALOG_STATE_SECOND_MESSAGE);
            assertFalse(annieUIDialog.isPositiveButtonPresent());
            Screenshots.takeScreenshot(browser, "Concurrent-Dataload-Annie-Dialog", getClass());
            annieUIDialog.clickOnCloseButton();

            waitForAddingDataTask();
            dataSource.applyAddSelectedFields();
            openAnnieDialog();
            checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkCustomDataSourceName() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            DataSource fbDataSource = new DataSource().withName("fb").withDatasets(personDataset);

            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);
            DataSource gaDataSource =
                    new DataSource().withName("ga").withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(fbDataSource, gaDataSource);

            openAnnieDialog();
            checkAvailableAdditionalFields(fbDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-1", getClass());
            checkAvailableAdditionalFields(gaDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-2", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            deleteOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkDataSourceNameCaseSensitive() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);

            DataSource gaDataSource =
                    new DataSource().withName("ga").withDatasets(personDataset, opportunityDataset);
            DataSource GADataSource =
                    new DataSource().withName("GA").withDatasets(personDataset, opportunityDataset);
            DataSource GoogleAnalyticsDataSource =
                    new DataSource().withName("Google Analytics").withDatasets(personDataset, opportunityDataset);
            DataSource GoogleanalyticsDataSource =
                    new DataSource().withName("Google analytics").withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(gaDataSource, GADataSource, GoogleAnalyticsDataSource,
                    GoogleanalyticsDataSource);

            openAnnieDialog();
            checkAvailableAdditionalFields(gaDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-ga", getClass());
            checkAvailableAdditionalFields(GADataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-GA", getClass());
            checkAvailableAdditionalFields(GoogleAnalyticsDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-GoogleAnalytics", getClass());
            checkAvailableAdditionalFields(GoogleanalyticsDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-custom-data-source-name-Googleanalytics", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            deleteOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkUnicodeDataSourceName() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);

            DataSource unicodeSourceName1 =
                    new DataSource().withName("řeč").withDatasets(personDataset, opportunityDataset);
            DataSource unicodeSourceName2 =
                    new DataSource().withName("ພາສາລາວ").withDatasets(personDataset, opportunityDataset);
            DataSource unicodeSourceName3 =
                    new DataSource().withName("résumé").withDatasets(personDataset, opportunityDataset);
            DataSource unicodeSourceName4 =
                    new DataSource().withName("Tiếng Việt").withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(unicodeSourceName1, unicodeSourceName2, unicodeSourceName3,
                    unicodeSourceName4);

            openAnnieDialog();
            checkAvailableAdditionalFields(unicodeSourceName1, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-unicode-data-source-name-1", getClass());
            checkAvailableAdditionalFields(unicodeSourceName2, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-unicode-data-source-name-2", getClass());
            checkAvailableAdditionalFields(unicodeSourceName3, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-unicode-data-source-name-3", getClass());
            checkAvailableAdditionalFields(unicodeSourceName4, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-unicode-data-source-name-4", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            deleteOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkEmptyDataSourceName() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);

            DataSource emptyDataSourceName = new DataSource().withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(emptyDataSourceName);

            openAnnieDialog();
            checkAvailableAdditionalFields(emptyDataSourceName.withName(DEFAULT_DATA_SOURCE_NAME), FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "check-empty-data-source-name", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            deleteOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void deleteDataSourceName() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            DataSource fbDataSource = new DataSource().withName("fb").withDatasets(personDataset);

            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);
            DataSource gaDataSource =
                    new DataSource().withName("ga").withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(fbDataSource, gaDataSource);

            openAnnieDialog();
            checkAvailableAdditionalFields(fbDataSource, FieldTypes.ALL);
            checkAvailableAdditionalFields(gaDataSource, FieldTypes.ALL);
            annieUIDialog.clickOnDismissButton();

            deleteOutputStageMetadata();

            DataSource unknownDataSource =
                    new DataSource().withName(DEFAULT_DATA_SOURCE_NAME).withDatasets(personDataset,
                            opportunityDataset);

            openAnnieDialog();
            checkAvailableAdditionalFields(unknownDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "delete-data-source-name", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            deleteOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 1)
    public void addNewDataWithCustomDataSourceName() {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

            Dataset personDataset = prepareDataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
            DataSource fbDataSource = new DataSource().withName("fb").withDatasets(personDataset);

            Dataset opportunityDataset = prepareDataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);
            DataSource gaDataSource =
                    new DataSource().withName("ga").withDatasets(personDataset, opportunityDataset);

            customOutputStageMetadata(fbDataSource, gaDataSource);

            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
            fbDataSource.updateDatasetStatus(selectedDataset);

            checkSuccessfulAddingData(fbDataSource, "add-data-with-custom-dataSource-name");
        } finally {
            deleteOutputStageMetadata();
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }
    }


    @AfterClass
    public void cleanUp() {
        deleteADSInstance(ads);
    }

    private void waitForAddingDataTask() {
        while (true) {
            openAnnieDialog();
            if (!DATA_CAN_NOT_ADD_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline()) && isDatasourceVisible()) {
                annieUIDialog.clickOnDismissButton();
                break;
            }
            annieUIDialog.clickOnCloseButton();
            sleepTight(2000);
        }
    }

    private boolean isDatasourceVisible() {
        try {
            sleepTight(1000);
            waitForElementVisible(AnnieUIDialogFragment.BY_DATASOURCES, browser);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
