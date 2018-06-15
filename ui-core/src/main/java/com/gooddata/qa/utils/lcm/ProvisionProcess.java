package com.gooddata.qa.utils.lcm;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.json.JSONObject;

import java.util.function.Supplier;

public final class ProvisionProcess extends RubyProcess {
    private TestParameters testParameters;
    private String adsUri;

    ProvisionProcess(final TestParameters testParameters, final String adsUri, final String projectId) {
        super(new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(), testParameters.getPassword(),
                        true)), projectId, Brick.ofProvisionBrick());
        this.testParameters = testParameters;
        this.adsUri = adsUri;
        this.defaultParameters = buildDefaultParams();
    }

    private Supplier<Parameters> buildDefaultParams() {
        final JSONObject adsClient = new JSONObject() {{
            put("username", testParameters.getDomainUser());
            put("password", "${ads_password}");
            put("jdbc_url", adsUri);
        }};

        return () -> new Parameters()
                .addParameter("organization", "default")
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
