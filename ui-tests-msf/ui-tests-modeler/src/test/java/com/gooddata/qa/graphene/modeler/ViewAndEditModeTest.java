package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.modeler.*;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class ViewAndEditModeTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private Canvas canvas;
    private ToolBar toolbar;
    private MainModelContent mainModelContent;
    private PreviewCSVDialog dialog;
    private FileUploadDialog uploadDialog;

    private static final String INITIAL_TEXT_TITLE = "Bring meaning to data";
    private static final String INITIAL_TEXT_CONTENT = "The logical data model is the foundation of every workspace. " +
            "The way you organize your data determines the performance, usability, and impact of your analytics solution. " +
            "We recommend that you start small and iterate.";
    private static final String GUIDE_LINE_LINK = "https://community.gooddata.com/administration-and-loading-and-modeling" +
            "-data-19/logical-data-model-introduction-151";
    private final String CLASS_DATASET = "class";

    @Test(dependsOnGroups = {"createProject"})
    public void initialPageTest() {
        ldmPage = openViewModeLDMPage();
        modeler = ldmPage.getDataContent().getModeler();
        modeler.getLayout().waitForLoading();
        toolbar = modeler.getToolbar();

        ViewMode viewMode = ViewMode.getInstance(browser);
        assertTrue(viewMode.getInitialDescribeText().contains(INITIAL_TEXT_TITLE),"Title Initial is not correct");
        assertTrue(viewMode.getInitialDescribeText().contains(INITIAL_TEXT_CONTENT),"Content Initial is not correct");
        assertEquals(viewMode.getGuideLineLink(), GUIDE_LINE_LINK);
    }

    @Test(dependsOnMethods = {"initialPageTest"})
    public void changeToEditModeTest() {
        switchModelPageToViewMode();
        sidebar = modeler.getSidebar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();

        assertFalse(toolbar.isSaveAsDraftVisible());
        toolbar.clickActionMenuButton();
        OverlayWrapper wrapper = OverlayWrapper.getInstanceByIndex(browser, 1);
        assertTrue(wrapper.isExportModelButtonExist(), "Export Model Button should display");
        assertTrue(wrapper.isImportModelButtonExist(), "Import Model Button should display");
        assertFalse(wrapper.isGenerateOutputStageItemButtonExist(), "OutputStage button should hidden");

        toolbar.clickChangeToViewModeBtn();
        ViewMode viewMode = ViewMode.getInstance(browser);
        assertTrue(viewMode.getInitialDescribeText().contains(INITIAL_TEXT_TITLE),"Title Initial is not correct");
        assertTrue(viewMode.getInitialDescribeText().contains(INITIAL_TEXT_CONTENT),"Content Initial is not correct");
        assertEquals(viewMode.getGuideLineLink(), GUIDE_LINE_LINK);

        switchModelPageToViewMode();
        final CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/class.csv"));
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());
        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        dialog.closeFirstTableDialog();
        //import to project
        dialog.clickImportButton();
        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        assertTrue(isElementVisible(mainModelContent.getModel(CLASS_DATASET).getRoot()));
        assertTrue(toolbar.isSaveAsDraftVisible());
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.uncheckUploadDataCsvFile().publishModel();

        modeler.getLayout().waitForLoading();
        assertTrue(isElementVisible(mainModelContent.getModel(CLASS_DATASET).getRoot()));
        assertTrue(toolbar.getTextNotification().contains("View mode"));
        mainModelContent.focusOnDataset(CLASS_DATASET);

        ModelAction action = mainModelContent.getModel(CLASS_DATASET).getModelAction();
        assertFalse(action.isAddAttributeExist(), "add Attribute button should hidden");
        assertFalse(action.isAddFactExist(), "add Fact button should hidden");
        assertFalse(action.isAddMoreExist(), "add More button should hidden");
        assertTrue(action.isViewDetailsExist(), "view Details button should display");

        EditDatasetDialog editDialog = action.openEditDatasetDialog();
        assertFalse(editDialog.isSaveChangeButtonExist(), "Save change button should hidden");
        editDialog.clickCancel();

        toolbar.clickActionMenuButton();
        assertTrue(wrapper.isExportModelButtonExist(), "Export Model Button should hidden");
        assertFalse(wrapper.isImportModelButtonExist(), "Import Model Button should display");
        assertTrue(wrapper.isGenerateOutputStageItemButtonExist(), "OutputStage button should display");

        toolbar.clickEditOnPopUp();
        modeler.getLayout().waitForLoading();
        mainModelContent.focusOnDataset(CLASS_DATASET);
        assertTrue(action.isAddAttributeExist(), "add Attribute button should display");
        assertTrue(action.isAddFactExist(), "add Fact button should display");
        assertTrue(action.isAddMoreExist(), "add More button should hidden");
        assertFalse(action.isViewDetailsExist(), "view Details button should display");
    }
}
