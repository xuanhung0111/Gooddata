package com.gooddata.qa.graphene.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.report.UiReportDefinition;

public class CsvUploaderSanityTest extends AbstractCsvUploaderTest {

    @Test(dependsOnGroups = {"createProject"})
    public void checkCsvUploadHappyPath() {
        assertTrue(uploadCsv(PAYROLL)
            .getStatus()
            .matches(SUCCESSFUL_STATUS_MESSAGE_REGEX));
    }

    @Test(dependsOnMethods = {"checkCsvUploadHappyPath"})
    public void verifyDataAfterUpload() {
        createReport(new UiReportDefinition().withName("Report").withHows("Education"), "Education Report");
        assertTrue(isEqualCollection(waitForFragmentVisible(reportPage).getTableReport().getAttributeValues(),
                asList("Bachelors Degree", "Graduate Degree", "High School Degree", "Partial College",
                        "Partial High School")));
    }
}
