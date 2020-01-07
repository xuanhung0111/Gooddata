package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class WorkspaceContainer extends AbstractFragment {

    @FindBy(className = "workspace-item")
    private List<WebElement> workspaceItems;

    public void gotoProject(final String projectId) {
        WebElement element = workspaceItems.stream().filter(e -> e.getAttribute("gdc:project_id").equals(projectId))
                .findFirst().orElseThrow(() -> new RuntimeException("Cannot find any workspace for project_id:" + projectId));
        element.click();
    }
}
