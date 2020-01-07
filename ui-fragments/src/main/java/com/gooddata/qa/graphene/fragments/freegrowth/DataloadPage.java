package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewPage;
import com.gooddata.qa.graphene.fragments.csvuploader.FileUploadDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataloadPage extends AbstractFragment {

    @FindBy(css = ".gd-header")
    private WorkspaceHeader workspaceHeader;

    @FindBy(css = ".splashscreen-section.splashscreen-line-aws")
    private LoadComponent aws;

    @FindBy(css = ".splashscreen-section.splashscreen-line-snowflake")
    private LoadComponent snowflake;

    @FindBy(css = ".splashscreen-section.splashscreen-line-bigquery")
    private LoadComponent bigquery;

    @FindBy(css = ".splashscreen-section.splashscreen-line-csv")
    private LoadComponent csv;

    public void verifyDataloadPage() {
        String text = aws.getHeadlineText().replaceAll("\\r?\\n", " ");
        Assert.assertEquals(text, "Amazon Redshift");

        text = snowflake.getHeadlineText().replaceAll("\\r?\\n", " ");
        Assert.assertEquals(text, "Snowflake");

        text = bigquery.getHeadlineText().replaceAll("\\r?\\n", " ");
        Assert.assertEquals(text, "Google BigQuery");

        text = csv.getHeadlineText().replaceAll("\\r?\\n", " ");
        Assert.assertEquals(text, "CSV");

        workspaceHeader.verifyWorkspaceHeader();
        workspaceHeader.verifyLoadMenuActive();
    }

    public static DataloadPage getInstance(SearchContext context) {
        DataloadPage dataloadPage = Graphene.createPageFragment(DataloadPage.class,
                waitForElementVisible(className("gdc-data-content"), context));
        dataloadPage.verifyDataloadPage();
        return dataloadPage;
    }

    public DataPreviewPage uploadFile(CsvFile csvFile) {
        csv.getConnectLink().click();
        FileUploadDialog.getInstane(browser)
                .pickCsvFile(csvFile.getFilePath())
                .clickUploadButton();
        return DataPreviewPage.getInstance(browser);
    }
}
