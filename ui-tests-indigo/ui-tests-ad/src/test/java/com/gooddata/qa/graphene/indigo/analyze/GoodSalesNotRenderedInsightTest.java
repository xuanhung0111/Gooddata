package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesNotRenderedInsightTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Not-Rendered-Insight-Test";
    }

    @DataProvider(name = "chartTypeDataProvider")
    public Object[][] chartTypeDataProvider() {
        return new Object[][] {
                { ReportType.COLUMN_CHART },
                { ReportType.BAR_CHART },
                { ReportType.LINE_CHART }
        };
    }

    @Test(dependsOnGroups = { "init" }, dataProvider = "chartTypeDataProvider")
    public void testNotSavableMissingMetricInsight(ReportType type) {
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).changeReportType(type);
        //checking empty Measures bucket is not a good way to verify insight containing no metric
        //because user still can add attribute to generate aggregation metric, it's a valid case to save insight
        assertEquals(analysisPage.getExplorerMessage(), "Now select a measure to display",
                "Missing metric message was not displayed");
        assertFalse(analysisPage.getPageHeader().isSaveButtonEnabled(), "The save button is enabled");
    }

    @Test(dependsOnGroups = { "init" })
    public void saveMissingMetricTableInsight() throws JSONException, IOException {
        final String insight = "Test-Save-Table-Insight-Missing-Metric";
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getTableReport().getHeaders(), singletonList(ATTR_ACTIVITY_TYPE.toUpperCase()),
                "Table was not displayed sucessfully");
        analysisPage.saveInsight(insight);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(insight),
                insight + " was not saved ");
    }
}
