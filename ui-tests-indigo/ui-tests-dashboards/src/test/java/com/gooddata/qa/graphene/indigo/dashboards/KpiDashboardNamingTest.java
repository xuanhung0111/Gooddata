package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboardUri;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.openqa.selenium.Keys;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiDashboardNamingTest extends AbstractDashboardTest {

    private static final String DEFAULT_TITLE = "Untitled";
    private static final String INSIGHT_ACTIVITIES = "Insight Activities";
    private static final String KPI_AMOUNT = "Amount";

    private static final String DASHBOARD_NEW = "Dashboard New";

    private static final String DASHBOARD_ONE = "Dashboard One";
    private static final String DASHBOARD_TWO = "Dashboard Two";
    private static final String DASHBOARD_THREE = "Dashboard Three";
    private static final String DASHBOARD_FOUR = "Dashboard Four";
    private static final String DASHBOARD_FIVE = "Dashboard Five";
    private static final String DASHBOARD_SIX = "Dashboard Six";

    private static final String DASHBOARD_ORIGINAL = "Dashboard Original";
    private static final String DASHBOARD_DUPLICATE = "Dashboard Duplicate";

    private static final String DASHBOARD_CANCEL = "Dashboard Cancel";

    @Override
    protected void customizeProject() throws IOException, JSONException {

        for (String dashboardName : asList(DASHBOARD_ONE, DASHBOARD_TWO, DASHBOARD_THREE,
                DASHBOARD_FOUR, DASHBOARD_FIVE, DASHBOARD_SIX, DASHBOARD_ORIGINAL,
                DASHBOARD_DUPLICATE, DASHBOARD_CANCEL)) {
            createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                    singletonList(createAmountKpi()), dashboardName);
        }

        createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricCreator().createNumberOfActivitiesMetric()))));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createDefaultNameKpiDashboard() throws IOException, JSONException {

        // remove the dashboard with default title (if exists)
        try {
            deleteAnalyticalDashboard(getRestApiClient(), getAnalyticalDashboardUri(DEFAULT_TITLE,
                    getRestApiClient(), testParams.getProjectId()));
        } catch (Exception e) {
            // ignore, dashboard does not exist, right?
        }

        // create new dashboard with default name
        initIndigoDashboardsPageWithWidgets().addDashboard().addInsight(INSIGHT_ACTIVITIES)
                .saveEditModeWithWidgets();

        takeScreenshot(browser, "New-Dashboard-Default", getClass());

        try {
            validateInsightDashboard(DEFAULT_TITLE);
        } finally {
            // remove the dashboard with default title
            deleteAnalyticalDashboard(getRestApiClient(), getAnalyticalDashboardUri(DEFAULT_TITLE,
                    getRestApiClient(), testParams.getProjectId()));
        }

    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNamedKpiDashboard() throws IOException, JSONException {

        initIndigoDashboardsPageWithWidgets().addDashboard().changeDashboardTitle(DASHBOARD_NEW)
                .addInsight(INSIGHT_ACTIVITIES).saveEditModeWithWidgets();

        takeScreenshot(browser, "New-Dashboard-Named", getClass());

        validateInsightDashboard(DASHBOARD_NEW);
    }


    @Test(dependsOnGroups = {"createProject"})
    public void renameToDuplicateNameKpiDashboard() throws IOException, JSONException {

        renameKpiDashboard(DASHBOARD_ORIGINAL, DASHBOARD_DUPLICATE);

        long dashboardCount = indigoDashboardsPage.getDashboardTitles().stream()
                .filter(p -> DASHBOARD_DUPLICATE.equals(p)).count();

        takeScreenshot(browser, "Dashboard-Renamed-Duplicate", getClass());

        assertTrue(dashboardCount == 2, "Duplicate dashboard names shall be listed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelRenameExistingKpiDashboard() throws IOException, JSONException {

        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(DASHBOARD_CANCEL)
                .switchToEditMode().changeDashboardTitle("Cancel Rename")
                .cancelEditModeWithChanges();

        takeScreenshot(browser, "Cancel-Dashboard-Rename", getClass());

        validateDashboard(DASHBOARD_CANCEL);
    }

    @DataProvider(name = "renameKPIDashboard")
    public Object[][] dashboardNameProvider() {

        return new Object[][] {
                // rename existing KPI Dashboard to new name
                {DASHBOARD_ONE, "Amount New"},
                // rename into empty name - gets default name "Untitled"
                {DASHBOARD_TWO, ""},
                // rename into name with spaces - blank spaces before/after should be ignored
                {DASHBOARD_THREE, "  new with spaces  "},
                // special characters
                {DASHBOARD_FOUR, "@#$%^&*()âêûťžŠô"},
                // XSS injections + long name
                {DASHBOARD_FIVE,
                        "<b onmouseover=alert('Wufff!')>click me!</b></body></html><&nbsp;>"},
                {DASHBOARD_SIX, "<IMG SRC=j&#X41vascript:alert('test2')>"}};

    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "renameKPIDashboard")
    public void renameExistingKpiDashboard(String currentDashboardName, String newDashboardName)
            throws IOException, JSONException {

        renameKpiDashboard(currentDashboardName, newDashboardName);

    }

    private void renameKpiDashboard(String name, String newName) throws JSONException, IOException {
        String newNameValue = newName;
        if ("".equals(newName)) {
            newNameValue = "" + Keys.DELETE;
            newName = DEFAULT_TITLE;
        }

        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(name).switchToEditMode()
                .changeDashboardTitle(newNameValue).saveEditModeWithWidgets();

        takeScreenshot(browser, "Dashboard-Renamed-" + name.replace(" ", "-"), getClass());

        validateDashboard(newName);
    }

    private void validateDashboard(String name) throws JSONException, IOException {

        assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on view mode");

        String expectedName = name.trim().replaceAll(" +", " ");
        String displayedName = indigoDashboardsPage.getSelectedKpiDashboard();

        // validate just prefix/suffix for shortened name.
        if (displayedName.indexOf("…") != -1) {
            assertTrue(expectedName.startsWith(displayedName.split("…")[0]));
            assertTrue(expectedName.endsWith(displayedName.split("…")[1]));
        } else {
            assertEquals(displayedName, expectedName);
        }

        assertEquals(indigoDashboardsPage.getKpiTitles(), asList(KPI_AMOUNT));
        assertEquals(indigoDashboardsPage.getDashboardTitle(), expectedName);
        assertThat(browser.getCurrentUrl(),
                containsString(getKpiDashboardIdentifierByTitle(expectedName)));
    }

    private void validateInsightDashboard(String dashboardName) throws JSONException, IOException {
        assertEquals(indigoDashboardsPage.getDashboardTitle(), dashboardName);
        assertFalse(indigoDashboardsPage.isOnEditMode(), "Should be on view mode");
        assertEquals(indigoDashboardsPage.getSelectedKpiDashboard(), dashboardName);
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(INSIGHT_ACTIVITIES));
        assertThat(browser.getCurrentUrl(),
                containsString(getKpiDashboardIdentifierByTitle(dashboardName)));
    }

}
