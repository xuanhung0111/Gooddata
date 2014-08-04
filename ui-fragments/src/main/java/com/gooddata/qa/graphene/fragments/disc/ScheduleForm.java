package com.gooddata.qa.graphene.fragments.disc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ScheduleForm extends AbstractFragment {

	protected static By BY_PARAMETER_VALUE = By.cssSelector(".param-value input");
	protected static By BY_PARAMETER_NAME = By.cssSelector(".param-name input");
	protected static By BY_PARAMETER_SHOW_SECURE_VALUE = By
			.cssSelector(".param-show-secure-value input");
	protected static By BY_PARAMETER_REMOVE_ACTION = By
			.cssSelector(".param-action a[title='Remove this parameter.']");

	@FindBy(css = ".ait-new-schedule-process-select-btn")
	protected WebElement selectProcessForNewSchedule;

	@FindBy(css = ".ait-new-schedule-executable-select-btn")
	protected WebElement selectExecutableForNewSchedule;

	@FindBy(css = ".ait-schedule-cron-select-btn")
	protected WebElement cronPicker;

	@FindBy(css = ".cron-editor-line select.select-small")
	protected WebElement selectDayInWeek;

	@FindBy(xpath = "//span[@class='option-content everyDay everyWeek cron-editor-line']/select")
	protected WebElement selectHourInDay;

	@FindBy(xpath = "//span[@class='option-content everyHour everyDay everyWeek cron-editor-line']/select")
	protected WebElement selectMinuteInHour;

	@FindBy(css = ".ait-schedule-cron-user-value input.input-text")
	protected WebElement cronExpression;

	@FindBy(css = ".userCron .bubble-overlay")
	protected WebElement cronExpressionErrorBubble;

	@FindBy(xpath = "//div[@class='schedule-params-actions']//a[text()='Add parameter']")
	protected WebElement addParameterLink;

	@FindBy(xpath = "//div[@class='schedule-params-actions']//a[text()='Add secure parameter']")
	protected WebElement addSecureParameterLink;

	@FindBy(xpath = "//button[contains(@class, 'ait-new-schedule-confirm-btn') and text()='Schedule']")
	protected WebElement confirmScheduleButton;

	@FindBy(xpath = "//a[contains(@class, 'ait-new-schedule-cancel-btn') and text()='Cancel']")
	protected WebElement cancelScheduleButton;

	@FindBy(css = ".schedule-param")
	protected List<WebElement> parameters;

	public void selectProcess(String processName) {
		waitForElementVisible(selectProcessForNewSchedule);
		Select select = new Select(selectProcessForNewSchedule);
		select.selectByVisibleText(processName);
	}

	public void selectExecutable(String executableName) {
		waitForElementVisible(selectExecutableForNewSchedule);
		Select select = new Select(selectExecutableForNewSchedule);
		select.selectByVisibleText(executableName);
	}

	public void selectHourInDay(Pair<String, List<String>> cronTime) {
		if (cronTime.getValue().get(1) != null) {
			waitForElementVisible(selectHourInDay);
			Select select = new Select(selectHourInDay);
			select.selectByVisibleText(cronTime.getValue().get(1));
		}
	}

	public void selectMinuteInHour(Pair<String, List<String>> cronTime) {
		if (cronTime.getValue().get(0) != null) {
			Calendar existingTime = Calendar.getInstance();
			SimpleDateFormat existingSdf = new SimpleDateFormat("mm");
			int existingMinute = Integer.valueOf(existingSdf.format(existingTime.getTime())) + 2;
			existingMinute = existingMinute >= 60 ? 2 : existingMinute;
			waitForElementVisible(selectMinuteInHour);
			Select select = new Select(selectMinuteInHour);
			select.selectByValue(cronTime.getValue().get(0)
					.replace("${minute}", String.valueOf(existingMinute)));
		}
	}

	public void selectCron(Pair<String, List<String>> cronTime) throws InterruptedException {
		Select selectCron = new Select(cronPicker);
		try {
			waitForElementVisible(selectCron);
			if (selectCron.getFirstSelectedOption().equals(
					ScheduleCronTimes.CRON_EXPRESSION.getCronTime()))
				Thread.sleep(1000);
		} catch (NoSuchElementException e) {
			System.out.println("Wait for selected element...");
			Thread.sleep(1000);
		} finally {
			System.out.println("Selected cron time: "
					+ selectCron.getFirstSelectedOption().getText());
		}
		selectCron.selectByVisibleText(cronTime.getKey());
		System.out.println("Selected cron time after change: "
				+ selectCron.getFirstSelectedOption().getText());
		if (cronTime.getValue() != null) {
			if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYWEEK.getCronTime())) {
				if (cronTime.getValue().get(2) != null) {
					Select selectWeek = new Select(selectDayInWeek);
					selectWeek.selectByVisibleText(cronTime.getValue().get(2));
				}
				selectHourInDay(cronTime);
				selectMinuteInHour(cronTime);
			}
			if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYDAY.getCronTime())) {
				selectHourInDay(cronTime);
				selectMinuteInHour(cronTime);
			}
			if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYHOUR.getCronTime())) {
				selectMinuteInHour(cronTime);
			}
			if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EXPRESSION.getCronTime())) {
				if (cronTime.getValue().get(0) != null) {
					if (cronExpression.getAttribute("value").isEmpty())
						Thread.sleep(1000);
					System.out.println("Cron expression value: "
							+ cronExpression.getAttribute("value"));
					waitForElementVisible(cronExpression).clear();
					if (!cronExpression.getAttribute("value").isEmpty())
						Thread.sleep(1000);
					System.out.println("Cron expression value after clearing: "
							+ cronExpression.getAttribute("value"));
					cronExpression.sendKeys(cronTime.getValue().get(0));
					assertEquals(cronTime.getValue().get(0),
							cronExpression.getAttribute("value"));
					System.out.println("Cron expression is set to... "
							+ cronExpression.getAttribute("value"));
				}
			}
		}
	}

	public WebElement getParameter(int parameterIndex) {
		return parameters.get(parameterIndex);
	}

	public void addParameters(Map<String, List<String>> newParameters) {
		int index = parameters.size();
		for (Entry<String, List<String>> newParameter : newParameters.entrySet()) {
			if (newParameter.getValue().get(0).equals("secure")) {
				System.out.println("Add new secure parameter: " + newParameter.getKey());
				addSecureParameterLink.click();
			} else {
				System.out.println("Add new parameter: " + newParameter.getKey());
				addParameterLink.click();
			}
			waitForElementVisible(parameters.get(index));
			parameters.get(index).findElement(BY_PARAMETER_NAME).sendKeys(newParameter.getKey());
			parameters.get(index).findElement(BY_PARAMETER_VALUE)
					.sendKeys(newParameter.getValue().get(1));
			if (newParameter.getValue().get(0).equals("secure")) {
				parameters.get(index).findElement(BY_PARAMETER_SHOW_SECURE_VALUE).click();
				assertEquals("text", parameters.get(index).findElement(BY_PARAMETER_VALUE)
						.getAttribute("type"));
				assertEquals(newParameter.getValue().get(1), parameters.get(index)
						.findElement(BY_PARAMETER_VALUE).getAttribute("value"));
			}
			index++;
		}
	}

	public void createNewSchedule(String processName, String executableName,
			Pair<String, List<String>> cronTime, Map<String, List<String>> parameters,
			boolean isConfirmed) throws InterruptedException {
		waitForElementVisible(getRoot());
		if (processName != null)
			selectProcess(processName);
		if (executableName != null)
			selectExecutable(executableName);
		if (cronTime != null)
			selectCron(cronTime);
		if (parameters != null)
			addParameters(parameters);
		if (isConfirmed)
			waitForElementVisible(confirmScheduleButton).click();
		else
			waitForElementVisible(cancelScheduleButton).click();
	}

	public WebElement getConfirmScheduleButton() {
		return confirmScheduleButton;
	}

	public void checkScheduleWithIncorrectCron(Pair<String, List<String>> incorrectCronTime,
			WebElement confirmButton) throws InterruptedException {
		if (incorrectCronTime != null)
			selectCron(incorrectCronTime);
		waitForElementVisible(confirmButton).click();
		waitForElementVisible(cronExpressionErrorBubble);
		System.out.println("cron exepression: " + cronExpression.getAttribute("class"));
		System.out.println("cron bubble: " + cronExpressionErrorBubble.getText());
		assertTrue(cronExpression.getAttribute("class").contains("has-error"));
		assertTrue(cronExpressionErrorBubble.getText().equals(
				"Inserted cron format is invalid. Please verify and try again."));
	}
}
