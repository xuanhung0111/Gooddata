package com.gooddata.qa.utils.lcm;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import org.json.JSONObject;

import java.util.function.Supplier;

public final class ProvisionProcess extends LcmProcess {

    private String adsUri;
    private TestParameters testParameters = TestParameters.getInstance();

    /**
     * Init a relase process which expected to run on old executor
     */
    ProvisionProcess(final String adsUri, final String projectId) {
        super(projectId, Brick.ofProvisionBrick(TestParameters.getInstance().getBrickAppstore()));
        this.adsUri = adsUri;
        this.defaultParameters = buildDefaultParams();
    }

    /**
     * Init a relase process which expected to run on k8s executor
     */
    ProvisionProcess(final String adsUri, final String projectId, final String lcmProcessName) {
        super(projectId, lcmProcessName);
        this.adsUri = adsUri;
        this.defaultParameters = buildDefaultParams();
    }

    @Override
    protected void createLcmProcess() {
        dataloadProcess = restClient.getProcessService().createProcess(getProject(),
                LcmDataloadProcess.createProvisionProcess(this.name, testParameters.getLcmDataloadProcessComponentVersion()));
    }

    private Supplier<Parameters> buildDefaultParams() {
        final JSONObject adsClient = new JSONObject() {{
            put("username", testParameters.getDomainUser());
            put("password", "${ads_password}");
            put("jdbc_url", adsUri);
        }};

        return () -> new Parameters()
                .addParameter("organization", testParameters.getUserDomain())
                .addParameter("DATA_PRODUCT", LcmRestUtils.ATT_LCM_DATA_PRODUCT)
                .addParameter("GDC_USERNAME", testParameters.getDomainUser())
                .addSecureParameter("GDC_PASSWORD", testParameters.getPassword())
                .addParameter("CLIENT_GDC_HOSTNAME", testParameters.getHost())
                .addParameter("CLIENT_GDC_PROTOCOL", "https")
                .addSecureParameter("ads_password", testParameters.getPassword())
                .addSecureParameter("GDC_PASSWORD", testParameters.getPassword())
                .addParameter("client_id_column", "client_id")
                .addParameter("project_title_column", "project_title")
                .addParameter("project_token_column", "project_token")
                .addParameter("project_id_column", "project_id")
                .addParameter("segment_id_column", "segment_id")
                .addParameter("gd_encoded_params",
                        new JSONObject() {{
                            put("ads_client", adsClient);
                        }}.toString());
    }
}
