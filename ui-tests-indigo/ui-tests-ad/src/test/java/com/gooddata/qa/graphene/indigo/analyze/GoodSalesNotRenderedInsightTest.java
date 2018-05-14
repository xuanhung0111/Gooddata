package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesNotRenderedInsightTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
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

    @Test(dependsOnGroups = { "createProject" }, dataProvider = "chartTypeDataProvider")
    public void testNotSavableMissingMetricInsight(ReportType type) {
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).changeReportType(type);
        //checking empty Measures bucket is not a good way to verify insight containing no metric
        //because user still can add attribute to generate aggregation metric, it's a valid case to save insight
        assertEquals(analysisPage.getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT",
                "Missing metric message was not displayed");
        assertFalse(analysisPage.isSaveInsightEnabled(), "The save button is enabled");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void saveMissingMetricTableInsight() throws JSONException, IOException {
        final String insight = "Test-Save-Table-Insight-Missing-Metric";
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getTableReport().getHeaders(), singletonList(ATTR_ACTIVITY_TYPE),
                "Table was not displayed sucessfully");
        analysisPage.saveInsight(insight);
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        assertTrue(indigoRestRequest.getAllInsightNames().contains(insight),
                insight + " was not saved ");
    }
}
