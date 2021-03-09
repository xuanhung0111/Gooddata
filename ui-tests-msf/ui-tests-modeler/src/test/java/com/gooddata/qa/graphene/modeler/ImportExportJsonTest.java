package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.JsonUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

public class ImportExportJsonTest extends AbstractLDMPageTest {
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private String projectBlank;

    private final String DISTRICT_DATASET = "district";
    private final String DISTRICT_STREET = "street";
    private final String NUMBER_STREET = "numstreet";
    private final String CITY_DATASET = "city";
    private final String CITY_NAME_ATTRIBUTE = "cityname";
    private final String CITY_ID_ATTRIBUTE = "cityid";

    private final String PEOPLE_DATASET = "people";
    private final String PEOPLE_NAME_ATTRIBUTE = "peoplename";
    private final String SALARY_FACT = "salary";
    private final String BIRTHDAY_DATE = "birthday";
    private final String BLANK_PROJECT_TITLE = "blank project title";

    @Test(dependsOnGroups = {"createProject"})
    public void initTest() {
        ldmPage = initLogicalDataModelPage();
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
        restClient = new RestClient(getProfile(ADMIN));
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)),
                testParams.getProjectId());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "export_import_json.txt"));
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "export_import_cpk.txt"));
        initLogicalDataModelPage();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        modeler.getLayout().waitForLoading();
    }
    // I edit Modeler as below :
    // add more attribute , fact to Model as Draft content
    // delete draft object in canvas , delete attribute on existing Model
    // add Custom mapping dataset B and dataset C
    // Remove Source column of some Custom mapping of dataset B
    // Collapse some datasets

    @Test(dependsOnMethods = "initTest")
    public void testExportJson() throws IOException {
        try {
            // add more attribute , fact to Model as Draft content
            Model modelDistrict = mainModelContent.getModel(DISTRICT_DATASET);
            mainModelContent.focusOnDataset(DISTRICT_DATASET);
            mainModelContent.addAttributeToDataset(DISTRICT_STREET, DISTRICT_DATASET);
            scrollElementIntoView(modelDistrict.getRoot(), browser);
            mainModelContent.addFactToDataset(NUMBER_STREET, DISTRICT_DATASET);

            mainModelContent.focusOnDataset(CITY_DATASET);
            Model modelCity = mainModelContent.getModel(CITY_DATASET);
            modelCity.deleteAttributeOnDataset(CITY_NAME_ATTRIBUTE);

            mainModelContent.focusOnDataset(PEOPLE_DATASET);
            Model modelPeople = mainModelContent.getModel(PEOPLE_DATASET);
            scrollElementIntoView(modelPeople.getRoot(), browser);
            modelPeople.openEditDialog();
            EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
            DataMapping mappingTab = dialog.clickOnDataMappingTab();
            mappingTab.editSourceColumnByName(PEOPLE_NAME_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(),
                    PEOPLE_NAME_ATTRIBUTE, false);
            mappingTab.editSourceColumnByName(SALARY_FACT, DataMapping.SOURCE_TYPE.FACT.getName(), SALARY_FACT, false);
            mappingTab.editSourceColumnByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName(), BIRTHDAY_DATE, false);
            mappingTab.editSourceColumnByName(CITY_ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.REFERENCE.getName(), CITY_ID_ATTRIBUTE, false);
            dialog.saveChanges();

            mainModelContent.focusOnDataset(PEOPLE_DATASET);
            scrollElementIntoView(modelPeople.getRoot(), browser);
            modelPeople.openEditDialog();
            dialog = EditDatasetDialog.getInstance(browser);
            mappingTab = dialog.clickOnDataMappingTab();
            mappingTab.editDateFormatByName(BIRTHDAY_DATE, "yyyy-MM-dd");
            dialog.saveChanges();

            mainModelContent.focusOnDataset(PEOPLE_DATASET);
            scrollElementIntoView(modelPeople.getRoot(), browser);
            modelPeople.clickCollapseButton();

            toolbar.exportJson();
            File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + projectTitle + "." + ExportFormat.JSON.getName());
            waitForExporting(exportFile);
            String json = JsonUtils.getJsonObjectFromFile(exportFile).toString();
            assertThat(json, containsString("{\"identifier\":{\"id\":\"dataset.city\",\"type\":\"dataset\"}"));
            assertThat(json, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"dataset.people\",\"type\":\"dataset\"}"));
            assertThat(json, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"dataset.district\",\"type\":\"dataset\"}"));
            assertThat(json, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"createddate\",\"type\":\"templateDataset\"}"));
            assertThat(json, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"birthday\",\"type\":\"templateDataset\"}"));
            assertThat(json, containsString(getJsonContentMapping(testParams.getProjectId(), "/content_mapping_expected.txt")));
        } finally {
            File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                    + projectTitle + "." + ExportFormat.JSON.getName());
            Files.deleteIfExists(exportFile.toPath());
        }
    }

    @Test(dependsOnMethods = {"testExportJson"})
    public void testExportAfterPublishAndImportJson() throws IOException {
       try {
           projectBlank = createNewEmptyProject(restClient, BLANK_PROJECT_TITLE);
           toolbar.clickPublish();
           PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
           publishModelDialog.overwriteDataSwitchToEditMode();
           OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
           wrapper.closePublishSuccess();

           initLogicalDataModelPage();
           modeler.getLayout().waitForLoading();
           toolbar.exportJson();
           File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                   + projectTitle + "." + ExportFormat.JSON.getName());
           waitForExporting(exportFile);
           String json = JsonUtils.getJsonObjectFromFile(exportFile).toString();
           assertThat(json, containsString("{\"identifier\":{\"id\":\"dataset.city\",\"type\":\"dataset\"}"));
           assertThat(json, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"dataset.people\",\"type\":\"dataset\"}"));
           //TODO: Open it after MSF-19935 fixed
           //assertThat(json, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"dataset.district\",\"type\":\"dataset\"}"));
           assertThat(json, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"createddate\",\"type\":\"templateDataset\"}"));
           assertThat(json, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"birthday\",\"type\":\"templateDataset\"}"));
           assertThat(json, containsString(getJsonContentMapping(testParams.getProjectId(), "/content_mapping_after_publish.txt")));

           ldmPage = initLogicalDataModelPageByPID(projectBlank);
           modeler.getLayout().waitForLoading();

           toolbar.exportJson();
           ErrorContent error = ErrorContent.getInstance(browser);
           assertEquals(error.getErrorMessage(), "No data to export.");
           assertEquals(error.getErrorTitle(), "Exporting model to JSON failed.");
           error.cancel();

           toolbar.importJson(exportFile.getPath());
           toolbar.exportJson();
           File exportOnBlankProject = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                   + BLANK_PROJECT_TITLE + "." + ExportFormat.JSON.getName());
           waitForExporting(exportOnBlankProject);
           String jsonOnBlankProject = JsonUtils.getJsonObjectFromFile(exportOnBlankProject).toString();
           assertThat(jsonOnBlankProject, containsString("{\"identifier\":{\"id\":\"dataset.city\",\"type\":\"dataset\"}"));
           assertThat(jsonOnBlankProject, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"dataset.people\",\"type\":\"dataset\"}"));
           //TODO: Open it after MSF-19935 fixed
           //assertThat(jsonOnBlankProject, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"dataset.district\",\"type\":\"dataset\"}"));
           assertThat(jsonOnBlankProject, containsString("\"collapse\":false},{\"identifier\":{\"id\":\"createddate\",\"type\":\"templateDataset\"}"));
           assertThat(jsonOnBlankProject, containsString("\"collapse\":true},{\"identifier\":{\"id\":\"birthday\",\"type\":\"templateDataset\"}"));
           assertThat(jsonOnBlankProject, containsString(getJsonContentMapping(projectBlank, "/content_mapping_after_publish.txt")));

           toolbar.clickPublish();
           publishModelDialog = PublishModelDialog.getInstance(browser);
           publishModelDialog.overwriteDataSwitchToEditMode();
           wrapper = OverlayWrapper.getInstance(browser);
           wrapper.closePublishSuccess();
           String sql = getResourceAsString("/model_view_import_blank_project.txt");
           ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, projectBlank);
           JSONObject modelView = modelRestRequest.getProductionProjectModelView(false);
           assertEquals(modelView.toString(), sql);
       } finally {
           File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                   + projectTitle + "." + ExportFormat.JSON.getName());
           File exportFile2 = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
                   + BLANK_PROJECT_TITLE + "." + ExportFormat.JSON.getName());
           Files.deleteIfExists(exportFile.toPath());
           Files.deleteIfExists(exportFile2.toPath());
           restClient.getProjectService().removeProject(getAdminRestClient().getProjectService().getProjectById(projectBlank));
       }
    }

    public String getJsonContentMapping (String projectId, String fileName) {
        return getResourceAsString(fileName).replaceFirst("PROJECTID", projectId)
                .replaceFirst("\"dataset.people\",\"references\"", "\"dataset.people\",\"system\":{},\"references\"");
    }
}
