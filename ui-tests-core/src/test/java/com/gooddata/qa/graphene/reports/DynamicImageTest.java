package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.utils.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.io.ResourceUtils;

public class DynamicImageTest extends AbstractProjectTest {

    private final static String IMAGE = "Image";
    private final static String NAME = "Name";
    private final static String ERROR_IMAGE = "/images/2011/il-no-image.png";
    private final static String ID_METRIC = "Id-Metric";

    private final static String IMAGE_SOURCE_1 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing/images/publicImage.png";
    private final static String IMAGE_SOURCE_2 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing/images/publicImage2.png";
    private final static String IMAGE_SOURCE_3 =
            "source=web&url=https://s3.amazonaws.com/gdc-testing/images/publicImage3.png";

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
        reportPage.getTableReport().getImageElements().get(0).click();
        reportPage.waitForReportExecutionProgress();
        takeScreenshot(browser, "drill-on-an-image", getClass());

        assertTrue(isEqualCollection(reportPage.getTableReport().getAttributeElements(),
                asList("Image 1", "Image 2", "Image 3")));

        howItem.setPosition(Position.TOP);

        browser.navigate().back();
        reportPage.waitForReportExecutionProgress()
                .openHowPanel()
                .selectAttributePosition(howItem)
                .doneSndPanel()
                .waitForReportExecutionProgress();

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

    private boolean isImageDisplayed(WebElement image) {
        //have 3 scenarios when loading image
        //1.Image is loaded successfully
        //      src attribute = https://api.monosnap.com/image/download?id=987dH7kckbVcKcv8UFK5jdoKXCJ8wk
        //2.An alternative img is loaded when browser encounters error
        //      src attribute = 
        //                  /gdc/app/projects/s03a3h7ru03dli7iq3o4xk4fl2xlgqmn/images?source=http:
        //                  //samsuria.com/wp-content/uploads/2014/10/wallpaper-nature-3d.jpg
        //                  &errorPage=/images/2011/il-no-image.png
        //3.A broken image symbol is displayed
        //      src attribute = /gdc/app/projects/s03a3h7ru03dli7iq3o4xk4fl2xlgqmn/images?source=web&url
        //                  =http://hdwallweb.com/wp-content/uploads/2014/10/01/nature-home-drawing.jpg&errorPage=/images
        //                  /2011/il-no-image.png
        //
        //Generally, calling restAPI with src attribute only handles case 2&3 by checking body response
        //due to src attribute differences.
        //Using JS to cover 3 above cases. To check an image is displayed completely, JS code works as below
        //1.wait for image is loaded
        //2.image's width must be defined and > 0

        final String js = "return arguments[0].complete && " 
                + "typeof arguments[0].naturalWidth != \"undefined\" && "
                + "arguments[0].naturalWidth > 0";

        return (Boolean) BrowserUtils.runScript(browser, js, image);
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
        assertTrue(isImageDisplayed(table.getImageElement(IMAGE_SOURCE_1)), "The image1 is not loaded");
        assertTrue(isImageDisplayed(table.getImageElement(IMAGE_SOURCE_2)), "The image2 is not loaded");

        assertTrue(
                isImageDisplayed(table.getImageElement(IMAGE_SOURCE_3))
                        && table.getImageElement(IMAGE_SOURCE_3).getAttribute("src").contains(ERROR_IMAGE),
                "The expected image is not displayed");
    }

}