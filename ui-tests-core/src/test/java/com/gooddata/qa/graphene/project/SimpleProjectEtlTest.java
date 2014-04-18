package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@Test(groups = {"projectSimple"}, description = "Tests for basic ETL functionality in GD platform")
public class SimpleProjectEtlTest extends AbstractProjectTest {

    protected int statusPollingCheckIterations = 60; // (60*5s)

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void loadProject() throws JSONException, URISyntaxException, IOException, InterruptedException {
        URL maqlResource = getClass().getResource("/etl/maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource),statusPollingCheckIterations);

        URL csvResource = getClass().getResource("/etl/invoice.csv");
        String webdavURL = uploadFileToWebDav(csvResource, null);

        URL uploadInfoResource = getClass().getResource("/etl/upload_info.json");
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/")+1,webdavURL.length()),statusPollingCheckIterations);
        successfulTest = true;
    }
}