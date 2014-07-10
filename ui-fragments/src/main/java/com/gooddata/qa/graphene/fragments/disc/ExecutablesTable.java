package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class ExecutablesTable extends AbstractTable {

	private final static By BY_EXECUTABLE_PATH = By.xpath("//span[@class='executable-path']");
	private final static By BY_EXECUTABLE = By.cssSelector(".executable-title-cell .executable");

	private final static String XPATH_EXECUTABLE = "//tr[${index}]/td[@class='executable-schedule-cell']/a[text()='Schedule']";

	public void assertExecutablesList(DISCProcessTypes processType, List<String> executables) {
		String executablePath = String.format("/%s/", processType.getProcessTypeExecutable());
		for (int i = 0; i < this.getNumberOfRows(); i++) {
			Assert.assertEquals(getRow(i).findElement(BY_EXECUTABLE_PATH).getText(), executablePath);
			Assert.assertEquals(executables.get(i), getRow(i).findElement(BY_EXECUTABLE).getText());
		}
	}

	public WebElement getExecutableScheduleLink(String executableName) {
		for (int i = 0; i < this.getNumberOfRows(); i++) {
			if (executableName.equals(getRow(i).findElement(BY_EXECUTABLE).getText()))
				return getRow(i).findElement(
						By.xpath(XPATH_EXECUTABLE.replace("${index}", String.valueOf(i + 1))));
		}
		return null;
	}
}
