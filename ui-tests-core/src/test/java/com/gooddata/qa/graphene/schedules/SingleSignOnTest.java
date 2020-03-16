package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertEquals;


public class SingleSignOnTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final Map<String, String> WEB_TAB_DOMAIN_MAPPINGS
        = ImmutableMap.of("isolated1-staging3.intgdc.com", "ATT_isolated_stg3_AD",
        "isolated1-staging2.intgdc.com", "ATT_isolated_stg2_AD",
        "isolated1-staging.intgdc.com", "ATT_isolated_stg1_AD"
    );

    private final String SUBJECT_EMAIL = "Verify your identity in Salesforce";
    private final By MORE_TABS_TAB = By.id("MoreTabs_Tab");
    private final By VERIFICATION_CODE_INPUT= By.id("emc");
    private final String IS_OPEN = "zen-moreTabsActive";

    private String verificationCode;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Override
    protected void customizeProject() throws Throwable { }

    @Override
    public void createProject() { }

    @Override
    public void initProperties() {
        log.info("init properties");
        validateAfterClass = false;
    }

    @Test
    public void loginIsolatedSalesForceDomain_Via_ServiceProviderGoodData() throws IOException, MessagingException {
        String webTabName = WEB_TAB_DOMAIN_MAPPINGS.get(testParams.getHost());

        if (Objects.isNull(webTabName)) {
            throw new NullPointerException("Stop testing because host isn't isolated domain");
        }

        logout();
        loginIsolatedSalesForceDomain();

        browser.get(testParams.getIsolatedDomainSalesForce());
        chooseTab(webTabName);

        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        analysisPage.addMetric(METRIC_AMOUNT).waitForReportComputing().exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog.getInstance(browser).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + "Untitled insight" + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        takeScreenshot(browser, "Untitled insight", getClass());
        log.info("Untitled insight" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            asList(asList("", METRIC_AMOUNT), asList(METRIC_AMOUNT, "1.1662545654E8")));
    }

    private void loginIsolatedSalesForceDomain() throws IOException, MessagingException {
        browser.get("https://" + testParams.getHost());
        LoginFragment.getInstance(browser).clickUseLoginOrganisation();

        waitForElementPresent(By.id("username"), browser).sendKeys(testParams.getUser());
        waitForElementPresent(By.id("password"), browser).sendKeys(testParams.getPassword());
        waitForElementPresent(By.id("Login"), browser).click();
        waitForVerificationCode(SUBJECT_EMAIL);
        sleepTightInSeconds(5);
        verify(verificationCode);

        waitForElementVisible(cssSelector("a.account-menu,.gd-header-account,.hamburger-icon,.logo-anchor"), browser);
        takeScreenshot(browser, "login-ui", this.getClass());
        System.out.println("Successful login with user: " + testParams.getUser());
    }

    private void chooseTab(String name) {
        WebElement moreTabsTab = waitForElementVisible(MORE_TABS_TAB, browser);
        if (!moreTabsTab.getAttribute("class").contains(IS_OPEN)) {
            moreTabsTab.click();
        }
        waitForElementVisible(By.className("wt-" + name), browser).click();
    }

    private void waitForVerificationCode(String title) throws MessagingException, IOException {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        String body = ImapUtils.getLastEmail(imapClient, GDEmails.NOREPLY_SALES_FORCE, title, -1)
            .getBody();

        verificationCode = body.substring(body.indexOf("Verification Code: "), body.indexOf("If you didn't"))
            .split("Verification Code: ")[1];
        log.info("Verification Code: " + verificationCode);
    }

    private void verify(String code) {
        waitForElementPresent(VERIFICATION_CODE_INPUT, browser).sendKeys(code);
        sleepTightInSeconds(2);
        try {
            waitForElementPresent(By.id("save"), browser).click();
            waitForElementNotPresent(By.id("save"), browser);
        } catch (TimeoutException e) {
            // Redirect to SaleForce so WebDriver unable to catch the loading indicator
        }
    }
}
