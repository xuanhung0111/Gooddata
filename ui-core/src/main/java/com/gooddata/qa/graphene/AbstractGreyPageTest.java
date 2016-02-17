package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.testng.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceUsersFragment;
import com.gooddata.qa.graphene.fragments.greypages.gdc.GdcFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.etl.pull.PullFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2.Manage2Fragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.singleloadinterface.SingleLoadInterfaceFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.ExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.PartialExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.ImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.PartialImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectElementsFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.query.attributes.QueryAttributesFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.validate.ValidateFragment;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.model.ModelRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;

public class AbstractGreyPageTest extends AbstractTest {

    protected static final By BY_GP_FORM = By.tagName("form");
    protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    protected static final By BY_GP_PRE_JSON = By.tagName("pre");
    protected static final By BY_GP_LINK = By.tagName("a");
    protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    protected static final String PAGE_GDC = "gdc";
    protected static final String PAGE_GDC_MD = PAGE_GDC + "/md";
    protected static final String PAGE_GDC_PROJECTS = PAGE_GDC + "/projects";
    protected static final String PAGE_ACCOUNT_LOGIN = PAGE_GDC + "/account/login";
    protected static final String HOST_NAME = "%{HostName}";
    protected static final String PROJECT_ID = "%{ProjectID}";
    /**
     * ----- Grey pages fragments -----
     */

    @FindBy(tagName = "form")
    protected AccountLoginFragment gpLoginFragment;

    @FindBy(tagName = "form")
    protected ProjectFragment gpProject;

    @FindBy(tagName = "form")
    protected ValidateFragment validateFragment;

    @FindBy(className = "param")
    protected GdcFragment gdcFragment;

    @FindBy(tagName = "form")
    protected Manage2Fragment manage2Fragment;

    @FindBy(tagName = "form")
    protected ExportFragment exportFragment;

    @FindBy(tagName = "form")
    protected PartialExportFragment partialExportFragment;

    @FindBy(tagName = "form")
    protected ImportFragment importFragment;

    @FindBy(tagName = "form")
    protected PartialImportFragment partialImportFragment;

    @FindBy(tagName = "form")
    protected PullFragment pullFragment;

    @FindBy(tagName = "pre")
    protected QueryAttributesFragment queryAttributesFragment;

    @FindBy(tagName = "pre")
    protected ObjectFragment objectFragment;

    @FindBy(tagName = "pre")
    protected ObjectElementsFragment objectElementsFragment;

    @FindBy(tagName = "form")
    protected SingleLoadInterfaceFragment singleLoadInterfaceFragment;

    @FindBy(tagName = "form")
    protected InstanceFragment storageForm;

    @FindBy(tagName = "form")
    protected InstanceUsersFragment storageUsersForm;

    public JSONObject loadJSON() throws JSONException {
        waitForElementPresent(BY_GP_PRE_JSON, browser);
        return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
    }

    public void signInAtGreyPages(String username, String password) throws JSONException {
        openUrl(PAGE_ACCOUNT_LOGIN);
        waitForElementPresent(gpLoginFragment.getRoot());
        gpLoginFragment.login(username, password);
        Screenshots.takeScreenshot(browser, "login-gp", this.getClass());
    }

