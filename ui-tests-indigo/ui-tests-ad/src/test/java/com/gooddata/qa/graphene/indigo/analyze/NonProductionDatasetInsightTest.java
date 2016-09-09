package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class NonProductionDatasetInsightTest extends AbstractAnalyseTest {

    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";
    private static final String PAYROLL_DATASET = "Payroll";
    private static final String PRODUCTION_DATASET = "Production data";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "NonProductionDatasetInsightTest";
    }

    @Override
    public void prepareSetupProject() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + PAYROLL_DATASET + "-dataset", getClass());
    }

    @Test(dependsOnGroups = {"init"},
            description = "Graphene test for bug ONE-1464 Get error when opening viz belong to non-production dataset")
    public void openInsightContainingNonProductionDataset() {
        final String insight = "Open-Insight-Containing-Non-Production-Dataset-Test";
        final CataloguePanel panel = initAnalysePage().getCataloguePanel();
        panel.changeDataset(PAYROLL_DATASET);
        analysisPage.addMetric("Amount", FieldType.FACT)
                .addAttribute("Education")
                .waitForReportComputing()
                .saveInsight(insight);

        panel.changeDataset(PRODUCTION_DATASET);
        takeScreenshot(browser, "change-dataset-to-production", getClass());
        assertTrue(panel.isDatasetApplied(PRODUCTION_DATASET), PRODUCTION_DATASET + " has not been applied");

        browser.navigate().refresh();
        analysisPage.openInsight(insight).waitForReportComputing();
        takeScreenshot(browser, "open-insight-containing-non-production-dataset", getClass());
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 5,
                "The chart renders incorrectly");
    }

    @Test(dependsOnGroups = {"init"},
            description = "CL-9954 Unable to add filter for measure in existing viz that belongs to non-production")
    public void makeSureCanAddFilterForMeasure() {
        final String insight = "Cover bug CL-9954";
        final CataloguePanel panel = initAnalysePage().getCataloguePanel();
        panel.changeDataset(PAYROLL_DATASET);

        assertTrue(analysisPage.addMetric("Amount", FieldType.FACT)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of Amount")
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getAllAttributesInViewPort()
            .size() > 0);

        analysisPage.saveInsight(insight);
        panel.changeDataset(PRODUCTION_DATASET);

        browser.navigate().refresh();
        assertTrue(waitForFragmentVisible(analysisPage)
            .openInsight(insight)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration("Sum of Amount")
            .expandConfiguration()
            .clickAddAttributeFilter()
            .getAllAttributesInViewPort()
            .size() > 0);
    }
}
