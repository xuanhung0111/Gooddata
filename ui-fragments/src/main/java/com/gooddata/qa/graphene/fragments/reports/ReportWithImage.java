package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ReportWithImage extends AbstractReport {

    @FindBy(xpath = "//div[@id='gridContainerTab']//div[contains(@class,'image')]//img")
    private List<WebElement> images;

    public void verifyImageOnReport() {
	String noImageURL = "/images/2011/il-no-image.png";
	for (WebElement element : images) {
	    assertFalse(waitForElementVisible(element).getAttribute("src")
		    .endsWith(noImageURL), "Image is NOT rendered in report!");
	}
    }

    public void verifyIfImageSFDCOnReport() {
	ArrayList<String> urls = getImagesLinks();
	for (int i = 0; i < urls.size(); i++) {
	    browser.get(urls.get(i));
	    assertFalse((browser.getCurrentUrl().endsWith("not+found.") || browser.getCurrentUrl().contains("errorCode")),
		    "SFDC image is NOT rendered in report!");
	}
    }

    private ArrayList<String> getImagesLinks() {
	ArrayList<String> imageLinks = new ArrayList<String>();
	for (WebElement element : images) {
	    imageLinks.add(waitForElementVisible(element).getAttribute("src"));
	}
	return imageLinks;
    }
}