    public String validateProject() throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/validate");
        waitForElementPresent(validateFragment.getRoot());
        int timeout = testParams.getProjectDriver() == ProjectDriver.VERTICA ?
                testParams.getExtendedTimeout() : testParams.getDefaultTimeout();
        String statusReturning = validateFragment.validate(timeout);
        Screenshots.takeScreenshot(browser, testParams.getProjectId() + "-validation", this.getClass());
        System.out.println("Validation result: " + statusReturning);
        return statusReturning;
    }

    public void postMAQL(String maql, int statusPollingCheckIterations) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/ldm/manage2");
        waitForElementPresent(manage2Fragment.getRoot());
        assertTrue(manage2Fragment.postMAQL(maql, statusPollingCheckIterations), "MAQL was not successfully processed");
    }

    public String uploadFileToWebDav(URL resourcePath, String webContainer) throws URISyntaxException {
        WebDavClient webDav = WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = new File(resourcePath.toURI());
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                    "Create WebDav storage structure");
        } else {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructureIfNotExists(gdcFragment.getUserUploadsURL()),
                    "Create WebDav storage structure if not exists");
            webDav.setWebDavStructure(webContainer);
        }

        webDav.uploadFile(resourceFile);
        return webDav.getWebDavStructure();
    }

    public InputStream getFileFromWebDav(String webContainer, URL resourcePath) throws URISyntaxException, IOException {
        File resourceFile = new File(resourcePath.toURI());
        return WebDavClient.getInstance(testParams.getUser(), testParams.getPassword()).getFile(webContainer + "/" + resourceFile.getName());
    }

    public String exportProject(boolean exportUsers, boolean exportData, boolean crossDataCenter,
            int statusPollingCheckIterations) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/export");
        waitForElementPresent(exportFragment.getRoot());
        return exportFragment.invokeExport(exportUsers, exportData, crossDataCenter, statusPollingCheckIterations);
    }

    public String exportPartialProject(String exportObjectUri, int statusPollingCheckIterations) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/partialmdexport");
        waitForElementPresent(partialExportFragment.getRoot());
        return partialExportFragment.invokeExport(exportObjectUri, statusPollingCheckIterations);
    }

    public void importProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/import");
        waitForElementPresent(importFragment.getRoot());
        assertTrue(importFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Project import failed");
    }

    public void importPartialProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/partialmdimport");
        waitForElementPresent(partialImportFragment.getRoot());
        assertTrue(partialImportFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Partial project import failed");
    }

    public void postPullIntegration(String integrationEntry, int statusPollingCheckIterations)
            throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/etl/pull");
        waitForElementPresent(pullFragment.getRoot());
        assertTrue(pullFragment.invokePull(integrationEntry, statusPollingCheckIterations),
                "ETL PULL was not successfully processed");
    }

    public JSONObject fetchSLIManifest(String dataset) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/ldm/singleloadinterface");
        waitForElementPresent(singleLoadInterfaceFragment.getRoot());
        return singleLoadInterfaceFragment.postDataset(dataset);
    }

    public int getAttributeID(String attributeTitle) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/query/attributes");
        waitForElementPresent(queryAttributesFragment.getRoot());
        return queryAttributesFragment.getAttributeIDByTitle(attributeTitle);
    }

    public JSONObject getObjectByID(int objectID) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/obj/" + objectID);
        waitForElementPresent(objectFragment.getRoot());
        return objectFragment.getObject();
    }

    public ArrayList<Pair<String, Integer>> getObjectElementsByID(int objectID) throws JSONException {
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/obj/" + objectID + "/elements");
        waitForElementPresent(objectElementsFragment.getRoot());
        return objectElementsFragment.getObjectElements();
    }

    @SuppressWarnings("unchecked")
    public void verifyLDMModelProject(long expectedSize) throws ParseException, IOException, JSONException {
        //download folder is not created automatically
        new File(testParams.getDownloadFolder()).mkdir();
        File imageFileName = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator() + getLDMImageFile());
        replaceContentInSVGFile(imageFileName, Pair.of(testParams.getHost(), HOST_NAME), Pair.of(testParams.getProjectId(), PROJECT_ID));
        System.out.println("imageFileName = " + imageFileName);
        long fileSize = imageFileName.length();
        System.out.println("File size: " + fileSize);
        assertTrue(fileSize == expectedSize, "LDM is probably invalid, check the LDM image manually! Current size is " + fileSize + ", but" + expectedSize + "in size was expected");
    }

    protected void addUsersWithOtherRolesToProject() throws ParseException, IOException, JSONException {
        addEditorUserToProject();
        addViewerUserToProject();
    }

    protected void addEditorUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getEditorUser(), UserRoles.EDITOR);
    }

    protected void addViewerUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(testParams.getViewerUser(), UserRoles.VIEWER);
    }

    protected void addUserToProject(String email, UserRoles userRole) throws ParseException, IOException, JSONException {
        UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), email, userRole);
    }

    private String getLDMImageFile() throws ParseException, IOException, JSONException {
        String imageURI = ModelRestUtils.getLDMImageURI(getRestApiClient(), testParams.getProjectId(),
                testParams.getHost());
        int indexSVG = imageURI.indexOf(".svg");
        String imageFileName = imageURI.substring(0, indexSVG + 4);
        imageFileName = imageFileName.substring(imageFileName.lastIndexOf("/") + 1);
        downloadFile(imageURI, imageFileName);
        return imageFileName;
    }

    private void downloadFile(String href, String filename) throws IOException {
        URL url = new URL(href);
        InputStream in = new BufferedInputStream(url.openStream());
        OutputStream out = new BufferedOutputStream(new FileOutputStream(testParams.getDownloadFolder() +
                testParams.getFolderSeparator() + filename));
        for ( int i; (i = in.read()) != -1; ) {
            out.write(i);
        }
        out.close();
        in.close();
    }

    protected void replaceContentInSVGFile(File file,
            @SuppressWarnings("unchecked") Pair<String,String>... replaceStrings) throws IOException {
        FileInputStream input = new FileInputStream(file);
        FileOutputStream output = null;
        try {
            String content = IOUtils.toString(input);
            for (Pair<String,String> replaceString : replaceStrings) {
                // process the hostname in svg file if it has some prefix (na1 or ea)
                if (HOST_NAME.equals(replaceString.getRight())) {
                    String pattern = "https://(\\w+.)*" + replaceString.getLeft();
                    content = content.replaceAll(pattern,"https://" + replaceString.getRight());
                    continue;
                }
                content = content.replaceAll(replaceString.getLeft(), replaceString.getRight());
            }
            output = new FileOutputStream(file);
            IOUtils.write(content, output);
        } finally {
            input.close();
            if (output!= null) output.close();
        }
    }
}
