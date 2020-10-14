package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.graphene.fragments.modeler.SearchDropDown.SEARCH_ITEM;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;


import static java.lang.String.format;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SearchModelTest extends AbstractLDMPageTest {
    private static final String SEARCH_DATASET_TEXT = "datasetA";
    private static final String DATASET_NAME = "datasetA";
    private static final String PRIMARY_KEY_NAME = "keyA";
    private static final String ATTRIBUTE_NAME = "attributeB";
    private static final String LABEL_NAME = "labelB";
    private static final String FACT_NAME = "factC";
    private static final String DATASET_ID= "dataseta";
    private static final String PRIMARY_KEY_A = "keya";
    private static final String ATTRIBUTE_B = "attributeb";
    private static final String LABEL_B = "labelb";
    private static final String FACT_C = "factc";
    private static final String TOTAL_ROW = "7 items found";
    private static final String TOTAL_ROW_UTF8 = "3 items found";
    private static final String TOTAL_ROW_UTF8_SECOND = "2 items found";
    private static final String TOTAL_ROW_DATE = "1 items found";

    private static final String SEARCH_ATTRIBUTE_TEXT = "kiểm tra";
    private static final String DATASET_UTF8_NAME = "qa kiểm tra";
    private static final String DATASET_UTF8_ID = "qakiemtra";
    private static final String ATTRIBUTE_UTF8_NAME = "key kiểm tra";
    private static final String ATTRIBUTE_UTF8_ID = "keykiemtra";
    private static final String FACT_UTF8_ID = "fact";

    private static final String SEARCH_FACT_TEXT = "fact ^&";
    private static final String FACT_UTF8_NAME = "fact ^&";
    private static final String SEARCH_DATE_TEXT = "datesearch";
    private static final String DATE_ID = "datesearch";

    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private MainModelContent mainModelContent;
    private RestClient restClient;
    private IndigoRestRequest indigoRestRequest;
    private Project project;

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
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)),
                testParams.getProjectId());
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + "search_model.txt"));
        initLogicalDataModelPage();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        modeler.getLayout().waitForLoading();
    }

    @Test(dependsOnMethods = "initTest")
    public void verifyBasicSearchResult() {
        SearchDropDown dropDown = toolbar.searchItem(SEARCH_DATASET_TEXT);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.DATASET.getClassId(), DATASET_ID)), DATASET_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.ATTRIBUTE.getClassId(), DATASET_ID, PRIMARY_KEY_A)), PRIMARY_KEY_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.LABEL.getClassId(), DATASET_ID, PRIMARY_KEY_A)), PRIMARY_KEY_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.ATTRIBUTE.getClassId(), DATASET_ID, ATTRIBUTE_B)), ATTRIBUTE_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.LABEL.getClassId(), DATASET_ID, ATTRIBUTE_B)), ATTRIBUTE_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.OPT_LABEL.getClassId(), DATASET_ID, ATTRIBUTE_B, LABEL_B)), LABEL_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.FACT.getClassId(), DATASET_ID, FACT_C)), FACT_NAME);

        toolbar.clearSearchText();
        SearchDropDown dropDown2 = toolbar.searchItem(SEARCH_DATE_TEXT);
        assertEquals(dropDown2.getSearchItemName(format(SEARCH_ITEM.DATE_DATASET.getClassId(), DATE_ID)), DATE_ID);
        assertEquals(dropDown2.getTextTotalRows(), TOTAL_ROW_DATE);
    }

    @Test(dependsOnMethods = "verifyBasicSearchResult")
    public void verifyUtf8SearchResult() {
        toolbar.clearSearchText();
        SearchDropDown dropDown = toolbar.searchItem(SEARCH_ATTRIBUTE_TEXT);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.DATASET.getClassId(), DATASET_UTF8_ID)), DATASET_UTF8_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.ATTRIBUTE.getClassId(),
                DATASET_UTF8_ID, ATTRIBUTE_UTF8_ID)), ATTRIBUTE_UTF8_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.LABEL.getClassId(),
                DATASET_UTF8_ID, ATTRIBUTE_UTF8_ID)), ATTRIBUTE_UTF8_NAME);
        assertEquals(dropDown.getTextTotalRows(), TOTAL_ROW_UTF8);

        toolbar.clearSearchText();
        SearchDropDown dropDown2 = toolbar.searchItem(SEARCH_FACT_TEXT);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.DATASET.getClassId(), DATASET_UTF8_ID)), DATASET_UTF8_NAME);
        assertEquals(dropDown.getSearchItemName(format(SEARCH_ITEM.FACT.getClassId(),
                DATASET_UTF8_ID, FACT_UTF8_ID)), FACT_UTF8_NAME);
        assertEquals(dropDown2.getTextTotalRows(), TOTAL_ROW_UTF8_SECOND);
    }

    @Test(dependsOnMethods = "verifyUtf8SearchResult")
    public void selectItemTest() {
        toolbar.clearSearchText();
        SearchDropDown dropDown = toolbar.searchItem(SEARCH_DATASET_TEXT);
        dropDown.selectItem(format(SEARCH_ITEM.DATASET.getClassId(), DATASET_ID));
        Model selectedModel = JointLayers.getInstance(browser).getSelectedDataset();
        assertEquals(selectedModel.getModelId(), format(SEARCH_ITEM.DATASET.getClassId(), DATASET_ID));

        toolbar.clearSearchText();
        SearchDropDown dropDown2 = toolbar.searchItem(SEARCH_DATASET_TEXT);
        dropDown2.selectItem(format(SEARCH_ITEM.ATTRIBUTE.getClassId(),
                DATASET_ID, PRIMARY_KEY_A));
        Model selectedModel2 = JointLayers.getInstance(browser).getSelectedDataset();
        assertEquals(selectedModel2.getModelId(), format(SEARCH_ITEM.DATASET.getClassId(), DATASET_ID));

        toolbar.clearSearchText();
        SearchDropDown dropDown3 = toolbar.searchItem(SEARCH_FACT_TEXT);
        dropDown3.selectItem(format(SEARCH_ITEM.FACT.getClassId(), DATASET_UTF8_ID, FACT_UTF8_ID));
        Model selectedModel3 = JointLayers.getInstance(browser).getSelectedDataset();
        assertEquals(selectedModel3.getModelId(), format(SEARCH_ITEM.DATASET.getClassId(), DATASET_UTF8_ID));
    }

    @Test(dependsOnMethods = "selectItemTest")
    public void selectLabelTest() {
        toolbar.clearSearchText();
        SearchDropDown dropDown = toolbar.searchItem(SEARCH_ATTRIBUTE_TEXT);
        dropDown.selectItem(format(SEARCH_ITEM.LABEL.getClassId(), DATASET_UTF8_ID, ATTRIBUTE_UTF8_ID));
        assertTrue(isElementVisible(EditDatasetDialog.getInstance(browser).getRoot()));
        assertTrue(EditDatasetDialog.getInstance(browser).getViewDetailDialog().isRowSelected(ATTRIBUTE_UTF8_NAME));
    }
}
