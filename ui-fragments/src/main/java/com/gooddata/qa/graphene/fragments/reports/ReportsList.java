package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForUserProfilePageLoaded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.AccountCard;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

public class ReportsList extends AbstractFragment {

    private static final By BY_REPORT_LABEL = By.xpath("h3/a");
    private static final By BY_REPORT_CHECKBOX = By.tagName("input");
    private static final By BY_FAVORITE = By.className("favorite");
    private static final By BY_USER_LABEL = By.xpath("p//a");
    public static final By ACCOUNT_CARD_LOCATOR = By.cssSelector(".bd_container div.userInfo");
    public static final By USER_PROFILE_LOCATOR = By.cssSelector("#p-profilePage");

    @FindBy(css = "div.report")
    private List<WebElement> reports;

    public List<WebElement> getReports() {
        return reports;
    }

    /**
     * Method to get number of reports
     *
     * @return number of reports
     */
    public int getNumberOfReports() {
        return reports.size();
    }

    /**
     * Method for opening report
     *
     * @param i - report index
     */
    public void openReport(int i) {
        getReportWebElement(i).findElement(BY_REPORT_LABEL).click();
    }

    /**
     * Method for opening report
     *
     * @param reportName - report name
     */
    public void openReport(String reportName) {
        for (int i = 0; i < reports.size(); i++) {
            if (getReportLabel(i).equals(reportName)) {
                openReport(i);
                return;
            }
        }
        Assert.fail("Folder with given name does not exist!");
    }

    /**
     * Method to get label of report with given index
     *
     * @param i - report index
     * @return label of report with given index
     */
    public String getReportLabel(int i) {
        WebElement elem = getReportWebElement(i).findElement(BY_REPORT_LABEL);
        return elem.getText();
    }

    /**
     * Method to get link of report with given index
     *
     * @param i - report index
     * @return link of report with given index
     */
    public String getReportLink(int i) {
        WebElement elem = getReportWebElement(i).findElement(BY_REPORT_LABEL);
        return elem.getAttribute("href");
    }

    /**
     * Method to get all report labels
     *
     * @return List<String> with all report labels
     */
    public List<String> getAllReportLabels() {
        List<String> reportLabels = new ArrayList<String>();
        for (int i = 0; i < reports.size(); i++) {
            reportLabels.add(getReportLabel(i));
        }
        return reportLabels;
    }

    public void selectReports(String... reports) {
        List<String> reportNames = Arrays.asList(reports);
        for (int i = 0; i < this.reports.size(); i++) {
            if (reportNames.contains(getReportLabel(i))) {
                getReportWebElement(i).findElement(BY_REPORT_CHECKBOX).click();
            }
        }
    }

    public void addFavorite(String... reports) {
        List<String> reportNames = Arrays.asList(reports);
        for (int i = 0; i < this.reports.size(); i++) {
            if (reportNames.contains(getReportLabel(i))) {
                getReportWebElement(i).findElement(BY_FAVORITE).click();
            }
        }
    }

    public Optional<WebElement> getReportWebElement(String name) {
        return reports.stream()
                .filter(element -> name.equals(element.findElement(BY_REPORT_LABEL).getText()))
                .findFirst();
    }

    public PersonalInfo getReportOwnerInfoFrom(String reportName) {
        new Actions(browser).moveToElement(getReportOwner(reportName)).perform();
        AccountCard accountCard = Graphene.createPageFragment(AccountCard.class,
                waitForElementVisible(ACCOUNT_CARD_LOCATOR, browser));
        return accountCard.getUserInfo();
    }

    public UserProfilePage openReportOwnerProfilePageFrom(String reportName) {
        getReportOwner(reportName).click();
        waitForUserProfilePageLoaded(browser);
        return Graphene.createPageFragment(UserProfilePage.class,
                waitForElementVisible(USER_PROFILE_LOCATOR, browser));
    }

    private WebElement getReportOwner(String reportName) {
        return getReportWebElement(reportName).get().findElement(BY_USER_LABEL);
    }

    private WebElement getReportWebElement(int i) {
        return reports.get(i);
    }
}
