package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.google.common.base.Predicate;

public class __ProjectDetailPage extends AbstractFragment {

    @FindBy(className = "ait-project-title")
    private WebElement title;

    @FindBy(css = "[class*='project-metadata'] .metadata-value")
    private WebElement projectIdMetadata;

    @FindBy(className = "action-link-with-icon")
    private WebElement goToDashboardsLink;

    @FindBy(css = ".empty-state .title")
    private WebElement emptyStateTitle;

    @FindBy(css = ".empty-state .message")
    private WebElement emptyStateMessage;

    @FindBy(className = "process-detail")
    private Collection<ProcessDetail> processes;

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public String getProjectIdMetadata() {
        return waitForElementVisible(projectIdMetadata).getText();
    }

    public void goToDashboards() {
        waitForElementVisible(goToDashboardsLink).click();
    }

    public String getEmptyStateTitle() {
        return waitForElementVisible(emptyStateTitle).getText();
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyStateMessage).getText();
    }

    public ProcessDetail getProcess(String processId) {
        return findProcess(processId).get();
    }

    public boolean hasProcess(String processId) {
        return findProcess(processId).isPresent();
    }

    public void downloadProcess(String processId) {
        getProcess(processId).downloadProcess();
    }

    public __ProjectDetailPage deleteProcess(String processId) {
        int processNumber = processes.size();
        getProcess(processId).deleteProcess();

        Predicate<WebDriver> processDeleted = browser -> processes.size() == processNumber - 1;
        Graphene.waitGui().until(processDeleted);
        return this;
    }

    public Collection<String> getProcessNames() {
        return processes.stream().map(p -> p.getTitle()).collect(toList());
    }

    private Optional<ProcessDetail> findProcess(String processId) {
        return processes.stream().filter(p -> isElementPresent(By.id(processId), p.getRoot())).findFirst();
    }
}
