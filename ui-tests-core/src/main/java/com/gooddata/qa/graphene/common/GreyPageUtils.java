package com.gooddata.qa.graphene.common;

import com.gooddata.qa.graphene.enums.Validation;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.graphene.fragments.greypages.gdc.GdcFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.etl.pull.PullFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2.Manage2Fragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.singleloadinterface.SingleLoadInterfaceFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.ExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.ImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.validate.ValidateFragment;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.webdav.WebDavClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.testng.Assert.assertTrue;

public class GreyPageUtils extends CommonUtils {

    public GreyPageUtils(WebDriver browser, CheckUtils checkUtils, TestParameters testParameters) {
        super(browser, checkUtils, testParameters);
    }

    public static final By BY_GP_FORM = By.tagName("form");
    public static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    public static final By BY_GP_PRE_JSON = By.tagName("pre");
    public static final By BY_GP_LINK = By.tagName("a");
    public static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    public static final String PAGE_GDC = "gdc";
    public static final String PAGE_GDC_MD = PAGE_GDC + "/md";
    public static final String PAGE_GDC_PROJECTS = PAGE_GDC + "/projects";
    public static final String PAGE_ACCOUNT_LOGIN = PAGE_GDC + "/account/login";

    /**
     * ----- Grey pages fragments -----
     */

    @FindBy(tagName = "form")
    public AccountLoginFragment gpLoginFragment;

    @FindBy(tagName = "form")
    public ProjectFragment gpProject;

    @FindBy(tagName = "form")
    public ValidateFragment validateFragment;

    @FindBy(className = "param")
    public GdcFragment gdcFragment;

    @FindBy(tagName = "form")
    public Manage2Fragment manage2Fragment;

    @FindBy(tagName = "form")
    public ExportFragment exportFragment;

    @FindBy(tagName = "form")
    public ImportFragment importFragment;

    @FindBy(tagName = "form")
    public PullFragment pullFragment;

    @FindBy(tagName = "form")
    public SingleLoadInterfaceFragment singleLoadInterfaceFragment;

    public JSONObject loadJSON() throws JSONException {
        checkUtils.waitForElementPresent(BY_GP_PRE_JSON);
        return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
    }

    public void signInAtGreyPages(String username, String password) throws JSONException {
        openUrl(PAGE_ACCOUNT_LOGIN);
        checkUtils.waitForElementPresent(gpLoginFragment.getRoot());
        gpLoginFragment.login(username, password);
        Screenshots.takeScreenshot(browser, "login-gp", this.getClass());
    }

    public String validateProject() throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/validate");
        checkUtils.waitForElementPresent(validateFragment.getRoot());
        String statusReturning = validateFragment.validate();
        Screenshots.takeScreenshot(browser, testParameters.getProjectId() + "-validation", this.getClass());
        return statusReturning;
    }

    public void postMAQL(String maql, int statusPollingCheckIterations) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/ldm/manage2");
        checkUtils.waitForElementPresent(manage2Fragment.getRoot());
        assertTrue(manage2Fragment.postMAQL(maql, statusPollingCheckIterations), "MAQL was not successfully processed");
    }

    public String uploadFileToWebDav(URL resourcePath, String webContainer) throws URISyntaxException {
        WebDavClient webDav = WebDavClient.getInstance(testParameters.getUser(), testParameters.getPassword());
        File resourceFile = new File(resourcePath.toURI());
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            checkUtils.waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()), " Create WebDav storage structure");
        } else webDav.setWebDavStructure(webContainer);

        webDav.uploadFile(resourceFile);
        return webDav.getWebDavStructure();
    }

    public java.io.InputStream getFileFromWebDav(String webContainer, URL resourcePath) throws URISyntaxException, IOException {
        File resourceFile = new File(resourcePath.toURI());
        return WebDavClient.getInstance(testParameters.getUser(), testParameters.getPassword()).getFile(webContainer + "/" + resourceFile.getName());
    }

    public String exportProject(boolean exportUsers, boolean exportData, int statusPollingCheckIterations) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/maintenance/export");
        checkUtils.waitForElementPresent(exportFragment.getRoot());
        return exportFragment.invokeExport(exportUsers, exportData, statusPollingCheckIterations);
    }

    public void importProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/maintenance/import");
        checkUtils.waitForElementPresent(importFragment.getRoot());
        assertTrue(importFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Project import failed");
    }

    public void postPullIntegration(String integrationEntry, int statusPollingCheckIterations)
            throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/etl/pull");
        checkUtils.waitForElementPresent(pullFragment.getRoot());
        assertTrue(pullFragment.invokePull(integrationEntry, statusPollingCheckIterations),
                "ETL PULL was not successfully processed");
    }

    public JSONObject fetchSLIManifest(String dataset) throws JSONException, InterruptedException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/ldm/singleloadinterface");
        checkUtils.waitForElementPresent(singleLoadInterfaceFragment.getRoot());
        return singleLoadInterfaceFragment.postDataset(dataset);
    }

    public String validateProjectPartial(Validation... validationOptions) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParameters.getProjectId() + "/validate");
        checkUtils.waitForElementPresent(validateFragment.getRoot());
        String statusReturning = validateFragment.validateOnly(validationOptions);
        Screenshots.takeScreenshot(browser, testParameters.getProjectId() + "-validation-partial", this.getClass());
        return statusReturning;
    }
}
