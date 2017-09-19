package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.qa.graphene.entity.disc.Parameters.createRandomParam;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;

public class CreateScheduleTest extends AbstractDataloadProcessTest {

    private static final String OPPORTUNITY_DATASET = "opportunity";
    private static final String PERSON_DATASET = "person";

    private static final String OVERLAPPED_DATASET_MESSAGE = "One or more of the selected datasets is already "
            + "included in an existing schedule. If multiple schedules that load same dataset run concurrently, "
            + "all schedules except the first will fail.";

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void createDataloadScheduleWithAllDatasets() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectAllDatasetsOption()
                .enterScheduleName(schedule)
                .schedule();

        try {
            assertTrue(DataloadScheduleDetail.getInstance(browser).isAllDatasetsOptionSelected(),
                    "All dataset radio option is not selected");
            assertTrue(projectDetailPage.getDataloadProcess().hasSchedule(schedule),
                    "Dataload schedule is not created");

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void createDataloadScheduleWithCustomDatasets() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(OPPORTUNITY_DATASET)
                .enterScheduleName(schedule)
                .schedule();

        try {
            DataloadScheduleDetail scheduleDetail = DataloadScheduleDetail.getInstance(browser);
            assertTrue(scheduleDetail.isCustomDatasetsOptionSelected(), "Custom dataset radio option is not selected");
            assertEquals(scheduleDetail.getSelectedDatasets(), singletonList(OPPORTUNITY_DATASET));
            assertTrue(projectDetailPage.getDataloadProcess().hasSchedule(schedule),
                    "Dataload schedule is not created");

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void createScheduleWithNoDataset() {
        DatasetDropdown dropdown = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectCustomDatasetsOption()
                .getDatasetDropdown()
                .expand()
                .clearAllSelected();
        assertFalse(dropdown.isSaveButtonEnabled(), "Save button is not disabled");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void editDataloadScheduleWithCustomDatasets() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(OPPORTUNITY_DATASET)
                .enterScheduleName(schedule)
                .schedule();

        try {
            DatasetDropdown dropdown = DataloadScheduleDetail.getInstance(browser).getDatasetDropdown().expand();
            dropdown.clearAllSelected();
            assertEquals(dropdown.getSelectedDatasets(), EMPTY_LIST);
            assertFalse(dropdown.isSaveButtonEnabled(), "Save button is not disabled");

            dropdown.selectAllDatasets();
            assertEquals(dropdown.getSelectedDatasets(), asList(OPPORTUNITY_DATASET, PERSON_DATASET));

            dropdown.cancel();
            assertEquals(dropdown.expand().getSelectedDatasets(), singletonList(OPPORTUNITY_DATASET));

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testSearchDataset() {
        DatasetDropdown dropdown = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectCustomDatasetsOption()
                .getDatasetDropdown()
                .expand();

        dropdown.searchDataset(OPPORTUNITY_DATASET);
        assertEquals(dropdown.getAvailableDatasets(), singletonList(OPPORTUNITY_DATASET));

        dropdown.searchDataset("portu");
        assertEquals(dropdown.getAvailableDatasets(), singletonList(OPPORTUNITY_DATASET));

        dropdown.searchDataset("!@#$%");
        assertEquals(dropdown.getAvailableDatasets(), EMPTY_LIST);

        dropdown.searchDataset("p");
        assertEquals(dropdown.getAvailableDatasets(), asList(OPPORTUNITY_DATASET, PERSON_DATASET));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDataloadDatasetsOverlap() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(OPPORTUNITY_DATASET)
                .enterScheduleName(schedule)
                .schedule();

        try {
            DataloadScheduleDetail.getInstance(browser).close();

            CreateScheduleForm scheduleForm = projectDetailPage.openCreateScheduleForm()
                    .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME);
            assertEquals(scheduleForm.getOverlappedDatasetMessage(), OVERLAPPED_DATASET_MESSAGE);

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void addRetryToDataloadSchedule() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(OPPORTUNITY_DATASET)
                .enterScheduleName(schedule)
                .schedule();

        try {
            final int retryInMinute = 15;

            DataloadScheduleDetail scheduleDetail = DataloadScheduleDetail.getInstance(browser);
            scheduleDetail.addRetryDelay(retryInMinute).saveChanges();
            assertEquals(scheduleDetail.getRetryDelayValue(), retryInMinute);

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void deleteDataloadSchedule() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(OPPORTUNITY_DATASET)
                .enterScheduleName(schedule)
                .schedule();

        try {
            DataloadScheduleDetail.getInstance(browser).deleteSchedule();
            assertFalse(projectDetailPage.getDataloadProcess().hasSchedule(schedule),
                    "Dataload schedule is not deleted");

        } finally {
            try {
                deleteScheduleByName(getDataloadProcess(), schedule);
            } catch (NoSuchElementException e) {
                // Schedule is already deleted in test
            }
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkParametersSectionIsDisplayed() {
        String parameterLabels = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .getParameterLabels();
        assertThat(parameterLabels, containsString("Add parameter"));
        assertThat(parameterLabels, containsString("Add secure parameter"));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void createAndUpdateParameter() throws JSONException, IOException {
        String schedule = "Schedule-" + generateHashString();
        Pair<String, String> parameter = createRandomParam();
        Pair<String, String> editedParameter = createRandomParam();
        CreateScheduleForm createScheduleForm = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule);

        createScheduleForm.addParameter(parameter.getKey(), parameter.getValue());
        createScheduleForm.schedule();
        try {
            DataloadScheduleDetail scheduleDetail = DataloadScheduleDetail.getInstance(browser);
            assertThat(scheduleDetail.getAllParametersInfo().entrySet(), hasItem(parameter));

            scheduleDetail.getParameter(parameter.getKey())
                    .editNameValuePair(editedParameter.getKey(), editedParameter.getValue());
            scheduleDetail.saveChanges();
            assertThat(scheduleDetail.getAllParametersInfo().entrySet(), hasItem(editedParameter));
        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void createParametersWithSpecialText() throws JSONException, IOException {
        String schedule = "Schedule-" + generateHashString();
        CreateScheduleForm createScheduleForm = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule);

        createScheduleForm.addParameter("GDC_DATALOAD_DATASETS", "DATALOAD_DATASETS");
        createScheduleForm.schedule();
        assertEquals(getBubbleMessage(browser), "System parameters are not allowed.");
        createScheduleForm.deleteParameter("GDC_DATALOAD_DATASETS");

        createScheduleForm.addParameter("abc", "abc|123");
        createScheduleForm.schedule();
        try {
            DataloadScheduleDetail scheduleDetail = DataloadScheduleDetail.getInstance(browser);
            assertTrue(scheduleDetail.hasParameter("abc"), "Parameter isn't added");
            assertEquals(scheduleDetail.getParameter("abc").getValue(), "abc|123");
        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void notSaveParameterAfterDeleted() throws JSONException, IOException {
        String schedule = "Schedule-" + generateHashString();
        Pair<String, String> parameter = createRandomParam();
        CreateScheduleForm createScheduleForm = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule);
        createScheduleForm.addParameter(parameter.getKey(), parameter.getValue());
        createScheduleForm.schedule();
        deleteScheduleByName(getDataloadProcess(), schedule);
        assertFalse(initDiscProjectDetailPage().openCreateScheduleForm().hasParameter(parameter.getKey()), 
                "Parameter is saved when delete");
    }
}
