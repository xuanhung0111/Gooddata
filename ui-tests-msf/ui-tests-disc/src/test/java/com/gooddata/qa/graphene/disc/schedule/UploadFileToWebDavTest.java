package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.utils.webdav.WebDavClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static java.lang.String.format;

public class UploadFileToWebDavTest extends AbstractProcessTest {

    @BeforeClass(alwaysRun = true)
    public void disableDynamicUser() {
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = "createProject")
    public void loadFileToWebDavTest() throws URISyntaxException, IOException {
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        URL csvResource = getClass().getResource("/webdav/sftp_pgp_csv_issue_details_2048.csv.gpg");
        URL gzipResource = getClass().getResource("/webdav/sftppages.gz.gpg");
        URL feedFileResource = getClass().getResource("/webdav/feed.txt.gpg");
        URL feedFileWebDavResource = getClass().getResource("/webdav/feed.txt");
        URL fileWebdavResource = getClass().getResource("/webdav/webdav_csv_issue_details.csv");
        String uploadURL = format("https://%s/gdc/uploads/", testParams.getHost());
        if (!checkExistingFile(uploadURL + "pgp/data/sftp_pgp_csv_issue_details_2048.csv.gpg")) {
            String webdavCSVURL = uploadFileToWebDav(csvResource, uploadURL + "pgp/data");
            log.info("Upload successfully: " + webdavCSVURL);
        }
        if (!checkExistingFile(uploadURL + "pgp/data/sftppages.gz.gpg")) {
            String webdavGZIPURL = uploadFileToWebDav(gzipResource, uploadURL + "pgp/data");
            log.info("Upload successfully: " + webdavGZIPURL);
        }
        if (!checkExistingFile(uploadURL + "pgp/feed/feed.txt.gpg")) {
            String webdavFeedFileEncryptURL = uploadFileToWebDav(feedFileResource, uploadURL + "pgp/feed");
            log.info("Upload successfully: " + webdavFeedFileEncryptURL);
        }
        if (!checkExistingFile(uploadURL + "webdav/feed/feed.txt")) {
            String webdavFeedFile = uploadFileToWebDav(feedFileWebDavResource, uploadURL + "webdav/feed");
            log.info("Upload successfully: " + webdavFeedFile);
        }
        if (!checkExistingFile(uploadURL + "webdav/data/webdav_csv_issue_details.csv")) {
            String webdavFile = uploadFileToWebDav(fileWebdavResource, uploadURL + "webdav/data");
            log.info("Upload successfully: " + webdavFile);
        }
    }

    private boolean checkExistingFile(String url) throws IOException {
        return WebDavClient.getInstance(testParams.getUser(), testParams.getPassword())
                .isFilePresent(url);
    }

}
