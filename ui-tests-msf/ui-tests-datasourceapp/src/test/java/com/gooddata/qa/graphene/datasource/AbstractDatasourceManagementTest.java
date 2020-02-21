package com.gooddata.qa.graphene.datasource;

import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractDataIntegrationTest;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static org.openqa.selenium.By.className;

public class AbstractDatasourceManagementTest extends AbstractDataIntegrationTest {

    protected DataSourceManagementPage initDatasourceManagementPage() {
        openUrl(DataSourceManagementPage.URI);
        return DataSourceManagementPage.getInstance(browser);
    }

    protected void waitForLoadingMenuBar() {
        final By loadingMenu = className("navigation");
        try {
            Function<WebDriver, Boolean> isLoadingMenuPresent = browser -> isElementPresent(loadingMenu, browser);
            Graphene.waitGui().withTimeout(2, TimeUnit.SECONDS).until(isLoadingMenuPresent);
        } catch (TimeoutException e) {
            //do nothing
        }

        waitForElementNotPresent(loadingMenu);
    }

    protected void setupMaql(String maql, Project project) {
        getAdminRestClient().getModelService().updateProjectModel(project, maql).get();
    }

    protected void addUserToSpecificProject(String email, UserRoles userRole, String projectId) throws IOException {
        final String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(new RestClient(
                new RestClient.RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true)),
                projectId);
        userManagementRestRequest.addUserToProject(email, userRole);
    }
}
