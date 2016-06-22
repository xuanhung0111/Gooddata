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
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class SpecialCasesTest extends AbstractAnalyseTest {

    private static final String MANY_COLUMNS_CSV_PATH = "/" + UPLOAD_CSV + "/many_columns.csv";
    private static final String MANY_CLOUMNS_DATASET = "Many Columns";

    private static final String PRODUCT = "Product";
    private static final String ACCOUNT = "Account";
    private static final String DEPARTMENT = "Department";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Special-Cases-Test";
    }

    @Override
    public void prepareSetupProject() {
        uploadCSV(getFilePathFromResource(MANY_COLUMNS_CSV_PATH));
        takeScreenshot(browser, "uploaded-" + MANY_CLOUMNS_DATASET +"-dataset", getClass());
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeLimitationInTableReport() {
        analysisPageReact.getCataloguePanel().changeDataset(MANY_CLOUMNS_DATASET)
            .filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPageReact.changeReportType(ReportType.TABLE);
        Stream.of(ACCOUNT, DEPARTMENT, "Forecast Category", "Is Active", "Is Closed", "Is Won",
                "Opportunity", PRODUCT, "Region", "Stag 1", "Stag 2", "Stag 3", "Stag 4", "Stag 5",
                "Stag 6", "Stag 7", "Stag 8", "Stag 9", "Stage Name", "Status")
                .forEach(analysisPageReact::addAttribute);
        takeScreenshot(browser, "testAttributeLimitationInTableReport-finishAdding20Attributes", getClass());

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 20);
    }
}
