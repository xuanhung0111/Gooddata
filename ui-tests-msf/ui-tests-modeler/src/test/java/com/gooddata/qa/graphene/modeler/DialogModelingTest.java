package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.project.Project;
import org.json.JSONObject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

public class DialogModelingTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private TableView tableView;
    private TableViewDataset tableViewDataset;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private Project project;

    private static final String LAST_NAME = "Lastname";
    private static final String FIRST_NAME = "Firstname";
    private static final String FIRST_NAME_CHANGED = "Firstname_Changed";
    private static final String DATE = "Date";
    private static final String DATE_2 = "Date2";
    private static final String AMOUNT = "Amount";
    private static final String USER_DATASET = "user";
    private static final String USER_PRIMARY_KEY = "userid";
    private static final String CITY_DATASET = "city";
    private static final String CITY_PRIMARY_KEY = "cityid";
    private static final String PAYROLL_DATASET = "payroll";

    private static final List<String> DROP_DOWN_APPLY_TEXT = asList(LAST_NAME, FIRST_NAME, DATE, AMOUNT, DATE_2);

    private static final List<String> DROP_DOWN_APPLY_MEASURE = asList(AMOUNT);

    private static final List<String> DROP_DOWN_APPLY_DATE = asList(DATE, DATE_2);

    private static final List<String> DROP_DOWN_APPLY_DATE_FORMAT = asList("MM-dd-yyyy", "dd-MM-yyyy");

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getLayout().getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
        project = getAdminRestClient().getProjectService().getProjectById(testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "initial_model.txt"));
        initLogicalDataModelPage();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        modeler.getLayout().waitForLoading();
    }

    @Test(dependsOnMethods = "initTest")
    public void verifyDropDownPreviewDialog() {
        final CsvFile csv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/payroll.csv"));
        FileUploadDialog uploadDialog = sidebar.openCSVDialog();
        uploadDialog.pickCsvFile(csv.getFilePath());

        PreviewCSVDialog dialog = uploadDialog.importCSVShowPreview();
        assertEquals(dialog.getListHeaders(), asList(LAST_NAME, FIRST_NAME, DATE, AMOUNT, DATE_2));
        assertTrue(dialog.isShowCorrectRow("51"));
        assertEquals(dialog.getEditDatasetZone().getListColumns(), asList(LAST_NAME, FIRST_NAME, DATE, AMOUNT, DATE_2));
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(LAST_NAME), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(FIRST_NAME), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(DATE), "Reference");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(AMOUNT), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextImbigousDateByName(DATE_2), "Date");

        GenericList recommendTypeAttribute = dialog.getEditDatasetZone().clickOnDatatypeByName(LAST_NAME);
        assertTrue(recommendTypeAttribute.isDropdownApplyForAttribute());
        GenericList recommendTypeMeasure = dialog.getEditDatasetZone().clickOnDatatypeByName(AMOUNT);
        assertTrue(recommendTypeMeasure.isDropdownApplyForMeasure());
        GenericList recommendTypeDate = dialog.getEditDatasetZone().clickOnDatatypeByName(DATE);
        assertTrue(recommendTypeDate.isDropdownApplyForDate());
        GenericList recommendTypeDate_type2 = dialog.getEditDatasetZone().clickOnImbigousDateByName(DATE_2);
        assertTrue(recommendTypeDate_type2.isDropdownApplyForDate());
        GenericList recommendDateFormat = dialog.getEditDatasetZone().clickOnImbigousDateFormatByName(DATE_2);
        assertTrue(recommendDateFormat.isDropdownApplyForFormat());
    }

    @Test(dependsOnMethods = "verifyDropDownPreviewDialog")
    public void verifyEditPreviewDialog() {
        PreviewCSVDialog dialog = PreviewCSVDialog.getInstance(browser);
        dialog.getEditDatasetZone().editColumnByName(FIRST_NAME, FIRST_NAME_CHANGED);
        GenericList dropdownRecommend = dialog.getEditDatasetZone().clickOnDatatypeByName(LAST_NAME);
        dropdownRecommend.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());

        GenericList dropdownRecommend2 = dialog.getEditDatasetZone().clickOnDatatypeByName(FIRST_NAME_CHANGED);
        DatasetEdit edit = dropdownRecommend2.selectReferenceItem();
        ChooseReferencePopUp referencePopup = edit.getChooseReferencePopUp(FIRST_NAME_CHANGED);
        assertEquals(referencePopup.getlistReferenceText(), asList(CITY_DATASET, USER_DATASET));
        referencePopup.selectReferenceByName(USER_DATASET);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(USER_DATASET));

        GenericList dropdownRecommendDate1 = dialog.getEditDatasetZone().clickOnDatatypeByName(DATE);
        DatasetEdit edit_datereference_1 = dropdownRecommendDate1.selectReferenceItem();
        ChooseReferencePopUp referencePopupDate1 = edit_datereference_1.getChooseReferencePopUp(DATE);
        assertEquals(referencePopupDate1.getlistReferenceText(), asList(CITY_DATASET));
        referencePopupDate1.selectReferenceByName(CITY_DATASET);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(CITY_DATASET));

        GenericList dropdownRecommendDate = dialog.getEditDatasetZone().clickOnImbigousDateByName(DATE_2);
        DatasetEdit edit_date_reference = dropdownRecommendDate.selectReferenceItem();
        ChooseReferencePopUp referencePopupDate2 = edit_date_reference.getChooseReferencePopUp(DATE_2);
        assertEquals(referencePopupDate2.getlistReferenceText(), asList(DATE));
        referencePopupDate2.selectReferenceByName(DATE);
        assertTrue(dialog.getEditDatasetZone().isColumnDisabled(DATE));

        dialog.clickImportButton();
        modeler.getLayout().waitForLoading();
        assertTrue(isElementVisible(mainModelContent.getModel(PAYROLL_DATASET).getRoot()));
    }

    @Test(dependsOnMethods = "verifyEditPreviewDialog")
    public void verifyDataMappingTab() {
        Model modelPayroll = mainModelContent.getModel(PAYROLL_DATASET);
        mainModelContent.focusOnDataset(PAYROLL_DATASET);
        scrollElementIntoView(modelPayroll.getRoot(), browser);
        modelPayroll.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        assertEquals(dialog.getTextSourceName(),"payroll.csv");

        assertEquals(mappingTab.getSourceColumnByName(LAST_NAME, DataMapping.SOURCE_TYPE.LABEL.getName()), LAST_NAME);
        assertEquals(mappingTab.getSourceColumnByName(AMOUNT, DataMapping.SOURCE_TYPE.FACT.getName()), AMOUNT);
        assertEquals(mappingTab.getSourceColumnByName(CITY_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), DATE);
        assertEquals(mappingTab.getSourceColumnByName(USER_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), FIRST_NAME);
        assertEquals(mappingTab.getSourceColumnByName(DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), DATE_2);

        assertEquals(mappingTab.getSourceTypeByName(LAST_NAME, DataMapping.SOURCE_TYPE.LABEL.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(AMOUNT, DataMapping.SOURCE_TYPE.FACT.getName()), "number");
        assertTrue(mappingTab.getSourceTypeByName(DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()).contains("MM-dd-yyyy"));
        assertEquals(mappingTab.getSourceTypeByName(CITY_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(USER_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "text");

        log.info("Dropdown 1:" + mappingTab.getDropdownSourceColumnByName(LAST_NAME, DataMapping.SOURCE_TYPE.LABEL.getName()));
        log.info("Array" + DROP_DOWN_APPLY_TEXT.toArray());
        assertThat(mappingTab.getDropdownSourceColumnByName(LAST_NAME, DataMapping.SOURCE_TYPE.LABEL.getName()), containsInAnyOrder(DROP_DOWN_APPLY_TEXT.toArray()));
        assertThat(mappingTab.getDropdownSourceColumnByName(AMOUNT, DataMapping.SOURCE_TYPE.FACT.getName()), containsInAnyOrder(DROP_DOWN_APPLY_MEASURE.toArray()));
        assertThat(mappingTab.getDropdownSourceColumnByName(DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), containsInAnyOrder(DROP_DOWN_APPLY_DATE.toArray()));
        assertThat(mappingTab.getDropdownDateFormatByName(DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), containsInAnyOrder(DROP_DOWN_APPLY_DATE_FORMAT.toArray()));
        assertThat(mappingTab.getDropdownSourceColumnByName(CITY_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), containsInAnyOrder(DROP_DOWN_APPLY_TEXT.toArray()));
        assertThat(mappingTab.getDropdownSourceColumnByName(USER_PRIMARY_KEY, DataMapping.SOURCE_TYPE.REFERENCE.getName()), containsInAnyOrder(DROP_DOWN_APPLY_TEXT.toArray()));
    }
}

