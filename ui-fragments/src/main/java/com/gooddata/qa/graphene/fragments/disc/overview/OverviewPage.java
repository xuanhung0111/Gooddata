package com.gooddata.qa.graphene.fragments.disc.overview;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.Integer.parseInt;

import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewProjects.OverviewProjectItem;
import com.google.common.base.Predicate;

public class OverviewPage extends AbstractFragment {

    public static final String URI = "admin/disc/#/overview";

    @FindBy(className = "overview-projects")
    private OverviewProjects overviewProjects;

    public OverviewPage selectState(OverviewState state) {
        waitForElementVisible(state.getLocator(), getRoot()).click();
        waitForPageLoaded();
        return this;
    }

    public boolean isStateActive(OverviewState state) {
        return waitForElementVisible(state.getLocator(), getRoot()).getAttribute("class").contains("active");
    }

    public int getStateNumber(OverviewState state) {
        return parseInt(waitForElementVisible(state.getLocator(), getRoot())
                .findElement(By.className("overview-state-count"))
                .getText());
    }

    public String getEmptyStateMessage() {
        return waitForFragmentVisible(overviewProjects).getEmptyStateMessage();
    }

    public OverviewProjectItem getOverviewProject(String title) {
        return waitForFragmentVisible(overviewProjects).getProject(title);
    }

    public OverviewPage markOnProject(String title) {
        waitForFragmentVisible(overviewProjects).markOnProject(title);
        return this;
    }

    public OverviewPage disableScheduleExecution() {
        waitForFragmentVisible(overviewProjects).disableScheduleExecution();
        return this;
    }

    public OverviewPage restartScheduleExecution() {
        waitForFragmentVisible(overviewProjects).restartScheduleExecution();
        return this;
    }

    public OverviewPage executeSchedule() {
        waitForFragmentVisible(overviewProjects).executeSchedule();
        return this;
    }

    public OverviewPage stopScheduleExecution() {
        waitForFragmentVisible(overviewProjects).stopScheduleExecution();
        return this;
    }

    public OverviewPage waitForExecutionFinish() {
        Predicate<WebDriver> executionFinish = browser -> refreshStateNumber(OverviewState.SCHEDULED) == 0 &&
                refreshStateNumber(OverviewState.RUNNING) == 0;

        Graphene.waitGui()
                .withTimeout(3, TimeUnit.MINUTES)
                .pollingEvery(5, TimeUnit.SECONDS)
                .until(executionFinish);

        return this;
    }

    public OverviewPage waitForPageLoaded() {
        Predicate<WebDriver> pageLoaded = browser -> !waitForFragmentVisible(overviewProjects)
                .getRoot().getAttribute("class").contains("loading");

        Graphene.waitGui().until(pageLoaded);
        return this;
    }

    private int refreshStateNumber(OverviewState state) {
        return selectState(state).getStateNumber(state);
    }

    public enum OverviewState {
        FAILED("ait-overview-field-failed"),
        RUNNING("ait-overview-field-running"),
        SCHEDULED("ait-overview-field-scheduled"),
        SUCCESSFUL("ait-overview-field-successful");

        private String locator;

        private OverviewState(String locator) {
            this.locator = locator;
        }

        private By getLocator() {
            return By.className(locator);
        }
    }
}
