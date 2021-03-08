package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.ObjectTypes;
import com.gooddata.qa.graphene.fragments.manage.DatasetDetailPage;
import com.gooddata.qa.graphene.fragments.manage.ObjectsTable;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.Canvas;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.PublishModelDialog;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;

import com.gooddata.qa.utils.http.RestClient;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;

public class DescriptionOfDatasetTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private RestClient restClient;

    private final String DATASET_NAME = "Company";
    private final String DATASET_DESCRIPTION = "Description for dataset";
    private final String FACT_DESCRIPTION = "Description for fact";
    private final String ATTR_DESCRIPTION = "Description for attribute";
    private final String FACT_NAME = "age";
    private final String ATTR_NAME = "employee";
    private final String NEW_ATTR_NAME = "type";


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
    public void checkDescriptionOfDatasetAfterPublisedModel() {
        canvas = modeler.getLayout().getCanvas();
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_default_model.txt"));
        initLogicalDataModelPage();
        waitForFragmentVisible( modeler.getLayout());
        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        initManagePage();
        ObjectsTable datasetTable = ObjectsTable.getInstance(id(ObjectTypes.DATA_SETS.getObjectsTableID()), browser);
        datasetTable.selectObject(DATASET_NAME);
        DatasetDetailPage page = DatasetDetailPage.getInstance(browser);
        page.changeDescription(DATASET_DESCRIPTION);
        assertEquals(page.getDescription(), DATASET_DESCRIPTION);

        initFactPage();
        ObjectsTable factTable = ObjectsTable.getInstance(id(ObjectTypes.FACT.getObjectsTableID()), browser);
        factTable.selectObject(FACT_NAME);
        page.changeDescription(FACT_DESCRIPTION);
        assertEquals(page.getDescription(), FACT_DESCRIPTION);

        initAttributePage();
        ObjectsTable attrTable = ObjectsTable.getInstance(id(ObjectTypes.ATTRIBUTE.getObjectsTableID()), browser);
        attrTable.selectObject(ATTR_NAME);
        page.changeDescription(ATTR_DESCRIPTION);
        assertEquals(page.getDescription(), ATTR_DESCRIPTION);

        initLogicalDataModelPage();
        mainModelContent.getModel(DATASET_NAME.toLowerCase());
        mainModelContent.focusOnDataset(DATASET_NAME.toLowerCase());
        mainModelContent.addAttributeToDataset(NEW_ATTR_NAME, DATASET_NAME.toLowerCase());

        publishOverrideModel();
        initManagePage();
        datasetTable.selectObject(DATASET_NAME);
        assertEquals(page.getDescription(), DATASET_DESCRIPTION);

        initFactPage();
        factTable.selectObject(FACT_NAME);
        assertEquals(page.getDescription(), FACT_DESCRIPTION);

        initAttributePage();
        attrTable.selectObject(ATTR_NAME);
        assertEquals(page.getDescription(), ATTR_DESCRIPTION);
    }

    public void publishOverrideModel() {
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.publishSwitchToEditMode();
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        wrapper.closePublishSuccess();
    }
}
