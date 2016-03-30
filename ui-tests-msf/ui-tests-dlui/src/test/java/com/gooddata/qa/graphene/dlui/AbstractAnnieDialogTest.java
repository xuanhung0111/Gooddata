package com.gooddata.qa.graphene.dlui;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldStatus;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.fragments.DataSourcesFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public abstract class AbstractAnnieDialogTest extends AbstractMSFTest {
    protected static final String ANNIE_DIALOG_HEADLINE = "Add data";
    protected static final String SUCCESSFUL_ANNIE_DIALOG_HEADLINE = "Data added successfully!";

    private static final String ANNIE_DIALOG_EMPTY_STATE_HEADING = "No additional data available.";
    private static final String ANNIE_DIALOG_EMPTY_STATE_MESSAGE =
            "Your project already contains all existing data. If you"
                    + " need to add more data, contact a project admin or GoodData Customer Support .";

    private static final String ERROR_ANNIE_DIALOG_HEADLINE = "Data loading failed";
    private static final String ERROR_ANNIE_DIALOG_MESSAGE_1 =
            "An error occured during loading, so we couldn’t load your data.";
    private static final String ERROR_ANNIE_DIALOG_MESSAGE_2 =
            "Please contact a project admin or GoodData Customer Support .";
    private static final String GOODDATA_SUPPORT_LINK =
            "https://support.gooddata.com/?utm_source=de&utm_campaign=error_message&utm_medium=dialog";

    private static final String ADD_DATA_BUTTON_CSS_LOCATOR = ".s-btn-add_data";
    protected static final String ANNIE_DIALOG_CSS_LOCATOR = ".annie-dialog-main";
    
    @FindBy(css = ADD_DATA_BUTTON_CSS_LOCATOR)
    private WebElement addDataButton;

    @FindBy(css = ANNIE_DIALOG_CSS_LOCATOR)
    protected AnnieUIDialogFragment annieUIDialog;

    @Test(dependsOnMethods = {"createProject"}, groups = {"initialDataForDLUI"})
    public void prepareDataForDLUI() throws JSONException, ParseException, IOException {
        initManagePage();
        assertThat(browser.findElements(By.cssSelector(ADD_DATA_BUTTON_CSS_LOCATOR)), is(empty()));

        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
        enableDataExplorer();
    }

    protected void openAnnieDialog() {
        initManagePage();
        waitForElementVisible(addDataButton).click();
        browser.switchTo().frame(
                waitForElementVisible(By.xpath("//iframe[contains(@src,'dlui-annie')]"), browser));
        waitForElementVisible(annieUIDialog.getRoot());
    }

    protected void checkAvailableAdditionalFields(DataSource datasource, FieldTypes fieldType) {
        annieUIDialog.selectFieldFilter(fieldType);
        List<Dataset> datasetInSpecificFilter = datasource.getAvailableDatasets(fieldType);
        if (datasetInSpecificFilter.isEmpty()) {
            DataSourcesFragment dataSourceList = annieUIDialog.getDataSources();
            if (dataSourceList.getAvailableDataSourceCount() == 0) {
                assertEquals(annieUIDialog.getEmptyState().getText(),
                        fieldType.getEmptyStateMessage(), "Incorrect empty state message: "
                                + annieUIDialog.getEmptyState().getText());
            } else {
                assertFalse(dataSourceList.isAvailable(datasource));
            }
            return;
        }
        chechAvailableDataSource(datasource, datasetInSpecificFilter, fieldType);
    }

    protected void checkSelectionArea(Collection<Field> expectedFields) {
        List<WebElement> selectedFields = annieUIDialog.getSelectionArea().getSelectedFields();
        assertEquals(selectedFields.size(), expectedFields.size(),
                "The number of selected fields is incorrect!");

        for (Field field : expectedFields) {
            assertTrue(field.hasCorrespondingWebElement(selectedFields));
        }

        Iterable<String> selectedFieldTitles =
                Iterables.transform(selectedFields, new Function<WebElement, String>() {

                    @Override
                    public String apply(WebElement selectedField) {
                        return selectedField.getText();
                    }

                });
        assertTrue(Ordering.natural().isOrdered(selectedFieldTitles),
                "Selected fields are not sorted!");
    }

    protected void assertErrorMessage() {
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver browser) {
                return ERROR_ANNIE_DIALOG_HEADLINE.equals(annieUIDialog.getAnnieDialogHeadline());
            }
        });
        assertEquals(annieUIDialog.getIntegrationStatusMessages().get(0).getText(),
                ERROR_ANNIE_DIALOG_MESSAGE_1, "Incorrect error message on Annie dialog!");
        assertEquals(annieUIDialog.getIntegrationStatusMessages().get(1).getText(),
                ERROR_ANNIE_DIALOG_MESSAGE_2, "Incorrect error message on Annie dialog!");
        assertEquals(
                annieUIDialog.getIntegrationStatusMessages().get(1)
                        .findElement(By.cssSelector("a")).getAttribute("href"),
                GOODDATA_SUPPORT_LINK, "Incorrect support link in error message of Annie dialog!");
        annieUIDialog.clickOnCloseButton();
    }

    protected void assertNoDataSelectedState() {
        assertTrue(annieUIDialog.isNoSelectionArea());
        assertEquals(annieUIDialog.getPositiveButton().getText(), "No data selected",
                "Incorrect apply button title when no data selected!");
        assertTrue(annieUIDialog.getPositiveButton().getAttribute("class").contains("disabled"),
                "Apply button is not disable!");
    }

    protected void checkSelectedFieldNumber(DataSource selectedDataSource) {
        DataSourcesFragment dataSourceList = annieUIDialog.getDataSources();
        WebElement dataSourceElement = dataSourceList.selectDataSource(selectedDataSource);
        int totalSelectedFieldNumber = 0;
        for (Dataset selectedDataset : selectedDataSource.getSelectedDataSets()) {
            WebElement datasetElement =
                    dataSourceList.selectDataset(dataSourceElement, selectedDataset);
            assertEquals(dataSourceList.getDatasetItemSelectionHint(datasetElement),
                    String.format("(%d)", selectedDataset.getSelectedFields().size()));
            totalSelectedFieldNumber += selectedDataset.getSelectedFields().size();
        }
        assertEquals(dataSourceList.getSourceItemSelectionHint(dataSourceElement),
                String.format("%d selected", totalSelectedFieldNumber),
                "Incorrect selected field number in DataSource: " + selectedDataSource.getName());
    }

    protected void checkSuccessfulDataAddingResult() {
        checkDataAddingResult(true);
    }

    protected void checkFailedDataAddingResult() {
        checkDataAddingResult(false);
    }

    protected void checkEmptyAnnieDialog() {
        assertEquals(annieUIDialog.getAnnieDialogHeadline(), ANNIE_DIALOG_HEADLINE);
        assertEquals(annieUIDialog.getEmptyStateHeading(), ANNIE_DIALOG_EMPTY_STATE_HEADING);
        assertEquals(annieUIDialog.getEmptyStateMessage(), ANNIE_DIALOG_EMPTY_STATE_MESSAGE);
        annieUIDialog.clickOnCloseButton();
    }

    protected void checkNewAddedDataReportAndCleanAddedData(UserRoles role,
            final AddedFields addedField, final ReportWithAddedFields reportWithAddedFields)
            throws JSONException, IOException {
        addDataFromAdsToLdmAndDropAfterTest(role, addedField.getCleanupMaqlFile(),
                new TestAction() {
                    @Override
                    public void doAction(UserRoles role) {
                        checkReportAfterDataAdding(role, addedField, reportWithAddedFields);
                    }
                });
    }

    protected void checkSuccessfulAddingData(DataSource dataSource, String screenshotName) {
        openAnnieDialog();
        annieUIDialog.selectFields(dataSource);
        Screenshots.takeScreenshot(browser, "select-fields-to-" + screenshotName, getClass());
        annieUIDialog.clickOnApplyButton();

        checkSuccessfulDataAddingResult();
        Screenshots.takeScreenshot(browser, "sucessful-" + screenshotName, getClass());
        annieUIDialog.clickOnCloseButton();

        dataSource.applyAddSelectedFields();

        openAnnieDialog();
        checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
        annieUIDialog.clickOnDismissButton();
    }

    protected void addMultiFieldsAndCheckReport(UserRoles role) throws JSONException, IOException {
        String maqlFile = "dropMultiAddedFieldsInLDM.txt";
        addDataFromAdsToLdmAndDropAfterTest(role, maqlFile, new TestAction() {
            @Override
            public void doAction(UserRoles role) {
                checkReportWithMultiAddedFields(role);
            }
        });
    }

    protected void addMultiFieldsAndAssertAnnieDialog(UserRoles role) throws JSONException, IOException {
        String maqlFile = "dropMultiAddedFieldsInLDM.txt";
        addDataFromAdsToLdmAndDropAfterTest(UserRoles.ADMIN, maqlFile, new TestAction() {
            @Override
            public void doAction(UserRoles role) {
                addMultiFields(role);
            }
        });
    }

    protected void checkNewDataAdding(UserRoles role, final AddedFields addedField) {
        DataSource dataSource =
                prepareADSTable(addedField.getADSTable()).updateDatasetStatus(
                        addedField.getDataset());

        checkSuccessfulAddingData(dataSource, role.getName() + "-add-new-field-"
                + addedField.getField().getName());
    }

    private void checkReportAfterDataAdding(UserRoles role, final AddedFields addedField,
            ReportWithAddedFields reportWithAddedFields) {
        checkNewDataAdding(role, addedField);
        checkReportAfterAddNewData(addedField.getField(), reportWithAddedFields);
    }

    private void checkReportWithMultiAddedFields(UserRoles role) {
        addMultiFields(role);

        checkReportAfterAddNewData(AddedFields.POSITION.getField(), ReportWithAddedFields.POSITION);
        checkReportAfterAddNewData(AddedFields.TOTALPRICE2.getField(),
                ReportWithAddedFields.TOTALPRICE2);
    }

    private void addMultiFields(UserRoles role) {
        Dataset personDataset =
                new Dataset().withName("person").withFields(
                        new Field("Date", FieldTypes.DATE, FieldStatus.SELECTED),
                        new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED));
        Dataset opportunityDataset =
                new Dataset().withName("opportunity").withFields(
                        new Field("Totalprice2", FieldTypes.FACT, FieldStatus.SELECTED),
                        new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED));

        DataSource dataSource =
                prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE).updateDatasetStatus(personDataset,
                        opportunityDataset);

        checkSuccessfulAddingData(dataSource, role.getName() + "-add-new-multi-fields");
    }

    private void chechAvailableDataSource(DataSource dataSource,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        DataSourcesFragment dataSourceList = annieUIDialog.getDataSources();
        WebElement datasourceElement = dataSourceList.selectDataSource(dataSource);
        assertEquals(dataSourceList.getAvailableDatasetCount(datasourceElement),
                datasetInSpecificFilter.size());
        checkAvailableDatasets(datasourceElement, datasetInSpecificFilter, fieldType);
    }

    private void checkAvailableDatasets(WebElement dataSourceElement,
            List<Dataset> datasetInSpecificFilter, FieldTypes fieldType) {
        for (Dataset dataset : datasetInSpecificFilter) {
            DataSourcesFragment dataSourceList = annieUIDialog.getDataSources();
            WebElement datasetElement = dataSourceList.selectDataset(dataSourceElement, dataset);
            List<Field> fieldsInSpecificFilter = dataset.getFieldsInSpecificFilter(fieldType);
            assertEquals(dataSourceList.getAvailableFieldCount(datasetElement),
                    fieldsInSpecificFilter.size(), "Incorrect number of fields in dataset: "
                            + dataset.getName());
            checkAvailableFieldsOfDataset(datasetElement, fieldsInSpecificFilter);
        }
    }

    private void checkAvailableFieldsOfDataset(final WebElement datasetElement,
            Collection<Field> fields) {
        List<WebElement> fieldElements =
                annieUIDialog.getDataSources().getAvailableFields(datasetElement);
        for (final Field field : fields) {
            assertTrue(field.hasCorrespondingWebElement(fieldElements));
        }
    }

    private void checkDataAddingResult(boolean isSuccessful) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver browser) {
                return !"Adding data...".equals(annieUIDialog.getAnnieDialogHeadline());
            }
        });
        if (isSuccessful) {
            assertEquals(annieUIDialog.getAnnieDialogHeadline(), SUCCESSFUL_ANNIE_DIALOG_HEADLINE,
                    "Incorrect dialog headline!");
            assertEquals(annieUIDialog.getIntegrationStatusMessages().get(0).getText(),
                    "Data has been added to your project.", "Incorrect successful message!");
        } else {
            assertEquals(annieUIDialog.getAnnieDialogHeadline(), "Failed to add data",
                    "Incorrect dialog headline!");
            assertEquals(annieUIDialog.getIntegrationStatusMessages().get(0).getText(),
                    "We couldn’t add this data because it contains an error. Show log file",
                    "Incorrect failed message!");
            assertEquals(annieUIDialog.getIntegrationStatusMessages().get(1).getText(),
                    "Please contact a project admin or GoodData Customer Support .",
                    "Incorrect failed message!");
        }
    }

    private void checkReportAfterAddNewData(Field addedField,
            ReportWithAddedFields reportWithAddedFields) {
        if (addedField.getType() == FieldTypes.FACT) {
            createMetric(addedField.getName() + " [Sum]", format("SELECT SUM([%s])",
                    getMdService().getObjUri(getProject(), Fact.class, title(addedField.getName()))), "#,##0.00");
        }

        createAndCheckReport(reportWithAddedFields.getReportName(), reportWithAddedFields.getAttribute(),
                reportWithAddedFields.getMetric(),
                reportWithAddedFields.getAttributeValues(),
                reportWithAddedFields.getMetricValues());
    }

    private void addDataFromAdsToLdmAndDropAfterTest(UserRoles role, String maqlFile,
            TestAction testAction) throws JSONException, IOException {
        try {
            if (role == UserRoles.EDITOR) {
                signInWithOtherRole(UserRoles.EDITOR);
            }
            testAction.doAction(role);
        } finally {
            if (role == UserRoles.EDITOR) {
                signInWithOtherRole(UserRoles.ADMIN);
            }
            dropAddedFieldsInLDM(getResourceAsString("/" + MAQL_FILES + "/" + maqlFile));
        }
    }

    private void signInWithOtherRole(UserRoles role) throws JSONException {
        logout();
        signIn(true, role);
    }

    private interface TestAction {
        void doAction(UserRoles role);
    }

    protected enum ReportWithAddedFields {
        POSITION("Report with Position", "Position", "age [Sum]",
                Lists.newArrayList("A1", "B2", "C3", "D4", "E5", "F6", "J7"),
                Lists.newArrayList("36", "34", "10", "8", "2", "40", "13")),
        POSITION_CONNECTION_POINT("Report with Position", "Position", "age [Sum]",
                Lists.newArrayList("A1", "B2", "C3", "D4", "E5", "F6", "J7"),
                Lists.newArrayList("36", "34", "10", "8", "2", "40", "13")),
        TOTALPRICE2("Report with Totalprice2", "name", "Totalprice2 [Sum]",
                Lists.newArrayList("A", "B", "C", "D", "E", "F"),
                Collections.nCopies(6, "400")),
        LABEL("Report with Label", "title", "price [Sum]",
                Lists.newArrayList("opportunityA", "opportunityB", "opportunityC", "opportunityD", "opportunityE",
                "opportunityF"), Collections.nCopies(6, "100")),
        LABEL_OF_NEW_FIELD("Report with Label", "Title2", "price [Sum]",
                Lists.newArrayList("opportunityA", "opportunityB", "opportunityC", "opportunityD", "opportunityE",
                        "opportunityF"), Collections.nCopies(6, "100")),
        DATE("Report with Date", "Date (Date)", "age [Sum]",
                Lists.newArrayList("01/01/2006", "01/02/2006", "01/02/2007", "01/02/2008", "07/02/2009"),
                Lists.newArrayList("34", "50", "36", "13", "10"));

        private String reportName;
        private String attribute;
        private String metric;
        private Collection<String> attributeValues;
        private Collection<String> metricValues;

        private ReportWithAddedFields(String reportName, String attribute, String metric,
                Collection<String> attributeValues, Collection<String> metricValues) {
            this.reportName = reportName;
            this.attribute = attribute;
            this.metric = metric;
            this.attributeValues = attributeValues;
            this.metricValues = metricValues;
        }

        public String getReportName() {
            return reportName;
        }

        public String getAttribute() {
            return attribute;
        }

        public String getMetric() {
            return metric;
        }

        public Collection<String> getAttributeValues() {
            return this.attributeValues;
        }

        public Collection<String> getMetricValues() {
            return this.metricValues;
        }
    }

    protected enum AddedFields {
        POSITION(
                ADSTables.WITH_ADDITIONAL_FIELDS,
                new Dataset().withName("person"),
                new Field("Position", FieldTypes.ATTRIBUTE, FieldStatus.SELECTED),
                "dropAddedAttributeInLDM_Person_Position.txt"),
        POSITION_CONNECTION_POINT(ADSTables.WITH_ADDITIONAL_CONNECTION_POINT, new Dataset()
                .withName("person"), new Field("Position", FieldTypes.ATTRIBUTE,
                FieldStatus.SELECTED), "dropAddedConnectionPointInLDM_Person_Position.txt"),
        TOTALPRICE2(
                ADSTables.WITH_ADDITIONAL_FIELDS,
                new Dataset().withName("opportunity"),
                new Field("Totalprice2", FieldTypes.FACT, FieldStatus.SELECTED),
                "dropAddedFactInLDM_Opportunity_Totalprice2.txt"),
        LABEL(
                ADSTables.WITH_ADDITIONAL_FIELDS,
                new Dataset().withName("opportunity"),
                new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED),
                "dropAddedLabelInLDM_Opportunity_Label.txt"),
        LABEL_OF_NEW_FIELD(
                ADSTables.WITH_ADDITIONAL_LABEL_OF_NEW_FIELD,
                new Dataset().withName("opportunity").withFields(
                        new Field("Title2", FieldTypes.ATTRIBUTE, FieldStatus.ADDED)),
                new Field("Label", FieldTypes.LABEL_HYPERLINK, FieldStatus.SELECTED),
                "dropAddedLabelInLDM_Opportunity_LabelOfNewField.txt"),
        DATE(ADSTables.WITH_ADDITIONAL_DATE, new Dataset().withName("person"), new Field("Date",
                FieldTypes.DATE, FieldStatus.SELECTED), "dropAddedDateInLDM_Person_Date.txt");

        private ADSTables adsTable;
        private Dataset dataset;
        private Field field;
        private String cleanupMaqlFile;

        private AddedFields(ADSTables adsTable, Dataset dataset, Field field, String cleanupMaqlFile) {
            this.adsTable = adsTable;
            this.dataset = dataset;
            this.field = field;
            this.cleanupMaqlFile = cleanupMaqlFile;
        }

        public ADSTables getADSTable() {
            return this.adsTable;
        }

        public Dataset getDataset() {
            return this.dataset.withFields(field);
        }

        public Field getField() {
            return this.field;
        }

        public String getCleanupMaqlFile() {
            return this.cleanupMaqlFile;
        }
    }
}
