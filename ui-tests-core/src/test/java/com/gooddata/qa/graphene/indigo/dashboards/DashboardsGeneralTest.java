package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.base.Predicate;

public class DashboardsGeneralTest extends GoodSalesAbstractTest {

    private Kpi selectedKpi;
    private String selectedKpiDataHeadline;
    private String selectedKpiDataValue;

    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    private static final String NUMBER_OF_ACTIVITIES_URI = "/gdc/md/%s/obj/14636";
    private static final String AMOUNT = "Amount";
    private static final String LOST = "Lost";

    private static final String TEST_HEADLINE = "Test headline";

    @BeforeClass
    public void before() throws InterruptedException {
        addUsersWithOtherRoles = true;
    }

    @Test
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            logout();
            openUrl(PAGE_INDIGO_DASHBOARDS);
            Graphene.waitGui().until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver browser) {
                    return browser.getCurrentUrl().contains(ACCOUNT_PAGE);
                }
            });
        } finally {
            loginFragment.login(testParams.getUser(), testParams.getPassword(), true);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initDashboardTests() {
        initDashboardsPage();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void kpisLoadedCheck() {
        initIndigoDashboardsPage();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkEditModeCancelNoChanges() {
        processKpiSelection(0);

        indigoDashboardsPage.cancelEditMode();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
        assertEquals(selectedKpi.getValue(), selectedKpiDataValue);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndDiscard() {
        processKpiSelection(0);

        selectedKpi.setHeadline(TEST_HEADLINE);

        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .submitClick();

        assertEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndAbortCancel() {
        processKpiSelection(0);

        selectedKpi.setHeadline(TEST_HEADLINE);

        assertTrue(selectedKpi.getHeadline().equals(TEST_HEADLINE));

        indigoDashboardsPage
                .cancelEditMode()
                .waitForDialog()
                .cancelClick();

        assertTrue(selectedKpi.getHeadline().equals(TEST_HEADLINE));
        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeAndSave() {
        processKpiSelection(0);

        selectedKpi.setHeadline(TEST_HEADLINE);

        assertNotEquals(selectedKpi.getHeadline(), selectedKpiDataHeadline);

        indigoDashboardsPage.saveEditMode();

        assertEquals(selectedKpi.getHeadline(), TEST_HEADLINE);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitleChangeWhenMetricChange() {
        processKpiSelection(1);

        indigoDashboardsPage.selectMetricByName(AMOUNT);

        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, AMOUNT);

        indigoDashboardsPage.selectMetricByName(LOST);

        assertEquals(selectedKpi.getHeadline(), LOST);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"adminTests"})
    public void checkKpiTitlePersistenceWhenMetricChange() {
        processKpiSelection(0);

        indigoDashboardsPage.selectMetricByName(AMOUNT);

        selectedKpi.setHeadline("abc");
        String metricHeadline = selectedKpi.getHeadline();

        assertEquals(metricHeadline, "abc");

        indigoDashboardsPage.selectMetricByName(AMOUNT);

        assertEquals(selectedKpi.getHeadline(), "abc");
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void checkViewerCannotEditDashboard() throws JSONException, InterruptedException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.VIEWER);

            initDashboardsPage();
            initIndigoDashboardsPage();

            assertFalse(indigoDashboardsPage.checkIfEditButtonIsPresent());
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"userTests"})
    public void checkEditorCanEditDashboard() throws JSONException, InterruptedException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initDashboardsPage();
            initIndigoDashboardsPage();

            assertTrue(indigoDashboardsPage.checkIfEditButtonIsPresent());
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "formattingProvider")
    public Object[][] formattingProvider() {
        return new Object[][] {
                {Formatter.BARS},
                {Formatter.DEFAULT},
                {Formatter.COLORS},
                {Formatter.TRUNCATE_NUMBERS},
                {Formatter.UTF_8}
        };
    }

    @Test(dependsOnMethods = {"createProject"}, dataProvider = "formattingProvider")
    public void testCustomMetricFormatting(Formatter format) throws ParseException, JSONException, IOException {
        String screenshot = "testCustomMetricFormatting-" + format.name();
        String uri = format(NUMBER_OF_ACTIVITIES_URI, testParams.getProjectId());
        initMetricPage();
        waitForFragmentVisible(metricEditorPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        try {
            switch (format) {
                case DEFAULT:
                    RestUtils.changeMetricFormat(getRestApiClient(), uri, "GDC#,##0.00");
                    initIndigoDashboardsPage();
                    takeScreenshot(browser, screenshot, getClass());
                    assertEquals(indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES), "GDC154,271.00");
                    return;
                case BARS:
                    waitForFragmentVisible(metricDetailPage).changeMetricFormat(format);
                    initIndigoDashboardsPage();
                    takeScreenshot(browser, screenshot, getClass());
                    assertTrue(format.toString().contains(
                            indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES)));
                    return;
                case TRUNCATE_NUMBERS:
                    waitForFragmentVisible(metricDetailPage).changeMetricFormat(format);
                    initIndigoDashboardsPage();
                    takeScreenshot(browser, screenshot, getClass());
                    assertEquals(indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES), "$154.3 K");
                    return;
                case COLORS:
                    waitForFragmentVisible(metricDetailPage).changeMetricFormat(format);
                    initIndigoDashboardsPage();
                    takeScreenshot(browser, screenshot, getClass());
                    assertEquals(indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES), "$154,271");
                    return;
                default:
                    RestUtils.changeMetricFormat(getRestApiClient(), uri, format.toString());
                    initIndigoDashboardsPage();
                    takeScreenshot(browser, screenshot, getClass());
                    assertEquals(indigoDashboardsPage.getValueFromKpi(NUMBER_OF_ACTIVITIES), format.toString());
            }
        } finally {
            initMetricPage();
            waitForFragmentVisible(metricEditorPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
            waitForFragmentVisible(metricDetailPage).changeMetricFormat(Formatter.DEFAULT);
            assertEquals(metricDetailPage.getMetricFormat(), Formatter.DEFAULT.toString());
        }
    }

    private Kpi processKpiSelection(int index) {
        selectedKpi = initIndigoDashboardsPage()
                .switchToEditMode()
                .selectKpi(index);

        selectedKpiDataHeadline = selectedKpi.getHeadline();
        selectedKpiDataValue = selectedKpi.getValue();

        return selectedKpi;
    }
}
