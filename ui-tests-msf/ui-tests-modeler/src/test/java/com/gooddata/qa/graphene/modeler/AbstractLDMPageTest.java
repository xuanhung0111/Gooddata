package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.AbstractDataIntegrationTest;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import org.openqa.selenium.TimeoutException;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

public class AbstractLDMPageTest extends AbstractDataIntegrationTest {

    @Override
    protected void initProperties() {
        String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        testParams.setDomainUser(domainUser);
    }

    public LogicalDataModelPage initLogicalDataModelPage() {
        openUrl(LogicalDataModelPage.getUri(testParams.getProjectId()));
        return LogicalDataModelPage.getInstance(browser);
    }

    public LogicalDataModelPage initLogicalDataModelPageByPID(String pid) {
        openUrl(LogicalDataModelPage.getUri(pid));
        return LogicalDataModelPage.getInstance(browser);
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
}
