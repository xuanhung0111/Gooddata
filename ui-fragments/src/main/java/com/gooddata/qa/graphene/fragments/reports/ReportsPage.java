package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportsPage extends AbstractFragment {

    private static final By BY_ADD_FOLDER_INPUT = By.cssSelector("#newDomain input");
    private static final By BY_ADD_FOLDER_SUBMIT_BUTTON = By.cssSelector("#newDomain button.s-newSpaceButton");

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

    @FindBy(className = "s-btn-move___")
    private WebElement moveReportButton;

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
        waitForElementVisible(createReportButton).click();
    }

    public void addNewFolder(String folderName) throws InterruptedException {
        int currentFoldersCount = customFolders.getNumberOfFolders();

        waitForElementVisible(addFolderButton).click();
        WebElement folderInput = waitForElementVisible(BY_ADD_FOLDER_INPUT, browser);
        folderInput.sendKeys(folderName);
        waitForElementVisible(BY_ADD_FOLDER_SUBMIT_BUTTON, browser).click();
        Thread.sleep(2000);

        Assert.assertEquals(customFolders.getNumberOfFolders(), currentFoldersCount + 1, "Number of folders is not increased");
        Assert.assertTrue(customFolders.getAllFolderNames().contains(folderName), "New folder name is not present in list");
    }

    public String getSelectedFolderName() {
        return waitForElementVisible(selectedFolderName).getText().trim();
    }

    public String getSelectedFolderDescription() {
        return waitForElementVisible(selectedFolderDescription).getText().trim();
    }

    public boolean isReportVisible(String reportName) {
        return waitForFragmentVisible(reportsList).getAllReportLabels().contains(reportName);
    }

    public void moveReportsToFolder(String folder, String... reports) {
        waitForFragmentVisible(reportsList).selectReports(reports);
        waitForElementVisible(moveReportButton).click();
        waitForElementVisible(By.className("ipeEditor"), browser).sendKeys(folder);
        waitForElementVisible(By.className("s-ipeSaveButton"), browser).click();
        checkGreenBar(browser);
    }
}
