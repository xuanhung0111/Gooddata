package com.gooddata.qa.graphene.fragments.projects;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

import java.util.List;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ProjectsPage extends AbstractFragment {

    @FindBy(xpath = "//ul[@id='myProjects']/li")
    private List<WebElement> projects;

    @FindBy(css = ".projectListSearch .gdc-input")
    private WebElement searchTextbox;

    public static final ProjectsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(ProjectsPage.class, waitForElementVisible(id("projectsCentral"), context));
    }

    public void goToProject(String nameOrId) {
        getProjectItem(nameOrId).open();
    }

    public boolean isProjectDisplayed(String nameOrId) {
        return searchProject(nameOrId).getProjectElementInViewport(nameOrId).isPresent();
    }

    public String getAlertMessage() {
        return waitForElementVisible(By.cssSelector(".info.messageAlert[style*='display: block']"), getRoot())
                .getText();
    }

    /**
     * The method handles projects paging on project html page. Please note that
     * search is available only when having more than 25 projects.
     *
     * please use search text wisely because it could be a potential risk when the result contains projects paging
     * @param nameOrId
     * @return
     */
    public ProjectsPage searchProject(String nameOrId) {
        if (isElementVisible(searchTextbox)) {
            searchTextbox.clear();
            searchTextbox.sendKeys(nameOrId);

            waitForElementNotVisible(id("spinner"));
        }

        return this;
    }

    public ProjectItem getProjectItem(String nameOrId) {
        WebElement foundElement = searchProject(nameOrId).getProjectElementInViewport(nameOrId)
                .orElseThrow(() -> new NoSuchElementException("There is no project named " + nameOrId));

        return Graphene.createPageFragment(ProjectItem.class, waitForElementVisible(foundElement));
    }

    public int getProjectsInViewPortCount() {
        return projects.size();
    }

    private Optional<WebElement> getProjectElementInViewport(String nameOrId) {
        return projects.stream()
                .filter(e -> nameOrId.equals(e.findElement(className("projectTitle")).getText())
                        || e.getAttribute("gdc:link").contains(nameOrId))
                .findFirst();
    }

    public class ProjectItem extends AbstractFragment {

        public void leave() {
            waitForElementVisible(className("leaveProject"), getRoot()).click();
            PopupDialog.getInstance(browser).leaveProject();
        }

        public void open() {
            waitForElementVisible(className("projectTitle"), getRoot()).click();
        }

        public String getId() {
            String gdcLink = getRoot().getAttribute("gdc:link");
            return gdcLink.substring(gdcLink.lastIndexOf("/") + 1);
        }
    }
}
