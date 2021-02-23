package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.fragments.modeler.ErrorContent;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.Model;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.EditDatasetDialog;
import com.gooddata.qa.graphene.fragments.modeler.DataMapping;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.ViewMode;
import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class WarningMessageTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private RestClient restClient;
    private Model modelCity;

    private final String CITY_DATASET = "city";
    private final String DISTRICT_ATTR = "district";
    private final String EMPLOYEE_ATTR = "employee";
    private final String AGE_FACT = "age";
    private final String HOMETOWN_ATTR = "hometown";
    private final String DISTRICT_SOURCE_COLUMN = "l__district";
    private final String HOMETOWN_SOURCE_COLUMN = "l__hometown";
    private final String MAPPING_NOT_SET = "Mapping not set";
    private final String TOOLTIP_CLASSNAME = "missing-mapping-tooltip";
    private final String NO_FACT_KEY = "The dataset has no primary key set and does not contain any fact either.\n" +
            "Either set a primary key or add a fact to the dataset.";
    private final String UNMAPPED_FIELD = "Some fields in this dataset are not mapped.\n" +
            "Click More… -> View details -> Load configuration to see the details.";
    private final String DUPLICATED_SOURCE = "Some source columns mapped to the dataset fields are used more than once.\n" +
            "Click More… -> View details -> Load configuration to see the details";
    private final String ERROR_PUBLISH = "The dataset 'dataset.%s' has no primary key set and does not contain any fact either.";
    private final String SAME_SOURCE = "This column is already mapped to district. Only one dataset field can be mapped to a source column at a time.";

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_model_warning.txt"));
        initLogicalDataModelPage();
        waitForFragmentVisible( modeler.getLayout());
        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
    }

    @Test(dependsOnMethods = {"initTest"})
    public void noFactAndPrimaryKey() {
        Model cityDataset = mainModelContent.getModel(CITY_DATASET);
        modelCity = mainModelContent.getModel(CITY_DATASET);
        mainModelContent.focusOnDataset(CITY_DATASET);
        cityDataset.deleteAttributeOnDataset(EMPLOYEE_ATTR);
        cityDataset.deleteFactOnDataset(AGE_FACT, CITY_DATASET);
        mainModelContent.loseFocusOnDataset();

        assertTrue(modelCity.isWarningMessageDisplayed(), "Warning message should be displayed");
        assertEquals(modelCity.getWarningMessageContent(), NO_FACT_KEY);

        publishModel();
        ErrorContent error = ErrorContent.getInstance(browser);
        assertThat(error.getErrorMessage(), containsString(String.format(ERROR_PUBLISH, CITY_DATASET)));
        error.cancel();

        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(DISTRICT_ATTR, CITY_DATASET);
        mainModelContent.loseFocusOnDataset();

        assertTrue(modelCity.isWarningMessageDisplayed(), "Warning message should be displayed");
        assertEquals(modelCity.getWarningMessageContent(), NO_FACT_KEY);
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addFactToDataset(AGE_FACT, CITY_DATASET);
        mainModelContent.loseFocusOnDataset();
        mainModelContent.focusOnDataset(CITY_DATASET);

        assertFalse(modelCity.isWarningMessageDisplayed(), "Warning message should be hidden because added fact");
        modelCity.deleteFactOnDataset(AGE_FACT, CITY_DATASET);
        assertTrue(modelCity.isWarningMessageDisplayed(), "Warning message should be displayed because deleted fact");

        mainModelContent.focusOnDataset(CITY_DATASET);
        modelCity.setPrimaryKey(DISTRICT_ATTR);
        assertFalse(modelCity.isWarningMessageDisplayed(), "Warning message should be hidden");

        publishModel();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        assertThat(wrapper.getTextPublishSuccess(), containsString("Model successfully published! Load data now"));
        wrapper.closePublishSuccess();
    }

    @Test(dependsOnMethods = {"noFactAndPrimaryKey"})
    public void unmappedFieldAndDuplicatedSourceColumn() {
        ToolBar.getInstance(browser).clickEditBtn();
        mainModelContent.focusOnDataset(CITY_DATASET);
        mainModelContent.addAttributeToDataset(HOMETOWN_ATTR, CITY_DATASET);
        modelCity.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();

        assertEquals(mappingTab.getWarningSourceColumnByName(HOMETOWN_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName()), MAPPING_NOT_SET);
        assertEquals(mappingTab.getWarningSourceColumnByName(DISTRICT_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName()), MAPPING_NOT_SET);

        mappingTab.editSourceColumnByName(HOMETOWN_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName(), HOMETOWN_SOURCE_COLUMN,false);
        dialog.saveChanges();

        mainModelContent.focusOnDataset(CITY_DATASET);
        assertTrue(modelCity.isWarningMessageDisplayed(), "Warning message should be displayed");
        assertEquals(modelCity.getWarningMessageContent(), UNMAPPED_FIELD);

        modelCity.openEditDialog();
        dialog.clickOnDataMappingTab();
        mappingTab.editSourceColumnByName(DISTRICT_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName(), DISTRICT_SOURCE_COLUMN,false);
        dialog.saveChanges();

        mainModelContent.focusOnDataset(CITY_DATASET);
        assertFalse(modelCity.isWarningMessageDisplayed(), "Warning message should be hidden");

        modelCity.openEditDialog();
        dialog.clickOnDataMappingTab();
        mappingTab.editSourceColumnByName(HOMETOWN_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName(), DISTRICT_SOURCE_COLUMN,true);

        assertEquals(mappingTab.getWarningMessage(HOMETOWN_ATTR, DataMapping.SOURCE_TYPE.LABEL.getName()), SAME_SOURCE);
        dialog.saveChanges();

        mainModelContent.focusOnDataset(CITY_DATASET);
        assertTrue(modelCity.isWarningMessageDisplayed(), "Warning message should be displayed");
        assertEquals(modelCity.getWarningMessageContent(), DUPLICATED_SOURCE);
    }

    public void publishModel() {
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteDataAcceptError();
    }
}
