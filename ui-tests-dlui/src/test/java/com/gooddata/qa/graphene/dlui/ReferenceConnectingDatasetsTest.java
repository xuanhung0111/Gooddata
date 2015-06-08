package com.gooddata.qa.graphene.dlui;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import static org.testng.Assert.*;

public class ReferenceConnectingDatasetsTest extends AbstractAnnieDialogTest {

    private String projectId;
    private Dataset selectedDataset;
    private DataSource dataSource;
    private Field selectedField;

    private static final String DATA_ADDED_SUCCESSFULLY = "Data added successfuly!";
    private static final String DATASET_NAME = "track";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Reference-Connecting-Dataset-Test";
        initialLdmMaqlFile = "create-ldm-references.txt";
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = {"setup"}, groups = {"initialDataForDLUI"})
    public void initialData() throws InterruptedException {
        projectId = testParams.getProjectId();
        selectedField = new Field("Trackname", FieldTypes.ATTRIBUTE);
        selectedDataset = new Dataset().withName(DATASET_NAME).withFields(selectedField);
        dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES)
                        .updateDatasetStatus(selectedDataset);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"reference"})
    public void autoCreationConnectingDatasetsViaRestApi() throws IOException, JSONException,
            ParseException, InterruptedException {
        try {
            createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES);
            createDataLoadProcess();

            String executionUri =
                    executeDataloadProcess(Lists.newArrayList(new ExecutionParameter(GDC_DE_SYNCHRONIZE_ALL,
                            true)));
            assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));
            
            List<String> references = getReferencesOfDataset(DATASET_NAME);
            System.out.println("References: " + references);
            assertTrue(references.contains("dataset.artist"),
                    "Reference was not added automatically!");
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedReference_API.txt");
        }
    }

    @Test(dependsOnMethods = {"autoCreationConnectingDatasetsViaRestApi"}, groups = {"reference"})
    public void autoCreationConnectingDatasets() throws ParseException, JSONException, IOException {
        try {
            updateFieldToSelected();
            addNewFieldWithAnnieDialog(dataSource);
            List<String> references = getReferencesOfDataset(DATASET_NAME);
            assertTrue(references.contains("dataset.artist"),
                    "Reference was not added automatically!");
            checkRemainingAdditionalFields(dataSource);
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedReferenceAddedField_Annie.txt");
        }
    }

    @Test(dependsOnMethods = {"autoCreationConnectingDatasetsViaRestApi"}, groups = {"reference"})
    public void checkAutoCreationReferenceWithEditorRole() throws InterruptedException,
            ParseException, JSONException, IOException {
        try {
            updateFieldToSelected();
            logout();
            signIn(true, UserRoles.EDITOR);

            addNewFieldWithAnnieDialog(dataSource);
            List<String> references = getReferencesOfDataset(DATASET_NAME);
            assertTrue(references.contains("dataset.artist"),
                    "Reference was not added automatically!");
            checkRemainingAdditionalFields(dataSource);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
            dropAddedFieldsInLDM(maqlFilePath + "dropAddedReferenceAddedField_Annie.txt");
        }
    }

    @Test(dependsOnGroups = {"reference"}, alwaysRun = true)
    public void autoCreationMultiConnectingDatasets() throws InterruptedException, ParseException,
            JSONException, IOException {
        updateModelOfGDProject(maqlFilePath + "dropAllDatasetsOfReference.txt");
        initialLdmMaqlFile = "create-ldm-multi-references.txt";
        try {
            dataSource =
                    prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_MULTI_REFERECES)
                            .updateDatasetStatus(selectedDataset);
            updateModelOfGDProject(maqlFilePath + initialLdmMaqlFile);
            updateFieldToSelected();
            addNewFieldWithAnnieDialog(dataSource);

            List<String> references = getReferencesOfDataset(DATASET_NAME);
            assertTrue(references.contains("dataset.artist"),
                    "Artist reference wasn't added automatically!");
            assertTrue(references.contains("dataset.author"),
                    "Author reference wasn't added automatically!");
            checkRemainingAdditionalFields(dataSource);
        } finally {
            dropAddedFieldsInLDM(maqlFilePath + "dropMultiAddedReferences_Annie.txt");
        }
    }

    @Test(dependsOnMethods = "autoCreationMultiConnectingDatasets", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private void addNewFieldWithAnnieDialog(DataSource dataSource) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return DATA_ADDED_SUCCESSFULLY.equals(annieUIDialog.getAnnieDialogHeadline());
            }
        });
        annieUIDialog.clickOnCloseButton();
    }

    private void checkRemainingAdditionalFields(DataSource dataSource) {
        dataSource.applyAddSelectedFields();
        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    private List<String> getReferencesOfDataset(String dataset) throws ParseException,
            JSONException, IOException {
        JSONArray array =
                RestUtils.getDatasetElementFromModelView(getRestApiClient(), projectId, dataset,
                        DatasetElements.REFERENCES, JSONArray.class);
        return new ObjectMapper().readValue(array.toString(), new TypeReference<List<String>>() {});
    }

    private void updateFieldToSelected() {
        selectedField.setStatus(FieldStatus.SELECTED);
        dataSource.updateDatasetStatus(selectedDataset);
    }
}
