package com.gooddata.qa.graphene.fragments.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
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

    public static final ProjectsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(ProjectsPage.class, waitForElementVisible(id("projectsCentral"), context));
    }

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

    public String getAlertMessage() {
        return waitForElementVisible(By.cssSelector(".info.messageAlert[style*='display: block']"), getRoot())
                .getText();
    }

    public ProjectsPage leaveProject(String projectId) {
        waitForElementVisible(findProject(getProjectNameFrom(projectId))).findElement(className("leaveProject")).click();
        getPopupDialog().leaveProject();

        return this;
    }

    public PopupDialog getPopupDialog() {
        return Graphene.createPageFragment(PopupDialog.class, waitForElementVisible(className
                ("t-infoMessageDialog"), browser));
    }

    private WebElement findProject(String name) {
        return projects.stream().filter(e -> name.equals(e.findElement(className("projectTitle")).getText()))
                .findFirst().get();
    }

    private String getProjectIdFrom(WebElement project) {
        String gdcLink = project.getAttribute("gdc:link");
        return gdcLink.substring(gdcLink.lastIndexOf("/") + 1);
    }

    public class PopupDialog extends AbstractFragment {
        @FindBy(className = "s-btn-leave")
        private WebElement leaveButton;

        public void leaveProject() {
            waitForElementVisible(leaveButton).click();
        }

        public String getMessage() {
            // getText() returns value contains child text, so it should be removed from error msg
            String returnString = waitForElementVisible(tagName("form"), getRoot()).getText();

            return returnString.substring(0, returnString.indexOf(waitForElementVisible(className("bd_controls"),
                    getRoot()).getText())).trim();
        }

        public void close() {
            waitForElementVisible(className("container-close"), getRoot()).click();
        }
    }
}
