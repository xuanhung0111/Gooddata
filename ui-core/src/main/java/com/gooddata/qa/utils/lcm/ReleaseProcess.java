package com.gooddata.qa.utils.lcm;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.json.JSONObject;

import java.util.function.Supplier;

public final class ReleaseProcess extends LcmProcess {

    private String adsUri;

    /**
     * Init a relase process which expected to run on old executor
     */
    ReleaseProcess(final TestParameters testParameters, final String adsUri, final String projectId) {
        super(testParameters, projectId, Brick.ofReleaseBrick(testParameters.getBrickAppstore()));
        this.adsUri = adsUri;
        this.defaultParameters = buildDefaultParams();
    }

    /**
     * Init a relase process which expected to run on k8s executor
     */
    ReleaseProcess(final TestParameters testParameters, final String adsUri, final String projectId, final String lcmProcessName) {
        super(testParameters, projectId, lcmProcessName);
        this.adsUri = adsUri;
        this.defaultParameters = buildDefaultParams();
    }

    @Override
    protected void createLcmProcess() {
        dataloadProcess = restClient.getProcessService().createProcess(getProject(),
                LcmDataloadProcess.createReleaseProcess(this.name, testParameters.getLcmDataloadProcessComponentVersion()));
    }

    private Supplier<Parameters> buildDefaultParams() {
        final JSONObject adsClient = new JSONObject() {{
            put("username", testParameters.getDomainUser());
            put("password", "${ads_password}");
            put("jdbc_url", adsUri);
        }};
        final JSONObject tokens = new JSONObject() {{
            put("pg", testParameters.getAuthorizationToken());
            put("vertica", testParameters.getAuthorizationToken());
        }};

        return () -> new Parameters()
                .addParameter("organization", testParameters.getUserDomain())
                .addParameter("DATA_PRODUCT", LcmRestUtils.ATT_LCM_DATA_PRODUCT)
                .addParameter("GDC_USERNAME", testParameters.getDomainUser())
                .addSecureParameter("GDC_PASSWORD", testParameters.getPassword())
                .addParameter("CLIENT_GDC_HOSTNAME", testParameters.getHost())
                .addParameter("CLIENT_GDC_PROTOCOL", "https")
                .addParameter("transfer_all", "true")
                .addSecureParameter("ads_password", testParameters.getPassword())
                .addParameter("gd_encoded_params",
                        new JSONObject() {{
                            put("ads_client", adsClient);
                            put("TOKENS", tokens);
                        }}.toString());
    }
}
