package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.API_RESOURCES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class DataloadResourcesTest extends AbstractMSFTest {
    
    private static final String DIFF_WITH_MAPPING_MATCH_TEXT = "-- ADS and LDM column mapping matches.";
    private static final String DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE = "diff-output-stage-empty.txt";
    private static final String NO_DATA_WAREHOUSE_ERROR_MESSAGE =
            "No Data Warehouse schema defined for project %s.";
    private static final String NO_DATALOAD_PROCESS_MESSAGE = "No DATALOAD process exists in project %s.";
    private static final String DIFF_KEY = Strings.repeat("-", 50);
    private static final String MAPPING_OUTPUT_STAGE_EMPTY_FILE = "mapping-output-stage-empty.txt";

    @BeforeClass
    public void initialProjectTitle() {
        projectTitle = "Dataload-resources-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"DataloadResourcesTest"})
    public void checkResourcesWithoutADSInstance() throws IOException, JSONException {
        prepareLDMAndADSInstance();

        assertTrue(getDiffResourceContent(getRestApiClient(), HttpStatus.NOT_FOUND)
                .contains(NO_DATA_WAREHOUSE_ERROR_MESSAGE),
                "No data warehouse error message is not displayed in Diff resource!");

        assertTrue(getMappingResourceContent(getRestApiClient(), HttpStatus.FORBIDDEN)
                .contains(NO_DATALOAD_PROCESS_MESSAGE),
                "No dataload process error message is not displayed in mapping resource!");
    }

    @Test(dependsOnMethods = {"checkResourcesWithoutADSInstance"},
            groups = {"DataloadResourcesTest", "initialData"}, alwaysRun = true)
    public void prepareData() throws IOException, JSONException {
        setUpOutputStageAndCreateCloudConnectProcess();
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"DataloadResourcesTest"})
    public void generateLdmAndAdsDifference() throws IOException {
        assertResourceContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                readResourceFile(DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE, DIFF_KEY));
        assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                readAllLinesOfFile(MAPPING_OUTPUT_STAGE_EMPTY_FILE));

        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                "Diff content is incorrect, ADS and LDM not match!");
        assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                readAllLinesOfFile("mapping-valid-case.txt"));
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"DataloadResourcesTest"}, priority = 1)
    public void  checkResourcesAfterChangeAdsInstance () throws IOException {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);
        ADSInstance newInstance = new ADSInstance().withName("ADS Instance for DLUI test")
                .withAuthorizationToken(dssAuthorizationToken);
        createADSInstance(newInstance);
        try {
            setDefaultSchemaForOutputStage(getRestApiClient(), newInstance.getId());

            assertResourceContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    readResourceFile(DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE,  DIFF_KEY));
            assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                    readAllLinesOfFile(MAPPING_OUTPUT_STAGE_EMPTY_FILE));

            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());

            assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    "Diff content is incorrect, ADS and LDM not match!");
            assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                    readAllLinesOfFile("mapping-valid-case.txt"));
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newInstance);
        }
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"DataloadResourcesTest"}, priority = 1)
    public void repairWrongMappingUsingDiffContent() throws IOException {
        createUpdateADSTableBySQLFiles("createTableWithErrorMapping_DiffTest.txt",
                "copyTableWithErrorMapping_DiffTest.txt", adsInstance);

        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());

        assertResourceContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                readResourceFile("diff-error-mapping.txt", DIFF_KEY));
        assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                readAllLinesOfFile("mapping-error-case.txt"));

        createUpdateADSTableBySQLFiles("alterTableToFixDiffErrorMapping.txt", "copyTable.txt", adsInstance);

        assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                "Diff content is incorrect, ADS and LDM not match!");
        assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                readAllLinesOfFile("mapping-valid-case.txt"));
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"DataloadResourcesTest"}, priority = 2)
    public void checkResourcesWithAllCases() throws IOException, JSONException {
        ADSInstance newInstance = new ADSInstance().withName("ADS Instance for DLUI test")
              .withAuthorizationToken(dssAuthorizationToken);
        createADSInstance(newInstance);
        setDefaultSchemaForOutputStage(getRestApiClient(), newInstance.getId());
        try {
            String dropTableFile = getResourceAsString("/" + MAQL_FILES + "/dropTablesOpportunityPerson.txt");
            String createDatasetFile = getResourceAsString("/" + MAQL_FILES + "/createDatasetWithAllCases.txt");
            updateModelOfGDProject(dropTableFile);
            updateModelOfGDProject(createDatasetFile);

            assertResourceContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    readResourceFile("diff-all-cases.txt", DIFF_KEY));
            assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                    readAllLinesOfFile("mapping-all-cases-error.txt"));

            createUpdateADSTableBySQLFiles("createTableWithAllCases.txt", "copyTableWithAllCases.txt",
                    newInstance);

            assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    "Diff content is incorrect, ADS and LDM not match!");
            assertResourceContent(getMappingResourceContent(getRestApiClient(), HttpStatus.OK),
                    readAllLinesOfFile("mapping-all-cases.txt"));

            String execution = executeDataloadProcess(getRestApiClient(), Lists.newArrayList(new ExecutionParameter(
                    GDC_DE_SYNCHRONIZE_ALL, true)));
            assertTrue(ProcessUtils.isExecutionSuccessful(restApiClient, execution),
                    "Process execution is not successful!");
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newInstance);
        }
    }

    @Test(dependsOnGroups = {"DataloadResourcesTest"}, alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private List<String> readResourceFile(String file, String key) throws IOException {
        return asList(getResourceAsString("/" + API_RESOURCES + "/" + file).split(key));
    }

    private List<String> readAllLinesOfFile(String file) throws IOException {
        return FileUtils.readLines(getResourceAsFile("/" + API_RESOURCES + "/" + file));
    }

    private void assertResourceContent(String actualContent, List<String> expectedParts) throws IOException {
        for (String part : expectedParts) {
            assertTrue(actualContent.contains(part.trim()),
                    "Resource content is not correct with different part: " + part);
        }
    }

}
