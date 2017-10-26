package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class RecommendationsWithoutDateDimensionTest extends AbstractAdE2ETest {

    private static final String MAQL_PATH = "/customer/customer.maql";
    private static final String UPLOADINFO_PATH = "/customer/upload_info.json";
    private static final String CSV_PATH = "/customer/customer.csv";

    @Override
    public void initProperties() {
        // create empty project and customized data
        projectTitle = "Recommendations-Without-Date-Dimension-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        setupMaql(LdmModel.loadFromFile(MAQL_PATH));
        setupData(CSV_PATH, UPLOADINFO_PATH);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void trending_recommendation_should_not_be_visible() {
        assertFalse(analysisPage.getCataloguePanel().getFieldNamesInViewPort().contains(DATE));
        analysisPage.addMetric(METRIC_AMOUNT, FieldType.FACT)
            .waitForReportComputing();
//        enable with CL-9443
//        assertTrue(isElementPresent(cssSelector(".s-recommendation-comparison"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-trending"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void metric_with_period_recommendation_should_not_be_visible() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .addAttribute("id")
            .waitForReportComputing();
        assertTrue(isElementPresent(cssSelector(".s-recommendation-contribution"), browser));
        assertFalse(isElementPresent(cssSelector(".s-recommendation-comparison-with-period"), browser));
    }
}
