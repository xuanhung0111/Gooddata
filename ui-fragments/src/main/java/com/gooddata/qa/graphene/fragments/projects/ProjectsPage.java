package com.gooddata.qa.graphene.fragments.projects;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ProjectsPage extends AbstractFragment {

    @FindBy(xpath = "//ul[@id='myProjects']/li")
    private List<WebElement> projects;

    @FindBy(xpath = "//ul[@id='demoProjects']/li")
    private List<WebElement> demoProjects;

    private static final By BY_SPAN_PROJECT_TITLE = By.xpath("span[@class='projectTitle']");

    public List<WebElement> getProjectsElements() {
        return projects;
    }

    public List<WebElement> getDemoProjectsElements() {
        return demoProjects;
    }

    public List<String> getProjectsIds() {
        return getProjectsIds(null);
    }

    public List<String> getProjectsIds(String projectSubstringFilter) {
        List<String> projectIds = new ArrayList<String>();
        boolean filter = projectSubstringFilter != null && projectSubstringFilter.length() > 0;
        for (WebElement elem : projects) {
            if (filter) {
                if (!elem.findElement(BY_SPAN_PROJECT_TITLE).getText()
                        .contains(projectSubstringFilter))
                    continue;
            }
            projectIds.add(getProjectIdFrom(elem));
        }
        return projectIds;
    }

    public void goToProject(final String projectId) {
        Iterables.find(Iterables.concat(demoProjects, projects), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement project) {
                return projectId.equals(getProjectIdFrom(project));
            }
        }).click();
    }

    private String getProjectIdFrom(WebElement project) {
        String gdcLink = project.getAttribute("gdc:link");
        return gdcLink.substring(gdcLink.lastIndexOf("/") + 1);
    }
}
