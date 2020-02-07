package com.gooddata.qa.graphene.growth;

import com.gooddata.qa.graphene.AbstractFreemiumGrowthTest;
import com.gooddata.qa.graphene.fragments.freegrowth.CreateWorkspaceDialog;
import com.gooddata.qa.graphene.fragments.freegrowth.GrowthLandingPage;
import com.gooddata.qa.graphene.fragments.freegrowth.WorkspaceHeader;
import com.gooddata.qa.graphene.utils.UrlParserUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicGrowthTest extends AbstractFreemiumGrowthTest {
    private String newWorkspaceName = "ATT_growth_new_ws_name_" + generateHashString();
    private String addWorkspaceName = "ATT_growth_add_ws_name_" + generateHashString();

    @Override
    public void initProperties() {
        maxProjects = 1;
        editionName = "Growth";
    }

    @Test(groups = {"landingPage"}, dependsOnMethods = {"verifyLandingPage"})
    public void createNewWorkSpace() {
        GrowthLandingPage.getGrowthInstance(browser).clickCreateWorkspaceButton();
        CreateWorkspaceDialog createWorkspaceDialog = CreateWorkspaceDialog.getInstance(browser);
        createWorkspaceDialog.createNewWorkspace(newWorkspaceName, testParams.getAuthorizationToken());
    }

    @Test(groups = {"landingPage"}, dependsOnMethods = {"verifyLandingPage"})
    public void gotoFirstProject() {
        String projectName = WorkspaceHeader.getInstance(browser).getCurrentProjectName();
        log.info(browser.getCurrentUrl());
        String projectId = UrlParserUtils.getProjectId(browser.getCurrentUrl());
        log.info("created projectId=" + projectId);
        createdProjects.add(Pair.of(projectId, newWorkspaceName));

        Assert.assertEquals(newWorkspaceName, projectName);
    }

    @Test(dependsOnMethods = {"testSignOutThenSignIn"})
    public void addNewWorkSpace() {
        openUrl(PAGE_PROJECTS);
        GrowthLandingPage growthLandingPage = GrowthLandingPage.getGrowthInstance(browser);
        growthLandingPage.clickCreateWorkspaceLink();

        CreateWorkspaceDialog createWorkspaceDialog = CreateWorkspaceDialog.getInstance(browser);
        createWorkspaceDialog.createNewWorkspace(addWorkspaceName, testParams.getAuthorizationToken());
    }
}
