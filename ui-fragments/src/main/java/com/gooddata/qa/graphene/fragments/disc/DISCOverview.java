package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCOverviewProjectStates;
import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class DISCOverview extends DISCOverviewProjects {

	@FindBy(css = ".ait-overview-field-failed .ait-overview-state")
	private WebElement failedState;

	@FindBy(css = ".ait-overview-field-failed .ait-overview-state-count")
	private WebElement failedStateNumber;

	@FindBy(css = ".ait-overview-field-running .ait-overview-state")
	private WebElement runningState;

	@FindBy(css = ".ait-overview-field-running .ait-overview-state-count")
	private WebElement runningStateNumber;

	@FindBy(css = ".ait-overview-field-scheduled .ait-overview-state")
	private WebElement scheduledState;

	@FindBy(css = ".ait-overview-field-scheduled .ait-overview-state-count")
	private WebElement scheduledStateNumber;

	@FindBy(css = ".ait-overview-field-successful .ait-overview-state")
	private WebElement successfulState;

	@FindBy(css = ".ait-overview-field-successful .ait-overview-state-count")
	private WebElement successfulStateNumber;

	@FindBy(css = ".s-btn-discard")
	private WebElement discardButton;

	public void waitForStateNumber(WebElement stateNumber) throws InterruptedException {
		for (int i = 0; i < 5 && stateNumber.getText().isEmpty(); i++)
			Thread.sleep(1000);
	}

	public void selectOverviewState(DISCOverviewProjectStates state) throws InterruptedException {
		switch (state) {
		case FAILED:
			waitForElementVisible(failedState).click();
			Thread.sleep(1000);
			waitForStateNumber(failedStateNumber);
			break;
		case RUNNING:
			waitForElementVisible(runningState).click();
			Thread.sleep(1000);
			waitForStateNumber(runningStateNumber);
			break;
		case SCHEDULED:
			waitForElementVisible(scheduledState).click();
			Thread.sleep(1000);
			waitForStateNumber(scheduledStateNumber);
			break;
		case SUCCESSFUL:
			waitForElementVisible(successfulState).click();
			Thread.sleep(1000);
			waitForStateNumber(successfulStateNumber);
			break;
		}
	}

	public String getFailedState() {
		return waitForElementVisible(failedState).getText();
	}

	public String getFailedStateNumber() throws InterruptedException {
		waitForElementVisible(failedStateNumber);
		waitForStateNumber(failedStateNumber);
		return failedStateNumber.getText();
	}

	public String getRunningState() {
		return waitForElementVisible(runningState).getText();
	}

	public String getRunningStateNumber() throws InterruptedException {
		waitForElementVisible(runningStateNumber);
		waitForStateNumber(runningStateNumber);
		return runningStateNumber.getText();
	}

	public String getScheduledState() {
		return waitForElementVisible(scheduledState).getText();
	}

	public String getScheduledStateNumber() throws InterruptedException {
		waitForElementVisible(scheduledStateNumber);
		waitForStateNumber(scheduledStateNumber);
		return scheduledStateNumber.getText();
	}

	public String getSuccessfulState() {
		return waitForElementVisible(successfulState).getText();
	}

	public String getSuccessfulStateNumber() throws InterruptedException {
		waitForElementVisible(successfulStateNumber);
		waitForStateNumber(successfulStateNumber);
		return successfulStateNumber.getText();
	}

	public boolean assertOverviewStateNumber(DISCOverviewProjectStates state, int number)
			throws InterruptedException {
		switch (state) {
		case FAILED:
			assertTrue(state.getOption().equalsIgnoreCase(getFailedState()));
			return getFailedStateNumber().equals(String.valueOf(number));
		case RUNNING:
			assertTrue(state.getOption().equalsIgnoreCase(getRunningState()));
			return getRunningStateNumber().equals(String.valueOf(number));
		case SCHEDULED:
			assertTrue(state.getOption().equalsIgnoreCase(getScheduledState()));
			return getScheduledStateNumber().equals(String.valueOf(number));
		case SUCCESSFUL:
			assertTrue(state.getOption().equalsIgnoreCase(getSuccessfulState()));
			return getSuccessfulStateNumber().equals(String.valueOf(number));
		}
		return false;
	}
}
