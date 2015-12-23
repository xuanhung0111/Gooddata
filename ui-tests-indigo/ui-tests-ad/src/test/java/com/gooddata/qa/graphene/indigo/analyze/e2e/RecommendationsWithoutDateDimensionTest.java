package com.gooddata.qa.graphene.indigo.analyze.e2e;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class RecommendationsWithoutDateDimensionTest extends AbstractAdE2ETest {

    private static final String MAQL_PATH = "/customer/customer.maql";
    private static final String UPLOADINFO_PATH = "/customer/upload_info.json";
    private static final String CSV_PATH = "/customer/customer.csv";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Recommendations-Without-Date-Dimension-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupProject"})
    public void setupProject() throws JSONException, IOException, URISyntaxException {
        if (testParams.isReuseProject()) {
            log.info("No need to setup data in reuse project.");
            return;
        }

        setupMaql(MAQL_PATH);
        setupData(CSV_PATH, UPLOADINFO_PATH);
    }

    @Test(dependsOnGroups = {"init"})
    public void trending_recommendation_should_not_be_visible() {
        visitEditor();

        expectMissing(DATE);
        dragFromCatalogue(".s-id-fact_customer_amount", METRICS_BUCKET);
        expectFind(".s-recommendation-comparison");
        expectMissing(".s-recommendation-trending");
    }

    @Test(dependsOnGroups = {"init"})
    public void metric_with_period_recommendation_should_not_be_visible() {
        visitEditor();

        dragFromCatalogue(".s-id-fact_customer_amount", METRICS_BUCKET);
        dragFromCatalogue(".s-id-attr_customer_id", CATEGORIES_BUCKET);
        expectFind(".s-recommendation-contribution");
        expectMissing(".s-recommendation-comparison-with-period");
    }

    @Test(dependsOnGroups = {"init"})
    public void trending_shortcut_should_not_appear() {
        visitEditor();

        startDrag(".s-id-fact_customer_amount");

        try {
            expectFind(".s-recommendation-metric-canvas");
            expectMissing(".s-recommendation-metric-over-time-canvas");
        } finally {
            stopDrag(new int[] {-1, -1});
        }
    }

    private void setupMaql(String maqlPath) throws JSONException, IOException {
        URL maqlResource = getClass().getResource(maqlPath);
        postMAQL(IOUtils.toString(maqlResource), 60);
    }

    private void setupData(String csvPath, String uploadInfoPath) throws JSONException, IOException, URISyntaxException {
        URL csvResource = getClass().getResource(csvPath);
        String webdavURL = uploadFileToWebDav(csvResource, null);

        URL uploadInfoResource = getClass().getResource(uploadInfoPath);
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()), 60);
    }
}
