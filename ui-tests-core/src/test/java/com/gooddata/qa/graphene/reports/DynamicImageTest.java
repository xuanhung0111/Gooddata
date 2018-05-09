package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.testng.annotations.Test;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

public class DynamicImageTest extends AbstractProjectTest {

    private final static String IMAGE = "Image";
    private final static String NAME = "Name";
    private final static String ID_METRIC = "Id-Metric";

    private final static String IMAGE_SOURCE_1 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage.png";
    private final static String IMAGE_SOURCE_2 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage2.png";
    private final static String IMAGE_SOURCE_3 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing-public/images/publicImage3.png";

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.DYNAMIC_IMAGES + "/image_url.csv"));
        takeScreenshot(browser, "uploaded-image-file", getClass());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testImageFromPubliclyAccessibleImages() {
        initAttributePage().initAttribute(IMAGE)
                .setDrillToAttribute(NAME)
                .selectLabelType(IMAGE);

        final HowItem howItem = new HowItem(IMAGE, Position.LEFT);

        initReportCreation().createReport(new UiReportDefinition()
                .withName("Report-Containing-Images")
                .withHows(howItem));

        takeScreenshot(browser, "report-containing-images", getClass());
        checkAllImagesInReport();

        //image's height is much smaller than cell's, so graphene probably missclick.
        //click on img element to drill instead
        reportPage.getTableReport().drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
        reportPage.waitForReportExecutionProgress();
        takeScreenshot(browser, "drill-on-an-image", getClass());

        assertTrue(isEqualCollection(reportPage.getTableReport().getAttributeValues(),
                asList("Image 1", "Image 2", "Image 3")));

        howItem.setPosition(Position.TOP);

        browser.navigate().back();
        reportPage.waitForReportExecutionProgress()
                .openHowPanel()
                .selectAttributePosition(howItem.getAttribute().getName(), howItem.getPosition())
                .done();
        reportPage.waitForReportExecutionProgress();

        takeScreenshot(browser, "images-having-top-position", getClass());
        checkAllImagesInReport();
    }

    @Test(dependsOnMethods = {"testImageFromPubliclyAccessibleImages"})
    public void createReportWithImageInVariousPositions() {
        final String idUri = getMdService()
                .getObjUri(getProject(), Fact.class, title("Id"));

        getMdService().createObj(getProject(),
                new Metric(ID_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", idUri)),
                        "#,##0.00"));

        final HowItem imageItem = new HowItem(IMAGE, Position.TOP);
        final HowItem nameItem = new HowItem(NAME, Position.TOP);

        //report contains images having inner top position
        createReportInVariousPositions("Inner-Top-Report", nameItem, imageItem);

        takeScreenshot(browser, "images-having-inner-top-position", getClass());
        checkAllImagesInReport();

        //report contains images having outer top position
        createReportInVariousPositions("Outer-Top-Report", imageItem, nameItem);

        takeScreenshot(browser, "images-having-outer-top-position", getClass());
        checkAllImagesInReport();

        imageItem.setPosition(Position.LEFT);
        nameItem.setPosition(Position.LEFT);

        //report contains images having inner left position
        createReportInVariousPositions("Inner-Left-Report", nameItem, imageItem);

        takeScreenshot(browser, "images-having-inner-left-position", getClass());
        checkAllImagesInReport();

        //report contains images having outer left position
        createReportInVariousPositions("Outer-Left-Report", imageItem, nameItem);

        takeScreenshot(browser, "images-having-outer-left-position", getClass());
        checkAllImagesInReport();
    }

    private void createReportInVariousPositions(final String reportName,
            final HowItem firstItem, final HowItem secondItem) {
        initReportCreation().createReport(
                new UiReportDefinition()
                        .withName(reportName)
                        .withWhats(ID_METRIC)
                        .withHows(firstItem)
                        .withHows(secondItem));
    }

    private void checkAllImagesInReport() {
        final TableReport table = reportPage.waitForReportExecutionProgress().getTableReport();

        // scr value is simplified and put into class attribute, so checking src is not necessary
        assertTrue(table.isImageDisplayed(IMAGE_SOURCE_1, CellType.ATTRIBUTE_VALUE), "The image1 is not loaded");
        assertTrue(table.isImageDisplayed(IMAGE_SOURCE_2, CellType.ATTRIBUTE_VALUE), "The image2 is not loaded");
        assertTrue(table.isErrorImageDisplayed(IMAGE_SOURCE_3, CellType.ATTRIBUTE_VALUE),
                "The expected image is not displayed");
    }
}
