package com.gooddata.qa.graphene.indigo.analyze;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.utils.http.RestUtils;

public class GoodSalesAccessDataSectionTest extends AnalyticalDesignerAbstractTest {

    private static final String DATA_LINK_BUBBLE_MESSAGE = "Upload a CSV file to add new\ndata for analyzing.";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Access-Data-Section-Test";
    }

    @Test(dependsOnMethods = {"cannotAccessDataSectionIfNotEnableFlag"})
    public void accessDataSection() throws IOException, JSONException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER);

        try {
            initAnalysePage();
            final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();
            assertTrue(cataloguePanel.isAddDataLinkVisible());
            assertEquals(cataloguePanel.getDataLinkBubbleMessage(), DATA_LINK_BUBBLE_MESSAGE);
            cataloguePanel.goToDataSectionPage();

            BrowserUtils.switchToLastTab(browser);
            assertEquals(browser.getCurrentUrl(),
                    getRootUrl() + format(CSV_UPLOADER_PROJECT_ROOT_TEMPLATE + "/upload", testParams.getProjectId()));
        } finally {
            RestUtils.disableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                    ProjectFeatureFlags.ENABLE_CSV_UPLOADER);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void cannotAccessDataSectionIfNotEnableFlag() throws IOException, JSONException {
        // make sure csv uploader flag is disabled
        RestUtils.disableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(), 
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER);

        initAnalysePage();
        assertFalse(analysisPage.getCataloguePanel().isAddDataLinkVisible());
    }
}
