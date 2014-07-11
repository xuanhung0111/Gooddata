package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ScheduleForm extends AbstractFragment {

	protected static String XPATH_SELECT_OPTION = "//span/select/option[text()='${option}']";
	protected static String XPATH_CRON_OPTION = "//option[text()='${option}']";
	protected static String XPATH_CRON_HOUR_OPTION = "//span[@class='option-content everyDay everyWeek cron-editor-line']//option[text()='${option}']";
	protected static String XPATH_CRON_MINUTE_OPTION = "//span[@class='option-content everyHour everyDay everyWeek cron-editor-line']//option[text()='${option}']";
	protected static String XPATH_PARAMETER_VALUE = "//div[@class='ember-view schedule-params']/div[${index}]//div[@class='param-value']//input";
	protected static String XPATH_PARAMETER_NAME = "//div[@class='ember-view schedule-params']/div[${index}]//div[@class='param-name']/input";
	protected static String XPATH_EXECUTABLE_SELECTION_IN_BROKEN_SCHEDULE = "//div[@class='broken-schedule-title']//select/option[text()='${option}']";

	@FindBy(xpath = "//div[@class='form-field']/label[text()='Process']/..")
	protected WebElement selectProcess;

	@FindBy(xpath = "//div[@class='form-field']/label[text()='Executable']/..")
	protected WebElement selectExecutable;

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

	@FindBy(xpath = "//div[contains(@class, 'schedule-form')]/button[text()='Schedule']")
	protected WebElement confirmScheduleButton;

	@FindBy(css = ".schedule-param")
	protected List<WebElement> parameters;

	public void selectProcess(String processName) {
		selectProcess.findElement(By.xpath(XPATH_SELECT_OPTION.replace("${option}", processName)))
				.click();
	}

	public void selectExecutable(String executableName) {
		selectExecutable.findElement(
				By.xpath(XPATH_SELECT_OPTION.replace("${option}", executableName))).click();
	}

	public void selectHourInDay(Pair<String, List<String>> cronTime) {
		if (cronTime.getValue().get(1) != null)
			selectHourInDay.findElement(
					By.xpath(XPATH_CRON_HOUR_OPTION
							.replace("${option}", cronTime.getValue().get(1)))).click();
	}

	public void selectMinuteInHour(Pair<String, List<String>> cronTime) {
		if (cronTime.getValue().get(0) != null)
			selectMinuteInHour.findElement(
					By.xpath(XPATH_CRON_MINUTE_OPTION.replace("${option}",
							cronTime.getValue().get(0)))).click();
	}

	public void selectCron(Pair<String, List<String>> cronTime) throws InterruptedException {
		Select select = new Select(cronPicker);
		try {
			waitForElementVisible(select);
			if (select.getFirstSelectedOption().equals(
					ScheduleCronTimes.CRON_EXPRESSION.getCronTime()))
				Thread.sleep(1000);
		} catch (NoSuchElementException e) {
			System.out.println("Wait for selected element...");
			Thread.sleep(1000);
		} finally {
			System.out.println("Selected cron time: " + select.getFirstSelectedOption().getText());
		}
		select.selectByVisibleText(cronTime.getKey());
		System.out.println("Selected cron time after change: "
				+ select.getFirstSelectedOption().getText());
		if (cronTime.getValue() != null) {
			if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EVERYWEEK.getCronTime())) {
				if (cronTime.getValue().get(2) != null)
					selectDayInWeek.findElement(
							By.xpath(XPATH_CRON_OPTION.replace("${option}", cronTime.getValue()
									.get(2)))).click();
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
					Assert.assertEquals(cronTime.getValue().get(0),
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
		int index = parameters.size() + 1;
		for (Entry<String, List<String>> newParameter : newParameters.entrySet()) {
			if (newParameter.getValue().get(0).equals("secure")) {
				System.out.println("Add new secure parameter: " + newParameter.getKey());
				addSecureParameterLink.click();
			} else {
				System.out.println("Add new parameter: " + newParameter.getKey());
				addParameterLink.click();
			}
			waitForElementVisible(getRoot().findElement(
					By.xpath(XPATH_PARAMETER_NAME.replace("${index}",
							String.valueOf(index))))).sendKeys(newParameter.getKey());
			waitForElementVisible(getRoot().findElement(
					By.xpath(XPATH_PARAMETER_VALUE.replace("${index}",
							String.valueOf(index))))).sendKeys(
					newParameter.getValue().get(1));
			index++;
		}
	}

	public void createNewSchedule(String processName, String executableName,
			Pair<String, List<String>> cronTime, Map<String, List<String>> parameters)
			throws InterruptedException {
		if (processName != null)
			selectProcess(processName);
		if (executableName != null)
			selectExecutable(executableName);
		if (cronTime != null)
			selectCron(cronTime);
		if (parameters != null)
			addParameters(parameters);
		confirmScheduleButton.click();
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
		Assert.assertTrue(cronExpression.getAttribute("class").contains("has-error"));
		Assert.assertTrue(cronExpressionErrorBubble.getText().equals(
				"Inserted cron format is invalid. Please verify and try again."));
	}
}
