package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.utils.S3Utils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;

public class UploadFileToS3Test extends AbstractProcessTest {
    private String defaultS3AccessKey;
    private String defaultS3SecretKey;
    private final String DEFAULT_S3_BUCKET_URI = "s3://msf-dev-grest/";

    @BeforeClass(alwaysRun = true)
    public void disableDynamicUser() {
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = "createProject")
    public void uploadFileToS3() throws URISyntaxException, IOException {
        defaultS3AccessKey = testParams.loadProperty("s3.accesskey");
        defaultS3SecretKey = testParams.loadProperty("s3.secretkey");
        File file = getCSVFile();
        S3Utils.deleteFile("_mtt_daily/s3/source/users.csv", defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        S3Utils.uploadFile(file, "_mtt_daily/s3/source/users.csv", defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
    }

    private  File getCSVFile() throws IOException {
        final CsvFile csvFile = CsvFile.loadFile(getFilePathFromResource("/users.csv"));
        int i = 0;
        while(i < 10) {
            csvFile.rows(generate8HashString(), "Phong", "HoChiMinh city", "20", "20")
                    .saveToDisc(testParams.getCsvFolder());
            i++;
        }
        return new File(csvFile.getFilePath());
    }
}
