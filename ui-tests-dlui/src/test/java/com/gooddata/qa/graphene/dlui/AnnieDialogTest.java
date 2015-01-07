package com.gooddata.qa.graphene.dlui;

import java.util.Arrays;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.dlui.ADSInstance;
import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.dlui.ProcessInfo;
import com.gooddata.qa.graphene.enums.dlui.AdditionalDatasets;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

public class AnnieDialogTest extends AbstractDLUITest {

    private static final String INITIAL_LDM_MAQL_FILE = "create-ldm.txt";

    private static final String DEFAULT_DATA_SOURCE_NAME = "Unknown data source";

    private static final String ADS_URL = "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";

    private ProcessInfo cloudconnectProcess;
    private ADSInstance adsInstance;

    @BeforeClass
    public void initProperties() {
        dluiZipFilePath = testParams.loadProperty("dluiZipFilePath") + testParams.getFolderSeparator();
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        projectTitle = "Dlui-annie-dialog-test";
    }

    @Test(dependsOnMethods = "createProject")
    public void initialData() {
        try {
            RestUtils.setFeatureFlagsToProject(getRestApiClient(), testParams.getProjectId(),
                    FeatureFlagOption.createFeatureClassOption(ENABLE_DATA_EXPLORER, true));
        } catch (JSONException e) {
            throw new IllegalStateException("There is a problem when enable data explorer flag! ",
                    e);
        }

        createModelForGDProject(maqlFilePath + INITIAL_LDM_MAQL_FILE);

        adsInstance =
                new ADSInstance()
                        .setAdsName("ADS Instance for DLUI test")
                        .setAdsDescription("ADS Instance for DLUI test")
                        .setAdsAuthorizationToken(testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(adsInstance);

        createDataLoadProcess();

        setDefaultSchemaForOutputStage(testParams.getProjectId(), adsInstance.getAdsId());

        cloudconnectProcess =
                new ProcessInfo().setProjectId(testParams.getProjectId())
                        .setProcessName("Initial Data for ADS Instance").setProcessType("GRAPH");
        createCloudConnectProcess(cloudconnectProcess);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkEmptyStateInAnnieDialog() {
        createADSTableWithAdditionalFields("createTable.txt", "copyTable.txt");
        openAnnieDialog();
        checkEmptyAnnieDialog();
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalFields() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");

        AdditionalDatasets personWithNewFields = AdditionalDatasets.PERSON_WITH_NEW_FIELDS;
        Dataset personDataset =
                new Dataset().setName(personWithNewFields.getName()).setFields(
                        personWithNewFields.getFields());

        AdditionalDatasets opportunityWithNewFields =
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS;
        Dataset opportunityDataset =
                new Dataset().setName(opportunityWithNewFields.getName()).setFields(
                        opportunityWithNewFields.getFields());

        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(personDataset, opportunityDataset));

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.ALL);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalAttributes() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");

        AdditionalDatasets personWithNewFields = AdditionalDatasets.PERSON_WITH_NEW_FIELDS;
        Dataset personDataset =
                new Dataset().setName(personWithNewFields.getName()).setFields(
                        personWithNewFields.getFields());

        AdditionalDatasets opportunityWithNewFields =
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS;
        Dataset opportunityDataset =
                new Dataset().setName(opportunityWithNewFields.getName()).setFields(
                        opportunityWithNewFields.getFields());

        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(personDataset, opportunityDataset));

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.ATTRIBUTE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalFacts() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");

        AdditionalDatasets personWithNewFields = AdditionalDatasets.PERSON_WITH_NEW_FIELDS;
        Dataset personDataset =
                new Dataset().setName(personWithNewFields.getName()).setFields(
                        personWithNewFields.getFields());

        AdditionalDatasets opportunityWithNewFields =
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS;
        Dataset opportunityDataset =
                new Dataset().setName(opportunityWithNewFields.getName()).setFields(
                        opportunityWithNewFields.getFields());
        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(personDataset, opportunityDataset));

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.FACT);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalFieldsFilter() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");

        AdditionalDatasets personWithNewFields = AdditionalDatasets.PERSON_WITH_NEW_FIELDS;
        Dataset personDataset =
                new Dataset().setName(personWithNewFields.getName()).setFields(
                        personWithNewFields.getFields());

        AdditionalDatasets opportunityWithNewFields =
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS;
        Dataset opportunityDataset =
                new Dataset().setName(opportunityWithNewFields.getName()).setFields(
                        opportunityWithNewFields.getFields());

        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(personDataset, opportunityDataset));

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.ATTRIBUTE);
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.FACT);
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.DATE);
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.LABLE_HYPERLINK);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAdditionalDateField() {
        createADSTableWithAdditionalFields("createTableWithAdditionalDate.txt",
                "copyTableWithAdditionalDate.txt");

        AdditionalDatasets personWithNewFields = AdditionalDatasets.PERSON_WITH_NEW_FIELDS;
        Dataset personDataset =
                new Dataset().setName(personWithNewFields.getName()).setFields(
                        personWithNewFields.getFields());
        AdditionalDatasets opportunityWithNewDate =
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_DATE_FIELD;
        Dataset opportunityDataset =
                new Dataset().setName(opportunityWithNewDate.getName()).setFields(
                        opportunityWithNewDate.getFields());
        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(personDataset, opportunityDataset));

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.DATE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkEmptyStateWithDateFilter() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");
        DataSource datasource = new DataSource();

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.DATE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkSearchAllFields() {
        createADSTableWithAdditionalFields("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt");
        Field field = new Field().setFieldName("Position").setFieldType(FieldTypes.ATTRIBUTE);
        Dataset dataset =
                new Dataset().setName(AdditionalDatasets.PERSON_WITH_NEW_FIELDS.getName())
                        .setFields(Arrays.asList(field));
        DataSource datasource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(
                        Arrays.asList(dataset));

        openAnnieDialog();
        annieUIDialog.searchFields("Pos");
        annieUIDialog.checkAvailableAdditionalFields(datasource, FieldTypes.ALL);
    }

    @Test(dependsOnGroups = "annieDialogTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private void createADSTableWithAdditionalFields(String createTableSqlFile,
            String copyTableSqlFile) {
        executeProcess(
                cloudconnectProcess.getProcessId(),
                ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                        adsInstance.getAdsId()), sqlFilePath + createTableSqlFile, sqlFilePath
                        + copyTableSqlFile);
    }
}
