package com.gooddata.qa.graphene.manage;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.greypages.sfdccredentials.ConfigureSFDCCredentials;
import com.gooddata.qa.graphene.fragments.reports.ReportWithImage;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns.OptionDataType;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"projectSimpleAttribute"}, description = "Tests for configuration of attribute labels functionality on simple project in GD platform")
public class AttributeLabelsTest extends AbstractProjectTest {

    private String csvFilePath;
    private String hyperlinkAttr;
    private String hyperlinkReport;

    @FindBy(tagName = "form")
    protected ConfigureSFDCCredentials sfdc;

    @BeforeClass
    public void setProjectTitle() {
        csvFilePath = testParams.loadProperty("csvFilePath") + testParams.getFolderSeparator();
        projectTitle = "SimpleProject-test-attribute-labels";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() throws InterruptedException, JSONException {
        hyperlinkAttr = "Hyperlink";
        hyperlinkReport = "Hyperlink Report";
    }

    @Test(dependsOnMethods = {"initialize"})
    public void initDataTest() throws InterruptedException {
        Map<Integer, OptionDataType> columnIndexAndType = new HashMap<Integer, OptionDataType>();
        uploadCSV(csvFilePath + "attribute_labels.csv", columnIndexAndType, "attribute-labels");
    }

    @Test(dependsOnMethods = {"initialize"})
    public void setSFDCCredentialsTest() throws InterruptedException,
            JSONException {
        openUrl(PAGE_GDC_PROJECTS + "/" + testParams.getProjectId() + "/credentials/sfdc");
        waitForElementVisible(sfdc.getRoot());
        sfdc.setSFDCCredentials(testParams.loadProperty("sfdc.email"),
                testParams.loadProperty("sfdc.password") + testParams.loadProperty("sfdc.securityToken"));
    }

    @Test(dependsOnMethods = {"initDataTest"})
    public void changeAttributeToImageTest() throws InterruptedException {
        changeAttributeLabel("Image", AttributeLabelTypes.IMAGE);
        changeAttributeLabel("Image_SFDC", AttributeLabelTypes.IMAGE);
    }

    @Test(dependsOnMethods = {"changeAttributeToImageTest"})
    public void verifyReportWithImageTest() throws InterruptedException {
        initReport("Image Top 5");
        ReportWithImage report = Graphene.createPageFragment(
                ReportWithImage.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyImageOnReport();
    }

    @Test(dependsOnMethods = {"setSFDCCredentialsTest", "changeAttributeToImageTest"})
    public void verifyReportWithImageSFDCTest() throws InterruptedException {
        initReport("Image_SFDC Top 5");
        ReportWithImage report = Graphene.createPageFragment(
                ReportWithImage.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyIfImageSFDCOnReport();

    }

    @Test(dependsOnMethods = {"initDataTest"})
    public void changeAttributeToHyperlinkTest() throws InterruptedException {
        changeAttributeLabel(hyperlinkAttr, AttributeLabelTypes.HYPERLINK);
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        attributePage.verifyHyperLink(hyperlinkAttr);
    }

    @Test(dependsOnMethods = {"changeAttributeToHyperlinkTest"})
    public void configDrillToExternalPageTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        attributePage.configureDrillToExternalPage(hyperlinkAttr);
    }

    @Test(dependsOnMethods = {"configDrillToExternalPageTest"})
    public void createReportWithHyperlinkTest() throws InterruptedException {
        createReport(new ReportDefinition().withName(hyperlinkReport)
                                           .withWhats("Count of Image")
                                           .withHows(hyperlinkAttr), 
                     "Simple hyperlink report");
    }

    @Test(dependsOnMethods = {"createReportWithHyperlinkTest"})
    public void verifyReportWithHyperlinkTest() throws InterruptedException {
        initReport(hyperlinkReport);
        TableReport report = Graphene.createPageFragment(TableReport.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyAttributeIsHyperlinkInReport();
    }

    private void initReport(String reportName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage|");
        waitForReportsPageLoaded(browser);
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded(browser);
    }

    private void changeAttributeLabel(String attribute, AttributeLabelTypes label)
            throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        attributePage.configureAttributeLabel(attribute, label);
    }

}