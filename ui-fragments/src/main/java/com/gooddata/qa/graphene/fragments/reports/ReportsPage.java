package com.gooddata.qa.graphene.fragments.reports;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ReportsPage extends AbstractFragment {

    private static final By BY_ADD_FOLDER_INPUT = By.xpath("..//input");
    private static final By BY_ADD_FOLDER_SUBMIT_BUTTON = By.xpath("../div//button[contains(@class,'s-newSpaceButton')]");

    @FindBy(id = "folderDomains")
    private ReportsFolders defaultFolders;

    @FindBy(id = "sharedDomains")
    private ReportsFolders customFolders;

    @FindBy(xpath = "//span[@id='newDomain']/button")
    private WebElement addFolderButton;

    @FindBy(xpath = "//div[@id='domain']/div/h1")
    private WebElement selectedFolderName;

    @FindBy(xpath = "//div[@id='domain']/div/p[@class='description']")
    private WebElement selectedFolderDescription;

    @FindBy(xpath = "//div[@id='reportList']")
    private ReportsList reportsList;

    @FindBy(xpath = "//button[text()='Create Report']")
    private WebElement createReportButton;

    public ReportsFolders getDefaultFolders() {
        return defaultFolders;
    }

    public ReportsFolders getCustomFolders() {
        return customFolders;
    }

    public ReportsList getReportsList() {
        return reportsList;
    }

    public void startCreateReport() {
        waitForElementVisible(createReportButton);
        createReportButton.click();
    }

    public void addNewFolder(String folderName) {
        int currentFoldersCount = customFolders.getNumberOfFolders();
        addFolderButton.click();
        waitForElementVisible(addFolderButton.findElement(BY_ADD_FOLDER_INPUT));
        addFolderButton.findElement(BY_ADD_FOLDER_INPUT).sendKeys(folderName);
        addFolderButton.findElement(BY_ADD_FOLDER_SUBMIT_BUTTON).click();
        Assert.assertEquals(customFolders.getNumberOfFolders(), currentFoldersCount + 1, "Number of folders increased");
        Assert.assertTrue(customFolders.getAllFolderNames().contains(folderName), "New folder name is present in list");
    }

    public String getSelectedFolderName() {
        return selectedFolderName.getText();
    }

    public String getSelectedFolderDescription() {
        return selectedFolderDescription.getText();
    }
}
