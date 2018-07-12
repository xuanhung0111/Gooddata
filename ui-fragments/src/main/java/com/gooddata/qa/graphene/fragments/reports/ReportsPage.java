package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForReportsPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForUserProfilePageLoaded;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.List;

import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.AccountCard;
import com.gooddata.qa.graphene.fragments.common.IpeEditor;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;

public class ReportsPage extends AbstractFragment {

    private static final By BY_TAG_CLOUD = By.cssSelector(".c-spaceCloud[style^='display: block'] .bd");
    private static final By BY_DESELECT_TAGS = By.className("deselect");
    private static final String REPORT_LIST_ID = "reportList";

    @FindBy(css = "#domainList li")
    private List<WebElement> folders;

    @FindBy(xpath = "//span[@id='newDomain']/button")
    private WebElement addFolderButton;

    @FindBy(xpath = "//div[@id='domain']/div/h1")
    private WebElement selectedFolderName;

    @FindBy(xpath = "//div[@id='domain']/div/p[@class='description']")
    private WebElement selectedFolderDescription;

    @FindBy(css = "#" + REPORT_LIST_ID + " .report")
    private List<ReportEntry> reports;

    @FindBy(xpath = "//button[text()='Create Report']")
    private WebElement createReportButton;

    @FindBy(className = "s-btn-move___")
    private WebElement moveReportButton;

    @FindBy(className = "s-btn-delete___")
    private WebElement deleteReportButton;

    @FindBy(className = "s-btn-permissions___")
    private WebElement permissionButton;

    @FindBy(css = ".grayBg .simpleBtnCyan")
    private List<WebElement> actionButtons;

    @FindBy(className = "s-group_by")
    private Select groupBy;

    public static final ReportsPage getInstance(SearchContext context) {
        waitForReportsPageLoaded(context);
        return Graphene.createPageFragment(ReportsPage.class, waitForElementVisible(id("p-domainPage"), context));
    }

    public ReportPage startCreateReport() {
        waitForElementVisible(createReportButton).click();
        waitForAnalysisPageLoaded(browser);
        return ReportPage.getInstance(browser);
    }

    public ReportsPage clickAddFolderButton() {
        waitForElementVisible(addFolderButton).click();
        return this;
    }

    public void clickReportOwner(String reportName) {
        getReport(reportName).getOwner().click();
    }

    public boolean isReportLocked(String reportName) {
        return getReport(reportName).isLocked();
    }

    public boolean isEdiTableReport(String reportName) {
        return getReport(reportName).isEditable();
    }

    public ReportsPage addNewFolder(String folderName) {
        waitForElementVisible(addFolderButton).click();
        IpeEditor.getInstance(browser).setText(folderName);
        sleepTightInSeconds(2);

        return this;
    }

    public List<String> getTitlesOfActionButtons() {
        return actionButtons.stream().map(actionButton -> actionButton.getText()).collect(toList());
    }

    public String getSelectedFolderName() {
        return waitForElementVisible(selectedFolderName).getText().trim();
    }

    public String getSelectedFolderDescription() {
        return waitForElementVisible(selectedFolderDescription).getText().trim();
    }

    public boolean isReportVisible(String reportName) { 
        if (isEmpty()) return false;
        return reports.stream().map(ReportEntry::getLabel).collect(toList()).contains(reportName);
    }

    public boolean isEmpty() {
        return !waitForElementPresent(id(REPORT_LIST_ID), getRoot()).isDisplayed();
    }

    public void selectReportsAndOpenDeleteDialog(String... reports) {
        selectReports(reports);
        waitForElementVisible(deleteReportButton).click();
    }

    public void tryDeleteReports(String... reports) {
        selectReportsAndOpenDeleteDialog(reports);
        waitForElementVisible(By.className("s-btn-delete"), browser).click();
    }

    public void deleteReports(String... reports) {
        tryDeleteReports(reports);
        checkGreenBar(browser);
    }

    public PermissionSettingDialog selectReportsAndOpenPermissionDialog(String... reports) {
        selectReports(reports);
        waitForElementVisible(permissionButton).click();
        return PermissionSettingDialog.getInstance(browser);
    }

    public void selectReportsAndOpenMoveDialog(String... reports) {
        selectReports(reports);
        waitForElementVisible(moveReportButton).click();
    }

    public ReportsPage moveReportsToFolder(String folder, String... reports) {
        String reportNumber = getNumberOfReportsInFolder(folder);
        selectReportsAndOpenMoveDialog(reports);

        waitForElementVisible(By.cssSelector(".c-ipeEditor:not([style*='display: none']) input"), browser).sendKeys(folder);
        waitForElementVisible(By.className("s-ipeSaveButton"), browser).click();
        waitForReportUpdatedFrom(folder, reportNumber);
        checkGreenBar(browser);
        waitForElementVisible(By.cssSelector("#status .box-success button"), browser).click();

        return this;
    }

    public ReportsPage moveReportsToFolderByDragDrop(String folderName, String reportName) {
        String reportNumber = getNumberOfReportsInFolder(folderName);
        tryToMoveReportsToFolderByDragDrop(folderName, reportName);
        waitForReportUpdatedFrom(folderName, reportNumber);
        return this;
    }

