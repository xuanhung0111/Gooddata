package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ReportWithImage extends AbstractDashboardReport {

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
        for (String url : urls) {
            System.out.println("Going to check image on url: " + url);
            browser.get(url);
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
