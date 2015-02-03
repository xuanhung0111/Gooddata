/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.FilterWidget;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDialogFiltersTest extends AbstractGoodSalesEmailSchedulesTest {

    private final String SUCCESSFULY_SCHEDULED_MESSAGE = "Dashboard scheduled successfully.";

    @FindBy(tagName = "fieldset")
    protected QueryScheduledEmailsFragment queryScheduledEmailsFragment;

    @FindBy(tagName = "pre")
    protected ObjectScheduledEmailFragment scheduledEmailFragment;

    @FindBy(tagName = "pre")
    protected ObjectExecutionContext executionContextFragments;

    private String custom_subject;
    private String md_base_uri;

    @BeforeClass
    public void getCustomSubject() {
        custom_subject = testParams.getTestIdentification();
    }

    @BeforeClass
    public void getMDBaseUri() {
        md_base_uri = PAGE_GDC_MD + "/" + testParams.getProjectId();
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() throws JSONException {
        initDashboardsPage();

        setupFilters();

        DashboardScheduleDialog dialog = dashboardsPage.showDashboardScheduleDialog();

        assertTrue(dialog.isFilterMessagePresent(), "Scheduled mail will be filtered with active filters");

        setupSchedule(dialog);

        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());

        dialog.schedule();
        checkGreenBar(browser, SUCCESSFULY_SCHEDULED_MESSAGE);
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void checkManagePageForScheduleExistence() {
        initEmailSchedulesPage();
        assertFalse(emailSchedulesPage.isSchedulePresent(custom_subject));
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void checkGreyPagesForScheduleExistence() {
        initGreyPage("/query/scheduledmails", queryScheduledEmailsFragment);
        assertTrue(queryScheduledEmailsFragment.existsScheduleWithTitle(custom_subject));
    }

    @Test(dependsOnMethods = {"checkGreyPagesForScheduleExistence"}, groups = {"schedules"})
    public void checkScheduleExecutionContext() throws JSONException, InterruptedException {
        assertTrue(scheduleHasValidExecutionContext(custom_subject));
    }

    private boolean scheduleHasValidExecutionContext(String title) throws JSONException, InterruptedException {
        int scheduleId = getScheduleId(title);
        int executionContextId = getExecutionContextId(scheduleId);

        initGreyPage("/obj/" + executionContextId, executionContextFragments);

        return executionContextFragments.getType().equals("email");
    }

    private int getExecutionContextId(int scheduleId) throws JSONException, InterruptedException {
        initGreyPage("/obj/" + scheduleId, scheduledEmailFragment);

        assertTrue(scheduledEmailFragment.hasExecutionContext());

        int executionContextId = scheduledEmailFragment.getExecutionContextId();

        assertTrue(executionContextId != ObjectScheduledEmailFragment.WRONG_ID);

        return executionContextId;
    }

    private int getScheduleId(String scheduleTitle) {
        initGreyPage("/query/scheduledmails", queryScheduledEmailsFragment);

        int scheduleID = queryScheduledEmailsFragment.getScheduleId(scheduleTitle);

        assertTrue(scheduleID != QueryScheduledEmailsFragment.WRONG_ID);

        return scheduleID;
    }

    private void initGreyPage(String uriSuffix, AbstractFragment fragment) {
        openUrl(md_base_uri.concat(uriSuffix));
        waitForElementPresent(fragment.getRoot());
    }

    private void setupFilters() {
        FilterWidget regionFilter = dashboardsPage.getFilterWidget("region");
        regionFilter.changeAttributeFilterValue("East Coast");
    }

    private void setupSchedule(DashboardScheduleDialog dialog) {
        dialog.showCustomForm();
        dialog.selectTabs(new int[]{1});
        dialog.selectTime(1);
        dialog.setCustomEmailSubject(custom_subject);
    }
}
