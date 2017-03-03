package com.gooddata.qa.graphene.disc.common;

import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.warehouse.Warehouse;

public class AbstractDataloadScheduleTest extends __AbstractDISCTest {

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "Automated Data Distribution";
    protected static final String UPDATE_ADS_TABLE_EXECUTABLE = "DLUI/graph/CreateAndCopyDataToADS.grf";

    protected Warehouse ads;
    protected DataloadProcess updateAdsTableProcess;

    @Test(dependsOnGroups = {"createProject"}, groups = {"initDataload"})
    public void setup() throws ParseException, JSONException, IOException {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_DATA_EXPLORER, true);

        ads = getAdsHelper().createAds("ads-" + generateHashString(),
                testParams.loadProperty("dss.authorizationToken"));

        getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());

        updateAdsTableProcess = createProcess(generateProcessName(), PackageFile.ADS_TABLE, ProcessType.CLOUD_CONNECT);
    }

    @AfterClass(alwaysRun = true)
    public void removeAdsInstance() throws ParseException, JSONException, IOException {
        getAdsHelper().removeAds(ads);
    }

    protected DataloadProcess getDataloadProcess() {
        return getProcessService().listProcesses(getProject()).
                stream().filter(p -> p.getType().equals("DATALOAD")).findFirst().get();
    }

    protected enum TxtFile {
        CREATE_LDM("createLdm.txt"),
        CREATE_REFERENCE_LDM("createReferenceLdm.txt"),
        ADS_TABLE("adsTable.txt"),
        LARGE_ADS_TABLE("largeAdsTable.txt"),
        REFERENCE_ADS_TABLE("referenceAdsTable.txt");

        private String name;

        private TxtFile(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
