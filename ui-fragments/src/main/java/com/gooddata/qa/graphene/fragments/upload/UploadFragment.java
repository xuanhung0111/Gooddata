package com.gooddata.qa.graphene.fragments.upload;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UploadFragment extends AbstractFragment {

    @FindBy
    private WebElement uploadFile;

    @FindBy(css = "button.s-btn-load")
    private WebElement loadButton;

    @FindBy(css = "div.s-uploadPage-annotation table")
    private UploadColumns uploadColumns;

    public void uploadFile(String filePath) throws InterruptedException {
        System.out.println("Going to upload file: " + filePath);
        waitForElementPresent(uploadFile).sendKeys(filePath);
        waitForElementNotVisible(uploadFile);
        waitForElementVisible(uploadColumns.getRoot());
    }

    public UploadColumns getUploadColumns() {
        return uploadColumns;
    }
    
    public void setColumnsType(UploadColumns uploadColumns, List<Integer> columnIndexs, List<String> dataTypes) throws InterruptedException {
		int index = 0;
    	for(int columnIndex : columnIndexs) {
    		String dataType = dataTypes.get(index);
    		uploadColumns.setColumnType(columnIndex, UploadColumns.OptionDataType.valueOf(dataType));
    		index++;
    	}
    }
    
    public void assertColumnsType(UploadColumns uploadColumns, List<Integer> columnIndexs, List<String> dataTypes) throws InterruptedException {
		int index = 0;
		for(int columnIndex : columnIndexs) {
    		String dataType = dataTypes.get(index);
    		assertEquals(uploadColumns.getColumnType(columnIndex), UploadColumns.OptionDataType.valueOf(dataType).getOptionLabel());
    		index++;
    	}
    }
    
    public void assertColumnsName(UploadColumns uploadColumns, List<Integer> columnIndexs, List<String> columnNames) throws InterruptedException {
		int index = 0;
		for(int columnIndex : columnIndexs) {
    		assertEquals(uploadColumns.getColumnName(columnIndex), columnNames.get(index));
    		index++;
    	}
    }

    public void confirmloadCsv() {
        waitForElementVisible(loadButton).click();
        waitForElementNotVisible(uploadColumns.getRoot());
    }
}
