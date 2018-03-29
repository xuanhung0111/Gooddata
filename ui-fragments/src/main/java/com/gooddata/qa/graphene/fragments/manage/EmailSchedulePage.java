package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForEmailSchedulePageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;


public class EmailSchedulePage extends AbstractFragment {

    private static final By BY_SCHEDULE_AUTHOR = By.cssSelector(".author a");
    private static final By BY_SCHEDULE_BCC_EMAILS = By.cssSelector(".bcc span");
    private static final By BY_SCHEDULE_CONTROLS = By.cssSelector(".scheduleControls button");
    private static final By BY_PARENT_TR_TAG = By.xpath("ancestor::tr[1]");
    private static final By BY_PRIVATE_SCHEDULES_TABLE_HIDDEN = By.cssSelector(".privateSchedules.hidden");
    private static final By BY_SCHEDULE_EMAIL_TITLES = By.cssSelector(".s-dataPage-listRow .title span");

    private static final String SCHEDULE_SELECTOR = "tbody td.title.s-title-%s";
    private static final String SCHEDULE_ANCHOR_SELECTOR = SCHEDULE_SELECTOR + " a";
    private static final String CONTROL_SELECTOR = SCHEDULE_SELECTOR + " ~ .scheduleControls";
    private static final String DELETE_SELECTOR = CONTROL_SELECTOR + " .s-btn-delete";
    private static final String DUPLICATE_SELECTOR = CONTROL_SELECTOR + " .s-btn-duplicate";
    private static final String AUTOCOMPLETE_SELECTOR = ".emailScheduleForm-ac-item.s-%s";

    @FindBy(css = ".s-btn-schedule_new_email")
    private WebElement addScheduleButton;

    @FindBy(css = ".globalNoSchedulesMsg")
    private WebElement noSchedulesMessage;

    @FindBy(css = ".globalSchedulesTable")
    private WebElement globalSchedulesTable;

    @FindBy(css = ".privateSchedulesTable")
    private WebElement privateSchedulesTable;

    @FindBy(css = ".emailScheduleForm")
    private WebElement scheduleDetail;

    @FindBy(css = ".selectize-input input")
    private WebElement emailToInput;

    @FindBy(css = "select.emailScheduleForm-emails")
    private WebElement emailToSelect;

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

    @FindBy(css = ".repeatBase .selection")
    private Select repeatBaseSelection;

    @FindBy(css = ".dashboards .picker .selected label")
    private List<WebElement> attachedDashboards;

    @FindBy(css = ".emailScheduleForm-atom")
    private List<WebElement> emailScheduleItems;

    @FindBy(css = ".pickers > :not([style*='display: none']) input.gdc-input")
    private WebElement searchReportInput;

    @FindBy(css = ".pickers > :not([style*='display: none']) .tabsPicker input.gdc-input")
    private WebElement searchDashboardTabInput;

    @FindBy(css = ".pickers > :not([style*='display: none']) .dashboardsPicker input.gdc-input")
    private WebElement searchDashboardInput;

    public static final EmailSchedulePage getInstance(SearchContext context) {
        return Graphene.createPageFragment(EmailSchedulePage.class, waitForElementVisible(id("p-emailSchedulePage"), context));
    }

    public String getSubjectFromInput() {
        return waitForElementVisible(emailSubjectInput).getAttribute("value");
    }

    public EmailSchedulePage setSubject(String subject) {
        waitForElementVisible(emailSubjectInput).clear();
        emailSubjectInput.sendKeys(subject);
        return this;
    }

    public String getMessageFromInput() {
        return waitForElementVisible(emailMessageInput).getAttribute("value");
    }

    public EmailSchedulePage openNewSchedule() {
        waitForElementVisible(addScheduleButton).click();
        waitForElementVisible(scheduleDetail);
        return this;
    }

    public List<String> getEmailToListItem() {
        waitForElementVisible(emailToInput);
        return emailScheduleItems.stream().map(emailItem -> emailItem.getText()).collect(Collectors.toList());
    }

