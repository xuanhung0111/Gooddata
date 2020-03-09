package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.google.common.collect.ImmutableMap;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class FreeGrowthDataset extends Dataset {

    private static final Map<String, String> CONNECT_TYPE_MAPPINGS
            = ImmutableMap.of("Amazon Redshift", "/admin/connect/#/datasource/create?ds_type=redshift",
            "Snowflake", "/admin/connect/#/datasource/create?ds_type=snowflake",
            "Google BigQuery", "/admin/connect/#/datasource/create?ds_type=bigQuery",
            "CSV", "#/projects/%s/upload"
    );

    @FindBy(css = ".buttonbar-section-inner .buttonbar-button-href")
    private List<WebElement> elements;

    public void verifyDatasetPage(String projectId) {
        Assert.assertEquals(elements.size(), 4);
        for (WebElement element : elements) {
            String text = element.getText().replaceAll("\\r?\\n", " ");
            String urlFormat = CONNECT_TYPE_MAPPINGS.get(text);
            String expectedHref = String.format(urlFormat, projectId);
            Assert.assertTrue(element.getAttribute("href").endsWith(expectedHref), "href not correct for " + text);
        }
    }

    public static FreeGrowthDataset getInstance(SearchContext context) {
        return Graphene.createPageFragment(FreeGrowthDataset.class, waitForElementVisible(By.className("gdc-data-content"), context));
    }
}
