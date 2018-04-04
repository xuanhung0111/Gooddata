package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;

public class DomainsPage extends AbstractFragment {

    private static final By DOMAINS_CLASS = By.className("content");

    public static DomainsPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DomainsPage.class, waitForElementVisible(DOMAINS_CLASS, searchContext));
    }

    public DomainUsersDialog openDomainUsersDialog(String domainId) {
        waitForElementVisible(getCssSelectorForDomainRef(domainId), getRoot()).click();
        return DomainUsersDialog.getInstance(browser);
    }

    public boolean isDomainPresent(String domainId) {
        waitForElementVisible(getCssSelectorForDomainRef(domainId), getRoot());
        return ElementUtils.isElementPresent(getCssSelectorForDomainRef(domainId), getRoot());
    }

    private By getCssSelectorForDomainRef(String domainId) {
        return By.cssSelector(format("[href = '#/domains/%s']", domainId));
    }
}