    public EmailSchedulePage setMessage(String message) {
        waitForElementVisible(emailMessageInput).clear();
        emailMessageInput.sendKeys(message);
        return this;
    }

    public String getTimeDescription() {
        return timeDescription.getText();
    }

    public EmailSchedulePage changeTime(RepeatTime time) {
        waitForElementVisible(repeatBaseSelection).selectByVisibleText(time.toString());
        return this;
    }

    public List<String> getAttachedDashboards() {
        List<String> selected = new ArrayList<String>();
        for (WebElement label : attachedDashboards) {
            selected.add(label.getText());
        }
        return selected;
    }

    public EmailSchedulePage openSchedule(String scheduleName) {
        Graphene.guardAjax(getScheduleLink(scheduleName)).click();
        waitForElementVisible(scheduleDetail);
        return this;
    }

    public String getScheduleDescription(String scheduleName) {
        String description = format(SCHEDULE_ANCHOR_SELECTOR, simplifyText(scheduleName)) + " span";
        return waitForElementPresent(globalSchedulesTable).findElement(By.cssSelector(description))
                .getAttribute("title");
    }

    public List<WebElement> getGlobalScheduleTitles() {
        return waitForElementPresent(globalSchedulesTable).findElements(BY_SCHEDULE_EMAIL_TITLES);
    }

