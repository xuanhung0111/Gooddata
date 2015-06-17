package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public abstract class AbstractDLUITest extends AbstractMSFTest {

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String OUTPUT_STAGE_METADATA_URI =
            "/gdc/dataload/projects/%s/outputStage/metadata";
    protected static final String ACCEPT_APPLICATION_JSON_WITH_VERSION = "application/json; version=1";
    protected static final String ACCEPT_TEXT_PLAIN_WITH_VERSION = "text/plain; version=1";
    protected static final String OUTPUTSTAGE_URI = "/gdc/dataload/projects/%s/outputStage/";

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";

    protected static final String EXECUTABLE = "executable";
    protected static final String GDC_DE_SYNCHRONIZE_ALL = "GDC_DE_SYNCHRONIZE_ALL";
    protected static final String TXT_FOLDER_NAME = "txt-file";

    @FindBy(css = ".s-btn-add_data")
    private WebElement addDataButton;

    @FindBy(css = ".annie-dialog-main")
    protected AnnieUIDialogFragment annieUIDialog;

    protected String technicalUser;
    protected String technicalUserPassword;
    protected String technicalUserUri;
    protected String apiResourcesPath;

    @BeforeClass
    public void initialPropertiesForDLUI() {
        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
        technicalUserUri = testParams.loadProperty("technicalUserUri");
        apiResourcesPath = testParams.loadProperty("apiResourcesPath") + testParams.getFolderSeparator();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"initialDataForDLUI", "setup"})
    public void prepareLDMAndADSInstanceForDLUI() throws JSONException, ParseException, IOException,
            InterruptedException {
        prepareLDMAndADSInstance();
    }

    @Test(dependsOnMethods = {"prepareLDMAndADSInstanceForDLUI"}, groups = {"initialDataForDLUI", "setup"},
            alwaysRun = true, priority = 1)
    public void setUpOutputStageAndCreateCloudConnectProcessForDLUI() {
        setUpOutputStageAndCreateCloudConnectProcess();
    }

    protected String getAdsUrl(ADSInstance adsInstance) {
        return ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                adsInstance.getId());
    }

    protected int createDataLoadProcess() {
        return createDataLoadProcess(new ProcessInfo().withProcessName(
                DEFAULT_DATAlOAD_PROCESS_NAME).withProjectId(getWorkingProject().getProjectId()));
    }

    protected int createDataLoadProcess(ProcessInfo processInfo) {
        return ProcessUtils.createDataloadProcess(getRestApiClient(), processInfo);
    }

    protected String failedToCreateDataloadExecution(HttpStatus expectedStatusCode,
            Collection<ExecutionParameter> params) throws IOException, JSONException {
        return ProcessUtils.failedToCreateDataloadProcessExecution(getRestApiClient(),
                expectedStatusCode, getDataloadProcessInfo(), params);
    }

    protected String executeDataloadProcess(RestApiClient restApiClient, Collection<ExecutionParameter> params)
            throws IOException, JSONException {
        return ProcessUtils
                .executeProcess(restApiClient, getDataloadProcessInfo(), "", params);
    }

    protected String executeDataloadProcessSuccessfully(RestApiClient restApiClient)
            throws IOException, JSONException {
        String executionUri =
                executeDataloadProcess(restApiClient, Lists.newArrayList(new ExecutionParameter(
                        GDC_DE_SYNCHRONIZE_ALL, true)));

        assertTrue(ProcessUtils.isExecutionSuccessful(restApiClient, executionUri),
                "Process execution is not successful!");
        return executionUri;
    }

    protected void deleteDataloadProcessAndCreateNewOne() throws IOException, JSONException {
        if (!getDataloadProcessId().isEmpty()) {
            ProcessUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                    testParams.getProjectId());
        }
        createDataLoadProcess();
        assertFalse(getDataloadProcessId().isEmpty());
    }

    protected void openAnnieDialog() {
        initManagePage();
        waitForElementVisible(addDataButton).click();
        browser.switchTo().frame(
                waitForElementVisible(By.xpath("//iframe[contains(@src,'dlui-annie')]"), browser));
        waitForElementVisible(annieUIDialog.getRoot());
    }

    protected DataSource prepareADSTable(ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        DataSource dataSource =
                new DataSource().withName(adsTable.datasourceName).withDatasets(
                        adsTable.getDatasets());

        return dataSource;
    }

    protected Dataset prepareDataset(AdditionalDatasets additionalDataset) {
        return additionalDataset.getDataset();
    }

    protected void createUpdateADSTable(ADSTables adsTable) {
        createUpdateADSTableBySQLFiles(adsTable.createTableSqlFile, adsTable.copyTableSqlFile, adsInstance);
    }

    protected void deleteOutputStageMetadata() {
        customOutputStageMetadata();
    }

    protected void customOutputStageMetadata(DataSource... dataSources) {
        String putBody = prepareOutputStageMetadata(dataSources);

        String putUri =
                String.format(OUTPUT_STAGE_METADATA_URI, getWorkingProject().getProjectId());

        HttpRequestBase putRequest = getRestApiClient().newPutMethod(putUri, putBody);
        putRequest.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION);

        HttpResponse putResponse =
                getRestApiClient().execute(putRequest, HttpStatus.OK,
                        "Metadata is not updated successfully! Put body: " + putBody);

        EntityUtils.consumeQuietly(putResponse.getEntity());
    }

    protected String getDataloadProcessUri() throws IOException, JSONException {
        return format(DATALOAD_PROCESS_URI, testParams.getProjectId()) + getDataloadProcessId();
    }

    protected String getDataloadProcessId() throws IOException, JSONException {
        return ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcess().getProcessId();
    }

    protected String getDiffResourceContent(RestApiClient restApiClient, HttpStatus status) {
        return RestUtils.getResourceWithCustomAcceptHeader(restApiClient, 
                format(OUTPUTSTAGE_URI, testParams.getProjectId()) + "diff",
                status, ACCEPT_TEXT_PLAIN_WITH_VERSION);
    }

    private ProcessInfo getDataloadProcessInfo() throws IOException, JSONException {
        return new ProcessInfo().withProjectId(getWorkingProject().getProjectId()).withProcessId(
                getDataloadProcessId());
    }

    private String prepareOutputStageMetadata(DataSource... dataSources) {
        JSONObject metaObject = new JSONObject();

        try {
            Collection<JSONObject> metadataObjects = Lists.newArrayList();
            for (DataSource dataSource : dataSources) {
                for (Dataset dataset : dataSource.getAvailableDatasets(FieldTypes.ALL)) {
                    metadataObjects.add(prepareMetadataObject(dataset.getName(),
                            dataSource.getName(), new JSONArray()));
                }
            }
            metaObject.put("outputStageMetadata",
                    new JSONObject().put("tableMeta", metadataObjects));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is JSONExcetion during prepareOutputStageMetadata!", e);
        }

        return metaObject.toString();
    }

    private JSONObject prepareMetadataObject(String tableName, String dataSourceName,
            JSONArray metaColumns) {
        JSONObject metadataObject = new JSONObject();
        try {
            metadataObject.put("tableMetadata",
                    new JSONObject().put("table", tableName).put("defaultSource", dataSourceName)
                            .put("columnMeta", metaColumns));
        } catch (JSONException e) {
            throw new IllegalStateException("JSONExeception", e);
        }

        return metadataObject;
    }

    protected enum AdditionalDatasets {

        PERSON_WITH_NEW_FIELDS("person", new Field("Position", FieldTypes.ATTRIBUTE)),
        PERSON_WITH_NEW_DATE_FIELD(
                "person",
                new Field("Position", FieldTypes.ATTRIBUTE),
                new Field("Date", FieldTypes.DATE)),
        OPPORTUNITY_WITH_NEW_FIELDS(
                "opportunity",
                new Field("Title2", FieldTypes.ATTRIBUTE),
                new Field("Label", FieldTypes.LABEL_HYPERLINK),
                new Field("Totalprice2", FieldTypes.FACT)),
        OPPORTUNITY_WITH_NEW_DATE_FIELD(
                "opportunity",
                new Field("Title2", FieldTypes.ATTRIBUTE),
                new Field("Label", FieldTypes.LABEL_HYPERLINK),
                new Field("Totalprice2", FieldTypes.FACT),
                new Field("Date", FieldTypes.DATE)),
        ARTIST_WITH_NEW_FIELD("artist", new Field("Artisttitle", FieldTypes.ATTRIBUTE)),
        AUTHOR_WITH_NEW_FIELD("author", new Field("Authorname", FieldTypes.ATTRIBUTE)),
        TRACK_WITH_NEW_FIELD("track", new Field("Trackname", FieldTypes.ATTRIBUTE));

        private String name;
        private List<Field> additionalFields;

        private AdditionalDatasets(String name, Field... additionalFields) {
            this.name = name;
            this.additionalFields = Lists.newArrayList(additionalFields);
        }

        public Dataset getDataset() {
            List<Field> fields = Lists.newArrayList();
            for (Field additionalField : additionalFields) {
                fields.add(additionalField.clone());
            }
            return new Dataset().withName(name).withFields(fields);
        }
    }

    protected enum ADSTables {

        WITHOUT_ADDITIONAL_FIELDS("createTable.txt", "copyTable.txt", "Unknown data source"),
        WITH_ADDITIONAL_FIELDS(
                "createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ADDITIONAL_DATE(
                "createTableWithAdditionalDate.txt",
                "copyTableWithAdditionalDate.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_DATE_FIELD,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ERROR_MAPPING("createTableWithErrorMapping.txt", "copyTableWithErrorMapping.txt"),
        WITH_ADDITIONAL_FIELDS_LARGE_DATA(
                "createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFieldsLargeData.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ADDITIONAL_FIELDS_AND_REFERECES(
                "createTableWithReferences.txt",
                "copyTableWithReferences.txt",
                "Unknown data source",
                AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
                AdditionalDatasets.TRACK_WITH_NEW_FIELD),
        WITH_ADDITIONAL_FIELDS_AND_MULTI_REFERECES(
                "createTableWithMultiReferences.txt",
                "copyTableWithMultiReferences.txt",
                "Unknown data source",
                AdditionalDatasets.TRACK_WITH_NEW_FIELD,
                AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
                AdditionalDatasets.AUTHOR_WITH_NEW_FIELD);

        private String createTableSqlFile;
        private String copyTableSqlFile;
        private String datasourceName;
        private List<AdditionalDatasets> additionalDatasets = Lists.newArrayList();

        private ADSTables(String createTableSqlFile, String copyTableSqlFile) {
            this(createTableSqlFile, copyTableSqlFile, "");
        }

        private ADSTables(String createTableSqlFile, String copyTableSqlFile,
                String datasourceName, AdditionalDatasets... datasets) {
            this.createTableSqlFile = createTableSqlFile;
            this.copyTableSqlFile = copyTableSqlFile;
            this.datasourceName = datasourceName;
            this.additionalDatasets = Arrays.asList(datasets);
        }

        public List<Dataset> getDatasets() {
            List<Dataset> datasets = Lists.newArrayList();
            for (AdditionalDatasets additionalDataset : this.additionalDatasets) {
                datasets.add(additionalDataset.getDataset());
            }
            return datasets;
        }
    }
}
