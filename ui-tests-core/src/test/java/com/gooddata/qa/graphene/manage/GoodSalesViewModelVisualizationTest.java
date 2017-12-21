package com.gooddata.qa.graphene.manage;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.models.GraphModel;
import com.google.common.base.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.IMAGES;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class GoodSalesViewModelVisualizationTest extends GoodSalesAbstractTest {

    private static final String MODEL_URI = "/gdc/projects/%s/ldm";
    private static final String MODEL_IMAGE_FILE = "ldm.svg";
    private static final String MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE = "ldmAfterChangeAccountToAcsount.svg";
    
    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"createProject"})
    public void checkLDMImageTest() throws IOException, JSONException {
        File tmpImage = getLDMImageFromGrayPage();
        GraphModel expectedGraph = GraphModel.readGraphXPath(getResourceAsFile("/" + IMAGES + "/" + MODEL_IMAGE_FILE));
        GraphModel actualGraph = GraphModel.readGraphXPath(tmpImage);

        assertEquals(actualGraph.getNodes(), expectedGraph.getNodes(),
                "actual nodes in graph do not match expected nodes");
        assertEquals(actualGraph.getEdges(), expectedGraph.getEdges(),
                "actual edges in graph do not match expected edges");
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnMethods = {"checkLDMImageTest"})
    public void checkLDMImageAfterChangeAttributeNameTest() throws ParseException, IOException, JSONException {
        changeAttributeName("Account", "Acsount");
        File tmpImage = getLDMImageFromGrayPage();
        try {

            GraphModel expectedGraph = GraphModel.readGraphXPath(getResourceAsFile("/" + IMAGES + "/"
                    + MODEL_IMAGE_WITH_ATTRIBUTE_ACCOUNT_CHANGED_FILE));
            GraphModel actualGraph = GraphModel.readGraphXPath(tmpImage);

            assertEquals(actualGraph.getNodes(), expectedGraph.getNodes());
            assertEquals(actualGraph.getEdges(), expectedGraph.getEdges());
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
}
