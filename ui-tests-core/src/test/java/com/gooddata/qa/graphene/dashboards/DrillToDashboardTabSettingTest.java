package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel.DrillingGroup;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DrillToDashboardTabSettingTest extends GoodSalesAbstractTest {

    private final String FIRST_TAB = "First Tab";
    private final String SECOND_TAB = "Second Tab";
    private final String THIRD_TAB = "Third Tab";

    private String reportUri;
    private WidgetConfigPanel widgetConfigPanel;
    private DrillingConfigPanel drillingConfigPanel;
    private TableReport report;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        reportUri = getReportCreator().createAmountByProductReport();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        getReportCreator().createActivitiesByTypeReport();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyDashboardDrillingTest() throws IOException, JSONException {
        final List<String> tabs = asList(DrillingGroup.ATTRIBUTES.getName(), DrillingGroup.REPORTS.getName(),
                DrillingGroup.DASHBOARDS.getName());
        final String toolTipIconHelp =
                "By clicking on an attribute value or a metric value, the linked dashboard tab will open.";
        Dashboard dash = initDashboard();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            dashboardsPage.editDashboard();

            TableReport reportOnFirstTab = dashboardsPage.getContent().getLatestReport(TableReport.class);

            WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                    .openConfigurationPanelFor(reportOnFirstTab.getRoot(), browser);
            DrillingConfigPanel drillingConfigPanel = widgetConfigPanel
                    .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);
            assertEquals(drillingConfigPanel.getTooltipFromHelpIcon(DrillingGroup.DASHBOARDS.getName()), toolTipIconHelp);
            assertEquals(drillingConfigPanel.getRightItemGroups(DrillingGroup.DASHBOARDS.getName()), tabs);
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tryToAddDrillingToDashboardTabTest() throws IOException, JSONException {
        Dashboard dash = initDashboard();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), SECOND_TAB, DrillingGroup.DASHBOARDS.getName()));

            widgetConfigPanel.discardConfiguration();
            assertFalse(report.isDrillable("CompuSci", CellType.ATTRIBUTE_VALUE),"Action discard configuration not work");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addMultiDrillingToDashboardTabTest() throws IOException, JSONException {
        Dashboard dash = initDashboard();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(METRIC_AMOUNT), SECOND_TAB, DrillingGroup.DASHBOARDS.getName()));

            drillingConfigPanel.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), THIRD_TAB), DrillingGroup.DASHBOARDS.getName());
            assertTrue(drillingConfigPanel.getAllValueLists(0).getRight().containsAll(
                    asList(FIRST_TAB, SECOND_TAB, THIRD_TAB)), "Some dashboard tabs is missing");

            widgetConfigPanel.saveConfiguration();
            dashboardsPage.saveDashboard();

            assertTrue(report.isDrillable("$27,222,899.64", CellType.METRIC_VALUE),
                    "Drill setting for the report is not saved");
            report.drillOnFirstValue(CellType.METRIC_VALUE);
            report.waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(SECOND_TAB).isSelected(),SECOND_TAB + " is not selected after drilling");

            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            assertTrue(report.isDrillable("CompuSci", CellType.ATTRIBUTE_VALUE),
                    "Drill setting for the report is not saved");
            report.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            report.waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(THIRD_TAB).isSelected(),THIRD_TAB + " is not selected after drilling");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addMultiInnerDrillingTest() throws IOException, JSONException {
        Dashboard dash = initDashboard();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(METRIC_AMOUNT), REPORT_ACTIVITIES_BY_TYPE, DrillingGroup.REPORTS.getName()));
            assertTrue(drillingConfigPanel.canAddInnerDrill(), "+ Drill further in Activities by Type is not displayed");

            drillingConfigPanel
                    .openNewInnerDrillPanel(0)   //Using to verify default setting
                    .addInnerDrillToLastItemPanel(Pair.of(singletonList(METRIC_NUMBER_OF_ACTIVITIES), SECOND_TAB))
                    .addInnerDrillToLastItemPanel(Pair.of(singletonList(ATTR_ACTIVITY_TYPE), THIRD_TAB));
            assertFalse(drillingConfigPanel.canAddInnerDrill(),
                    "Drill further in Activities by Type is not hidden after all metrics/atts are added drilling setting");

            assertEquals(drillingConfigPanel.getSettingsOnLastItemPanel(), Pair.of(METRIC_AMOUNT, "Activities By Type"));
            assertEquals(
                    drillingConfigPanel.getAllInnerDrillSettingsOnLastPanel(),
                    asList(Pair.of("Select Metric / Attribute...", "Select Dashboard"),
                            Pair.of("# Of Activities", SECOND_TAB), Pair.of(ATTR_ACTIVITY_TYPE, THIRD_TAB)));

            drillingConfigPanel.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), SECOND_TAB), DrillingGroup.DASHBOARDS.getName());
            assertEquals(drillingConfigPanel.getSettingsOnLastItemPanel(), Pair.of(ATTR_PRODUCT, SECOND_TAB));
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name);
            tab.addItems(items);
        }).build();
    }

    private Dashboard initDashboard() {
        Tab firstTab = initDashboardTab(FIRST_TAB, singletonList(createReportItem(reportUri)));
        Tab scecondTab = initDashboardTab(SECOND_TAB, singletonList(createReportItem(reportUri)));
        Tab thirdTab = initDashboardTab(THIRD_TAB, singletonList(createReportItem(reportUri)));

        return  Builder.of(Dashboard::new).with((Dashboard dash) -> {
            dash.setName("Dashboard " + generateHashString());
            dash.addTab(firstTab);
            dash.addTab(scecondTab);
            dash.addTab(thirdTab);
        }).build();
    }

    private void addDrillSettingsToLatestReport(DrillSetting setting) {
        report = dashboardsPage.getContent()
                .getLatestReport(TableReport.class)
                .waitForLoaded();

        dashboardsPage.editDashboard();

        widgetConfigPanel = WidgetConfigPanel
                .openConfigurationPanelFor(report.getRoot(), browser);
        drillingConfigPanel = widgetConfigPanel
                .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);

        drillingConfigPanel.addDrilling(setting.getValuesAsPair(), setting.getGroup());
        if (!setting.getInnerDrillSetting().isEmpty()) {
            setting.getInnerDrillSetting().forEach(innerSetting ->
                    drillingConfigPanel.addInnerDrillToLastItemPanel(innerSetting.getValuesAsPair()));
        }
    }

    private class DrillSetting {
        List<String> leftValues;
        String rightValue;
        String group;
        List<DrillSetting> innerDrillSetting;

        private DrillSetting(List<String> leftValues, String rightValue, String group) {
            this.leftValues = leftValues;
            this.rightValue = rightValue;
            this.group = group;

            innerDrillSetting = new ArrayList<>();
        }

        private String getGroup() {
            return group;
        }

        private List<DrillSetting> getInnerDrillSetting() {
            return innerDrillSetting;
        }

        private Pair<List<String>, String> getValuesAsPair() {
            return Pair.of(leftValues, rightValue);
        }
    }
}
