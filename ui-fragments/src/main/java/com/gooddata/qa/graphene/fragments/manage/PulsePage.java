package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static org.openqa.selenium.By.tagName;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class PulsePage extends AbstractFragment {

    @FindBy(css = ".regularContent .todaySection div")
    private List<WebElement> todayActivityRecords;

    public void openUptoDateReport(String report) {
        waitForCollectionIsNotEmpty(todayActivityRecords)
            .stream()
            .map(this::getReportLink)
            .filter(e -> e.isPresent())
            .filter(e -> report.equals(e.get().findElement(tagName("span")).getAttribute("title")))
            .findFirst()
            .get()
            .orElseThrow(() -> new NoSuchElementException("Cannot find: " + report))
            .click();
        waitForAnalysisPageLoaded(browser);
    }

    private Optional<WebElement> getReportLink(WebElement record) {
        return record.findElements(BY_LINK)
            .stream()
            .filter(e -> e.getAttribute("href").contains("|analysisPage"))
            .findFirst();
    }
}
