package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class AnnieDialogTest extends AbstractDLUITest {

    private static final String DEFAULT_DATA_SOURCE_NAME = "Unknown data source";
    private static final String DATA_CAN_NOT_ADD_HEADLINE = "Data cannot be added right now";
    private static final String ADDING_DATA_HEADLINE = "Adding data...";
    private static final String DIALOG_STATE_FIRST_MESSAGE = 
            "We’re sorry, but it’s not possible to add new data at the moment." +
            " Other data is currently being uploaded into the project " +
            "and due to technical limitations it is not possible to add new data at the same time.";
    private static final String DIALOG_STATE_SECOND_MESSAGE = "It can take a while so please come later.";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Dlui-annie-dialog-test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"annieDialogTest"}, priority = 0)
    public void checkEmptyStateInAnnieDialog() {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkEmptyAnnieDialog();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalFields() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "available-additional-fields", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalAttributes() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ATTRIBUTE);
        Screenshots.takeScreenshot(browser, "available-additional-attributes", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalFacts() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.FACT);
        Screenshots.takeScreenshot(browser, "available-additional-facts", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkAvailableAdditionalLabelHyperlink() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.LABEL_HYPERLINK);
        Screenshots.takeScreenshot(browser, "available-additional-label-hyperlinks", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkAdditionalDateField() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
        Screenshots.takeScreenshot(browser, "available-additional-date", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkEmptyStateWithDateFilter() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkSearchAllFields() {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        Field field = new Field("Position", FieldTypes.ATTRIBUTE);
        Dataset dataset = new Dataset().withName("person").withFields(field);
        DataSource dataSource =
                new DataSource().withName(DEFAULT_DATA_SOURCE_NAME).withDatasets(dataset);

        openAnnieDialog();
        annieUIDialog.enterSearchKey("Pos");
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkErrorMappingOnAnnieDialog() {
        // Prepare an ADS which doesn't contain some fields in LDM
        createUpdateADSTable(ADSTables.WITH_ERROR_MAPPING);

        openAnnieDialog();
        annieUIDialog.assertErrorMessage();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void selectAndDeselectFields() {
        Field selectedField1 = new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED);
        Field selectedField2 = new Field("Title2", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED);

        Dataset selectedDataset1 = new Dataset().withName("person").withFields(selectedField1);
        Dataset selectedDataset2 = new Dataset().withName("opportunity").withFields(selectedField2);

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                        selectedDataset1, selectedDataset2);

        openAnnieDialog();
        annieUIDialog.assertNoDataSelectedState();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.checkSelectionArea(Lists.newArrayList(selectedField2, selectedField1));
        annieUIDialog.assertSelectedFieldNumber(dataSource);
        annieUIDialog.deselectFields(dataSource);
        annieUIDialog.assertNoDataSelectedState();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 0)
    public void checkCancelAddNewFieldFromADSToLDM() {
        Dataset selectedDataset =
                new Dataset().withName("person").withFields(
                        new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                        selectedDataset);

        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnDismissButton();

        dataSource.cancelAddSelectedFields();

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        Screenshots.takeScreenshot(browser, "cancel-add-new-field", getClass());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void addNewAttributeFromADSToLDM() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "add-new-attribute");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedAttributeInLDM_Person_Position.txt");
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void addNewFactFromADSToLDM() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("opportunity").withFields(
                            new Field("Totalprice2", FieldTypes.FACT, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            checkSuccessfulAddingData(dataSource, "add-new-fact");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedFactInLDM_Opportunity_Totalprice2.txt");
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void addNewLabelFromADSToLDM() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("opportunity").withFields(
                            new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);


            checkSuccessfulAddingData(dataSource, "add-new-label");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedLabelInLDM_Opportunity_Label.txt");
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void addNewDateFieldFromADSToLDM() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Date", FieldTypes.DATE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).updateDatasetStatus(
                            selectedDataset);

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
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void checkFailToAddNewField() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

            openAnnieDialog();
            annieUIDialog.selectFields(dataSource);

            updateModelOfGDProject(maqlFilePath + "addUnmappingFieldToLDM.txt");

            annieUIDialog.clickOnApplyButton();
            annieUIDialog.checkDataAddingProgress();
            annieUIDialog.checkFailedDataAddingResult();
            Screenshots.takeScreenshot(browser, "add-new-field-failed", getClass());
            annieUIDialog.clickOnCloseButton();

            openAnnieDialog();
            annieUIDialog.assertErrorMessage();
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "deleteUnmappingField.txt");
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest", priority = 1)
    public void addNewFieldsToLDMWithEditorRole() {
        try {
            Dataset selectedDataset =
                    new Dataset().withName("person").withFields(
                            new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));

            DataSource dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(
                            selectedDataset);

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

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = "annieDialogTest")
    public void checkConcurrentDataLoadWithAnnieDialog() throws InterruptedException {
        Dataset dataset = new Dataset().withName("person").withFields(
                new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS).updateDatasetStatus(dataset);
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
            Thread.sleep(500);
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
        } finally {
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

        dataSource.applyAddSelectedFields();

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }
}
