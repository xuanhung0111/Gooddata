package com.gooddata.qa.utils.lcm;

import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import org.json.JSONObject;

import java.util.function.Supplier;

public final class RolloutProcess extends RubyProcess {
    private TestParameters testParameters;
    private String adsUri;

    RolloutProcess(final TestParameters testParameters, final String adsUri, final String projectId) {
        super(new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(), testParameters.getPassword(),
                        true)), projectId, Brick.ofRolloutBrick());
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
        final JSONObject tokens = new JSONObject() {{
            put("pg", testParameters.getAuthorizationToken());
            put("vertica", testParameters.getAuthorizationToken());
        }};
        return () -> new Parameters()
                .addParameter("organization", "default")
                .addParameter("GDC_USERNAME", testParameters.getDomainUser())
                .addSecureParameter("GDC_PASSWORD", testParameters.getPassword())
                .addParameter("CLIENT_GDC_HOSTNAME", testParameters.getHost())
                .addParameter("CLIENT_GDC_PROTOCOL", "https")
                .addSecureParameter("ads_password", testParameters.getPassword())
                .addSecureParameter("GDC_PASSWORD", testParameters.getPassword())
                .addParameter("gd_encoded_params",
                        new JSONObject() {{
                            put("ads_client", adsClient);
                            put("TOKENS", tokens);
                        }}.toString());
    }
}
