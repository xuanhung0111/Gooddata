package com.gooddata.qa.graphene.tools;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;

/**
 * This is a helper class to delete custom fields in zendesk account
 * 
 * Marked with brokenTest group so that it's not executed as part of testng-tools.xml
 * 
 * @author michal.vanco@gooddata.com
 *
 */
@Test(groups = { "tools", "brokenTest" }, description = "Tools tests")
public class DeleteZendeskCustomFields extends AbstractTest {
	
	@BeforeClass
	public void initStartPage() {
		startPage = "ticket_fields";
		host = "gooddataqaent.zendesk-staging.com";
	}
	
	/**
	 * host=gooddataqaent.zendesk-staging.com (or other) have to be used 
	 */
	@Test
	public void deleteZendeskCustomFields() {
		//login
		waitForElementVisible(By.id("user_email"));
		browser.findElement(By.id("user_email")).sendKeys("qa@gooddata.com");
		browser.findElement(By.id("user_password")).sendKeys("changeit321");
		browser.findElement(By.xpath("//input[@name='commit']")).click();
		waitForElementVisible(By.xpath("//h2[text()='Ticket fields']"));
		
		List<WebElement> fields = browser.findElements(By.xpath("//div[@id='fields']/div[@class='item']"));
		int i = 0;
		for (WebElement field : fields) {
			String title = field.findElement(By.xpath("div/div/span[@class='title']")).getText();
			if (title.contains("testfield")) {
				System.out.println("About to delete custom field " + title);
				i++;
				field.findElement(By.xpath("div[@class='item-actions']/a[@class='edit_this']")).click();
				waitForElementVisible(By.xpath("//a[text()='Delete']"));
				browser.findElement(By.xpath("//a[text()='Delete']")).click();
				Alert javascriptAlert = browser.switchTo().alert();
				javascriptAlert.accept();
				waitForElementVisible(By.xpath("//h2[text()='Ticket fields']"));
			}
		}
		System.out.println("Deleted " + i + " custom fields");
		
	}
	
}
