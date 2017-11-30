package com.gooddata.qa.graphene.lcmconsole.tests;

import com.gooddata.qa.graphene.fragments.lcmconsole.DomainUsersDialog;
import com.gooddata.qa.graphene.fragments.lcmconsole.DomainsPage;
import com.gooddata.qa.graphene.lcmconsole.AbstractLcmConsoleTest;
import org.json.JSONException;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_1;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_2;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.DOMAIN_ID_3;
import static com.gooddata.qa.graphene.lcmconsole.NamingConstants.USER_1_LOGIN;
import static org.testng.AssertJUnit.assertTrue;

public class DomainsTest extends AbstractLcmConsoleTest {

    private DomainsPage domainsPage;

    @Test(dependsOnMethods = {"signIn"})
    public void initTest() throws JSONException {
        domainsPage = initDataproductPage().openDomainsPage();
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDomainsExist() {
        assertTrue(domainsPage.isDomainPresent(DOMAIN_ID_1));
        assertTrue(domainsPage.isDomainPresent(DOMAIN_ID_2));
        assertTrue(domainsPage.isDomainPresent(DOMAIN_ID_3));
    }

    @Test(dependsOnMethods = {"initTest"})
    public void testDomainUsers() {
        DomainUsersDialog domainUsersDialog = domainsPage.openDomainUsersDialog(DOMAIN_ID_1);

        assertTrue(USER_1_LOGIN + " should be presented in the list", domainUsersDialog.isUserPresent(USER_1_LOGIN));
    }
}