    public ReportsPage tryToMoveReportsToFolderByDragDrop(String folderName, String reportName) {
        WebElement report = reports.stream()
                .filter(entry -> reportName.equals(entry.getLabel()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find report: " + reportName))
                .getRoot();

        getActions().clickAndHold(report).moveToElement(getFolder(folderName))
                .moveByOffset(1, 1).release().perform();
        return this;
    }

    public Collection<String> getGroupByVisibility() {
        return getElementTexts(waitForElementVisible(groupBy).getOptions());
    }

    public boolean isTagCloudVisible() {
        return isElementPresent(BY_TAG_CLOUD, browser);
    }

    public ReportsPage filterByTag(String tag) {
        if (!isTagCloudVisible()) {
            fail("Tag cloud is not visible!");
        }

        int reportCountsBefore = getReportsCount();
        waitForElementVisible(BY_TAG_CLOUD, browser).findElements(BY_LINK)
            .stream()
            .filter(tagElement -> tag.toLowerCase().equals(tagElement.getText()))
            .findFirst()
            .orElseThrow(()-> new NoSuchElementException("Cannot find tag: " + tag))
            .click();
        Graphene.waitGui().until(browser -> getReportsCount() != reportCountsBefore);
        return this;
    }

    public ReportsPage deselectAllTags() {
        if (!isTagCloudVisible()) {
            fail("Tag cloud is not visible!");
        }

        int reportCountsBefore = getReportsCount();
        waitForElementVisible(BY_DESELECT_TAGS, browser).click();
        Graphene.waitGui().until(browser -> getReportsCount() != reportCountsBefore);
        return this;
    }

    public ReportsPage addFavorite(String... reports) {
        List<String> reportNames = asList(reports);

        for (ReportEntry entry: this.reports) {
            if (reportNames.contains(entry.getLabel())) {
                entry.addFavorite();
            }
        }
        return this;
    }

    public boolean isFolderSelected(String folderName) {
        return getFolder(folderName).getAttribute("class").contains("active");
    }

    public PersonalInfo getReportOwnerInfoFrom(String reportName) {
        AccountCard.makeDismiss();
        getActions().moveToElement(getReport(reportName).getOwner()).moveByOffset(1, 1).perform();
        return AccountCard.getInstance(browser).getUserInfo();
    }

    public UserProfilePage openReportOwnerProfilePageFrom(String reportName) {
        clickReportOwner(reportName);
        waitForUserProfilePageLoaded(browser);
        return UserProfilePage.getInstance(browser);
    }

    public ReportsPage openFolder(String folderName) {
        folders.stream()
            .map(e -> e.findElement(BY_LINK))
            .filter(e -> folderName.equals(e.getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Folder with given name does not exist!"))
            .click();

        waitForReportsPageLoaded(browser);
        return this;
    }

    public List<String> getAllFolderNames() {
        return folders.stream()
            .map(e -> e.findElement(BY_LINK))
            .map(WebElement::getText)
            .collect(toList());
    }

    public int getFoldersCount() {
        return folders.size();
    }

    public ReportPage openReport(String report) {
        getReport(report).openReport();
        waitForAnalysisPageLoaded(browser);
        return ReportPage.getInstance(browser).waitForReportExecutionProgress();
    }

    public int getReportsCount() {
        return reports.size();
    }

    public String getNumberOfReportsInFolder(String folder) {
        return getFolder(folder).findElement(By.className("rate")).getText();
    }

    private void selectReports(String... reports) {
        List<String> reportNames = asList(reports);

        for (ReportEntry entry: this.reports) {
            if (reportNames.contains(entry.getLabel())) {
                entry.select();
            }
        }
    }

    private WebElement getFolder(String folderName) {
        return folders.stream()
            .filter(element -> folderName.equals(element.findElement(BY_LINK).getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find folder: " + folderName));
    }

    private ReportEntry getReport(String reportName) {
        return Graphene.createPageFragment(ReportEntry.class,
                waitForElementVisible(xpath(format("//a[@title= '%s']/../..", reportName)), getRoot()));
    }

    private void waitForReportUpdatedFrom(String folder, String oldReportNumber) {
        Graphene.waitGui().ignoring(NoSuchElementException.class)
                .until(browser -> !getNumberOfReportsInFolder(folder).equals(oldReportNumber));
    }

    public static class ReportEntry extends AbstractFragment {

        @FindBy(css = "h3 a")
        private WebElement label;

        @FindBy(tagName = "input")
        private WebElement checkbox;

        @FindBy(className = "favorite")
        private WebElement favorite;

        @FindBy(css = "p a")
        private WebElement user;

        public void addFavorite() {
            waitForElementVisible(favorite).click();
        }

        public String getLabel() {
            return waitForElementVisible(label).getText();
        }

        public void select() {
            waitForElementVisible(checkbox).click();
        }

        public WebElement getOwner() {
            return waitForElementVisible(user);
        }

        public boolean isLocked() {
            return isElementVisible(By.className("s-lockIcon"), getRoot());
        }

        public boolean isEditable() {
            return isElementVisible(By.tagName("input"), getRoot());
        }

        public void openReport() {
            waitForElementVisible(label).sendKeys(Keys.ENTER);
        }
    }
}
