package com.gooddata.qa.graphene;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecution;
import com.gooddata.sdk.model.dataload.processes.ProcessType;
import com.gooddata.sdk.model.warehouse.Warehouse;
import com.gooddata.sdk.service.dataload.processes.ProcessService;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static com.gooddata.qa.graphene.AbstractDataloadProcessTest.ATTR_GEO_PUSHPIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.ads.AdsHelper.ADS_DB_CONNECTION_URL;
import static java.lang.String.format;

public class AbstractGeoPushpinTest extends AbstractProjectTest {

    protected static final String CHART_TYPE = "Geo pushpin";
    protected static final String UPDATE_ADS_TABLE_EXECUTABLE = "DLUI/graph/CreateAndCopyDataToADS.grf";
    protected Supplier<Parameters> defaultParameters;
    protected DataloadProcess updateAdsTableProcess;
    protected AdsHelper adsHelper;
    protected Warehouse ads;

    @Override
    protected void customizeProject() throws Throwable {
        setup();
        initData();
        configAttributeToGeoPushpin();
    }

    public void setup() throws ParseException, JSONException, IOException {
        if (BrowserUtils.isFirefox()) {
            throw new SkipException("Skip test case on Firefox Browser due to disabled weblg ");
        }
        adsHelper = new AdsHelper(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        ads = adsHelper.createAds("att-ads-" + generateHashString(), testParams.getAdsToken());

        log.info("---created ads uri:" + ads.getUri());

        adsHelper.associateAdsWithProject(ads);

        updateAdsTableProcess = getProcessService().createProcess(getProject(),
                new DataloadProcess(generateProcessName(), ProcessType.GRAPH),
                DeployProcessForm.PackageFile.ADS_TABLE.loadFile());

        defaultParameters = () -> new Parameters()
                .addParameter(Parameter.ADS_URL, format(ADS_DB_CONNECTION_URL, testParams.getHost(), ads.getId()))
                .addParameter(Parameter.ADS_USER, testParams.getUser())
                .addSecureParameter(Parameter.ADS_PASSWORD, testParams.getPassword());
    }

    public void initData() throws JSONException {
        String adsTableText = SqlBuilder.loadFromFile(SQL_FILES.getPath() + "adsGeoTable.txt");
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "createGeoLdm.txt"));
        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY, adsTableText);
        new RestClient(getProfile(Profile.ADMIN)).getProcessService()
                .executeProcess(new ProcessExecution(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                        parameters.getParameters(), parameters.getSecureParameters()))
                .get();
        openUrl(format(ProjectDetailPage.URI, testParams.getProjectId()));

        Graphene.createPageFragment(ProjectDetailPage.class, waitForElementVisible(
                By.className("ait-project-detail-fragment"), browser)).openCreateScheduleForm()
                .selectProcess(DeployProcessForm.ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle())
                .selectAllDatasetsOption().schedule();
        DataloadScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish().close();
    }

    public void configAttributeToGeoPushpin() {
        initAttributePage().initAttribute(ATTR_GEO_PUSHPIN).selectGeoLableType(CHART_TYPE);
    }

    /* Viet fix to force it follows Aquillian cycle */
    @AfterClass(groups = {"arquillian"}, inheritGroups = true, alwaysRun = true)
    //@AfterClass(alwaysRun = true)
    public void removeAdsInstance() throws ParseException, JSONException, IOException {
        if (BrowserUtils.isFirefox()) {
            throw new SkipException("Skip test case on Firefox Browser due to disabled weblg ");
        }
        adsHelper.removeAds(ads);
    }

    protected ProcessService getProcessService() {
        return getAdminRestClient().getProcessService();
    }

    protected String generateProcessName() {
        return "Process-" + generateHashString();
    }
}
