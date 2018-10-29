package com.gooddata.qa.graphene.schedules;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

public class GoodSalesEmailScheduleEmbeddedDashboardTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String TAB_NAME = "Tab having report";
    private static final String NUMBER_OF_PAGE = "Page 1/1";
    private String firstDashboard = "First Dashboard";
    private String secondDashboard = "Second Dashboard";
    private String thirdDashboard = "Third Dashboard";
    private String firstReport = "First Report";
    private String secondReport = "Second Report";
    private String thirdReport = "Third Report";
    private ReportRestRequest reportRestRequest;
    private DashboardRestRequest dashboardRestRequest;
    private CommonRestRequest commonRestRequest;
    private String today;

    @BeforeClass
    public void setUp() {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory =
                new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
        firstDashboard = firstDashboard + identification;
        secondDashboard = secondDashboard + identification;
        thirdDashboard = thirdDashboard + identification;
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    protected void customizeProject() throws Throwable {
        reportRestRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        //To make sure that select dashboard method can work
        initDashboardHasReport("Zero Dashboard", "Zero Report");
    }

    @Test(dependsOnGroups = "createProject", groups = "schedules")
    public void signInImapUser() throws JSONException, IOException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateProjectConfiguration("newUIEnabled", "classic");
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void removeAttribute() throws IOException, MessagingException {
        initDashboardHasReport(firstDashboard, firstReport);
        initDashboardsPage().selectDashboard(firstDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(firstDashboard, 1);

        ReportDefinition reportDefinition = GridReportDefinitionContent.create(firstReport,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(firstReport, reportDefinition);

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(firstDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(firstReport, "Stage Name Amount", "Interest", "Discovery", "Short List", "Risk Assessment",
                "Conviction", "Negotiation", "Closed Won", "Closed Lost", "$18,447,266.14", "$4,249,027.88", "$5,612,062.60",
                "$2,606,293.46", "$3,067,466.12", "$1,862,015.73", "$38,310,753.45", "$42,470,571.16"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "removeAttribute")
    public void addAttribute() throws IOException, MessagingException {
        ReportDefinition reportDefinition = GridReportDefinitionContent.create(firstReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(firstReport, reportDefinition);
        initDashboardsPage().selectDashboard(firstDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(firstDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(firstReport, "Available area too small to display", "report"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void addReportFilter() throws IOException, MessagingException {
        initDashboardHasReport(secondDashboard, secondReport);
        initDashboardsPage().selectDashboard(secondDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(secondDashboard, 1);

        ReportDefinition reportDefinition = GridReportDefinitionContent.create(secondReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("(SELECT [%s]) < 0", getMetricByTitle(METRIC_AMOUNT).getUri()))));
        reportRestRequest.updateReport(secondReport, reportDefinition);

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(secondDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(secondReport, "No data"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "addReportFilter")
    public void removeReportFilter() throws IOException, MessagingException {
        ReportDefinition reportDefinition = GridReportDefinitionContent.create(secondReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))));
        reportRestRequest.updateReport(secondReport, reportDefinition);
        initDashboardsPage().selectDashboard(secondDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        initEmbeddedDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(secondDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(secondReport, "Available area too small to display", "report"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void addDashboardFilter() throws IOException, MessagingException {
        initDashboardHasReport(thirdDashboard, thirdReport);
        initDashboardsPage().selectDashboard(thirdDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        waitForScheduleMessages(thirdDashboard, 1);

        initEmbeddedDashboard()
                .editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .saveDashboard();
        embeddedDashboard.showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(thirdDashboard, 2), 1).split("\n"));
        //verify report
        assertThat(contents, hasItems(thirdReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, hasItems("STAGE NAME", "All"));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    @Test(dependsOnMethods = "addDashboardFilter")
    public void removeDashboardFilter() throws IOException, MessagingException {
        initDashboardsPage().selectDashboard(thirdDashboard);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        embeddedDashboard.editDashboard();
        DashboardEditWidgetToolbarPanel.removeWidget(embeddedDashboard.getContent().getFirstFilter().getRoot(), browser);
        embeddedDashboard.saveDashboard().showDashboardScheduleDialog().schedule();
        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        today = DateRange.getCurrentDate();
        List<String> contents =
                asList(getPdfContentFrom(waitForScheduleMessages(thirdDashboard, 3), 2).split("\n"));
        //verify report
        assertThat(contents, hasItems(thirdReport, "Available area too small to display", "report"));
        //verify filter
        assertThat(contents, not(hasItems("STAGE NAME", "All")));
        //verify title
        assertThat(contents, hasItem(format("%s %s", TAB_NAME, today)));
        //verify page
        assertThat(contents, hasItem(NUMBER_OF_PAGE));
    }

    private void initDashboardHasReport(String titleDashboard, String titleReport) throws IOException {
        createReport(GridReportDefinitionContent.create(titleReport,
                singletonList(METRIC_GROUP),
                Arrays.asList(
                        new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)),
                        new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));
        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(titleDashboard);
            dash.addTab(
                    Builder.of(Tab::new)
                            .with(tab -> {
                                tab.addItem(Builder.of(ReportItem::new).with(report -> {
                                    report.setObjUri(getReportByTitle(titleReport).getUri());
                                    report.setPosition(ItemPosition.LEFT);
                                }).build());
                                tab.setTitle(TAB_NAME);
                            }).build());
        }).build();

        dashboardRestRequest.createDashboard(dashboard.getMdObject());
    }
}
