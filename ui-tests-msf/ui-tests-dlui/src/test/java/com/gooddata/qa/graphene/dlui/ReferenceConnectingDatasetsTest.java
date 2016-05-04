package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.google.common.base.Predicate;

public class ReferenceConnectingDatasetsTest extends AbstractAnnieDialogTest {

    private Dataset selectedDataset;
    private DataSource dataSource;
    private Field selectedField;

    private static final String DATASET_NAME = "track";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Reference-Connecting-Dataset-Test";
        initialLdmMaqlFile = "create-ldm-references.txt";
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"prepareDataForDLUI"}, groups = {"initialDataForDLUI"})
    public void initialData() {
        selectedField = new Field("Trackname", FieldTypes.ATTRIBUTE);
        selectedDataset = new Dataset().withName(DATASET_NAME).withFields(selectedField);

        dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES)
                .updateDatasetStatus(selectedDataset);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"reference"})
    public void autoCreationConnectingDatasetsViaRestApi() throws IOException, JSONException,
            ParseException {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES);
            assertTrue(executeProcess(createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, "DATALOAD"), "", SYNCHRONIZE_ALL_PARAM).isSuccess());

            final List<String> references = getReferencesOfDataset(DATASET_NAME);
            log.info("References: " + references);
            assertTrue(references.contains("dataset.artist"), "Reference was not added automatically!");
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/dropAddedReference_API.txt"));
        }
    }

    @Test(dependsOnMethods = {"autoCreationConnectingDatasetsViaRestApi"}, groups = {"reference"})
    public void autoCreationConnectingDatasets() throws ParseException, JSONException, IOException {
        try {
            updateFieldToSelected();
            addNewFieldWithAnnieDialog(dataSource);
            final List<String> references = getReferencesOfDataset(DATASET_NAME);
            assertTrue(references.contains("dataset.artist"), "Reference was not added automatically!");
            checkRemainingAdditionalFields(dataSource);

            checkReportAfterAddReferenceToDataset();
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES +
                    "/dropAddedReferenceAddedField_Annie.txt"));
        }
    }

    @Test(dependsOnMethods = {"autoCreationConnectingDatasetsViaRestApi"}, groups = {"reference"})
    public void checkAutoCreationReferenceWithEditorRole() throws 
            ParseException, JSONException, IOException {
        try {
            updateFieldToSelected();
            logout();
            signIn(true, UserRoles.EDITOR);

            addNewFieldWithAnnieDialog(dataSource);
            final List<String> references = getReferencesOfDataset(DATASET_NAME);
            assertTrue(references.contains("dataset.artist"), "Reference was not added automatically!");
            checkRemainingAdditionalFields(dataSource);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES +
                    "/dropAddedReferenceAddedField_Annie.txt"));
        }
    }

    @Test(dependsOnGroups = {"reference"}, alwaysRun = true)
    public void autoCreationMultiConnectingDatasets() throws ParseException,
            JSONException, IOException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/dropAllDatasetsOfReference.txt"));
        initialLdmMaqlFile = "create-ldm-multi-references.txt";
        try {
            dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_MULTI_REFERECES)
                            .updateDatasetStatus(selectedDataset);
            updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));
            updateFieldToSelected();
            addNewFieldWithAnnieDialog(dataSource);

            final List<String> references = getReferencesOfDataset("artist");
            assertTrue(references.contains("dataset.track"), "Track reference wasn't added automatically!");
            assertTrue(references.contains("dataset.author"), "Author reference wasn't added automatically!");
            checkRemainingAdditionalFields(dataSource);

            prepareMetricToCheckNewAddedFields("number");
            createAndCheckReport("Report to check reference 1", "Trackname", "number [Sum]",
                    asList("10 trackNameA", "11 trackNameA", "12 trackNameA", "13 trackNameA",
                        "14 trackNameA", "1 trackNameA", "2 trackNameA", "3 trackNameA", "4 trackNameA",
                        "5 trackNameA", "6 trackNameA", "7 trackNameA", "8 trackNameA", "9 trackNameA"),
                    Collections.nCopies(14, "100"));
            createAndCheckReport("Report to check reference 2", "authorid", "number [Sum]",
                    asList("author1", "author10", "author11", "author12", "author13", "author14",
                        "author19", "author2", "author3", "author4", "author5", "author6", "author7", "author8"),
                    Collections.nCopies(14, "100"));
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/dropMultiAddedReferences_Annie.txt"));
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        getAdsHelper().removeAds(ads);
    }

    private void addNewFieldWithAnnieDialog(DataSource dataSource) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();

        final Predicate<WebDriver> successful =
            browser -> SUCCESSFUL_ANNIE_DIALOG_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline());
        Graphene.waitGui().until(successful);

        annieUIDialog.clickOnCloseButton();
    }

    private void checkRemainingAdditionalFields(DataSource dataSource) {
        dataSource.applyAddSelectedFields();
        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        annieUIDialog.clickOnDismissButton();
    }

    private void updateFieldToSelected() {
        selectedField.setStatus(FieldStatus.SELECTED);
        dataSource.updateDatasetStatus(selectedDataset);
    }
}
