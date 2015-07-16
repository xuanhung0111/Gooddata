package com.gooddata.qa.graphene.fragments.manage;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class EmailSchedulePage extends AbstractFragment {

    private static final By BY_SCHEDULE_AUTHOR = By.cssSelector(".author a");
    private static final By BY_SCHEDULE_BCC_EMAILS = By.cssSelector(".bcc span");
    private static final By BY_SCHEDULE_CONTROLS = By.cssSelector(".controls span");
    private static final By BY_PARENT_TR_TAG = By.xpath("ancestor::tr[1]");
    private static final By BY_PRIVATE_SCHEDULES_TABLE_HIDDEN = By.cssSelector(".dashCreated.hidden");
    private static final By BY_SCHEDULE_EMAIL_TITLES = By.cssSelector(".s-dataPage-listRow .title span");

    @FindBy(css = ".s-btn-schedule_new_email")
    private WebElement addScheduleButton;

    @FindBy(css = ".noSchedulesMsg")
    private WebElement noSchedulesMessage;

    @FindBy(css = ".listView>.listTable")
    private WebElement globalSchedulesTable;

    @FindBy(css = ".listView .dashCreated .listTable")
    private WebElement privateSchedulesTable;

    @FindBy(css = ".detailView")
    private WebElement scheduleDetail;

    @FindBy(name = "emailAddresses")
    private WebElement emailToInput;

    @FindBy(name = "emailSubject")
    private WebElement emailSubjectInput;

    @FindBy(name = "emailBody")
    private WebElement emailMessageInput;

    @FindBy(css = ".objectSelect .dashboards")
    private WebElement dashboardsSelector;

    @FindBy(css = ".objectSelect .reports")
    private WebElement reportsSelector;

    @FindBy(css = ".dashboards .picker .yui3-c-simpleColumn-window.loaded .c-checkBox")
    private List<WebElement> dashboardsList;

    @FindBy(css = ".reports .picker .yui3-c-simpleColumn-window.loaded .c-checkBox")
    private List<WebElement> reportsList;

    @FindBy(css = ".reports .exportFormat .c-checkBox")
    private List<WebElement> formatsList;

    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;

    @FindBy(css = "#unsubscribeTooltip span.info")
    private WebElement unsubscribeTooltip;

    @FindBy(xpath = "//div[@id='gd-overlays']//div[contains(@class,'bubble-primary')]//div[contains(@class,'bubble-content')]//div[contains(@class,'content')]")
    private WebElement unsubscribedTooltipAddresses;

    @FindBy(css = ".timeScheduler .description")
    private WebElement timeDescription;

    @FindBy(css = ".dashboards .picker .selected label")
    private List<WebElement> attachedDashboards;

    @FindBy(css = ".pickers > :not([style*='display: none']) input.gdc-input")
    private WebElement searchInput;

    public String getSubjectFromInput() {
        return waitForElementVisible(emailSubjectInput).getAttribute("value");
    }

    public String getMessageFromInput() {
        return waitForElementVisible(emailMessageInput).getAttribute("value");
    }

    public String getToFromInput() {
        return waitForElementVisible(emailToInput).getAttribute("value");
    }

    public String getTimeDescription() {
        return timeDescription.getText();
    }

    public List<String> getAttachedDashboards() {
        List<String> selected = new ArrayList<String>();
        for (WebElement label : attachedDashboards) {
            selected.add(label.getText());
        }
        return selected;
    }

    public void openSchedule(String scheduleName) {
        Graphene.guardAjax(getScheduleLink(scheduleName)).click();
        waitForElementVisible(scheduleDetail);
    }

    public List<WebElement> getGlobalScheduleTitles() {
        return waitForElementPresent(globalSchedulesTable).findElements(BY_SCHEDULE_EMAIL_TITLES);
    }

    public List<WebElement> getPrivateScheduleTitles() {
        return waitForElementPresent(privateSchedulesTable).findElements(BY_SCHEDULE_EMAIL_TITLES);
    }

    public int getNumberOfGlobalSchedules() {
        return getGlobalScheduleTitles().size();
    }

    public int getNumberOfPrivateSchedules() {
        return getPrivateScheduleTitles().size();
    }

    public boolean isGlobalSchedulePresent(String title) {
        return isSchedulePresent(this.getGlobalScheduleTitles(), title);
    }

    public boolean isPrivateSchedulePresent(String title) {
        return isSchedulePresent(this.getPrivateScheduleTitles(), title);
    }

    public String getSubscribed(String scheduleName) {
        openSchedule(scheduleName);
        return waitForElementVisible(emailToInput).getAttribute("value");
    }

    public String getUnsubscribed(String scheduleName) {
        openSchedule(scheduleName);
        waitForElementPresent(unsubscribeTooltip).click();
        String unsubscribed = waitForElementPresent(unsubscribedTooltipAddresses).getText();
        System.out.println("Unsubscribed: " + unsubscribed);
        return unsubscribed;
    }

    public String getNoSchedulesMessage() {
        return waitForElementVisible(noSchedulesMessage).getText();
    }

    public void scheduleNewDahboardEmail(String emailTo, String emailSubject, String emailBody,
            String dashboardName) throws InterruptedException {
        Graphene.guardAjax(waitForElementVisible(addScheduleButton)).click();
        waitForElementVisible(scheduleDetail);
        waitForElementVisible(emailToInput).sendKeys(emailTo);
        emailSubjectInput.sendKeys(emailSubject);
        emailMessageInput.sendKeys(emailBody);
        waitForElementVisible(dashboardsSelector);
        waitForEmailSchedulePageLoaded(browser);
        assertTrue(dashboardsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"),
                "Dashboards selector is not selected by default");
        selectDashboard(dashboardName);
        // TODO - schedule (will be sent in the nearest time slot now)
        Graphene.guardAjax(waitForElementVisible(saveButton)).click();
        waitForElementNotVisible(scheduleDetail);
        waitForElementVisible(globalSchedulesTable);
    }

    public void scheduleNewReportEmail(String emailTo, String emailSubject, String emailBody, String reportName,
            ExportFormat format) throws InterruptedException {
        Graphene.guardAjax(waitForElementVisible(addScheduleButton)).click();
        waitForElementVisible(scheduleDetail);
        waitForElementVisible(emailToInput).sendKeys(emailTo);
        emailSubjectInput.sendKeys(emailSubject);
        emailMessageInput.sendKeys(emailBody);
        waitForElementVisible(reportsSelector).click();
        waitForEmailSchedulePageLoaded(browser);
        assertTrue(reportsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"),
                "Reports selector is not selected");
        selectReport(reportName);
        selectReportFormat(format);
        // TODO - schedule (will be sent in the nearest time slot now)
        Graphene.guardAjax(waitForElementVisible(saveButton)).click();
        waitForElementNotVisible(scheduleDetail);
        waitForElementVisible(globalSchedulesTable);
    }

    public String getScheduleMailUriByName(String scheduleName) {
        String anchorSelector = "tbody td.title.s-title-" + CssUtils.simplifyText(scheduleName) + " a";
        WebElement aElement = waitForElementPresent(globalSchedulesTable).findElement(By.cssSelector(anchorSelector));
        String hRef = aElement.getAttribute("href");

        String[] hRefParts = hRef.split("\\|");
        return hRefParts[hRefParts.length - 1];
    }

    public void deleteSchedule(String scheduleName) {
        String deleteSelector = "tbody td.title.s-title-" + CssUtils.simplifyText(scheduleName) +
                " ~ .controls .s-btn-delete";
        waitForElementVisible(By.cssSelector(deleteSelector), browser).click();
    }

    public WebElement getPrivateSchedule(String title) {
        for (WebElement scheduledEmailsTitle : getPrivateScheduleTitles()) {
            if (scheduledEmailsTitle.getAttribute("title").matches("^" + title + ".*$")) {
                return scheduledEmailsTitle.findElement(BY_PARENT_TR_TAG);
            }
        }

        throw new IllegalArgumentException("Schedule could not found!");
    }

    public String getAuthorUriOfSchedule(WebElement schedule) {
        return waitForElementVisible(schedule).findElement(BY_SCHEDULE_AUTHOR).getAttribute("gdc:link");
    }

    public String getBccEmailsOfPrivateSchedule(WebElement schedule) {
        return waitForElementVisible(schedule).findElement(BY_SCHEDULE_BCC_EMAILS).getAttribute("title");
    }

    public List<String> getControlsOfSchedule(WebElement schedule) {
        List<WebElement> controlElements = waitForElementVisible(schedule).findElements(BY_SCHEDULE_CONTROLS);
        if (controlElements.size() == 0) 
            throw new IllegalArgumentException("No control buttons for this schedule: " + schedule);

        List<String> results = new ArrayList<String>();
        for (WebElement ele : controlElements) {
            results.add(ele.getText());
        }
        return results;
    }

    public boolean isPrivateSchedulesTableVisible() {
        return browser.findElements(BY_PRIVATE_SCHEDULES_TABLE_HIDDEN).isEmpty();
    }

    public boolean isBccColumnPresent() {
        return waitForElementVisible(privateSchedulesTable).findElements(BY_SCHEDULE_BCC_EMAILS).size() > 0;
    }

    private void searchItem(String item) throws InterruptedException {
        waitForElementVisible(searchInput).clear();
        searchInput.sendKeys(item);
        Thread.sleep(2000);
    }

    private void selectDashboard(String dashboardName) throws InterruptedException {
        searchItem(dashboardName);
        waitForCollectionIsNotEmpty(dashboardsList);
        if (dashboardsList != null && dashboardsList.size() > 0) {
            for (WebElement elem : dashboardsList) {
                if (elem.findElement(By.tagName("label")).getText().equals(dashboardName)) {
                    elem.findElement(By.tagName("input")).click();
                    return;
                }
            }
            fail("Requested dashboard wasn't found");
        } else {
            fail("No dashboards are available");
        }
    }

    private void selectReport(String reportName) throws InterruptedException {
        searchItem(reportName);
        waitForCollectionIsNotEmpty(reportsList);
        if (reportsList != null && reportsList.size() > 0) {
            for (WebElement elem : reportsList) {
                if (elem.findElement(By.tagName("label")).getText().equals(reportName)) {
                    elem.findElement(By.tagName("input")).click();
                    return;
                }
            }
            fail("Requested report wasn't found");
        } else {
            fail("No reports are available");
        }
    }

    private void selectReportFormat(ExportFormat format) {
        if (formatsList != null && formatsList.size() > 0) {
            By checkboxLocator = By.tagName("input");
            switch (format) {
                case ALL:
                    for (int i = 1; i < formatsList.size(); i++) {
                        formatsList.get(i).findElement(checkboxLocator).click();
                    }
                    break;
                case PDF:
                    formatsList.get(1).findElement(checkboxLocator).click();
                    break;
                case EXCEL_XLS:
                    formatsList.get(2).findElement(checkboxLocator).click();
                    break;
                case EXCEL_XLSX:
                    formatsList.get(3).findElement(checkboxLocator).click();
                    break;
                case CSV:
                    formatsList.get(4).findElement(checkboxLocator).click();
                    break;
                default:
                    System.out.println("Invalid format!!!");
                    break;
            }
        }
    }

    private WebElement getScheduleLink(String scheduleName) {
        String anchorSelector = "tbody td.title.s-title-" + CssUtils.simplifyText(scheduleName) + " a";
        return waitForElementPresent(globalSchedulesTable).findElement(By.cssSelector(anchorSelector));
    }

    private boolean isSchedulePresent(Collection<WebElement> scheduleTitles, String title) {
        for (WebElement scheduledEmailsTitle : scheduleTitles) {
            if (scheduledEmailsTitle.getAttribute("title").matches("^" + title + ".*$")) {
                return true;
            }
        }

        return false;
    }
}
