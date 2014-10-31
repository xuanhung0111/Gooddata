package com.gooddata.qa.graphene.manage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

@Test(groups = {"viewModelVisualization"}, description = "Test view model visualization")
public class GoodSalesViewModelVisualizationTest extends GoodSalesAbstractTest {

    private static final String URI                                             =    "/gdc/projects/%s/ldm";
    private static final String MODEL_IMAGE_FILE                                =    "ldm.png";
    private static final String MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE =    "ldmAfterChangeAccountToAcsount.png";

    @Test(dependsOnMethods = {"createProject"}, groups = {"viewModel"})
    public void checkLDMImageTest() throws InterruptedException, IOException, JSONException {
        File tmpImage = getLDMImageFromGrayPage(URI);
        try {
            double diffPercent = getDiffPercentBetweenTwoImages(new File(testParams.loadProperty("imageFilePath"),
                                                                         MODEL_IMAGE_FILE),
                                                                tmpImage);
            System.out.println("Diff percent between 2 images is " + diffPercent + " %.");
            assertTrue(diffPercent < 0.001);
        } finally {
            tmpImage.delete();
        }
    }

    @Test(dependsOnMethods = {"checkLDMImageTest"}, groups = {"viewModel"})
    public void checkLDMImageAfterChangeAttributeNameTest() throws ParseException, IOException, JSONException {
        changeAttributeNameTest("Account", "Acsount");

        File tmpImage = getLDMImageFromGrayPage(URI);
        try {
            double diffPercent = getDiffPercentBetweenTwoImages(new File(testParams.loadProperty("imageFilePath"),
                                                                         MODEL_IMAGE_FILE),
                                                                tmpImage);
            System.out.println("Diff percent between 2 images is " + diffPercent + " %.");
            assertFalse(diffPercent < 0.001);

            diffPercent = getDiffPercentBetweenTwoImages(new File(testParams.loadProperty("imageFilePath"),
                                                                  MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE),
                                                         tmpImage);
            System.out.println("Diff percent between 2 images is " + diffPercent + " %.");
            assertTrue(diffPercent < 0.001);
        } finally {
            tmpImage.delete();
            changeAttributeNameTest("Acsount", "Account");
        }
    }

    @Test(dependsOnGroups = {"viewModel"}, groups = {"tests"})
    public void teardown() {
        successfulTest = true;
    }
    

    private void changeAttributeNameTest(String attributeName, String newName) {
        initAttributePage();
        attributePage.renameAttribute(attributeName, newName);
    }

    private File getLDMImageFromGrayPage(String uri) throws IOException, ParseException, JSONException {
        getRestApiClient();

        HttpRequestBase getRequest = restApiClient.newGetMethod(String.format(uri, testParams.getProjectId()));
        HttpResponse getResponse = restApiClient.execute(getRequest);
        assertEquals(getResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");

        URL url = new URL(new JSONObject(EntityUtils.toString(getResponse.getEntity()))
                                        .get("uri").toString());
        File image = new File(testParams.loadProperty("java.io.tmpdir") + MODEL_IMAGE_FILE);
        FileUtils.copyURLToFile(url, image);

        return image;
    }

    /**
     * @see http://rosettacode.org/wiki/Percentage_difference_between_images#Java
     */
    private double getDiffPercentBetweenTwoImages(File file1, File file2) throws IOException {
        System.out.println("Length of the first image is " + file1.length());
        System.out.println("Length of the second image is " + file2.length());
        if (file1.length() != file2.length()) return 100D;

        BufferedImage image1 = ImageIO.read(file1);
        BufferedImage image2 = ImageIO.read(file2);

        if (!doTwoImagesHaveSameSize(image1, image2)) {
            System.out.println("Images dimensions mismatch");
            return 100D;
        }

        long diff = getDiffNumberFromTwoImages(image1, image2);
        return calculateDiffPercentBetweenTwoImages(diff, image1.getWidth() * image1.getHeight() * 3);
    }

    private double calculateDiffPercentBetweenTwoImages(double diffNumber, double totalPixels) {
        return (diffNumber / totalPixels / 255.0) * 100.0;
    }

    private boolean doTwoImagesHaveSameSize(BufferedImage image1, BufferedImage image2) {
        return image1.getWidth() == image2.getWidth() && image1.getHeight() == image2.getHeight();
    }

    private long getDiffNumberFromTwoImages(BufferedImage image1, BufferedImage image2) {
        long diff = 0;
        int rgb1, rgb2, r1, r2, b1, b2, g1, g2;

        for (int i = 0, n = image1.getWidth(); i < n; i++) {
            for (int j = 0, m = image1.getHeight(); j < m; j++) {
                rgb1 = image1.getRGB(i, j);
                rgb2 = image2.getRGB(i, j);

                r1 = (rgb1 >> 16) & 0xff;
                g1 = (rgb1 >>  8) & 0xff;
                b1 = (rgb1      ) & 0xff;
                r2 = (rgb2 >> 16) & 0xff;
                g2 = (rgb2 >>  8) & 0xff;
                b2 = (rgb2      ) & 0xff;

                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
          }
        }

        return diff;
    }
}
