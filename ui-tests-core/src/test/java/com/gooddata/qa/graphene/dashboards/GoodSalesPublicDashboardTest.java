package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static java.nio.file.Files.deleteIfExists;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

public class GoodSalesPublicDashboardTest extends GoodSalesAbstractTest {

    private List<String> tabs = asList("Outlook", "What's Changed", "Waterfall Analysis",
        "Leaderboards", "Activities", "Sales Velocity", "Quarterly Trends", "Seasonality", "");
    private String selectedTab = "Outlook";
    private static final String STAGING3 = "staging3.intgdc.com";
    private static final String STAGING2 = "staging2.intgdc.com";
    private static final String STAGING = "staging.intgdc.com";
    private static final long EXPECTED_EXPORT_DASHBOARD_SIZE = 60000L;
    private String publicDashboardUri;


    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-Public-dashboard-test";
    }

    @Test(dependsOnGroups = "createProject")
    public void initPublicDashboard() {
        switch(testParams.getHost()) {
            case STAGING3:
                publicDashboardUri = "https://staging3.intgdc.com/dashboard.html#project=" +
                    "/gdc/projects/lkyocr2gnh6mcnet55rgea1izt1nvena&dashboard=/gdc/md/lkyocr2gnh6mcnet55rgea1izt1nvena/obj/916" +
                    "&tab=adzD7xEmdhTx&publicAccessCode=6XI7rqQ7T42Hp9mi";
                return;
            case STAGING2:
                publicDashboardUri = "https://staging2.intgdc.com/dashboard.html#project=" +
                    "/gdc/projects/hy0dsfjsodowczug6ywq4rkq4hhjhl0b&dashboard=/gdc/md/hy0dsfjsodowczug6ywq4rkq4hhjhl0b/obj/916" +
                    "&tab=adzD7xEmdhTx&publicAccessCode=N5G3XyRMfXffHL6d";
                return;
            case STAGING:
                publicDashboardUri = "https://staging.intgdc.com/dashboard.html#project=" +
                    "/gdc/projects/t0uyev2dbuvbdwfdyovzuuosduhyg6dw&dashboard=/gdc/md/t0uyev2dbuvbdwfdyovzuuosduhyg6dw/obj/21284" +
                    "&tab=a0b4ce17cec4&publicAccessCode=2E2jmianWW27Q1gk";
                tabs = asList("Executive Overview", "Marketing Contribution", "Sales Forecast", "Create Insights");
                selectedTab = "Executive Overview";
                return;
            default:
                System.out.println("Test just runs on staging, staging2 and staging3");
        }
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void viewPublicDashboardWithoutLoggedUser() {
        if (!isOnStagingCluster())
            return;
        logout();
        try {
            openLink(publicDashboardUri);
            waitForDashboardPageLoaded(browser);
            Screenshots.takeScreenshot(browser, "View_Public_Dashboard_WithOut_Logged_User", getClass());
            assertEquals(dashboardsPage.getTabs().getAllTabNames(), tabs);
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), selectedTab);
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void embedPublicDashboardInWebContent() {
        if (!isOnStagingCluster())
            return;
        initDashboardsPage().addNewDashboard("Embed Dashboard").editDashboard()
            .addWebContentToDashboard(publicDashboardUri).saveDashboard();
        DashboardTabs dashboardTabs = dashboardsPage.getLastEmbeddedWidget().getEmbeddedDashboard().getTabs();
        Screenshots.takeScreenshot(browser, "Embed_Public_Dashboard_In_Web_Content", getClass());
        assertEquals(dashboardTabs.getAllTabNames().size(), tabs.size());
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void tryAccessProjectHTML() {
        if (!isOnStagingCluster())
            return;
        logout();
        try {
            openLink(publicDashboardUri);
            waitForDashboardPageLoaded(browser);
            assertEquals(dashboardsPage.getTabs().getAllTabNames(), tabs);
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), selectedTab);

            openUrl(PAGE_PROJECTS);
            LoginFragment.waitForPageLoaded(browser);
            Screenshots.takeScreenshot(browser, "Try_Access_Project_HTML", getClass());
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void tryWithInvalidPublicAccessCode() {
        if (!isOnStagingCluster())
            return;
        String wrongUri = publicDashboardUri + "Wrong";
        logout();
        try {
            openLink(wrongUri);
            LoginFragment.waitForPageLoaded(browser);
            Screenshots.takeScreenshot(browser, "Try_With_Invalid_Public_Access_Code", getClass());
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void viewPublicDashboardByInAccessUser() {
        if (!isOnStagingCluster())
            return;
        openLink(publicDashboardUri);
        waitForDashboardPageLoaded(browser);
        Screenshots.takeScreenshot(browser, "View_Public_Dashboard_By_In_Access_User", getClass());
        assertEquals(dashboardsPage.getTabs().getAllTabNames(), tabs);
        assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), selectedTab);
        assertEquals(initProjectsAndUsersPage().getUserRole(testParams.getUser()), UserRoles.ADMIN.getName());
    }

    @Test(dependsOnMethods = "initPublicDashboard")
    public void printPublicDashboard() throws IOException {
        if (!isOnStagingCluster())
            return;
        logout();
        openLink(publicDashboardUri);
        waitForDashboardPageLoaded(browser);
        String exportedDashboardName = dashboardsPage.printDashboardTab(0);
        try {
            verifyDashboardExport(exportedDashboardName, selectedTab, EXPECTED_EXPORT_DASHBOARD_SIZE);
            checkRedBar(browser);
        } finally {
            deleteIfExists(Paths.get(getExportFilePath(exportedDashboardName, ExportFormat.PDF)));
            signIn(true, UserRoles.ADMIN);
        }
    }

    private String getExportFilePath(String name, ExportFormat format) {
        return testParams.getDownloadFolder() + testParams.getFolderSeparator() + name + "." + format.getName();
    }

    private void openLink(String uri) {
        browser.get(uri);
        System.out.println("Loading page ... " + uri);
    }

    private boolean isOnStagingCluster() {
        if (testParams.getHost().equals(STAGING3) || testParams.getHost().equals(STAGING2) ||
                testParams.getHost().equals(STAGING)) {
            return true;
        }
        System.out.println("Test just runs on Staging, Staging2 and Staging3. Skip and mark as passed test");
        return false;
    }
}