    public List<WebElement> getPrivateScheduleTitles() {
        return waitForElementVisible(privateSchedulesTable).findElements(BY_SCHEDULE_EMAIL_TITLES);
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
        return waitForElementVisible(emailToSelect).getAttribute("value");
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

    public EmailSchedulePage saveSchedule() {
        Graphene.guardAjax(waitForElementVisible(saveButton)).click();
        waitForElementNotVisible(scheduleDetail);
        waitForElementVisible(globalSchedulesTable);
        return this;
    }

    public void fillToField(String emailTo) {
        searchEmail(emailTo);
        selectEmail(emailTo);
    }

    private void searchEmail(String emailTo) {
        waitForElementVisible(emailToInput).clear();
        emailToInput.sendKeys(emailTo);
        // there can be async call on the background after you search which fills the autocompletion.
        sleepTightInSeconds(2);
    }

    private void selectEmail(String emailTo) {
        WebElement acItem = waitForElementVisible(By.cssSelector(format(AUTOCOMPLETE_SELECTOR, simplifyText(emailTo))), browser);
        acItem.click();
    }

    public EmailSchedulePage scheduleNewDashboardEmail(String emailTo, String emailSubject, String emailBody,
            String dashboardName) {
        openNewSchedule();
        fillToField(emailTo);
        emailSubjectInput.sendKeys(emailSubject);
        emailMessageInput.sendKeys(emailBody);
        waitForElementVisible(dashboardsSelector);
        waitForEmailSchedulePageLoaded(browser);
        assertTrue(dashboardsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"),
                "Dashboards selector is not selected by default");
        selectDashboard(dashboardName);
        // TODO - schedule (will be sent in the nearest time slot now)
        saveSchedule();
        return this;
    }

    public void scheduleNewReportEmail(String emailTo, String emailSubject, String emailBody, String reportName,
            ExportFormat format) {
        scheduleNewReportEmail(emailTo, emailSubject, emailBody, reportName, format, null);
    }

    public void scheduleNewReportEmail(String emailTo, String emailSubject, String emailBody, String reportName,
            ExportFormat format, RepeatTime repeatTime) {
        openNewSchedule();
        fillToField(emailTo);
        emailSubjectInput.sendKeys(emailSubject);
        emailMessageInput.sendKeys(emailBody);
        waitForElementVisible(reportsSelector).click();
        waitForEmailSchedulePageLoaded(browser);
        assertTrue(reportsSelector.getAttribute("class").contains("yui3-c-radiowidgetitem-selected"),
                "Reports selector is not selected");
        selectReport(reportName);
        selectReportFormat(format);
        if (repeatTime != null) {
            changeTime(repeatTime);
        }
        // TODO - schedule (will be sent in the nearest time slot now)
        saveSchedule();
    }

    public String getScheduleMailUriByName(String scheduleName) {
        String anchorSelector = format(SCHEDULE_ANCHOR_SELECTOR, simplifyText(scheduleName));
        WebElement aElement = waitForElementPresent(globalSchedulesTable).findElement(By.cssSelector(anchorSelector));
        String hRef = aElement.getAttribute("href");

        String[] hRefParts = hRef.split("\\|");
        return hRefParts[hRefParts.length - 1];
    }

    public EmailSchedulePage deleteSchedule(final String scheduleName) {
        final int numberOfSchedule = getNumberOfGlobalSchedules();
        waitForElementVisible(By.cssSelector(format(DELETE_SELECTOR, simplifyText(scheduleName))), browser)
            .click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver browser) {
                return getNumberOfGlobalSchedules() == numberOfSchedule - 1;
            }
        });
        return this;
    }

    public void duplicateSchedule(String scheduleName) {
        waitForElementVisible(By.cssSelector(format(DUPLICATE_SELECTOR, simplifyText(scheduleName))), browser)
            .click();
        waitForElementVisible(scheduleDetail);
        saveSchedule();
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

    public EmailSchedulePage selectReportFormat(ExportFormat format) {
        if (formatsList == null || formatsList.isEmpty()) {
            return this;
        }

        By checkboxLocator = By.tagName("input");
        switch (format) {
            case ALL:
                for (WebElement ele : formatsList) {
                    selectCheckbox(ele.findElement(checkboxLocator));
                }
                break;
            case PDF:
                selectCheckbox(formatsList.get(1).findElement(checkboxLocator));
                break;
            case SCHEDULES_EMAIL_EXCEL_XLSX:
                selectCheckbox(formatsList.get(2).findElement(checkboxLocator));
                break;
            case SCHEDULES_EMAIL_CSV:
                selectCheckbox(formatsList.get(3).findElement(checkboxLocator));
                break;
            default:
                System.out.println("Invalid format!!!");
                break;
        }

        return this;
    }

    public List<String> getSelectedFormats() {
        List<String> selectedFormats = Lists.newArrayList();
        if (formatsList == null || formatsList.isEmpty()) {
            return selectedFormats;
        }

        By checkboxLocator = By.tagName("input");
        for (WebElement ele : formatsList) {
            if (ele.findElement(checkboxLocator).isSelected()) {
                selectedFormats.add(ele.getText());
            }
        }
        return selectedFormats;
    }

    private void selectCheckbox(WebElement checkbox) {
        if (checkbox.isSelected()) {
            return;
        }
        checkbox.click();
    }

    private void searchReportItem(String item) {
        searchItem(searchReportInput, item);
    }

    private void searchDashboardItem(String item) {
        searchItem(searchDashboardTabInput, item);
    }

    private void searchItem(WebElement input, String item) {
        waitForElementVisible(input).clear();
        input.sendKeys(item);
        sleepTightInSeconds(2);
    }

    private void selectDashboard(String dashboardName) {
        searchDashboardItem(dashboardName);
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

    private void selectReport(String reportName) {
        searchReportItem(reportName);
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

    private WebElement getScheduleLink(String scheduleName) {
        return waitForElementPresent(globalSchedulesTable).findElement(
                By.cssSelector(format(SCHEDULE_ANCHOR_SELECTOR, simplifyText(scheduleName))));
    }

    private boolean isSchedulePresent(Collection<WebElement> scheduleTitles, String title) {
        for (WebElement scheduledEmailsTitle : scheduleTitles) {
            if (scheduledEmailsTitle.getAttribute("title").matches("^" + title + ".*$")) {
                return true;
            }
        }

        return false;
    }

    public static enum RepeatTime {
        NONE("Does not repeat"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        YEARLY("Yearly");

        private String label;

        private RepeatTime(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
