package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.ZoomValue;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ModelingFollowUpTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private ZoomMenu zoomMenu;
    private RestClient restClient;

    private final String USER_DATASET = "user";
    private final String CLASS_DATASET = "class";
    private final String USERNAME_ATTRIBUTE = "username";
    private final String AGE_FACT = "age";
    private final String USERID_PRIMARY_KEY = "userid";
    private final String USERID_PRIMARY_KEY_LABEL = "useridkeylabel";
    private final String USERCODE_FIRST_ATTRIBUTE = "usercodefirst";
    private final String CLASSNAME2_ATTRIBUTE = "classtotal";
    private final String CITY_DATASET = "city";
    private final String CITY_KEY_DATASET = "cityprimarykey";
    private List<String> LIST_ZOOM_VALUE = asList("Zoom To Fit", "25%", "50%", "75%", "100%", "125%");

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
    }

    @Test(dependsOnMethods = {"initTest"})
    public void initialPageTest() {
        canvas = modeler.getLayout().getCanvas();
        String textCanvas = modeler.getLayout().getTextBlankCanvas();
        assertThat(textCanvas, containsString("Get started"));
        assertThat(textCanvas, containsString("Drag items from the left panel to\n" +
                "canvas to build your model."));
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_model.txt"));

        assertEquals(ldmPage.getLinkDISC(), format("https://%s/admin/disc/#/projects", testParams.getHost()));
        assertTrue(ldmPage.getMenuItems().equals(Arrays.asList("Model data", "Load data")));
        initLogicalDataModelPage();
        waitForFragmentVisible( modeler.getLayout());
        modeler.getLayout().waitForLoading();
        zoomMenu = ZoomMenu.getInstance(browser);
    }

    @Test(dependsOnMethods = {"verifyZoomValue"})
    public void verifyZoomInAndZoomOut(){
        JointLayers modelerLayers = JointLayers.getInstance(browser);
        zoomMenu.clickZoomOutBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "125%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.ONR_HUNDRED_TWENTY_FIVE.getTransformLayer());
        zoomMenu.clickZoomOutBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "125%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.ONR_HUNDRED_TWENTY_FIVE.getTransformLayer());
        zoomMenu.clickZoomInBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "100%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.ONE_HUNDRED.getTransformLayer());
        zoomMenu.clickZoomInBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "75%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.SEVENTY_FIVE.getTransformLayer());
        zoomMenu.clickZoomInBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "50%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.FIFTY.getTransformLayer());
        zoomMenu.clickZoomInBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "25%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.TWENTY_FIVE.getTransformLayer());
        zoomMenu.clickZoomInBtn();
        assertEquals(zoomMenu.getCurrentZoomValue(), "25%");
        assertEquals(modelerLayers.getTransformLayer(), ZoomValue.TWENTY_FIVE.getTransformLayer());
        zoomMenu.clickZoomValueBtn().selectZoomValue("100%");
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void checkDefaultZoomValue(){
        assertEquals(zoomMenu.getCurrentZoomValue(), "100%");
    }

    @DataProvider(name = "listZoomValue")
    public Object[][] selectZoomValue() {
        return new Object[][] {
                {ZoomValue.TWENTY_FIVE},
                {ZoomValue.FIFTY},
                {ZoomValue.SEVENTY_FIVE},
                {ZoomValue.ONE_HUNDRED},
                {ZoomValue.ONR_HUNDRED_TWENTY_FIVE},
        };
    }

    @Test(dependsOnMethods = {"checkDefaultZoomValue"}, dataProvider = "listZoomValue")
    public void verifyZoomValue(ZoomValue zoomValue){
        JointLayers modelerLayers = JointLayers.getInstance(browser);
        assertEquals(zoomMenu.clickZoomValueBtn().getZoomValueList(), LIST_ZOOM_VALUE);
        zoomMenu.clickZoomValueBtn();
        zoomMenu.clickZoomValueBtn().selectZoomValue(zoomValue.getZoomValue());
        assertEquals(zoomMenu.getCurrentZoomValue(), zoomValue.getZoomValue());
        assertEquals(modelerLayers.getTransformLayer(), zoomValue.getTransformLayer());
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void deleteAllAttributeAndFactInDialog() {
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent.focusOnDataset(CLASS_DATASET);
        mainModelContent.addAttributeToDataset(USERNAME_ATTRIBUTE, CLASS_DATASET);
        mainModelContent.addAttributeToDataset(USERCODE_FIRST_ATTRIBUTE, CLASS_DATASET);
        mainModelContent.addFactToDataset(AGE_FACT, CLASS_DATASET);

        Model modelClass = mainModelContent.getModel(CLASS_DATASET);
        assertEquals(USERNAME_ATTRIBUTE, modelClass.getAttributeText(USERNAME_ATTRIBUTE));
        assertEquals(AGE_FACT, modelClass.getFactText(AGE_FACT));
        assertEquals(USERCODE_FIRST_ATTRIBUTE, modelClass.getAttributeText(USERCODE_FIRST_ATTRIBUTE));
        modelClass.openEditDialog();

        ViewDetailDialog viewDialog = ViewDetailDialog.getInstance(browser);
        assertTrue(viewDialog.isAttributeExist(USERNAME_ATTRIBUTE));
        assertTrue(viewDialog.isAttributeExist(USERCODE_FIRST_ATTRIBUTE));
        assertTrue(viewDialog.isFactExist(AGE_FACT));

        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        assertTrue(viewDialog.getNumberOfAttributes() > 0);
        assertTrue(viewDialog.getNumberOfFacts() > 0);

        dialog.clickOnAttributeInDataset(USERNAME_ATTRIBUTE, CLASS_DATASET);
        dialog.deleteAttributesOrFactInEditDatasetDialog(USERNAME_ATTRIBUTE);
        dialog.clickOnAttributeInDataset(USERCODE_FIRST_ATTRIBUTE, CLASS_DATASET);
        dialog.deleteAttributesOrFactInEditDatasetDialog(USERCODE_FIRST_ATTRIBUTE);
        dialog.clickOnFactInDataset(CLASSNAME2_ATTRIBUTE, CLASS_DATASET);
        dialog.deleteAttributesOrFactInEditDatasetDialog(CLASSNAME2_ATTRIBUTE);
        dialog.clickOnFactInDataset(AGE_FACT, CLASS_DATASET);
        dialog.deleteAttributesOrFactInEditDatasetDialog(AGE_FACT);

        assertFalse(viewDialog.isAttributeExist(USERNAME_ATTRIBUTE));
        assertFalse(viewDialog.isAttributeExist(USERCODE_FIRST_ATTRIBUTE));
        assertFalse(viewDialog.isAttributeExist(CLASSNAME2_ATTRIBUTE));
        assertFalse(viewDialog.isFactExist(AGE_FACT));

        assertTrue(viewDialog.getNumberOfAttributes() == 0);
        assertTrue(viewDialog.getNumberOfFacts() == 0);
        dialog.clickCancel();
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void verifyCannotDeletePrimaryLabel() throws NoSuchElementException {
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent.focusOnDataset(USER_DATASET);
        Model modelUser = mainModelContent.getModel(USER_DATASET);
        modelUser.addNewLabelForPrimaryKey(USERID_PRIMARY_KEY, USERID_PRIMARY_KEY_LABEL);
        mainModelContent.focusOnDataset(USER_DATASET);
        modelUser.openEditDialog();

        ViewDetailDialog viewDialog = ViewDetailDialog.getInstance(browser);
        assertTrue(viewDialog.isLabelAttributeExist(USERID_PRIMARY_KEY_LABEL));
        assertTrue(viewDialog.isLabelAttributeExist(USERID_PRIMARY_KEY));

        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        dialog.deleteLabelInDatasetDialog(USERID_PRIMARY_KEY_LABEL, USER_DATASET + "." + USERID_PRIMARY_KEY);
        assertFalse(viewDialog.isLabelAttributeExist(USERID_PRIMARY_KEY_LABEL));
        assertFalse(dialog.isDeleteButtonPresentOnLabel(USERID_PRIMARY_KEY, USER_DATASET));
        dialog.clickCancel();
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void verifyErrorMessagesInPrimaryKeyDialog() {
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(CITY_KEY_DATASET, CITY_DATASET);
        Model modelCity = mainModelContent.getModel(CITY_DATASET);
        modelCity.searchPrimaryKey(CITY_KEY_DATASET);
        ChangePrimaryKeyDialog primaryKeyDialog = ChangePrimaryKeyDialog.getInstance(browser);
        assertTrue(primaryKeyDialog.isErrorMessageVisible());
        primaryKeyDialog.clickCancel();
    }
}
