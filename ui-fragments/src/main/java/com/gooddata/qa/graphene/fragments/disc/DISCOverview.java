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
	
	public String getState(DISCOverviewProjectStates state) {
		switch (state) {
		case FAILED:
			return waitForElementVisible(failedState).getText();
		case RUNNING:
			return waitForElementVisible(runningState).getText();
		case SCHEDULED:
			return waitForElementVisible(scheduledState).getText();
		case SUCCESSFUL:
			return waitForElementVisible(successfulState).getText();
		}
		return null;
	}
	
	public String getStateNumber(DISCOverviewProjectStates state) throws InterruptedException {
		switch (state) {
		case FAILED:
			waitForElementVisible(failedStateNumber);
			waitForStateNumber(failedStateNumber);
			return failedStateNumber.getText();
		case RUNNING:
			waitForElementVisible(runningStateNumber);
			waitForStateNumber(runningStateNumber);
			return runningStateNumber.getText();
		case SCHEDULED:
			waitForElementVisible(scheduledStateNumber);
			waitForStateNumber(scheduledStateNumber);
			return scheduledStateNumber.getText();
		case SUCCESSFUL:
			waitForElementVisible(successfulStateNumber);
			waitForStateNumber(successfulStateNumber);
			return successfulStateNumber.getText();
		}
		return null;
	}

	public boolean assertOverviewStateNumber(DISCOverviewProjectStates state, int number)
			throws InterruptedException {
		assertTrue(state.getOption().equalsIgnoreCase(getState(state)));
		return getStateNumber(state).equals(String.valueOf(number));
	}
}
