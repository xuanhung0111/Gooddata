package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;

public class DomainsPage extends AbstractFragment {

    public static final String URI = "/lcmconsole/#/domains";
    private static final By DOMAINS_CLASS = By.className("content");

    @FindBy(className = "domains-list")
    private WebElement domainsList;

    public static final DomainsPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DomainsPage.class, waitForElementVisible(DOMAINS_CLASS, searchContext));
    }

    public DomainUsersDialog openDomainUsersDialog(String domainId) {
        waitForElementVisible(domainsList).findElement(getCssSelectorForDomainRef(domainId)).click();
        return DomainUsersDialog.getInstance(browser);
    }

    public boolean isDomainPresent(String domainId) {
        return ElementUtils.isElementPresent(getCssSelectorForDomainRef(domainId), waitForElementVisible(domainsList));
    }

    private By getCssSelectorForDomainRef(String domainId) {
        return By.cssSelector(format("[href = '#/domains/%s']", domainId));
    }
}
