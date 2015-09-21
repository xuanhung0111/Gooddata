package com.gooddata.qa.graphene.fragments.projects;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final By BY_PROJECT_TITLE = By.cssSelector(".projectTitle");

    public List<WebElement> getProjectsElements() {
        return projects;
    }

    public List<WebElement> getDemoProjectsElements() {
        return demoProjects;
    }

    public List<String> getProjectsIds() {
        return getProjectsIds("");
    }

    public List<String> getProjectsIds(String projectSubstringFilter) {
        return Stream.concat(demoProjects.stream(), projects.stream())
                .filter(e -> e.findElement(BY_PROJECT_TITLE).getText().contains(projectSubstringFilter))
                .map(this::getProjectIdFrom)
                .collect(Collectors.toList());
    }

    public void goToProject(final String projectId) {
        Iterables.find(Iterables.concat(demoProjects, projects), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement project) {
                return projectId.equals(getProjectIdFrom(project));
            }
        }).click();
    }

    public String getProjectNameFrom(String projectId) {
        return projects.stream()
                .filter(e -> e.getAttribute("gdc:link").contains(projectId))
                .map(e -> e.findElement(By.cssSelector(".projectTitle")))
                .map(WebElement::getText)
                .findFirst()
                .get();
    }

    public boolean isProjectDisplayed(String projectId) {
        return projects.stream()
                .filter(e -> e.getAttribute("gdc:link").contains(projectId))
                .findFirst()
                .isPresent();
    }

    private String getProjectIdFrom(WebElement project) {
        String gdcLink = project.getAttribute("gdc:link");
        return gdcLink.substring(gdcLink.lastIndexOf("/") + 1);
    }
}
