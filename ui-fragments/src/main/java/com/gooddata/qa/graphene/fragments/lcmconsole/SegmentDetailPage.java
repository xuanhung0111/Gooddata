package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.web.util.UriTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

public class SegmentDetailPage extends AbstractFragment {

    private static final By SEGMENT_DETAIL_ID = By.id("segment-detail");
    private static final By CHANGE_MASTER_PROJECT_CLASS = By.className("s-change_master_project");
    private static final String URI_TEMPLATE = DataproductsPage.URI + "/#/dataproducts/{dataProductId}/segments/{segmentId}/domains/{domainId}";

    @FindBy(className = "segment-detail-header-actions")
    private WebElement contextMenu;

    @FindBy(className = "gd-input-field")
    private WebElement searchInputField;

    @FindBy(className = "table-row")
    private WebElement tableRow;

    @FindBy(className = "table-container")
    private WebElement tableContainer;

    @FindBy(xpath = "//table[@class='segment-detail-table']//tr[2]/td[2]")
    private WebElement masterProjectId;

    @FindBy(css = ".table-filter h3")
    private WebElement clientCountHeader;

    public static String getUri(String dataProductId, String segmentId, String domainId) {
        return new UriTemplate(URI_TEMPLATE).expand(dataProductId, segmentId, domainId).toString();
    }

    public static SegmentDetailPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SegmentDetailPage.class, waitForElementVisible(SEGMENT_DETAIL_ID, searchContext));
    }

    public MasterProjectDialog openMasterProjectDialog() {
        waitForElementVisible(contextMenu).click();
        waitForElementVisible(CHANGE_MASTER_PROJECT_CLASS, browser).click();
        return MasterProjectDialog.getInstance(browser);
    }

    public ClientDetailDialog openClientDetailDialog(String clientId) {
        assertTrue(isClientPresent(clientId), format("Cannot open client detail, client '%s' not found", clientId));
        waitForElementVisible(getCssSelectorForClient(clientId), tableContainer).click();
        return ClientDetailDialog.getInstance(browser);
    }

    public void filterClients(String searchValue) {
        waitForElementVisible(searchInputField).sendKeys(searchValue);
    }

    public boolean isClientPresent(String clientId) {
        waitForElementVisible(tableRow);
        return isElementPresent(getCssSelectorForClient(clientId), tableContainer);
    }

    public void waitForClientIsNotPresent(String clientId) {
        waitForElementNotPresent(getCssSelectorForClient(clientId));
    }

    public String getMasterProjectId() {
        return waitForElementVisible(masterProjectId).getText();
    }

    public int getClientCount() {
        waitForElementVisible(tableRow);
        final String clientsString = waitForElementVisible(clientCountHeader).getText();
        final Matcher clientsMatcher = Pattern.compile("Clients \\(([0-9]+)\\)").matcher(clientsString);
        assertTrue(clientsMatcher.matches(), "Cannot get client count, header pattern not matched");

        return Integer.valueOf(clientsMatcher.group(1));
    }

    public String getClientProjectId(String clientId) {
        waitForElementVisible(tableRow);
        return waitForElementVisible(getXpathSelectorForClientProject(clientId), tableContainer).getText();
    }

    private By getCssSelectorForClient(String clientId) {
        return By.cssSelector(format("[title = '%s']", clientId));
    }

    private By getXpathSelectorForClientProject(String clientId) {
        return By.xpath(format("//span[@title='%s']/following-sibling::span", clientId));
    }
}
