package com.gooddata.qa.graphene.indigo.analyze.e2e;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

public class CsvUploaderTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Csv-Uploader-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_visible_with_feature_flag_on() throws IOException, JSONException {
        setFeatureFlag(true);

        visitEditor();

        expectFind(".csv-link-section .s-btn-add_data");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_hidden_with_feature_flag_off() throws IOException, JSONException {
        setFeatureFlag(false);

        visitEditor();

        expectMissing(".csv-link-section .s-btn-add_data");
    }

    private void setFeatureFlag(boolean on) throws IOException, JSONException {
        RestUtils.setFeatureFlags(getRestApiClient(), FeatureFlagOption.createFeatureClassOption(
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER.getFlagName(), on));
    }
}
