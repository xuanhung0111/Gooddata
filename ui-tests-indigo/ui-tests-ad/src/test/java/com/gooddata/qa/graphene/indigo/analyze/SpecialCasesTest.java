package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class SpecialCasesTest extends AnalyticalDesignerAbstractTest {

    private static final String MANY_COLUMNS_CSV_PATH = "/" + UPLOAD_CSV + "/many_columns.csv";
    private static final String MANY_CLOUMNS_DATASET = "Many Columns";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "";
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-Special-Cases-Test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupProject"})
    public void uploadDatasets() {
        uploadCSV(getFilePathFromResource(MANY_COLUMNS_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + MANY_CLOUMNS_DATASET +"-dataset", getClass());
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeLimitationInTableReport() {
        initAnalysePage();

        analysisPage.getCataloguePanel().changeDataset(MANY_CLOUMNS_DATASET)
            .filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPage.changeReportType(ReportType.TABLE);
        Stream.of(ACCOUNT, DEPARTMENT, "Forecast Category", "Is Active", "Is Closed", "Is Won",
                "Opportunity", PRODUCT, "Region", "Stag 1", "Stag 2", "Stag 3", "Stag 4", "Stag 5",
                "Stag 6", "Stag 7", "Stag 8", "Stag 9", "Stage Name", "Status")
                .forEach(analysisPage::addAttribute);
        takeScreenshot(browser, "testAttributeLimitationInTableReport-finishAdding20Attributes", getClass());

        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 20);
    }
}
