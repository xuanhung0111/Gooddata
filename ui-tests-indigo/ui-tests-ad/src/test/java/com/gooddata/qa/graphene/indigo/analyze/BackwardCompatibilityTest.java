package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;

public class BackwardCompatibilityTest extends AbstractProjectTest {

    private static final String STAGING3 = "staging3.intgdc.com";
    private static final String STAGING2 = "staging2.intgdc.com";
    private static final String STAGING = "staging.intgdc.com";

    @Override
    public void initProperties() {
        super.initProperties();
        log.info("these tests are meaningful when performing testing on created project");
        testParams.setReuseProject(true);
        switch(testParams.getHost()) {
            case STAGING3:
                testParams.setProjectId("kf8tobvrdszda3xocsptnjdjf7xxyexs");
                return;
            case STAGING2:
                testParams.setProjectId("u7yus6202jxbsbjl5ijba87x00n0swmf");
                return;
            case STAGING:
                testParams.setProjectId("nqixedz4xxwsy461sghtyy9grz084288");
                return;
            default:
                System.out.println("Test just runs on staging, staging2 and staging3");
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testWithPoP() {
        if (!isOnStagingCluster()) {
            return;
        }
        final String insightUri = format("analyze/#/%s/80514/edit", testParams.getProjectId());
        final String avgAmount = "Amount Tr [Avg]";
        final String avgAmountAgo = "Amount Tr [Avg] - SP year ago";
        openUrl(insightUri);
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(avgAmountAgo, avgAmount));
        analysisPage.waitForReportComputing();
        assertThat(analysisPage.getTableReport().getHeaders(), hasItems(avgAmountAgo, avgAmount));
    }

    private boolean isOnStagingCluster() {
        if (testParams.getHost().equals(STAGING3) || testParams.getHost().equals(STAGING2) ||
                testParams.getHost().equals(STAGING)) {
            return true;
        }
        System.out.println("Test just runs on Staging, Staging2 and Staging3. Skip and mark as passed test");
        return false;
    }
}
