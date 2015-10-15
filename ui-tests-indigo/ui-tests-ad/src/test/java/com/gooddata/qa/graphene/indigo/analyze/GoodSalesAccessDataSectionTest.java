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
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

public class GoodSalesAccessDataSectionTest extends AnalyticalDesignerAbstractTest {

    private static final String DATA_LINK_BUBBLE_MESSAGE = "Upload a CSV file to add new\ndata for analyzing.";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Access-Data-Section-Test";
    }

    @Test(dependsOnMethods = {"cannotAccessDataSectionIfNotEnableFlag"})
    public void accessDataSection() throws IOException, JSONException {
        RestUtils.setFeatureFlags(getRestApiClient(), FeatureFlagOption.createFeatureClassOption(
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER.getFlagName(), true));

        initAnalysePage();
        assertTrue(analysisPage.isAddDataLinkVisible());
        assertEquals(analysisPage.getDataLinkBubbleMessage(), DATA_LINK_BUBBLE_MESSAGE);
        analysisPage.goToDataSectionPage();

        BrowserUtils.switchToLastTab(browser);
        assertEquals(browser.getCurrentUrl(),
                getRootUrl() + format(DATA_UPLOAD_PAGE_URI_TEMPLATE, testParams.getProjectId()));
    }

    @Test(dependsOnGroups = {"init"})
    public void cannotAccessDataSectionIfNotEnableFlag() {
        initAnalysePage();
        assertFalse(analysisPage.isAddDataLinkVisible());
    }
}
