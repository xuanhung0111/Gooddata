package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DomainSegmentFragment extends AbstractFragment {

    @FindBy(className = "domain-detail-table")
    private WebElement table;

    @FindBy(className = "domain-segment")
    private WebElement domainSegment;

    @FindBy(xpath = "//span[contains(.,'clients')]")
    private WebElement numberOfClientsSpan;

    public static final DomainSegmentFragment getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DomainSegmentFragment.class, waitForElementVisible(By.xpath("/*"), searchContext));
    }

    public String getMasterProjectName() {
        return waitForElementVisible(table).findElements(By.className("table-value")).get(0).getText();
    }

    public int getClientsCount() {
        final String numberOfClientsText = numberOfClientsSpan.getText();
        return Integer.parseInt(numberOfClientsText.split(" ")[0]);
    }

}