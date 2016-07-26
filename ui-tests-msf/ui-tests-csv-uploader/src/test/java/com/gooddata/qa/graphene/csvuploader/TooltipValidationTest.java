package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;

import java.awt.AWTException;
import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;

public class TooltipValidationTest extends AbstractCsvUploaderTest {

    private static final String ADD_DATA_BUTTON_TOOLTIP = "Add data by loading\n" + "a CSV file.";
    private static final String ADDING_DATASET_TOOLTIP = "Cannot open until data addition is complete.";
    private static final String UPDATE_BUTTON_TOOLTIP = "Replace the current data with\n" + "new data from a CSV file.";
    private static final String DELELE_BUTTON_TOOLTIP = "Delete the dataset, related computed\n" 
                + "measures, and visualizations.";

    private static final String DUPLICATED_NAME_MESSAGE = "The name is already in use. Use an unique name.";
//    private static final String LIMITED_CHARACTER_MESSAGE = "Header name exceeds the limit of 255 characters.";

    @Test(dependsOnGroups = "createProject")
    public void checkTooltipValidation() throws IOException, AWTException {
        String csvFileFilePath = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/24dates.yyyy.csv"))
                .getFilePath();

        initDataUploadPage();

        String tooltipOnAddDataButton = getTooltipFromElement(datasetsListPage.waitForAddDataButtonVisible(), browser);

        takeScreenshot(browser, "Tooltip-on-add-data-button", getClass());
        assertEquals(tooltipOnAddDataButton, ADD_DATA_BUTTON_TOOLTIP);

        datasetsListPage
                .uploadFile(csvFileFilePath)
                .triggerIntegration();

        try {
            Dataset.waitForDatasetLoading(browser);

            By byAddingDatasetTooltip = By.cssSelector(".s-dataset-name .item-disabled");

            if (!isElementPresent(byAddingDatasetTooltip, browser)) {
                throw new NoSuchElementException("Adding dataset tooltip not found!");
            }

            String addingDatasetTooltip = getTooltipFromElement(byAddingDatasetTooltip, browser);

            takeScreenshot(browser, "Adding-dataset-tooltip", getClass());
            assertEquals(addingDatasetTooltip, ADDING_DATASET_TOOLTIP);

        } catch (NoSuchElementException | TimeoutException e) {
            log.info("Selenium is too slow to capture the adding dataset process");
            log.info("Skip checking!");
        }

        Dataset.waitForDatasetLoaded(browser);
        String tooltipOnUpdateButton = getTooltipFromElement(Dataset.BY_UPDATE_BUTTON, browser);

        takeScreenshot(browser, "Tooltip-on-update-dataset-button", getClass());
        assertEquals(tooltipOnUpdateButton, UPDATE_BUTTON_TOOLTIP);

        String tooltipOnDeleteButton = getTooltipFromElement(Dataset.BY_DELETE_BUTTON, browser);

        takeScreenshot(browser, "Tooltip-on-delete-dataset-button", getClass());
        assertEquals(tooltipOnDeleteButton, DELELE_BUTTON_TOOLTIP);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkErrorBubbleValidation() throws AWTException, IOException {
        String csvFileFilePath = new CsvFile("Bubble validation")
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"))
                .rows("Khoa", "99999")
                .saveToDisc(testParams.getCsvFolder());

        initDataUploadPage();

        DataPreviewTable dataPreviewTable = datasetsListPage.uploadFile(csvFileFilePath)
                .getDataPreviewTable();

        dataPreviewTable.changeColumnName(0, "Number");

        takeScreenshot(browser, "Column-name-is-duplicated", getClass());
        assertEquals(getBubbleMessage(browser), DUPLICATED_NAME_MESSAGE);

        /*
         *  Comment this checkpoint due to bug MSF-10176, will remove when this bug is fixed
         */
//        dataPreviewTable.changeColumnName(0, RandomStringUtils.randomAlphabetic(256));
//
//        takeScreenshot(browser, "Column-name-exceeds-255-characters", getClass());
//        assertEquals(getBubbleMessage(browser), LIMITED_CHARACTER_MESSAGE);
    }
}
