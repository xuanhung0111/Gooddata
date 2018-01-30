package com.gooddata.qa.graphene.fragments.disc.overview;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.Integer.parseInt;

import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.WaitUtils;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewProjects.OverviewProjectItem;

public class OverviewPage extends AbstractFragment {

    public static final String URI = "admin/disc/#/overview";

    @FindBy(className = "overview-projects")
    private OverviewProjects overviewProjects;

    @FindBy(className = "overview")
    private WebElement overviewSection;

    public OverviewPage selectState(OverviewState state) {
        waitForElementVisible(state.getLocator(), getRoot()).click();
        waitForPageLoaded();
        return this;
    }

    public boolean isStateActive(OverviewState state) {
        return waitForElementVisible(state.getLocator(), getRoot()).getAttribute("class").contains("active");
    }

    public boolean containsAnyOverviewState() {
        return ElementUtils.isElementPresent(By.className("overview-state"), overviewSection);
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
        Function<WebDriver, Boolean> executionFinish = browser -> refreshStateNumber(OverviewState.SCHEDULED) == 0 &&
                refreshStateNumber(OverviewState.RUNNING) == 0;

        Graphene.waitGui()
                .withTimeout(3, TimeUnit.MINUTES)
                .pollingEvery(5, TimeUnit.SECONDS)
                .until(executionFinish);

        return this;
    }

    public OverviewPage waitForPageLoaded() {
        Function<WebDriver, Boolean> pageLoaded = browser -> getRoot().findElements(By.cssSelector("[class*='loading']")).size() == 0;

        Graphene.waitGui().until(pageLoaded);
        return this;
    }

    public String getOverviewEmptyStateTitle() {
        return waitForElementVisible(overviewSection).findElement(By.className("title")).getText();
    }

    public String getOverviewEmptyStateMessage() {
        return waitForElementVisible(overviewSection).findElement(By.className("message")).getText();
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
