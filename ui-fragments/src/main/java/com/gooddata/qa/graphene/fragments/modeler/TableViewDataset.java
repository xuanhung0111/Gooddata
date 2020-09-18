package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;

public class TableViewDataset extends AbstractFragment {
    private static final By TABLE_VIEW_DATASET = By.className("table-view-datasets");
    private static final By TABLE_CELLS = By.className("public_fixedDataTableCell_main");

    @FindBy(className = "fixedDataTableRowLayout_body")
    List<WebElement> rowTable;

    public static final TableViewDataset getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(TableViewDataset.class, waitForElementVisible(TABLE_VIEW_DATASET, searchContext));
    }

    public WebElement getRowByName(String datasetName) {
        WebElement row = rowTable.stream()
                .filter(input -> input.findElements(By.cssSelector(".public_fixedDataTableCell_main .dataset-title")).size() != 0)
                .filter(input -> input.findElement(By.cssSelector(".public_fixedDataTableCell_main .dataset-title")).getText().equals(datasetName))
                .findFirst()
                .get();
        return row;
    }

    public String getStatus(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        return cells.get(1).getText();
    }

    public String getLastLoad(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        return cells.get(2).getText();
    }

    public FileUploadDialog clickButtonUpdateFromFile(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        cells.get(3).findElement(By.className("s-update_from_file")).click();
        return OverlayWrapper.getInstance(browser).getFileUploadDialog();
    }

    public void clickButtonDownloadTemplate(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        cells.get(3).findElement(By.className("s-csv_template")).click();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        wrapper.waittingDialog();
        wrapper.closeWaitingDialog();
    }

    public boolean isButtonUpdateDisable(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        return isElementDisabled(cells.get(3).findElement(By.className("s-update_from_file")));
    }

    public boolean isButtonDownloadTemplateDisable(String datasetName) {
        WebElement row = getRowByName(datasetName);
        List<WebElement> cells = row.findElements(TABLE_CELLS);
        return isElementDisabled(cells.get(3).findElement(By.className("s-csv_template")));
    }
}
