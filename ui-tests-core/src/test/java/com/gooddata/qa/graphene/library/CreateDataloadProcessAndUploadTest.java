package com.gooddata.qa.graphene.library;

import com.gooddata.GoodData;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Test creating dataload process and upload to webdav by using new instance of gooddata client
 * instead of the one used to create project
 */
public class CreateDataloadProcessAndUploadTest extends AbstractProjectTest {

    @Override
    public void init(String windowSize) {
        if (testParams.isClientDemoEnvironment()) {
            validateAfterClass = false;
            throw new SkipException("Skip testing dataload in client demo env");
        }
        super.init(windowSize);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testCreatingDataloadProcess() {
        final String processName = generateProcessName();
        RestProfile profile = getProfile(Profile.ADMIN);
        GoodData goodDataClient = new GoodData(profile.getHost().getHostName(), profile.getUsername(),
                profile.getPassword(), profile.getHost().getPort());

        DataloadProcess process = goodDataClient.getProcessService().createProcess(getProject(),
                new DataloadProcess(processName, ProcessType.GRAPH), PackageFile.ADS_TABLE.loadFile());
        log.info("Created dataload process: " + processName);
        goodDataClient.getProcessService().removeProcess(process);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testUploadDataTowebdav() throws IOException {
        final String webdavFolder = generateWebdavFolder();
        RestProfile profile = getProfile(Profile.ADMIN);
        GoodData goodDataClient = new GoodData(profile.getHost().getHostName(), profile.getUsername(),
                profile.getPassword(), profile.getHost().getPort());

        goodDataClient.getDataStoreService().upload(webdavFolder, new FileInputStream(PackageFile.ADS_TABLE.loadFile()));
        log.info("Created webdavfolder:" + webdavFolder);
        goodDataClient.getDataStoreService().delete(webdavFolder);
        log.info("Deleted webdavfolder:" + webdavFolder);
    }

    private String generateProcessName() {
        return "Ads_dataload_process_" + generateHashString();
    }

    private String generateWebdavFolder() {
        return "att_webdav_test_" + generateHashString();
    }
}
