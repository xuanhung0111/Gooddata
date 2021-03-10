package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.AbstractDataIntegrationTest;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;

import com.gooddata.qa.graphene.fragments.modeler.Layout;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import com.gooddata.qa.graphene.fragments.modeler.ToolBar;
import com.gooddata.qa.graphene.fragments.modeler.ViewMode;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;

public class AbstractLDMPageTest extends AbstractDataIntegrationTest {

    @Override
    protected void initProperties() {
        String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        testParams.setDomainUser(domainUser);
    }

    public LogicalDataModelPage openViewModeLDMPage() {
        openUrl(LogicalDataModelPage.getUri(testParams.getProjectId()));
        return LogicalDataModelPage.getInstance(browser);
    }

    public LogicalDataModelPage initLogicalDataModelPage() {
        openUrl(LogicalDataModelPage.getUri(testParams.getProjectId()));
        switchModelPageToViewMode();
        return LogicalDataModelPage.getInstance(browser);
    }

    public void switchToEditMode() {
            ToolBar.getInstance(browser).clickEditBtn();
    }

    public LogicalDataModelPage initLogicalDataModelPageByPID(String pid) {
        openUrl(LogicalDataModelPage.getUri(pid));
        switchModelPageToViewMode();
        return LogicalDataModelPage.getInstance(browser);
    }

    public void switchModelPageToViewMode() {
        sleepTightInSeconds(6);
        // make sure that is view mode initial
        if (Layout.getInstance(browser).isInitialPagePresent()) {
            ViewMode.getInstance(browser).clickButtonChangeToEditMode();
        }
        if (!isElementPresent(By.className("gdc-ldm-sidebar"), browser)) {
            switchToEditMode();
        }
    }

    public LogicalDataModelPage initDashboardIgnoreAlert() {
        try {
            return  initLogicalDataModelPage();
        } catch (Exception handleAlert) {
            browser.navigate().refresh();
            browser.switchTo().alert().accept();
            browser.switchTo().defaultContent();
            return initLogicalDataModelPage();
        }
    }

    public ProjectDetailPage initDISCIgnoreAlert(String projectId) {
        try {
            return  initDiscProjectDetailPage(projectId);
        } catch (Exception handleAlert) {
            browser.navigate().refresh();
            browser.switchTo().alert().accept();
            browser.switchTo().defaultContent();
            return initDiscProjectDetailPage(projectId);
        }
    }

    protected DataSourceManagementPage initDatasourceManagementPage() {
        try {
            openUrl(DataSourceManagementPage.URI);
            return DataSourceManagementPage.getInstance(browser);
        } catch (TimeoutException timeout) {
            takeScreenshot(browser, "Datasource timeout", getClass());
            timeout.printStackTrace();
            browser.navigate().refresh();
        } finally {
            return DataSourceManagementPage.getInstance(browser);
        }
    }

    protected ProjectDetailPage initDiscProjectDetailPage(String id) {
        openUrl(format(ProjectDetailPage.URI, id));
        return waitForFragmentVisible(projectDetailPage);
    }

    protected void openEditMode() {
        initLogicalDataModelPage();
        ViewMode.getInstance(browser).clickButtonChangeToEditMode();
    }
}
