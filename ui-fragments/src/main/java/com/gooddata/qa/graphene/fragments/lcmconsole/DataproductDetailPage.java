package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;

public class DataproductDetailPage extends AbstractFragment {

    private static final By DATA_PRODUCTS_ID = By.id("data-products-detail");

    @FindBy(className = "segment-list")
    private WebElement segmentList;

    public static DataproductDetailPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DataproductDetailPage.class, waitForElementVisible(DATA_PRODUCTS_ID, searchContext));
    }

    public DomainSegmentFragment getDomainSegment(String domainId, String segmentId) {
        final WebElement domainSegment = waitForElementVisible(segmentList).findElement(getIdSelectorForDomainSegment(domainId, segmentId));
        return DomainSegmentFragment.getInstance(domainSegment);
    }

    public SegmentDetailPage openDomainSegment(String domainId, String segmentId) {
        waitForElementVisible(segmentList.findElement(getIdSelectorForDomainSegment(domainId, segmentId))).click();
        return SegmentDetailPage.getInstance(browser);
    }

    public boolean isDomainPresent(String domainId) {
        return ElementUtils.isElementPresent(getXpathSelectorForDomainColumn(domainId), segmentList);
    }

    public boolean isDomainSegmentsPresent(String domainId, String segmentId) {
        if (!isDomainPresent(domainId)) {
            return false;
        }
        final WebElement domainColumn = segmentList.findElement(getXpathSelectorForDomainColumn(domainId));
        return ElementUtils.isElementPresent(getIdSelectorForDomainSegment(domainId, segmentId), domainColumn);
    }

    public DomainProjectsDialog openProjectList(String domainId) {
        waitForElementVisible(segmentList.findElement(getCssSelectorForProjectList(domainId))).click();
        return DomainProjectsDialog.getInstance(browser);
    }

    public int getNumberOfProjectsInDomain(String domainId) {
        final WebElement domainColumn = segmentList.findElement(getXpathSelectorForDomainColumn(domainId));
        final WebElement projectsHeader = domainColumn.findElement(getXpathSelectorForNumberOfProject());
        final Matcher projectsMatcher = Pattern.compile("([0-9]+) projects").matcher(projectsHeader.getText());

        // wait for the number of projects to load, because its value is updated after the fragment is loaded
        Graphene.waitGui().until(browser -> projectsMatcher.reset(projectsHeader.getText()).matches());

        return Integer.parseInt(projectsMatcher.group(1));
    }

    private By getXpathSelectorForNumberOfProject() {
        return By.xpath("//h4[contains(.,'projects')]");
    }

    private By getXpathSelectorForDomainColumn(String domainId) {
        return By.xpath(format("//h3[contains(., '%s')]/parent::div/parent::div", domainId.toLowerCase()));
    }

    private By getIdSelectorForDomainSegment(String domainId, String segmentId) {
        return By.id(format("domain-segment-%s-%s", domainId.toLowerCase(), segmentId.toLowerCase()));
    }

    private By getCssSelectorForProjectList(String domainId) {
        return By.cssSelector(format("[href = '#/domains/%s/projects']", domainId));
    }
}
