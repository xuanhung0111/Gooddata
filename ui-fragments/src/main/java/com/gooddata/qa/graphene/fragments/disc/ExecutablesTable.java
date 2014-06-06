package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class ExecutablesTable extends AbstractTable {
	
	private final static By	BY_EXECUTABLE_PATH = By.xpath("//span[@class='executable-path']");
	private final static By	BY_EXECUTABLE = By.cssSelector(".executable-title-cell .executable");

	@FindBy(css = ".executable-title-cell .executable-path")
	protected WebElement executablePath;
	
	@FindBy(css = ".executable-title-cell .executable")
	protected WebElement executable;
	
	public void assertExecutablesList(DISCProcessTypes processType, List<String> executables) {
		for(int i = 0; i < this.getNumberOfRows(); i++) {
			if(processType == DISCProcessTypes.GRAPH)
				Assert.assertEquals(getRow(i).findElement(BY_EXECUTABLE_PATH).getText(), "/graph/");
			else if(processType == DISCProcessTypes.RUBY)
				Assert.assertEquals(getRow(i).findElement(BY_EXECUTABLE_PATH).getText(), "/script/");
			Assert.assertEquals(executables.get(i), getRow(i).findElement(BY_EXECUTABLE).getText());
		}
	}
}
