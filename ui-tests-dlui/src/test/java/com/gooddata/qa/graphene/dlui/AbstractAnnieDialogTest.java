package com.gooddata.qa.graphene.dlui;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.fragments.DataSourcesFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import static org.testng.Assert.*;

public abstract class AbstractAnnieDialogTest extends AbstractDLUITest {
    protected static final String ANNIE_DIALOG_HEADLINE = "Add data";

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
                annieUIDialog.getIntegrationStatusMessages().get(1).findElement(By.cssSelector("a"))
                        .getAttribute("href"), GOODDATA_SUPPORT_LINK,
                "Incorrect support link in error message of Annie dialog!");
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

    protected void checkDataAddingProgress() {
        assertEquals(annieUIDialog.getAnnieDialogHeadline(), "Adding data...",
                "Incorrect Annie dialog headline!");
        assertEquals(annieUIDialog.getRunningStateHeading(), "Adding data to your project...",
                "Incorrect running state heading!");
        assertEquals(
                annieUIDialog.getRunningStateMessage(),
                "Uploading this data for these fields may take a while - we will send you an email when it's ready. "
                        + "If you close this dialog, you can still track the progress of data loading on this page:",
                "Incorrect running state message!");
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
            assertEquals(annieUIDialog.getAnnieDialogHeadline(), "Data added successfuly!",
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
}
