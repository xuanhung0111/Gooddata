package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

public class ReportsPage extends AbstractFragment {

    private static final By BY_ADD_FOLDER_INPUT = By.cssSelector("#newDomain input");
    private static final By BY_ADD_FOLDER_SUBMIT_BUTTON = By.cssSelector("#newDomain button.s-newSpaceButton");
    private static final By BY_TAG_CLOUD = By.cssSelector(".c-spaceCloud[style^='display: block'] .bd");
    private static final By BY_DESELECT_TAGS = By.className("deselect");

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

    @FindBy(className = "s-btn-delete___")
    private WebElement deleteReportButton;

    @FindBy(className = "s-group_by")
    private Select groupBy;

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

    public void addNewFolder(String folderName) {
        int currentFoldersCount = customFolders.getNumberOfFolders();

        waitForElementVisible(addFolderButton).click();
        WebElement folderInput = waitForElementVisible(BY_ADD_FOLDER_INPUT, browser);
        folderInput.sendKeys(folderName);
        waitForElementVisible(BY_ADD_FOLDER_SUBMIT_BUTTON, browser).click();
        sleepTightInSeconds(2);

        assertEquals(customFolders.getNumberOfFolders(), currentFoldersCount + 1,
                "Number of folders is not increased");
        assertTrue(customFolders.getAllFolderNames().contains(folderName),
                "New folder name is not present in list");
    }

    public String getSelectedFolderName() {
        return waitForElementVisible(selectedFolderName).getText().trim();
    }

    public String getSelectedFolderDescription() {
        return waitForElementVisible(selectedFolderDescription).getText().trim();
    }

    public boolean isReportVisible(String reportName) { 
        if (isEmpty()) return false;
        return waitForFragmentVisible(reportsList).getAllReportLabels().contains(reportName);
    }

    public boolean isEmpty() {
        return !waitForElementPresent(reportsList.getRoot()).isDisplayed();
    }

    public void tryDeleteReports(String... reports) {
        waitForFragmentVisible(reportsList).selectReports(reports);
        waitForElementVisible(deleteReportButton).click();
        waitForElementVisible(By.className("s-btn-delete"), browser).click();
    }

    public void deleteReports(String... reports) {
        tryDeleteReports(reports);
        checkGreenBar(browser);
    }

    public void moveReportsToFolder(String folder, String... reports) {
        waitForFragmentVisible(reportsList).selectReports(reports);
        waitForElementVisible(moveReportButton).click();
        waitForElementVisible(By.className("ipeEditor"), browser).sendKeys(folder);
        waitForElementVisible(By.className("s-ipeSaveButton"), browser).click();
        checkGreenBar(browser);
    }

    public ReportsPage moveReportsToFolderByDragDrop(String folderName, String reportName) {
        WebElement report = waitForFragmentVisible(reportsList)
                .getReportWebElement(reportName)
                .orElseThrow(() -> new NoSuchElementException("Cannot find report: " + reportName));

        new Actions(browser).dragAndDrop(report, getFolderElement(folderName)).perform();
        sleepTightInSeconds(2);
        return this;
    }

    public Collection<String> getGroupByVisibility() {
        return waitForElementVisible(groupBy).getOptions()
                .stream()
                .map(WebElement::getText)
                .collect(toList());
    }

    public boolean isTagCloudVisible() {
        return isElementPresent(BY_TAG_CLOUD, browser);
    }

    public ReportsPage filterByTag(String tag) {
        if (!isTagCloudVisible()) {
            fail("Tag cloud is not visible!");
        }

        waitForElementVisible(BY_TAG_CLOUD, browser).findElements(BY_LINK)
            .stream()
            .filter(tagElement -> tag.toLowerCase().equals(tagElement.getText()))
            .findFirst()
            .orElseThrow(()-> new NoSuchElementException("Cannot find tag: " + tag))
            .click();
        return this;
    }

    public ReportsPage deselectAllTags() {
        if (!isTagCloudVisible()) {
            fail("Tag cloud is not visible!");
        }

        waitForElementVisible(BY_DESELECT_TAGS, browser).click();
        return this;
    }

    public ReportsPage addFavorite(String... reports) {
        waitForFragmentVisible(reportsList).addFavorite(reports);
        return this;
    }

    public boolean isFolderSelected(String folderName) {
        return getFolderElement(folderName).getAttribute("class").contains("active");
    }

    private WebElement getFolderElement(String folderName) {
        Optional<WebElement> folder = waitForFragmentVisible(defaultFolders)
                .getFolderWebElement(folderName);
        if (!folder.isPresent()) {
            folder = waitForFragmentVisible(customFolders)
                    .getFolderWebElement(folderName);
        }
        return folder.orElseThrow(() -> new NoSuchElementException("Cannot find folder: " + folderName));
    }

    public PersonalInfo getReportOwnerInfoFrom(String reportName) {
        return waitForFragmentVisible(reportsList).getReportOwnerInfoFrom(reportName);
    }

    public UserProfilePage openReportOwnerProfilePageFrom(String reportName) {
        return waitForFragmentVisible(reportsList).openReportOwnerProfilePageFrom(reportName);
    }
}
