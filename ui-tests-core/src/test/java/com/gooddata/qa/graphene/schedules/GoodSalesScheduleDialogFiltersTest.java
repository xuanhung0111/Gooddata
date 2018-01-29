/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkGreenBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
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

    private static final String DASHBOARD_HAVING_FILTER = "Dashboard having filter";

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

    @Override
    protected void customizeProject() throws Throwable {
        String reportUri = getReportCreator().createAmountByProductReport();
        FilterItemContent regionFilter = createSingleValueFilter(getAttributeByTitle(ATTR_REGION));
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_FILTER);
            dash.addTab(Builder.of(Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(regionFilter.getId());
                            item.setPosition(TabItem.ItemPosition.RIGHT);
                        }).build();

                        tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                            reportItem.setObjUri(reportUri);
                            reportItem.setPosition(TabItem.ItemPosition.LEFT);
                            reportItem.setAppliedFilterIds(singletonList(filterItem.getId()));
                        }).build());
                        tab.addItem(filterItem);
                    })
                    .build());
            dash.addFilter(regionFilter);
        }).build();

        DashboardsRestUtils.createDashboard(getRestApiClient(), testParams.getProjectId(), dashboard.getMdObject());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"schedules"})
    public void verifyEmptySchedules() {
        assertEquals(initEmailSchedulesPage().getNumberOfGlobalSchedules(), 0, "There is no schedule.");
        Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
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
        dialog.selectTabs(new int[]{0});
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
