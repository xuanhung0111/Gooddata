package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.CRC32;

import static org.testng.Assert.assertEquals;

@Test(groups = {"projectSimple"}, description = "Tests for basic ETL functionality in GD platform")
public class SimpleProjectEtlTest extends AbstractProjectTest {

    protected int statusPollingCheckIterations = 60; // (60*5s)
    private static final boolean exportUsers = true;
    private static final boolean exportData = true;

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void loadProject() throws JSONException, URISyntaxException, IOException, InterruptedException {
        URL maqlResource = getClass().getResource("/etl/maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource), statusPollingCheckIterations);

        URL csvResource = getClass().getResource("/etl/invoice.csv");
        String webdavURL = uploadFileToWebDav(csvResource, null);
        InputStream fileFromWebDav = getFileFromWebDav(webdavURL, csvResource);
        System.out.println("Checking local and remote CRC");
        assertEquals(getCRC(csvResource.openStream()), getCRC(fileFromWebDav), "Local and remote file CRC do not match");

        URL uploadInfoResource = getClass().getResource("/etl/upload_info.json");
        uploadFileToWebDav(uploadInfoResource, webdavURL);
        fileFromWebDav = getFileFromWebDav(webdavURL, uploadInfoResource);
        System.out.println("Checking local and remote CRC");
        assertEquals(getCRC(uploadInfoResource.openStream()), getCRC(fileFromWebDav), "Local and remote file CRC checksum do not match");

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()), statusPollingCheckIterations);
    }

    @Test(dependsOnMethods = {"loadProject"}, groups = {"tests"})
    public void exportImportProject() throws JSONException, InterruptedException {
        String exportToken = exportProject(exportUsers, exportData, statusPollingCheckIterations);
        String parentProjectId = projectId;

        // New projectID is needed here. Load it from export, validate, delete and restore original one
        createProject();
        importProject(exportToken,statusPollingCheckIterations);
        validateProject();
        deleteProject();

        projectId = parentProjectId;
        successfulTest = true;
    }

    public static long getCRC(InputStream inputStreamn) throws IOException {
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStreamn.read()) != -1) {
            crc.update(cnt);
        }
        return crc.getValue();
    }
}