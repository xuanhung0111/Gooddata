package com.gooddata.qa.graphene.manage;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.IMAGES;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static com.gooddata.md.Restriction.title;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.google.common.base.Predicate;

public class GoodSalesViewModelVisualizationTest extends GoodSalesAbstractTest {

    private static final String MODEL_URI = "/gdc/projects/%s/ldm";
    private static final String MODEL_IMAGE_FILE = "ldm.svg";
    private static final String MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE = "ldmAfterChangeAccountToAcsount.svg";
    
    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"createProject"})
    public void checkLDMImageTest() throws IOException, JSONException {
        File tmpImage = getLDMImageFromGrayPage();
        String hostname = testParams.isHostProxy()? testParams.getHostProxy(): testParams.getHost();
        replaceContentInSVGFile(tmpImage, Pair.of(hostname, HOST_NAME), 
                Pair.of(testParams.getProjectId(), PROJECT_ID));
        assertTrue(compareTwoFile(getResourceAsFile("/" + IMAGES + "/" + MODEL_IMAGE_FILE), tmpImage));
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"checkLDMImageTest"})
    public void checkLDMImageAfterChangeAttributeNameTest() throws ParseException, IOException, JSONException {
        changeAttributeName("Account", "Acsount");
        File tmpImage = getLDMImageFromGrayPage();
        String hostname = testParams.isHostProxy()? testParams.getHostProxy(): testParams.getHost();
        replaceContentInSVGFile(tmpImage, Pair.of(hostname, HOST_NAME), 
                Pair.of(testParams.getProjectId(), PROJECT_ID));
        try {
            assertTrue(compareTwoFile(getResourceAsFile("/" + IMAGES + "/"
                    + MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE), tmpImage));
        } finally {
            changeAttributeName("Acsount", "Account");
        }
    }

    private void changeAttributeName(String attributeName, String newName) {
        initAttributePage().renameAttribute(attributeName, newName);

        Predicate<WebDriver> nameChanged = browser ->
                !getMdService().find(getProject(), Attribute.class, title(newName)).isEmpty();
        Graphene.waitGui().until(nameChanged);
    }

    private File getLDMImageFromGrayPage() throws IOException, ParseException, JSONException {
        final URL url = new URL(getJsonObject(getRestApiClient(), format(MODEL_URI, testParams.getProjectId()))
            .getString("uri"));
        final File image = new File(testParams.loadProperty("user.home"), MODEL_IMAGE_FILE);
        FileUtils.copyURLToFile(url, image);
        return image;
    }

    private boolean compareTwoFile(File file1, File file2) throws IOException {
        System.out.println("Length of the first image is " + file1.length());
        System.out.println("Length of the second image is " + file2.length());
        if (file1.length() != file2.length())
            return false;
        BufferedReader reader1 = new BufferedReader(new FileReader(file1));
        BufferedReader reader2 = new BufferedReader(new FileReader(file2));
        String line1 = null;
        String line2 = null;
        try {
            while (((line1 = reader1.readLine()) != null)
                    && ((line2 = reader2.readLine()) != null)) {
                if (!line1.equals(line2))
                    return false;
            }
        } finally {
            reader1.close();
            reader2.close();
        }
        return true;
    }
}
