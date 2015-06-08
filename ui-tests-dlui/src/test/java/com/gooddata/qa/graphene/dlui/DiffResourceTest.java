package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class DiffResourceTest extends AbstractDLUITest {
    
    private static final String DIFF_WITH_MAPPING_MATCH_TEXT = "-- ADS and LDM column mapping matches.";
    private static final String DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE = "diff-output-stage-empty.txt";
    private static final String NO_DATA_WAREHOUSE_ERROR_MESSAGE =
            "No Data Warehouse schema defined for project %s.";

    @BeforeClass
    public void initialProjectTitle() {
        projectTitle = "Diff-resource-test";
    }

    @Test(dependsOnMethods = {"prepareLDMAndADSInstance"}, groups = {"DiffResourceTest"})
    public void checkDiffResourceWithoutADSInstance() throws IOException {
        assertTrue(getDiffResourceContent(getRestApiClient(), HttpStatus.NOT_FOUND)
                .contains(NO_DATA_WAREHOUSE_ERROR_MESSAGE),
                "No data warehouse error message is not displayed in Diff resource!");
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"DiffResourceTest"})
    public void generateLdmAndAdsDifference() throws IOException {
        assertDiffContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                readDiffFileToString(DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE));

        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                "Diff content is incorrect, ADS and LDM not match!");
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"DiffResourceTest"}, priority = 1)
    public void  checkDiffResourceAfterChangeAdsInstance () throws IOException {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);
        ADSInstance newInstance = new ADSInstance().withName("ADS Instance for DLUI test")
                .withAuthorizationToken(dssAuthorizationToken);
        createADSInstance(newInstance);
        try {
            setDefaultSchemaForOutputStage(getRestApiClient(), newInstance.getId());
            System.out.println("equals: " + readDiffFileToString(DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE)
                    .contains(getDiffResourceContent(getRestApiClient(), HttpStatus.OK)));
            assertDiffContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    readDiffFileToString(DIFF_WITH_EMPTY_OUTPUT_STAGE_FILE));
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    "Diff content is incorrect, ADS and LDM not match!");
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newInstance);
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"DiffResourceTest"}, priority = 1)
    public void repairWrongMappingUsingDiffContent() throws IOException {
        createUpdateADSTableBySQLFiles("createTableWithErrorMapping_DiffTest.txt",
                "copyTableWithErrorMapping_DiffTest.txt", adsInstance);

        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        assertDiffContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                readDiffFileToString("diff-error-mapping.txt"));

        createUpdateADSTableBySQLFiles("alterTableToFixDiffErrorMapping.txt", "copyTable.txt", adsInstance);

        assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                "Diff content is incorrect, ADS and LDM not match!");
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"DiffResourceTest"}, priority = 2)
    public void checkDiffResourceWithAllCases() throws IOException, JSONException {
        ADSInstance newInstance = new ADSInstance().withName("ADS Instance for DLUI test")
              .withAuthorizationToken(dssAuthorizationToken);
        createADSInstance(newInstance);
        setDefaultSchemaForOutputStage(getRestApiClient(), newInstance.getId());
        try {
            updateModelOfGDProject(maqlFilePath + "dropTablesOpportunityPerson.txt");
            updateModelOfGDProject(maqlFilePath + "createDatasetWithAllCases.txt");

            assertDiffContent(getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    readDiffFileToString("diff-all-cases.txt"));

            createUpdateADSTableBySQLFiles("createTableWithAllCases.txt", "copyTableWithAllCases.txt",
                    newInstance);

            assertEquals(DIFF_WITH_MAPPING_MATCH_TEXT, getDiffResourceContent(getRestApiClient(), HttpStatus.OK),
                    "Diff content is incorrect, ADS and LDM not match!");

            String execution = executeDataloadProcess(Lists.newArrayList(new ExecutionParameter(
                    GDC_DE_SYNCHRONIZE_ALL, true)));
            assertTrue(ProcessUtils.isExecutionSuccessful(restApiClient, execution),
                    "Process execution is not successful!");
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newInstance);
        }
    }

    @Test(dependsOnGroups = {"DiffResourceTest"}, alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private String readDiffFileToString(String file) throws IOException {
        String filePath = maqlFilePath.replace("maql-file", "txt-file") + file;
        return FileUtils.readFileToString(new File(filePath));
    }

    private void assertDiffContent(String actualContent, String expectedContent) throws IOException {
        String[] contentParts = expectedContent.split(Strings.repeat("-", 50));
        for(int i = 0; i < contentParts.length; i++ ){
            assertTrue(actualContent.contains(contentParts[i].trim()));
        }
    }
}
