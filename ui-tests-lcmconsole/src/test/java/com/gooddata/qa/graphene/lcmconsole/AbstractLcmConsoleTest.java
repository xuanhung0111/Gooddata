package com.gooddata.qa.graphene.lcmconsole;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.lcmconsole.DataproductsPage;
import com.gooddata.qa.graphene.fragments.lcmconsole.DomainsPage;
import org.json.JSONException;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AbstractLcmConsoleTest extends AbstractUITest {

    @Test
    protected void signIn() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }
}
