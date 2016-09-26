/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectExecutionContext;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectScheduledEmailFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.query.scheduledemails.QueryScheduledEmailsFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

public class GoodSalesScheduleDialogFiltersTest extends AbstractGoodSalesEmailSchedulesTest {

    @FindBy(tagName = "fieldset")
    protected QueryScheduledEmailsFragment queryScheduledEmailsFragment;

    @FindBy(tagName = "pre")
    protected ObjectScheduledEmailFragment scheduledEmailFragment;

    @FindBy(tagName = "pre")
    protected ObjectExecutionContext executionContextFragments;

    private String customSubject;
    private String mdBaseUri;

    @BeforeClass
    public void getCustomSubject() {
        customSubject = testParams.getTestIdentification();
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() throws JSONException {
        initDashboardsPage();

        getMDBaseUri();

        setupFilters();

        DashboardScheduleDialog dialog = dashboardsPage.showDashboardScheduleDialog();

        assertTrue(dialog.isFilterMessagePresent(), "Scheduled mail will be filtered with active filters");

        setupSchedule(dialog);

        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog", this.getClass());

        dialog.schedule();
        checkGreenBar(browser, "Dashboard scheduled successfully.");
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void checkManagePageForScheduleExistence() {
        assertFalse(initEmailSchedulesPage().isGlobalSchedulePresent(customSubject));
    }

    @Test(dependsOnMethods = {"createDashboardSchedule"}, groups = {"schedules"})
    public void checkGreyPagesForScheduleExistence() {
        initGreyPage("/query/scheduledmails", queryScheduledEmailsFragment);
        assertTrue(queryScheduledEmailsFragment.existsScheduleWithTitle(customSubject));
    }

    @Test(dependsOnMethods = {"checkGreyPagesForScheduleExistence"}, groups = {"schedules"})
    public void checkScheduleExecutionContext() throws JSONException {
        assertTrue(scheduleHasValidExecutionContext(customSubject));
    }

    private void getMDBaseUri() {
        mdBaseUri = PAGE_GDC_MD + "/" + testParams.getProjectId();
    }

    private boolean scheduleHasValidExecutionContext(String title) throws JSONException {
        int scheduleId = getScheduleId(title);
        int executionContextId = getExecutionContextId(scheduleId);

        initGreyPage("/obj/" + executionContextId, executionContextFragments);

        return executionContextFragments.getType().equals("email");
    }

    private int getExecutionContextId(int scheduleId) throws JSONException {
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
        openUrl(mdBaseUri.concat(uriSuffix));
        waitForElementPresent(fragment.getRoot());
    }

    private void setupFilters() {
        FilterWidget regionFilter = dashboardsPage.getFilterWidget("region");
        regionFilter.changeAttributeFilterValues("East Coast");
    }

    private void setupSchedule(DashboardScheduleDialog dialog) {
        dialog.showCustomForm();
        dialog.selectTabs(new int[]{1});
        dialog.selectTime(1);
        dialog.setCustomEmailSubject(customSubject);
    }

    @AfterClass
    private void deleteEmailScheduleAndExecutionContext() throws JSONException {
        int scheduleId = getScheduleId(customSubject);
        int executionContextId = getExecutionContextId(scheduleId);

        restApiClient = getRestApiClient();

        deleteObject(scheduleId);
        deleteObject(executionContextId);
    }

    private void deleteObject(int objectId) {
        RestUtils.executeRequest(restApiClient,
                restApiClient.newDeleteMethod(getRootUrl() + mdBaseUri + "/obj/" + Integer.toString(objectId)));
    }
}
