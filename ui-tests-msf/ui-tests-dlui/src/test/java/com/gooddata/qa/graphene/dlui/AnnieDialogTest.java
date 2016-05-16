package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.http.RestUtils.ACCEPT_HEADER_VALUE_WITH_VERSION;
import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.utils.ads.AdsHelper;
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

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 1)
    public void checkFailToAddNewField() throws IOException, JSONException {
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
    public void checkConcurrentDataLoadWithAnnieDialog() throws IOException, JSONException {
        Dataset dataset =
                new Dataset().withName("person").withFields(
                        new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA).updateDatasetStatus(dataset);
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        annieUIDialog.clickOnApplyButton();
        try {
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return ADDING_DATA_HEADLINE.equals(waitForFragmentVisible(annieUIDialog).getAnnieDialogHeadline());
                }
            });
            annieUIDialog.clickOnCloseButton();
            openAnnieDialog();
            try {
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
            } catch (TimeoutException e) {
                if (ANNIE_DIALOG_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline())) {
                    log.warning("\"Data cannot be added right now\" dialog is not show "
                            + "or graphene is too slow to capture it!");
                    annieUIDialog.clickOnDismissButton();
                } else {
                    throw e;
                }
            }
            dataSource.applyAddSelectedFields();
            openAnnieDialog();
            checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        } finally {
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkCustomDataSourceName() throws ParseException, IOException, JSONException {
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
            customOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkDataSourceNameCaseSensitive() throws ParseException, IOException, JSONException {
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
            customOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkUnicodeDataSourceName() throws ParseException, IOException, JSONException {
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
            customOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void checkEmptyDataSourceName() throws ParseException, IOException, JSONException {
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
            customOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 0)
    public void deleteDataSourceName() throws ParseException, IOException, JSONException {
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

            customOutputStageMetadata();

            DataSource unknownDataSource =
                    new DataSource().withName(DEFAULT_DATA_SOURCE_NAME).withDatasets(personDataset,
                            opportunityDataset);

            openAnnieDialog();
            checkAvailableAdditionalFields(unknownDataSource, FieldTypes.ALL);
            Screenshots.takeScreenshot(browser, "delete-data-source-name", getClass());
            annieUIDialog.clickOnDismissButton();
        } finally {
            customOutputStageMetadata();
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 1)
    public void addNewDataWithCustomDataSourceName() throws IOException, JSONException {
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
            customOutputStageMetadata();
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES
                    + "/dropAddedAttributeInLDM_Person_Position.txt"));
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, priority = 1)
    public void addNewDataWithCSVUploader() throws JSONException, IOException {
        try {
            uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/payroll.csv"));
            addMultiFieldsAndAssertAnnieDialog(UserRoles.ADMIN);
            initManagePage();
            assertThat(datasetsTable.getAllItems(), containsInAnyOrder("person", "opportunity", "Payroll", 
                    "Date (Paydate)")); 
            openProjectDetailPage(getWorkingProject());
            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                                    .setHasDataloadProcess(true)
                                    .setSynchronizeAllDatasets(false)
                                    .setScheduleName("2 datasets")
                                    .setConfirmed(true);
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
            scheduleDetail.openDatasetDialog();
            assertThat(scheduleDetail.getSearchedDatasets(), containsInAnyOrder("person", "opportunity")); 
        } finally {
            scheduleDetail.disableSchedule();
        }
        
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        getAdsHelper().removeAds(ads);
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

    private Dataset prepareDataset(AdditionalDatasets additionalDataset) {
        return additionalDataset.getDataset();
    }

    private void customOutputStageMetadata(final DataSource... dataSources)
            throws ParseException, IOException, JSONException {
        final String putBody = prepareOutputStageMetadata(dataSources);
        final String putUri = format(AdsHelper.OUTPUT_STAGE_METADATA_URI, getWorkingProject().getProjectId());
        getResource(getRestApiClient(),
                getRestApiClient().newPutMethod(putUri, putBody),
                req -> req.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION),
                HttpStatus.OK);
    }

    private String prepareOutputStageMetadata(final DataSource... dataSources) throws JSONException {
        final JSONObject metaObject = new JSONObject();

        final Collection<JSONObject> metadataObjects = Lists.newArrayList();
        for (DataSource dataSource : dataSources) {
            for (Dataset dataset : dataSource.getAvailableDatasets(FieldTypes.ALL)) {
                metadataObjects.add(prepareMetadataObject(dataset.getName(), dataSource.getName()));
            }
        }
        metaObject.put("outputStageMetadata", new JSONObject().put("tableMeta", metadataObjects));

        return metaObject.toString();
    }

    private JSONObject prepareMetadataObject(final String tableName, final String dataSourceName) throws JSONException {
        return new JSONObject() {{
            put("tableMetadata", new JSONObject() {{
                put("table", tableName);
                put("defaultSource", dataSourceName);
                put("columnMeta", new JSONArray());
            }});
        }};
    }
}
