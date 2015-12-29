package com.gooddata.qa.graphene.indigo.analyze.e2e;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;
import com.gooddata.qa.utils.http.RestUtils;

public class CsvUploaderTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Csv-Uploader-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_visible_with_feature_flag_on() throws IOException, JSONException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER);

        try {
            visitEditor();

            expectFind(".csv-link-section .s-btn-add_data");
        } finally {
            RestUtils.disableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                    ProjectFeatureFlags.ENABLE_CSV_UPLOADER);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_hidden_with_feature_flag_off() throws IOException, JSONException {
        RestUtils.disableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER);

        visitEditor();

        expectMissing(".csv-link-section .s-btn-add_data");
    }
}
